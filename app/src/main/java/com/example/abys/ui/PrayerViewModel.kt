package com.example.abys.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.R
import com.example.abys.data.FallbackContent
import com.example.abys.data.PrayerTimesRepository
import com.example.abys.data.PrayerTimesSerializer
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
            val cached = SettingsStore.getLastJson(context)?.let { PrayerTimesSerializer.decode(it) }
            val fallbackLabel = savedCity ?: cached?.timezone?.id?.substringAfterLast('/')
                ?.replace('_', ' ')
            val demoTimings = FallbackContent.prayerTimes
            _state.update {
                it.copy(
                    selectedSchool = savedSchool,
                    locationLabel = fallbackLabel ?: it.locationLabel ?: FallbackContent.cityLabel,
                    timings = cached ?: it.timings ?: demoTimings
                )
            }
        }
    }

    fun load(context: Context, lat: Double, lon: Double, label: String? = null) {
        _state.update { it.copy(isLoading = true, errorMessage = null, locationLabel = label ?: it.locationLabel) }
        viewModelScope.launch {
            val result = repo.fetch(lat, lon)
            if (result != null) {
                val resolvedLabel = label ?: result.timezone.id.substringAfterLast('/')
                    .replace('_', ' ')
                PrayerTimesSerializer.encode(result)?.let { SettingsStore.setLastJson(context, it) }
                _state.update { prev ->
                    prev.copy(
                        isLoading = false,
                        timings = result,
                        locationLabel = resolvedLabel,
                        errorMessage = null
                    )
                }
            } else {
                val cached = SettingsStore.getLastJson(context)?.let { PrayerTimesSerializer.decode(it) }
                _state.update { prev ->
                    if (cached != null) {
                        prev.copy(
                            isLoading = false,
                            timings = cached,
                            errorMessage = context.getString(R.string.prayer_load_error_cached)
                        )
                    } else {
                        prev.copy(
                            isLoading = false,
                            errorMessage = context.getString(R.string.prayer_load_error_generic)
                        )
                    }
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
