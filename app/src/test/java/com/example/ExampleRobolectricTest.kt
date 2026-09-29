package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KalleshLocalDatabase
import com.example.data.local.LocalCacheRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kallesh AI Hub", appName)
    }

    @Test
    fun `room database persists conversations and chat history messages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KalleshLocalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = LocalCacheRepository(db.localDao())

        val userId = "test_user_kallesh"
        val createdConv = repo.createLocalConversation(
            userId = userId,
            title = "Kotlin Architecture Chat",
            modelId = "gemini-3.5-flash",
            folder = "Coding"
        )

        repo.saveLocalChatMessage(
            conversationId = createdConv.id,
            userId = userId,
            role = "user",
            content = "How do I persist chat history with Room?",
            modelId = "gemini-3.5-flash"
        )

        repo.saveLocalChatMessage(
            conversationId = createdConv.id,
            userId = userId,
            role = "model",
            content = "Use @Entity, @Dao returning Flow<List<T>>, and a Repository.",
            modelId = "gemini-3.5-flash",
            citations = listOf("https://developer.android.com/training/data-storage/room")
        )

        val conversations = repo.observeConversations(userId).first()
        assertEquals(1, conversations.size)
        assertEquals("Kotlin Architecture Chat", conversations.first().title)
        assertEquals(2, conversations.first().messageCount)
        assertTrue(conversations.first().lastMessagePreview.contains("Use @Entity"))

        val messages = repo.observeMessages(createdConv.id).first()
        assertEquals(2, messages.size)
        assertEquals("user", messages[0].role)
        assertEquals("model", messages[1].role)
        assertEquals(1, messages[1].citations.size)

        val searchHits = repo.searchMessages(userId, "Repository").first()
        assertEquals(1, searchHits.size)

        repo.deleteLocalConversation(createdConv.id)
        assertEquals(0, repo.observeConversations(userId).first().size)
        assertEquals(0, repo.observeMessages(createdConv.id).first().size)

        db.close()
    }
}
