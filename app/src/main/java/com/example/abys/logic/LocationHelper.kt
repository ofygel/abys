package com.example.abys.logic

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(ctx: Context, timeoutMs: Long = 3000): Location? {
        // Попробуем FusedLocationProvider, если есть
        return try {
            val client = LocationServices.getFusedLocationProviderClient(ctx)
            val task = client.lastLocation
            val start = System.currentTimeMillis()
            while (!task.isComplete && System.currentTimeMillis() - start < timeoutMs) {
                Thread.sleep(20)
            }
            task.result ?: fallbackLocation(ctx)
        } catch (_: Throwable) {
            fallbackLocation(ctx)
        }
    }

    @Suppress("MissingPermission")
    private fun fallbackLocation(ctx: Context): Location? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        return providers.asSequence().mapNotNull { p -> lm.getLastKnownLocation(p) }.firstOrNull()
    }
}
