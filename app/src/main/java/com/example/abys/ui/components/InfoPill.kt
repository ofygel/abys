package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoPill(
    title: String,
    value: String,
    badge: String? = null,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val container = if (highlight) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(container)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = buildString {
                if (!badge.isNullOrBlank()) {
                    append(badge)
                    append("  ")
                }
                append(title)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
