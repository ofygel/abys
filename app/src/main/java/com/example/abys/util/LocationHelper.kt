package com.example.abys.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocationHelper {

    /** Возвращает (lat, lon) или null, если ничего не доступно. */
    @SuppressLint("MissingPermission")
    suspend fun getLastBestLocation(ctx: Context): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            ).filter { lm.isProviderEnabled(it) }

            val loc: Location? = providers
                .mapNotNull { lm.getLastKnownLocation(it) }
                .maxByOrNull { it.accuracy * -1 }  // самая точная

            loc?.let { it.latitude to it.longitude }
        }
}
