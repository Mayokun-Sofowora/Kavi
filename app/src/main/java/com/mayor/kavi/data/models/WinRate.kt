package com.mayor.kavi.data.models

import kotlinx.serialization.Serializable

/**
 * Represents the win rate for a specific game.
 *
 * @property wins The number of games won by the player.
 * @property total The total number of games played by the player.
 */
@Serializable
data class WinRate(
    var wins: Int = 0,
    var total: Int = 0
)
