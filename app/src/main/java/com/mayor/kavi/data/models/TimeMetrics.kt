package com.mayor.kavi.data.models

import kotlinx.serialization.Serializable

/**
 * Represents the time-related metrics of a player's gameplay.
 *
 * @param averageGameDuration The average duration of a game played by the player.
 * @param averageTurnDuration The average duration of a turn in the game.
 * @param fastestGame The fastest game completed by the player.
 * @param totalPlayTime The total amount of time the player has spent playing games.
 */
@Serializable
data class TimeMetrics(
    val averageGameDuration: Long = 0L,
    val averageTurnDuration: Long = 0L,
    val fastestGame: Long = 0L,
    val totalPlayTime: Long = 0L
)