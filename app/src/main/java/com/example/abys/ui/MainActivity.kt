package com.example.abys.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.abys.ui.screen.MainApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Полная Compose-верстка: фон + всё остальное внутри MainApp()
        setContent { MainApp() }
    }
}
