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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.abys.logic.TimeHelper
import java.time.ZoneId
import java.time.ZonedDateTime
import com.example.abys.R

@Composable
fun NightTimeline(maghrib: String, fajr: String, zone: ZoneId) {
    val parts = runCatching { TimeHelper.splitNight(maghrib, fajr, zone) }.getOrNull() ?: return
    val now = ZonedDateTime.now(zone)
    val activeIdx = when {
        now.isBefore(parts.first.second) -> 0
        now.isBefore(parts.second.second) -> 1
        else -> 2
    }

    val activeFill = colorResource(R.color.bm_accent)
    val inactiveFill = Color.White.copy(alpha = 0.12f)
    val textColor = colorResource(R.color.bm_text)

    Column {
        Text(
            "Ночь (Магриб→Фаджр):",
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(48.dp)) {
            listOf(parts.first, parts.second, parts.third).forEachIndexed { idx, p ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (idx == activeIdx) activeFill else inactiveFill)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${TimeHelper.formatZ(p.first)} — ${TimeHelper.formatZ(p.second)}",
                            color = textColor
                        )
                        val numeralColor = if (idx == activeIdx) {
                            colorResource(R.color.bm_accent2)
                        } else {
                            textColor
                        }
                        Text(listOf("I", "II", "III")[idx], color = numeralColor)
                    }
                }
                if (idx < 2) Spacer(Modifier.width(6.dp))
            }
        }
    }
}
