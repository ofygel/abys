package com.example.abys.logic

import androidx.lifecycle.*
import com.example.abys.net.NominatimPlace
import com.example.abys.net.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Поиск города (Nominatim) с дебаунсом и отменой предыдущего запроса.
 */
class CitySearchViewModel : ViewModel() {
    private val api = RetrofitProvider.nominatim

    private val _results = MutableLiveData<List<NominatimPlace>>(emptyList())
    val results: LiveData<List<NominatimPlace>> = _results

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private var job: Job? = null

    fun search(query: String) {
        job?.cancel()
        if (query.isBlank()) {
            _results.value = emptyList()
            _loading.value = false
            return
        }
        job = viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)
            delay(250) // debounce
            val resp = runCatching { api.search(query) }.getOrNull()
            if (resp?.isSuccessful == true) {
                _results.postValue(resp.body().orEmpty())
            } else {
                _results.postValue(emptyList())
            }
            _loading.postValue(false)
        }
    }

    fun query(query: String) = search(query)
}
