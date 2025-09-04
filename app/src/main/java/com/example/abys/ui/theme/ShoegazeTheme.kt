package com.example.abys.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ShoegazeDark = darkColorScheme(
    primary = Color(0xFF9AD8D6),
    secondary = Color(0xFFF39A6B),
    background = Color(0xFF0F1416),
    surface = Color(0xFF12191B),
    onBackground = Color(0xFFECE7DE),
)

@Composable
fun ShoegazeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShoegazeDark,
        content = content
    )
}
