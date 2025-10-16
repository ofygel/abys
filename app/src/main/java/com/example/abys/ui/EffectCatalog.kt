package com.example.abys.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.abys.R
import com.example.abys.data.EffectId
import com.example.abys.ui.background.Slides

private val effectCatalog = listOf(
    EffectThumb(EffectId.leaves, R.drawable.slide_01),
    EffectThumb(EffectId.lightning, R.drawable.slide_02),
    EffectThumb(EffectId.night, R.drawable.slide_03),
    EffectThumb(EffectId.rain, R.drawable.slide_04),
    EffectThumb(EffectId.snow, R.drawable.slide_05),
    EffectThumb(EffectId.storm, R.drawable.slide_06),
    EffectThumb(EffectId.sunset_snow, R.drawable.slide_07),
    EffectThumb(EffectId.wind, R.drawable.slide_08),
)

private val effectBackgrounds = mapOf(
    EffectId.leaves to listOf(R.drawable.slide_01),
    EffectId.lightning to listOf(R.drawable.slide_02),
    EffectId.night to listOf(R.drawable.slide_03),
    EffectId.rain to listOf(R.drawable.slide_04),
    EffectId.snow to listOf(R.drawable.slide_05),
    EffectId.storm to listOf(R.drawable.slide_06),
    EffectId.sunset_snow to listOf(R.drawable.slide_07),
    EffectId.wind to listOf(R.drawable.slide_08),
)

@Composable
fun rememberEffectCatalogFromRes(): List<EffectThumb> = remember { effectCatalog }

@Composable
fun rememberEffectBackgrounds(effect: EffectId): List<Int> =
    remember(effect) { effectBackgrounds[effect] ?: Slides.all }

@Composable
fun rememberCitiesFromRes(): List<String> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        context.resources.getStringArray(R.array.abys_cities_kz).toList()
    }
}
