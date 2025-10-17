package com.example.abys.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.abys.logic.PersistedUiState
import com.example.abys.logic.PrayerAlarmScheduler
import com.example.abys.logic.SettingsStore
import com.example.abys.logic.UiTimings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId

class AlarmReschedulerBootReceiver : BroadcastReceiver() {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(PersistedUiState::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in relevantActions) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val persisted = SettingsStore.getLastJson(context)?.let { raw ->
                    runCatching { adapter.fromJson(raw) }.getOrNull()
                }
                val school = SettingsStore.getSchool(context)
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
                    PrayerAlarmScheduler(context).schedule(ui, school)
                }
            } finally {
                withContext(Dispatchers.Main) { pendingResult.finish() }
            }
        }
    }

    companion object {
        private val relevantActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
    }
}
