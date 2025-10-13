package com.example.abys.util   // ← оставьте тот же пакет

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocationHelper {

    /** Новый универсальный метод — сразу (lat,lon) или null */
    @SuppressLint("MissingPermission")
    suspend fun getLastBestLocation(ctx: Context): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            getLastKnownLocation(ctx)?.let { it.latitude to it.longitude }
        }

    /* ------------ ↓ совместимость со старым кодом ↓ ------------ */

    /** Старая сигнатура, которую ждёт MainViewModel */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(ctx: Context): Location? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).filter { lm.isProviderEnabled(it) }

        // выбираем наиболее точный lastKnownLocation
        return providers
            .mapNotNull { lm.getLastKnownLocation(it) }
            .minByOrNull { it.accuracy }   // accuracy: меньше = лучше
    }
}
