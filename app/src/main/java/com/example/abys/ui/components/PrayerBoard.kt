package com.example.abys.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.colorResource
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import java.time.Duration
import java.time.ZoneId
import com.example.abys.R

@Composable
private fun RowItem(label: String, valueRight: @Composable () -> Unit, accent: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val labelColor = if (accent) {
            colorResource(R.color.bm_accent2)
        } else {
            colorResource(R.color.bm_text)
        }
        Text(label, color = labelColor, style = MaterialTheme.typography.titleMedium)
        Box(Modifier.wrapContentWidth()) { valueRight() }
    }
}

@Composable
private fun ValueText(text: String, strong: Boolean = true, color: Color = Color.Unspecified) {
    val resolvedColor = color.takeOrElse { colorResource(R.color.bm_text) }
    Text(
        text,
        color = resolvedColor,
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
            .background(colorResource(R.color.bm_accent)),
        contentAlignment = Alignment.Center
    ) { ValueText(label) }
}

@Composable
private fun ThirdChip(label: String, text: String, active: Boolean) {
    Box(
        Modifier.padding(start = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) colorResource(R.color.bm_accent) else Color.White.copy(alpha = 0.12f)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text("$label  $text",
            color = colorResource(R.color.bm_text),
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

    val cardBg = colorResource(R.color.bm_card).copy(alpha = 0.8f)
    val outline = colorResource(R.color.bm_outline)
    val cardShape = RoundedCornerShape(24.dp)
    val highlight = next?.first

    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .blur(20.dp)
            .clip(cardShape)
            .background(cardBg)
            .border(BorderStroke(1.dp, outline), cardShape)
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            RowItem("Fajr", { ValueText(t.fajr) }, accent = highlight == "Fajr")
            RowItem("Shuruq", { ValueText(t.sunrise) }, accent = highlight == "Shuruq")
            RowItem("Dhuhr", { ValueText(t.dhuhr) }, accent = highlight == "Dhuhr")

            // Asr: два времени рядом
            RowItem("Asr", {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ValueText(t.asrStd, color = Color(0xFFAAAAAA))
                    Text("  |  ", color = colorResource(R.color.bm_text).copy(alpha = 0.6f))
                    ValueText(t.asrHan, color = colorResource(R.color.bm_accent2))
                }
            }, accent = highlight == "Asr")

            RowItem("Maghrib", { ValueText(t.maghrib) }, accent = highlight == "Maghrib")
            RowItem("Isha", { ValueText(t.isha) }, accent = highlight == "Isha")

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
