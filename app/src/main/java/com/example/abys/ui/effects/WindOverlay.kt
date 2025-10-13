package com.example.abys.ui.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun WindOverlay(modifier: Modifier = Modifier, strength: Float = 8f) {
    var t by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) { t += 0.05f; delay(16) }
    }
    // Просто прозрачная коробка: эффект смещения будет применяться к карточке (см. HomeScreen)
    Box(
        modifier
            .fillMaxSize()
            .alpha(0f)
            .graphicsLayer { /* placeholder */ }
    )
}

/** Модификатор лёгкого сдвига под ветер — применим к карточке */
fun Modifier.windSway(enabled: Boolean, t: Float, strength: Float = 8f): Modifier =
    if (!enabled) this else this.graphicsLayer {
        translationX = sin(t) * strength
        translationY = sin(t * 0.7f) * (strength / 3f)
    }
