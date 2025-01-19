package com.mayor.kavi.util

import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMessagesTest {

    @Test
    fun `test buildPigScoreMessage for game over - AI wins`() {
        val message = GameMessages.buildPigScoreMessage(
            die = 0,
            score = 100,
            playerIndex = AI_PLAYER_ID.hashCode(),
            isGameOver = true
        )
        assertEquals("AI wins with 100 points! Better luck next time!", message)
    }

    @Test
    fun `test buildPigScoreMessage for game over - Player wins`() {
        val message = GameMessages.buildPigScoreMessage(
            die = 0,
            score = 100,
            playerIndex = 0,
            isGameOver = true
        )
        assertEquals("You win with 100 points!", message)
    }

    @Test
    fun `test buildGreedScoreMessage for game over - AI wins`() {
        val message = GameMessages.buildGreedScoreMessage(
            dice = emptyList(),
            score = 10000,
            turnScore = 0,
            playerIndex = AI_PLAYER_ID.hashCode(),
            isGameOver = true
        )
        assertEquals("AI wins with 10000 points! Better luck next time!", message)
    }

    @Test
    fun `test buildGreedScoreMessage for game over - Player wins`() {
        val message = GameMessages.buildGreedScoreMessage(
            dice = emptyList(),
            score = 10000,
            turnScore = 0,
            playerIndex = 0,
            isGameOver = true
        )
        assertEquals("You win with 10000 points!", message)
    }

    @Test
    fun `test buildGreedScoreMessage for normal turn with roll`() {
        val message = GameMessages.buildGreedScoreMessage(
            dice = listOf(1, 2, 3, 4, 5),
            score = 500,
            turnScore = 150,
            playerIndex = 0,
            isGameOver = false
        )
        assertEquals("You rolled: 1, 2, 3, 4, 5\nTurn Score: 150", message)
    }

    @Test
    fun `test buildGreedScoreMessage for new player with no score`() {
        val message = GameMessages.buildGreedScoreMessage(
            dice = emptyList(),
            score = 0,
            turnScore = 0,
            playerIndex = 0,
            isGameOver = false
        )
        assertEquals("Your turn\nYou need 800 points to start scoring", message)
    }

    @Test
    fun `test buildGreedScoreMessage for no scoring dice`() {
        val message = GameMessages.buildGreedScoreMessage(
            dice = listOf(2, 3, 2, 4, 6),
            score = 500,
            turnScore = 0,
            playerIndex = 0,
            isGameOver = false
        )
        assertEquals("You rolled: 2, 3, 2, 4, 6\nNo scoring dice - turn ended!", message)
    }

    @Test
    fun `test buildBalutCategoryMessage for game over - AI wins`() {
        val message = GameMessages.buildBalutCategoryMessage(
            category = "",
            playerIndex = AI_PLAYER_ID.hashCode(),
            isGameOver = true,
            totalScore = 300
        )
        assertEquals("AI wins with 300 points! Better luck next time!", message)
    }

    @Test
    fun `test buildBalutCategoryMessage for normal turn with category`() {
        val message = GameMessages.buildBalutCategoryMessage(
            category = "Full House",
            playerIndex = 0,
            isGameOver = false,
            totalScore = 35
        )
        assertEquals("You - Category: Full House, Total Score: 35", message)
    }

    @Test
    fun `test buildBalutCategoryMessage for empty category`() {
        val message = GameMessages.buildBalutCategoryMessage(
            category = "",
            playerIndex = 0,
            isGameOver = false
        )
        assertEquals("Your turn", message)
    }

    @Test
    fun `test buildBalutCategoryMessage for AI turn with category`() {
        val message = GameMessages.buildBalutCategoryMessage(
            category = "Five of a Kind",
            playerIndex = AI_PLAYER_ID.hashCode(),
            isGameOver = false,
            totalScore = 50
        )
        assertEquals("AI - Category: Five of a Kind, Total Score: 50", message)
    }
}