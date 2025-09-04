package com.example.abys.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ShoegazeDark = darkColorScheme(
    primary = Color(0xFF9AD8D6),   // холодный циан
    secondary = Color(0xFFF39A6B), // тёплый персик
    background = Color(0xFF0F1416), // тёмный с зелёным подтоном
    surface = Color(0xFF12191B),
    onPrimary = Color(0xFF0E1011),
    onBackground = Color(0xFFECE7DE)
)

@Composable
fun ShoegazeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShoegazeDark,
        typography = MaterialTheme.typography,
        content = content
    )
}
