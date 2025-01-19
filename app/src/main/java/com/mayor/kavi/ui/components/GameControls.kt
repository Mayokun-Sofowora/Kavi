package com.mayor.kavi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.*
import com.mayor.kavi.R

/**
 * A composable that provides game control buttons and interactions.
 *
 * Features:
 * - Roll button with state management
 * - Optional banking control
 * - Category selection for Balut
 * - Turn management
 * - Dynamic button states
 * - Visual feedback
 *
 * The controls adapt based on:
 * - Current game variant
 * - Game state
 * - Player turn
 * - Available actions
 *
 * @param onRoll Callback for dice roll action
 * @param onBank Optional callback for banking points
 * @param onSelectedCategory Optional callback for category selection (Balut)
 * @param onEndTurn Optional callback for ending turn
 * @param isRolling Whether a roll animation is in progress
 * @param canReroll Whether rolling is currently allowed
 */
@Composable
fun GameControls(
    onRoll: () -> Unit,
    onBank: (() -> Unit)? = null,
    onSelectedCategory: (() -> Unit)? = null,
    onEndTurn: (() -> Unit)? = null,
    isRolling: Boolean = false,
    canReroll: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onRoll,
            enabled = !isRolling && canReroll,
            colors = ButtonDefaults.buttonColors(
                contentColor = colorResource(id = R.color.on_primary)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Roll")
        }

        if (onBank != null) {
            Button(
                onClick = onBank,
                colors = ButtonDefaults.buttonColors(
                    contentColor = colorResource(id = R.color.on_primary)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Bank")
            }
        }

        if (onEndTurn != null) {
            Button(
                onClick = onEndTurn,
                colors = ButtonDefaults.buttonColors(
                    contentColor = colorResource(id = R.color.on_primary)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("End Turn")
            }
        }

        if (onSelectedCategory != null) {
            Button(
                onClick = onSelectedCategory,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    contentColor = colorResource(id = R.color.on_primary)
                )
            ) {
                Text("Score")
            }
        }
    }
}
