package com.mayor.kavi.data.manager

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mayor.kavi.data.models.*
import com.mayor.kavi.data.models.enums.Achievement
import com.mayor.kavi.data.models.enums.PlayStyle
import com.mayor.kavi.di.AppModule.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import javax.inject.Inject
import timber.log.Timber
import javax.inject.Singleton
import kotlin.math.*
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class StatisticsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val applicationScope = CoroutineScope(dispatcher + SupervisorJob())

    companion object {
        private val STATS_KEY = stringPreferencesKey("GAME_STATS")

        val LocalStatisticsManager = staticCompositionLocalOf<StatisticsManager> {
            error("No StatisticsManager provided")
        }
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "statistics")
    private val dataStore = context.dataStore
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
    }

    private val _playerAnalysis = MutableStateFlow<PlayerAnalysis?>(null)
    val playerAnalysis: StateFlow<PlayerAnalysis?> = _playerAnalysis.asStateFlow()

    private val _modelTrainingStatus = MutableStateFlow<String?>(null)
    val modelTrainingStatus: StateFlow<String?> = _modelTrainingStatus.asStateFlow()

    private val _gameStatistics = MutableStateFlow<GameStatistics?>(null)
    val gameStatistics: StateFlow<GameStatistics?> = _gameStatistics.asStateFlow()

    private val _gameStartTime = MutableStateFlow<Long>(0)
    private val _turnStartTime = MutableStateFlow<Long>(0)
    private val _decisionTimes = MutableStateFlow<List<Long>>(emptyList())
    private val _bankingScores = MutableStateFlow<List<Int>>(emptyList())
    private val _rollsThisTurn = MutableStateFlow(0)

    private val statisticsMutex = Mutex()

    init {
        applicationScope.launch(dispatcher) {
            loadGameStatistics()
            updatePlayerAnalysis()
        }
    }

    private suspend fun loadGameStatistics() {
        try {
            loadGameStatisticsFromDataStore()
            if (_gameStatistics.value == null) {
                _gameStatistics.value = GameStatistics()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in loadGameStatistics")
            _gameStatistics.value = GameStatistics()
        }
    }

    private suspend fun loadGameStatisticsFromDataStore() {
        try {
            dataStore.data.firstOrNull()?.let { prefs ->
                val jsonString = prefs[STATS_KEY]
                if (jsonString != null) {
                    val stats = json.decodeFromString<GameStatistics>(jsonString)
                    _gameStatistics.value = stats
                    Timber.d("Successfully loaded game statistics from DataStore")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading game statistics from DataStore")
            _gameStatistics.value = GameStatistics()
        }
    }

    private suspend fun saveGameStatisticsToDataStore(stats: GameStatistics) {
        try {
            val jsonString = json.encodeToString(stats)
            dataStore.edit { prefs ->
                prefs[STATS_KEY] = jsonString
            }
            Timber.d("Successfully saved game statistics to DataStore")
        } catch (e: Exception) {
            Timber.e(e, "Error saving game statistics to DataStore")
        }
    }

    fun updateGameStatistics(
        gameMode: String,
        score: Int,
        isWin: Boolean,
        wasComeback: Boolean = false,
        wasCloseGame: Boolean = false
    ) {
        applicationScope.launch(dispatcher) {
            statisticsMutex.withLock {
                val currentStats = _gameStatistics.value ?: GameStatistics()
                val timeMetrics = updateTimeMetrics(currentStats)
                val decisionPatterns = updateDecisionPatterns()
                val performanceMetrics = updatePerformanceMetrics(
                    currentStats, isWin, gameMode, score, wasComeback, wasCloseGame
                )
                val achievements =
                    calculateAchievements(timeMetrics, performanceMetrics, decisionPatterns)

                val updatedStats = currentStats.copy(
                    gamesPlayed = currentStats.gamesPlayed + 1,
                    highScores = currentStats.highScores.toMutableMap().apply {
                        val currentHigh = this[gameMode] ?: 0
                        if (score > currentHigh) {
                            this[gameMode] = score
                        }
                    },
                    winRates = currentStats.winRates.toMutableMap().apply {
                        val currentWinRate = this[gameMode] ?: WinRate()
                        this[gameMode] = WinRate(
                            wins = currentWinRate.wins + if (isWin) 1 else 0,
                            total = currentWinRate.total + 1
                        )
                    },
                    playerAnalysis = currentStats.playerAnalysis?.copy(
                        performanceMetrics = performanceMetrics,
                        timeMetrics = timeMetrics,
                        decisionPatterns = decisionPatterns,
                        achievementProgress = achievements
                    ) ?: PlayerAnalysis(),
                    lastSeen = System.currentTimeMillis()
                )

                _gameStatistics.value = updatedStats
                saveGameStatisticsToDataStore(updatedStats)
                        updatePlayerAnalysis()
            }
        }
    }

    private fun updatePlayerAnalysis() {
        val stats = _gameStatistics.value ?: return

        val analysis = PlayerAnalysis(
            predictedWinRate = calculateWinRate(stats),
            consistency = calculateConsistency(stats),
            playStyle = determinePlayStyle(stats),
            improvement = calculateImprovement(stats),
            decisionPatterns = stats.playerAnalysis?.decisionPatterns ?: DecisionPatterns(),
            timeMetrics = stats.playerAnalysis?.timeMetrics ?: TimeMetrics(),
            performanceMetrics = stats.playerAnalysis?.performanceMetrics ?: PerformanceMetrics(),
            achievementProgress = stats.playerAnalysis?.achievementProgress ?: emptyMap()
        )

        _playerAnalysis.value = analysis
    }

    private fun calculateWinRate(stats: GameStatistics): Float {
        val totalWins = stats.winRates.values.sumOf { it.wins }
        val totalGames = stats.winRates.values.sumOf { it.total }
        return if (totalGames > 0) totalWins.toFloat() / totalGames else 0f
    }

    private fun calculateConsistency(stats: GameStatistics): Float {
        if (stats.gamesPlayed < 3) return 0.5f // Not enough games for meaningful consistency

        val scores =
            stats.playerAnalysis?.performanceMetrics?.averageScoreByMode?.values ?: return 0.5f
        if (scores.isEmpty()) return 0.5f

        val mean = scores.average().toFloat()
        val variance = scores.map { (it - mean).pow(2) }.average().toFloat()
        // Convert variance to a 0-1 scale where 1 is most consistent
        return (1f - sqrt(variance) / mean).coerceIn(0f, 1f)
    }

    private fun determinePlayStyle(stats: GameStatistics): PlayStyle {
        if (stats.gamesPlayed < 3) return PlayStyle.BALANCED

        val avgRollsPerTurn = stats.playerAnalysis?.decisionPatterns?.averageRollsPerTurn ?: 0f
        val riskTaking = stats.playerAnalysis?.decisionPatterns?.riskTaking ?: 0.5f

        return when {
            avgRollsPerTurn > 4 || riskTaking > 0.7f -> PlayStyle.AGGRESSIVE
            avgRollsPerTurn < 2 || riskTaking < 0.3f -> PlayStyle.CAUTIOUS
            else -> PlayStyle.BALANCED
        }
    }

    fun addDecisionTime(time: Long) {
        _decisionTimes.value = (_decisionTimes.value + time)
    }

    fun addBankingScore(score: Int) {
        _bankingScores.value = (_bankingScores.value + score)
    }

    fun incrementRollCount() {
        _rollsThisTurn.value++
    }

    fun resetTurnStats() {
        _rollsThisTurn.value = 0
        _decisionTimes.value = emptyList()
        _bankingScores.value = emptyList()
    }

    private fun calculateImprovement(stats: GameStatistics): Float {
        if (stats.gamesPlayed < 5) return 0f

        // Calculate score improvement
        val recentScores =
            stats.playerAnalysis?.performanceMetrics?.averageScoreByMode?.values ?: return 0f
        if (recentScores.isEmpty()) return 0f

        val avgScore = recentScores.average().toFloat()
        val maxScore = (recentScores.maxOrNull() ?: 0.0).toFloat()

        // Calculate win rate improvement
        val winRateImprovement = stats.playerAnalysis.performanceMetrics.let { metrics ->
            metrics.currentStreak.toFloat() / maxOf(metrics.longestStreak, 1)
        }

        // Combine score and win rate improvements
        return ((maxScore / avgScore - 1f) * 0.5f + winRateImprovement * 0.5f).coerceIn(0f, 1f)
    }

    private fun updateTimeMetrics(currentStats: GameStatistics): TimeMetrics {
        val currentTime = System.currentTimeMillis()
        val gameDuration = if (_gameStartTime.value > 0) {
            (currentTime - _gameStartTime.value).coerceIn(0L, 3600000L) // Cap at 1 hour
        } else 0L

        val currentMetrics = currentStats.playerAnalysis?.timeMetrics ?: TimeMetrics()

        return if (gameDuration > 0) {
            TimeMetrics(
                averageGameDuration = calculateNewAverage(
                    currentMetrics.averageGameDuration,
                    gameDuration,
                    currentStats.gamesPlayed
                ),
                averageTurnDuration = if (_decisionTimes.value.isEmpty())
                    currentMetrics.averageTurnDuration
                else _decisionTimes.value.average().toLong(),
                fastestGame = minOf(
                    currentMetrics.fastestGame.takeIf { it > 0 } ?: Long.MAX_VALUE,
            gameDuration
                ),
                totalPlayTime = (currentMetrics.totalPlayTime + gameDuration)
                    .coerceIn(0L, 360000000L) // Cap at 100 hours
            )
        } else {
            currentMetrics
        }
    }

    /* Helper function to calculate the new average with a new value */
    private fun calculateNewAverage(oldAverage: Long, newValue: Long, oldCount: Int): Long {
        return if (oldCount == 0) newValue
        else ((oldAverage * oldCount + newValue) / (oldCount + 1))
    }

    private fun updateDecisionPatterns(): DecisionPatterns {
        val avgRollsPerTurn = _rollsThisTurn.value.toFloat()
        val avgBankingScore = if (_bankingScores.value.isEmpty()) 0f
        else _bankingScores.value.average().toFloat()
        val avgDecisionTime = if (_decisionTimes.value.isEmpty()) 0f
        else _decisionTimes.value.average().toFloat()

        // Normalize banking score to calculate risk taking
        // Using 1000 as a reasonable maximum banking score
        // This will give us a value between 0 and 1
        val normalizedRiskTaking = (avgBankingScore / 1000f).coerceIn(0f, 1f)

        return DecisionPatterns(
            averageRollsPerTurn = avgRollsPerTurn,
            bankingThreshold = avgBankingScore,
            riskTaking = normalizedRiskTaking,
            decisionSpeed = avgDecisionTime
        )
    }

    private fun updatePerformanceMetrics(
        currentStats: GameStatistics,
        isWin: Boolean,
        gameMode: String,
        finalScore: Int,
        wasComeback: Boolean,
        wasCloseGame: Boolean
    ): PerformanceMetrics {
        val currentMetrics = currentStats.playerAnalysis?.performanceMetrics ?: PerformanceMetrics()

        return PerformanceMetrics(
            currentStreak = if (isWin) currentMetrics.currentStreak + 1 else 0,
            longestStreak = maxOf(
                if (isWin) currentMetrics.currentStreak + 1 else 0,
                currentMetrics.longestStreak
            ),
            comebacks = currentMetrics.comebacks + if (wasComeback) 1 else 0,
            closeGames = currentMetrics.closeGames + if (wasCloseGame) 1 else 0,
            personalBests = currentMetrics.personalBests.toMutableMap().apply {
                if (finalScore > (this[gameMode] ?: 0)) {
                    this[gameMode] = finalScore
                }
            },
            averageScoreByMode = currentMetrics.averageScoreByMode.toMutableMap().apply {
                val currentAvg = this[gameMode] ?: 0f
                this[gameMode] = if (currentAvg == 0f) finalScore.toFloat()
                else (currentAvg + finalScore) / 2f
            }
        )
    }

    /**
     * Calculates achievement progress for various player accomplishments.
     *
     * This function processes different categories of achievements:
     * - Winning Streaks (consecutive wins)
     * - Comeback Achievements (wins from behind)
     * - Consistency Achievements (maintaining high scores)
     * - Risk Taking (banking decisions and high scores)
     * - Speed Achievements (quick decisions and fast games)
     * - Experience Achievements (playtime and total wins)
     * - Game-Specific Achievements (accomplishments in specific game modes)
     *
     * All achievement progress is normalized to a 0-1 scale, where:
     * - 0.0 represents no progress
     * - 1.0 represents achievement completion
     *
     * @param timeMetrics Player's time-related statistics (playtime, fastest games)
     * @param performanceMetrics Player's performance statistics (streaks, scores, wins)
     * @param decisionPatterns Player's decision-making patterns (risk taking, speed)
     *
     * @return Map of achievement names to their progress (0.0 to 1.0)
     *
     * Achievement Calculations:
     * - STREAK_MASTER: Progress = current streak / 15 (max 1.0)
     * - WINNING_SPREE: Progress = current streak / 25 (max 1.0)
     * - COMEBACK_KING: Progress = comebacks / 10 (max 1.0)
     * - CLUTCH_MASTER: Progress = close games / 5 (max 1.0)
     * - CONSISTENT_PLAYER: Average score relative to max possible (requires 20+ games)
     * - PERFECTIONIST: Number of near-perfect scores / 10 (95% of max score)
     * - RISK_TAKER: Based on banking decisions and risk patterns
     * - HIGH_ROLLER: Highest Greed score / 2000
     * - SPEED_STAR: Inverse formula based on decision speed
     * - LIGHTNING_FAST: Complete game under 2 minutes
     * - VETERAN_PLAYER: Hours played / 20 (capped at 100 hours)
     * - DICE_MASTER: Total wins / 100
     * - BALUT_EXPERT: Combination of high score (300+) and proper reroll usage
     * - GREED_GURU: Highest Greed score / 3000
     * - PIG_PRODIGY: Win with perfect score (opponent 0)
     */
    private fun calculateAchievements(
        timeMetrics: TimeMetrics,
        performanceMetrics: PerformanceMetrics,
        decisionPatterns: DecisionPatterns
    ): Map<String, Float> {
        val achievements = mutableMapOf<String, Float>()

        // Winning Streaks
        achievements[Achievement.STREAK_MASTER.name] =
            (performanceMetrics.longestStreak.toFloat() / 15f).coerceAtMost(1f)
        achievements[Achievement.WINNING_SPREE.name] =
            (performanceMetrics.longestStreak.toFloat() / 25f).coerceAtMost(1f)

        // Comeback Achievements
        achievements[Achievement.COMEBACK_KING.name] =
            (performanceMetrics.comebacks.toFloat() / 10f).coerceAtMost(1f)
        achievements[Achievement.CLUTCH_MASTER.name] =
            (performanceMetrics.closeGames.toFloat() / 5f).coerceAtMost(1f)

        // Consistency Achievements
        val avgScores = performanceMetrics.averageScoreByMode.values
        val highConsistency = if (avgScores.size >= 20) {
            avgScores.average() / getMaxPossibleScore(performanceMetrics.averageScoreByMode.keys)
        } else {
            0.0
        }
        achievements[Achievement.CONSISTENT_PLAYER.name] =
            highConsistency.toFloat().coerceAtMost(1f)

        // Calculate perfectionist progress based on high scores
        val maxScoreCount = performanceMetrics.personalBests.count { (mode, score) ->
            score >= getMaxPossibleScore(listOf(mode)) * 0.95 // 95% of max possible score
        }
        achievements[Achievement.PERFECTIONIST.name] =
            (maxScoreCount.toFloat() / 10f).coerceAtMost(1f)

        // Risk Taking
        achievements[Achievement.RISK_TAKER.name] = decisionPatterns.riskTaking.coerceAtMost(1f)
        val highestScore = performanceMetrics.personalBests["Greed"] ?: 0
        achievements[Achievement.HIGH_ROLLER.name] =
            (highestScore.toFloat() / 2000f).coerceAtMost(1f)

        /*Speed Achievement uses an inverse formula to calculate speed rating,
        based on how fast you make decision*/
        achievements[Achievement.SPEED_STAR.name] =
            (1000f / (decisionPatterns.decisionSpeed + 500f)).coerceAtMost(1f)
        val fastestGameMinutes = timeMetrics.fastestGame / 60000f // Convert to minutes
        achievements[Achievement.LIGHTNING_FAST.name] = when {
            fastestGameMinutes <= 2f -> 1f
            fastestGameMinutes >= 4f -> 0f
            else -> ((4f - fastestGameMinutes) / 2f).coerceIn(0f, 1f)
        }

        // Experience Achievements
        val hoursPlayed =
            (timeMetrics.totalPlayTime / 3600000f).coerceIn(0f, 100f) // Cap at 100 hours
        achievements[Achievement.VETERAN_PLAYER.name] = (hoursPlayed / 20f).coerceAtMost(1f)
        val totalWins = performanceMetrics.averageScoreByMode.keys.sumOf { mode ->
            _gameStatistics.value?.winRates?.get(mode)?.wins ?: 0
        }
        achievements[Achievement.DICE_MASTER.name] = (totalWins.toFloat() / 100f).coerceAtMost(1f)

        // Game-Specific Achievements
        val balutHighScore = performanceMetrics.personalBests["Balut"] ?: 0
        val balutWins = _gameStatistics.value?.winRates?.get("Balut")?.wins ?: 0
        val avgRollsPerTurn = decisionPatterns.averageRollsPerTurn
        val balutProgress = if (balutWins > 0) {
            // Calculate score progress
            val scoreProgress = (balutHighScore.toFloat() / 300f).coerceAtMost(1f)
            // Calculate reroll factor - penalize low reroll counts
            // avgRollsPerTurn should be close to 2 for legitimate play (initial roll + ~1-2 rerolls)
            val rerollFactor = (avgRollsPerTurn / 2f).coerceIn(0f, 1f)
            // Combine score and reroll requirements
            // Player needs both high score and reasonable number of rerolls
            (scoreProgress * 0.6f + rerollFactor * 0.4f).coerceAtMost(1f)
        } else 0f
        achievements[Achievement.BALUT_EXPERT.name] = balutProgress

        val greedHighScore = performanceMetrics.personalBests["Greed"] ?: 0
        achievements[Achievement.GREED_GURU.name] =
            (greedHighScore.toFloat() / 3000f).coerceAtMost(1f)

        // PIG_PRODIGY - Check if player has won a game with opponent score of 0
        val pigHighScore = performanceMetrics.personalBests["Pig"] ?: 0
        val pigWins = _gameStatistics.value?.winRates?.get("Pig")?.wins ?: 0
        achievements[Achievement.PIG_PRODIGY.name] =
            if (pigHighScore == 100 && pigWins > 0) 1f else 0f

        return achievements
    }

    private fun getMaxPossibleScore(modes: Collection<String>): Double {
        return modes.maxOfOrNull { mode ->
            when (mode) {
                "Balut" -> 365.0  // Maximum theoretical score in Balut
                "Greed" -> 3000.0 // High but achievable score in Greed
                "Pig" -> 100.0    // Maximum score in Pig
                else -> 100.0     // Default for unknown modes
            }
        } ?: 100.0
    }

    suspend fun clearAllData() {
        statisticsMutex.withLock {
        // Reset all local state
        _gameStatistics.value = GameStatistics()
        _playerAnalysis.value = null
        _gameStartTime.value = 0
        _turnStartTime.value = 0
            resetTurnStats()

        // Clear local storage
        try {
                dataStore.edit { prefs ->
                    prefs.remove(STATS_KEY)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error clearing local statistics data")
        }
        }
    }

    fun startGameTiming() {
        _gameStartTime.value = System.currentTimeMillis()
        _turnStartTime.value = System.currentTimeMillis()
        resetTurnStats()
    }
}
