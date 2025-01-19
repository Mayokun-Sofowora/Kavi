package com.mayor.kavi.util

import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID

/**
 * Utility object for building game messages.
 */
object GameMessages {
    /**
     * Gets the name of a player based on their index.
     *
     * @param playerIndex The index of the player.
     * @return The player's name.
     */
    private fun getPlayerName(playerIndex: Int): String =
        if (playerIndex == AI_PLAYER_ID.hashCode()) "AI" else "You"

    /**
     * Gets the possessive form of a player's name based on their index.
     *
     * @param playerIndex The index of the player.
     * @return The string of the player's possessive form.
     */
    private fun getPossessivePlayer(playerIndex: Int): String =
        if (playerIndex == AI_PLAYER_ID.hashCode()) "AI's" else "Your"

    /**
     * Builds a message for the Pig game score.
     *
     * @param die The value of the die roll.
     * @param score The current score.
     * @param playerIndex The index of the player.
     * @param isGameOver Whether the game is over.
     * @param hasRolled Whether the player has rolled the die.
     * @return The built message.
     */
    fun buildPigScoreMessage(
        die: Int,
        score: Int,
        playerIndex: Int,
        isGameOver: Boolean,
        hasRolled: Boolean = false
    ): String {
        if (isGameOver) {
            val winner = getPlayerName(playerIndex)
            return if (playerIndex == AI_PLAYER_ID.hashCode())
                "$winner wins with $score points! Better luck next time!"
            else "$winner win with $score points!"
        }
        return buildString {
            if (hasRolled) {
                if (die > 0) append("${getPlayerName(playerIndex)} rolled $die")
            } else {
                append("You loose a turn if you roll a 1")
                append("\n${getPossessivePlayer(playerIndex)} turn to roll")
            }
        }.trim()
    }

    /**
     * Builds a message for the Greed game score.
     *
     * @param dice The list of die values.
     * @param score The current score.
     * @param turnScore The score for the current turn.
     * @param playerIndex The index of the player.
     * @param isGameOver Whether the game is over.
     * @return The built message.
     */
    fun buildGreedScoreMessage(
        dice: List<Int>,
        score: Int,
        turnScore: Int,
        playerIndex: Int,
        isGameOver: Boolean = false
    ): String {
        if (isGameOver) {
            val winner = getPlayerName(playerIndex)
            return if (playerIndex == AI_PLAYER_ID.hashCode())
                "$winner wins with $score points! Better luck next time!"
            else
                "$winner win with $score points!"
        }
        return buildString {
            if (dice.isNotEmpty()) {
                append("${getPlayerName(playerIndex)} rolled: ${dice.joinToString()}")
                // Add message for bust or bad roll
                if (turnScore == 0 && dice.isNotEmpty()) append("\nNo scoring dice - turn ended!")
            } else append("${getPossessivePlayer(playerIndex)} turn")
            if (turnScore > 0) append("\nTurn Score: $turnScore")
            if (score == 0) append("\nYou need 800 points to start scoring")
        }.trim()
    }

    /**
     * Builds a message for the Balut game category.
     *
     * @param category The selected category.
     * @param playerIndex The index of the player.
     * @param isGameOver Whether the game is over.
     * @param totalScore The total score.
     * @return The built message.
     */
    fun buildBalutCategoryMessage(
        category: String,
        playerIndex: Int,
        isGameOver: Boolean = false,
        totalScore: Int = 0
    ): String = when {
        isGameOver -> buildEndGameMessage(totalScore, playerIndex)
        category.isEmpty() -> when (playerIndex) {
            0 -> "${getPossessivePlayer(playerIndex)} turn"
            else -> "${getPossessivePlayer(playerIndex)} turn"
        }

        else -> buildString {
            append("${getPlayerName(playerIndex)} - ")
            append("Category: $category")
            if (totalScore > 0) append(", Total Score: $totalScore")
        }
    }

    private fun buildEndGameMessage(totalScore: Int, playerIndex: Int): String {
        val playerName = getPlayerName(playerIndex)
        return if (playerIndex == AI_PLAYER_ID.hashCode())
            "$playerName wins with $totalScore points! Better luck next time!"
        else
            "$playerName win with $totalScore points!"
    }
}
