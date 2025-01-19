package com.mayor.kavi.data.repository

import com.mayor.kavi.data.models.GameStatistics
import com.mayor.kavi.data.models.PlayerAnalysis
import com.mayor.kavi.util.Result

/**
 * This repository handles the interaction with the backend for user statistics related operations.
 */
interface StatisticsRepository {
    suspend fun getGameStatistics(): Result<GameStatistics>
    suspend fun updateGameStatistics(stats: GameStatistics): Result<Unit>
    suspend fun updatePlayerAnalysis(userId: String, analysis: PlayerAnalysis): Result<Unit>
    suspend fun clearUserStatistics(): Result<Unit>
    suspend fun getAllUsersGameStatistics(): Result<List<GameStatistics>> // New method
}
