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
    var index by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMs)
            index = (index + 1) % images.size
        }
    }

    Crossfade(targetState = index, label = "bg") { i ->
        AsyncImage(
            model = images[i],
            contentDescription = null,
            modifier = modifier
        )
    }
}
