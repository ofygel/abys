package com.example.abys.data.geocoding

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.http.GET
import retrofit2.http.Query

class UAInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header("User-Agent", "ABYS/1.0 (contact: example@abys.app)")
            .build()
        return chain.proceed(req)
    }
}

interface NominatimService {
    @GET("search?format=json&addressdetails=0&limit=5")
    suspend fun search(@Query("q") q: String): List<NominatimItem>

    companion object {
        fun create(): NominatimService {
            val ok = okhttp3.OkHttpClient.Builder()
                .addInterceptor(UAInterceptor())
                .build()
            return retrofit2.Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .client(ok)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(NominatimService::class.java)
        }
    }
}
