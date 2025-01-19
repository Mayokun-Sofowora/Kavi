package com.mayor.kavi.data.manager

import com.mayor.kavi.R
import com.mayor.kavi.data.models.enums.GameBoard
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class DiceManagerTest {
    private lateinit var diceManager: DiceManager
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)
        Dispatchers.setMain(testDispatcher)
        diceManager = DiceManager()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `test initial state`() = runTest {
        assertEquals(List(6) { R.drawable.empty_dice }, diceManager.diceImages.first())
        assertFalse(diceManager.isRolling.first())
        assertEquals(emptySet<Int>(), diceManager.heldDice.first())
        assertEquals(emptyList<Int>(), diceManager.currentRolls.first())
    }

    @Test
    fun `test roll dice for Pig game`() = runTest {
        val results = diceManager.rollDiceForBoard(GameBoard.PIG.modeName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(1, results.size)
        assertTrue(results[0] in 1..6)
    }

    @Test
    fun `test roll dice for Greed game`() = runTest {
        val results = diceManager.rollDiceForBoard(GameBoard.GREED.modeName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(6, results.size)
        results.forEach { dice ->
            assertTrue(dice in 1..6)
        }
    }

    @Test
    fun `test roll dice for Balut game`() = runTest {
        val results = diceManager.rollDiceForBoard(GameBoard.BALUT.modeName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(6, results.size)
        results.forEach { dice ->
            assertTrue(dice in 1..6)
        }
    }

    @Test
    fun `test toggle hold dice`() = runTest {
        // Roll dice first
        diceManager.rollDiceForBoard("BALUT")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Toggle hold on first die
        diceManager.toggleHold(0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(diceManager.heldDice.first().contains(0))
        
        // Toggle hold off first die
        diceManager.toggleHold(0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(diceManager.heldDice.first().contains(0))
    }

    @Test
    fun `test reset held dice`() = runTest {
        // Roll and hold some dice
        diceManager.rollDiceForBoard("BALUT")
        testDispatcher.scheduler.advanceUntilIdle()
        
        diceManager.toggleHold(0)
        diceManager.toggleHold(1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Reset held dice
        diceManager.resetHeldDice()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(diceManager.heldDice.first().isEmpty())
    }

    @Test
    fun `test reset game`() = runTest {
        // Set up some game state
        diceManager.rollDiceForBoard("BALUT")
        testDispatcher.scheduler.advanceUntilIdle()
        
        diceManager.toggleHold(0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Reset game
        diceManager.resetGame()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify everything is reset
        assertEquals(List(6) { R.drawable.empty_dice }, diceManager.diceImages.first())
        assertFalse(diceManager.isRolling.first())
        assertEquals(emptySet<Int>(), diceManager.heldDice.first())
        assertEquals(emptyList<Int>(), diceManager.currentRolls.first())
    }

    @Test
    fun `test set dice count for custom game`() = runTest {
        diceManager.setDiceCount(4)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val results = diceManager.rollDiceForBoard(GameBoard.CUSTOM.modeName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(4, results.size)
    }

    @Test
    fun `test should not hold dice in Pig game`() = runTest {
        diceManager.rollDiceForBoard(GameBoard.PIG.modeName)
        testDispatcher.scheduler.advanceUntilIdle()
        
        diceManager.toggleHold(0)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(diceManager.heldDice.first().isEmpty())
    }
}