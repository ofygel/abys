package com.example.abys.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels          // ← делегат для ViewModel
import com.example.abys.ui.screens.HomeScreen
import com.example.abys.ui.theme.AbysTheme

class MainActivity : ComponentActivity() {

    /** создаём ViewModel “по-классически”, без compose-экстеншенов */
    private val vm: PrayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbysTheme {
                HomeScreen(viewModel = vm)
            }
        }
    }
}
