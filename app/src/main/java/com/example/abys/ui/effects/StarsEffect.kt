package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.android.awaitFrame
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

data class StarsParams(
    val starsCount: Int = 64,
    val twinklePeriodMs: IntRange = 1200..2400,
    val twinkleAmplitude: Float = 0.65f,
    val baseAlpha: ClosedFloatingPointRange<Float> = 0.35f..0.85f,
    val shootingStars: Int = 2,
    val shootingIntervalMs: IntRange = 20000..35000,
    val shootingSpeedPxSec: Float = 900f,
    val tailLengthPx: Float = 160f,
    val tailAlpha: Float = 0.9f
) : EffectParams {
    override val kind: EffectKind = EffectKind.NIGHT
}

/**
 * Звёздное небо:
 * - 3 слоя звёзд (размер/альфа/скорость мерцания отличаются) → ощущение глубины
 * - vsync-драйвер через awaitFrame() → гладкое мерцание
 * - нулевая аллокация в кадре (все массивы предсозданы и модифицируются in-place)
 * - опционально 1–2 «падающие звезды» с хвостом (редко, без GC)
 */
@Composable
fun StarsEffect(
    modifier: Modifier = Modifier,
    params: StarsParams = StarsParams(),
    intensity: Float = 1f
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    var n by remember { mutableStateOf(0) }
    var x by remember { mutableStateOf(FloatArray(0)) }
    var y by remember { mutableStateOf(FloatArray(0)) }
    var r by remember { mutableStateOf(FloatArray(0)) }
    var baseA by remember { mutableStateOf(FloatArray(0)) }
    var ampA by remember { mutableStateOf(FloatArray(0)) }
    var omega by remember { mutableStateOf(FloatArray(0)) }
    var phase by remember { mutableStateOf(FloatArray(0)) }
    var layer by remember { mutableStateOf(IntArray(0)) }

    data class Shooting(
        var active: Boolean = false,
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var ttlMs: Float = 0f,
        var cooldownMs: Long = 0L
    )
    var shooters by remember { mutableStateOf(Array(params.shootingStars) { Shooting() }) }

    var frame by remember { mutableIntStateOf(0) }

    LaunchedEffect(size, intensity, params) {
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        val w = size.width.toFloat()
        val h = size.height.toFloat()

        val areaFactor = ((w * h) / (1080f * 1920f)).coerceIn(0.5f, 2.2f)
        val target = (params.starsCount * intensity * areaFactor).toInt().coerceIn(24, 120)

        n = target
        x = FloatArray(n)
        y = FloatArray(n)
        r = FloatArray(n)
        baseA = FloatArray(n)
        ampA = FloatArray(n)
        omega = FloatArray(n)
        phase = FloatArray(n)
        layer = IntArray(n)

        val rnd = Random(System.nanoTime())
        for (i in 0 until n) {
            val L = when (rnd.nextFloat()) {
                in 0f..0.30f -> 0
                in 0.30f..0.75f -> 1
                else -> 2
            }
            layer[i] = L

            x[i] = rnd.nextFloat() * w
            y[i] = rnd.nextFloat() * h

            val baseR = 1f + rnd.nextFloat() * 2f
            r[i] = when (L) {
                0 -> baseR * 1.25f
                1 -> baseR
                else -> baseR * 0.8f
            }

            val ba = params.baseAlpha.start + rnd.nextFloat() *
                (params.baseAlpha.endInclusive - params.baseAlpha.start)
            baseA[i] = when (L) {
                0 -> ba * 1.0f
                1 -> ba * 0.9f
                else -> ba * 0.8f
            }.coerceIn(0.25f, 1f)

            val amp = params.twinkleAmplitude * (0.6f + rnd.nextFloat() * 0.6f)
            ampA[i] = when (L) {
                0 -> amp * 0.8f
                1 -> amp
                else -> amp * 1.2f
            }.coerceIn(0.15f, 0.95f)

            val periodMs = rnd.nextInt(params.twinklePeriodMs.first, params.twinklePeriodMs.last)
            val periodSec = max(0.6f, periodMs / 1000f)
            omega[i] = (2f * PI.toFloat()) / periodSec

            phase[i] = rnd.nextFloat() * (2f * PI.toFloat())
        }

        shooters = Array(params.shootingStars) {
            Shooting().apply {
                active = false
                cooldownMs = 1_000L
            }
        }
    }

    LaunchedEffect(size, params) {
        if (size.width == 0 || size.height == 0) return@LaunchedEffect
        val w = size.width.toFloat()
        val h = size.height.toFloat()
        val rnd = Random(System.nanoTime())
        var lastNs = 0L
        var tSec = 0f

        while (true) {
            val now = awaitFrame()
            if (lastNs == 0L) {
                lastNs = now
                continue
            }
            val dtMs = ((now - lastNs) / 1_000_000L).coerceAtMost(34L).toFloat()
            lastNs = now
            val dt = dtMs / 1000f
            tSec += dt

            val driftX = sin(tSec * 0.04f) * 0.6f
            val driftY = cos(tSec * 0.03f) * 0.4f

            for (i in 0 until n) {
                val L = layer[i]
                val twinkle = baseA[i] + ampA[i] * 0.5f * (1f + sin(omega[i] * tSec + phase[i]))
                val par = when (L) {
                    0 -> 1.0f
                    1 -> 0.6f
                    else -> 0.35f
                }
                x[i] = (x[i] + driftX * par).let { xx ->
                    when {
                        xx < -8f -> w + 8f
                        xx > w + 8f -> -8f
                        else -> xx
                    }
                }
                y[i] = (y[i] + driftY * par).let { yy ->
                    when {
                        yy < -8f -> h + 8f
                        yy > h + 8f -> -8f
                        else -> yy
                    }
                }
                baseA[i] = twinkle.coerceIn(0.15f, 1f)
            }

            for (s in shooters) {
                if (!s.active) {
                    s.cooldownMs -= dtMs.toLong()
                    if (s.cooldownMs <= 0L) {
                        s.active = true
                        val side = rnd.nextInt(0, 2)
                        s.x = if (side == 0) -20f else w + 20f
                        s.y = rnd.nextFloat() * (h * 0.35f)
                        val angle = if (side == 0) -15f else 195f
                        val rad = angle / 180f * PI.toFloat()
                        s.vx = cos(rad) * params.shootingSpeedPxSec
                        s.vy = sin(rad) * params.shootingSpeedPxSec
                        s.ttlMs = (1_600f..2_400f).random()
                        s.cooldownMs = rnd.nextLong(
                            params.shootingIntervalMs.first.toLong(),
                            params.shootingIntervalMs.last.toLong()
                        )
                    }
                } else {
                    s.x += s.vx * dt
                    s.y += s.vy * dt
                    s.ttlMs -= dtMs
                    if (s.ttlMs <= 0f || s.x < -100f || s.x > w + 100f || s.y > h + 100f) {
                        s.active = false
                    }
                }
            }

            frame++
        }
    }

    Canvas(modifier = modifier.onSizeChanged { size = it }) {
        val _ = frame

        for (i in 0 until n) {
            drawCircle(
                color = Color.White,
                radius = r[i],
                center = Offset(x[i], y[i]),
                alpha = baseA[i]
            )
        }

        shooters.forEach { s ->
            if (!s.active) return@forEach
            drawCircle(
                color = Color.White,
                radius = 2.6f,
                center = Offset(s.x, s.y),
                alpha = params.tailAlpha
            )
            val segs = 6
            val step = params.tailLengthPx / segs
            val len = hypot(s.vx, s.vy)
            val ux = if (len > 0f) -s.vx / len else 0f
            val uy = if (len > 0f) -s.vy / len else 0f
            var px = s.x
            var py = s.y
            var a = params.tailAlpha
            repeat(segs) {
                val nx = px + ux * step
                val ny = py + uy * step
                drawLine(
                    color = Color.White.copy(alpha = a),
                    start = Offset(px, py),
                    end = Offset(nx, ny),
                    strokeWidth = 2f
                )
                px = nx
                py = ny
                a *= 0.72f
            }
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float {
    val start = start
    val end = endInclusive
    return start + (end - start) * Random.nextFloat()
}
