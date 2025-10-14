package com.example.abys.ui.effects

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/** Ожидается ваш LightningParams с полями:
 *  minDelayMs, maxDelayMs, flashAlpha, flashMs
 *  (остальные не обязательны)
 */

@Composable
fun LightningOverlay(
    modifier: Modifier = Modifier,
    params: LightningParams,
    intensity: Float
) {
    val alphaAnim = remember { Animatable(0f) }

    // Состояние «очага» вспышки (для радиального/вертикального градиента)
    var center by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableStateOf(0f) }
    var useRadial by remember { mutableStateOf(true) }

    // Главная петля: ждём случайный интервал → пускаем 1–3 импульса
    LaunchedEffect(params, intensity.coerceIn(0f, 1f)) {
        val minI = max(300L, params.minDelayMs.toLong())
        val maxI = max(minI + 1, params.maxDelayMs.toLong())
        val mean = lerp(maxI.toFloat(), minI.toFloat(), intensity.coerceIn(0f, 1f)).toLong()

        while (true) {
            val wait = expDelay(mean, minI, maxI)  // экспоненциальная «естественная» пауза
            delay(wait)

            // Рандомизируем геометрию очага под текущий кадр/размер — на Canvas пересчитаем точно
            useRadial = Random.nextBoolean()

            // Сколько вспышек подряд (редко 2–3)
            val pulses = if (Random.nextFloat() < 0.22f) Random.nextInt(2, 4) else 1

            repeat(pulses) { i ->
                // Пик яркости (чуть зависит от intensity)
                val peak = lerp(0.65f, params.flashAlpha.coerceIn(0f, 1f), intensity)

                // Длительности на основе flashMs: пред-флэш / пик / хвост
                val preMs  = max(24, (params.flashMs * 0.35f).toInt())
                val mainMs = max(40, (params.flashMs * 0.65f).toInt())
                val tailMs = max(80, (params.flashMs * 1.25f).toInt())

                // Перераздать центр/радиус непосредственно перед импульсом
                // (центр — верхняя половина, радиус — от 35% до 65% диагонали вьюпорта;
                //  Canvas точные размеры даст в draw блоке)
                // ↓ маркеры обновятся при первом draw
                center = Offset(-1f, -1f)
                radius = -1f

                // 0 → pre-peak → peak → 0 (с экспоненциальным хвостом)
                alphaAnim.snapTo(0f)
                alphaAnim.animateTo(peak * 0.55f, tween(durationMillis = preMs, easing = LinearEasing))
                alphaAnim.animateTo(peak, tween(durationMillis = mainMs, easing = LinearEasing))
                alphaAnim.animateTo(0f, tween(durationMillis = tailMs, easing = { t -> 1f - (1f - t) * (1f - t) }))
                if (i < pulses - 1) delay(90L)
            }
        }
    }

    val a = alphaAnim.value
    if (a > 0f) {
        Canvas(modifier) {
            // Инициализация очага под реальные размеры канвы — один раз на импульс
            if (center.x < 0f) {
                val w = size.width; val h = size.height
                val topBandY = h * 0.15f
                val topMaxY  = h * 0.45f
                center = Offset(
                    x = Random.nextFloat() * w,
                    y = Random.nextFloat() * (topMaxY - topBandY) + topBandY
                )
                val diag = sqrt(w * w + h * h)
                radius = Random.nextFloat() * (diag * 0.30f) + (diag * 0.35f) // 35%..65% диагонали
            }

            // Цвет: холодный бело-голубой, без «грязного» серого
            val white = Color(0xFFFFFFFF)
            val cold  = Color(0xFFE6F0FF)

            if (useRadial) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            white.copy(alpha = a * 0.95f),
                            cold.copy(alpha = a * 0.45f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    size = size
                )
            } else {
                // Вертикальный градиент «от облаков»
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to white.copy(alpha = a * 0.85f),
                        0.25f to cold.copy(alpha = a * 0.50f),
                        1f to Color.Transparent
                    ),
                    size = size
                )
            }
        }
    }
}

/* ------- helpers ------- */

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/** Экспоненциальная задержка (приближенно под «случайные молнии») + клип по min/max */
private fun expDelay(meanMs: Long, minMs: Long, maxMs: Long): Long {
    val u = Random.nextDouble(1e-6, 1.0)              // (0,1]
    val raw = (-kotlin.math.ln(u) * meanMs).toLong()  // Exp(mean)
    return min(max(raw, minMs), maxMs)
}
