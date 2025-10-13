package com.example.abys.ui.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun LightningOverlay(
    modifier: Modifier = Modifier,
    params: LightningParams,
    intensity: Float
) {
    var flash by remember { mutableStateOf(0f) }
    val alpha by animateFloatAsState(targetValue = flash, label = "flash")

    LaunchedEffect(Unit) {
        while (true) {
            val delayRange = params.minDelayMs..params.maxDelayMs
            val spread = (delayRange.last - delayRange.first).coerceAtLeast(0)
            val scaledSpread = (spread * (1f - intensity)).toLong().coerceAtLeast(300L)
            val nextDelay = delayRange.first + Random.nextLong(0, scaledSpread + 1)
            delay(nextDelay.coerceAtLeast(600L))
            flash = params.flashAlpha
            delay(params.flashMs.toLong())
            flash = 0f
        }
    }

    if (alpha > 0f) {
        Box(
            modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = alpha))
        )
    }
}
