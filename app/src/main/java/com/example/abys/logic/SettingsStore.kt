package com.example.abys.logic

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Лёгкое хранилище на SharedPreferences (без DataStore), чтобы не тащить лишние зависимости.
 * Все операции — на IO-пуле.
 */
object SettingsStore {
    private const val PREF_NAME      = "settings"
    private const val KEY_SCHOOL     = "asr_school"        // 0 = Standard, 1 = Hanafi
    private const val KEY_CITY       = "last_city"
    private const val KEY_LAST_JSON  = "last_timings_json"
    private const val KEY_THEME_ID   = "theme_id"
    private const val KEY_LAST_LAT   = "last_lat"
    private const val KEY_LAST_LON   = "last_lon"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /* ---------------- asr_school ---------------- */

    suspend fun setSchool(ctx: Context, school: Int) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putInt(KEY_SCHOOL, school.coerceIn(0, 1)) }
    }

    suspend fun getSchool(ctx: Context): Int = withContext(Dispatchers.IO) {
        prefs(ctx).getInt(KEY_SCHOOL, 0)
    }

    /* ---------------- last_city ---------------- */

    suspend fun setCity(ctx: Context, city: String) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putString(KEY_CITY, city) }
    }

    suspend fun getCity(ctx: Context): String? = withContext(Dispatchers.IO) {
        prefs(ctx).getString(KEY_CITY, null)
    }

    suspend fun setLastCoordinates(ctx: Context, lat: Double, lon: Double) = withContext(Dispatchers.IO) {
        prefs(ctx).edit {
            putString(KEY_LAST_LAT, lat.toString())
            putString(KEY_LAST_LON, lon.toString())
        }
    }

    suspend fun getLastCoordinates(ctx: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val lat = prefs(ctx).getString(KEY_LAST_LAT, null)?.toDoubleOrNull()
        val lon = prefs(ctx).getString(KEY_LAST_LON, null)?.toDoubleOrNull()
        if (lat != null && lon != null) lat to lon else null
    }

    /* -------------- last_timings_json ----------- */

    suspend fun setLastJson(ctx: Context, json: String) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putString(KEY_LAST_JSON, json) }
    }

    suspend fun getLastJson(ctx: Context): String? = withContext(Dispatchers.IO) {
        prefs(ctx).getString(KEY_LAST_JSON, null)
    }

    /* ---------------- theme_id ------------------ */

    suspend fun setThemeId(ctx: Context, id: String) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putString(KEY_THEME_ID, id) }
    }

    suspend fun getThemeId(ctx: Context): String? = withContext(Dispatchers.IO) {
        prefs(ctx).getString(KEY_THEME_ID, null)
    }
}
