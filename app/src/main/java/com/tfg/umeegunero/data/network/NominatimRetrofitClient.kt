package com.tfg.umeegunero.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

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
            
            try {
                return chain.proceed(requestWithUserAgent)
            } catch (e: Exception) {
                // Registrar el error en Crashlytics
                FirebaseCrashlytics.getInstance().log("Error en petición Nominatim: ${e.message}")
                FirebaseCrashlytics.getInstance().setCustomKey("nominatim_request_url", originalRequest.url.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
                
                // Registrar también en Timber
                Timber.e(e, "Error en petición Nominatim a ${originalRequest.url}")
                
                // Propagar el error
                throw e
            }
        }
    }
    
    // Configurar el logging para depuración
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.tfg.umeegunero.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor())
        .addInterceptor(loggingInterceptor) // Añadir logging
        .connectTimeout(30, TimeUnit.SECONDS) // Aumentar timeout de conexión para redes móviles
        .readTimeout(30, TimeUnit.SECONDS) // Aumentar timeout de lectura
        .writeTimeout(30, TimeUnit.SECONDS) // Añadir timeout de escritura
        .retryOnConnectionFailure(true) // Reintentar en caso de fallos de conexión
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val nominatimApiService: NominatimApiService = retrofit.create(NominatimApiService::class.java)
} 