package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopOverlay(city: String?, hijri: String?) {
    Row(
        Modifier
            .wrapContentWidth()
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            (city ?: "—"),
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(
            hijri ?: "—",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp
        )
    }
}
