package com.example.platform

import com.example.data.local.LocalCacheRepository
import com.example.data.remote.ChatResponseResult
import com.example.data.remote.GeminiHubService

data class RoutedAiExecutionResult(
    val text: String,
    val citations: List<String>,
    val primaryModelAttempted: String,
    val actualModelUsed: String,
    val usedFallbackProvider: Boolean,
    val responseTimeMs: Long,
    val estimatedTokens: Int
)

/**
 * Centralized AI Model Router with Automatic Primary -> Secondary Provider Fallback,
 * input validation, rate-limit guard, and live telemetry recording.
 */
class AiModelRouter(
    private val geminiService: GeminiHubService,
    private val localRepo: LocalCacheRepository
) {
    private var lastRequestTimestampMs: Long = 0L

    fun validateInputAndRateLimit(prompt: String, attachmentMimeType: String?): Result<Unit> {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() && attachmentMimeType.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Prompt or file attachment cannot be empty."))
        }
        if (trimmed.length > 40000) {
            return Result.failure(IllegalArgumentException("Input exceeds maximum allowed length (40,000 characters)."))
        }
        if (!attachmentMimeType.isNullOrBlank()) {
            val allowedPrefixes = listOf("image/", "application/pdf", "text/", "audio/", "application/json", "application/octet-stream")
            val isAllowed = allowedPrefixes.any { attachmentMimeType.startsWith(it, ignoreCase = true) }
            if (!isAllowed) {
                return Result.failure(IllegalArgumentException("Unsupported or unsafe file MIME type: $attachmentMimeType"))
            }
        }
        val now = System.currentTimeMillis()
        if (now - lastRequestTimestampMs < 350L) {
            return Result.failure(IllegalStateException("Rate limit protection triggered: Please wait a moment before sending another request."))
        }
        lastRequestTimestampMs = now
        return Result.success(Unit)
    }

    suspend fun executeRoutedTask(
        feature: PlatformFeature,
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        userSystemContext: String = "",
        inlineMimeType: String? = null,
        inlineBase64Data: String? = null,
        forceGrounding: Boolean = false,
        onChunk: (String) -> Unit
    ): Result<RoutedAiExecutionResult> {
        val validation = validateInputAndRateLimit(prompt, inlineMimeType)
        if (validation.isFailure) {
            return Result.failure(validation.exceptionOrNull()!!)
        }

        val taskType = feature.taskType
        val primaryModel = taskType.primaryModelId
        val fallbackModel = taskType.fallbackModelId
        val enableSearch = forceGrounding || taskType.usesSearchGrounding
        val fullSystemInstruction = buildString {
            if (userSystemContext.isNotBlank()) {
                append(userSystemContext).append("\n\n")
            }
            append("Active Feature Module: ${feature.name} (v${feature.version}, Category: ${feature.category.displayName}).\n")
            append(feature.systemInstruction)
        }

        val startTime = System.currentTimeMillis()

        // 1. Attempt Primary AI Provider / Model
        val primaryResult: Result<ChatResponseResult> = geminiService.streamChatCompletion(
            modelId = primaryModel,
            history = history,
            prompt = prompt,
            systemInstructionText = fullSystemInstruction,
            inlineMimeType = inlineMimeType,
            inlineBase64Data = inlineBase64Data,
            enableGoogleSearch = enableSearch,
            onChunk = onChunk
        )

        if (primaryResult.isSuccess) {
            val res = primaryResult.getOrNull()!!
            val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(110L)
            val estTokens = ((prompt.length + res.text.length) / 4).coerceAtLeast(64)
            localRepo.recordTelemetryEvent(
                featureId = feature.featureId,
                responseTimeMs = elapsed,
                isError = false,
                usedFallback = false,
                estimatedTokens = estTokens
            )
            return Result.success(
                RoutedAiExecutionResult(
                    text = res.text,
                    citations = res.citations,
                    primaryModelAttempted = primaryModel,
                    actualModelUsed = primaryModel,
                    usedFallbackProvider = false,
                    responseTimeMs = elapsed,
                    estimatedTokens = estTokens
                )
            )
        }

        // 2. Primary failed -> Trigger Secondary Fallback Provider / Model automatically
        if (fallbackModel.isNotBlank() && fallbackModel != primaryModel && !fallbackModel.startsWith("android-")) {
            val fallbackResult = geminiService.streamChatCompletion(
                modelId = fallbackModel,
                history = history,
                prompt = prompt,
                systemInstructionText = fullSystemInstruction,
                inlineMimeType = inlineMimeType,
                inlineBase64Data = inlineBase64Data,
                enableGoogleSearch = false,
                onChunk = onChunk
            )

            if (fallbackResult.isSuccess) {
                val res = fallbackResult.getOrNull()!!
                val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(140L)
                val estTokens = ((prompt.length + res.text.length) / 4).coerceAtLeast(64)
                localRepo.recordTelemetryEvent(
                    featureId = feature.featureId,
                    responseTimeMs = elapsed,
                    isError = false,
                    usedFallback = true,
                    estimatedTokens = estTokens
                )
                localRepo.recordAuditLog(
                    actor = "AI_ROUTER",
                    action = "FALLBACK_PROVIDER_ACTIVATED",
                    targetId = feature.featureId,
                    details = "Primary ($primaryModel) unavailable; routed seamlessly to Secondary ($fallbackModel)."
                )
                return Result.success(
                    RoutedAiExecutionResult(
                        text = res.text,
                        citations = res.citations,
                        primaryModelAttempted = primaryModel,
                        actualModelUsed = fallbackModel,
                        usedFallbackProvider = true,
                        responseTimeMs = elapsed,
                        estimatedTokens = estTokens
                    )
                )
            }
        }

        // 3. Both primary and secondary failed -> record error telemetry & return actionable message
        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(100L)
        localRepo.recordTelemetryEvent(
            featureId = feature.featureId,
            responseTimeMs = elapsed,
            isError = true,
            usedFallback = true,
            estimatedTokens = 0
        )
        val errMsg = primaryResult.exceptionOrNull()?.message
            ?: "Both Primary ($primaryModel) and Fallback ($fallbackModel) AI providers are currently unreachable. Please verify GEMINI_API_KEY in the Secrets panel."
        return Result.failure(IllegalStateException(errMsg))
    }
}
