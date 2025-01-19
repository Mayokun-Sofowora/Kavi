package com.mayor.kavi.util

import com.mayor.kavi.data.models.GameScoreState

/**
 * Utility object for calculating scores in various dice games.
 *
 * This calculator handles score computation for different dice game variants:
 * - Greed: Complex scoring with multiple dice combinations
 * - Balut: Category-based scoring similar to Yahtzee
 * - Pig: Simple single-die scoring
 */
object ScoreCalculator {

    /**
     * Calculates the score for a Greed game roll.
     *
     * Scoring rules:
     * - Straight (1-2-3-4-5-6): 1500 points
     * - Six of a kind: 3000 points
     * - Five of a kind: 2000 points
     * - Three pairs: 1500 points
     * - Three of a kind: Number × 100 (1000 for three 1s)
     * - Single 1s: 100 points each
     * - Single 5s: 50 points each
     *
     * @param dice List of die values to score
     * @return Pair of (total score, indices of scoring dice)
     */
    fun calculateGreedScore(dice: List<Int>): Pair<Int, Set<Int>> {
        var score = 0
        val scoringDice = mutableSetOf<Int>()
        val diceFrequency = dice.groupBy { it }
        // Check for special combinations (straight, 6 of a kind, 5 of a kind)
        when {
            dice.sorted() == listOf(1, 2, 3, 4, 5, 6) -> {
                score = 1500
                scoringDice.addAll(dice.indices)
                return Pair(score, scoringDice)
            }

            diceFrequency.any { it.value.size == 6 } -> {
                score = 3000
                scoringDice.addAll(dice.indices)
                return Pair(score, scoringDice)
            }

            diceFrequency.any { it.value.size == 5 } -> {
                score = 2000
                scoringDice.addAll(dice.indices)
                return Pair(score, scoringDice)
            }

            diceFrequency.count { it.value.size == 2 } == 3 -> {
                score = 1500
                scoringDice.addAll(dice.indices)
                return Pair(score, scoringDice)
            }
        }
        // Process remaining combinations (3 pairs, 1s, 5s)
        for ((number, occurrences) in diceFrequency) {
            when {
                occurrences.size >= 3 -> {
                    score += if (number == 1) 1000 else number * 100
                    scoringDice.addAll(dice.mapIndexedNotNull { index, it1 -> if (it1 == number) index
                    else null }
                        .take(3))
                    // Score remaining 1s and 5s
                    val remaining = occurrences.size - 3
                    if (number == 1 || number == 5) {
                        score += remaining * (GameScoreState.GreedScoreState.SCORING_VALUES[number]
                            ?: 0)
                        scoringDice.addAll(dice.mapIndexedNotNull { index, it1 -> if (it1 == number) index
                        else null }
                            .takeLast(remaining))
                    }
                }

                number == 1 || number == 5 -> {
                    score += occurrences.size * (GameScoreState.GreedScoreState.SCORING_VALUES[number]
                        ?: 0)
                    scoringDice.addAll(dice.mapIndexedNotNull { index, it1 -> if (it1 == number) index
                    else null })
                }
            }
        }

        return Pair(score, scoringDice)
    }

    /**
     * Calculates the score for a Balut category.
     *
     * Scoring rules vary by category:
     * - Number categories (Ones through Sixes): Sum of matching numbers
     * - Full House: Sum of all dice
     * - Four of a Kind: Sum of all dice
     * - Straight: 30 points
     * - Five of a Kind: 50 points
     * - Choice: Sum of all dice
     *
     * @param dice List of die values to score
     * @return Pair of (score for the category, indices of scoring dice)
     */
    fun calculateBalutScore(dice: List<Int>): Pair<Int, Set<Int>> {
        val grouped = dice.groupBy { it }

        // Check for Five of a Kind (all dice showing same number)
        if (grouped.values.any { it.size == 5 }) {
            grouped.entries.first { it.value.size == 5 }.key
            return Pair(50, dice.mapIndexed { index, d -> index }.toSet())
        }

        // Check for Four of a Kind
        grouped.entries.find { it.value.size >= 4 }?.let { entry ->
            val scoringDice = dice.mapIndexedNotNull { index, d ->
                if (d == entry.key) index else null
            }.take(4).toSet()
            return Pair(40, scoringDice)
        }

        // Check for Full House
        if (grouped.size == 2 && grouped.values.any { it.size == 3 }) {
            return Pair(35, dice.mapIndexed { index, _ -> index }.toSet())
        }

        // Check for Large Straight
        if (hasLargeStraight(dice)) {
            return Pair(40, dice.mapIndexed { index, _ -> index }.toSet())
        }

        // Check for Small Straight
        if (hasSmallStraight(dice)) {
            return Pair(30, dice.mapIndexed { index, _ -> index }.toSet())
        }

        // No scoring combination
        return Pair(0, emptySet())
    }

    fun calculateCategoryScore(dice: List<Int>, category: String): Int = when (category) {
        // Number categories (Ones through Sixes)
        "Ones" -> dice.count { it == 1 } * 1
        "Twos" -> dice.count { it == 2 } * 2
        "Threes" -> dice.count { it == 3 } * 3
        "Fours" -> dice.count { it == 4 } * 4
        "Fives" -> dice.count { it == 5 } * 5
        "Sixes" -> dice.count { it == 6 } * 6

        // Special combinations
        "Five of a Kind" -> {
            if (dice.groupBy { it }.any { it.value.size == 5 }) 50 else 0
        }

        "Four of a Kind" -> {
            if (dice.groupBy { it }.any { it.value.size >= 4 }) 40 else 0
        }

        "Full House" -> {
            val groups = dice.groupBy { it }
            if (groups.size == 2 && groups.values.any { it.size == 3 }) 35 else 0
        }

        "Small Straight" -> if (hasSmallStraight(dice)) 30 else 0
        "Large Straight" -> if (hasLargeStraight(dice)) 40 else 0
        "Choice" -> dice.sum()
        else -> 0
    }

    private fun hasSmallStraight(dice: List<Int>): Boolean {
        val sortedDice = dice.distinct().sorted()
        if (sortedDice.size < 4) return false

        return when {
            sortedDice.take(4) == listOf(1, 2, 3, 4) -> true
            sortedDice.size >= 5 && sortedDice.subList(1, 5) == listOf(2, 3, 4, 5) -> true
            sortedDice.take(4) == listOf(2, 3, 4, 5) -> true
            sortedDice.size >= 5 && sortedDice.subList(1, 5) == listOf(3, 4, 5, 6) -> true
            sortedDice.take(4) == listOf(3, 4, 5, 6) -> true
            else -> false
        }
    }

    private fun hasLargeStraight(dice: List<Int>): Boolean {
        val sortedDice = dice.distinct().sorted()
        return sortedDice == listOf(1, 2, 3, 4, 5) ||
                sortedDice == listOf(2, 3, 4, 5, 6)
    }

}

