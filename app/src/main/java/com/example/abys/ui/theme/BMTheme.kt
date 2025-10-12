package com.example.abys.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.abys.R

// ----------  Цвета  ----------
private val bmColors = darkColorScheme(
    primary          = Color(0xFF6E0000),   // бордовый
    onPrimary        = Color.White,
    surface          = Color(0xFF0B0B0C),
    onSurface        = Color(0xFFE6E6E6),
    surfaceVariant   = Color(0xFF262629),
    outline          = Color(0xFF444444),
    secondary        = Color(0xFF007070),   // бирюзовый
    onSecondary      = Color.White
)

// ----------  Шрифты  ----------
private val blackLetter = FontFamily(
    Font(R.font.unifrakturmaguntia, weight = FontWeight.Bold)
)

private val bmTypography = Typography(
    displayLarge = TextStyle(fontFamily = blackLetter, fontSize = 32.sp, lineHeight = 36.sp),
    titleMedium  = TextStyle(fontFamily = blackLetter, fontSize = 18.sp),
    bodyLarge    = TextStyle(fontSize = 16.sp)
)

// ----------  Тема  ----------
@Composable
fun BMTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = bmColors,
        typography  = bmTypography,
        content     = content
    )
}
