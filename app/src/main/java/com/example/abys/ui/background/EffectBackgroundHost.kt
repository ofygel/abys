package com.example.abys.ui.background

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.abys.data.EffectId

@Composable
fun BackgroundHost(effect: EffectId) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val images = remember(effect, configuration) {
        resolveEffectSlides(context, effect)
    }
    SlideshowBackground(images = images)
}

@SuppressLint("DiscouragedApi")
private fun resolveEffectSlides(context: Context, effect: EffectId): List<Int> {
    val prefix = "theme_${effect.name}_bg"
    val packageName = context.packageName
    val resources = context.resources
    val ids = buildList {
        for (index in 1..4) {
            val name = "%s%02d".format(prefix, index)
            val id = resources.getIdentifier(name, "drawable", packageName)
            if (id != 0) add(id)
        }
    }
    return ids.ifEmpty { Slides.all }
}