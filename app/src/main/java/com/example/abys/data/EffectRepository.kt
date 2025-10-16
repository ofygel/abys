package com.example.abys.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.effectDataStore by preferencesDataStore(name = "abys_effects")

enum class EffectId { leaves, lightning, night, rain, snow, storm, sunset_snow, wind }

class EffectRepository(private val appContext: Context) {

    private val keyEffect = stringPreferencesKey("effect_id")

    val selectedEffect: Flow<EffectId> =
        appContext.effectDataStore.data.map { prefs ->
            val stored = prefs[keyEffect]
            stored?.let { runCatching { EffectId.valueOf(it) }.getOrNull() } ?: EffectId.night
        }

    suspend fun setEffect(id: EffectId) {
        appContext.effectDataStore.edit { prefs ->
            prefs[keyEffect] = id.name
        }
    }
}
