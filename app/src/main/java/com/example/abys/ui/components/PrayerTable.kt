package com.example.abys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZoneId
import com.example.abys.R

@Composable
fun PrayerTable(t: UiTimings, selectedSchool: Int) {
    val tz: ZoneId = t.tz
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val next = t.nextPrayer(selectedSchool)
    val remain: Duration? = next?.second?.let { TimeHelper.untilNowTo(it, tz) }

    val rows = listOf(
        Triple("Fajr", R.string.prayer_fajr, t.fajr),
        Triple("Shuruq", R.string.prayer_shuruq, t.sunrise),
        Triple("Dhuhr", R.string.prayer_dhuhr, t.dhuhr),
        Triple("Asr", R.string.prayer_asr, t.asr(selectedSchool)),
        Triple("Maghrib", R.string.prayer_maghrib, t.maghrib),
        Triple("Isha", R.string.prayer_isha, t.isha)
    )

    Column(Modifier.fillMaxWidth()) {
        rows.forEach { (key, label, time) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // СЛЕВА: Время + «до события»
                Column {
                    Text(text = time, style = MaterialTheme.typography.titleLarge)
                    if (next?.first == key && remain != null) {
                        val h = remain.toHours()
                        val m = remain.toMinutes() % 60
                        val s = remain.seconds % 60
                        Text(
                            text = stringResource(id = R.string.prayer_remaining, h, m, s),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                // СПРАВА: Название
                Text(text = stringResource(id = label), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
