package com.mayor.kavi.data.manager.games

import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.models.GameScoreState.BalutScoreState
import com.mayor.kavi.data.models.enums.PlayStyle
import com.mayor.kavi.data.service.GameTracker
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import com.mayor.kavi.util.ScoreCalculator
import javax.inject.Inject
import kotlin.random.Random

/**
 * Manager class for the Balut dice game variant.
 *
 * Balut is a strategic dice game similar to Yahtzee, where players:
 * - Roll five dice up to three times per turn
 * - Choose which dice to keep between rolls
 * - Score in various categories based on dice combinations
 *
 * The game proceeds through multiple rounds, with each player
 * scoring once in each category. The game ends when all categories
 * are filled, and the player with the highest total score wins.
 *
 * @property statisticsManager Manages game statistics and AI behavior analysis
 */
class BalutGameManager @Inject constructor(
    private val statisticsManager: StatisticsManager,
    private val gameTracker: GameTracker
) {

    companion object {
        /** Maximum number of rolls allowed per turn */
        const val MAX_ROLLS = 3

        /** Available scoring categories in the game */
        val CATEGORIES = setOf(
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Full House", "Four of a Kind", "Small Straight", "Large Straight", "Five of a Kind",
            "Choice"
        )
    }

    /**
     * Initializes a new Balut game.
     *
     * Creates a new game state with:
     * - Random starting player selection
     * - Empty score maps for all players
     * - Initial game parameters (rounds, rolls, etc.)
     *
     * @return A new [BalutScoreState] with initial game settings
     */
    fun initializeGame(): BalutScoreState {
        val startingPlayer = if (Random.nextInt(0, 2) == 0) 0 else AI_PLAYER_ID.hashCode()
        return BalutScoreState(
            playerScores = mapOf(
                0 to emptyMap(),
                AI_PLAYER_ID.hashCode() to emptyMap()
            ),
            currentPlayerIndex = startingPlayer,
            currentRound = 1,
            maxRounds = CATEGORIES.size,
            rollsLeft = MAX_ROLLS,
            heldDice = emptySet(),
            isGameOver = false
        )
    }

    /**
     * Handles a turn in the game, processing dice results and updating game state.
     *
     * Different handling for:
     * - Player turns: Manages dice holding and roll counting
     * - AI turns: Makes strategic decisions about dice selection
     *
     * @param diceResults Results of the current dice roll
     * @param currentState Current state of the game
     * @param heldDice Set of dice indices that are currently held
     * @return Updated game state after processing the turn
     */
    fun handleTurn(
        diceResults: List<Int>, currentState: BalutScoreState, heldDice: Set<Int>
    ): BalutScoreState {
        return if (currentState.currentPlayerIndex == AI_PLAYER_ID.hashCode()) {
            handleAITurn(diceResults, currentState)
        } else {
            handlePlayerTurn(currentState, heldDice)
        }
    }

    private fun handlePlayerTurn(
        currentState: BalutScoreState, heldDice: Set<Int>
    ): BalutScoreState {
        if (currentState.rollsLeft <= 0) return currentState
        gameTracker.trackRoll()
        return currentState.copy(
            rollsLeft = currentState.rollsLeft - 1,
            heldDice = heldDice
        )
    }

    private fun handleAITurn(
        diceResults: List<Int>, currentState: BalutScoreState
    ): BalutScoreState {
        gameTracker.trackDecision()
        if (currentState.rollsLeft <= 0) {
            // AI chooses a category
            val category = chooseAICategory(diceResults, currentState)
            gameTracker.trackBanking(ScoreCalculator.calculateCategoryScore(diceResults, category))
            return scoreCategory(currentState, diceResults, category)
        }

        // AI decides which dice to hold
        gameTracker.trackRoll()
        val diceToHold = decideAIDiceHolds(diceResults)

        return currentState.copy(
            rollsLeft = currentState.rollsLeft - 1,
            heldDice = diceToHold
        )
    }

    /**
     * Scores a category for the current player.
     *
     * This method:
     * 1. Validates that the current player can score these dice
     * 2. Calculates score for the selected category
     * 3. Updates player's score map
     * 4. Advances to next player/round
     * 5. Checks for game completion
     *
     * @param currentState Current state of the game
     * @param dice Current dice results to score
     * @param category Category to score in
     * @return Updated game state after scoring
     */
    fun scoreCategory(
        currentState: BalutScoreState,
        dice: List<Int>,
        category: String
    ): BalutScoreState {
        if (currentState.currentPlayerIndex != AI_PLAYER_ID.hashCode()) {
            gameTracker.trackDecision()
            gameTracker.trackBanking(ScoreCalculator.calculateCategoryScore(dice, category))
        }
        // Validate that it's the correct player's turn and they have dice to score
        if (currentState.rollsLeft == MAX_ROLLS && currentState.currentPlayerIndex != AI_PLAYER_ID.hashCode()) {
            // If player hasn't rolled yet, they can't score
            return currentState
        }

        val currentPlayer = currentState.currentPlayerIndex
        // Calculate score for the selected category
        val score = ScoreCalculator.calculateCategoryScore(dice, category)

        // Update player scores
        val updatedScores = currentState.playerScores.toMutableMap()
        updatedScores[currentPlayer] =
            updatedScores[currentPlayer].orEmpty() + (category to score)

        // Determine next round and player
        val nextPlayer = if (currentPlayer == 0) AI_PLAYER_ID.hashCode() else 0
        val nextRound =
            if (nextPlayer == 0) currentState.currentRound + 1 else currentState.currentRound
        val isGameOver = checkIfGameOver(updatedScores)

        return currentState.copy(
            playerScores = updatedScores,
            currentPlayerIndex = nextPlayer,
            currentRound = nextRound,
            rollsLeft = MAX_ROLLS,
            heldDice = emptySet(),
            isGameOver = isGameOver
        )
    }

    private fun checkIfGameOver(updatedScores: Map<Int, Map<String, Int>>): Boolean =
        updatedScores.values.any { scores ->
            CATEGORIES.all { it in scores }
        }

    private fun decideAIDiceHolds(diceResults: List<Int>): Set<Int> {
        val playerAnalysis = statisticsManager.playerAnalysis.value
        val aiStyle = when (playerAnalysis?.playStyle) {
            PlayStyle.AGGRESSIVE -> 0.7
            PlayStyle.CAUTIOUS -> 0.5
            else -> 0.6
        }
        // Get the scoring pattern from calculateBalutScore
        val (_, scoringDice) = ScoreCalculator.calculateBalutScore(diceResults)
        // If we have a scoring pattern, hold those dice
        if (scoringDice.isNotEmpty()) {
            return scoringDice
        }
        // If no scoring pattern, use probability-based holding for number categories
        return diceResults.mapIndexedNotNull { index, value ->
            when {
                // Hold high-value numbers more often
                value == 6 || value == 5 -> if (Random.nextDouble() < aiStyle) index else null
                // Hold other numbers based on AI style and their count
                else -> {
                    val numberCount = diceResults.count { it == value }
                    val holdProbability = when {
                        numberCount >= 2 -> aiStyle  // More likely to hold pairs
                        else -> aiStyle - 0.3        // Less likely to hold singles
                    }
                    if (Random.nextDouble() < holdProbability) index else null
                }
            }
        }.toSet()
    }

    /**
     * Determines the optimal category choice for the AI player.
     *
     * Enhanced AI category selection with an adaptive difficulty and dynamic strategy.
     *
     * @param diceResults Current dice results to analyze
     * @param currentState Current game state
     * @return The chosen category name
     */
    fun chooseAICategory(
        diceResults: List<Int>, currentState: BalutScoreState, skillLevel: Double = 1.0
    ): String {
        val aiScores = currentState.playerScores[AI_PLAYER_ID.hashCode()] ?: emptyMap()
        val availableCategories = CATEGORIES.filter { it !in aiScores }
        if (availableCategories.isEmpty()) return "Choice"

        // Add randomization factor based on skill level
        val randomFactor = (1.0 - skillLevel) * Random.nextDouble(0.5, 1.5)

        // Calculate scores with situational awareness
        val categoryScores = availableCategories.associateWith { category ->
            val baseScore = ScoreCalculator.calculateCategoryScore(diceResults, category)

            // Basic category weights - prioritize high-value categories
            val baseWeight = when (category) {
                "Five of a Kind" -> if (baseScore > 0) 2.5 else 0.0
                "Large Straight" -> if (baseScore > 0) 2.2 else 0.0
                "Small Straight" -> if (baseScore > 0) 2.0 else 0.0
                "Four of a Kind" -> if (baseScore > 0) 1.8 else 0.0
                "Full House" -> if (baseScore > 0) 1.6 else 0.0
                "Choice" -> 0.5  // Reduce priority of Choice
                else -> 1.0
            }

            // Enhanced number category scoring
            val weight = when (category) {
                in listOf("Ones", "Twos", "Threes", "Fours", "Fives", "Sixes") -> {
                    val targetNumber = when (category) {
                        "Ones" -> 1
                        "Twos" -> 2
                        "Threes" -> 3
                        "Fours" -> 4
                        "Fives" -> 5
                        else -> 6
                    }
                    val count = diceResults.count { it == targetNumber }
                    weightNumberCategory(baseScore, count)
                }

                "Choice" -> {
                    val total = diceResults.sum()
                    when {
                        total >= 24 -> baseScore * 1.2
                        total >= 20 -> baseScore * 1.0
                        total >= 15 -> baseScore * 0.8
                        else -> baseScore * 0.6
                    }
                }

                else -> baseScore.toDouble() * baseWeight
            }

            // Apply skill-based randomization
            weight * (1.0 + (randomFactor - 1.0) * (1.0 - skillLevel))
        }

        // Strategic considerations
        val roundsLeft = CATEGORIES.size - currentState.currentRound
        val playerScores = currentState.playerScores
        val isWinning = isAIWinning(playerScores)

        // Adjust strategy based on game state
        return when {
            // Only use Choice in late game if we have a really good roll
            roundsLeft <= 2 && "Choice" in availableCategories && diceResults.sum() >= 24 -> "Choice"
            // Otherwise prioritize highest scoring categories
            isWinning && skillLevel > 0.7 -> categoryScores.maxByOrNull { it.value }?.key
                ?: availableCategories.first()

            !isWinning && skillLevel > 0.7 -> findRiskierOption(categoryScores)
            else -> categoryScores.entries
                .sortedByDescending { it.value }
                .take(2)  // Only consider top 2 options
                .maxByOrNull { it.value }?.key ?: availableCategories.first()
        }
    }

    private fun weightNumberCategory(baseScore: Int, count: Int): Double = when (count) {
        5 -> baseScore * 1.8
        4 -> baseScore * 1.5
        3 -> baseScore * 1.2
        2 -> baseScore * 0.9
        else -> baseScore * 0.7
    }

    /**
     * Finds the most risky option based on scores.
     *
     * @param scores A map of category names to their scores
     * @return The name of the most risky option
     */
    private fun findRiskierOption(scores: Map<String, Double>): String = scores.entries
        .sortedByDescending { it.value }
        .take(3)
        .maxByOrNull { entry ->
            when (entry.key) {
                "Five of a Kind", "Large Straight" -> entry.value * 1.5
                "Four of a Kind", "Full House" -> entry.value * 1.2
                else -> entry.value
            }
        }?.key ?: scores.keys.first()

    /**
     * Checks if the AI has won the game.
     *
     * @param playerScores A map of player IDs to their scores
     * @return Boolean indicating if the AI has won
     */
    private fun isAIWinning(playerScores: Map<Int, Map<String, Int>>): Boolean {
        val aiTotal = playerScores[AI_PLAYER_ID.hashCode()]?.values?.sum() ?: 0
        val humanTotal = playerScores.entries
            .firstOrNull { it.key != AI_PLAYER_ID.hashCode() }
            ?.value?.values?.sum() ?: 0
        return aiTotal > humanTotal
    }
}