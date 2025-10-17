package com.example.abys.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.abys.R
import com.example.abys.data.EffectId
import com.example.abys.ui.background.Slides

private data class EffectAssets(
    val id: EffectId,
    val thumbnailName: String,
    val backgroundNames: List<String>,
    val fallbackThumbnail: Int,
    val fallbackBackgrounds: List<Int>,
)

private val effectAssets = listOf(
    EffectAssets(
        id = EffectId.leaves,
        thumbnailName = "thumb_leaves",
        backgroundNames = themeBackgrounds("theme_leaves"),
        fallbackThumbnail = R.drawable.slide_01,
        fallbackBackgrounds = listOf(R.drawable.slide_01),
    ),
    EffectAssets(
        id = EffectId.lightning,
        thumbnailName = "thumb_lightning",
        backgroundNames = themeBackgrounds("theme_lightning"),
        fallbackThumbnail = R.drawable.slide_02,
        fallbackBackgrounds = listOf(R.drawable.slide_02),
    ),
    EffectAssets(
        id = EffectId.night,
        thumbnailName = "thumb_night",
        backgroundNames = themeBackgrounds("theme_night"),
        fallbackThumbnail = R.drawable.slide_03,
        fallbackBackgrounds = listOf(R.drawable.slide_03),
    ),
    EffectAssets(
        id = EffectId.rain,
        thumbnailName = "thumb_rain",
        backgroundNames = themeBackgrounds("theme_rain"),
        fallbackThumbnail = R.drawable.slide_04,
        fallbackBackgrounds = listOf(R.drawable.slide_04),
    ),
    EffectAssets(
        id = EffectId.snow,
        thumbnailName = "thumb_snow",
        backgroundNames = themeBackgrounds("theme_snow"),
        fallbackThumbnail = R.drawable.slide_05,
        fallbackBackgrounds = listOf(R.drawable.slide_05),
    ),
    EffectAssets(
        id = EffectId.storm,
        thumbnailName = "thumb_storm",
        backgroundNames = themeBackgrounds("theme_storm"),
        fallbackThumbnail = R.drawable.slide_06,
        fallbackBackgrounds = listOf(R.drawable.slide_06),
    ),
    EffectAssets(
        id = EffectId.sunset_snow,
        thumbnailName = "thumb_sunset_snow",
        backgroundNames = themeBackgrounds("theme_sunset_snow"),
        fallbackThumbnail = R.drawable.slide_07,
        fallbackBackgrounds = listOf(R.drawable.slide_07),
    ),
    EffectAssets(
        id = EffectId.wind,
        thumbnailName = "thumb_wind",
        backgroundNames = themeBackgrounds("theme_wind"),
        fallbackThumbnail = R.drawable.slide_08,
        fallbackBackgrounds = listOf(R.drawable.slide_08),
    ),
)

private val effectAssetsById = effectAssets.associateBy { it.id }

@Composable
fun rememberEffectCatalogFromRes(): List<EffectThumb> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) {
        effectAssets.map { assets ->
            EffectThumb(assets.id, assets.resolveThumbnail(context))
        }
    }
}

@Composable
fun rememberEffectBackgrounds(effect: EffectId): List<Int> {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(effect, context, configuration) {
        effectAssetsById[effect]?.resolveBackgrounds(context) ?: Slides.all
    }
}

private fun EffectAssets.resolveThumbnail(context: Context): Int {
    return resolveDrawableId(context, thumbnailName) ?: fallbackThumbnail
}

private fun EffectAssets.resolveBackgrounds(context: Context): List<Int> {
    val resolved = backgroundNames.mapNotNull { name -> resolveDrawableId(context, name) }
    return if (resolved.isNotEmpty()) resolved else fallbackBackgrounds
}

private fun themeBackgrounds(prefix: String): List<String> =
    (1..4).map { index -> "${prefix}_bg${index.toString().padStart(2, '0')}" }

private fun resolveDrawableId(context: Context, name: String): Int? {
    if (name.isBlank()) return null
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return id.takeIf { it != 0 }
}

