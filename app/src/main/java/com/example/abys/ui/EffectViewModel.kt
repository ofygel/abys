package com.example.abys.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.abys.data.EffectId
import com.example.abys.data.EffectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EffectViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = EffectRepository(app)

    val effect: StateFlow<EffectId> = repository.selectedEffect
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EffectId.night)

    fun onEffectSelected(id: EffectId) {
        viewModelScope.launch { repository.setEffect(id) }
    }
}
