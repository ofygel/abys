package com.example.abys.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.abys.R

object AbysFonts {
    val inter: FontFamily = FontFamily(
        Font(resId = R.font.inter_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(resId = R.font.inter_semibold, weight = FontWeight.SemiBold, style = FontStyle.Normal),
        Font(resId = R.font.inter_bold, weight = FontWeight.Bold, style = FontStyle.Normal),
        Font(resId = R.font.inter_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(resId = R.font.inter_bold_italic, weight = FontWeight.Bold, style = FontStyle.Italic)
    )
}
