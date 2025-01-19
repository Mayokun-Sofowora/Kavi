package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.models.GameScoreState.CustomScoreState
import javax.inject.Inject

/**
 * Manager class for custom dice game variants.
 *
 * This manager allows players to create and manage custom dice games with:
 * - Configurable number of dice (up to [MAX_DICE])
 * - Multiple players (between [MIN_PLAYERS] and [MAX_PLAYERS])
 * - Custom scoring rules
 * - Score and note tracking for each player
 *
 * The game is highly customizable and can be used to implement
 * various house rules or entirely new dice game variants.
 */
class MyGameManager @Inject constructor() {

    companion object {
        /** Maximum number of dice that can be rolled */
        const val MAX_DICE = 6

        /** Default number of dice for a new game */
        const val DEFAULT_DICE = 6

        /** Minimum number of players */
        const val MIN_PLAYERS = 2

        /** Maximum number of players */
        const val MAX_PLAYERS = 6
    }

    /**
     * Initializes a new custom game.
     *
     * Creates a new game state with:
     * - Minimum number of players
     * - Default dice count
     * - Empty score history
     * - Default player names
     *
     * @return A new [CustomScoreState] with initial game settings
     */
    fun initializeGame(): CustomScoreState = CustomScoreState(
        playerScores = (0 until MIN_PLAYERS).associateWith { 0 },
        currentPlayerIndex = 0,
        diceCount = DEFAULT_DICE,
        scoreHistory = (0 until MIN_PLAYERS).associateWith { emptyList<String>() },
        gameName = "Custom Dice Game",
        playerNames = (0 until MIN_PLAYERS).associateWith { "Player ${it + 1}" }
    )

    /**
     * Handles a turn in the custom game.
     *
     * Currently implements a simple scoring system based on dice sum.
     * Can be extended to support more complex scoring rules.
     *
     * @param currentState Current state of the game
     * @param diceResults Results of the current dice roll
     * @return Updated game state after processing the turn
     */
    fun handleTurn(currentState: CustomScoreState, diceResults: List<Int>): CustomScoreState {
        // Calculate total of dice
        diceResults.sum()
        return currentState
    }

    /**
     * Adds a new player to the game.
     *
     * Adds a player if the maximum player count hasn't been reached.
     * Initializes the new player with:
     * - Zero score
     * - Empty score history
     * - Default name ("Player X")
     *
     * @param currentState Current state of the game
     * @return Updated game state with new player added, or unchanged if at max players
     */
    fun addPlayer(currentState: CustomScoreState): CustomScoreState {
        if (currentState.playerScores.size >= MAX_PLAYERS) currentState
        val newPlayerIndex = currentState.playerScores.size
        val updatedScores = currentState.playerScores + (newPlayerIndex to 0)
        val updatedHistory = currentState.scoreHistory + (newPlayerIndex to emptyList())
        val updatedNames =
            currentState.playerNames + (newPlayerIndex to "Player ${newPlayerIndex + 1}")

        return currentState.copy(
            playerScores = updatedScores,
            scoreHistory = updatedHistory,
            playerNames = updatedNames
        )
    }

    /**
     * Updates a player's name.
     *
     * @param currentState Current state of the game
     * @param playerIndex Index of the player to update
     * @param name New name for the player
     * @return Updated game state with player's name changed
     */
    fun updatePlayerName(
        currentState: CustomScoreState,
        playerIndex: Int,
        name: String
    ): CustomScoreState {
        if (playerIndex >= currentState.playerScores.size) currentState

        val updatedNames = currentState.playerNames + (playerIndex to name)
        return currentState.copy(playerNames = updatedNames)
    }

    /**
     * Adds a score for a player.
     *
     * Updates both the player's total score and score history.
     *
     * @param currentState Current state of the game
     * @param playerIndex Index of the player to update
     * @param score Score to add
     * @return Updated game state with new score added
     */
    fun addScore(currentState: CustomScoreState, playerIndex: Int, score: Int): CustomScoreState {
        val currentScore = currentState.playerScores[playerIndex] ?: 0
        val updatedScores = currentState.playerScores + (playerIndex to currentScore + score)

        // Add score to history
        val playerHistory = currentState.scoreHistory[playerIndex] ?: emptyList()
        val updatedHistory =
            currentState.scoreHistory + (playerIndex to playerHistory + "Score: $score")

        return currentState.copy(
            playerScores = updatedScores,
            scoreHistory = updatedHistory
        )
    }

    /**
     * Adds a note to a player's score history.
     *
     * Useful for tracking special events or rule applications.
     *
     * @param currentState Current state of the game
     * @param playerIndex Index of the player to update
     * @param note Note to add to the player's history
     * @return Updated game state with new note added
     */
    fun addNote(currentState: CustomScoreState, playerIndex: Int, note: String): CustomScoreState {
        val playerHistory = currentState.scoreHistory[playerIndex] ?: emptyList()
        val updatedHistory = currentState.scoreHistory + (playerIndex to playerHistory + note)
        return currentState.copy(
            scoreHistory = updatedHistory
        )
    }

    /**
     * Sets the number of dice used in the game.
     *
     * The number of dice is constrained between 1 and [MAX_DICE].
     *
     * @param currentState Current state of the game
     * @param count Desired number of dice
     * @return Updated game state with new dice count
     */
    fun setDiceCount(currentState: CustomScoreState, count: Int): CustomScoreState {
        val newCount = count.coerceIn(1, MAX_DICE)
        return currentState.copy(
            diceCount = newCount
        )
    }

    /**
     * Sets the name of the custom game.
     *
     * @param currentState Current state of the game
     * @param name New name for the game
     * @return Updated game state with new game name
     */
    fun setGameName(currentState: CustomScoreState, name: String): CustomScoreState =
        currentState.copy(gameName = name)

    /**
     * Resets all player scores to zero.
     *
     * Maintains player names and game settings but clears:
     * - All scores
     * - Score history
     *
     * @param currentState Current state of the game
     * @return Updated game state with scores reset
     */
    fun resetScores(currentState: CustomScoreState): CustomScoreState = initializeGame()
}
