package com.mayor.kavi.data.service

/**
 * Interface for tracking game state and user interactions.
 */
interface GameTracker {
    fun trackDecision()
    fun trackBanking(score: Int)
    fun trackRoll()
}
