package com.example.abys.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
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
}
