package com.example.abys.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.withFrameNanos
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Параметры дождя (оставь твой, если уже есть с такими полями) */
data class RainParams(
    val dropsCount: Int = 140,     // «бюджет» при intensity=1 и FullHD
    val speed: Float = 12f,        // базовая скорость падения (px/кадр при 60 fps)
    val angleDeg: Float = 15f      // наклон ветра (градусы от вертикали, вправо)
)

/** Внутренняя предсозданная «частица». Без аллокаций на кадр. */
private class Drop(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var len: Float,
    var width: Float,
    var phase: Float,   // для мелкой турбулентности sin/cos
    var freq: Float,    // скорость фазы
    var alpha: Float    // слой/прозрачность
)

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    params: RainParams = RainParams(),
    intensity: Float = 1f          // 0..1
) {
    var w by remember { mutableStateOf(0f) }
    var h by remember { mutableStateOf(0f) }
    var drops by remember { mutableStateOf<MutableList<Drop>>(mutableListOf()) }
    var frame by remember { mutableStateOf(0) } // единственный триггер перерисовки

    val rnd = remember { Random(System.nanoTime()) }
    val intensityClamped = intensity.coerceIn(0f, 1f)

    // Инициализация/реинициализация при изменении размера или параметров
    Canvas(modifier) {
        if (w != size.width || h != size.height || drops.isEmpty()) {
            w = size.width
            h = size.height

            // нормируем «бюджет» к площади
            val areaFactor = ((w * h) / (1080f * 1920f)).coerceIn(0.6f, 1.6f)
            val target = (params.dropsCount * areaFactor * (0.35f + 0.65f * intensityClamped))
                .toInt()
                .coerceIn(32, 220)

            drops = MutableList(target) { i ->
                // 3 слоя: ближний/средний/дальний
                val layer = when {
                    i % 7 == 0 -> 0 // ближний (~14%)
                    i % 3 == 0 -> 2 // дальний (~33%)
                    else       -> 1 // средний (~53%)
                }
                val layerSpeedMul = when (layer) { 0 -> 1.8f; 1 -> 1.3f; else -> 1.0f }
                val layerAlpha    = when (layer) { 0 -> 0.85f; 1 -> 0.65f; else -> 0.45f }
                val layerWidth    = when (layer) { 0 -> 2.4f;  1 -> 1.8f;  else -> 1.2f }

                val angleRad = toRad(params.angleDeg)
                val base = params.speed * (0.8f + rnd.nextFloat() * 0.6f) * (0.8f + intensityClamped * 0.6f)
                val vy = base * cos(angleRad) * layerSpeedMul
                val vx = base * sin(angleRad) * layerSpeedMul

                val len = (10f + rnd.nextFloat() * 18f) * layerSpeedMul // длина — от скорости/слоя
                val width = (layerWidth * (0.9f + rnd.nextFloat() * 0.4f))

                Drop(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    vx = vx,
                    vy = vy,
                    len = len,
                    width = width,
                    phase = rnd.nextFloat() * (PI * 2).toFloat(),
                    freq = 0.04f + rnd.nextFloat() * 0.06f,
                    alpha = layerAlpha
                )
            }
        }
    }

    // Физика (фикcированный шаг ~16мс). Никаких аллокаций в петле.
    LaunchedEffect(w, h, params.dropsCount, params.speed, params.angleDeg, intensityClamped) {
        var last = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (last == 0L) {
                last = now
                continue
            }
            val dt = ((now - last) / 1_000_000f).coerceAtMost(32f) / 16f // ~60fps-нормировка
            last = now

            val margin = 32f
            drops.forEach { d ->
                val sway = sin(d.phase) * (0.35f + 0.65f * intensityClamped)
                d.x += (d.vx + sway) * dt
                d.y += d.vy * dt
                d.phase += d.freq * dt
                if (d.y > h + d.len || d.x < -margin || d.x > w + margin) {
                    d.y = -d.len
                    d.x = rnd.nextFloat() * w
                    d.phase = rnd.nextFloat() * (PI * 2).toFloat()
                    d.width *= (0.9f + rnd.nextFloat() * 0.2f)
                }
            }
            frame++ // триггер перерисовки Canvas
        }
    }

    // Отрисовка одним Canvas
    Canvas(modifier) {
        // Цвет капли мягкий, альфа варьируется слоем
        val angleRad = toRad(params.angleDeg)
        val dxUnit = sin(angleRad)
        val dyUnit = cos(angleRad)

        for (i in 0 until drops.size) {
            val d = drops[i]
            // линия наклонена в ТУ ЖЕ сторону, что и вектор движения
            val dx = d.len * dxUnit
            val dy = d.len * dyUnit

            // легкое «подсветить» ближний слой (alpha множитель)
            val color = RAIN_COLOR.copy(alpha = (d.alpha))

            drawLine(
                color = color,
                start = Offset(d.x, d.y),
                end   = Offset(d.x + dx, d.y + dy),
                strokeWidth = d.width
            )
        }
    }
}

private fun toRad(deg: Float): Float = Math.toRadians(deg.toDouble()).toFloat()

// небесно-голубой с прозрачностью; ближний слой даст реальную яркость
private val RAIN_COLOR = Color(0xFFB3E5FC)
