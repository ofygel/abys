package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import java.time.Duration
import java.time.ZoneId

@Composable
private fun RowItem(label: String, valueRight: @Composable () -> Unit, accent: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (accent) Color(0xFF34D399) else Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.titleMedium
        )
        Box(Modifier.wrapContentWidth()) { valueRight() }
    }
}

@Composable
private fun ValueText(text: String, strong: Boolean = true, color: Color = Color.White) {
    Text(
        text,
        color = color,
        style = if (strong) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
        fontWeight = if (strong) FontWeight.Bold else FontWeight.SemiBold
    )
}

@Composable private fun NextBanner(name: String, remain: Duration?) {
    val label = buildString {
        append(name)
        remain?.let {
            append("  ")
            val h = it.toHours(); val m = (it.toMinutes() % 60); val s = (it.seconds % 60)
            append("%02d:%02d:%02d".format(h, m, s))
        }
    }
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Color(0xFF16A34A)),
        contentAlignment = Alignment.Center
    ) { ValueText(label) }
}

@Composable
private fun ThirdChip(label: String, text: String, active: Boolean) {
    Box(
        Modifier.padding(start = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) Color(0xFF10B981) else Color.White.copy(alpha = 0.12f)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text("$label  $text",
            color = if (active) Color.White else Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PrayerBoard(t: UiTimings, selectedSchool: Int) {
    // тик раз в секунду для баннера/активной трети
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); tick++ } }

    val tz: ZoneId = t.tz
    val next = t.nextPrayer(selectedSchool)
    val remainNext = next?.second?.let { TimeHelper.untilNowTo(it, tz) }

    // thirds of night
    val parts = TimeHelper.splitNight(t.maghrib, t.fajr, tz)
    val now = java.time.ZonedDateTime.now(tz)
    val active = when {
        now.isBefore(parts.first.second) -> 1
        now.isBefore(parts.second.second) -> 2
        else -> 3
    }

    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .blur(20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(0f to Color(0xCC0F0F10), 1f to Color(0x990C0C0D))
            )
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            RowItem("Fajr", { ValueText(t.fajr) })
            RowItem("Shuruq", { ValueText(t.sunrise) })
            RowItem("Dhuhr", { ValueText(t.dhuhr) })

            // Asr: два времени рядом
            RowItem("Asr", {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ValueText(t.asrStd)
                    Text("  |  ", color = Color.White.copy(alpha = 0.6f))
                    ValueText(t.asrHan, color = Color(0xFFB4F1C4))
                }
            })

            RowItem("Maghrib", { ValueText(t.maghrib) })
            RowItem("Isha", { ValueText(t.isha) })

            // Под Isha — три части ночи до фаджра
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                ThirdChip("I",  "${TimeHelper.formatZ(parts.first.first)}–${TimeHelper.formatZ(parts.first.second)}",   active==1)
                ThirdChip("II", "${TimeHelper.formatZ(parts.second.first)}–${TimeHelper.formatZ(parts.second.second)}", active==2)
                ThirdChip("III","${TimeHelper.formatZ(parts.third.first)}–${TimeHelper.formatZ(parts.third.second)}",  active==3)
            }

            Spacer(Modifier.height(10.dp))
        }

        Column {
            Spacer(Modifier.height(IntrinsicSize.Min))
            Spacer(Modifier.weight(1f))
            if (next != null) NextBanner(next.first, remainNext)
        }
    }
}
