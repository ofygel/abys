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
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/* ----------------------------------------------------------------------- */
/*  SMALL LOCAL HELPERS                                                    */
/* ----------------------------------------------------------------------- */

@Composable
private fun LabeledRow(
    label: String,
    value: String,
    accent: Boolean = false
) = Row(
    Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        label,
        color = if (accent) Color(0xFF34D399) else Color.White.copy(alpha = 0.9f),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        value,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun NextBanner(nextName: String, remain: Duration?) {
    val label = buildString {
        append(nextName)
        remain?.let {
            append("  ")
            append(
                "%02d:%02d:%02d".format(
                    it.toHours(),
                    it.toMinutesPart(),
                    it.toSecondsPart()
                )
            )
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(Color(0xFF16A34A)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ThirdChip(text: String, active: Boolean) {
    Box(
        Modifier
            .padding(start = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) Color(0xFF10B981) else Color.White.copy(alpha = 0.12f)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            color = if (active) Color.White else Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ----------------------------------------------------------------------- */
/*  MAIN COMPOSABLE                                                        */
/* ----------------------------------------------------------------------- */

@Composable
fun PrayerBoard(timings: UiTimings, selectedSchool: Int) {

    /* тик-состояние для секундного обновления */
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); tick++ } }

    /* --- расчёты --- */
    val tz: ZoneId = timings.tz
    val next = timings.nextPrayer(selectedSchool)                       // Pair(name,time) или null
    val remain = next?.second?.let { TimeHelper.untilNowTo(it, tz) }    // Duration?

    val thirds = TimeHelper.splitNight(timings.maghrib, timings.fajr, tz) // Triple<Pair,Pair,Pair>
    val now: ZonedDateTime = ZonedDateTime.now(tz)
    val activeThird = when {
        now.isBefore(thirds.first.second)  -> 1
        now.isBefore(thirds.second.second) -> 2
        else                               -> 3
    }

    /* --- UI --- */
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .blur(20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    0f to Color(0xCC0F0F10),
                    1f to Color(0x990C0C0D)
                )
            )
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            LabeledRow("Fajr",    timings.fajr)
            LabeledRow("Shuruq",  timings.sunrise)
            LabeledRow("Dhuhr",   timings.dhuhr)

            /* Asr (два мазхаба) */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Asr", color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleMedium)
                Row {
                    Text(timings.asrStd,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("  |  ", color = Color.White.copy(alpha = 0.6f))
                    Text(timings.asrHan,
                        color = Color(0xFFB4F1C4),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                }
            }

            LabeledRow("Maghrib", timings.maghrib)
            LabeledRow("Isha",    timings.isha)

            /* треть ночи */
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ThirdChip(
                    "I   ${TimeHelper.formatZ(thirds.first.first)}–${TimeHelper.formatZ(thirds.first.second)}",
                    activeThird == 1
                )
                ThirdChip(
                    "II  ${TimeHelper.formatZ(thirds.second.first)}–${TimeHelper.formatZ(thirds.second.second)}",
                    activeThird == 2
                )
                ThirdChip(
                    "III ${TimeHelper.formatZ(thirds.third.first)}–${TimeHelper.formatZ(thirds.third.second)}",
                    activeThird == 3
                )
            }
        }

        /* баннер «следующий намаз» снизу */
        Column {
            Spacer(Modifier.height(IntrinsicSize.Min))
            Spacer(Modifier.weight(1f))
            next?.let { NextBanner(it.first, remain) }
        }
    }
}
