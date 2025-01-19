package com.mayor.kavi.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.*
import com.mayor.kavi.R

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(true) }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.confetti)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        speed = 1f,
        iterations = 1,
        restartOnPlay = false,
        cancellationBehavior = LottieCancellationBehavior.Immediately
    )
    // Handle completion
    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
            onComplete()
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}