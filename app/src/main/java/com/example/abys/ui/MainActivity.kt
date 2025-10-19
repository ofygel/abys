package com.example.abys.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.abys.ui.screen.MainApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val startFaded = intent?.getBooleanExtra(EXTRA_START_FADED, false) == true && savedInstanceState == null

        setContent { MainApp(startFaded = startFaded) }
    }

    companion object {
        const val EXTRA_START_FADED = "extra_start_faded"
    }
}