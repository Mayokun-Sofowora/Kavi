package com.mayor.kavi.data.models

import com.mayor.kavi.data.models.enums.PlayStyle
import kotlinx.serialization.Serializable

/**
 * Represents an analysis of a player's performance and behavior.
 *
 * @param predictedWinRate The predicted win rate of the player.
 * @param consistency The consistency of the player's performance.
 * @param playStyle The player's play style (e.g., aggressive, balanced, cautious).
 * @param improvement The player's potential for improvement.
 * @param decisionPatterns The patterns observed in the player's decisions during the game.
 * @param timeMetrics The metrics related to the time the player spends playing.
 * @param performanceMetrics The overall performance metrics of the player.
 * @param achievementProgress A map of achievements and their progress (range from 0 to 1).
 */
@Serializable
data class PlayerAnalysis(
    val predictedWinRate: Float = 0f,
    val consistency: Float = 0f,
    val playStyle: PlayStyle = PlayStyle.BALANCED,
    val improvement: Float = 0f,
    val decisionPatterns: DecisionPatterns = DecisionPatterns(),
    val timeMetrics: TimeMetrics = TimeMetrics(),
    val performanceMetrics: PerformanceMetrics = PerformanceMetrics(),
    val achievementProgress: Map<String, Float> = emptyMap()
)

