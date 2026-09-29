package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UsageCategory
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KalleshHubRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createConversation_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = KalleshHubRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createConversation("Kotlin Architecture Chat", "gemini-3.5-flash", "Coding")
        }
        assertTrue(createResult.isSuccess)
        val convId = createResult.getOrThrow()
        assertTrue(convId.isNotEmpty())
    }

    @Test
    fun getUserConversations_authenticatedOwner_returnsMatchingConversations() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KalleshHubRepository(firestore, auth)
        val convId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createConversation("My Saved Thread").getOrThrow()
        }

        val listResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserConversations(uid)
        }
        assertTrue(listResult.isSuccess)
        assertTrue(listResult.getOrThrow().map { it.id }.contains(convId))
    }

    @Test
    fun observeConversations_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = KalleshHubRepository(firestore, auth)
        val convId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createConversation("Realtime Thread").getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeConversations(uid).first { list -> list.any { it.id == convId } }
        }
        assertTrue(emitted.map { it.id }.contains(convId))
    }

    @Test
    fun getConversationById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = KalleshHubRepository(firestore, auth)
        val convId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createConversation("Alice Private Chat").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = KalleshHubRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getConversationById(convId)
        }
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as? FirebaseFirestoreException
        assertNotNull(ex)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, ex?.code)
    }

    @Test
    fun observeConversations_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = KalleshHubRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeConversations("unauthenticated_uid").first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (e: Exception) {
            val firestoreEx = generateSequence<Throwable>(e) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            assertNotNull(firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun checkAndIncrementUsage_andProfileFounderIntro_succeedForOwner() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = KalleshHubRepository(firestore, auth)

        val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserProfile("Alice", ALICE_EMAIL, null)
        }
        assertTrue("Profile error: ${profileResult.exceptionOrNull()}", profileResult.isSuccess)

        val introUpdate = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.markFounderIntroSeen()
        }
        assertTrue("Intro update error: ${introUpdate.exceptionOrNull()}", introUpdate.isSuccess)

        val usageResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.checkAndIncrementUsage(UsageCategory.CHAT, SubscriptionPlan.FREE)
        }
        assertTrue("Usage error: ${usageResult.exceptionOrNull()}", usageResult.isSuccess)
        assertTrue(usageResult.getOrThrow().chatCount >= 1)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_kallesh@test.com"
        const val BOB_EMAIL = "bob_kallesh@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
