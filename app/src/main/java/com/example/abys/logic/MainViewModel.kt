package com.example.abys.logic

import android.content.Context
import androidx.lifecycle.*
import com.example.abys.net.RetrofitProvider
import com.example.abys.net.TimingsResponse
import com.example.abys.util.LocationHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Главная VM: грузит тайминги по гео или по городу, хранит выбранный мазхаб и заголовок (город/хиджра).
 */
class MainViewModel : ViewModel() {

    private val io: CoroutineDispatcher = Dispatchers.IO

    private val api = RetrofitProvider.aladhan

    private val hadiths = listOf(
        "Поистине, дела оцениваются по намерениям, …",
        "Лучшим из вас является тот, кто изучает Коран …"
    )
    private val _hadithToday = MutableLiveData(
        hadiths[LocalDate.now().dayOfYear % hadiths.size]
    )
    val hadithToday: LiveData<String> = _hadithToday

    private val _sheetVisible = MutableLiveData(false)
    val sheetVisible: LiveData<Boolean> = _sheetVisible

    private val _pickerVisible = MutableLiveData(false)
    val pickerVisible: LiveData<Boolean> = _pickerVisible

    private val _city = MutableLiveData("Almaty")
    val city: LiveData<String> = _city

    private val _hijri = MutableLiveData<String?>()
    val hijri: LiveData<String?> = _hijri

    private val _timings = MutableLiveData<UiTimings?>()
    val timings: LiveData<UiTimings?> = _timings

    private val _school = MutableLiveData(0) // 0=Standard,1=Hanafi
    val school: LiveData<Int> = _school

    private val _prayerTimes = MutableLiveData<Map<String, String>>(emptyMap())
    val prayerTimes: LiveData<Map<String, String>> = _prayerTimes

    private val _thirds = MutableLiveData(Triple("--:--", "--:--", "--:--"))
    val thirds: LiveData<Triple<String, String, String>> = _thirds

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val _clock = MutableLiveData(LocalTime.now().format(timeFormatter))
    val clock: LiveData<String> = _clock

    init {
        viewModelScope.launch {
            while (true) {
                val zone = _timings.value?.tz ?: ZoneId.systemDefault()
                _clock.postValue(TimeHelper.now(zone).format(timeFormatter))
                delay(1_000L)
            }
        }
    }

    fun setSchool(s: Int, reload: Boolean = true, ctx: Context? = null) {
        val v = s.coerceIn(0, 1)
        _school.value = v
        if (ctx != null) viewModelScope.launch(io) { SettingsStore.setSchool(ctx, v) }
        if (!reload) return

        // Перезагружаем по тому источнику, что есть
        val c = _city.value
        if (!c.isNullOrBlank()) {
            loadByCity(c)
        } else if (ctx != null) {
            loadByLocation(ctx)
        }
    }

    fun toggleSheet() {
        val newValue = !(_sheetVisible.value ?: false)
        _sheetVisible.value = newValue
        if (!newValue) {
            _pickerVisible.value = false
        }
    }

    fun togglePicker() {
        _pickerVisible.value = !(_pickerVisible.value ?: false)
    }

    fun setCity(c: String) {
        if (c.isBlank()) return
        _city.value = c
        _pickerVisible.value = false
        _sheetVisible.value = false
        loadByCity(c)
    }

    fun loadSavedSchool(ctx: Context) {
        viewModelScope.launch(io) {
            val s = SettingsStore.getSchool(ctx)
            _school.postValue(s)
        }
    }

    /** Геолокация → запрос по lat/lon (оба мазхаба). */
    fun loadByLocation(ctx: Context) {
        viewModelScope.launch(io) {
            val last = LocationHelper.getLastBestLocation(ctx) ?: run {
                // Гео нет — оставляем как есть; CityPicker на UI подстрахует
                return@launch
            }
            val lat = last.first
            val lon = last.second

            val std = runCatching {
                api.timings(latitude = lat, longitude = lon, method = 2, school = 0)
            }.getOrNull()
            val han = runCatching {
                api.timings(latitude = lat, longitude = lon, method = 2, school = 1)
            }.getOrNull()

            handlePairResponses(std, han, cityOverride = null)
        }
    }

    /** По названию города (оба мазхаба). */
    fun loadByCity(city: String, country: String = DEFAULT_COUNTRY) {
        viewModelScope.launch(io) {
            val std = runCatching {
                api.timingsByCity(city = city, country = country, method = 2, school = 0)
            }.getOrNull()
            val han = runCatching {
                api.timingsByCity(city = city, country = country, method = 2, school = 1)
            }.getOrNull()

            handlePairResponses(std, han, cityOverride = city)
        }
    }

    private fun handlePairResponses(
        std: Response<TimingsResponse>?,
        han: Response<TimingsResponse>?,
        cityOverride: String?
    ) {
        if (std?.isSuccessful == true && han?.isSuccessful == true) {
            val dStd = std.body()!!.data
            val dHan = han.body()!!.data
            val tz = ZoneId.of(dStd.meta.timezone)

            val ui = UiTimings(
                fajr    = dStd.timings.fajr,
                sunrise = dStd.timings.sunrise,
                dhuhr   = dStd.timings.dhuhr,
                asrStd  = dStd.timings.asr,
                asrHan  = dHan.timings.asr,
                maghrib = dStd.timings.maghrib,
                isha    = dStd.timings.isha,
                tz      = tz
            )
            _timings.postValue(ui)
            updateDerived(ui)

            // Город: либо из аргумента, либо «подрезаем» timezone "Asia/Almaty" → "Almaty"
            val cityName = cityOverride ?: dStd.meta.timezone.substringAfter('/', dStd.meta.timezone)
            _city.postValue(cityName)

            _hijri.postValue(hijriText(dStd))
        }
    }

    private fun updateDerived(ui: UiTimings) {
        _prayerTimes.postValue(
            mapOf(
                "Fajr" to ui.fajr,
                "Sunrise" to ui.sunrise,
                "Dhuhr" to ui.dhuhr,
                "AsrStd" to ui.asrStd,
                "AsrHana" to ui.asrHan,
                "Maghrib" to ui.maghrib,
                "Isha" to ui.isha
            )
        )

        val parts = TimeHelper.splitNight(ui.maghrib, ui.fajr, ui.tz)
        _thirds.postValue(
            Triple(
                TimeHelper.formatZ(parts.first.first),
                TimeHelper.formatZ(parts.second.first),
                TimeHelper.formatZ(parts.third.first)
            )
        )
    }

    private fun hijriText(d: TimingsResponse.Data): String? {
        val h = d.date.hijri
        // аккуратно собираем: "Rabi' al-thani 1446"
        val parts = listOfNotNull(h?.month?.en, h?.year)
        return parts.joinToString(" ").ifBlank { null }
    }

    companion object {
        private const val DEFAULT_COUNTRY = "Kazakhstan"
    }
}
