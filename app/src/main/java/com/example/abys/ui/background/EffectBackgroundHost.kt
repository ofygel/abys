package com.example.abys.ui.background

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.abys.data.EffectId

@Composable
fun BackgroundHost(effect: EffectId) {
    val backgrounds = remember(effect) { ThemeBackgrounds.backgroundsFor(effect) }
    SlideshowBackground(images = backgrounds)
}
