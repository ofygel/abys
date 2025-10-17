package com.example.abys

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.abys.R
import com.example.abys.work.BackgroundRefreshWorker

/**
 * Application entry point used for one-time system configuration such as
 * creating the notification channels required by the checklist.
 */
class AbysApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannels()
        scheduleBackgroundRefresh()
    }

    private fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java) ?: return

        val prayerChannel = NotificationChannel(
            CHANNEL_PRAYER_TIMES,
            getString(R.string.channel_prayer_times_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_prayer_times_description)
            setShowBadge(true)
        }

        val backgroundChannel = NotificationChannel(
            CHANNEL_BACKGROUND_UPDATES,
            getString(R.string.channel_background_updates_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_background_updates_description)
            setShowBadge(false)
        }

        manager.createNotificationChannels(listOf(prayerChannel, backgroundChannel))
    }

    private fun scheduleBackgroundRefresh() {
        BackgroundRefreshWorker.schedule(this)
    }

    companion object {
        const val CHANNEL_PRAYER_TIMES = "prayer_times"
        const val CHANNEL_BACKGROUND_UPDATES = "background_updates"
    }
}
