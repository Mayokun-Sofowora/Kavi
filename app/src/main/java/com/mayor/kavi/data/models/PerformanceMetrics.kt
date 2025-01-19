package com.mayor.kavi.data.models

import kotlinx.serialization.Serializable

/**
 * Represents various performance metrics for a player.
 *
 * @param currentStreak The player's current winning streak.
 * @param longestStreak The longest winning streak of the player.
 * @param comebacks The number of times the player has made a comeback in a game.
 * @param closeGames The number of close games played by the player (games decided by a small margin).
 * @param personalBests A map of the player's personal bests in various game categories.
 * @param averageScoreByMode A map of the player's average score by different game modes.
 */
@Serializable
data class PerformanceMetrics(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val comebacks: Int = 0,
    val closeGames: Int = 0,
    val personalBests: Map<String, Int> = emptyMap(),
    val averageScoreByMode: Map<String, Float> = emptyMap()
)