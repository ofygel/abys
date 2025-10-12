package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.abys.logic.TimeHelper
import java.time.ZoneId

@Composable
fun DayProgress(
    fajr: String, sunrise: String, dhuhr: String, asr: String, maghrib: String, isha: String,
    zone: ZoneId
) {
    val times = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
        .mapNotNull { TimeHelper.parseHHmmLocal(it, zone) }
    if (times.size < 6) return

    val start = times.first()           // Fajr
    val end = times.last()              // Isha
    val now = TimeHelper.now(zone)
    val totalMin = (end.hour*60+end.minute) - (start.hour*60+start.minute)
    val passedMin = ((now.hour*60+now.minute) - (start.hour*60+start.minute)).coerceIn(0, totalMin)

    Column(Modifier.fillMaxWidth()) {
        Text("Прогресс дня", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            val frac = if (totalMin > 0) passedMin.toFloat()/totalMin else 0f
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f))
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Fajr","Sun","Dhuhr","Asr","Maghrib","Isha").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
