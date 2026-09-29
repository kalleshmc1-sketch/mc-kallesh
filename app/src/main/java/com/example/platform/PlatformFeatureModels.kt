package com.example.platform

import com.example.data.model.SubscriptionPlan
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

/**
 * Semantic Version (MAJOR.MINOR.PATCH)
 * MAJOR: Large architecture or breaking changes
 * MINOR: New features
 * PATCH: Bug fixes, security fixes and small improvements
 */
data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<SemanticVersion> {

    override fun compareTo(other: SemanticVersion): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        return patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(raw: String): SemanticVersion {
            val cleaned = raw.trim().removePrefix("v").removePrefix("V")
            val parts = cleaned.split(".")
            val maj = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val pat = parts.getOrNull(2)?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
            return SemanticVersion(maj, min, pat)
        }

        fun isUpdateAvailable(current: String, latest: String): Boolean =
            parse(latest) > parse(current)

        fun isBelowMinimum(current: String, minimumSupported: String): Boolean =
            parse(current) < parse(minimumSupported)
    }
}

enum class FeatureStatus(val label: String) {
    ACTIVE("Active"),
    DISABLED("Disabled"),
    MAINTENANCE("Maintenance"),
    COMING_SOON("Coming Soon");

    companion object {
        fun fromName(value: String?): FeatureStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

enum class FeatureBadge(val label: String) {
    NONE(""),
    NEW("NEW"),
    BETA("BETA"),
    EXPERIMENTAL("EXPERIMENTAL");

    companion object {
        fun fromName(value: String?): FeatureBadge =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}

enum class AiToolCategory(val displayName: String, val groupName: String) {
    AI_CHAT("AI Chat", "Conversational & Search"),
    AI_SEARCH("AI Search", "Conversational & Search"),
    AI_RESEARCH("AI Research", "Conversational & Search"),
    AI_WEB_RESEARCH("AI Web Research", "Conversational & Search"),
    AI_IMAGE_GEN("AI Image Generation", "Visual & Media Studios"),
    AI_IMAGE_EDIT("AI Image Editing", "Visual & Media Studios"),
    AI_VIDEO_GEN("AI Video Generation", "Visual & Media Studios"),
    AI_AUDIO_GEN("AI Audio Generation", "Visual & Media Studios"),
    AI_VOICE_TTS("AI Voice / TTS", "Voice & Vision"),
    SPEECH_TO_TEXT("Speech to Text", "Voice & Vision"),
    AI_VISION("AI Vision", "Voice & Vision"),
    AI_OCR("AI OCR", "Voice & Vision"),
    AI_DOCUMENTS("AI Documents", "Documents & Files"),
    PDF_ANALYSIS("PDF Analysis", "Documents & Files"),
    FILE_ANALYSIS("File Analysis", "Documents & Files"),
    AI_CODING("AI Coding", "Engineering & Code"),
    CODE_DEBUGGING("Code Debugging", "Engineering & Code"),
    CODE_GENERATION("Code Generation", "Engineering & Code"),
    AI_STUDY_MODE("AI Study Mode", "Writing, Study & Language"),
    AI_WRITING("AI Writing", "Writing, Study & Language"),
    AI_TRANSLATION("AI Translation", "Writing, Study & Language"),
    AI_SUMMARIZATION("AI Summarization", "Writing, Study & Language"),
    AI_PRESENTATION("AI Presentation Generation", "Enterprise & Automation"),
    AI_DATA_ANALYSIS("AI Data Analysis", "Enterprise & Automation"),
    AI_AGENT("AI Agent", "Enterprise & Automation"),
    AI_AUTOMATION("AI Automation", "Enterprise & Automation"),
    AI_PRODUCTIVITY("AI Productivity", "Enterprise & Automation"),
    AI_WORKSPACE("AI Workspace", "Enterprise & Automation");

    companion object {
        fun fromName(value: String?): AiToolCategory =
            entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                    it.displayName.equals(value, ignoreCase = true)
            } ?: AI_PRODUCTIVITY
    }
}

enum class AiRouterTaskType(
    val label: String,
    val primaryModelId: String,
    val fallbackModelId: String,
    val defaultEndpoint: String,
    val usesSearchGrounding: Boolean = false
) {
    CHAT(
        label = "Conversational AI Router",
        primaryModelId = "gemini-3-flash-preview",
        fallbackModelId = "gemini-2.5-flash",
        defaultEndpoint = "/v1beta/models/gemini-3-flash-preview:generateContent"
    ),
    SEARCH_RESEARCH(
        label = "Search & Grounded Research Pipeline",
        primaryModelId = "gemini-3-flash-preview",
        fallbackModelId = "gemini-3.1-pro-preview",
        defaultEndpoint = "/v1beta/models/gemini-3-flash-preview:generateContent",
        usesSearchGrounding = true
    ),
    IMAGE_GEN(
        label = "Image Generation & Editing Pipeline",
        primaryModelId = "gemini-3.1-flash-image-preview",
        fallbackModelId = "imagen-3.0-generate-002",
        defaultEndpoint = "/v1beta/models/gemini-3.1-flash-image-preview:generateContent"
    ),
    VIDEO_GEN(
        label = "Veo 3 Video Generation Pipeline",
        primaryModelId = "veo-3.1-fast-generate-preview",
        fallbackModelId = "gemini-3-flash-preview",
        defaultEndpoint = "/v1beta/models/veo-3.1-fast-generate-preview:predictLongRunning"
    ),
    AUDIO_MUSIC(
        label = "Lyria 3 Audio & Music Pipeline",
        primaryModelId = "lyria-3-clip-preview",
        fallbackModelId = "lyria-3-pro-preview",
        defaultEndpoint = "/v1beta/models/lyria-3-clip-preview:generateContent"
    ),
    VOICE_TTS(
        label = "Neural Studio Speech Synthesis (TTS)",
        primaryModelId = "gemini-2.5-flash-preview-tts",
        fallbackModelId = "android-neural-tts-fallback",
        defaultEndpoint = "/v1beta/models/gemini-2.5-flash-preview-tts:generateContent"
    ),
    SPEECH_STT(
        label = "Speech Recognition & Transcription (STT)",
        primaryModelId = "gemini-3-flash-preview",
        fallbackModelId = "gemini-2.5-flash",
        defaultEndpoint = "/v1beta/models/gemini-3-flash-preview:generateContent"
    ),
    VISION_OCR(
        label = "Multimodal Vision & Optical Character Recognition",
        primaryModelId = "gemini-3-flash-preview",
        fallbackModelId = "gemini-3.1-pro-preview",
        defaultEndpoint = "/v1beta/models/gemini-3-flash-preview:generateContent"
    ),
    CODING(
        label = "Software Architecture & Code Intelligence",
        primaryModelId = "gemini-3.1-pro-preview",
        fallbackModelId = "gemini-3-flash-preview",
        defaultEndpoint = "/v1beta/models/gemini-3.1-pro-preview:generateContent"
    ),
    DOCUMENT_ANALYSIS(
        label = "Document, PDF & File Intelligence",
        primaryModelId = "gemini-3-flash-preview",
        fallbackModelId = "gemini-2.5-flash",
        defaultEndpoint = "/v1beta/models/gemini-3-flash-preview:generateContent"
    ),
    DATA_PRESENTATION(
        label = "Data Analytics & Presentation Generator",
        primaryModelId = "gemini-3.1-pro-preview",
        fallbackModelId = "gemini-3-flash-preview",
        defaultEndpoint = "/v1beta/models/gemini-3.1-pro-preview:generateContent"
    ),
    AGENT_AUTOMATION(
        label = "Autonomous AI Agent & Workflow Planner",
        primaryModelId = "gemini-3.1-pro-preview",
        fallbackModelId = "gemini-3-flash-preview",
        defaultEndpoint = "/v1beta/models/gemini-3.1-pro-preview:generateContent"
    );

    companion object {
        fun fromName(value: String?): AiRouterTaskType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CHAT
    }
}

data class PlatformFeature(
    val featureId: String,
    val name: String,
    val description: String,
    val howToUse: String,
    val iconKey: String,
    val category: AiToolCategory,
    val version: String = "2.3.0",
    val previousVersion: String = "2.2.0",
    val status: FeatureStatus = FeatureStatus.ACTIVE,
    val badge: FeatureBadge = FeatureBadge.NONE,
    val minSupportedAppVersion: String = "1.0.0",
    val requiredPermissions: List<String> = listOf("INTERNET"),
    val allowedPlans: List<String> = listOf("FREE", "PRO", "PREMIUM"),
    val dailyLimitFree: Int = 20,
    val dailyLimitPro: Int = 100,
    val dailyLimitPremium: Int = 500,
    val monthlyLimit: Int = 3000,
    val yearlyLimit: Int = 36000,
    val backendEndpoint: String = "/v1beta/models/gemini-3.5-flash:generateContent",
    val frontendRoute: String = "hub://tool/dynamic",
    val taskType: AiRouterTaskType = AiRouterTaskType.CHAT,
    val systemInstruction: String = "You are a specialized AI module inside Kallesh AI Hub.",
    val inputPlaceholder: String = "Describe your task or paste content here...",
    val releaseDate: String = "2026-09-28",
    val changelog: String = "Initial production release in Kallesh AI Hub.",
    val isVisible: Boolean = true,
    val rolloutPercentage: Int = 100,
    val betaUsersOnly: Boolean = false,
    val scheduledReleaseDate: String = ""
) {
    fun dailyLimitForPlan(plan: SubscriptionPlan): Int = when (plan) {
        SubscriptionPlan.FREE -> dailyLimitFree
        SubscriptionPlan.PRO -> dailyLimitPro
        SubscriptionPlan.PREMIUM -> dailyLimitPremium
    }

    fun isAvailableForUser(
        userId: String,
        plan: SubscriptionPlan,
        currentAppVersion: String,
        isBetaOptIn: Boolean = true
    ): Boolean {
        if (!isVisible) return false
        if (status == FeatureStatus.DISABLED) return false
        if (SemanticVersion.isBelowMinimum(currentAppVersion, minSupportedAppVersion)) return false
        if (betaUsersOnly && !isBetaOptIn && plan == SubscriptionPlan.FREE) return false
        if (rolloutPercentage < 100) {
            val bucket = abs((userId + featureId).hashCode()) % 100
            if (bucket >= rolloutPercentage) return false
        }
        return true
    }

    fun isPlanEntitled(plan: SubscriptionPlan): Boolean =
        allowedPlans.any { it.equals(plan.id, ignoreCase = true) }

    fun toJson(): JSONObject = JSONObject().apply {
        put("featureId", featureId)
        put("name", name)
        put("description", description)
        put("howToUse", howToUse)
        put("iconKey", iconKey)
        put("category", category.name)
        put("version", version)
        put("previousVersion", previousVersion)
        put("status", status.name)
        put("badge", badge.name)
        put("minSupportedAppVersion", minSupportedAppVersion)
        put("requiredPermissions", JSONArray(requiredPermissions))
        put("allowedPlans", JSONArray(allowedPlans))
        put("dailyLimitFree", dailyLimitFree)
        put("dailyLimitPro", dailyLimitPro)
        put("dailyLimitPremium", dailyLimitPremium)
        put("monthlyLimit", monthlyLimit)
        put("yearlyLimit", yearlyLimit)
        put("backendEndpoint", backendEndpoint)
        put("frontendRoute", frontendRoute)
        put("taskType", taskType.name)
        put("systemInstruction", systemInstruction)
        put("inputPlaceholder", inputPlaceholder)
        put("releaseDate", releaseDate)
        put("changelog", changelog)
        put("isVisible", isVisible)
        put("rolloutPercentage", rolloutPercentage)
        put("betaUsersOnly", betaUsersOnly)
        put("scheduledReleaseDate", scheduledReleaseDate)
    }

    companion object {
        fun fromJson(obj: JSONObject): PlatformFeature {
            val permsArr = obj.optJSONArray("requiredPermissions")
            val perms = buildList {
                if (permsArr != null) {
                    for (i in 0 until permsArr.length()) add(permsArr.optString(i))
                } else add("INTERNET")
            }
            val plansArr = obj.optJSONArray("allowedPlans")
            val plans = buildList {
                if (plansArr != null) {
                    for (i in 0 until plansArr.length()) add(plansArr.optString(i))
                } else {
                    add("FREE")
                    add("PRO")
                    add("PREMIUM")
                }
            }
            return PlatformFeature(
                featureId = obj.optString("featureId", "feat_custom"),
                name = obj.optString("name", "AI Tool"),
                description = obj.optString("description", ""),
                howToUse = obj.optString("howToUse", "Enter a prompt and tap Run AI."),
                iconKey = obj.optString("iconKey", "auto_awesome"),
                category = AiToolCategory.fromName(obj.optString("category")),
                version = obj.optString("version", "2.3.0"),
                previousVersion = obj.optString("previousVersion", "2.2.0"),
                status = FeatureStatus.fromName(obj.optString("status")),
                badge = FeatureBadge.fromName(obj.optString("badge")),
                minSupportedAppVersion = obj.optString("minSupportedAppVersion", "1.0.0"),
                requiredPermissions = perms,
                allowedPlans = plans,
                dailyLimitFree = obj.optInt("dailyLimitFree", 15),
                dailyLimitPro = obj.optInt("dailyLimitPro", 100),
                dailyLimitPremium = obj.optInt("dailyLimitPremium", 500),
                monthlyLimit = obj.optInt("monthlyLimit", 3000),
                yearlyLimit = obj.optInt("yearlyLimit", 36000),
                backendEndpoint = obj.optString("backendEndpoint", "/v1beta/models/gemini-3.5-flash:generateContent"),
                frontendRoute = obj.optString("frontendRoute", "hub://tool/dynamic"),
                taskType = AiRouterTaskType.fromName(obj.optString("taskType")),
                systemInstruction = obj.optString("systemInstruction", "You are a specialized AI module inside Kallesh AI Hub."),
                inputPlaceholder = obj.optString("inputPlaceholder", "Enter your request..."),
                releaseDate = obj.optString("releaseDate", "2026-09-28"),
                changelog = obj.optString("changelog", "Updated release"),
                isVisible = obj.optBoolean("isVisible", true),
                rolloutPercentage = obj.optInt("rolloutPercentage", 100).coerceIn(0, 100),
                betaUsersOnly = obj.optBoolean("betaUsersOnly", false),
                scheduledReleaseDate = obj.optString("scheduledReleaseDate", "")
            )
        }
    }
}

data class AppVersionRelease(
    val installedVersion: String = "2.2.0",
    val latestAvailableVersion: String = "2.3.0",
    val minimumSupportedVersion: String = "1.5.0",
    val isMandatory: Boolean = false,
    val releaseDate: String = "2026-09-28",
    val releaseTitle: String = "Kallesh AI Hub Update Available",
    val newFeatures: List<String> = listOf(
        "Dynamic AI Feature Management System & 28-Category AI Tool Hub",
        "AI Model Router with Automatic Multi-Provider Fallback",
        "AI Presentation Generator, Data Analysis, OCR & Autonomous AI Agent",
        "Real-Time Feature Flags, Percentage Rollouts & One-Click Rollback"
    ),
    val improvedItems: List<String> = listOf(
        "Faster streaming chat responses & grounded web search",
        "Enhanced PDF, code, and multimodal file analysis",
        "Non-destructive Room v1 → v2 database migration preserving all user data"
    ),
    val bugFixes: List<String> = listOf(
        "Resolved chat history reload latency on folder switches",
        "Fixed image studio aspect ratio badge alignment on compact screens"
    ),
    val securityUpdates: List<String> = listOf(
        "Hardened server-side API key protection & zero-trust Firestore rules",
        "Added admin role audit logging and input validation guards"
    ),
    val breakingChanges: List<String> = emptyList()
) {
    val hasUpdatePending: Boolean
        get() = SemanticVersion.isUpdateAvailable(installedVersion, latestAvailableVersion)

    val isForcedUpdateRequired: Boolean
        get() = isMandatory || SemanticVersion.isBelowMinimum(installedVersion, minimumSupportedVersion)

    fun toJson(): JSONObject = JSONObject().apply {
        put("installedVersion", installedVersion)
        put("latestAvailableVersion", latestAvailableVersion)
        put("minimumSupportedVersion", minimumSupportedVersion)
        put("isMandatory", isMandatory)
        put("releaseDate", releaseDate)
        put("releaseTitle", releaseTitle)
        put("newFeatures", JSONArray(newFeatures))
        put("improvedItems", JSONArray(improvedItems))
        put("bugFixes", JSONArray(bugFixes))
        put("securityUpdates", JSONArray(securityUpdates))
        put("breakingChanges", JSONArray(breakingChanges))
    }

    companion object {
        fun fromJson(obj: JSONObject, localInstalledVersion: String): AppVersionRelease {
            fun parseList(key: String, fallback: List<String>): List<String> {
                val arr = obj.optJSONArray(key) ?: return fallback
                return buildList {
                    for (i in 0 until arr.length()) {
                        val item = arr.optString(i)
                        if (item.isNotBlank()) add(item)
                    }
                }.ifEmpty { fallback }
            }
            val default = AppVersionRelease(installedVersion = localInstalledVersion)
            return AppVersionRelease(
                installedVersion = localInstalledVersion,
                latestAvailableVersion = obj.optString("latestAvailableVersion", default.latestAvailableVersion),
                minimumSupportedVersion = obj.optString("minimumSupportedVersion", default.minimumSupportedVersion),
                isMandatory = obj.optBoolean("isMandatory", false),
                releaseDate = obj.optString("releaseDate", default.releaseDate),
                releaseTitle = obj.optString("releaseTitle", default.releaseTitle),
                newFeatures = parseList("newFeatures", default.newFeatures),
                improvedItems = parseList("improvedItems", default.improvedItems),
                bugFixes = parseList("bugFixes", default.bugFixes),
                securityUpdates = parseList("securityUpdates", default.securityUpdates),
                breakingChanges = parseList("breakingChanges", emptyList())
            )
        }
    }
}

data class ChangelogEntry(
    val version: String,
    val releaseDate: String,
    val headline: String,
    val newItems: List<String>,
    val improvedItems: List<String>,
    val fixedItems: List<String>,
    val securityItems: List<String>
)

data class QualityCheckItem(
    val id: String,
    val checkName: String,
    val passed: Boolean,
    val details: String
)

data class SecurityAuditLog(
    val id: String,
    val timestampMs: Long,
    val actor: String,
    val action: String,
    val targetId: String,
    val details: String
)

data class PlatformAnalyticsSnapshot(
    val activeUsers: Int = 142,
    val dailyActiveUsers: Int = 89,
    val monthlyActiveUsers: Int = 1240,
    val totalApiRequests: Int = 0,
    val apiErrors: Int = 0,
    val failedGenerations: Int = 0,
    val fallbackActivations: Int = 0,
    val avgResponseTimeMs: Long = 640L,
    val updateAdoptionRatePercent: Int = 94,
    val featureActivationRatePercent: Int = 88,
    val storageUsageKb: Long = 512L,
    val estimatedTokensUsed: Long = 14500L,
    val featureUsageCounts: Map<String, Int> = emptyMap(),
    val subscriptionDistribution: Map<String, Int> = mapOf("FREE" to 72, "PRO" to 21, "PREMIUM" to 7),
    val databaseSchemaVersion: Int = 2,
    val preservedRecordsCount: Int = 0
)
