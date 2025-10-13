package com.example.abys.ui.effects

import androidx.annotation.DrawableRes
import com.example.abys.R

enum class EffectKind { LEAVES, RAIN, SNOW, LIGHTNING, WIND, STORM }

data class EffectSpec(
    val kind: EffectKind,
    val title: String,
    @DrawableRes val preview: Int
)

val EFFECTS: List<EffectSpec> = listOf(
    EffectSpec(EffectKind.LEAVES,    "Листопад", R.drawable.slide_01),
    EffectSpec(EffectKind.RAIN,      "Дождь",    R.drawable.slide_02),
    EffectSpec(EffectKind.SNOW,      "Снег",     R.drawable.slide_03),
    EffectSpec(EffectKind.LIGHTNING, "Гроза",    R.drawable.slide_04),
    EffectSpec(EffectKind.WIND,      "Ветер",    R.drawable.slide_05),
    EffectSpec(EffectKind.STORM,     "Шторм",    R.drawable.slide_06)
)
