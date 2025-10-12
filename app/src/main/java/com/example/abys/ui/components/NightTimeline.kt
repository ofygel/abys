package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.abys.logic.TimeHelper
import java.time.ZoneId

@Composable
fun NightTimeline(maghrib: String, fajr: String, zone: ZoneId) {
    val parts = runCatching { TimeHelper.splitNight(maghrib, fajr, zone) }.getOrNull() ?: return

    Column {
        Text("Ночь (Магриб→Фаджр):", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(48.dp)) {
            listOf(parts.first, parts.second, parts.third).forEachIndexed { idx, p ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${TimeHelper.formatZ(p.first)} — ${TimeHelper.formatZ(p.second)}")
                        Text(listOf("I", "II", "III")[idx], color = MaterialTheme.colorScheme.secondary)
                    }
                }
                if (idx < 2) Spacer(Modifier.width(6.dp))
            }
        }
    }
}
