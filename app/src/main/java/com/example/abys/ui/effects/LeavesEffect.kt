package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Leaf(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var r: Float,
    var phase: Float,
    val hue: Float
)

@Composable
fun LeavesEffect(modifier: Modifier = Modifier, density: Float = 0.1f) {
    // density ~ доля листьев на 10_000 px^2
    var particles by remember { mutableStateOf<List<Leaf>>(emptyList()) }
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }

    Canvas(modifier) {
        if (w != size.width || h != size.height || particles.isEmpty()) {
            w = size.width; h = size.height
            val count = ((w * h) / 10000f * density).toInt().coerceAtLeast(12)
            val rnd = Random(System.currentTimeMillis())
            particles = List(count) {
                val r = rnd.nextFloat() * 5f + 2f
                Leaf(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    vx = (rnd.nextFloat() - 0.5f) * 0.6f,
                    vy = (rnd.nextFloat() * 1.2f + 0.6f),
                    r = r,
                    phase = rnd.nextFloat() * 6.28f,
                    hue = 0.11f + rnd.nextFloat() * 0.1f // желто-оранжевая гамма
                )
            }
        }

        drawIntoCanvas {
            // шаг симуляции ~16мс
        }
    }

    LaunchedEffect(w, h) {
        while (true) {
            particles = particles.map { p ->
                val nx = p.x + p.vx + sin(p.phase) * 0.4f
                val ny = p.y + p.vy
                var x = nx; var y = ny; var phase = p.phase + 0.04f
                if (y > h + 20f) {                // респавн сверху
                    y = -10f
                    x = Random.nextFloat() * w
                }
                if (x < -20f) x = w + 10f
                if (x > w + 20f) x = -10f
                p.copy(x = x, y = y, phase = phase)
            }
            // перерисовать
            // используем recomposition триггер через новое значение
            delay(16L)
        }
    }

    Canvas(modifier) {
        particles.forEach { p ->
            // простые кружки (блики листьев), более редкие и хаотичные
            drawCircle(
                color = Color(0xFFF2B233),
                radius = p.r,
                center = Offset(p.x, p.y)
            )
        }
    }
}
