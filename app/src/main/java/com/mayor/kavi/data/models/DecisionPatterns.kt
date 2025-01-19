package com.mayor.kavi.data.models

import kotlinx.serialization.Serializable

/**
 * Represents the patterns in a player's decisions during gameplay.
 *
 * @param averageRollsPerTurn The average number of rolls a player makes per turn.
 * @param bankingThreshold The threshold at which a player decides to bank their dice.
 * @param riskTaking The level of risk the player is willing to take.
 * @param decisionSpeed The speed at which the player makes decisions.
 */
@Serializable
data class DecisionPatterns(
    val averageRollsPerTurn: Float = 0f,
    val bankingThreshold: Float = 0f,
    val riskTaking: Float = 0f,
    val decisionSpeed: Float = 0f
)