package com.example.abys.ui.background

import com.example.abys.R
import com.example.abys.data.EffectId
import com.example.abys.ui.EffectThumb

/**
 * Maps carousel thumbnails to the slideshow image sets.
 * If a theme has no dedicated backgrounds yet, we fall back to the default slide deck.
 */
object ThemeBackgrounds {
    private data class Entry(
        val id: EffectId,
        val thumb: Int,
        val backgrounds: List<Int>
    )

    private val entries = listOf(
        Entry(
            id = EffectId.leaves,
            thumb = R.drawable.thumb_leaves,
            backgrounds = listOf(R.drawable.slide_01, R.drawable.slide_02)
        ),
        Entry(
            id = EffectId.lightning,
            thumb = R.drawable.thumb_lightning,
            backgrounds = listOf(R.drawable.slide_03, R.drawable.slide_07)
        ),
        Entry(
            id = EffectId.night,
            thumb = R.drawable.thumb_night,
            backgrounds = listOf(R.drawable.slide_04, R.drawable.slide_08)
        ),
        Entry(
            id = EffectId.rain,
            thumb = R.drawable.thumb_rain,
            backgrounds = listOf(R.drawable.slide_05)
        ),
        Entry(
            id = EffectId.snow,
            thumb = R.drawable.thumb_snow,
            backgrounds = listOf(R.drawable.slide_06)
        ),
        Entry(
            id = EffectId.storm,
            thumb = R.drawable.thumb_storm,
            backgrounds = listOf(R.drawable.slide_07, R.drawable.slide_03)
        ),
        Entry(
            id = EffectId.sunset_snow,
            thumb = R.drawable.thumb_sunset_snow,
            backgrounds = listOf(R.drawable.slide_08, R.drawable.slide_02)
        ),
        Entry(
            id = EffectId.wind,
            thumb = R.drawable.thumb_wind,
            backgrounds = listOf(R.drawable.slide_01, R.drawable.slide_05)
        )
    )

    val thumbnails: List<EffectThumb> = entries.map { entry ->
        EffectThumb(entry.id, entry.thumb)
    }

    fun backgroundsFor(effect: EffectId): List<Int> {
        val backgrounds = entries.firstOrNull { it.id == effect }?.backgrounds
        return if (backgrounds.isNullOrEmpty()) Slides.all else backgrounds
    }
}
