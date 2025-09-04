package com.example.abys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun GlassTopBar(
    currentBgRes: Int,
    title: String,
    onClick: () -> Unit,
    onGps: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .clickable { onClick() }
    ) {
        // псевдо-glass
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(currentBgRes),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.matchParentSize().blur(10.dp)
        )
        Box(
            Modifier
                .matchParentSize()
                .background(Color(0x33FFFFFF))
        )
        // лёгкий серебристый градиент
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(0.20f),
                            Color(0xFFB0B7C4).copy(0.10f),
                            Color.White.copy(0.08f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.OutlinedButton(onClick = onGps, contentPadding = PaddingValues(horizontal = 14.dp)) {
                Text("GPS")
            }
        }
    }
}
