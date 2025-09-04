package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun NightTimeline(
    first: Pair<String,String>,
    second: Pair<String,String>,
    third: Pair<String,String>
) {
    Column {
        Text("Ночь (3 части)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            listOf(first, second, third).forEachIndexed { idx, p ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                if (idx==1)
                                    listOf(MaterialTheme.colorScheme.secondary.copy(0.25f),
                                        MaterialTheme.colorScheme.primary.copy(0.15f))
                                else
                                    listOf(MaterialTheme.colorScheme.primary.copy(0.20f),
                                        MaterialTheme.colorScheme.background.copy(0.10f))
                            )
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${p.first} — ${p.second}")
                    Text(
                        text = when(idx){0->"I";1->"II";else->"III"},
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (idx<2) Spacer(Modifier.width(6.dp))
            }
        }
    }
}
