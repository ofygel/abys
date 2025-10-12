package com.example.abys.logic

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsStore {
    private val KEY_SCHOOL = intPreferencesKey("asr_school") // 0=Standard, 1=Hanafi
    private val KEY_CITY = stringPreferencesKey("last_city")
    private val KEY_LAST_JSON = stringPreferencesKey("last_timings_json")

    suspend fun setSchool(ctx: Context, school: Int) {
        ctx.dataStore.edit { it[KEY_SCHOOL] = school.coerceIn(0,1) }
    }
    suspend fun getSchool(ctx: Context): Int {
        val prefs = ctx.dataStore.data.first()
        return prefs[KEY_SCHOOL] ?: 0
    }

    suspend fun setCity(ctx: Context, city: String) {
        ctx.dataStore.edit { it[KEY_CITY] = city }
    }
    suspend fun getCity(ctx: Context): String? {
        val prefs = ctx.dataStore.data.first()
        return prefs[KEY_CITY]
    }

    suspend fun setLastJson(ctx: Context, json: String) {
        ctx.dataStore.edit { it[KEY_LAST_JSON] = json }
    }
    suspend fun getLastJson(ctx: Context): String? =
        ctx.dataStore.data.first()[KEY_LAST_JSON]
}
