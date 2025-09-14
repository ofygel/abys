package com.example.abys.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("abys_prefs")

private val KEY_LABEL = stringPreferencesKey("city_label")
private val KEY_LAT = doublePreferencesKey("city_lat")
private val KEY_LON = doublePreferencesKey("city_lon")

data class CityPrefs(val label: String, val lat: Double, val lon: Double)

object CityStore {
    fun flow(context: Context) = context.dataStore.data.map {
        CityPrefs(
            label = it[KEY_LABEL] ?: "",
            lat = it[KEY_LAT] ?: 0.0,
            lon = it[KEY_LON] ?: 0.0
        )
    }

    suspend fun save(context: Context, label: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LABEL] = label
            prefs[KEY_LAT] = lat
            prefs[KEY_LON] = lon
        }
    }
}

