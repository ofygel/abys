package com.example.abys.ui.effects

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EffectLayer(
    modifier: Modifier = Modifier,
    theme: ThemeSpec,
    intensityOverride: Float? = null
) {
    Crossfade(targetState = theme, label = "effect-crossfade") { spec ->
        val intensity = (intensityOverride ?: (spec.defaultIntensity / 100f)).coerceIn(0f, 1f)

        when (val params = spec.params) {
            is LeavesParams -> LeavesEffect(modifier, params, intensity)
            is RainParams -> RainEffect(modifier, params, intensity)
            is SnowParams -> SnowEffect(modifier, params, intensity)
            is LightningParams -> LightningOverlay(modifier, params, intensity)
            is WindParams -> Unit
            is StarsParams -> StarsEffect(modifier, params, intensity)
            is StormParams -> {
                RainEffect(modifier, params.rain, intensity)
                LightningOverlay(modifier, params.lightning, intensity)
            }
        }
    }
}
