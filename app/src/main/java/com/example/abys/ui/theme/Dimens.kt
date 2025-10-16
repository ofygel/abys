package com.example.abys.ui.theme

import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Масштаб из спека: sx = w/636, sy = h/1131.
 * Радиусы умножаем на s = (sx+sy)/2.
 */
object Dimens {
    @Composable
    fun sx(): Float = LocalConfiguration.current.screenWidthDp / 636f

    @Composable
    fun sy(): Float = LocalConfiguration.current.screenHeightDp / 1131f

    @Composable
    fun s(): Float = (sx() + sy()) * 0.5f

    @Composable
    fun scaled(px: Int) = (px * s()).dp

    @Composable
    fun scaledX(@DimenRes resId: Int): Dp {
        val base = dimensionResource(resId)
        return (base.value * sx()).dp
    }

    @Composable
    fun scaledY(@DimenRes resId: Int): Dp {
        val base = dimensionResource(resId)
        return (base.value * sy()).dp
    }

    @Composable
    fun scaledRadius(@DimenRes resId: Int): Dp {
        val base = dimensionResource(resId)
        return (base.value * s()).dp
    }
}
