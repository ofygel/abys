package com.example.abys.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.abys.AbysApp
import com.example.abys.R

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val time = intent.getStringExtra(EXTRA_PRAYER_TIME)

        val notification = NotificationCompat.Builder(context, AbysApp.CHANNEL_PRAYER_TIMES)
            .setSmallIcon(R.drawable.ic_prayer_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(
                time?.let { context.getString(R.string.prayer_alarm_notification, name, it) }
                    ?: context.getString(R.string.prayer_alarm_notification_no_time, name)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(name.hashCode(), notification)
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
    }
}
