package com.example.abys.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Текущее состояние ветра — читается UI-слоями. */
@Stable
data class WindState(
    val phase: Float = 0f,     // «фаза» синуса
    val amp: Float = 0f,       // общая амплитуда (с учётом порывов и intensity)
    val swayX: Float = 0f,     // готовые значения для карточки
    val swayY: Float = 0f,
    val rotZ: Float = 0f,      // вращение карточки в градусах
    val parallaxX: Float = 0f, // базовый параллакс, умножайте на depth
    val parallaxY: Float = 0f,
    val parallaxBackScale: Float = WindParams().parallaxBack,
    val parallaxFrontScale: Float = WindParams().parallaxFront
)

/** Хук: вернуть актуальный WindState, синхронизированный с VSync и с «порывами». */
@Composable
fun rememberWind(
    params: WindParams = WindParams(),
    intensity: Float = 1f      // 0..1
): WindState {
    val I = intensity.coerceIn(0f, 1f)

    var state by remember { mutableStateOf(WindState()) }

    // внутренняя машина времени
    LaunchedEffect(params, I) {
        if (I <= 0f) {
            state = WindState(
                parallaxBackScale = params.parallaxBack,
                parallaxFrontScale = params.parallaxFront
            )
            return@LaunchedEffect
        }

        var lastNs = 0L
        var phase = 0f

        // envelope порывов: план порыва (targetAmp) и плавный переход к нему
        var env = 0f
        var envTarget = 0f
        var envVel = 0f

        // расписание порывов
        var gustLeftMs = 0L
        var gustDurMs = 0L
        val rnd = Random(System.nanoTime())

        fun scheduleNextGust() {
            gustDurMs = (params.gustPeriodSec.start * 1000f +
                    rnd.nextFloat() * ((params.gustPeriodSec.endInclusive - params.gustPeriodSec.start) * 1000f)).toLong()
            gustLeftMs = gustDurMs
            // цель амплитуды: от 0.3..1.0 (база) до 1.0..gustBoost (порыв) — но учитываем I
            val base = 0.35f + 0.65f * I
            val gust = 1f + (params.gustBoost - 1f) * (0.6f + 0.4f * rnd.nextFloat())
            envTarget = base * gust
        }
        scheduleNextGust()

        while (true) {
            val now = withFrameNanos { it }
            if (lastNs == 0L) { lastNs = now; continue }
            val dtSec = ((now - lastNs) / 1_000_000_000.0).toFloat()
            lastNs = now

            // фаза ветра
            val omega = params.speed * (0.8f + 0.6f * I)
            phase += omega * dtSec * 60f   // нормируем к ~60 FPS по ощущениям

            // обновление порыва (envelope)
            gustLeftMs -= (dtSec * 1000f).toLong()
            if (gustLeftMs <= 0L) {
                scheduleNextGust()
            }
            // критично: плавно «подъезжаем» к целевой амплитуде
            val k = 6f
            envVel += (envTarget - env) * k * dtSec
            envVel *= (1f - 3f * dtSec)    // демпфер
            env += envVel * dtSec

            // итоговая амплитуда
            val amp = env.coerceIn(0.2f, params.gustBoost)

            // готовые значения для UI
            val swayX = sin(phase) * params.swayX * amp
            val swayY = sin(phase * 0.7f + PI.toFloat() * 0.2f) * params.swayY * amp
            val rotZ = sin(phase * 0.9f) * params.rotZDeg * amp

            // параллакс базовый (умножайте на depth в модификаторах)
            val parallaxX = -swayX
            val parallaxY = -swayY * 0.6f

            state = WindState(
                phase = phase,
                amp = amp,
                swayX = swayX,
                swayY = swayY,
                rotZ = rotZ,
                parallaxX = parallaxX,
                parallaxY = parallaxY,
                parallaxBackScale = params.parallaxBack,
                parallaxFrontScale = params.parallaxFront
            )
        }
    }

    return state
}

/* ------------------------------ МОДИФИКАТОРЫ ------------------------------ */

/** Применить ветер к «стеклянной» карточке (сдвиг + лёгкий наклон). */
fun Modifier.windSway(wind: WindState, boost: Float = 1f): Modifier =
    this.graphicsLayer {
        val clamped = boost.coerceIn(0.5f, 2.2f)
        translationX = wind.swayX * clamped
        translationY = wind.swayY * clamped
        rotationZ = wind.rotZ * clamped
    }

/** Параллакс: depth < 0 для задних слоёв (фон), > 0 — для передних. */
fun Modifier.windParallax(wind: WindState, depth: Float): Modifier =
    this.graphicsLayer {
        val scale = if (depth < 0f) wind.parallaxBackScale else wind.parallaxFrontScale
        translationX += wind.parallaxX * scale * depth
        translationY += wind.parallaxY * scale * depth
    }

/** Лёгкая «дрожь» букв/цифр (опционально для заголовков, строк расписания). */
fun Modifier.windJitter(wind: WindState, amplitudePx: Float = 0.6f, seed: Int = 0): Modifier =
    this.graphicsLayer {
        // динамический микродрейф, но без рандома в кадре — от фазы
        val fx = sin(wind.phase * 2.1f + seed * 0.7f)
        val fy = cos(wind.phase * 2.7f + seed * 0.3f)
        translationX += fx * amplitudePx * wind.amp
        translationY += fy * amplitudePx * 0.6f * wind.amp
    }

/** Глобальный провайдер состояния ветра, чтобы и фон, и карточка могли его читать. */
val LocalWind: ProvidableCompositionLocal<WindState?> = staticCompositionLocalOf { null }

/** Обёртка, которая рассчитывает состояние ветра для темы и кладёт его в LocalWind. */
@Composable
fun ProvideWind(
    theme: ThemeSpec,
    intensityOverride: Float? = null,
    content: @Composable () -> Unit
) {
    val intensity = (intensityOverride ?: (theme.defaultIntensity / 100f)).coerceIn(0f, 1f)
    if (intensity <= 0f) {
        CompositionLocalProvider(LocalWind provides null, content = content)
        return
    }
    val wind: WindState? = when (val p = theme.params) {
        is WindParams -> rememberWind(p, intensity)
        is StormParams -> rememberWind(p.wind, intensity)
        else -> null
    }
    CompositionLocalProvider(LocalWind provides wind, content = content)
}
