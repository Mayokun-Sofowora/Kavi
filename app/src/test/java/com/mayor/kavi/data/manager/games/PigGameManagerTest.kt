package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.service.GameTracker
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import junit.framework.TestCase.*

class PigGameManagerTest {

    @Mock
    private lateinit var statisticsManager: StatisticsManager

    @Mock
    private lateinit var gameTracker: GameTracker

    private lateinit var pigGameManager: PigGameManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        pigGameManager = PigGameManager(statisticsManager, gameTracker)
    }

    @Test
    fun `test game initialization`() {
        val gameState = pigGameManager.initializeGame()

        // Verify initial state
        assertNotNull(gameState.playerScores)
        assertEquals(2, gameState.playerScores.size)
        assertEquals(0, gameState.playerScores[0])
        assertEquals(0, gameState.playerScores[AI_PLAYER_ID.hashCode()])
        assertEquals(0, gameState.currentTurnScore)
        assertFalse(gameState.isGameOver)
    }

    @Test
    fun `test player rolls 1 and loses turn`() {
        val initialState = pigGameManager.initializeGame().copy(currentPlayerIndex = 0)
        val updatedState = pigGameManager.handleTurn(initialState, 1)

        // Verify turn was switched and score was reset
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
        assertEquals(0, updatedState.currentTurnScore)
        assertEquals(0, updatedState.playerScores[0])
    }

    @Test
    fun `test player accumulates score on valid roll`() {
        val initialState = pigGameManager.initializeGame().copy(currentPlayerIndex = 0)
        val updatedState = pigGameManager.handleTurn(initialState, 4)

        // Verify score accumulation
        assertEquals(4, updatedState.currentTurnScore)
        assertEquals(0, updatedState.playerScores[0]) // Score not banked yet
    }

    @Test
    fun `test banking score`() {
        val initialState = pigGameManager.initializeGame()
            .copy(currentPlayerIndex = 0, currentTurnScore = 10)
        val updatedState = pigGameManager.bankScore(initialState)

        // Verify score was banked and turn switched
        assertEquals(10, updatedState.playerScores[0])
        assertEquals(0, updatedState.currentTurnScore)
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
    }

    @Test
    fun `test winning condition`() {
        val initialState = pigGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentTurnScore = 10,
            playerScores = mutableMapOf(
                0 to 95,
                AI_PLAYER_ID.hashCode() to 50
            )
        )
        val updatedState = pigGameManager.bankScore(initialState)

        // Verify game over condition
        assertTrue(updatedState.isGameOver)
        assertEquals(105, updatedState.playerScores[0])
    }
} 