package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Drop(var x: Float, var y: Float, var l: Float, var s: Float)

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    params: RainParams,
    intensity: Float
) {
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    var drops by remember { mutableStateOf(listOf<Drop>()) }

    Canvas(modifier) {
        if (w != size.width || h != size.height || drops.isEmpty()) {
            w = size.width; h = size.height
            val rnd = Random(System.currentTimeMillis())
            val areaFactor = ((w * h) / (1080f * 1920f)).coerceIn(0.6f, 1.4f)
            val targetCount = (params.dropsCount * intensity * areaFactor)
                .toInt()
                .coerceIn(24, 140)
            drops = List(targetCount) {
                val baseSpeed = params.speed * (0.6f + rnd.nextFloat() * 0.6f)
                Drop(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    l = rnd.nextFloat() * 16f + 8f,
                    s = baseSpeed
                )
            }
        }
    }

    LaunchedEffect(w, h) {
        while (true) {
            val angleRad = Math.toRadians(params.angleDeg.toDouble()).toFloat()
            val drift = kotlin.math.tan(angleRad) * 4f
            val speedBoost = (0.8f + intensity * 0.6f)
            drops.forEach { d ->
                d.x += drift
                d.y += d.s * speedBoost
                if (d.y > h + 20f) {
                    d.y = -d.l
                    d.x = Random.nextFloat() * w
                }
            }
            delay(16L)
        }
    }

    Canvas(modifier) {
        val angleRad = angleRadFrom(params.angleDeg)
        val c = Color(0x66B3E5FC)
        drops.forEach { d ->
            drawLine(
                color = c,
                start = Offset(d.x, d.y),
                end = Offset(d.x - d.l * kotlin.math.sin(angleRad), d.y + d.l),
                strokeWidth = 2f
            )
        }
    }
}

private fun angleRadFrom(angle: Float): Float = Math.toRadians(angle.toDouble()).toFloat()
