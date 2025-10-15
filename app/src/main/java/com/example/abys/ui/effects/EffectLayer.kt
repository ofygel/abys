package com.example.abys.ui.effects

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

@Composable
fun EffectLayer(
    modifier: Modifier = Modifier,
    theme: ThemeSpec,
    intensityOverride: Float? = null
) {
    val intensity = (intensityOverride ?: (theme.defaultIntensity / 100f)).coerceIn(0f, 1f)
    if (intensity <= 0f) return

    AnimatedContent(
        targetState = theme.params.kind,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
        label = "effect-kind"
    ) { kind ->
        key(kind) {
            when (kind) {
                EffectKind.LEAVES -> {
                    val p = theme.params as LeavesParams
                    LeavesEffect(modifier, p, intensity)
                }
                EffectKind.RAIN -> {
                    val p = theme.params as RainParams
                    RainEffect(modifier, p, intensity)
                }
                EffectKind.SNOW, EffectKind.SUNSET_SNOW -> {
                    val p = theme.params as SnowParams
                    SnowEffect(modifier, p, intensity)
                }
                EffectKind.LIGHTNING -> {
                    val p = theme.params as LightningParams
                    LightningOverlay(modifier, p, intensity)
                }
                EffectKind.WIND -> {
                    // ветер применяется к UI через LocalWind
                }
                EffectKind.NIGHT -> {
                    val p = theme.params as StarsParams
                    StarsEffect(modifier, p, intensity)
                }
                EffectKind.STORM -> {
                    val p = theme.params as StormParams
                    RainEffect(modifier, p.rain, intensity)
                    LightningOverlay(modifier, p.lightning, intensity)
                }
            }
        }
    }
}
