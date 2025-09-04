package com.example.abys

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.example.abys.ui.screens.HomeScreen
import com.example.abys.ui.theme.ShoegazeTheme

class MainActivity : ComponentActivity() {

    private val vm by viewModels<MainViewModel>()

    private val requestLocation = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val ok = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) vm.fetchByGps()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoegazeTheme {
                Surface { HomeScreen(vm) { askLocPerm() } }
            }
        }
    }

    private fun askLocPerm() {
        requestLocation.launch(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }
}
