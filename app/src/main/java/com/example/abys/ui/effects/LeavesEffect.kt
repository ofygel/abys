package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private val leafRandom = Random(System.currentTimeMillis())

private data class Leaf(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var phase: Float,
    var rotation: Float,
    var spin: Float,
    val sway: Float,
    val bend: Float,
    val color: Color,
    val veinColor: Color
)

@Composable
fun LeavesEffect(
    modifier: Modifier = Modifier,
    params: LeavesParams,
    intensity: Float
) {
    val density = (params.density * intensity).coerceAtLeast(0.02f)
    var particles by remember { mutableStateOf<List<Leaf>>(emptyList()) }
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    val leafPath = remember { Path() }

    LaunchedEffect(w, h) {
        while (true) {
            if (w == 0f || h == 0f) {
                delay(32L)
                continue
            }
            particles = particles.map { p ->
                val nx = p.x + p.vx + sin(p.phase) * p.sway
                val ny = p.y + p.vy
                val phaseRaw = p.phase + 0.035f
                val phase = if (phaseRaw > TAU) phaseRaw - TAU else phaseRaw
                val rotation = p.rotation + p.spin

                if (ny > h + 40f || nx < -40f || nx > w + 40f) {
                    createLeaf(w, h, params, intensity, spawnFromTop = true)
                } else {
                    p.copy(x = nx, y = ny, phase = phase, rotation = rotation)
                }
            }
            // перерисовать
            // используем recomposition триггер через новое значение
            delay(16L)
        }
    }

    Canvas(modifier) {
        if (size.width == 0f || size.height == 0f) return@Canvas

        if (w != size.width || h != size.height || particles.isEmpty()) {
            w = size.width; h = size.height
            val areaFactor = (w * h) / (1080f * 1920f)
            val baseCount = ((w * h) / 10000f * density).toInt()
            val capped = (baseCount * areaFactor.coerceAtMost(1.4f)).toInt()
            val count = capped.coerceIn(8, 60)
            particles = List(count) { createLeaf(w, h, params, intensity) }
        }

        particles.forEach { p ->
            leafPath.configureLeaf(p.size, p.bend)
            withTransform({
                translate(p.x, p.y)
                rotate(p.rotation)
            }) {
                drawPath(path = leafPath, color = p.color)
                drawLine(
                    color = p.veinColor,
                    start = Offset(0f, -p.size * 1.2f),
                    end = Offset(0f, p.size * 1.1f),
                    strokeWidth = p.size * 0.12f
                )
                val branch = p.size * 0.7f
                drawLine(
                    color = p.veinColor.copy(alpha = 0.8f),
                    start = Offset(0f, -p.size * 0.2f),
                    end = Offset(branch, p.size * 0.3f),
                    strokeWidth = p.size * 0.08f
                )
                drawLine(
                    color = p.veinColor.copy(alpha = 0.8f),
                    start = Offset(0f, p.size * 0.2f),
                    end = Offset(-branch, p.size * 0.5f),
                    strokeWidth = p.size * 0.08f
                )
            }
        }
    }
}

private const val TAU = 6.2831855f

private fun createLeaf(
    width: Float,
    height: Float,
    params: LeavesParams,
    intensity: Float,
    spawnFromTop: Boolean = false
): Leaf {
    val leafSize = leafRandom.nextFloat() * 9f + 6f
    val hue = 30f + leafRandom.nextFloat() * 25f
    val saturation = 0.65f + leafRandom.nextFloat() * 0.25f
    val value = 0.7f + leafRandom.nextFloat() * 0.2f
    val color = Color.hsv(hue, saturation, value)
    val veinColor = Color.hsv(hue, saturation * 0.5f, (value * 0.8f).coerceAtMost(1f))
    val startY = if (spawnFromTop) -leafRandom.nextFloat() * height * 0.3f - 40f else leafRandom.nextFloat() * height
    return Leaf(
        x = leafRandom.nextFloat() * width,
        y = startY,
        vx = (leafRandom.nextFloat() - 0.5f) * params.driftX * 1.2f,
        vy = (params.speedY * intensity * 0.8f) + leafRandom.nextFloat() * params.speedY,
        size = leafSize,
        phase = leafRandom.nextFloat() * TAU,
        rotation = leafRandom.nextFloat() * 360f,
        spin = (leafRandom.nextFloat() - 0.5f) * 1.2f,
        sway = (0.15f + leafRandom.nextFloat() * 0.35f) * leafSize,
        bend = (leafRandom.nextFloat() - 0.5f) * 1.2f,
        color = color,
        veinColor = veinColor
    )
}

private fun Path.configureLeaf(size: Float, bend: Float) {
    reset()
    val halfLength = size * 1.4f
    val halfWidth = size * 0.6f
    val bendOffset = bend * size * 0.5f
    moveTo(0f, -halfLength)
    cubicTo(
        halfWidth,
        -halfLength * 0.25f,
        halfWidth + bendOffset,
        halfLength * 0.2f,
        0f,
        halfLength
    )
    cubicTo(
        -halfWidth + bendOffset,
        halfLength * 0.2f,
        -halfWidth,
        -halfLength * 0.25f,
        0f,
        -halfLength
    )
    close()
}
