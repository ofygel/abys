package com.example.abys.logic

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SettingsStore {

    private const val PREF_NAME      = "settings"
    private const val KEY_SCHOOL     = "asr_school"        // 0 = Standard, 1 = Hanafi
    private const val KEY_CITY       = "last_city"
    private const val KEY_LAST_JSON  = "last_timings_json"

    /* -------- helpers -------- */
    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /* -------- public API (suspend, как раньше) -------- */

    /** 0 или 1 */
    suspend fun setSchool(ctx: Context, school: Int) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putInt(KEY_SCHOOL, school.coerceIn(0, 1)) }
    }
    suspend fun getSchool(ctx: Context): Int = withContext(Dispatchers.IO) {
        prefs(ctx).getInt(KEY_SCHOOL, 0)
    }

    suspend fun setCity(ctx: Context, city: String) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putString(KEY_CITY, city) }
    }
    suspend fun getCity(ctx: Context): String? = withContext(Dispatchers.IO) {
        prefs(ctx).getString(KEY_CITY, null)
    }

    suspend fun setLastJson(ctx: Context, json: String) = withContext(Dispatchers.IO) {
        prefs(ctx).edit { putString(KEY_LAST_JSON, json) }
    }
    suspend fun getLastJson(ctx: Context): String? = withContext(Dispatchers.IO) {
        prefs(ctx).getString(KEY_LAST_JSON, null)
    }
}
