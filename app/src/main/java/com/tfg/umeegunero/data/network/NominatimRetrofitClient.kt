package com.tfg.umeegunero.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para Nominatim con configuración adecuada para cumplir con las políticas de uso
 * https://operations.osmfoundation.org/policies/nominatim/
 */
object NominatimRetrofitClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val USER_AGENT = "UmeEgunero/1.0" // Nombre de la aplicación y versión
    
    /**
     * Interceptor para añadir el User-Agent a todas las peticiones
     * Nominatim requiere un User-Agent válido para todas las peticiones
     */
    private class UserAgentInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", USER_AGENT)
                .build()
            return chain.proceed(requestWithUserAgent)
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val nominatimApiService: NominatimApiService = retrofit.create(NominatimApiService::class.java)
} 