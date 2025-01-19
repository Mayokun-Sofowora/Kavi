package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.service.GameTracker
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import junit.framework.TestCase.*

class GreedGameManagerTest {

    @Mock
    private lateinit var statisticsManager: StatisticsManager

    @Mock
    private lateinit var gameTracker: GameTracker

    private lateinit var greedGameManager: GreedGameManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        greedGameManager = GreedGameManager(statisticsManager, gameTracker)
    }

    @Test
    fun `test game initialization`() {
        val gameState = greedGameManager.initializeGame()

        // Verify initial state
        assertEquals(2, gameState.playerScores.size)
        assertEquals(0, gameState.playerScores[0])
        assertEquals(0, gameState.playerScores[AI_PLAYER_ID.hashCode()])
        assertEquals(0, gameState.currentTurnScore)
        assertTrue(gameState.canReroll)
        assertTrue(gameState.lastRoll.isEmpty())
        assertFalse(gameState.isGameOver)
    }

    @Test
    fun `test player turn with no scoring dice`() {
        val initialState = greedGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentTurnScore = 500
        )
        val diceResults = listOf(2, 2, 3, 4, 4) // No scoring combination
        val heldDice = emptySet<Int>()

        val updatedState = greedGameManager.handleTurn(diceResults, initialState, heldDice)

        // Verify turn was lost
        assertEquals(0, updatedState.currentTurnScore)
        assertFalse(updatedState.canReroll)
        assertTrue(updatedState.scoringDice.isEmpty())
    }

    @Test
    fun `test player turn with scoring dice`() {
        val initialState = greedGameManager.initializeGame().copy(currentPlayerIndex = 0)
        val diceResults = listOf(1, 2, 3, 4, 5) // One 1 and one 5 are scoring
        val heldDice = emptySet<Int>()

        val updatedState = greedGameManager.handleTurn(diceResults, initialState, heldDice)

        // Verify score accumulation (100 for 1 and 50 for 5)
        assertEquals(150, updatedState.currentTurnScore)
        assertTrue(updatedState.canReroll)
        assertEquals(2, updatedState.scoringDice.size)
    }

    @Test
    fun `test hot dice scenario`() {
        val initialState = greedGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentTurnScore = 500
        )
        val diceResults = listOf(1, 1, 1, 1, 1) // Five of a kind
        val heldDice = emptySet<Int>()

        val updatedState = greedGameManager.handleTurn(diceResults, initialState, heldDice)

        // Verify hot dice handling (500 + 2000 for five of a kind)
        assertTrue(updatedState.heldDice.isEmpty())
        assertTrue(updatedState.scoringDice.isEmpty())
        assertTrue(updatedState.canReroll)
        assertEquals(2500, updatedState.currentTurnScore)
    }

    @Test
    fun `test banking below minimum score`() {
        val initialState = greedGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentTurnScore = 500 // Below minimum 800
        )

        val updatedState = greedGameManager.bankScore(initialState)

        // Verify score was not banked due to minimum requirement
        assertEquals(0, updatedState.playerScores[0])
        assertEquals(0, updatedState.currentTurnScore)
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
    }

    @Test
    fun `test banking above minimum score`() {
        val initialState = greedGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentTurnScore = 1000 // Above minimum 800
        )

        val updatedState = greedGameManager.bankScore(initialState)

        // Verify score was banked
        assertEquals(1000, updatedState.playerScores[0])
        assertEquals(0, updatedState.currentTurnScore)
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
    }

    @Test
    fun `test winning condition`() {
        val initialState = greedGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentTurnScore = 1000,
            playerScores = mapOf(
                0 to 9500,
                AI_PLAYER_ID.hashCode() to 8000
            )
        )

        val updatedState = greedGameManager.bankScore(initialState)

        // Verify game over condition (total score > 10000)
        assertTrue(updatedState.isGameOver)
        assertEquals(10500, updatedState.playerScores[0])
    }
} 