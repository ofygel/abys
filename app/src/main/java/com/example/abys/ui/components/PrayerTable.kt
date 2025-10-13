package com.example.abys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.abys.logic.TimeHelper
import com.example.abys.logic.UiTimings
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZoneId

@Composable
fun PrayerTable(t: UiTimings, selectedSchool: Int) {
    val tz: ZoneId = t.tz
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val next = t.nextPrayer(selectedSchool)
    val remain: Duration? = next?.second?.let { TimeHelper.untilNowTo(it, tz) }

    val rows = listOf(
        "Fajr" to t.fajr,
        "Shuruq" to t.sunrise,
        "Dhuhr" to t.dhuhr,
        "Asr" to t.asr(selectedSchool),
        "Maghrib" to t.maghrib,
        "Isha" to t.isha
    )

    Column(Modifier.fillMaxWidth()) {
        rows.forEach { (name, time) ->
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
                    if (next?.first == name && remain != null) {
                        val h = remain.toHours()
                        val m = remain.toMinutes() % 60
                        val s = remain.seconds % 60
                        Text(
                            text = " через %02d:%02d:%02d".format(h, m, s),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                // СПРАВА: Название
                Text(text = name, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
