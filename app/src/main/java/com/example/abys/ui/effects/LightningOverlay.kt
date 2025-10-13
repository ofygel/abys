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
fun LightningOverlay(modifier: Modifier = Modifier) {
    var flash by remember { mutableStateOf(0f) }
    val alpha by animateFloatAsState(targetValue = flash, label = "flash")

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1500, 6000))
            flash = 0.9f
            delay(60)
            flash = 0f
        }
    }

    if (alpha > 0f) {
        Box(
            modifier
                .fillMaxSize()
                .background(Color(0x88FFFFFF))
        )
    }
}
