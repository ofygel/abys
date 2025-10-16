package com.example.abys.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.use
import com.example.abys.R
import com.example.abys.data.EffectId
import com.example.abys.ui.background.Slides

@Composable
fun rememberEffectCatalogFromRes(): List<EffectThumb> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val resources = context.resources
        val ids = resources.getStringArray(R.array.abys_effect_ids)
        resources.obtainTypedArray(R.array.abys_effect_thumbs).use { thumbs ->
            buildList {
                for (index in ids.indices) {
                    val id = runCatching { EffectId.valueOf(ids[index]) }.getOrNull() ?: continue
                    val thumbRes = thumbs.getResourceId(index, 0)
                    if (thumbRes != 0) {
                        add(EffectThumb(id, thumbRes))
                    }
                }
            }
        }
    }
}

@Composable
fun rememberEffectBackgrounds(effect: EffectId): List<Int> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(effect, configuration) {
        val resources = context.resources
        val ids = resources.getStringArray(R.array.abys_effect_ids)
        val index = ids.indexOf(effect.name)
        val backgrounds = resources.obtainTypedArray(R.array.abys_effect_backgrounds)
        val result = backgrounds.use { array ->
            if (index in 0 until array.length()) {
                val resId = array.getResourceId(index, 0)
                if (resId != 0) listOf(resId) else emptyList()
            } else {
                emptyList()
            }
        }
        if (result.isEmpty()) Slides.all else result
    }
}

@Composable
fun rememberCitiesFromRes(): List<String> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        context.resources.getStringArray(R.array.abys_cities_kz).toList()
    }
}
