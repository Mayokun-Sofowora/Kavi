package com.mayor.kavi.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mayor.kavi.R
import com.mayor.kavi.data.models.enums.DiceArrangement

/**
 * A composable that displays interactive dice with animations and hold functionality.
 *
 * Features:
 * - Configurable layout (grid or row)
 * - Rolling animations
 * - Dice holding mechanism
 * - Turn-based interaction control
 * - Customizable size and appearance
 *
 * @param diceImages List of resource IDs for dice face images
 * @param isRolling Whether dice are currently in rolling animation
 * @param heldDice Set of indices of dice that are currently held
 * @param isMyTurn Whether it's the player's turn (affects interaction)
 * @param onDiceHold Callback for when a die is held/unheld (null disables holding)
 * @param diceSize Size of each die
 * @param arrangement Layout arrangement (GRID or ROW)
 * @param modifier Optional modifier for the component
 */
@Composable
fun DiceDisplay(
    diceImages: List<Int>,
    isRolling: Boolean,
    heldDice: Set<Int>,
    isMyTurn: Boolean = true,
    onDiceHold: ((Int) -> Unit)? = null,
    diceSize: Dp = 100.dp,
    arrangement: DiceArrangement = DiceArrangement.GRID,
    modifier: Modifier? = Modifier
) {
    when (arrangement) {
        DiceArrangement.GRID -> DiceGrid(
            diceImages = diceImages,
            isRolling = isRolling,
            heldDice = heldDice,
            isMyTurn = isMyTurn,
            onDiceHold = onDiceHold,
            diceSize = diceSize
        )
        DiceArrangement.ROW -> DiceRow(
            diceImages = diceImages,
            isRolling = isRolling,
            heldDice = heldDice,
            isMyTurn = isMyTurn,
            onDiceHold = onDiceHold,
            diceSize = diceSize
        )
    }
}

/**
 * Displays dice in a grid layout.
 *
 * Arranges dice in a responsive grid that adjusts based on:
 * - Number of dice
 * - Available space
 * - Device orientation
 *
 * @param diceImages List of resource IDs for dice face images
 * @param isRolling Whether dice are currently in rolling animation
 * @param heldDice Set of indices of dice that are currently held
 * @param isMyTurn Whether it's the player's turn
 * @param onDiceHold Callback for dice hold/unhold actions
 * @param diceSize Size of each die
 */
@Composable
private fun DiceGrid(
    diceImages: List<Int>,
    isRolling: Boolean,
    heldDice: Set<Int>,
    isMyTurn: Boolean,
    onDiceHold: ((Int) -> Unit)?,
    diceSize: Dp
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row (3 dice)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            diceImages.take(3).forEachIndexed { index, diceImage ->
                DiceItem(
                    isRolling = isRolling,
                    isHeld = heldDice.contains(index),
                    diceImage = diceImage,
                    isMyTurn = isMyTurn,
                    onDiceClick = { onDiceHold?.invoke(index) },
                    size = diceSize
                )
            }
        }

        // Second row (3 dice)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            diceImages.drop(3).take(3).forEachIndexed { index, diceImage ->
                DiceItem(
                    isRolling = isRolling,
                    isHeld = heldDice.contains(index + 3),
                    diceImage = diceImage,
                    isMyTurn = isMyTurn,
                    onDiceClick = { onDiceHold?.invoke(index + 3) },
                    size = diceSize
                )
            }
        }
    }
}

/**
 * Displays dice in a horizontal row.
 *
 * Ideal for:
 * - Single die games (Pig)
 * - Small number of dice
 * - Horizontal layouts
 *
 * @param diceImages List of resource IDs for dice face images
 * @param isRolling Whether dice are currently in rolling animation
 * @param heldDice Set of indices of dice that are currently held
 * @param isMyTurn Whether it's the player's turn
 * @param onDiceHold Callback for dice hold/unhold actions
 * @param diceSize Size of each die
 */
@Composable
private fun DiceRow(
    diceImages: List<Int>,
    isRolling: Boolean,
    heldDice: Set<Int>,
    isMyTurn: Boolean,
    onDiceHold: ((Int) -> Unit)?,
    diceSize: Dp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        diceImages.forEachIndexed { index, diceImage ->
            DiceItem(
                isRolling = isRolling,
                isHeld = heldDice.contains(index),
                diceImage = diceImage,
                isMyTurn = isMyTurn,
                onDiceClick = { onDiceHold?.invoke(index) },
                size = diceSize
            )
        }
    }
}

/**
 * Individual die display component.
 *
 * Features:
 * - Interactive touch handling
 * - Hold state visualization
 * - Rolling animation
 * - Accessibility support
 *
 * @param imageResource Resource ID for the die face
 * @param isHeld Whether the die is currently held
 * @param isRolling Whether the die is in rolling animation
 * @param onClick Callback for die interaction
 * @param size Size of the die
 */
@Composable
fun DiceItem(
    isRolling: Boolean,
    isHeld: Boolean,
    diceImage: Int,
    isMyTurn: Boolean,
    onDiceClick: () -> Unit,
    size: Dp = 100.dp
) {
    Box(
        modifier = Modifier
            .clickable(
                enabled = !isRolling && isMyTurn,
                onClick = onDiceClick
            )
            .background(
                color = if (isHeld)
                    colorResource(id = R.color.scrim).copy(alpha = 0.2f)
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        DiceRollAnimation(
            isRolling = isRolling && !isHeld,
            diceImage = diceImage,
            modifier = Modifier.size(size)
        )
    }
}