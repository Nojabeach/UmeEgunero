package com.tfg.umeegunero.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utilidades para verificar conectividad de red y servicios externos en UmeEgunero.
 * 
 * Esta clase objeto proporciona funciones para verificar el estado de la conectividad
 * a Internet y la disponibilidad de servicios externos. Es especialmente útil para
 * validar la conectividad antes de realizar operaciones que requieren acceso a la red,
 * como sincronización de datos, envío de notificaciones o carga de contenido remoto.
 * 
 * ## Funcionalidades principales:
 * - Verificación de conectividad a Internet
 * - Detección del tipo de conexión (WiFi, móvil, Ethernet)
 * - Logging detallado del estado de conectividad
 * - Soporte para diferentes tipos de transporte de red
 * 
 * ## Tipos de conexión soportados:
 * - **WiFi**: Conexión inalámbrica local
 * - **Cellular**: Conexión de datos móviles (3G, 4G, 5G)
 * - **Ethernet**: Conexión por cable (tablets/dispositivos con puerto Ethernet)
 * 
 * Las funciones utilizan las APIs modernas de Android (NetworkCapabilities)
 * para una detección precisa y compatible con versiones recientes del sistema.
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
object NetworkUtils {

    /**
     * Verifica si hay una conexión a Internet disponible en el dispositivo.
     * 
     * Esta función utiliza ConnectivityManager y NetworkCapabilities para determinar
     * si el dispositivo tiene acceso a Internet a través de cualquier tipo de conexión
     * disponible (WiFi, datos móviles o Ethernet).
     * 
     * La función registra el tipo de conexión detectada en los logs para facilitar
     * la depuración y el monitoreo del estado de conectividad.
     * 
     * ## Tipos de conexión verificados:
     * - **TRANSPORT_WIFI**: Conexión WiFi activa
     * - **TRANSPORT_CELLULAR**: Conexión de datos móviles activa
     * - **TRANSPORT_ETHERNET**: Conexión Ethernet activa (dispositivos compatibles)
     * 
     * @param context Contexto de la aplicación necesario para acceder al ConnectivityManager
     * @return `true` si hay una conexión a Internet disponible, `false` en caso contrario
     * 
     * @sample
     * ```kotlin
     * if (NetworkUtils.isNetworkAvailable(context)) {
     *     // Realizar operación que requiere Internet
     *     sincronizarDatos()
     * } else {
     *     // Mostrar mensaje de error o usar datos en caché
     *     mostrarMensajeSinConexion()
     * }
     * ```
     * 
     * @see ConnectivityManager
     * @see NetworkCapabilities
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Timber.d("Network: conectado via WiFi")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Timber.d("Network: conectado via red móvil")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Timber.d("Network: conectado via Ethernet")
                    return true
                }
            }
        }
        
        Timber.d("Network: sin conexión a Internet")
        return false
    }
} 