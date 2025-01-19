package com.mayor.kavi.data.models

import kotlinx.serialization.Serializable

/**
 * Represents the overall statistics of a player's game performance.
 *
 * @property gamesPlayed The total number of games played by the player.
 * @property highScores A map of game names to the player's highest score in each game.
 * @property winRates A map of game names to the player's win rates in each game.
 * @property lastSeen The timestamp (in milliseconds since the epoch) when the player was last active.
 * @property playerAnalysis Additional analysis or insights about the player's performance.
 * @property isOnline Whether the player is currently online
 * @property analysis Additional analysis data with string values
 */
@Serializable
data class GameStatistics(
    val gamesPlayed: Int = 0,
    val highScores: Map<String, Int> = emptyMap(),
    val winRates: Map<String, WinRate> = emptyMap(),
    val lastSeen: Long? = System.currentTimeMillis(),
    val playerAnalysis: PlayerAnalysis? = null,
    val isOnline: Boolean = false,
    val analysis: Map<String, String>? = null
)
