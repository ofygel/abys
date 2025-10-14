package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SnowEffect(
    modifier: Modifier = Modifier,
    params: SnowParams,
    intensity: Float
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    var flakesCount by remember { mutableStateOf(0) }
    var x by remember { mutableStateOf(FloatArray(0)) }
    var y by remember { mutableStateOf(FloatArray(0)) }
    var r by remember { mutableStateOf(FloatArray(0)) }
    var speed by remember { mutableStateOf(FloatArray(0)) }
    var phase by remember { mutableStateOf(FloatArray(0)) }
    var layer by remember { mutableStateOf(IntArray(0)) }
    var alpha by remember { mutableStateOf(FloatArray(0)) }

    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(size, intensity, params) {
        if (size.width == 0 || size.height == 0) return@LaunchedEffect

        val width = size.width.toFloat()
        val height = size.height.toFloat()
        val areaFactor = ((width * height) / (1080f * 1920f)).coerceIn(0.6f, 2.2f)
        val target = (params.flakesCount * intensity * areaFactor)
            .toInt()
            .coerceIn(40, 180)

        flakesCount = target
        x = FloatArray(flakesCount)
        y = FloatArray(flakesCount)
        r = FloatArray(flakesCount)
        speed = FloatArray(flakesCount)
        phase = FloatArray(flakesCount)
        layer = IntArray(flakesCount)
        alpha = FloatArray(flakesCount)

        val random = Random(System.nanoTime())
        val radiusMin = params.size.start
        val radiusMax = params.size.endInclusive

        repeat(flakesCount) { index ->
            val layerValue = when (random.nextFloat()) {
                in 0f..0.30f -> 0
                in 0.30f..0.70f -> 1
                else -> 2
            }
            layer[index] = layerValue

            val baseRadius = radiusMin + random.nextFloat() * (radiusMax - radiusMin)
            val radius = when (layerValue) {
                0 -> baseRadius * 1.15f
                1 -> baseRadius
                else -> baseRadius * 0.85f
            }
            r[index] = radius

            val baseSpeed = params.speed * (0.6f + radius * 0.35f)
            speed[index] = baseSpeed * when (layerValue) {
                0 -> 1.25f
                1 -> 1.0f
                else -> 0.8f
            }

            x[index] = random.nextFloat() * width
            y[index] = random.nextFloat() * height
            phase[index] = random.nextFloat() * (PI.toFloat() * 2f)

            alpha[index] = when (layerValue) {
                0 -> 0.90f
                1 -> 0.72f
                else -> 0.55f
            }
        }
    }

    LaunchedEffect(size, flakesCount, params, intensity) {
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        if (flakesCount == 0) return@LaunchedEffect

        val width = size.width.toFloat()
        val height = size.height.toFloat()
        var lastTime = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (lastTime == 0L) {
                lastTime = now
                continue
            }
            val deltaMs = ((now - lastTime) / 1_000_000L).coerceAtMost(34L).toFloat()
            lastTime = now
            val dt = deltaMs / 16f
            val baseDrift = params.driftX

            for (index in 0 until flakesCount) {
                val layerValue = layer[index]
                val drift = baseDrift * when (layerValue) {
                    0 -> 0.45f
                    1 -> 0.60f
                    else -> 0.75f
                } * (1f / (0.8f + r[index]))

                phase[index] += 0.03f + (0.008f * layerValue)
                y[index] += speed[index] * (0.6f + intensity * 0.7f) * dt
                x[index] += sin(phase[index]) * drift * dt * 16f

                if (y[index] > height + r[index]) {
                    y[index] = -Random.nextFloat() * (height * 0.15f) - r[index]
                    val sideJitter = (if (Random.nextBoolean()) 1 else -1) * (2f + 18f * Random.nextFloat())
                    val newX = x[index] + sideJitter
                    x[index] = when {
                        newX < -20f -> width + 10f
                        newX > width + 20f -> -10f
                        else -> newX
                    }
                }
            }

            frame++
        }
    }

    Canvas(
        modifier = modifier.onSizeChanged { size = it }
    ) {
        val _ = frame
        for (index in 0 until flakesCount) {
            drawCircle(
                color = Color.White,
                radius = r[index],
                center = Offset(x[index], y[index]),
                alpha = alpha[index]
            )
        }
    }
}
