package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.abys.TimingsUi
import com.example.abys.util.PrayerUtils
import kotlinx.coroutines.delay

@Composable
fun NextPrayerChip(t: TimingsUi) {
    var label by remember { mutableStateOf("--") }
    var left by remember { mutableStateOf("--:--:--") }

    LaunchedEffect(t) {
        while (true) {
            PrayerUtils.nextPrayer(t)?.let { n ->
                label = n.name
                left = PrayerUtils.formatDur(n.inDur)
            }
            delay(1_000L)
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text("До $label:  ")
        Text(left)
    }
}
