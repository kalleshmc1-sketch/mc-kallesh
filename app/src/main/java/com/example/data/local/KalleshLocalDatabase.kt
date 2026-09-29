package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

@Entity(tableName = "cached_prompts")
data class CachedPromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val promptText: String,
    val isBuiltIn: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_tts_audio")
data class CachedAudioEntity(
    @PrimaryKey val cacheKey: String,
    val filePath: String,
    val voiceName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_feature_overrides")
data class PlatformFeatureOverrideEntity(
    @PrimaryKey val featureId: String,
    val featureJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_version_state")
data class AppVersionStateEntity(
    @PrimaryKey val stateKey: String = "primary",
    val installedVersion: String = "2.2.0",
    val latestReleaseJson: String = "",
    val dismissedVersionPrompt: String = "",
    val seenDiscoveryFeatureIdsCsv: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_audit_logs")
data class SecurityAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val actor: String,
    val action: String,
    val targetId: String,
    val details: String
)

@Entity(tableName = "platform_telemetry")
data class PlatformTelemetryEntity(
    @PrimaryKey val metricKey: String = "global_metrics",
    val totalApiRequests: Int = 0,
    val apiErrors: Int = 0,
    val failedGenerations: Int = 0,
    val fallbackActivations: Int = 0,
    val lastResponseTimeMs: Long = 580L,
    val estimatedTokensUsed: Long = 14800L,
    val featureUsageJson: String = "{}"
)

@Entity(
    tableName = "chat_conversations",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["updatedAtMs"])
    ]
)
data class LocalConversationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val modelId: String,
    val folder: String = "General",
    val isPinned: Boolean = false,
    val isShared: Boolean = false,
    val shareId: String = "",
    val lastMessagePreview: String = "",
    val messageCount: Int = 0,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    fun toDomain(): Conversation = Conversation(
        id = id,
        userId = userId,
        title = title,
        modelId = modelId,
        folder = folder,
        isPinned = isPinned,
        isShared = isShared,
        shareId = shareId,
        lastMessagePreview = lastMessagePreview,
        messageCount = messageCount,
        createdAt = Timestamp(Date(createdAtMs)),
        updatedAt = Timestamp(Date(updatedAtMs))
    )

    companion object {
        fun fromDomain(conversation: Conversation): LocalConversationEntity {
            val now = System.currentTimeMillis()
            val createdMs = conversation.createdAt?.toDate()?.time ?: now
            val updatedMs = conversation.updatedAt?.toDate()?.time ?: createdMs
            return LocalConversationEntity(
                id = conversation.id,
                userId = conversation.userId,
                title = conversation.title,
                modelId = conversation.modelId,
                folder = conversation.folder,
                isPinned = conversation.isPinned,
                isShared = conversation.isShared,
                shareId = conversation.shareId,
                lastMessagePreview = conversation.lastMessagePreview,
                messageCount = conversation.messageCount,
                createdAtMs = createdMs,
                updatedAtMs = updatedMs
            )
        }
    }
}

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["userId"]),
        Index(value = ["createdAtMs"])
    ]
)
data class LocalChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val userId: String,
    val role: String,
    val content: String,
    val modelId: String,
    val feedback: String = "NONE",
    val citationsJson: String = "[]",
    val attachmentName: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChatMessage {
        val parsedCitations = runCatching {
            val arr = JSONArray(citationsJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i)
                    if (item.isNotBlank()) add(item)
                }
            }
        }.getOrDefault(emptyList())

        return ChatMessage(
            id = id,
            userId = userId,
            conversationId = conversationId,
            role = role,
            content = content,
            modelId = modelId,
            feedback = feedback,
            citations = parsedCitations,
            attachmentName = attachmentName,
            createdAt = Timestamp(Date(createdAtMs)),
            updatedAt = Timestamp(Date(updatedAtMs))
        )
    }

    companion object {
        fun encodeCitations(citations: List<String>): String {
            val arr = JSONArray()
            citations.forEach { arr.put(it) }
            return arr.toString()
        }

        fun fromDomain(message: ChatMessage): LocalChatMessageEntity {
            val now = System.currentTimeMillis()
            val createdMs = message.createdAt?.toDate()?.time ?: now
            val updatedMs = message.updatedAt?.toDate()?.time ?: createdMs
            return LocalChatMessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                userId = message.userId,
                role = message.role,
                content = message.content,
                modelId = message.modelId,
                feedback = message.feedback,
                citationsJson = encodeCitations(message.citations),
                attachmentName = message.attachmentName,
                createdAtMs = createdMs,
                updatedAtMs = updatedMs
            )
        }
    }
}

@Dao
interface KalleshLocalDao {
    @Query("SELECT * FROM cached_prompts ORDER BY isBuiltIn DESC, timestamp DESC")
    fun observeAllPrompts(): Flow<List<CachedPromptEntity>>

    @Query("SELECT COUNT(*) FROM cached_prompts")
    suspend fun getPromptCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: CachedPromptEntity)

    @Query("DELETE FROM cached_prompts WHERE id = :id")
    suspend fun deletePromptById(id: Int)

    @Query("SELECT * FROM cached_tts_audio WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCachedAudio(cacheKey: String): CachedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedAudio(entry: CachedAudioEntity)

    // v2 Dynamic Feature Registry & Version Management Queries
    @Query("SELECT * FROM platform_feature_overrides ORDER BY updatedAt DESC")
    fun observeFeatureOverrides(): Flow<List<PlatformFeatureOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeatureOverride(entity: PlatformFeatureOverrideEntity)

    @Query("DELETE FROM platform_feature_overrides WHERE featureId = :featureId")
    suspend fun deleteFeatureOverride(featureId: String)

    @Query("SELECT * FROM app_version_state WHERE stateKey = 'primary' LIMIT 1")
    fun observeAppVersionState(): Flow<AppVersionStateEntity?>

    @Query("SELECT * FROM app_version_state WHERE stateKey = 'primary' LIMIT 1")
    suspend fun getAppVersionState(): AppVersionStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppVersionState(state: AppVersionStateEntity)

    @Query("SELECT * FROM security_audit_logs ORDER BY timestampMs DESC LIMIT 60")
    fun observeAuditLogs(): Flow<List<SecurityAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: SecurityAuditLogEntity)

    @Query("SELECT * FROM platform_telemetry WHERE metricKey = 'global_metrics' LIMIT 1")
    fun observeTelemetry(): Flow<PlatformTelemetryEntity?>

    @Query("SELECT * FROM platform_telemetry WHERE metricKey = 'global_metrics' LIMIT 1")
    suspend fun getTelemetry(): PlatformTelemetryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTelemetry(telemetry: PlatformTelemetryEntity)

    // v3 Persisted Chat Conversations & Messages Queries
    @Query("SELECT * FROM chat_conversations WHERE userId = :userId ORDER BY isPinned DESC, updatedAtMs DESC")
    fun observeConversationsForUser(userId: String): Flow<List<LocalConversationEntity>>

    @Query("SELECT * FROM chat_conversations ORDER BY isPinned DESC, updatedAtMs DESC")
    fun observeAllConversations(): Flow<List<LocalConversationEntity>>

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): LocalConversationEntity?

    @Query("SELECT COUNT(*) FROM chat_conversations WHERE userId = :userId")
    suspend fun getConversationCountForUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM chat_conversations")
    suspend fun getTotalConversationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: LocalConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<LocalConversationEntity>)

    @Query("UPDATE chat_conversations SET title = :title, folder = :folder, updatedAtMs = :updatedAtMs WHERE id = :conversationId")
    suspend fun updateConversationTitleAndFolder(
        conversationId: String,
        title: String,
        folder: String,
        updatedAtMs: Long = System.currentTimeMillis()
    )

    @Query("UPDATE chat_conversations SET isPinned = :isPinned, updatedAtMs = :updatedAtMs WHERE id = :conversationId")
    suspend fun updateConversationPin(
        conversationId: String,
        isPinned: Boolean,
        updatedAtMs: Long = System.currentTimeMillis()
    )

    @Query("UPDATE chat_conversations SET isShared = :isShared, shareId = :shareId, updatedAtMs = :updatedAtMs WHERE id = :conversationId")
    suspend fun updateConversationShare(
        conversationId: String,
        isShared: Boolean,
        shareId: String,
        updatedAtMs: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM chat_conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Query("DELETE FROM chat_conversations WHERE userId = :userId")
    suspend fun clearConversationsForUser(userId: String)

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAtMs ASC")
    fun observeMessagesForConversation(conversationId: String): Flow<List<LocalChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY createdAtMs DESC")
    fun observeAllMessagesForUser(userId: String): Flow<List<LocalChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAtMs ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<LocalChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): LocalChatMessageEntity?

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getTotalMessageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: LocalChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<LocalChatMessageEntity>)

    @Query("UPDATE chat_messages SET feedback = :feedback, updatedAtMs = :updatedAtMs WHERE id = :messageId")
    suspend fun updateMessageFeedback(
        messageId: String,
        feedback: String,
        updatedAtMs: Long = System.currentTimeMillis()
    )

    @Query("UPDATE chat_messages SET content = :content, updatedAtMs = :updatedAtMs WHERE id = :messageId")
    suspend fun updateMessageContent(
        messageId: String,
        content: String,
        updatedAtMs: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearMessagesForUser(userId: String)

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND content LIKE '%' || :query || '%' ORDER BY createdAtMs DESC")
    fun searchMessagesForUser(userId: String, query: String): Flow<List<LocalChatMessageEntity>>
}

@Database(
    entities = [
        CachedPromptEntity::class,
        CachedAudioEntity::class,
        PlatformFeatureOverrideEntity::class,
        AppVersionStateEntity::class,
        SecurityAuditLogEntity::class,
        PlatformTelemetryEntity::class,
        LocalConversationEntity::class,
        LocalChatMessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KalleshLocalDatabase : RoomDatabase() {
    abstract fun localDao(): KalleshLocalDao

    companion object {
        /**
         * Non-destructive versioned migration from v1 to v2.
         * Preserves all existing cached_prompts and cached_tts_audio rows while adding
         * Dynamic Feature Management, App Version State, Security Audit Logs, and Telemetry tables.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `platform_feature_overrides` (
                        `featureId` TEXT NOT NULL,
                        `featureJson` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`featureId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_version_state` (
                        `stateKey` TEXT NOT NULL,
                        `installedVersion` TEXT NOT NULL,
                        `latestReleaseJson` TEXT NOT NULL,
                        `dismissedVersionPrompt` TEXT NOT NULL,
                        `seenDiscoveryFeatureIdsCsv` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`stateKey`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `security_audit_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestampMs` INTEGER NOT NULL,
                        `actor` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `targetId` TEXT NOT NULL,
                        `details` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `platform_telemetry` (
                        `metricKey` TEXT NOT NULL,
                        `totalApiRequests` INTEGER NOT NULL,
                        `apiErrors` INTEGER NOT NULL,
                        `failedGenerations` INTEGER NOT NULL,
                        `fallbackActivations` INTEGER NOT NULL,
                        `lastResponseTimeMs` INTEGER NOT NULL,
                        `estimatedTokensUsed` INTEGER NOT NULL,
                        `featureUsageJson` TEXT NOT NULL,
                        PRIMARY KEY(`metricKey`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Non-destructive versioned migration from v2 to v3.
         * Adds persistent local Room tables `chat_conversations` and `chat_messages`
         * so users can view and search all previous conversations and chat messages offline and across app restarts.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `chat_conversations` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `modelId` TEXT NOT NULL,
                        `folder` TEXT NOT NULL,
                        `isPinned` INTEGER NOT NULL,
                        `isShared` INTEGER NOT NULL,
                        `shareId` TEXT NOT NULL,
                        `lastMessagePreview` TEXT NOT NULL,
                        `messageCount` INTEGER NOT NULL,
                        `createdAtMs` INTEGER NOT NULL,
                        `updatedAtMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_chat_conversations_userId` ON `chat_conversations` (`userId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_chat_conversations_updatedAtMs` ON `chat_conversations` (`updatedAtMs`)"
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `chat_messages` (
                        `id` TEXT NOT NULL,
                        `conversationId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `modelId` TEXT NOT NULL,
                        `feedback` TEXT NOT NULL,
                        `citationsJson` TEXT NOT NULL,
                        `attachmentName` TEXT NOT NULL,
                        `createdAtMs` INTEGER NOT NULL,
                        `updatedAtMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_chat_messages_conversationId` ON `chat_messages` (`conversationId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_chat_messages_userId` ON `chat_messages` (`userId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_chat_messages_createdAtMs` ON `chat_messages` (`createdAtMs`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: KalleshLocalDatabase? = null

        fun getInstance(context: Context): KalleshLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KalleshLocalDatabase::class.java,
                    "kallesh_ai_hub_local.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LocalCacheRepository(private val dao: KalleshLocalDao) {
    val allPrompts: Flow<List<CachedPromptEntity>> = dao.observeAllPrompts()
    val featureOverrides: Flow<List<PlatformFeatureOverrideEntity>> = dao.observeFeatureOverrides()
    val appVersionState: Flow<AppVersionStateEntity?> = dao.observeAppVersionState()
    val auditLogs: Flow<List<SecurityAuditLogEntity>> = dao.observeAuditLogs()
    val telemetry: Flow<PlatformTelemetryEntity?> = dao.observeTelemetry()

    suspend fun getPromptCount(): Int = dao.getPromptCount()

    suspend fun savePrompt(prompt: CachedPromptEntity) = dao.insertPrompt(prompt)

    suspend fun deletePrompt(id: Int) = dao.deletePromptById(id)

    suspend fun getCachedAudioPath(cacheKey: String): String? =
        dao.getCachedAudio(cacheKey)?.filePath

    suspend fun saveCachedAudioPath(cacheKey: String, filePath: String, voiceName: String) {
        dao.insertCachedAudio(CachedAudioEntity(cacheKey = cacheKey, filePath = filePath, voiceName = voiceName))
    }

    // --- Persisted Chat History & Conversations (Room) ---

    fun observeConversations(userId: String): Flow<List<Conversation>> =
        dao.observeConversationsForUser(userId).map { list -> list.map { it.toDomain() } }

    fun observeAllConversations(): Flow<List<Conversation>> =
        dao.observeAllConversations().map { list -> list.map { it.toDomain() } }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> =
        dao.observeMessagesForConversation(conversationId).map { list -> list.map { it.toDomain() } }

    fun observeAllMessages(userId: String): Flow<List<ChatMessage>> =
        dao.observeAllMessagesForUser(userId).map { list -> list.map { it.toDomain() } }

    fun searchMessages(userId: String, query: String): Flow<List<ChatMessage>> =
        dao.searchMessagesForUser(userId, query).map { list -> list.map { it.toDomain() } }

    suspend fun getConversationCount(userId: String): Int =
        dao.getConversationCountForUser(userId)

    suspend fun getTotalConversationCount(): Int =
        dao.getTotalConversationCount()

    suspend fun getTotalMessageCount(): Int =
        dao.getTotalMessageCount()

    suspend fun getMessagesForConversationSnapshot(conversationId: String): List<ChatMessage> =
        dao.getMessagesForConversation(conversationId).map { it.toDomain() }

    suspend fun createLocalConversation(
        userId: String,
        title: String,
        modelId: String = "gemini-3.5-flash",
        folder: String = "General",
        customId: String? = null
    ): Conversation {
        val now = System.currentTimeMillis()
        val convId = customId ?: "conv_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val entity = LocalConversationEntity(
            id = convId,
            userId = userId,
            title = title.take(200).ifBlank { "New Conversation" },
            modelId = modelId,
            folder = folder.ifBlank { "General" },
            isPinned = false,
            isShared = false,
            shareId = "",
            lastMessagePreview = "New conversation started",
            messageCount = 0,
            createdAtMs = now,
            updatedAtMs = now
        )
        dao.upsertConversation(entity)
        return entity.toDomain()
    }

    suspend fun saveLocalChatMessage(
        conversationId: String,
        userId: String,
        role: String,
        content: String,
        modelId: String,
        attachmentName: String = "",
        citations: List<String> = emptyList(),
        autoTitle: String? = null,
        customId: String? = null,
        customTimestampMs: Long? = null
    ): ChatMessage {
        val now = customTimestampMs ?: System.currentTimeMillis()
        val msgId = customId ?: "msg_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val messageEntity = LocalChatMessageEntity(
            id = msgId,
            conversationId = conversationId,
            userId = userId,
            role = if (role == "model") "model" else "user",
            content = content,
            modelId = modelId,
            feedback = "NONE",
            citationsJson = LocalChatMessageEntity.encodeCitations(citations),
            attachmentName = attachmentName,
            createdAtMs = now,
            updatedAtMs = now
        )
        dao.insertChatMessage(messageEntity)

        val existingConv = dao.getConversationById(conversationId)
        val totalMsgsInConv = dao.getMessagesForConversation(conversationId).size
        val previewSnippet = content.replace(Regex("\\s+"), " ").trim().take(120)
        if (existingConv != null) {
            dao.upsertConversation(
                existingConv.copy(
                    title = if (!autoTitle.isNullOrBlank()) autoTitle.take(200) else existingConv.title,
                    lastMessagePreview = previewSnippet.ifBlank { existingConv.lastMessagePreview },
                    messageCount = totalMsgsInConv,
                    updatedAtMs = now
                )
            )
        } else {
            dao.upsertConversation(
                LocalConversationEntity(
                    id = conversationId,
                    userId = userId,
                    title = autoTitle?.take(200)?.ifBlank { "AI Conversation" } ?: "AI Conversation",
                    modelId = modelId,
                    folder = "General",
                    lastMessagePreview = previewSnippet.ifBlank { "Conversation active" },
                    messageCount = totalMsgsInConv,
                    createdAtMs = now,
                    updatedAtMs = now
                )
            )
        }
        return messageEntity.toDomain()
    }

    suspend fun syncRemoteConversations(conversations: List<Conversation>) {
        if (conversations.isEmpty()) return
        val entities = conversations.map { LocalConversationEntity.fromDomain(it) }
        dao.upsertConversations(entities)
    }

    suspend fun syncRemoteMessages(messages: List<ChatMessage>) {
        if (messages.isEmpty()) return
        val entities = messages.map { LocalChatMessageEntity.fromDomain(it) }
        dao.insertChatMessages(entities)
    }

    suspend fun updateLocalMessageFeedback(messageId: String, feedback: String) {
        dao.updateMessageFeedback(messageId, feedback, System.currentTimeMillis())
    }

    suspend fun updateLocalMessageContent(messageId: String, updatedText: String) {
        val now = System.currentTimeMillis()
        dao.updateMessageContent(messageId, updatedText, now)
        val msg = dao.getMessageById(messageId)
        if (msg != null) {
            val conv = dao.getConversationById(msg.conversationId)
            if (conv != null) {
                dao.upsertConversation(
                    conv.copy(
                        lastMessagePreview = updatedText.replace(Regex("\\s+"), " ").trim().take(120),
                        updatedAtMs = now
                    )
                )
            }
        }
    }

    suspend fun deleteLocalMessage(conversationId: String, messageId: String) {
        dao.deleteMessageById(messageId)
        val remaining = dao.getMessagesForConversation(conversationId)
        val conv = dao.getConversationById(conversationId)
        if (conv != null) {
            val latestPreview = remaining.lastOrNull()?.content?.replace(Regex("\\s+"), " ")?.trim()?.take(120)
                ?: "No messages yet"
            dao.upsertConversation(
                conv.copy(
                    messageCount = remaining.size,
                    lastMessagePreview = latestPreview,
                    updatedAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun togglePinLocalConversation(conversationId: String, isPinned: Boolean) {
        dao.updateConversationPin(conversationId, isPinned, System.currentTimeMillis())
    }

    suspend fun renameLocalConversation(conversationId: String, newTitle: String, folder: String) {
        dao.updateConversationTitleAndFolder(
            conversationId = conversationId,
            title = newTitle.take(200).ifBlank { "Conversation" },
            folder = folder.take(80).ifBlank { "General" },
            updatedAtMs = System.currentTimeMillis()
        )
    }

    suspend fun toggleShareLocalConversation(conversationId: String, isShared: Boolean, shareId: String) {
        dao.updateConversationShare(conversationId, isShared, shareId, System.currentTimeMillis())
    }

    suspend fun deleteLocalConversation(conversationId: String) {
        dao.deleteMessagesForConversation(conversationId)
        dao.deleteConversationById(conversationId)
    }

    suspend fun clearAllLocalChatHistory(userId: String) {
        dao.clearMessagesForUser(userId)
        dao.clearConversationsForUser(userId)
    }

    // --- Dynamic Feature Registry & Telemetry ---

    suspend fun upsertFeatureOverride(featureId: String, featureJson: String) {
        dao.upsertFeatureOverride(
            PlatformFeatureOverrideEntity(
                featureId = featureId,
                featureJson = featureJson,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeFeatureOverride(featureId: String) {
        dao.deleteFeatureOverride(featureId)
    }

    suspend fun getOrInitVersionState(): AppVersionStateEntity {
        val existing = dao.getAppVersionState()
        if (existing != null) return existing
        val initial = AppVersionStateEntity(
            stateKey = "primary",
            installedVersion = "2.2.0",
            latestReleaseJson = "",
            dismissedVersionPrompt = "",
            seenDiscoveryFeatureIdsCsv = "feat_ai_chat,feat_image_gen"
        )
        dao.saveAppVersionState(initial)
        return initial
    }

    suspend fun saveVersionState(state: AppVersionStateEntity) {
        dao.saveAppVersionState(state.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun recordAuditLog(actor: String, action: String, targetId: String, details: String) {
        dao.insertAuditLog(
            SecurityAuditLogEntity(
                actor = actor,
                action = action,
                targetId = targetId,
                details = details
            )
        )
    }

    suspend fun recordTelemetryEvent(
        featureId: String,
        responseTimeMs: Long,
        isError: Boolean = false,
        usedFallback: Boolean = false,
        estimatedTokens: Int = 350
    ) {
        val current = dao.getTelemetry() ?: PlatformTelemetryEntity()
        val usageObj = try {
            JSONObject(current.featureUsageJson)
        } catch (_: Exception) {
            JSONObject()
        }
        val prevCount = usageObj.optInt(featureId, 0)
        usageObj.put(featureId, prevCount + 1)

        val avgLatency = if (current.totalApiRequests == 0) {
            responseTimeMs.coerceAtLeast(120L)
        } else {
            ((current.lastResponseTimeMs * 3) + responseTimeMs.coerceAtLeast(120L)) / 4
        }

        dao.saveTelemetry(
            current.copy(
                totalApiRequests = current.totalApiRequests + 1,
                apiErrors = current.apiErrors + (if (isError) 1 else 0),
                failedGenerations = current.failedGenerations + (if (isError) 1 else 0),
                fallbackActivations = current.fallbackActivations + (if (usedFallback) 1 else 0),
                lastResponseTimeMs = avgLatency,
                estimatedTokensUsed = current.estimatedTokensUsed + estimatedTokens,
                featureUsageJson = usageObj.toString()
            )
        )
    }
}
