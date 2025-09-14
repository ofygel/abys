package com.example.abys.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun GlassCard(
    currentBgRes: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(26.dp)
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color(0x26FFFFFF)),
        border = BorderStroke(1.dp, Color.White.copy(0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(Modifier.clip(shape)) {
            AsyncImage(
                model = currentBgRes,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(10.dp)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.14f))
            )
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}
