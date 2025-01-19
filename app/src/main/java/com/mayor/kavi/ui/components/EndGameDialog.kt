package com.mayor.kavi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mayor.kavi.util.ConfettiAnimation

@Composable
fun GameEndDialog(
    message: String,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    // Box for layering confetti and dialog
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Dimmed background
    ) {
        // Confetti animation at the top layer
        if (message.contains("You win")) {
            ConfettiAnimation(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f) // Ensure confetti is above dialog
            )
        }
        // Dialog content
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
                .zIndex(1f) // Ensure dialog is below confetti
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = if (message.contains("You win")) "Congratulations" else "Game Over",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(24.dp)
                ) {
                    TextButton(onClick = onExit) {
                        Text("Exit")
                    }
                    TextButton(onClick = onPlayAgain) {
                        Text("Play Again")
                    }
                }
            }
        }
    }
}
