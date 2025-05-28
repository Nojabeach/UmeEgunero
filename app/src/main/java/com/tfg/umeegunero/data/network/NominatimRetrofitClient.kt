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
 * Cliente Retrofit para realizar peticiones a la API de Nominatim con configuración adecuada para cumplir 
 * con las políticas de uso.
 * 
 * Nominatim es un servicio de geocodificación que utiliza datos de OpenStreetMap para convertir 
 * direcciones en coordenadas geográficas y viceversa. Este cliente implementa las mejores prácticas
 * para interactuar con el servicio de acuerdo a sus términos y condiciones.
 * 
 * Referencias:
 * - [Políticas de uso de Nominatim](https://operations.osmfoundation.org/policies/nominatim/)
 * - [Documentación de la API](https://nominatim.org/release-docs/develop/api/)
 * 
 * Características:
 * - Añade el User-Agent requerido a todas las peticiones
 * - Implementa tiempos de espera adecuados para conexiones móviles
 * - Registra errores en Crashlytics para monitorización
 * - Configura logging para depuración solo en builds de desarrollo
 * 
 * @property nominatimApiService Servicio API Retrofit expuesto para realizar peticiones a Nominatim
 * 
 * @see NominatimApiService
 * @see NominatimPlace
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
object NominatimRetrofitClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val USER_AGENT = "UmeEgunero/1.0" // Nombre de la aplicación y versión
    
    /**
     * Interceptor para añadir el User-Agent a todas las peticiones.
     * 
     * Nominatim requiere un User-Agent válido para todas las peticiones como parte de sus
     * términos de servicio. Este interceptor garantiza que cada solicitud incluya un 
     * encabezado User-Agent adecuado identificando a nuestra aplicación.
     * 
     * También maneja los errores de red, registrándolos en Crashlytics y Timber para
     * facilitar el diagnóstico de problemas.
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
    
    /**
     * Cliente HTTP configurado para peticiones a Nominatim.
     * 
     * Incluye:
     * - Interceptor para añadir User-Agent
     * - Logging para depuración
     * - Timeouts adecuados para redes móviles
     * - Reintentos automáticos en caso de fallos de conexión
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor())
        .addInterceptor(loggingInterceptor) // Añadir logging
        .connectTimeout(30, TimeUnit.SECONDS) // Aumentar timeout de conexión para redes móviles
        .readTimeout(30, TimeUnit.SECONDS) // Aumentar timeout de lectura
        .writeTimeout(30, TimeUnit.SECONDS) // Añadir timeout de escritura
        .retryOnConnectionFailure(true) // Reintentar en caso de fallos de conexión
        .build()
    
    /**
     * Instancia de Retrofit configurada para conectar con Nominatim.
     * 
     * Utiliza GsonConverterFactory para la serialización/deserialización JSON automática.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * Servicio API para realizar peticiones a Nominatim.
     * 
     * Este servicio expone métodos para:
     * - Búsqueda por texto (direcciones, ciudades, etc.)
     * - Búsqueda por código postal
     * - Geocodificación inversa (coordenadas a dirección)
     * - Búsqueda de detalles de objetos OSM
     * 
     * @see NominatimApiService para más detalles sobre los métodos disponibles
     */
    val nominatimApiService: NominatimApiService = retrofit.create(NominatimApiService::class.java)
} 