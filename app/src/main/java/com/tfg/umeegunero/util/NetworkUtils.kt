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
 * Clase de utilidad para verificar la conectividad a Internet y servicios externos
 */
object NetworkUtils {

    /**
     * Verifica si hay una conexión a Internet disponible
     * @param context Contexto de la aplicación
     * @return true si hay conexión a Internet disponible
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
    
    /**
     * Verifica la conectividad a los servidores de SendGrid
     * @return true si se puede establecer conexión con SendGrid
     */
    suspend fun checkSendGridConnectivity(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.sendgrid.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000 // 5 segundos
                connection.readTimeout = 5000
                connection.requestMethod = "HEAD"
                
                val responseCode = connection.responseCode
                Timber.d("SendGrid ping: código respuesta $responseCode")
                
                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: IOException) {
                Timber.e(e, "Error al verificar conectividad con SendGrid")
                false
            }
        }
    }
} 