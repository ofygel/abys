package com.example.abys.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.ZoneId

@Composable
fun NextPrayerChip(name: String, time: String, zone: ZoneId, remaining: Duration?) {
    val label = buildString {
        append(name)
        append(" • ")
        append(time)
        if (remaining != null) {
            val h = remaining.toHours()
            val m = (remaining.toMinutes() % 60)
            append("  (")
            append("%02d:%02d".format(h, m))
            append(")")
        }
    }
    AssistChip(
        onClick = {},
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            labelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.wrapContentWidth().height(IntrinsicSize.Min)
    )
}
