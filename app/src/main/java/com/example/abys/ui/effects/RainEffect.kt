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
fun RainEffect(modifier: Modifier = Modifier) {
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    var drops by remember { mutableStateOf(listOf<Drop>()) }

    Canvas(modifier) {
        if (w != size.width || h != size.height || drops.isEmpty()) {
            w = size.width; h = size.height
            val rnd = Random(System.currentTimeMillis())
            drops = List(120) {
                Drop(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    l = rnd.nextFloat() * 16f + 8f,
                    s = rnd.nextFloat() * 6f + 8f
                )
            }
        }
    }

    LaunchedEffect(w, h) {
        while (true) {
            drops.forEach { d ->
                d.x += 1.8f
                d.y += d.s
                if (d.y > h) { d.y = -d.l; d.x = Random.nextFloat() * w }
            }
            delay(16L)
        }
    }

    Canvas(modifier) {
        val c = Color(0x66B3E5FC)
        drops.forEach { d ->
            drawLine(
                color = c,
                start = Offset(d.x, d.y),
                end = Offset(d.x - d.l * 0.3f, d.y + d.l),
                strokeWidth = 2f
            )
        }
    }
}
