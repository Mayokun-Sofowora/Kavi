package com.mayor.kavi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import kotlin.random.Random

@Composable
fun DiceRollAnimation(
    isRolling: Boolean,
    diceImage: Int,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice animation")
    
    // Rotation animation with random direction and speed
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (Random.nextBoolean()) 360f else -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (400..600).random(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ), label = "dice rotation"
    )

    // Scale animation for bouncing effect
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (300..500).random(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "dice scale"
    )

    // Random horizontal movement
    val translateX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (300..500).random(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "dice translate X"
    )

    // Random vertical movement
    val translateY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (300..500).random(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "dice translate Y"
    )

    Image(
        painter = painterResource(id = diceImage),
        contentDescription = "Dice Image",
        modifier = modifier
            .graphicsLayer {
                if (isRolling) {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                    translationX = translateX
                    translationY = translateY
                }
            },
        contentScale = ContentScale.Fit
    )
}
