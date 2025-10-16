package com.example.abys.ui.background

import com.example.abys.R

/**
 * Maps carousel thumbnails to the slideshow image sets.
 * If a theme has no dedicated backgrounds yet, we fall back to the default slide deck.
 */
object ThemeBackgrounds {
    private data class Entry(val thumb: Int, val backgrounds: List<Int>)

    private val entries = listOf(
        Entry(
            thumb = R.drawable.thumb_leaves,
            backgrounds = listOf(R.drawable.slide_01, R.drawable.slide_02)
        ),
        Entry(
            thumb = R.drawable.thumb_lightning,
            backgrounds = listOf(R.drawable.slide_03, R.drawable.slide_07)
        ),
        Entry(
            thumb = R.drawable.thumb_night,
            backgrounds = listOf(R.drawable.slide_04, R.drawable.slide_08)
        ),
        Entry(
            thumb = R.drawable.thumb_rain,
            backgrounds = listOf(R.drawable.slide_05)
        ),
        Entry(
            thumb = R.drawable.thumb_snow,
            backgrounds = listOf(R.drawable.slide_06)
        ),
        Entry(
            thumb = R.drawable.thumb_storm,
            backgrounds = listOf(R.drawable.slide_07, R.drawable.slide_03)
        ),
        Entry(
            thumb = R.drawable.thumb_sunset_snow,
            backgrounds = listOf(R.drawable.slide_08, R.drawable.slide_02)
        ),
        Entry(
            thumb = R.drawable.thumb_wind,
            backgrounds = listOf(R.drawable.slide_01, R.drawable.slide_05)
        )
    )

    val thumbnails: List<Int> = entries.map(Entry::thumb)

    fun backgroundsFor(effectThumb: Int?): List<Int> {
        if (effectThumb == null) return Slides.all
        val backgrounds = entries.firstOrNull { it.thumb == effectThumb }?.backgrounds
        return if (backgrounds.isNullOrEmpty()) Slides.all else backgrounds
    }
}
