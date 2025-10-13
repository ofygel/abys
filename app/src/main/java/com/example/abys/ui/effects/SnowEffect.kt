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
fun SnowEffect(
    modifier: Modifier = Modifier,
    params: SnowParams,
    intensity: Float
) {
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    var flakes by remember { mutableStateOf(listOf<Snow>()) }
    var t by remember { mutableStateOf(0f) }

    Canvas(modifier) {
        if (w != size.width || h != size.height || flakes.isEmpty()) {
            w = size.width; h = size.height
            val rnd = Random(System.nanoTime())
            val areaFactor = ((w * h) / (1080f * 1920f)).coerceIn(0.6f, 1.4f)
            val targetCount = (params.flakesCount * intensity * areaFactor)
                .toInt()
                .coerceIn(30, 120)
            flakes = List(targetCount) {
                Snow(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    r = rnd.nextFloat() * (params.size.endInclusive - params.size.start) + params.size.start,
                    w = rnd.nextFloat() * params.speed + params.speed * 0.2f
                )
            }
        }
    }

    LaunchedEffect(w, h) {
        while (true) {
            t += 0.02f
            flakes.forEach {
                it.y += it.w * (0.6f + intensity * 0.7f)
                it.x += sin(t + it.w) * params.driftX
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
