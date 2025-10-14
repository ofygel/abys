package com.example.abys.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.data.PrayerTimesRepository
import com.example.abys.data.model.PrayerTimes
import com.example.abys.logic.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrayerViewModel(
    private val repo: PrayerTimesRepository = PrayerTimesRepository()
) : ViewModel() {

    data class PrayerUiState(
        val isLoading: Boolean = false,
        val timings: PrayerTimes? = null,
        val selectedSchool: Int = 0,
        val locationLabel: String? = null,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(PrayerUiState())
    val state = _state.asStateFlow()

    fun initialize(context: Context) {
        viewModelScope.launch {
            val savedSchool = SettingsStore.getSchool(context)
            val savedCity = SettingsStore.getCity(context)
            _state.update { it.copy(selectedSchool = savedSchool, locationLabel = savedCity ?: it.locationLabel) }
        }
    }

    fun load(lat: Double, lon: Double, label: String? = null) {
        _state.update { it.copy(isLoading = true, errorMessage = null, locationLabel = label ?: it.locationLabel) }
        viewModelScope.launch {
            val result = repo.fetch(lat, lon)
            _state.update { prev ->
                if (result == null) {
                    prev.copy(isLoading = false, errorMessage = "Не удалось загрузить расписание")
                } else {
                    val resolvedLabel = label ?: result.timezone.id.substringAfterLast('/')
                        .replace('_', ' ')
                    prev.copy(
                        isLoading = false,
                        timings = result,
                        locationLabel = resolvedLabel,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun updateSchool(context: Context, school: Int) {
        val normalized = school.coerceIn(0, 1)
        _state.update { it.copy(selectedSchool = normalized) }
        viewModelScope.launch { SettingsStore.setSchool(context, normalized) }
    }
}
