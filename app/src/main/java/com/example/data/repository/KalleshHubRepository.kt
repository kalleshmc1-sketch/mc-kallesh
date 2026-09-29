package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.DailyUsage
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SystemConfig
import com.example.data.model.UsageCategory
import com.example.data.model.UserProfile
import com.example.data.model.WorkspaceItem
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KalleshHubRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id)),
        auth = FirebaseAuth.getInstance()
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun todayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    // --- 1. User Profile & Personalization ---

    fun observeUserProfile(userId: String = requireUserId()): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot -> UserProfile.fromSnapshot(snapshot) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String,
        avatarUrl: String?
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        val existingProfile = UserProfile.fromSnapshot(snapshot)
        if (existingProfile != null) {
            return@runCatching existingProfile
        }

        val newProfile = UserProfile(
            userId = uid,
            displayName = displayName.ifBlank { email.substringBefore("@").ifBlank { "Explorer" } },
            email = email,
            avatarUrl = avatarUrl,
            plan = "FREE",
            hasSeenFounderIntro = false,
            memoryEnabled = true,
            memorySummary = "",
            preferredLanguage = "English",
            responseStyle = "Professional",
            defaultModel = "gemini-3.5-flash",
            themeMode = "DARK",
            voiceEnabled = true
        )
        try {
            docRef.set(newProfile.toCreateMap()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
        newProfile
    }

    suspend fun markFounderIntroSeen(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "hasSeenFounderIntro" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserProfileSettings(
        displayName: String,
        preferredLanguage: String,
        responseStyle: String,
        defaultModel: String,
        themeMode: String,
        voiceEnabled: Boolean,
        memoryEnabled: Boolean,
        memorySummary: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "displayName" to displayName.take(100).ifBlank { "Explorer" },
                    "preferredLanguage" to preferredLanguage.take(50),
                    "responseStyle" to responseStyle.take(50),
                    "defaultModel" to defaultModel.take(100),
                    "themeMode" to themeMode,
                    "voiceEnabled" to voiceEnabled,
                    "memoryEnabled" to memoryEnabled,
                    "memorySummary" to memorySummary.take(4000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun upgradeSubscriptionPlan(plan: SubscriptionPlan): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "plan" to plan.id,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 2. Conversations & Multi-Turn Messages ---

    fun observeConversations(userId: String = requireUserId()): Flow<List<Conversation>> = flow {
        val path = "conversations"
        emitAll(
            db.collection("conversations")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { querySnapshot ->
                    querySnapshot.documents
                        .map { Conversation.fromSnapshot(it) }
                        .sortedWith(
                            compareByDescending<Conversation> { it.isPinned }
                                .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                        )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getUserConversations(userId: String = requireUserId()): Result<List<Conversation>> = runCatching {
        val path = "conversations"
        try {
            val snapshot = db.collection(path)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.map { Conversation.fromSnapshot(it) }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun getConversationById(conversationId: String): Result<Conversation> = runCatching {
        val docRef = db.collection("conversations").document(conversationId)
        try {
            val doc = docRef.get().await()
            Conversation.fromSnapshot(doc)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createConversation(
        title: String,
        modelId: String = "gemini-3.5-flash",
        folder: String = "General"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val convId = "conv_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("conversations").document(convId)
        val conversation = Conversation(
            id = convId,
            userId = uid,
            title = title.take(200).ifBlank { "New Conversation" },
            modelId = modelId,
            folder = folder,
            isPinned = false,
            isShared = false,
            shareId = "",
            lastMessagePreview = "New conversation started",
            messageCount = 0
        )
        try {
            docRef.set(conversation.toCreateMap()).await()
            convId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun renameConversation(conversationId: String, newTitle: String, folder: String): Result<Unit> = runCatching {
        val docRef = db.collection("conversations").document(conversationId)
        try {
            docRef.update(
                mapOf(
                    "title" to newTitle.take(200).ifBlank { "Conversation" },
                    "folder" to folder.take(80).ifBlank { "General" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun togglePinConversation(conversationId: String, isPinned: Boolean): Result<Unit> = runCatching {
        val docRef = db.collection("conversations").document(conversationId)
        try {
            docRef.update(
                mapOf(
                    "isPinned" to isPinned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleShareConversation(conversationId: String, enableShare: Boolean): Result<String> = runCatching {
        val docRef = db.collection("conversations").document(conversationId)
        val shareId = if (enableShare) "share_${UUID.randomUUID().toString().take(8)}" else ""
        try {
            docRef.update(
                mapOf(
                    "isShared" to enableShare,
                    "shareId" to shareId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            shareId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteConversation(conversationId: String): Result<Unit> = runCatching {
        val docRef = db.collection("conversations").document(conversationId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeMessages(conversationId: String, userId: String = requireUserId()): Flow<List<ChatMessage>> = flow {
        val path = "conversations/$conversationId/messages"
        emitAll(
            db.collection("conversations").document(conversationId)
                .collection("messages")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .map { ChatMessage.fromSnapshot(it) }
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addChatMessage(
        conversationId: String,
        role: String,
        content: String,
        modelId: String,
        citations: List<String> = emptyList(),
        attachmentName: String = "",
        updatedMessageCount: Int,
        autoTitle: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val msgRef = db.collection("conversations").document(conversationId)
            .collection("messages").document(msgId)
        val message = ChatMessage(
            id = msgId,
            userId = uid,
            conversationId = conversationId,
            role = role,
            content = content,
            modelId = modelId,
            feedback = "NONE",
            citations = citations,
            attachmentName = attachmentName
        )
        try {
            msgRef.set(message.toCreateMap()).await()
            val convRef = db.collection("conversations").document(conversationId)
            val convUpdates = mutableMapOf<String, Any>(
                "lastMessagePreview" to content.take(500),
                "messageCount" to updatedMessageCount,
                "modelId" to modelId.take(100),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!autoTitle.isNullOrBlank()) {
                convUpdates["title"] = autoTitle.take(200)
            }
            convRef.update(convUpdates).await()
            msgId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            throw e
        }
    }

    suspend fun updateMessageFeedback(
        conversationId: String,
        messageId: String,
        feedback: String
    ): Result<Unit> = runCatching {
        val msgRef = db.collection("conversations").document(conversationId)
            .collection("messages").document(messageId)
        try {
            msgRef.update(
                mapOf(
                    "feedback" to feedback,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
            throw e
        }
    }

    suspend fun updateMessageContent(
        conversationId: String,
        messageId: String,
        newContent: String
    ): Result<Unit> = runCatching {
        val msgRef = db.collection("conversations").document(conversationId)
            .collection("messages").document(messageId)
        try {
            msgRef.update(
                mapOf(
                    "content" to newContent.take(50000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msgRef.path)
            throw e
        }
    }

    // --- 3. Daily Usage Enforcement ---

    fun observeTodayUsage(userId: String = requireUserId()): Flow<DailyUsage> = flow {
        val dateKey = todayDateKey()
        val docId = "${userId}_${dateKey}"
        val path = "usage"
        emitAll(
            db.collection("usage")
                .whereEqualTo("userId", userId)
                .whereEqualTo("dateKey", dateKey)
                .snapshots()
                .map { querySnapshot ->
                    val doc = querySnapshot.documents.firstOrNull()
                    if (doc != null) {
                        DailyUsage.fromSnapshot(doc, userId, dateKey)
                    } else {
                        DailyUsage(id = docId, userId = userId, dateKey = dateKey)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun checkAndIncrementUsage(
        category: UsageCategory,
        plan: SubscriptionPlan
    ): Result<DailyUsage> = runCatching {
        val uid = requireUserId()
        val dateKey = todayDateKey()
        val docId = "${uid}_${dateKey}"
        val docRef = db.collection("usage").document(docId)

        val querySnapshot = try {
            db.collection("usage")
                .whereEqualTo("userId", uid)
                .whereEqualTo("dateKey", dateKey)
                .get()
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "usage")
            throw e
        }

        val existingDoc = querySnapshot.documents.firstOrNull()
        val current = if (existingDoc != null) {
            DailyUsage.fromSnapshot(existingDoc, uid, dateKey)
        } else {
            DailyUsage(id = docId, userId = uid, dateKey = dateKey)
        }

        val used = current.countFor(category)
        val limit = current.limitFor(category, plan)
        if (used >= limit) {
            throw IllegalStateException("You have reached today's ${category.label} limit ($used / $limit on ${plan.displayName}). Upgrade your plan for higher daily limits.")
        }

        val updated = when (category) {
            UsageCategory.CHAT -> current.copy(chatCount = current.chatCount + 1)
            UsageCategory.IMAGE -> current.copy(imageCount = current.imageCount + 1)
            UsageCategory.VIDEO -> current.copy(videoCount = current.videoCount + 1)
            UsageCategory.FILE -> current.copy(fileCount = current.fileCount + 1)
            UsageCategory.VOICE -> current.copy(voiceCount = current.voiceCount + 1)
            UsageCategory.MUSIC -> current.copy(musicCount = current.musicCount + 1)
        }

        try {
            if (existingDoc == null) {
                docRef.set(updated.toCreateMap()).await()
            } else {
                docRef.update(
                    mapOf(
                        category.fieldName to updated.countFor(category),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
        updated
    }

    suspend fun saveDailyUsageSnapshot(usage: DailyUsage): Result<DailyUsage> = runCatching {
        val uid = requireUserId()
        val dateKey = todayDateKey()
        val docId = "${uid}_${dateKey}"
        val docRef = db.collection("usage").document(docId)
        val normalized = usage.copy(id = docId, userId = uid, dateKey = dateKey)
        try {
            docRef.set(normalized.toCreateMap()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
        normalized
    }

    // --- 4. Personal AI Workspace & Studio Items ---

    fun observeWorkspaceItems(userId: String = requireUserId()): Flow<List<WorkspaceItem>> = flow {
        val path = "workspace_items"
        emitAll(
            db.collection("workspace_items")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .map { WorkspaceItem.fromSnapshot(it) }
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createWorkspaceItem(
        type: String,
        title: String,
        prompt: String,
        content: String,
        mediaData: String = "",
        aspectRatio: String = "1:1",
        status: String = "COMPLETED",
        category: String = "General"
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val itemId = "item_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("workspace_items").document(itemId)
        val item = WorkspaceItem(
            id = itemId,
            userId = uid,
            type = type,
            title = title,
            prompt = prompt,
            content = content,
            mediaData = mediaData,
            aspectRatio = aspectRatio,
            status = status,
            category = category
        )
        try {
            docRef.set(item.toCreateMap()).await()
            itemId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateWorkspaceItem(
        itemId: String,
        status: String,
        content: String,
        mediaData: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("workspace_items").document(itemId)
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "content" to content.take(60000),
                    "mediaData" to mediaData.take(200000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteWorkspaceItem(itemId: String): Result<Unit> = runCatching {
        val docRef = db.collection("workspace_items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 5. Notifications Center ---

    fun observeNotifications(userId: String = requireUserId()): Flow<List<AppNotification>> = flow {
        val path = "notifications"
        emitAll(
            db.collection("notifications")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .map { AppNotification.fromSnapshot(it) }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createNotification(title: String, message: String, type: String = "INFO"): Result<Unit> = runCatching {
        val uid = requireUserId()
        val notifId = "notif_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("notifications").document(notifId)
        val notif = AppNotification(
            id = notifId,
            userId = uid,
            title = title,
            message = message,
            type = type,
            isRead = false
        )
        try {
            docRef.set(notif.toCreateMap()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> = runCatching {
        val docRef = db.collection("notifications").document(notificationId)
        try {
            docRef.update(
                mapOf(
                    "isRead" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- 6. Founder Introduction & System Config (Admin Managed) ---

    fun observeFounderConfig(): Flow<SystemConfig> = observeSystemConfigDoc("founder_intro")

    fun observeSystemConfigDoc(docId: String): Flow<SystemConfig> = flow {
        val path = "system_config/$docId"
        emitAll(
            db.collection("system_config").document(docId)
                .snapshots()
                .map { snapshot -> SystemConfig.fromSnapshot(snapshot) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    emit(SystemConfig(id = docId))
                }
        )
    }

    suspend fun saveFounderConfig(config: SystemConfig): Result<Unit> =
        saveSystemConfigDoc("founder_intro", config)

    suspend fun saveSystemConfigDoc(docId: String, config: SystemConfig): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("system_config").document(docId)
        val existing = try {
            docRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        val updatedConfig = config.copy(id = docId, userId = uid)
        try {
            if (!existing.exists()) {
                docRef.set(updatedConfig.toCreateMap()).await()
            } else {
                docRef.update(
                    mapOf(
                        "userId" to uid,
                        "title" to updatedConfig.title.take(200).ifBlank { docId },
                        "description" to updatedConfig.description.take(10000).ifBlank { "{}" },
                        "voiceName" to updatedConfig.voiceName.take(60).ifBlank { "Puck" },
                        "language" to updatedConfig.language.take(20).ifBlank { "en" },
                        "audioEnabled" to updatedConfig.audioEnabled,
                        "showOnFirstLogin" to updatedConfig.showOnFirstLogin,
                        "allowReplay" to updatedConfig.allowReplay,
                        "announcement" to updatedConfig.announcement.take(1000),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
