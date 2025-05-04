package com.tfg.umeegunero.di

import com.tfg.umeegunero.data.network.NominatimApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import timber.log.Timber
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Módulo de inyección de dependencias para configuraciones de red.
 * 
 * Este módulo proporciona las dependencias relacionadas con servicios web 
 * y APIs externas utilizadas en la aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val USER_AGENT = "UmeEgunero/1.0" // Nombre de la aplicación y versión
    
    /**
     * Interceptor para añadir el User-Agent a todas las peticiones de Nominatim
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
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Proporciona el servicio API de Nominatim utilizando el cliente Retrofit existente.
     * 
     * @return Implementación del servicio NominatimApiService
     */
    @Provides
    @Singleton
    fun provideNominatimApiService(retrofit: Retrofit): NominatimApiService {
        return retrofit.create(NominatimApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideKtorHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true // Importante para flexibilidad en respuestas
                })
            }
            // Aquí puedes añadir más configuración de Ktor si es necesaria (timeouts, logging, etc.)
            engine {
                requestTimeout = 30_000 // Timeout de 30 segundos
            }
            // Logging omitido por ahora para simplificar
        }
    }
} 