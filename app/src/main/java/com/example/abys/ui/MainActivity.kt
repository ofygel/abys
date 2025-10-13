package com.example.abys.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.abys.ui.theme.AbysTheme
import com.example.abys.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbysTheme {
                val vm: PrayerViewModel = viewModel()
                HomeScreen(viewModel = vm)
            }
        }
    }
}
