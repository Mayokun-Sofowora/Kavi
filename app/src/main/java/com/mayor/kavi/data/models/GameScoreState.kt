package com.mayor.kavi.data.models

import kotlinx.serialization.Serializable

/**
 * Represents the state of a game's scoring system.
 * This sealed class is extended to provide specific score states for different dice games.
 */
@Serializable
sealed class GameScoreState {
    /**
     * Indicates whether the game is over.
     */
    abstract val isGameOver: Boolean

    /**
     * The index of the current player whose turn it is.
     */
    abstract val currentPlayerIndex: Int

    /**
     * Represents the score state for the "Pig" dice game.
     *
     * @property playerScores A map of player indices to their respective scores.
     * @property currentTurnScore The score accumulated during the current turn.
     * @property currentPlayerIndex The index of the current player.
     * @property isGameOver Indicates if the game has ended.
     */
    @Serializable
    data class PigScoreState(
        val playerScores: Map<Int, Int> = emptyMap(),
        val currentTurnScore: Int = 0,
        override val currentPlayerIndex: Int = 0,
        override val isGameOver: Boolean = false
    ) : GameScoreState()

    /**
     * Represents the score state for the "Greed" dice game.
     *
     * @property playerScores A map of player indices to their respective scores.
     * @property currentTurnScore The score accumulated during the current turn.
     * @property heldDice A set of dice that the player has chosen to hold.
     * @property scoringDice A set of dice that are contributing to the player's score.
     * @property canReroll Indicates if the player is allowed to reroll dice.
     * @property lastRoll The result of the most recent dice roll.
     * @property currentPlayerIndex The index of the current player.
     * @property isGameOver Indicates if the game has ended.
     */
    @Serializable
    data class GreedScoreState(
        val playerScores: Map<Int, Int> = emptyMap(),
        val currentTurnScore: Int = 0,
        val heldDice: Set<Int> = emptySet(),
        val scoringDice: Set<Int> = emptySet(),
        val canReroll: Boolean = true,
        val lastRoll: List<Int> = emptyList(),
        override val currentPlayerIndex: Int = 0,
        override val isGameOver: Boolean = false
    ) : GameScoreState() {
        companion object {
            /**
             * A map of dice values to their respective scoring points in the "Greed" game.
             */
            val SCORING_VALUES = mapOf(1 to 100, 5 to 50)
        }
    }

    /**
     * Represents the score state for the "Balut" dice game.
     *
     * @property playerScores A map of player indices to their scores for each category.
     * @property rollsLeft The number of rolls remaining in the current turn.
     * @property heldDice A set of dice that the player has chosen to hold.
     * @property currentRound The current round of the game.
     * @property maxRounds The total number of rounds in the game.
     * @property currentPlayerIndex The index of the current player.
     * @property isGameOver Indicates if the game has ended.
     */
    @Serializable
    data class BalutScoreState(
        val playerScores: Map<Int, Map<String, Int>> = emptyMap(),
        val rollsLeft: Int = 4,
        val heldDice: Set<Int> = emptySet(),
        val currentRound: Int = 1,
        val maxRounds: Int = 11,
        override val currentPlayerIndex: Int = 0,
        override val isGameOver: Boolean = false
    ) : GameScoreState()

    /**
     * Represents the score state for a custom dice game.
     *
     * @property diceCount The number of dice used in the game.
     * @property scoreHistory A map of player indices to a list of their score history.
     * @property gameName The name of the custom game.
     * @property playerNames A map of player indices to their respective names.
     * @property notes A list of notes associated with the game.
     * @property playerScores A map of player indices to their respective scores.
     * @property currentPlayerIndex The index of the current player.
     * @property isGameOver Indicates if the game has ended.
     */
    @Serializable
    data class CustomScoreState(
        val diceCount: Int = 2,
        val scoreHistory: Map<Int, List<String>> = emptyMap(),
        val gameName: String = "Custom Dice Game",
        val playerNames: Map<Int, String> = mapOf(0 to "Player 1", 1 to "Player 2"),
        val notes: List<String> = emptyList(),
        val playerScores: Map<Int, Int> = emptyMap(),
        override val currentPlayerIndex: Int = 0,
        override val isGameOver: Boolean = false
    ) : GameScoreState()
}
