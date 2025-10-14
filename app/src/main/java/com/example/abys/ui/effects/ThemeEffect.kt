package com.example.abys.ui.effects

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.abys.R

enum class EffectKind { LEAVES, RAIN, SNOW, LIGHTNING, WIND, STORM, SUNSET_SNOW, NIGHT }

sealed interface EffectParams {
    val kind: EffectKind
}

data class LeavesParams(
    val density: Float,
    val speedY: Float,
    val driftX: Float
) : EffectParams {
    override val kind: EffectKind = EffectKind.LEAVES
}

data class RainParams(
    val dropsCount: Int,
    val speed: Float,
    val angleDeg: Float
) : EffectParams {
    override val kind: EffectKind = EffectKind.RAIN
}

data class SnowParams(
    val flakesCount: Int,
    val speed: Float,
    val driftX: Float,
    val size: ClosedFloatingPointRange<Float>
) : EffectParams {
    override val kind: EffectKind = EffectKind.SNOW
}

data class LightningParams(
    val minDelayMs: Int,
    val maxDelayMs: Int,
    val flashAlpha: Float,
    val flashMs: Int
) : EffectParams {
    override val kind: EffectKind = EffectKind.LIGHTNING
}

data class WindParams(
    val swayX: Float,
    val swayY: Float,
    val speed: Float
) : EffectParams {
    override val kind: EffectKind = EffectKind.WIND
}

data class StormParams(
    val rain: RainParams,
    val wind: WindParams,
    val lightning: LightningParams
) : EffectParams {
    override val kind: EffectKind = EffectKind.STORM
}

data class ThemeSpec(
    val id: String,
    @StringRes val titleRes: Int,
    @DrawableRes val thumbRes: Int,
    @DrawableRes val backgrounds: List<Int>,
    val params: EffectParams,
    val defaultIntensity: Int,
    val supportsWindSway: Boolean,
    val supportsFlash: Boolean
)

val THEMES: List<ThemeSpec> = listOf(
    ThemeSpec(
        id = "leaves",
        titleRes = R.string.theme_leaves,
        thumbRes = R.drawable.thumb_leaves,
        backgrounds = listOf(R.drawable.theme_leaves_bg01),
        params = LeavesParams(
            density = 0.12f,
            speedY = 0.9f,
            driftX = 0.45f
        ),
        defaultIntensity = 65,
        supportsWindSway = false,
        supportsFlash = false
    ),
    ThemeSpec(
        id = "rain",
        titleRes = R.string.theme_rain,
        thumbRes = R.drawable.thumb_rain,
        backgrounds = listOf(R.drawable.theme_rain_bg01),
        params = RainParams(
            dropsCount = 140,
            speed = 14f,
            angleDeg = 18f
        ),
        defaultIntensity = 70,
        supportsWindSway = false,
        supportsFlash = false
    ),
    ThemeSpec(
        id = "snow",
        titleRes = R.string.theme_snow,
        thumbRes = R.drawable.thumb_snow,
        backgrounds = listOf(R.drawable.theme_snow_bg01),
        params = SnowParams(
            flakesCount = 100,
            speed = 1.4f,
            driftX = 0.4f,
            size = 1.5f..3.5f
        ),
        defaultIntensity = 55,
        supportsWindSway = false,
        supportsFlash = false
    ),
    ThemeSpec(
        id = "lightning",
        titleRes = R.string.theme_lightning,
        thumbRes = R.drawable.thumb_lightning,
        backgrounds = listOf(R.drawable.theme_lightning_bg01),
        params = LightningParams(
            minDelayMs = 1500,
            maxDelayMs = 6000,
            flashAlpha = 0.85f,
            flashMs = 80
        ),
        defaultIntensity = 40,
        supportsWindSway = false,
        supportsFlash = true
    ),
    ThemeSpec(
        id = "wind",
        titleRes = R.string.theme_wind,
        thumbRes = R.drawable.thumb_wind,
        backgrounds = listOf(R.drawable.theme_wind_bg01),
        params = WindParams(
            swayX = 8f,
            swayY = 3f,
            speed = 0.05f
        ),
        defaultIntensity = 60,
        supportsWindSway = true,
        supportsFlash = false
    ),
    ThemeSpec(
        id = "storm",
        titleRes = R.string.theme_storm,
        thumbRes = R.drawable.thumb_storm,
        backgrounds = listOf(R.drawable.theme_storm_bg01),
        params = StormParams(
            rain = RainParams(dropsCount = 160, speed = 16f, angleDeg = 20f),
            wind = WindParams(swayX = 12f, swayY = 5f, speed = 0.06f),
            lightning = LightningParams(minDelayMs = 2200, maxDelayMs = 5200, flashAlpha = 0.8f, flashMs = 70)
        ),
        defaultIntensity = 75,
        supportsWindSway = true,
        supportsFlash = true
    ),
    ThemeSpec(
        id = "sunset_snow",
        titleRes = R.string.theme_sunset_snow,
        thumbRes = R.drawable.thumb_sunset_snow,
        backgrounds = listOf(R.drawable.theme_sunset_snow_bg01),
        params = SnowParams(
            flakesCount = 80,
            speed = 0.9f,
            driftX = 0.3f,
            size = 2.0f..4.5f
        ),
        defaultIntensity = 50,
        supportsWindSway = false,
        supportsFlash = false
    ),
    ThemeSpec(
        id = "night",
        titleRes = R.string.theme_night,
        thumbRes = R.drawable.thumb_night,
        backgrounds = listOf(R.drawable.theme_night_bg01),
        params = StarsParams(starsCount = 70, twinklePeriodMs = 1400..2500),
        defaultIntensity = 45,
        supportsWindSway = false,
        supportsFlash = false
    )
)

fun themeById(id: String): ThemeSpec = THEMES.firstOrNull { it.id == id } ?: THEMES.first()
