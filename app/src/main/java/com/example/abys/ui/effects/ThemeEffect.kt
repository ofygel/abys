package com.example.abys.ui.effects

import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.example.abys.R
import kotlin.ranges.IntRange as KotlinIntRange

/** Типы эффектов, которые умеет рендерить EffectLayer */
enum class EffectKind { LEAVES, RAIN, SNOW, LIGHTNING, WIND, STORM, SUNSET_SNOW, NIGHT }

/** Базовый «контракт» параметров эффекта */
@Stable
sealed interface EffectParams { val kind: EffectKind }

/* ----------------------------- ПАРАМЕТРЫ ЭФФЕКТОВ ----------------------------- */

data class LeavesParams(
    /** относительная плотность (частиц/площадь), 0.05..0.25 */
    val density: Float = 0.12f,
    /** вертикальная скорость базовая */
    val speedY: Float = 0.9f,
    /** амплитуда горизонтального дрейфа */
    val driftX: Float = 0.45f
) : EffectParams { override val kind = EffectKind.LEAVES }

data class RainParams(
    /** целевое количество капель на эталонной площади (1080x1920) */
    val dropsCount: Int = 140,
    /** базовая скорость падения (пикс/кадр нормированно) */
    val speed: Float = 14f,
    /** угол наклона струек в градусах */
    val angleDeg: Float = 18f
) : EffectParams { override val kind = EffectKind.RAIN }

data class SnowParams(
    /** целевое количество хлопьев на эталонной площади */
    val flakesCount: Int = 100,
    /** базовая скорость падения */
    val speed: Float = 1.4f,
    /** амплитуда дрейфа по X */
    val driftX: Float = 0.4f,
    /** диапазон радиусов (px) */
    val size: ClosedFloatingPointRange<Float> = 1.5f..3.5f
) : EffectParams { override val kind = EffectKind.SNOW }

data class LightningParams(
    /** min задержка между вспышками (мс) */
    val minDelayMs: Int = 1500,
    /** max задержка между вспышками (мс) */
    val maxDelayMs: Int = 6000,
    /** прозрачность вспышки 0..1 */
    val flashAlpha: Float = 0.85f,
    /** длительность основной вспышки (мс), дальше — хвост */
    val flashMs: Int = 80
) : EffectParams { override val kind = EffectKind.LIGHTNING }

/** Параметры ветра используются рендерером и модификаторами (см. Wind.kt) */
data class WindParams(
    val speed: Float = 0.06f,
    val swayX: Float = 10f,
    val swayY: Float = 4f,
    val rotZDeg: Float = 0.45f,
    val parallaxBack: Float = 0.25f,
    val parallaxFront: Float = 0.10f,
    val gustBoost: Float = 1.8f,
    val gustPeriodSec: ClosedFloatingPointRange<Float> = 3f..7f
) : EffectParams { override val kind = EffectKind.WIND }

/** Ночное небо: звезды + мерцание */
data class StarsParams(
    val starsCount: Int = 70,
    /** период мерцания одной звезды (мс), выбирается случайно в этом диапазоне */
    val twinklePeriodMs: KotlinIntRange = 1400..2500
) : EffectParams { override val kind = EffectKind.NIGHT }

/**
 * Композит «шторм»: дождь + ветер + молнии.
 * [rain] управляет плотностью/углом капель, [wind] — свингом фона/карточки, [lightning] — огибающей вспышек.
 */
data class StormParams(
    val rain: RainParams = RainParams(dropsCount = 160, speed = 16f, angleDeg = 20f),
    val wind: WindParams = WindParams(
        speed = 0.07f, swayX = 14f, swayY = 6f, rotZDeg = 0.65f,
        parallaxBack = 0.30f, parallaxFront = 0.15f, gustBoost = 2.2f, gustPeriodSec = 3f..6.5f
    ),
    val lightning: LightningParams = LightningParams(minDelayMs = 2200, maxDelayMs = 5200, flashAlpha = 0.8f, flashMs = 70)
) : EffectParams { override val kind = EffectKind.STORM }

/* --------------------------------- ТЕМА --------------------------------- */

data class ThemeSpec(
    val id: String,
    @StringRes val titleRes: Int,
    @DrawableRes val thumbRes: Int,
    /** Пак фонов для слайдера. IntArray — чтобы @DrawableRes работал на всём массиве. */
    @DrawableRes val backgrounds: IntArray,
    val params: EffectParams,
    /** 0..100 */
    @IntRange(from = 0, to = 100) val defaultIntensity: Int = 60,
    /** Хард-флаг (ручной оверрайд), если нужно принудительно включить кач карточки */
    val supportsWindSway: Boolean = false,
    /** Хард-флаг для визуальных «вспышек» поверх */
    val supportsFlash: Boolean = false
) {
    /** Нормализованная интенсивность 0f..1f */
    val intensityF: Float get() = (defaultIntensity.coerceIn(0, 100)) / 100f

    /** Эффективные флаги, учитывающие тип params (шторм/ветер/молния) */
    val supportsWindSwayEffective: Boolean
        get() = supportsWindSway || params is StormParams || params.kind == EffectKind.WIND

    val supportsFlashEffective: Boolean
        get() = supportsFlash || params is StormParams || params.kind == EffectKind.LIGHTNING
}

/* ------------------------------- РЕЕСТР ТЕМ ------------------------------- */

val THEMES: List<ThemeSpec> = listOf(
    ThemeSpec(
        id = "leaves",
        titleRes = R.string.theme_leaves,
        thumbRes = R.drawable.thumb_leaves,
        backgrounds = intArrayOf(R.drawable.theme_leaves_bg01),
        params = LeavesParams(density = 0.12f, speedY = 0.9f, driftX = 0.45f),
        defaultIntensity = 65,
        supportsWindSway = false,
        supportsFlash = false
    ),
    ThemeSpec(
        id = "rain",
        titleRes = R.string.theme_rain,
        thumbRes = R.drawable.thumb_rain,
        backgrounds = intArrayOf(R.drawable.theme_rain_bg01),
        params = RainParams(dropsCount = 140, speed = 14f, angleDeg = 18f),
        defaultIntensity = 70
    ),
    ThemeSpec(
        id = "snow",
        titleRes = R.string.theme_snow,
        thumbRes = R.drawable.thumb_snow,
        backgrounds = intArrayOf(R.drawable.theme_snow_bg01),
        params = SnowParams(flakesCount = 100, speed = 1.4f, driftX = 0.4f, size = 1.5f..3.5f),
        defaultIntensity = 55
    ),
    ThemeSpec(
        id = "lightning",
        titleRes = R.string.theme_lightning,
        thumbRes = R.drawable.thumb_lightning,
        backgrounds = intArrayOf(R.drawable.theme_lightning_bg01),
        params = LightningParams(minDelayMs = 1500, maxDelayMs = 6000, flashAlpha = 0.85f, flashMs = 80),
        defaultIntensity = 40,
        supportsFlash = true
    ),
    ThemeSpec(
        id = "wind",
        titleRes = R.string.theme_wind,
        thumbRes = R.drawable.thumb_wind,
        backgrounds = intArrayOf(R.drawable.theme_wind_bg01),
        params = WindParams(
            speed = 0.065f, swayX = 12f, swayY = 5f, rotZDeg = 0.5f,
            parallaxBack = 0.25f, parallaxFront = 0.12f, gustBoost = 2.0f, gustPeriodSec = 3.5f..7.5f
        ),
        defaultIntensity = 60,
        supportsWindSway = true
    ),
    ThemeSpec(
        id = "storm",
        titleRes = R.string.theme_storm,
        thumbRes = R.drawable.thumb_storm,
        backgrounds = intArrayOf(R.drawable.theme_storm_bg01),
        params = StormParams(), // см. дефолты выше
        defaultIntensity = 75,
        supportsWindSway = true,
        supportsFlash = true
    ),
    ThemeSpec(
        id = "sunset_snow",
        titleRes = R.string.theme_sunset_snow,
        thumbRes = R.drawable.thumb_sunset_snow,
        backgrounds = intArrayOf(R.drawable.theme_sunset_snow_bg01),
        params = SnowParams(flakesCount = 80, speed = 0.9f, driftX = 0.3f, size = 2.0f..4.5f),
        defaultIntensity = 50
    ),
    ThemeSpec(
        id = "night",
        titleRes = R.string.theme_night,
        thumbRes = R.drawable.thumb_night,
        backgrounds = intArrayOf(R.drawable.theme_night_bg01),
        params = StarsParams(starsCount = 70, twinklePeriodMs = 1400..2500),
        defaultIntensity = 45
    )
)

private val THEMES_BY_ID: Map<String, ThemeSpec> = THEMES.associateBy { it.id }

/** Безопасный доступ по id с фолбэком на первую тему */
fun themeById(id: String): ThemeSpec = THEMES_BY_ID[id] ?: THEMES.first()
