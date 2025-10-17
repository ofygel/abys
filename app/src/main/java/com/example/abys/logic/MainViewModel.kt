package com.example.abys.logic

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.data.FallbackContent
import com.example.abys.net.RetrofitProvider
import com.example.abys.net.TimingsResponse
import com.example.abys.util.LocationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class CitySheetTab { Wheel, Search }

/**
 * Главная VM: грузит тайминги по гео или по городу, хранит выбранный мазхаб и заголовок (город/хиджра).
 */
class MainViewModel(
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val api = RetrofitProvider.aladhan
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val persistedAdapter = moshi.adapter(PersistedUiState::class.java)
    private var lastPersistContext: WeakReference<Context>? = null

    private val hadiths = listOf(
        "Поистине, дела оцениваются по намерениям, …",
        "Лучшим из вас является тот, кто изучает Коран …"
    )
    private val _hadithToday = MutableLiveData(
        hadiths[LocalDate.now().dayOfYear % hadiths.size]
    )
    val hadithToday: LiveData<String> = _hadithToday

    private val fallbackTimings = FallbackContent.uiTimings
    private val fallbackPrayerMap = mapOf(
        "Fajr" to fallbackTimings.fajr,
        "Sunrise" to fallbackTimings.sunrise,
        "Dhuhr" to fallbackTimings.dhuhr,
        "AsrStd" to fallbackTimings.asrStd,
        "AsrHana" to fallbackTimings.asrHan,
        "Maghrib" to fallbackTimings.maghrib,
        "Isha" to fallbackTimings.isha
    )

    private val fallbackThirds = FallbackContent.nightIntervals

    private val _sheetVisible = MutableLiveData(false)
    val sheetVisible: LiveData<Boolean> = _sheetVisible

    private val _sheetTab = MutableLiveData(CitySheetTab.Wheel)
    val sheetTab: LiveData<CitySheetTab> = _sheetTab

    private val shouldKeepSheetHidden = AtomicBoolean(false)

    private val _city = MutableLiveData(FallbackContent.cityLabel)
    val city: LiveData<String> = _city

    private val _hijri = MutableLiveData<String?>(FallbackContent.hijriLabel)
    val hijri: LiveData<String?> = _hijri

    private val _timings = MutableLiveData<UiTimings?>(fallbackTimings)
    val timings: LiveData<UiTimings?> = _timings

    private val _school = MutableLiveData(0) // 0=Standard,1=Hanafi
    val school: LiveData<Int> = _school

    private val _prayerTimes = MutableLiveData<Map<String, String>>(fallbackPrayerMap)
    val prayerTimes: LiveData<Map<String, String>> = _prayerTimes

    private val _thirds = MutableLiveData(fallbackThirds)
    val thirds: LiveData<NightIntervals> = _thirds

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val _clock = MutableLiveData(TimeHelper.now(fallbackTimings.tz).format(timeFormatter))
    val clock: LiveData<String> = _clock

    init {
        updateDerived(fallbackTimings)
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
            loadByCity(c, ctx = ctx)
        } else if (ctx != null) {
            loadByLocation(ctx)
        }
    }

    fun toggleSheet() {
        val newValue = !(_sheetVisible.value ?: false)
        _sheetVisible.value = newValue
        if (!newValue) {
            _sheetTab.value = CitySheetTab.Wheel
        } else {
            _sheetTab.value = CitySheetTab.Wheel
        }
        if (newValue) {
            shouldKeepSheetHidden.set(false)
        }
    }

    fun hideSheet() {
        shouldKeepSheetHidden.set(true)
        _sheetVisible.value = false
        _sheetTab.value = CitySheetTab.Wheel
    }

    fun setSheetTab(tab: CitySheetTab) {
        _sheetTab.value = tab
    }

    fun setCity(c: String, ctx: Context? = null) {
        if (c.isBlank()) return
        _city.value = c
        _sheetTab.value = CitySheetTab.Wheel
        _sheetVisible.value = false
        shouldKeepSheetHidden.set(true)
        if (ctx != null) {
            lastPersistContext = WeakReference(ctx.applicationContext)
            viewModelScope.launch(io) { SettingsStore.setCity(ctx, c) }
        }
        loadByCity(c, ctx = ctx)
    }

    fun loadSavedSchool(ctx: Context) {
        viewModelScope.launch(io) {
            val s = SettingsStore.getSchool(ctx)
            _school.postValue(s)
        }
    }

    fun restorePersisted(ctx: Context) {
        // Гарантируем, что главный экран стартует в режиме Dashboard,
        // даже если сохранённое состояние когда-то открыло city sheet.
        shouldKeepSheetHidden.set(true)
        _sheetVisible.value = false
        _sheetTab.value = CitySheetTab.Wheel
        lastPersistContext = WeakReference(ctx.applicationContext)
        viewModelScope.launch(io) {
            val school = SettingsStore.getSchool(ctx)
            _school.postValue(school)

            val savedCity = SettingsStore.getCity(ctx)
            val persisted = SettingsStore.getLastJson(ctx)?.let { raw ->
                runCatching { persistedAdapter.fromJson(raw) }.getOrNull()
            }

            if (persisted != null) {
                val zone = runCatching { ZoneId.of(persisted.tz) }.getOrElse { ZoneId.systemDefault() }
                val ui = UiTimings(
                    fajr = persisted.fajr,
                    sunrise = persisted.sunrise,
                    dhuhr = persisted.dhuhr,
                    asrStd = persisted.asrStd,
                    asrHan = persisted.asrHan,
                    maghrib = persisted.maghrib,
                    isha = persisted.isha,
                    tz = zone
                )
                _timings.postValue(ui)
                updateDerived(ui)
                _city.postValue(persisted.city)
                _hijri.postValue(persisted.hijri)
            } else if (!savedCity.isNullOrBlank()) {
                _city.postValue(savedCity)
            }

            when {
                !persisted?.city.isNullOrBlank() -> loadByCity(persisted!!.city, ctx = ctx)
                !savedCity.isNullOrBlank() -> loadByCity(savedCity!!, ctx = ctx)
                else -> loadByLocation(ctx)
            }

            // Перестраховываемся на случай фоновых обновлений, но не мешаем
            // пользователю, если он успел открыть шит вручную.
            if (shouldKeepSheetHidden.compareAndSet(true, false)) {
                _sheetVisible.postValue(false)
                _sheetTab.postValue(CitySheetTab.Wheel)
            }
        }
    }

    /** Геолокация → запрос по lat/lon (оба мазхаба). */
    fun loadByLocation(ctx: Context) {
        lastPersistContext = WeakReference(ctx.applicationContext)
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

            handlePairResponses(std, han, cityOverride = null, persistCtx = ctx)
        }
    }

    /** По названию города (оба мазхаба). */
    fun loadByCity(city: String, country: String = DEFAULT_COUNTRY, ctx: Context? = null) {
        ctx?.let { lastPersistContext = WeakReference(it.applicationContext) }
        viewModelScope.launch(io) {
            val std = runCatching {
                api.timingsByCity(city = city, country = country, method = 2, school = 0)
            }.getOrNull()
            val han = runCatching {
                api.timingsByCity(city = city, country = country, method = 2, school = 1)
            }.getOrNull()

            handlePairResponses(std, han, cityOverride = city, persistCtx = ctx ?: lastPersistContext?.get())
        }
    }

    private fun handlePairResponses(
        std: Response<TimingsResponse>?,
        han: Response<TimingsResponse>?,
        cityOverride: String?,
        persistCtx: Context?,
    ) {
        val stdBody = std?.takeIf { it.isSuccessful }?.body()?.data ?: return
        val hanBody = han?.takeIf { it.isSuccessful }?.body()?.data

        val tz = runCatching { ZoneId.of(stdBody.meta.timezone) }.getOrElse { ZoneId.systemDefault() }

        val ui = UiTimings(
            fajr = stdBody.timings.fajr,
            sunrise = stdBody.timings.sunrise,
            dhuhr = stdBody.timings.dhuhr,
            asrStd = stdBody.timings.asr,
            asrHan = hanBody?.timings?.asr ?: stdBody.timings.asr,
            maghrib = stdBody.timings.maghrib,
            isha = stdBody.timings.isha,
            tz = tz
        )
        _timings.postValue(ui)
        updateDerived(ui)

        val derivedCity = stdBody.meta.timezone.substringAfter('/', stdBody.meta.timezone).ifBlank { null }
        val cityName = cityOverride?.takeIf { it.isNotBlank() }
            ?: derivedCity
            ?: _city.value
            ?: FallbackContent.cityLabel
        _city.postValue(cityName)

        val hijri = hijriText(stdBody)
        _hijri.postValue(hijri)

        persistCtx?.let { context ->
            val persisted = PersistedUiState(
                city = cityName,
                hijri = hijri,
                fajr = ui.fajr,
                sunrise = ui.sunrise,
                dhuhr = ui.dhuhr,
                asrStd = ui.asrStd,
                asrHan = ui.asrHan,
                maghrib = ui.maghrib,
                isha = ui.isha,
                tz = ui.tz.id
            )
            viewModelScope.launch(io) {
                SettingsStore.setLastJson(context, persistedAdapter.toJson(persisted))
                SettingsStore.setCity(context, cityName)
                PrayerAlarmScheduler(context).schedule(ui, _school.value ?: 0)
            }
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
        val intervals = NightIntervals(
            first = TimeHelper.formatZ(parts.first.first) to TimeHelper.formatZ(parts.first.second),
            second = TimeHelper.formatZ(parts.second.first) to TimeHelper.formatZ(parts.second.second),
            third = TimeHelper.formatZ(parts.third.first) to TimeHelper.formatZ(parts.third.second)
        )
        _thirds.postValue(intervals)
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
