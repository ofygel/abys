package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Star(var x: Float, var y: Float, var radius: Float, var phase: Float, var period: Float)

@Composable
fun StarsEffect(modifier: Modifier = Modifier, params: StarsParams, intensity: Float) {
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    var stars by remember { mutableStateOf(listOf<Star>()) }
    var time by remember { mutableStateOf(0f) }

    Canvas(modifier) {
        if (w != size.width || h != size.height || stars.isEmpty()) {
            w = size.width; h = size.height
            val rnd = Random(System.nanoTime())
            val areaFactor = ((w * h) / (1080f * 1920f)).coerceIn(0.4f, 1.2f)
            val count = (params.starsCount * intensity * areaFactor)
                .toInt()
                .coerceIn(24, 80)
            stars = List(count) {
                val period = rnd.nextInt(params.twinklePeriodMs.first, params.twinklePeriodMs.last)
                Star(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    radius = rnd.nextFloat() * 2f + 1f,
                    phase = rnd.nextFloat() * 6.28f,
                    period = period.toFloat()
                )
            }
        }
    }

    LaunchedEffect(w, h) {
        while (true) {
            time += 16f
            delay(16L)
        }
    }

    Canvas(modifier) {
        stars.forEach { star ->
            val twinkle = ((kotlin.math.sin((time + star.phase * 100f) / star.period * 6.28f) + 1f) / 2f)
                .coerceIn(0.2f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = twinkle),
                radius = star.radius,
                center = Offset(star.x, star.y)
            )
        }
    }
}
