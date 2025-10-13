package com.example.abys.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB3261E),
    onPrimary = Color.White,
    background = Color(0xFF101010),
    onBackground = Color.White
)

@Composable
fun AbysTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
