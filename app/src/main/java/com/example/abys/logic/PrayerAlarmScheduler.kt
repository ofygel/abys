package com.example.abys.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.abys.receiver.PrayerAlarmReceiver
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Schedules prayer reminders through [AlarmManager.setExactAndAllowWhileIdle].
 * Each prayer slot gets its own pending intent keyed by a stable request code so a
 * reschedule simply replaces the previous alarm.
 */
class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = context.getSystemService(AlarmManager::class.java)

    fun schedule(ui: UiTimings, selectedSchool: Int) {
        val manager = alarmManager ?: return
        cancelAll()

        val zone = ui.tz
        val order = listOf(
            REQUEST_FAJR to ("Fajr" to ui.fajr),
            REQUEST_SUNRISE to ("Sunrise" to ui.sunrise),
            REQUEST_DHUHR to ("Dhuhr" to ui.dhuhr),
            REQUEST_ASR to ("Asr" to ui.asr(selectedSchool)),
            REQUEST_MAGHRIB to ("Maghrib" to ui.maghrib),
            REQUEST_ISHA to ("Isha" to ui.isha)
        )

        val now = ZonedDateTime.now(zone)
        order.forEach { (requestCode, slot) ->
            val (label, hhmm) = slot
            val trigger = nextTriggerTime(hhmm, zone, now) ?: return@forEach
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, label)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, hhmm)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                pendingFlags(PendingIntent.FLAG_CANCEL_CURRENT)
            )
            manager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger.toInstant().toEpochMilli(),
                pending
            )
        }
    }

    fun cancelAll() {
        val manager = alarmManager ?: return
        listOf(REQUEST_FAJR, REQUEST_SUNRISE, REQUEST_DHUHR, REQUEST_ASR, REQUEST_MAGHRIB, REQUEST_ISHA)
            .forEach { code ->
                val pending = PendingIntent.getBroadcast(
                    context,
                    code,
                    Intent(context, PrayerAlarmReceiver::class.java),
                    pendingFlags(PendingIntent.FLAG_NO_CREATE)
                )
                if (pending != null) {
                    manager.cancel(pending)
                }
            }
    }

    private fun nextTriggerTime(hhmm: String, zone: ZoneId, now: ZonedDateTime): ZonedDateTime? {
        val local = TimeHelper.parseHHmmLocal(hhmm, zone) ?: return null
        var target = ZonedDateTime.of(LocalDate.now(zone), local, zone)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return target
    }

    private fun pendingFlags(base: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            base or PendingIntent.FLAG_IMMUTABLE
        } else {
            base
        }
    }

    companion object {
        private const val REQUEST_FAJR = 1001
        private const val REQUEST_SUNRISE = 1002
        private const val REQUEST_DHUHR = 1003
        private const val REQUEST_ASR = 1004
        private const val REQUEST_MAGHRIB = 1005
        private const val REQUEST_ISHA = 1006
    }
}
