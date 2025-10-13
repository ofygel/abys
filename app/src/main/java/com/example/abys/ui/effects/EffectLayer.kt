package com.example.abys.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EffectLayer(
    modifier: Modifier = Modifier,
    theme: ThemeSpec,
    intensityOverride: Float? = null
) {
    val intensity = (intensityOverride ?: (theme.defaultIntensity / 100f)).coerceIn(0.2f, 1f)

    when (theme.effect) {
        EffectKind.LEAVES -> LeavesEffect(modifier, theme.params as LeavesParams, intensity)
        EffectKind.RAIN -> RainEffect(modifier, theme.params as RainParams, intensity)
        EffectKind.SNOW -> SnowEffect(modifier, theme.params as SnowParams, intensity)
        EffectKind.LIGHTNING -> LightningOverlay(modifier, theme.params as LightningParams, intensity)
        EffectKind.WIND -> WindOverlay(modifier, theme.params as WindParams, intensity)
        EffectKind.STORM -> {
            val stormParams = theme.params as StormParams
            RainEffect(modifier, stormParams.rain, intensity)
            WindOverlay(modifier, stormParams.wind, intensity)
            LightningOverlay(modifier, stormParams.lightning, intensity)
        }
        EffectKind.SUNSET_SNOW -> SnowEffect(modifier, theme.params as SnowParams, intensity)
        EffectKind.NIGHT -> StarsEffect(modifier, theme.params as StarsParams, intensity)
    }

}
