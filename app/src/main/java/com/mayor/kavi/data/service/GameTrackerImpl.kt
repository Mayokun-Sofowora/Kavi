package com.mayor.kavi.data.service

import com.mayor.kavi.data.manager.StatisticsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameTrackerImpl @Inject constructor(
    private val statisticsManager: StatisticsManager
) : GameTracker {
    private var lastDecisionTime: Long = System.currentTimeMillis()

    override fun trackDecision() {
        val currentTime = System.currentTimeMillis()
        val decisionTime = currentTime - lastDecisionTime
        statisticsManager.addDecisionTime(decisionTime)
        lastDecisionTime = currentTime
    }

    override fun trackBanking(score: Int) {
        statisticsManager.addBankingScore(score)
    }

    override fun trackRoll() {
        statisticsManager.incrementRollCount()
    }
} 