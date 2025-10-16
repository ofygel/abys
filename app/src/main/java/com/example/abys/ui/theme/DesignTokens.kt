package com.example.abys.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import com.example.abys.R

object Tokens {
    object Colors {
        val text: Color
            @Composable get() = colorResource(R.color.abys_fg)
        val overlayTop: Color
            @Composable get() = colorResource(R.color.abys_overlayTop)
        val overlayCard: Color
            @Composable get() = colorResource(R.color.abys_overlayCard)
        val separator: Color
            @Composable get() = colorResource(R.color.abys_separator50)
        val chipStroke: Color
            @Composable get() = colorResource(R.color.abys_chipStroke)
        val tickFull: Color
            @Composable get() = colorResource(R.color.abys_tickFull)
        val tickDark: Color
            @Composable get() = colorResource(R.color.abys_quoteStroke)
        val glassSheetBlur: Color
            @Composable get() = colorResource(R.color.abys_glassSheetBlur)
        val glassPickerBlur: Color
            @Composable get() = colorResource(R.color.abys_glassPickerBlur)
        val glassSheetOpaque: Color
            @Composable get() = colorResource(R.color.abys_glassSheetOpaque)
        val glassPickerOpaque: Color
            @Composable get() = colorResource(R.color.abys_glassPickerOpaque)
    }

    object Radii {
        @Composable
        fun pill() = Dimens.scaledRadius(R.dimen.abys_radius_pill)

        @Composable
        fun card() = Dimens.scaledRadius(R.dimen.abys_radius_card)

        @Composable
        fun chip() = Dimens.scaledRadius(R.dimen.abys_radius_chip)

        @Composable
        fun glass() = Dimens.scaledRadius(R.dimen.abys_radius_list)
    }

    object TypographyPx {
        const val city = 57
        const val timeNow = 59
        const val label = 43
        const val subLabel = 41
        const val timeline = 38
    }

    object TypographySp {
        val city = TypographyPx.city.sp
        val timeNow = TypographyPx.timeNow.sp
        val label = TypographyPx.label.sp
        val subLabel = TypographyPx.subLabel.sp
        val timeline = TypographyPx.timeline.sp
    }
}
