package com.example.abys.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.Month

/**
 * Simple helper that picks a particle theme based on the current month.
 * It keeps the existing effect system so MainActivity can show something
 * even when no explicit theme selection was made yet.
 */
@Composable
fun SeasonalParticles(
    modifier: Modifier = Modifier,
    month: Month = LocalDate.now().month,
    intensityOverride: Float? = null
) {
    val theme = remember(month) { month.toThemeSpec() }
    EffectLayer(
        modifier = modifier,
        theme = theme,
        intensityOverride = intensityOverride
    )
}

private fun Month.toThemeSpec(): ThemeSpec = when (this) {
    Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> themeById("snow")
    Month.MARCH, Month.APRIL -> themeById("rain")
    Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST -> themeById("storm")
    Month.SEPTEMBER, Month.OCTOBER -> themeById("leaves")
    Month.NOVEMBER -> themeById("night")
}

