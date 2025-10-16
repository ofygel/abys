package com.example.abys.ui.background

import androidx.compose.runtime.Composable
import com.example.abys.data.EffectId
import com.example.abys.ui.rememberEffectBackgrounds

@Composable
fun BackgroundHost(effect: EffectId) {
    val backgrounds = rememberEffectBackgrounds(effect)
    SlideshowBackground(images = backgrounds)
}
