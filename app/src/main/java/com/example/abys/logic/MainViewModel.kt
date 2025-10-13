package com.example.abys.logic

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.net.RetrofitProvider
import com.example.abys.net.TimingsResponse
import com.example.abys.util.LocationHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.ZoneId

class MainViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val api = RetrofitProvider.aladhan

    private val _city = MutableLiveData<String?>()
    val city: LiveData<String?> = _city

    private val _timings = MutableLiveData<UiTimings?>()
    val timings: LiveData<UiTimings?> = _timings

    private val _school = MutableLiveData(0)
    val school: LiveData<Int> = _school

    private val _hijri = MutableLiveData<String?>()
    val hijri: LiveData<String?> = _hijri

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var lastLocation: Pair<Double, Double>? = null
    private var lastCity: String? = null

    fun loadSavedSchool(ctx: Context) {
        viewModelScope.launch(ioDispatcher) {
            val savedSchool = SettingsStore.getSchool(ctx)
            _school.postValue(savedSchool)

            val savedCity = SettingsStore.getCity(ctx)
            lastCity = savedCity
            _city.postValue(savedCity)
        }
    }

    fun setSchool(school: Int, reload: Boolean, ctx: Context) {
        viewModelScope.launch(ioDispatcher) {
            SettingsStore.setSchool(ctx, school)
            _school.postValue(school)

            if (reload) {
                when {
                    lastLocation != null -> {
                        val (lat, lon) = lastLocation!!
                        loadTimingsForLocation(lat, lon, null)
                    }
                    lastCity != null -> {
                        val city = lastCity!!
                        loadTimingsFromFetcher(city) { school ->
                            api.timingsByCity(city, DEFAULT_COUNTRY, school = school)
                        }
                    }
                }
            }
        }
    }

    fun loadByLocation(ctx: Context) {
        viewModelScope.launch(ioDispatcher) {
            val loc = LocationHelper.getLastBestLocation(ctx)
            if (loc == null) {
                _error.postValue("location_unavailable")
                return@launch
            }
            lastLocation = loc
            lastCity = null
            loadTimingsForLocation(loc.first, loc.second, null)
        }
    }

    fun loadByCity(cityName: String, country: String = DEFAULT_COUNTRY) {
        viewModelScope.launch(ioDispatcher) {
            lastCity = cityName
            lastLocation = null
            loadTimingsFromFetcher(cityName) { school ->
                api.timingsByCity(cityName, country, school = school)
            }
        }
    }

    private suspend fun loadTimingsForLocation(
        latitude: Double,
        longitude: Double,
        cityName: String?
    ) {
        loadTimingsFromFetcher(cityName) { school ->
            api.timings(latitude, longitude, school = school)
        }
    }

    private suspend fun loadTimingsFromFetcher(
        cityName: String?,
        fetcher: suspend (Int) -> Response<TimingsResponse>
    ) {
        val std = runCatching { fetcher(0) }.getOrElse { throwable ->
            _error.postValue(throwable.message)
            return
        }
        val han = runCatching { fetcher(1) }.getOrElse { throwable ->
            _error.postValue(throwable.message)
            return
        }

        val stdData = std.body()?.data
        val hanData = han.body()?.data
        if (!std.isSuccessful || !han.isSuccessful || stdData == null || hanData == null) {
            _error.postValue("api_error")
            return
        }

        val zoneId = runCatching { ZoneId.of(stdData.meta.timezone) }.getOrElse { ZoneId.systemDefault() }
        val timings = UiTimings(
            fajr = stdData.timings.Fajr,
            sunrise = stdData.timings.Sunrise,
            dhuhr = stdData.timings.Dhuhr,
            asrStd = stdData.timings.Asr,
            asrHan = hanData.timings.Asr,
            maghrib = stdData.timings.Maghrib,
            isha = stdData.timings.Isha,
            tz = zoneId
        )
        _timings.postValue(timings)

        val hijriInfo = stdData.date.hijri
        _hijri.postValue(
            hijriInfo?.let { info ->
                listOfNotNull(info.date, info.month?.en, info.year)
                    .joinToString(separator = " ")
            }
        )

        cityName?.let { name ->
            _city.postValue(name)
        }
    }

    companion object {
        private const val DEFAULT_COUNTRY = "Kazakhstan"
    }
}