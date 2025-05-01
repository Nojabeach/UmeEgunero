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
} 