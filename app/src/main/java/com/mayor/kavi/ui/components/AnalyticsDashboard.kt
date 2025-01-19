package com.mayor.kavi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mayor.kavi.data.manager.StatisticsManager
import com.mayor.kavi.data.models.*
import com.mayor.kavi.data.models.enums.Achievement

@Composable
fun AnalyticsDashboard(
    modifier: Modifier = Modifier
) {
    val statisticsManager = StatisticsManager.LocalStatisticsManager.current
    val gameStats by statisticsManager.gameStatistics.collectAsState()
    val playerAnalysis = gameStats?.playerAnalysis

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Performance Overview Card
        PerformanceCard(playerAnalysis)
        Spacer(modifier = Modifier.height(16.dp))
        // Time Analytics Card
        TimeAnalyticsCard(playerAnalysis?.timeMetrics)
        Spacer(modifier = Modifier.height(16.dp))
        // Decision Patterns Card
        DecisionPatternsCard(playerAnalysis?.decisionPatterns)
        Spacer(modifier = Modifier.height(16.dp))
        // Achievements Card
        AchievementsCard(playerAnalysis?.achievementProgress)
    }
}

@Composable
fun PerformanceCard(analysis: PlayerAnalysis?) {
    var selectedMetric by remember { mutableStateOf<String?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val secondaryColor = colorScheme.secondary
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Performance Metrics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val metrics = analysis?.performanceMetrics
            if (metrics != null) {
                // Interactive performance graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val metricWidth = size.width / 3
                                    selectedMetric = when ((offset.x / metricWidth).toInt()) {
                                        0 -> "streak"
                                        1 -> "comebacks"
                                        2 -> "close_games"
                                        else -> null
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val barWidth = width / 4

                        // Draw streak bar
                        drawRect(
                            color = if (selectedMetric == "streak") primaryColor else secondaryColor,
                            topLeft = Offset(
                                0f,
                                height * (1 - metrics.currentStreak.toFloat() / maxOf(
                                    metrics.longestStreak,
                                    1
                                ))
                            ),
                            size = Size(
                                barWidth,
                                height * metrics.currentStreak.toFloat() / maxOf(
                                    metrics.longestStreak,
                                    1
                                )
                            ),
                            alpha = if (selectedMetric == "streak") 1f else 0.7f
                        )
                        // Draw comebacks bar
                        drawRect(
                            color = if (selectedMetric == "comebacks") primaryColor else secondaryColor,
                            topLeft = Offset(
                                width / 3,
                                height * (1 - metrics.comebacks.toFloat() / 10f)
                            ),
                            size = Size(barWidth, height * metrics.comebacks.toFloat() / 10f),
                            alpha = if (selectedMetric == "comebacks") 1f else 0.7f
                        )
                        // Draw close games bar
                        drawRect(
                            color = if (selectedMetric == "close_games") primaryColor else secondaryColor,
                            topLeft = Offset(
                                2 * width / 3,
                                height * (1 - metrics.closeGames.toFloat() / 10f)
                            ),
                            size = Size(barWidth, height * metrics.closeGames.toFloat() / 10f),
                            alpha = if (selectedMetric == "close_games") 1f else 0.7f
                        )
                    }
                }
                // Tooltip for selected metric
                selectedMetric?.let { metric ->
                    val (label, value) = when (metric) {
                        "streak" -> "Current Streak" to metrics.currentStreak.toString()
                        "comebacks" -> "Comebacks" to metrics.comebacks.toString()
                        "close_games" -> "Close Games" to metrics.closeGames.toString()
                        else -> "" to ""
                    }
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    value,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Current Streak", metrics.currentStreak.toString())
                    StatItem("Comebacks", metrics.comebacks.toString())
                    StatItem("Close Games", metrics.closeGames.toString())
                }
            }
        }
    }
}

@Composable
private fun TimeAnalyticsCard(timeMetrics: TimeMetrics?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Time Analytics",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (timeMetrics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        "Avg Game",
                        formatTime(timeMetrics.averageGameDuration)
                    )
                    StatItem(
                        "Fastest",
                        formatTime(timeMetrics.fastestGame)
                    )
                    StatItem(
                        "Total Time",
                        formatTime(timeMetrics.totalPlayTime)
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionPatternsCard(patterns: DecisionPatterns?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Decision Patterns",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (patterns != null) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Risk Taking", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { patterns.riskTaking },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Text("Average Rolls per Turn", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { patterns.averageRollsPerTurn / 6f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Text("Banking Threshold", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { patterns.bankingThreshold / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Risk Level", "${(patterns.riskTaking * 100).toInt()}%")
                    StatItem("Avg Rolls", "%.1f".format(patterns.averageRollsPerTurn))
                    StatItem("Bank At", patterns.bankingThreshold.toInt().toString())
                }
            }
        }
    }
}

@Composable
private fun AchievementsCard(achievements: Map<String, Float>?) {
    var lastUnlockedAchievement by remember { mutableStateOf<String?>(null) }
    var showCelebration by remember { mutableStateOf(false) }
    // Check for newly unlocked achievements
    LaunchedEffect(achievements) {
        achievements?.forEach { (achievement, progress) ->
            if (progress >= 1f && achievement != lastUnlockedAchievement) {
                lastUnlockedAchievement = achievement
                showCelebration = true
                // Reset celebration after delay
                kotlinx.coroutines.delay(5000)
                showCelebration = false
            }
        }
    }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Achievements",
                    style = MaterialTheme.typography.titleLarge
                )
                // Achievement count
                achievements?.let { achievementMap ->
                    val unlockedCount = achievementMap.count { it.value >= 1f }
                    Text(
                        "$unlockedCount/${achievementMap.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Achievement celebration animation
            AnimatedVisibility(
                visible = showCelebration,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                lastUnlockedAchievement?.let { achievement ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Achievement Unlocked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(
                                        animateFloatAsState(
                                            targetValue = if (showCelebration) 1.2f else 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        ).value
                                    )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Achievement Unlocked!",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                achievement.replace("_", " "),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                Achievement.valueOf(achievement).description,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            // Achievement list with animations
            achievements?.forEach { (achievement, progress) ->
                val isUnlocked = progress >= 1f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                AchievementItem(
                    name = achievement.replace("_", " "),
                    progress = animatedProgress,
                    isUnlocked = isUnlocked
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AchievementItem(
    name: String,
    progress: Float,
    isUnlocked: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Unlocked",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnlocked)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnlocked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = when {
                isUnlocked -> MaterialTheme.colorScheme.primary
                progress >= 0.8f -> Color(0xFF4CAF50)
                progress >= 0.5f -> Color(0xFFFFC107)
                else -> Color(0xFFFF5722)
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(timeInMillis: Long): String {
    val seconds = (timeInMillis / 1000) % 60
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val hours = (timeInMillis / (1000 * 60 * 60))

    return when {
        hours > 0 -> "%dh %02dm %02ds".format(hours, minutes, seconds)
        minutes > 0 -> "%dm %02ds".format(minutes, seconds)
        else -> "%ds".format(seconds)
    }
}