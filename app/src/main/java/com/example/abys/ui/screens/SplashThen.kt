package com.example.abys.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.abys.R
import kotlinx.coroutines.delay

@Composable
fun SplashThen(content: @Composable () -> Unit) {
    var show by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1500); show = false }   // 1.5 сек приветствия
    AnimatedContent(
        targetState = show,
        transitionSpec = { fadeIn(tween(600)) togetherWith fadeOut(tween(600)) },
        label = "splash"
    ) { s ->
        if (s) {
            Image(
                painter = painterResource(R.drawable.greeting_abys),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            content()
        }
    }
}
