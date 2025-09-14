package com.example.abys

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.data.aladhan.AladhanService
import com.example.abys.data.geocoding.NominatimItem
import com.example.abys.data.geocoding.NominatimService
import com.example.abys.data.location.LocationManager
import com.example.abys.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.ZoneId

data class TimingsUi(
    val fajr: String = "--:--",
    val sunrise: String = "--:--",
    val dhuhr: String = "--:--",
    val asrStandard: String = "--:--",
    val asrHanafi: String = "--:--",
    val maghrib: String = "--:--",
    val isha: String = "--:--",
    val nightParts: Triple<Pair<String,String>, Pair<String,String>, Pair<String,String>> =
        Triple("--:--" to "--:--","--:--" to "--:--","--:--" to "--:--"),
    val cityLabel: String = "—",
    val tzId: String = ZoneId.systemDefault().id   // новое поле
)

class MainViewModel(app: Application): AndroidViewModel(app) {

    private val aladhan = AladhanService.create()
    private val nominatim = NominatimService.create()
    private val loc = LocationManager(app.applicationContext)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _suggestions = MutableStateFlow<List<NominatimItem>>(emptyList())
    val suggestions: StateFlow<List<NominatimItem>> = _suggestions

    private val _timings = MutableStateFlow(TimingsUi())
    val timings: StateFlow<TimingsUi> = _timings

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .flatMapLatest { q ->
                    flow {
                        val result = if (q.length < 2) emptyList() else
                            runCatching { nominatim.search(q) }.getOrElse { emptyList() }
                        emit(result)
                    }
                }
                .collect { _suggestions.value = it }
        }
    }

    fun updateQuery(q: String) {
        _query.value = q
        if (q.length < 2) { _suggestions.value = emptyList() }
    }

    fun selectSuggestion(item: NominatimItem) {
        _query.value = item.display_name
        _suggestions.value = emptyList()
        fetchByCoords(item.lat.toDouble(), item.lon.toDouble(), item.display_name)
    }

    fun fetchByCity(city: String) {
        viewModelScope.launch {
            val std = runCatching { aladhan.timingsByCity(city = city, school = 0) }.getOrNull()
            val han = runCatching { aladhan.timingsByCity(city = city, school = 1) }.getOrNull()
            if (std != null && han != null) {
                val tz = ZoneId.of(std.data.meta.timezone)
                val parts = runCatching {
                    TimeUtils.splitNight(std.data.timings.Maghrib, std.data.timings.Fajr, tz)
                }.onFailure { println("splitNight error: ${'$'}{it.message}") }
                    .getOrNull() ?: return@launch
                _timings.value = TimingsUi(
                    fajr = std.data.timings.Fajr,
                    sunrise = std.data.timings.Sunrise,
                    dhuhr = std.data.timings.Dhuhr,
                    asrStandard = std.data.timings.Asr,
                    asrHanafi = han.data.timings.Asr,
                    maghrib = std.data.timings.Maghrib,
                    isha = std.data.timings.Isha,
                    nightParts = Triple(
                        TimeUtils.format(parts.first.first) to TimeUtils.format(parts.first.second),
                        TimeUtils.format(parts.second.first) to TimeUtils.format(parts.second.second),
                        TimeUtils.format(parts.third.first) to TimeUtils.format(parts.third.second)
                    ),
                    cityLabel = city,
                    tzId = tz.id     // передаём таймзону
                )
            }
        }
    }

    fun fetchByGps() {
        viewModelScope.launch {
            val l = loc.lastKnown() ?: return@launch
            fetchByCoords(l.latitude, l.longitude, "GPS ${"%.3f".format(l.latitude)}, ${"%.3f".format(l.longitude)}")
        }
    }

    private fun fetchByCoords(lat: Double, lon: Double, label: String) {
        viewModelScope.launch {
            val std = runCatching { aladhan.timingsByCoords(lat, lon, school = 0) }.getOrNull()
            val han = runCatching { aladhan.timingsByCoords(lat, lon, school = 1) }.getOrNull()
            if (std != null && han != null) {
                val tz = ZoneId.of(std.data.meta.timezone)
                val parts = runCatching {
                    TimeUtils.splitNight(std.data.timings.Maghrib, std.data.timings.Fajr, tz)
                }.onFailure { println("splitNight error: ${'$'}{it.message}") }
                    .getOrNull() ?: return@launch
                _timings.value = TimingsUi(
                    fajr = std.data.timings.Fajr,
                    sunrise = std.data.timings.Sunrise,
                    dhuhr = std.data.timings.Dhuhr,
                    asrStandard = std.data.timings.Asr,
                    asrHanafi = han.data.timings.Asr,
                    maghrib = std.data.timings.Maghrib,
                    isha = std.data.timings.Isha,
                    nightParts = Triple(
                        TimeUtils.format(parts.first.first) to TimeUtils.format(parts.first.second),
                        TimeUtils.format(parts.second.first) to TimeUtils.format(parts.second.second),
                        TimeUtils.format(parts.third.first) to TimeUtils.format(parts.third.second)
                    ),
                    cityLabel = label,
                    tzId = tz.id   // передаём таймзону
                )
            }
        }
    }
}
