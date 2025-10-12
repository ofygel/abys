package com.example.abys.logic

import android.content.Context
import androidx.lifecycle.*
import com.example.abys.net.RetrofitProvider
import com.example.abys.net.TimingsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.ZoneId

class MainViewModel : ViewModel() {

    private val _city = MutableLiveData<String?>()
    val city: LiveData<String?> = _city

    private val _hijri = MutableLiveData<String?>()
    val hijri: LiveData<String?> = _hijri

    private val _timings = MutableLiveData<UiTimings?>()
    val timings: LiveData<UiTimings?> = _timings

    private val _school = MutableLiveData(0) // 0=Standard,1=Hanafi (для логики nextPrayer)
    val school: LiveData<Int> = _school

    private val api = RetrofitProvider.aladhan

    fun setSchool(s: Int, reload: Boolean = true, ctx: Context? = null) {
        _school.value = s.coerceIn(0,1)
        if (ctx != null) viewModelScope.launch(Dispatchers.IO) { SettingsStore.setSchool(ctx, _school.value!!) }
        if (reload) {
            _city.value?.let { loadByCity(it) } ?: ctx?.let { loadByLocation(it) }
        }
    }

    fun loadSavedSchool(ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val s = SettingsStore.getSchool(ctx)
            _school.postValue(s)
        }
    }

    fun loadByLocation(ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val loc = LocationHelper.getLastKnownLocation(ctx) ?: return@launch
            // две параллельные загрузки: school 0 и 1
            val std = async {
                api.timings(latitude = loc.latitude, longitude = loc.longitude, method = 2, school = 0)
            }
            val han = async {
                api.timings(latitude = loc.latitude, longitude = loc.longitude, method = 2, school = 1)
            }
            val rStd = std.await()
            val rHan = han.await()
            if (rStd.isSuccessful && rHan.isSuccessful) {
                val dStd = rStd.body()!!.data
                val dHan = rHan.body()!!.data
                val tz = ZoneId.of(dStd.meta.timezone)
                _city.postValue(dStd.meta.timezone.substringAfter('/'))
                _hijri.postValue(hijriText(dStd))
                _timings.postValue(UiTimings.fromResponses(dStd, dHan, tz))
            }
        }
    }

    fun loadByCity(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val std = async { api.timingsByCity(city = city, country = "Kazakhstan", method = 2, school = 0) }
            val han = async { api.timingsByCity(city = city, country = "Kazakhstan", method = 2, school = 1) }
            val rStd = std.await()
            val rHan = han.await()
            if (rStd.isSuccessful && rHan.isSuccessful) {
                val dStd = rStd.body()!!.data
                val dHan = rHan.body()!!.data
                val tz = ZoneId.of(dStd.meta.timezone)
                _city.postValue(city)
                _hijri.postValue(hijriText(dStd))
                _timings.postValue(UiTimings.fromResponses(dStd, dHan, tz))
            }
        }
    }

    private fun hijriText(d: TimingsResponse.Data): String? {
        val h = d.date.hijri
        return listOfNotNull(h?.month?.en, h?.year).joinToString(" ").ifBlank { null }
    }
}

data class UiTimings(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asrStd: String,
    val asrHan: String,
    val maghrib: String,
    val isha: String,
    val tz: java.time.ZoneId
) {
    fun asr(selectedSchool: Int): String = if (selectedSchool == 1) asrHan else asrStd

    fun toDisplayList(selectedSchool: Int): List<Pair<String,String>> = listOf(
        "Fajr" to fajr,
        "Shuruq" to sunrise,
        "Dhuhr" to dhuhr,
        "Asr" to asr(selectedSchool), // в списке отражаем выбранный, но в стеклянной карточке покажем оба
        "Maghrib" to maghrib,
        "Isha" to isha
    )

    fun nextPrayer(selectedSchool: Int): Pair<String, String>? {
        val order = listOf(
            "Fajr" to fajr,
            "Shuruq" to sunrise,
            "Dhuhr" to dhuhr,
            "Asr" to asr(selectedSchool),
            "Maghrib" to maghrib,
            "Isha" to isha
        )
        val now = TimeHelper.now(tz)
        return order.firstOrNull { (_, t) ->
            val tt = TimeHelper.parseHHmmLocal(t, tz) ?: return@firstOrNull false
            now.isBefore(tt)
        } ?: order.firstOrNull()
    }

    companion object {
        fun fromResponses(std: TimingsResponse.Data, han: TimingsResponse.Data, tz: java.time.ZoneId) =
            UiTimings(
                fajr    = std.timings.Fajr,
                sunrise = std.timings.Sunrise,
                dhuhr   = std.timings.Dhuhr,
                asrStd  = std.timings.Asr,
                asrHan  = han.timings.Asr,
                maghrib = std.timings.Maghrib,
                isha    = std.timings.Isha,
                tz      = tz
            )
    }
}
