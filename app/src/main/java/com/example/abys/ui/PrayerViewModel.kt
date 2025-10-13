package com.example.abys.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.data.PrayerTimesRepository
import com.example.abys.data.model.PrayerTimes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrayerViewModel(
    private val repo: PrayerTimesRepository = PrayerTimesRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<PrayerTimes?>(null)
    val state = _state.asStateFlow()

    fun load(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = repo.fetch(lat, lon)
        }
    }
}
