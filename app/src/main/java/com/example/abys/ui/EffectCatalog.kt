package com.example.abys.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.abys.R
import com.example.abys.data.EffectId

private data class EffectAssets(
    val id: EffectId,
    val thumbnailName: String,
    val fallbackThumbnail: Int,
)

private val effectAssets = listOf(
    EffectAssets(
        id = EffectId.leaves,
        thumbnailName = "thumb_leaves",
        fallbackThumbnail = R.drawable.slide_01,
    ),
    EffectAssets(
        id = EffectId.lightning,
        thumbnailName = "thumb_lightning",
        fallbackThumbnail = R.drawable.slide_02,
    ),
    EffectAssets(
        id = EffectId.night,
        thumbnailName = "thumb_night",
        fallbackThumbnail = R.drawable.slide_03,
    ),
    EffectAssets(
        id = EffectId.rain,
        thumbnailName = "thumb_rain",
        fallbackThumbnail = R.drawable.slide_04,
    ),
    EffectAssets(
        id = EffectId.snow,
        thumbnailName = "thumb_snow",
        fallbackThumbnail = R.drawable.slide_05,
    ),
    EffectAssets(
        id = EffectId.storm,
        thumbnailName = "thumb_storm",
        fallbackThumbnail = R.drawable.slide_06,
    ),
    EffectAssets(
        id = EffectId.sunset_snow,
        thumbnailName = "thumb_sunset_snow",
        fallbackThumbnail = R.drawable.slide_07,
    ),
    EffectAssets(
        id = EffectId.wind,
        thumbnailName = "thumb_wind",
        fallbackThumbnail = R.drawable.slide_08,
    ),
)

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

private fun EffectAssets.resolveThumbnail(context: Context): Int {
    return resolveDrawableId(context, thumbnailName) ?: fallbackThumbnail
}

private fun resolveDrawableId(context: Context, name: String): Int? {
    if (name.isBlank()) return null
    val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    return id.takeIf { it != 0 }
}