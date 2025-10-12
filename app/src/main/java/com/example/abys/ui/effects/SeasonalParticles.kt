package com.example.abys.ui.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
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

@Composable
fun SeasonalParticles() {
    val season = remember { currentSeason() }
    val seed = remember { Random.nextInt() }
    var t by remember { mutableStateOf(0f) }
    val smoothT by animateFloatAsState(targetValue = t, label = "tick")

    LaunchedEffect(season) {
        while (true) {
            t += 0.016f // ~60 FPS
            delay(16)
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        Random(seed)
        val count =
            when (season) {
                Season.Winter -> 120
                Season.Autumn -> 90
                Season.Spring -> 70
                Season.Summer -> 50
            }

        repeat(count) { i ->
            val baseX = (i * 997) % size.width.toInt()
            val baseY = (i * 613) % size.height.toInt()
            val depth = (i % 7 + 1) / 7f // 0..1
            val speed = when (season) {
                Season.Winter -> 28f
                Season.Autumn -> 22f
                else -> 14f
            } * (0.5f + depth)

            val wind = when (season) {
                Season.Autumn -> 0.8f
                Season.Winter -> 0.4f
                else -> 0.2f
            }

            val x = (baseX + (smoothT * speed * wind * (1f + 0.2f * sin(i.toFloat())))) % size.width
            val y = (baseY + (smoothT * speed * (1.2f - 0.4f * depth))) % size.height

            val color = when (season) {
                Season.Winter -> Color(0xCCFFFFFF)
                Season.Autumn -> Color(0xCCF9A825) // желто-оранж
                Season.Spring -> Color(0xCC81C784) // зелень
                Season.Summer -> Color(0xCC64B5F6) // голубой
            }
            val r = (2f + 3f * depth)

            drawIntoCanvas {
                drawCircle(color, radius = r.dp.toPx(), center = Offset(x, y))
                if (season == Season.Winter) {
                    // лёгкая дрожь, имитация снежинки
                    drawCircle(color.copy(alpha = 0.6f),
                        radius = (r*0.6f).dp.toPx(),
                        center = Offset(x + cos(smoothT + i) * 1.2f, y + sin(smoothT * 0.9f + i) * 1.2f)
                    )
                }
            }
        }
    }
}
