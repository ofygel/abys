package com.example.abys.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Tokens {
    object Colors {
        val text          = Color(0xFFFFFFFF)
        val overlayTop    = Color(0x73000000)   // 45 %
        val overlayCard   = Color(0x59000000)   // 35 %
        val glassSheet    = Color.White.copy(alpha = 0.14f)
        val glassPicker   = Color.White.copy(alpha = 0.26f)
        val tickDark      = Color(0xD9000000)   // 85 %
    }
    object Radii {
        private const val pillPx  = 22f
        private const val cardPx  = 19f
        private const val chipPx  = 22f
        private const val glassPx = 28f

        @Composable
        fun pill() = (pillPx * Dimens.s()).dp

        @Composable
        fun card() = (cardPx * Dimens.s()).dp

        @Composable
        fun chip() = (chipPx * Dimens.s()).dp

        @Composable
        fun glass() = (glassPx * Dimens.s()).dp
    }
    object TypographyPx {      // px-значения из спека (если нужен расчёт площади)
        const val city      = 57
        const val timeNow   = 59
        const val label     = 43
        const val subLabel  = 41
        const val timeline  = 38
    }
    // Для Compose сразу конвертируем в sp
    object TypographySp {
        val city     = TypographyPx.city.sp
        val timeNow  = TypographyPx.timeNow.sp
        val label    = TypographyPx.label.sp
        val subLabel = TypographyPx.subLabel.sp
        val timeline = TypographyPx.timeline.sp
    }
}
