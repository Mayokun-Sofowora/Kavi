package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.models.GameScoreState
import com.mayor.kavi.data.models.enums.PlayStyle
import com.mayor.kavi.data.service.GameTracker
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

/**
 * Manager class for the Pig dice game variant.
 *
 * The Pig game is a simple dice game where players take turns rolling a die.
 * On their turn, a player can:
 * - Roll the die and accumulate points
 * - Bank their accumulated points
 * - Lose all accumulated points if they roll a 1
 *
 * The game ends when a player reaches or exceeds [WINNING_SCORE] points.
 *
 * @property statisticsManager Manages game statistics and AI behavior analysis
 */
class PigGameManager @Inject constructor(
    private val statisticsManager: StatisticsManager,
    private val gameTracker: GameTracker
) {

    companion object {
        /** Score required to win the game */
        const val WINNING_SCORE = 100

        /** Die value that causes a player to lose their turn and accumulated points */
        const val BUST_VALUE = 1
    }

    private var _turnStartTime: Long = 0

    /**
     * Initializes a new Pig game.
     *
     * Creates a new game state with:
     * - Random starting player selection
     * - Zero scores for both players
     * - Game timer initialization
     *
     * @return A new [GameScoreState.PigScoreState] with initial game settings
     */
    fun initializeGame(): GameScoreState.PigScoreState {
        val startingPlayer = if (Math.random() < 0.5) 0 else AI_PLAYER_ID.hashCode()
        return GameScoreState.PigScoreState(
            playerScores = mutableMapOf(
                0 to 0,
                AI_PLAYER_ID.hashCode() to 0
            ),
            currentPlayerIndex = startingPlayer,
            isGameOver = false,
            currentTurnScore = 0
        )
    }

    /**
     * Handles a player's turn in the game.
     *
     * Processes the dice roll result and updates the game state accordingly:
     * - For player turns: Updates score or switches turn on bust
     * - For AI turns: Makes strategic decisions about continuing or banking
     *
     * @param currentState Current state of the game
     * @param diceResult Result of the die roll (null if no roll has occurred)
     * @return Updated game state after processing the turn
     */
    fun handleTurn(
        currentState: GameScoreState.PigScoreState,
        diceResult: Int? = null
    ): GameScoreState.PigScoreState =
        when (currentState.currentPlayerIndex) {
            AI_PLAYER_ID.hashCode() -> handleAITurn(currentState, diceResult)
            else -> handlePlayerTurn(currentState, diceResult)
        }

    private fun handlePlayerTurn(
        currentState: GameScoreState.PigScoreState,
        diceResult: Int?
    ): GameScoreState.PigScoreState {
        if (diceResult != null) {
            if (diceResult == BUST_VALUE) {
                return switchTurn(currentState)
            }
            // Continue turn with accumulated score
            return currentState.copy(
                currentTurnScore = currentState.currentTurnScore + diceResult
            )
        }
        return currentState
    }

    private fun handleAITurn(
        currentState: GameScoreState.PigScoreState,
        diceResult: Int?
    ): GameScoreState.PigScoreState {
        if (diceResult == null) return currentState
        return if (diceResult == BUST_VALUE) {
            // AI rolled a 1, lose turn and points
            currentState.copy(
                currentTurnScore = 0,
                currentPlayerIndex = 0
            )
        } else {
            // Add to current turn score
            val newTurnScore = currentState.currentTurnScore + diceResult
            val updatedState = currentState.copy(
                currentTurnScore = newTurnScore
            )
            // AI decides whether to bank or continue
            if (shouldAIBank(
                    currentTurnScore = newTurnScore,
                    aiTotalScore = updatedState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0,
                    playerTotalScore = updatedState.playerScores[0] ?: 0
                )
            ) {
                bankScore(updatedState)
            } else {
                updatedState
            }
        }
    }

    /**
     * Banks the current turn's score for the active player.
     *
     * This method:
     * 1. Adds accumulated turn score to player's total
     * 2. Records banking statistics
     * 3. Checks for game completion
     * 4. Updates game state and switches turns if game continues
     *
     * @param currentState Current state of the game
     * @return Updated game state after banking the score
     */
    fun bankScore(currentState: GameScoreState.PigScoreState): GameScoreState.PigScoreState {
        val currentPlayerScore = currentState.playerScores[currentState.currentPlayerIndex] ?: 0
        val newScore = currentPlayerScore + currentState.currentTurnScore
        val updatedState = currentState.copy(
            playerScores = currentState.playerScores.toMutableMap().apply {
                put(currentState.currentPlayerIndex, newScore)
            },
            currentTurnScore = 0
        )
        // Check if game is over and update statistics
        if (newScore >= WINNING_SCORE) {
            val isPlayerWin = currentState.currentPlayerIndex != AI_PLAYER_ID.hashCode()
            val wasComeback = checkIfComeback(currentState)
            val wasCloseGame = checkIfCloseGame(updatedState)
            statisticsManager.updateGameStatistics(
                gameMode = "PIG",
                score = newScore,
                isWin = isPlayerWin,
                wasComeback = wasComeback,
                wasCloseGame = wasCloseGame
            )
            return updatedState.copy(isGameOver = true)
        }
        return switchTurn(updatedState)
    }

    private fun checkIfComeback(state: GameScoreState.PigScoreState): Boolean {
        val playerScore = state.playerScores[0] ?: 0
        val aiScore = state.playerScores[AI_PLAYER_ID.hashCode()] ?: 0
        // Consider it a comeback if player was behind by 50+ points
        return state.currentPlayerIndex == 0 && (aiScore - playerScore) >= 50
    }

    private fun checkIfCloseGame(state: GameScoreState.PigScoreState): Boolean {
        val scores = state.playerScores.values.toList()
        if (scores.size != 2) return false
        // Consider it a close game if the difference is 5 points or less
        return abs(scores[0] - scores[1]) <= 5
    }

    private fun switchTurn(state: GameScoreState.PigScoreState): GameScoreState.PigScoreState {
        _turnStartTime = System.currentTimeMillis() // Reset turn timer
        return state.copy(
            currentPlayerIndex = if (state.currentPlayerIndex == 0)
                AI_PLAYER_ID.hashCode()
            else
                0,
            currentTurnScore = 0
        )
    }

    fun shouldAIBank(currentTurnScore: Int, aiTotalScore: Int, playerTotalScore: Int): Boolean {
        // If AI can win by banking, always bank
        if (aiTotalScore + currentTurnScore >= WINNING_SCORE) {
            gameTracker.trackDecision()
            gameTracker.trackBanking(currentTurnScore)
            return true
        }
        val currentState = GameScoreState.PigScoreState(
            playerScores = mutableMapOf(
                0 to playerTotalScore,
                AI_PLAYER_ID.hashCode() to aiTotalScore
            ),
            currentPlayerIndex = AI_PLAYER_ID.hashCode(),
            currentTurnScore = currentTurnScore,
            isGameOver = false
        )
        val isCloseGame = checkIfCloseGame(currentState)
        val wouldBeComeback = checkIfComeback(currentState)
        val playerAnalysis = statisticsManager.playerAnalysis.value
        val playerStyle = playerAnalysis?.playStyle ?: PlayStyle.BALANCED
        // Base chance to bank increases with score
        val baseChance = when {
            currentTurnScore >= 20 -> 0.8
            currentTurnScore >= 15 -> 0.6
            currentTurnScore >= 10 -> 0.4
            else -> 0.2
        }
        // Adjust chance based on game situation
        val situationalAdjustment = when {
            isCloseGame -> 0.2  // More likely to bank in close games
            wouldBeComeback -> -0.2  // Less likely to bank when behind
            playerTotalScore >= 75 -> 0.3  // More likely to bank when player is close to winning
            aiTotalScore >= 75 -> -0.1  // Less likely to bank when AI is close to winning
            else -> 0.0
        }
        // Adjust based on player style
        val styleAdjustment = when (playerStyle) {
            PlayStyle.AGGRESSIVE -> -0.1
            PlayStyle.CAUTIOUS -> 0.1
            else -> 0.0
        }
        val finalChance = (baseChance + situationalAdjustment + styleAdjustment).coerceIn(0.1, 0.9)
        // If we decide to bank based on probability
        if (Random.nextDouble() < finalChance) {
            gameTracker.trackDecision()
            gameTracker.trackBanking(currentTurnScore)
            return true
        }
        return false
    }
}
