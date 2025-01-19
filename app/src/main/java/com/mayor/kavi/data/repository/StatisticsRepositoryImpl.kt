package com.mayor.kavi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mayor.kavi.data.models.*
import com.mayor.kavi.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : StatisticsRepository {

    private fun getUserDocumentRef(userId: String) =
        firestore.collection("users").document(userId)

    override suspend fun getGameStatistics(): Result<GameStatistics> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId()
                ?: return@withContext Result.Error("User not authenticated")

            val document = getUserDocumentRef(currentUserId).get().await()

            if (document.exists()) {
                val stats = document.toObject(GameStatistics::class.java)
                    ?: GameStatistics()
                Result.Success(stats)
            } else {
                val defaultStats = GameStatistics()
                getUserDocumentRef(currentUserId).set(defaultStats).await()
                Result.Success(defaultStats)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get game statistics", e)
        }
    }

    override suspend fun updateGameStatistics(stats: GameStatistics): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = getCurrentUserId()
                    ?: return@withContext Result.Error("User not authenticated")

                getUserDocumentRef(currentUserId).set(stats).await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error("Error updating game statistics", e)
            }
        }

    override suspend fun updatePlayerAnalysis(
        userId: String,
        analysis: PlayerAnalysis
    ): Result<Unit> = try {
        getUserDocumentRef(userId).update("analysis", analysis).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating player analysis")
        Result.Error("Error updating player analysis", e)
    }

    override suspend fun getAllUsersGameStatistics(): Result<List<GameStatistics>> = try {
        val snapshot = firestore.collection("statistics")
            .get()
            .await()
        val statistics = snapshot.documents.mapNotNull { doc ->
            doc.toObject(GameStatistics::class.java)
        }
        Result.Success(statistics)
    } catch (e: Exception) {
        Result.Error("Error fetching game statistics", e)
    }

    override suspend fun clearUserStatistics(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId()
                ?: return@withContext Result.Error("User not authenticated")

            getUserDocumentRef(currentUserId).set(GameStatistics()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing user statistics")
            Result.Error("Error clearing user statistics", e)
        }
    }

    private fun getCurrentUserId(): String? = auth.currentUser?.uid
}