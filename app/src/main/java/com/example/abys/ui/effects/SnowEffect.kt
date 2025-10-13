package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private data class Snow(var x: Float, var y: Float, var r: Float, var w: Float)

@Composable
fun SnowEffect(modifier: Modifier = Modifier) {
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    var flakes by remember { mutableStateOf(listOf<Snow>()) }
    var t by remember { mutableStateOf(0f) }

    Canvas(modifier) {
        if (w != size.width || h != size.height || flakes.isEmpty()) {
            w = size.width; h = size.height
            val rnd = Random(System.nanoTime())
            flakes = List(90) {
                Snow(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    r = rnd.nextFloat() * 2.5f + 1.5f,
                    w = rnd.nextFloat() * 2f + 0.5f
                )
            }
        }
    }

    LaunchedEffect(w, h) {
        while (true) {
            t += 0.02f
            flakes.forEach {
                it.y += it.w
                it.x += sin(t + it.w) * 0.4f
                if (it.y > h) { it.y = -it.r; it.x = Random.nextFloat() * w }
            }
            delay(16L)
        }
    }

    Canvas(modifier) {
        flakes.forEach {
            drawCircle(Color(0xCCFFFFFF), radius = it.r, center = Offset(it.x, it.y))
        }
    }
}
