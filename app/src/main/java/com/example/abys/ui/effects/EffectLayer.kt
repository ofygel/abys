package com.example.abys.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EffectLayer(
    modifier: Modifier = Modifier,
    kind: EffectKind
) {
    when (kind) {
        EffectKind.LEAVES -> LeavesEffect(modifier, density = 0.12f)   // редкое, хаотичное
        EffectKind.RAIN -> RainEffect(modifier)
        EffectKind.SNOW -> SnowEffect(modifier)
        EffectKind.LIGHTNING -> LightningOverlay(modifier)
        EffectKind.WIND -> WindOverlay(modifier, strength = 8f)
        EffectKind.STORM -> {
            // комбо: дождь + ветер + случайные вспышки
            RainEffect(modifier)
            WindOverlay(modifier, strength = 12f)
            LightningOverlay(modifier)
        }
    }
}
