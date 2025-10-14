package com.example.abys.ui.screens.background

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun SlideshowBackground(
    modifier: Modifier = Modifier,
    images: List<Int>,
    intervalMs: Long = 8000L
) {
    var index by remember(images) { mutableStateOf(0) }

    LaunchedEffect(images) {
        while (true) {
            delay(intervalMs)
            if (images.isNotEmpty()) {
                index = (index + 1) % images.size
            }
        }
    }

    if (images.isNotEmpty()) {
        Crossfade(targetState = index, label = "bg") { i ->
            AsyncImage(
                model = images[i],
                contentDescription = null,
                modifier = modifier
            )
        }
    }
}
