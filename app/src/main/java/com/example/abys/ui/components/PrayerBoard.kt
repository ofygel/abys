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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.colorResource
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import com.example.abys.R

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

@Composable
private fun ThirdChip(text: String, active: Boolean) {
    Box(
        Modifier
            .padding(start = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) colorResource(R.color.bm_accent) else Color.White.copy(alpha = 0.12f)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text,
            color = colorResource(R.color.bm_text),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/* ----------------------------------------------------------------------- */
/*  MAIN COMPOSABLE                                                        */
/* ----------------------------------------------------------------------- */

@Composable
fun PrayerBoard(t: UiTimings, selectedSchool: Int) {

    /* тик-состояние для секундного обновления */
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); tick++ } }

    /* --- расчёты --- */
    val tz: ZoneId = t.tz
    val next = t.nextPrayer(selectedSchool)                             // Pair(name,time) или null
    val remain = next?.second?.let { TimeHelper.untilNowTo(it, tz) }    // Duration?

    val thirds = TimeHelper.splitNight(t.maghrib, t.fajr, tz)           // Triple<Pair,Pair,Pair>
    val now: ZonedDateTime = remember(t, tz, tick) { ZonedDateTime.now(tz) }
    val activeThird = when {
        now.isBefore(thirds.first.second)  -> 1
        now.isBefore(thirds.second.second) -> 2
        else                               -> 3
    }

    val cardBg = colorResource(R.color.bm_card).copy(alpha = 0.82f)
    val outline = colorResource(R.color.bm_outline)
    val cardShape = RoundedCornerShape(28.dp)
    val highlight = next?.first
    val accent = colorResource(R.color.bm_accent2)

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 360.dp)
                .defaultMinSize(minHeight = 320.dp)
                .blur(20.dp)
                .clip(cardShape)
                .background(cardBg)
                .border(BorderStroke(1.dp, outline), cardShape)
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {

                PrayerRow(
                    name = "Fajr",
                    isNext = highlight == "Fajr",
                    countdown = remain.takeIf { highlight == "Fajr" }
                ) { ValueText(t.fajr) }

                PrayerRow(
                    name = "Shuruq",
                    isNext = highlight == "Shuruq",
                    countdown = remain.takeIf { highlight == "Shuruq" }
                ) { ValueText(t.sunrise) }

                PrayerRow(
                    name = "Dhuhr",
                    isNext = highlight == "Dhuhr",
                    countdown = remain.takeIf { highlight == "Dhuhr" }
                ) { ValueText(t.dhuhr) }

                val asrPrimary = if (selectedSchool == 1) t.asrHan else t.asrStd
                val asrAltLabel = if (selectedSchool == 1) "Стандарт: ${t.asrStd}" else "Ханафи: ${t.asrHan}"

                PrayerRow(
                    name = "Asr",
                    isNext = highlight == "Asr",
                    countdown = remain.takeIf { highlight == "Asr" }
                ) {
                    Column {
                        ValueText(asrPrimary, color = accent)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            asrAltLabel,
                            color = colorResource(R.color.bm_text).copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                PrayerRow(
                    name = "Maghrib",
                    isNext = highlight == "Maghrib",
                    countdown = remain.takeIf { highlight == "Maghrib" }
                ) { ValueText(t.maghrib) }

                PrayerRow(
                    name = "Isha",
                    isNext = highlight == "Isha",
                    countdown = remain.takeIf { highlight == "Isha" }
                ) { ValueText(t.isha) }

                Spacer(Modifier.height(12.dp))

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
        }
    }
}

@Composable
private fun PrayerRow(
    name: String,
    isNext: Boolean,
    countdown: Duration?,
    timeContent: @Composable () -> Unit
) {
    val textColor = if (isNext) colorResource(R.color.bm_accent2) else colorResource(R.color.bm_text)

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            timeContent()
            if (isNext && countdown != null) {
                Spacer(Modifier.height(2.dp))
                val hours = countdown.toHours()
                val minutes = Math.floorMod(countdown.toMinutes(), 60)
                val seconds = Math.floorMod(countdown.seconds, 60)
                Text(
                    "%02d:%02d:%02d".format(
                        hours,
                        minutes,
                        seconds
                    ),
                    color = colorResource(R.color.bm_text).copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Text(
            name,
            modifier = Modifier.widthIn(min = 72.dp),
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}