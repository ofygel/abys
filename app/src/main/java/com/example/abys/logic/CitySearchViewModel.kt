package com.example.abys.logic

import androidx.lifecycle.*
import com.example.abys.net.NominatimPlace
import com.example.abys.net.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CitySearchViewModel : ViewModel() {
    private val api = RetrofitProvider.nominatim

    private val _results = MutableLiveData<List<NominatimPlace>>(emptyList())
    val results: LiveData<List<NominatimPlace>> = _results

    private var job: Job? = null

    fun query(q: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            delay(200) // debounce
            if (q.length < 2) {
                _results.postValue(emptyList()); return@launch
            }
            val resp = api.search(q)
            _results.postValue(resp.body().orEmpty())
        }
    }
}
