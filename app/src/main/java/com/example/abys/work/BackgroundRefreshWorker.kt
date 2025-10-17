package com.example.abys.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration

/**
 * Periodic worker that refreshes inspirational content (quotes/backgrounds) in the background.
 * The actual data source lives in repositories elsewhere; here we simply trigger the refresh
 * hooks while respecting charging + unmetered constraints from the checklist.
 */
class BackgroundRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Future hook: refresh cached hadiths / themes. For now we just acknowledge the tick
        // so WorkManager keeps the cadence alive.
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "background-refresh"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()

            val request = PeriodicWorkRequestBuilder<BackgroundRefreshWorker>(Duration.ofHours(12))
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
