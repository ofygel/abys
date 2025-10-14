package com.example.abys.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object LocationHelper {

    private const val ACCEPTABLE_ACCURACY_METERS = 200f
    private val MAX_LOCATION_AGE_MILLIS = TimeUnit.HOURS.toMillis(2)

    /** Возвращает (lat, lon) или null, если ничего не доступно. */
    @SuppressLint("MissingPermission")
    suspend fun getLastBestLocation(ctx: Context): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            getLastKnownLocation(ctx)?.let { it.latitude to it.longitude }
        }

    /** Совместимость со старым кодом, ожидающим Location? */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(ctx: Context): Location? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).filter { provider ->
            runCatching { lm.isProviderEnabled(provider) }.getOrDefault(false)
        }

        if (providers.isEmpty()) return null

        val now = System.currentTimeMillis()

        val locations = providers
            .mapNotNull { provider ->
                runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            }
            .ifEmpty { return null }

        val recentLocations = locations.filter { now - it.time <= MAX_LOCATION_AGE_MILLIS }
        val accurateRecent = recentLocations.filter { !it.hasAccuracy() || it.accuracy <= ACCEPTABLE_ACCURACY_METERS }

        return accurateRecent.maxByOrNull { it.time }
            ?: recentLocations.maxByOrNull { it.time }
            ?: locations.maxByOrNull { it.time }
    }
}
