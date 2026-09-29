package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class SubscriptionPlan(
    val id: String,
    val displayName: String,
    val badgeText: String,
    val priceLabel: String,
    val dailyChatLimit: Int,
    val dailyImageLimit: Int,
    val dailyVideoLimit: Int,
    val dailyFileLimit: Int,
    val dailyVoiceLimit: Int,
    val dailyMusicLimit: Int,
    val maxFileSizeMb: Int
) {
    FREE(
        id = "FREE",
        displayName = "Starter Free",
        badgeText = "FREE",
        priceLabel = "Free Forever",
        dailyChatLimit = 20,
        dailyImageLimit = 3,
        dailyVideoLimit = 1,
        dailyFileLimit = 5,
        dailyVoiceLimit = 10,
        dailyMusicLimit = 2,
        maxFileSizeMb = 5
    ),
    PRO(
        id = "PRO",
        displayName = "Kallesh Pro",
        badgeText = "PRO",
        priceLabel = "$19 / month",
        dailyChatLimit = 100,
        dailyImageLimit = 25,
        dailyVideoLimit = 5,
        dailyFileLimit = 30,
        dailyVoiceLimit = 50,
        dailyMusicLimit = 15,
        maxFileSizeMb = 25
    ),
    PREMIUM(
        id = "PREMIUM",
        displayName = "Kallesh Enterprise",
        badgeText = "PREMIUM",
        priceLabel = "$49 / month",
        dailyChatLimit = 500,
        dailyImageLimit = 100,
        dailyVideoLimit = 25,
        dailyFileLimit = 100,
        dailyVoiceLimit = 200,
        dailyMusicLimit = 50,
        maxFileSizeMb = 100
    );

    companion object {
        fun fromId(id: String?): SubscriptionPlan =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FREE
    }
}

enum class UsageCategory(val fieldName: String, val label: String) {
    CHAT("chatCount", "AI Messages"),
    IMAGE("imageCount", "Image Generations"),
    VIDEO("videoCount", "Video Generations"),
    FILE("fileCount", "File Analyses"),
    VOICE("voiceCount", "Voice & Audio"),
    MUSIC("musicCount", "Music Tracks")
}

data class LimitReachedAlertState(
    val category: UsageCategory,
    val usedCount: Int,
    val dailyLimit: Int,
    val currentPlan: SubscriptionPlan,
    val featureName: String = category.label,
    val message: String = "You have reached today's ${category.label} limit ($usedCount / $dailyLimit) on ${currentPlan.displayName}. Upgrade your plan to unlock higher daily limits immediately."
)

data class AiModelOption(
    val alias: String,
    val modelId: String,
    val badge: String,
    val description: String,
    val supportsVision: Boolean = true,
    val requiresPro: Boolean = false
)

val AVAILABLE_CHAT_MODELS = listOf(
    AiModelOption(
        alias = "Kallesh Auto",
        modelId = "gemini-3-flash-preview",
        badge = "AUTO",
        description = "Balanced reasoning, speed, and multimodal intelligence",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Fast AI",
        modelId = "gemini-3.1-flash-lite-preview",
        badge = "FAST",
        description = "Ultra-low latency responses for quick daily tasks",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Smart AI",
        modelId = "gemini-3-flash-preview",
        badge = "SMART",
        description = "Deep multi-turn chat, summarization, and document analysis",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Advanced AI",
        modelId = "gemini-3.1-pro-preview",
        badge = "PRO",
        description = "Complex coding, STEM, architecture, and deep reasoning",
        supportsVision = true,
        requiresPro = false
    ),
    AiModelOption(
        alias = "Vision AI",
        modelId = "gemini-3-flash-preview",
        badge = "VISION",
        description = "Specialized for reading diagrams, photos, and visual Q&A",
        supportsVision = true,
        requiresPro = false
    )
)

const val DEFAULT_FOUNDER_INTRO_TITLE = "Welcome to Kallesh AI Hub"
const val DEFAULT_FOUNDER_INTRO_SCRIPT = """Welcome to Kallesh AI Hub.

I’m Kallesh MC, the founder of Kallesh AI Hub.

Kallesh AI Hub is created with a simple vision — to bring powerful AI tools together in one place and make them easier for everyone to use.

Here, you can chat with AI, ask questions, understand files, work with images, generate creative content, and explore new AI-powered tools.

This is more than just a chatbot. It is a growing AI hub built to help you learn, create, and turn your ideas into reality.

Welcome to Kallesh AI Hub.

Let’s create something amazing."""

data class UserProfile(
    val userId: String = "",
    val displayName: String = "Explorer",
    val email: String = "",
    val avatarUrl: String? = null,
    val plan: String = "FREE",
    val hasSeenFounderIntro: Boolean = false,
    val memoryEnabled: Boolean = true,
    val memorySummary: String = "",
    val preferredLanguage: String = "English",
    val responseStyle: String = "Professional",
    val defaultModel: String = "gemini-3.5-flash",
    val themeMode: String = "DARK",
    val voiceEnabled: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val subscriptionPlan: SubscriptionPlan
        get() = SubscriptionPlan.fromId(plan)

    val isAdminUser: Boolean
        get() = email.equals("mckallesh14@gmail.com", ignoreCase = true)

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.take(100).ifBlank { "Explorer" },
        "email" to email.take(200),
        "avatarUrl" to avatarUrl?.take(1000),
        "plan" to plan,
        "hasSeenFounderIntro" to hasSeenFounderIntro,
        "memoryEnabled" to memoryEnabled,
        "memorySummary" to memorySummary.take(4000),
        "preferredLanguage" to preferredLanguage.take(50),
        "responseStyle" to responseStyle.take(50),
        "defaultModel" to defaultModel.take(100),
        "themeMode" to themeMode,
        "voiceEnabled" to voiceEnabled,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }.mapValues { it.value!! }

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): UserProfile? {
            if (!doc.exists()) return null
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return UserProfile(
                userId = doc.getString("userId") ?: doc.id,
                displayName = doc.getString("displayName") ?: "Explorer",
                email = doc.getString("email") ?: "",
                avatarUrl = doc.getString("avatarUrl"),
                plan = doc.getString("plan") ?: "FREE",
                hasSeenFounderIntro = doc.getBoolean("hasSeenFounderIntro") ?: false,
                memoryEnabled = doc.getBoolean("memoryEnabled") ?: true,
                memorySummary = doc.getString("memorySummary") ?: "",
                preferredLanguage = doc.getString("preferredLanguage") ?: "English",
                responseStyle = doc.getString("responseStyle") ?: "Professional",
                defaultModel = doc.getString("defaultModel") ?: "gemini-3.5-flash",
                themeMode = doc.getString("themeMode") ?: "DARK",
                voiceEnabled = doc.getBoolean("voiceEnabled") ?: true,
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class Conversation(
    val id: String = "",
    val userId: String = "",
    val title: String = "New Conversation",
    val modelId: String = "gemini-3.5-flash",
    val folder: String = "General",
    val isPinned: Boolean = false,
    val isShared: Boolean = false,
    val shareId: String = "",
    val lastMessagePreview: String = "Start a conversation...",
    val messageCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200).ifBlank { "New Conversation" },
        "modelId" to modelId.take(100),
        "folder" to folder.take(80),
        "isPinned" to isPinned,
        "isShared" to isShared,
        "shareId" to shareId.take(128),
        "lastMessagePreview" to lastMessagePreview.take(500),
        "messageCount" to messageCount,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): Conversation {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return Conversation(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: "Conversation",
                modelId = doc.getString("modelId") ?: "gemini-3.5-flash",
                folder = doc.getString("folder") ?: "General",
                isPinned = doc.getBoolean("isPinned") ?: false,
                isShared = doc.getBoolean("isShared") ?: false,
                shareId = doc.getString("shareId") ?: "",
                lastMessagePreview = doc.getString("lastMessagePreview") ?: "",
                messageCount = (doc.getLong("messageCount") ?: 0L).toInt(),
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val role: String = "user",
    val content: String = "",
    val modelId: String = "gemini-3.5-flash",
    val feedback: String = "NONE",
    val citations: List<String> = emptyList(),
    val attachmentName: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "conversationId" to conversationId,
        "role" to if (role == "model") "model" else "user",
        "content" to content.take(50000).ifBlank { "..." },
        "modelId" to modelId.take(100),
        "feedback" to if (feedback in listOf("NONE", "LIKE", "DISLIKE")) feedback else "NONE",
        "citations" to citations.take(10).map { it.take(500) },
        "attachmentName" to attachmentName.take(255),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): ChatMessage {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            val rawCitations = (doc.get("citations") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return ChatMessage(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                conversationId = doc.getString("conversationId") ?: "",
                role = doc.getString("role") ?: "user",
                content = doc.getString("content") ?: "",
                modelId = doc.getString("modelId") ?: "gemini-3.5-flash",
                feedback = doc.getString("feedback") ?: "NONE",
                citations = rawCitations,
                attachmentName = doc.getString("attachmentName") ?: "",
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class DailyUsage(
    val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val chatCount: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0,
    val fileCount: Int = 0,
    val voiceCount: Int = 0,
    val musicCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun countFor(category: UsageCategory): Int = when (category) {
        UsageCategory.CHAT -> chatCount
        UsageCategory.IMAGE -> imageCount
        UsageCategory.VIDEO -> videoCount
        UsageCategory.FILE -> fileCount
        UsageCategory.VOICE -> voiceCount
        UsageCategory.MUSIC -> musicCount
    }

    fun limitFor(category: UsageCategory, plan: SubscriptionPlan): Int = when (category) {
        UsageCategory.CHAT -> plan.dailyChatLimit
        UsageCategory.IMAGE -> plan.dailyImageLimit
        UsageCategory.VIDEO -> plan.dailyVideoLimit
        UsageCategory.FILE -> plan.dailyFileLimit
        UsageCategory.VOICE -> plan.dailyVoiceLimit
        UsageCategory.MUSIC -> plan.dailyMusicLimit
    }

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "dateKey" to dateKey.take(10),
        "chatCount" to chatCount,
        "imageCount" to imageCount,
        "videoCount" to videoCount,
        "fileCount" to fileCount,
        "voiceCount" to voiceCount,
        "musicCount" to musicCount,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot, fallbackUserId: String, fallbackDateKey: String): DailyUsage {
            if (!doc.exists()) {
                return DailyUsage(
                    id = "${fallbackUserId}_${fallbackDateKey}",
                    userId = fallbackUserId,
                    dateKey = fallbackDateKey
                )
            }
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return DailyUsage(
                id = doc.id,
                userId = doc.getString("userId") ?: fallbackUserId,
                dateKey = doc.getString("dateKey") ?: fallbackDateKey,
                chatCount = (doc.getLong("chatCount") ?: 0L).toInt(),
                imageCount = (doc.getLong("imageCount") ?: 0L).toInt(),
                videoCount = (doc.getLong("videoCount") ?: 0L).toInt(),
                fileCount = (doc.getLong("fileCount") ?: 0L).toInt(),
                voiceCount = (doc.getLong("voiceCount") ?: 0L).toInt(),
                musicCount = (doc.getLong("musicCount") ?: 0L).toInt(),
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class WorkspaceItem(
    val id: String = "",
    val userId: String = "",
    val type: String = "PROMPT",
    val title: String = "",
    val prompt: String = "",
    val content: String = "",
    val mediaData: String = "",
    val aspectRatio: String = "1:1",
    val status: String = "COMPLETED",
    val category: String = "General",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "type" to type,
        "title" to title.take(200).ifBlank { "Untitled Artifact" },
        "prompt" to prompt.take(10000),
        "content" to content.take(60000),
        "mediaData" to mediaData.take(200000),
        "aspectRatio" to aspectRatio.take(20),
        "status" to status,
        "category" to category.take(80),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): WorkspaceItem {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return WorkspaceItem(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                type = doc.getString("type") ?: "PROMPT",
                title = doc.getString("title") ?: "Artifact",
                prompt = doc.getString("prompt") ?: "",
                content = doc.getString("content") ?: "",
                mediaData = doc.getString("mediaData") ?: "",
                aspectRatio = doc.getString("aspectRatio") ?: "1:1",
                status = doc.getString("status") ?: "COMPLETED",
                category = doc.getString("category") ?: "General",
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO",
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200).ifBlank { "Notification" },
        "message" to message.take(2000).ifBlank { "..." },
        "type" to type.take(50).ifBlank { "INFO" },
        "isRead" to isRead,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): AppNotification {
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return AppNotification(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: "",
                message = doc.getString("message") ?: "",
                type = doc.getString("type") ?: "INFO",
                isRead = doc.getBoolean("isRead") ?: false,
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}

data class SystemConfig(
    val id: String = "founder_intro",
    val userId: String = "",
    val title: String = DEFAULT_FOUNDER_INTRO_TITLE,
    val description: String = DEFAULT_FOUNDER_INTRO_SCRIPT,
    val voiceName: String = "Puck",
    val language: String = "en",
    val audioEnabled: Boolean = true,
    val showOnFirstLogin: Boolean = true,
    val allowReplay: Boolean = true,
    val announcement: String = "Welcome to Kallesh AI Hub — One AI Hub. Limitless Possibilities.",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200).ifBlank { DEFAULT_FOUNDER_INTRO_TITLE },
        "description" to description.take(10000).ifBlank { DEFAULT_FOUNDER_INTRO_SCRIPT },
        "voiceName" to voiceName.take(60).ifBlank { "Puck" },
        "language" to language.take(20).ifBlank { "en" },
        "audioEnabled" to audioEnabled,
        "showOnFirstLogin" to showOnFirstLogin,
        "allowReplay" to allowReplay,
        "announcement" to announcement.take(1000),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): SystemConfig {
            if (!doc.exists()) return SystemConfig()
            val tsBehavior = DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            return SystemConfig(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: DEFAULT_FOUNDER_INTRO_TITLE,
                description = doc.getString("description") ?: DEFAULT_FOUNDER_INTRO_SCRIPT,
                voiceName = doc.getString("voiceName") ?: "Puck",
                language = doc.getString("language") ?: "en",
                audioEnabled = doc.getBoolean("audioEnabled") ?: true,
                showOnFirstLogin = doc.getBoolean("showOnFirstLogin") ?: true,
                allowReplay = doc.getBoolean("allowReplay") ?: true,
                announcement = doc.getString("announcement") ?: "",
                createdAt = doc.getTimestamp("createdAt", tsBehavior),
                updatedAt = doc.getTimestamp("updatedAt", tsBehavior)
            )
        }
    }
}
