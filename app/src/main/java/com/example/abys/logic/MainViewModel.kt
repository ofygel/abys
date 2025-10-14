package com.example.abys.logic

import android.content.Context
import androidx.lifecycle.*
import com.example.abys.net.RetrofitProvider
import com.example.abys.net.TimingsResponse
import com.example.abys.util.LocationHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.ZoneId

/**
 * Главная VM: грузит тайминги по гео или по городу, хранит выбранный мазхаб и заголовок (город/хиджра).
 */
class MainViewModel(
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val api = RetrofitProvider.aladhan

    private val _city = MutableLiveData<String?>()
    val city: LiveData<String?> = _city

    private val _hijri = MutableLiveData<String?>()
    val hijri: LiveData<String?> = _hijri

    private val _timings = MutableLiveData<UiTimings?>()
    val timings: LiveData<UiTimings?> = _timings

    private val _school = MutableLiveData(0) // 0=Standard,1=Hanafi
    val school: LiveData<Int> = _school

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

            // Город: либо из аргумента, либо «подрезаем» timezone "Asia/Almaty" → "Almaty"
            val cityName = cityOverride ?: dStd.meta.timezone.substringAfter('/', dStd.meta.timezone)
            _city.postValue(cityName)

            _hijri.postValue(hijriText(dStd))
        }
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
