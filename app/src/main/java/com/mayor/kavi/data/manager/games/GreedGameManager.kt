package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.models.GameScoreState.GreedScoreState
import com.mayor.kavi.data.models.enums.PlayStyle
import com.mayor.kavi.data.service.GameTracker
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import com.mayor.kavi.util.ScoreCalculator
import javax.inject.Inject
import kotlin.random.Random

/**
 * Manager class for the Greed dice game variant.
 *
 * Greed is a dice game where players roll multiple dice and try to accumulate points
 * through various scoring combinations. Players can:
 * - Roll dice and select which ones to keep
 * - Bank their accumulated points
 * - Risk losing points if no scoring dice are rolled
 *
 * The game ends when a player reaches or exceeds [WINNING_SCORE] points.
 * Players must achieve a minimum score of [MINIMUM_STARTING_SCORE] to start banking points.
 *
 * @property statisticsManager Manages game statistics and AI behavior analysis
 */
class GreedGameManager @Inject constructor(
    private val statisticsManager: StatisticsManager,
    private val gameTracker: GameTracker
) {
    companion object {
        /** Score required to win the game */
        const val WINNING_SCORE = 10000

        /** Minimum score required to start banking points */
        const val MINIMUM_STARTING_SCORE = 800
    }

    /**
     * Initializes a new Greed game.
     *
     * Creates a new game state with:
     * - Random starting player selection
     * - Zero scores for both players
     * - Initial game parameters
     *
     * @return A new [GreedScoreState] with initial game settings
     */
    fun initializeGame(): GreedScoreState {
        val startingPlayer = if (Random.nextBoolean()) 0 else AI_PLAYER_ID.hashCode()
        return GreedScoreState(
            playerScores = mapOf(
                0 to 0,
                AI_PLAYER_ID.hashCode() to 0
            ),
            currentPlayerIndex = startingPlayer,
            isGameOver = false,
            currentTurnScore = 0,
            canReroll = true,
            lastRoll = emptyList()
        )
    }

    /**
     * Handles a turn in the game, processing dice results and updating game state.
     *
     * Different handling for:
     * - Player turns: Processes held dice and scoring combinations
     * - AI turns: Makes strategic decisions about dice selection and banking
     *
     * @param diceResults Results of the current dice roll
     * @param currentState Current state of the game
     * @param heldDice Set of dice indices that are currently held
     * @return Updated game state after processing the turn
     */
    fun handleTurn(
        diceResults: List<Int>, currentState: GreedScoreState, heldDice: Set<Int>
    ): GreedScoreState {
        return when (currentState.currentPlayerIndex) {
            AI_PLAYER_ID.hashCode() -> handleAITurn(diceResults, currentState)
            0 -> handlePlayerTurn(currentState, diceResults, heldDice)
            else -> currentState
        }
    }

    private fun handlePlayerTurn(
        currentState: GreedScoreState, diceResults: List<Int>, heldDice: Set<Int>
    ): GreedScoreState {
        if (!currentState.canReroll) {
            return currentState
        }
        // If all dice are held, player must bank or risk losing points
        if (heldDice.size == diceResults.size) {
            return currentState.copy(
                canReroll = false,
                heldDice = heldDice
            )
        }
        // If previous roll was hot dice and player didn't reroll all dice, they bust
        if (currentState.scoringDice.isEmpty() && currentState.heldDice.isNotEmpty()) {
            return currentState.copy(
                currentTurnScore = 0,
                heldDice = emptySet(),
                scoringDice = emptySet(),
                lastRoll = diceResults,
                canReroll = false
            )
        }
        // Calculate score only for non-held dice
        val availableDice =
            diceResults.indices.toSet() - currentState.heldDice - currentState.scoringDice
        if (availableDice.isEmpty()) {
            return currentState.copy(
                canReroll = false,
                heldDice = heldDice  // Preserve held dice so UI can show them
            )
        }
        val newDiceResults = availableDice.map { diceResults[it] }
        val (score, scoringDice) = ScoreCalculator.calculateGreedScore(newDiceResults)
        // If no scoring dice in new roll, lose all accumulated points
        if (scoringDice.isEmpty()) {
            return currentState.copy(
                currentTurnScore = 0,
                heldDice = emptySet(),
                scoringDice = emptySet(),
                lastRoll = diceResults,
                canReroll = false
            )
        }
        val newTurnScore = currentState.currentTurnScore + score
        // Map scoring dice to original indices
        val newScoringDiceIndices = scoringDice.map { availableDice.elementAt(it) }.toSet()
        // If all dice are now scoring, force player to reroll all dice (hot dice)
        val allDiceScoring =
            (currentState.scoringDice + newScoringDiceIndices).size == diceResults.size
        val finalHeldDice = if (allDiceScoring) emptySet() else heldDice
        val finalScoringDice =
            if (allDiceScoring) emptySet() else (currentState.scoringDice + newScoringDiceIndices)
        // Check if all dice will be held after this roll
        val allDiceWillBeHeld = finalHeldDice.size == diceResults.size
        return currentState.copy(
            heldDice = finalHeldDice,
            currentTurnScore = newTurnScore,
            lastRoll = diceResults,
            scoringDice = finalScoringDice,
            canReroll = !allDiceWillBeHeld  // Disable reroll if all dice will be held
        )
    }

    private fun handleAITurn(
        diceResults: List<Int>,
        currentState: GreedScoreState
    ): GreedScoreState {
        // If we can't reroll or have no available dice, bank
        if (!currentState.canReroll ||
            (currentState.heldDice.size + currentState.scoringDice.size) == diceResults.size
        ) {
            return bankScore(currentState)
        }
        // Calculate available dice and their scores
        val availableDice =
            diceResults.indices.toSet() - currentState.heldDice - currentState.scoringDice
        val newDiceResults = availableDice.map { diceResults[it] }
        val (score, scoringDice) = ScoreCalculator.calculateGreedScore(newDiceResults)
        // If no scoring dice in new roll, bust
        if (scoringDice.isEmpty()) {
            return currentState.copy(
                currentTurnScore = 0,
                heldDice = emptySet(),
                scoringDice = emptySet(),
                lastRoll = diceResults,
                canReroll = true,
                currentPlayerIndex = 0
            )
        }
        // Calculate new scores
        val newTurnScore = currentState.currentTurnScore + score
        val currentBankedScore = currentState.playerScores[currentState.currentPlayerIndex] ?: 0
        // Convert scoring dice indices to match original dice array
        val newScoringDiceIndices = scoringDice.map { availableDice.elementAt(it) }.toSet()
        // Check for hot dice (all dice scoring)
        val allDiceScoring =
            (currentState.scoringDice + newScoringDiceIndices).size == diceResults.size
        // Decide which dice to hold (unless we have hot dice)
        val finalHeldDice = if (allDiceScoring) {
            emptySet() // Hot dice - must reroll all
        } else {
            decideAIDiceHolds(
                currentScore = newTurnScore,
                scoringCombos = newScoringDiceIndices
            )
        }
        // Update scoring dice set
        val finalScoringDice = if (allDiceScoring) {
            emptySet() // Hot dice - clear scoring dice
        } else {
            currentState.scoringDice + newScoringDiceIndices
        }
        // Decide whether to bank
        if (!allDiceScoring && shouldAIBank(newTurnScore, currentBankedScore)) {
            gameTracker.trackDecision()
            gameTracker.trackBanking(newTurnScore)
            return bankScore(currentState.copy(currentTurnScore = newTurnScore))
        }
        // Continue rolling
        return currentState.copy(
            heldDice = finalHeldDice,
            currentTurnScore = newTurnScore,
            lastRoll = diceResults,
            scoringDice = finalScoringDice,
            canReroll = true
        )
    }

    fun shouldAIBank(currentTurnScore: Int, aiTotalScore: Int): Boolean {
        // Always bank if we can win
        if (aiTotalScore + currentTurnScore >= WINNING_SCORE) return true
        // Never bank below minimum unless we already have points banked
        if (currentTurnScore < MINIMUM_STARTING_SCORE && aiTotalScore == 0) return false
        val playerAnalysis = statisticsManager.playerAnalysis.value
        // Base threshold for banking decision
        val baseThreshold = when (playerAnalysis?.playStyle) {
            PlayStyle.AGGRESSIVE -> 1200  // Riskier play against aggressive players
            PlayStyle.CAUTIOUS -> 900    // Bank earlier against cautious players
            else -> 1000                 // Default threshold
        }
        // Adjust threshold based on game situation
        val situationalAdjustment = when {
            aiTotalScore == 0 -> -200  // More willing to risk when no points banked
            aiTotalScore >= 8000 -> -300  // More aggressive when close to winning
            currentTurnScore >= 2000 -> 500  // More likely to bank big scores
            else -> 0
        }
        val finalThreshold = (baseThreshold + situationalAdjustment).coerceIn(800, 2000)
        // Random factor to make AI less predictable (±100 points)
        val randomFactor = Random.nextInt(-100, 101)
        return currentTurnScore >= finalThreshold + randomFactor
    }

    /**
     * Decides which scoring dice the AI should hold based on game state and score potential
     *
     * @param currentScore Current turn score
     * @param scoringCombos Map of dice indices to their scoring combinations
     * @return Set of dice indices to hold
     */
    private fun decideAIDiceHolds(
        currentScore: Int,
        scoringCombos: Set<Int>
    ): Set<Int> {
        // If we have a good scoring combination, hold it
        if (currentScore >= 800) {
            return scoringCombos
        }
        // Base decision on player analysis and current scores
        val playerAnalysis = statisticsManager.playerAnalysis.value
        val riskTolerance = when (playerAnalysis?.playStyle) {
            PlayStyle.AGGRESSIVE -> 0.7  // More likely to hold scoring dice
            PlayStyle.CAUTIOUS -> 0.4    // More likely to reroll
            else -> 0.55
        }
        // Adjust risk based on current turn score
        val scoreMultiplier = (currentScore / 500.0).coerceAtMost(2.0)
        // If random roll is below adjusted risk tolerance, hold the dice
        return if (Random.nextDouble() < riskTolerance * scoreMultiplier) {
            scoringCombos
        } else {
            emptySet() // Reroll all dice
        }
    }

    /**
     * Banks the current turn's score for the active player.
     *
     * This method:
     * 1. Validates if banking is allowed (minimum score requirement)
     * 2. Updates player's total score
     * 3. Switches to next player
     * 4. Checks for game completion
     *
     * @param currentState Current state of the game
     * @return Updated game state after banking the score
     */
    fun bankScore(currentState: GreedScoreState): GreedScoreState {
        gameTracker.trackDecision()
        gameTracker.trackBanking(currentState.currentTurnScore)
        val currentScore = currentState.playerScores[currentState.currentPlayerIndex] ?: 0
        val canBank = currentState.currentTurnScore >= MINIMUM_STARTING_SCORE || currentScore > 0
        val updatedScores = currentState.playerScores.toMutableMap()
        if (canBank) {
            updatedScores[currentState.currentPlayerIndex] =
                currentScore + currentState.currentTurnScore
        }
        val nextPlayer = if (currentState.currentPlayerIndex == 0) AI_PLAYER_ID.hashCode() else 0
        val isGameOver = checkIfGameOver(updatedScores)
        return currentState.copy(
            playerScores = updatedScores,
            currentPlayerIndex = nextPlayer,
            isGameOver = isGameOver,
            currentTurnScore = 0,
            heldDice = emptySet(),
            scoringDice = emptySet(),
            canReroll = true
        )
    }

    private fun checkIfGameOver(scores: Map<Int, Int>): Boolean {
        return scores.values.any { it >= WINNING_SCORE }
    }
}