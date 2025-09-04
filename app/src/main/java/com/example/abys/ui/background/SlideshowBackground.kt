package com.example.abys.ui.background

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SlideshowBackground(
    images: List<Int>,
    anchorIndex: Int,
    anchorEpoch: Long,
    intervalMs: Long = 20_000,
    fadeMs: Int = 800,
    onIndexChange: (Int) -> Unit
) {
    if (images.isEmpty()) return

    // Стартовый индекс и задержка до первой смены — считаем один раз
    val start = remember(images, anchorIndex, anchorEpoch, intervalMs) {
        val now = System.currentTimeMillis()
        val elapsed = (now - anchorEpoch).coerceAtLeast(0L)
        val steps = if (intervalMs > 0L) (elapsed / intervalMs).toInt() else 0
        val startIdx = mod(anchorIndex + steps, images.size)
        val firstDelay = if (intervalMs > 0L) intervalMs - (elapsed % intervalMs) else 0L
        Start(index = startIdx, firstDelay = firstDelay)
    }

    var idx by remember { mutableStateOf(start.index) }
    val dim = remember { Animatable(0f) }

    LaunchedEffect(images, anchorIndex, anchorEpoch, intervalMs) {
        onIndexChange(idx)
        var wait: Long = start.firstDelay
        while (isActive) {
            if (wait > 0) delay(wait)
            // затемнение -> смена -> проявление
            dim.animateTo(1f, tween(fadeMs))
            idx = mod(idx + 1, images.size)
            onIndexChange(idx)
            dim.snapTo(1f)
            dim.animateTo(0f, tween(fadeMs))
            wait = intervalMs
        }
    }

    val painter = painterResource(id = images[idx])
    val black = ColorPainter(Color.Black)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Image(
        painter = black,
        contentDescription = null,
        modifier = Modifier.fillMaxSize().alpha(dim.value),
        contentScale = ContentScale.Crop
    )
}

private data class Start(val index: Int, val firstDelay: Long)

private fun mod(a: Int, b: Int): Int {
    if (b == 0) return 0
    val m = a % b
    return if (m < 0) m + b else m
}
