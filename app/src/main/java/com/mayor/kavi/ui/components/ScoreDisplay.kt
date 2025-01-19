package com.mayor.kavi.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.*
import com.mayor.kavi.R
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID

/**
 * A composable that displays game scores and status information.
 *
 * Features:
 * - Current scores for all players
 * - Turn indicator
 * - Game messages and status
 * - Current turn score
 * - Visual highlighting for active player
 *
 * The component adapts its display based on the game mode:
 * - Pig: Simple numeric scores
 * - Greed: Scores with minimum threshold indication
 * - Balut: Category-based scoring
 * - Custom: Configurable score display
 *
 * @param scores Map of player indices to their scores (can be Int or Map for category-based games)
 * @param currentTurnScore Points accumulated in the current turn
 * @param message Game status or feedback message
 * @param currentPlayerIndex Index of the currently active player
 */
@Composable
fun ScoreDisplay(
    scores: Map<Int, Any>,
    currentTurnScore: Int = 0,
    message: String = "",
    currentPlayerIndex: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant),
            contentColor = colorResource(id = R.color.on_surface_variant)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.8f),
                color = colorResource(id = R.color.on_surface_variant)
            )

            ScoreGrid(
                scores = scores,
                currentTurnScore = currentTurnScore,
                currentPlayerIndex = currentPlayerIndex
            )
        }
    }
}

@Composable
fun ScoreGrid(
    scores: Map<Int, Any>,
    currentTurnScore: Int = 0,
    currentPlayerIndex: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Player Scores
        scores.forEach { (playerId, score) ->
            ScoreColumn(
                title = if (playerId == AI_PLAYER_ID.hashCode()) "AI" else
                    "Player ${playerId.hashCode() % 100}",
                score = when (score) {
                    is Int -> score
//                    is Map<*, *> -> (score as? Map<*, Int>)?.values?.sum() ?:0
                    is Map<*, *> -> score.values.filterIsInstance<Int>().sum()
                    else -> 0
                },
                isCurrentPlayer = playerId == currentPlayerIndex,
                isTurnScore = false
            )
        }

        // Turn Score (if applicable)
        if (currentTurnScore > 0) {
            ScoreColumn(
                title = "Turn Score",
                score = currentTurnScore,
                isCurrentPlayer = false,
                isTurnScore = true
            )
        }
    }
}

@Composable
private fun ScoreColumn(
    title: String,
    score: Int,
    isCurrentPlayer: Boolean,
    isTurnScore: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(
                width = if (isCurrentPlayer) 2.dp else 0.dp,
                color = if (isCurrentPlayer)
                    MaterialTheme.colorScheme.primary
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = when {
                isTurnScore -> "Turn Score"
                title == "AI" -> "AI"
                else -> "You"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (isTurnScore)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$score",
            style = MaterialTheme.typography.titleLarge,
            color = if (isTurnScore)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BalutScoreDisplay(
    playerScores: Map<String, Int>,
    aiScores: Map<String, Int>,
    playerName: String = "You",
    aiName: String = "AI"
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant),
            contentColor = colorResource(id = R.color.on_surface_variant)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Scores", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Scores
                Column(modifier = Modifier.weight(1f)) {
                    Text(playerName, style = MaterialTheme.typography.titleSmall)
                    playerScores.forEach { (category, score) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, style = MaterialTheme.typography.bodySmall)
                            Text("$score", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorResource(id = R.color.on_surface_variant)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleSmall)
                        Text("${playerScores.values.sum()}")
                    }
                }

                VerticalDivider(color = colorResource(id = R.color.on_surface_variant))

                // AI Scores
                Column(modifier = Modifier.weight(1f)) {
                    Text(aiName, style = MaterialTheme.typography.titleSmall)
                    aiScores.forEach { (category, score) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, style = MaterialTheme.typography.bodySmall)
                            Text("$score", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colorResource(id = R.color.on_surface_variant)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleSmall)
                        Text("${aiScores.values.sum()}")
                    }
                }
            }
        }
    }
}