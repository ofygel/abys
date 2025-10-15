package com.example.abys.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.abys.R
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import java.time.Duration
import java.time.ZoneId
import kotlinx.coroutines.delay

@Composable
fun PrayerTable(t: UiTimings, selectedSchool: Int, highlightKey: String? = null) {
    val tz: ZoneId = t.tz
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val next = t.nextPrayer(selectedSchool)
    val remain: Duration? = next?.second?.let { TimeHelper.untilNowTo(it, tz) }

    val asrAltTime = if (selectedSchool == 1) t.asrStd else t.asrHan
    val asrAltLabel = if (selectedSchool == 1) R.string.prayer_asr_alt_standard else R.string.prayer_asr_alt_hanafi

    val rows = listOf(
        PrayerRow("Fajr", R.string.prayer_fajr, t.fajr),
        PrayerRow("Shuruq", R.string.prayer_shuruq, t.sunrise),
        PrayerRow("Dhuhr", R.string.prayer_dhuhr, t.dhuhr),
        PrayerRow(
            key = "Asr",
            titleRes = R.string.prayer_asr,
            time = t.asr(selectedSchool),
            secondary = stringResource(id = asrAltLabel, asrAltTime)
        ),
        PrayerRow("Maghrib", R.string.prayer_maghrib, t.maghrib),
        PrayerRow("Isha", R.string.prayer_isha, t.isha)
    )

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            val active = highlightKey == row.key || next?.first == row.key
            val background by animateColorAsState(
                if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                label = "prayerRowBg"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(background),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = row.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    Column(
                        modifier = Modifier.sizeIn(minWidth = 96.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = row.time,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (row.secondary != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = row.secondary,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End
                            )
                        }
                        if (active && remain != null) {
                            Spacer(Modifier.height(4.dp))
                            val h = remain.toHours()
                            val m = remain.toMinutes() % 60
                            val s = remain.seconds % 60
                            Text(
                                text = stringResource(id = R.string.prayer_remaining, h, m, s),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PrayerRow(
    val key: String,
    val titleRes: Int,
    val time: String,
    val secondary: String? = null
)
