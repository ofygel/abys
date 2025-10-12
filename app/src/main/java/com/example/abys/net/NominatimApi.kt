package com.example.abys.net

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class NominatimPlace(
    val display_name: String,
    val lat: String,
    val lon: String
)

interface NominatimApi {
    // Требует User-Agent, мы добавим его в клиенте
    @GET("search?format=json&addressdetails=0&limit=10")
    suspend fun search(@Query("q") query: String): Response<List<NominatimPlace>>
}
