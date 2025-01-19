package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.service.GameTracker
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import junit.framework.TestCase.*

class BalutGameManagerTest {

    @Mock
    private lateinit var statisticsManager: StatisticsManager

    @Mock
    private lateinit var gameTracker: GameTracker

    private lateinit var balutGameManager: BalutGameManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        balutGameManager = BalutGameManager(statisticsManager, gameTracker)
    }

    @Test
    fun `test game initialization`() {
        val gameState = balutGameManager.initializeGame()

        // Verify initial state
        assertEquals(2, gameState.playerScores.size)
        assertTrue(gameState.playerScores[0]?.isEmpty() == true)
        assertTrue(gameState.playerScores[AI_PLAYER_ID.hashCode()]?.isEmpty() == true)
        assertEquals(1, gameState.currentRound)
        assertEquals(BalutGameManager.Companion.MAX_ROLLS, gameState.rollsLeft)
        assertTrue(gameState.heldDice.isEmpty())
        assertFalse(gameState.isGameOver)
    }

    @Test
    fun `test player turn handling`() {
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = 3
        )
        val diceResults = listOf(1, 2, 3, 4, 5)
        val heldDice = setOf(0, 1) // Hold first two dice

        val updatedState = balutGameManager.handleTurn(diceResults, initialState, heldDice)

        // Verify turn state
        assertEquals(2, updatedState.rollsLeft)
        assertEquals(heldDice, updatedState.heldDice)
    }

    @Test
    fun `test scoring number category - Ones`() {
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = 2,  // Player has used one roll
            playerScores = mapOf(
                0 to emptyMap(),
                AI_PLAYER_ID.hashCode() to emptyMap()
            )
        )
        val diceResults = listOf(1, 1, 2, 3, 4) // Two ones

        val updatedState = balutGameManager.scoreCategory(initialState, diceResults, "Ones")

        // Verify scoring and state transition
        assertEquals(2, updatedState.playerScores[0]?.get("Ones"))
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
        assertEquals(BalutGameManager.MAX_ROLLS, updatedState.rollsLeft)
        assertTrue(updatedState.heldDice.isEmpty())
        assertFalse(updatedState.isGameOver)
    }

    @Test
    fun `test scoring number category - Sixes`() {
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = 1,  // Player has used two rolls
            playerScores = mapOf(
                0 to emptyMap(),
                AI_PLAYER_ID.hashCode() to emptyMap()
            )
        )
        val diceResults = listOf(6, 6, 6, 2, 1) // Three sixes

        val updatedState = balutGameManager.scoreCategory(initialState, diceResults, "Sixes")

        // Verify scoring and state transition
        assertEquals(18, updatedState.playerScores[0]?.get("Sixes"))
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
        assertEquals(BalutGameManager.MAX_ROLLS, updatedState.rollsLeft)
        assertTrue(updatedState.heldDice.isEmpty())
    }

    @Test
    fun `test invalid scoring attempt without rolling`() {
        // Test human player - should not be able to score without rolling
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = BalutGameManager.MAX_ROLLS,  // Haven't rolled yet
            playerScores = mapOf(
                0 to emptyMap(),
                AI_PLAYER_ID.hashCode() to emptyMap()
            )
        )
        val diceResults = listOf(1, 1, 1, 1, 1)

        val updatedState = balutGameManager.scoreCategory(initialState, diceResults, "Ones")

        // Verify state remains unchanged for human player
        assertEquals(initialState, updatedState)

        // Test AI player - should be able to score without rolling
        val aiInitialState = initialState.copy(currentPlayerIndex = AI_PLAYER_ID.hashCode())
        val aiUpdatedState = balutGameManager.scoreCategory(aiInitialState, diceResults, "Ones")

        // Verify AI can score without rolling
        assertEquals(5, aiUpdatedState.playerScores[AI_PLAYER_ID.hashCode()]?.get("Ones"))
        assertEquals(0, aiUpdatedState.currentPlayerIndex)
        assertEquals(BalutGameManager.MAX_ROLLS, aiUpdatedState.rollsLeft)
        assertTrue(aiUpdatedState.heldDice.isEmpty())
    }

    @Test
    fun `test scoring five of a kind`() {
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = 1,
            playerScores = mapOf(
                0 to emptyMap(),
                AI_PLAYER_ID.hashCode() to emptyMap()
            )
        )
        val diceResults = listOf(3, 3, 3, 3, 3, 4) // Five threes

        val updatedState = balutGameManager.scoreCategory(initialState, diceResults, "Five of a Kind")

        // Verify scoring and state transition
        assertEquals(50, updatedState.playerScores[0]?.get("Five of a Kind"))
        assertEquals(AI_PLAYER_ID.hashCode(), updatedState.currentPlayerIndex)
        assertEquals(BalutGameManager.MAX_ROLLS, updatedState.rollsLeft)
        assertTrue(updatedState.heldDice.isEmpty())
    }

    @Test
    fun `test game completion`() {
        // Create a state where all categories are filled except one
        val allCategories = BalutGameManager.CATEGORIES.associateWith { 10 }.toMutableMap()
        allCategories.remove("Choice") // Leave one category empty
        
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = 2,
            playerScores = mapOf(
                0 to allCategories,
                AI_PLAYER_ID.hashCode() to emptyMap()
            )
        )
        val diceResults = listOf(1, 2, 3, 4, 5)

        // Score the last category
        val updatedState = balutGameManager.scoreCategory(initialState, diceResults, "Choice")

        // Verify game completion
        assertTrue(updatedState.isGameOver)
        assertEquals(15, updatedState.playerScores[0]?.get("Choice"))
        assertTrue(BalutGameManager.CATEGORIES.all { it in updatedState.playerScores[0].orEmpty() })
    }

    @Test
    fun `test no rolls left`() {
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            rollsLeft = 0
        )
        val diceResults = listOf(1, 2, 3, 4, 5)
        val heldDice = setOf(0, 1)

        val updatedState = balutGameManager.handleTurn(diceResults, initialState, heldDice)

        // Verify state remains unchanged when no rolls left
        assertEquals(initialState, updatedState)
    }

    @Test
    fun `test round advancement`() {
        val initialState = balutGameManager.initializeGame().copy(
            currentPlayerIndex = 0,
            currentRound = 1,
            rollsLeft = 2,
            playerScores = mapOf(
                0 to emptyMap(),
                AI_PLAYER_ID.hashCode() to emptyMap()
            )
        )
        val diceResults = listOf(1, 1, 1, 1, 1)

        // Score a category for both players to advance the round
        val firstPlayerTurn = balutGameManager.scoreCategory(initialState, diceResults, "Ones")
        val secondPlayerTurn = balutGameManager.scoreCategory(firstPlayerTurn, diceResults, "Ones")

        // Verify round advanced and state transitions
        assertEquals(2, secondPlayerTurn.currentRound)
        assertEquals(0, secondPlayerTurn.currentPlayerIndex)
        assertEquals(5, firstPlayerTurn.playerScores[0]?.get("Ones"))
        assertEquals(5, secondPlayerTurn.playerScores[AI_PLAYER_ID.hashCode()]?.get("Ones"))
        assertEquals(BalutGameManager.MAX_ROLLS, secondPlayerTurn.rollsLeft)
        assertTrue(secondPlayerTurn.heldDice.isEmpty())
    }
}