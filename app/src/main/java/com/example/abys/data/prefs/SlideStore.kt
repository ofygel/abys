package com.example.abys.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("abys_prefs")
private val KEY_INDEX = intPreferencesKey("slide_index")
private val KEY_EPOCH = longPreferencesKey("slide_epoch")

data class SlideAnchor(val index: Int, val epoch: Long)

object SlideStore {
    fun flow(context: Context) = context.dataStore.data.map {
        SlideAnchor(index = it[KEY_INDEX] ?: 0, epoch = it[KEY_EPOCH] ?: 0L)
    }
    suspend fun save(context: Context, index: Int, epoch: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[KEY_INDEX] = index
            prefs[KEY_EPOCH] = epoch
        }
    }
}
