package com.mayor.kavi.data.repository

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.mayor.kavi.data.models.*
import com.mayor.kavi.util.Result
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StatisticsRepositoryTest {
    @Mock
    private lateinit var firestore: FirebaseFirestore
    @Mock
    private lateinit var auth: FirebaseAuth
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var documentReference: DocumentReference
    @Mock
    private lateinit var documentSnapshot: DocumentSnapshot
    @Mock
    private lateinit var collectionReference: CollectionReference
    @Mock
    private lateinit var currentUser: FirebaseUser

    private lateinit var repository: StatisticsRepositoryImpl
    private val testUserId = "test-user-id"

    @Before
    fun setup() {
        // Basic mocking setup
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn(testUserId)
        whenever(firestore.collection("users")).thenReturn(collectionReference)
        whenever(collectionReference.document(any())).thenReturn(documentReference)

        repository = StatisticsRepositoryImpl(firestore, auth)
    }

    @Test
    fun `getGameStatistics returns existing statistics`() = runTest {
        val expectedStats = GameStatistics(gamesPlayed = 5)

        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(true)
        whenever(documentSnapshot.toObject(GameStatistics::class.java)).thenReturn(expectedStats)

        val result = repository.getGameStatistics()
        assertTrue(result is Result.Success)
        assertEquals(expectedStats, (result as Result.Success).data)
    }

    @Test
    fun `getGameStatistics creates default statistics when none exist`() = runTest {
        whenever(documentReference.get()).thenReturn(Tasks.forResult(documentSnapshot))
        whenever(documentSnapshot.exists()).thenReturn(false)
        whenever(documentReference.set(any<GameStatistics>())).thenReturn(Tasks.forResult(null))

        val result = repository.getGameStatistics()
        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data.gamesPlayed)
    }

    @Test
    fun `updateGameStatistics updates successfully`() = runTest {
        val stats = GameStatistics(gamesPlayed = 10)
        whenever(documentReference.set(stats)).thenReturn(Tasks.forResult(null))

        val result = repository.updateGameStatistics(stats)
        assertTrue(result is Result.Success)
    }

    @Test
    fun `updatePlayerAnalysis updates successfully`() = runTest {
        val analysis = PlayerAnalysis()
        whenever(documentReference.update("analysis", analysis)).thenReturn(Tasks.forResult(null))

        val result = repository.updatePlayerAnalysis(testUserId, analysis)
        assertTrue(result is Result.Success)
    }

    @Test
    fun `clearUserStatistics clears successfully`() = runTest {
        whenever(documentReference.set(any<GameStatistics>())).thenReturn(Tasks.forResult(null))

        val result = repository.clearUserStatistics()
        assertTrue(result is Result.Success)
    }

    @Test
    fun `operations fail when user is not authenticated`() = runTest {
        // Setup auth to return null user
        whenever(auth.currentUser).thenReturn(null)

        // Test getGameStatistics
        val statsResult = repository.getGameStatistics()
        assertTrue(statsResult is Result.Error)
        assertEquals("User not authenticated", (statsResult as Result.Error).message)

        // Test updateGameStatistics
        val updateResult = repository.updateGameStatistics(GameStatistics())
        assertTrue(updateResult is Result.Error)
        assertEquals("User not authenticated", (updateResult as Result.Error).message)

        // Test clearUserStatistics
        val clearResult = repository.clearUserStatistics()
        assertTrue(clearResult is Result.Error)
        assertEquals("User not authenticated", (clearResult as Result.Error).message)
    }

}