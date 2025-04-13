package com.tfg.umeegunero.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de conectividad para monitorizar el estado de la red
 */
@Singleton
class NetworkConnectivityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()
    
    // StateFlow para emitir cambios en el estado de la conectividad
    private val _isNetworkAvailable = MutableStateFlow(false)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable.asStateFlow()
    
    // NetworkCallback para monitorizar los cambios de red
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Timber.d("Red disponible")
            _isNetworkAvailable.value = true
        }
        
        override fun onLost(network: Network) {
            Timber.d("Red perdida")
            _isNetworkAvailable.value = false
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val hasValidatedInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            
            Timber.d("Capacidades de red cambiadas - Internet: $hasInternet, Validada: $hasValidatedInternet")
            
            // Solo consideramos que hay conectividad si internet es accesible y validado
            _isNetworkAvailable.value = hasInternet && hasValidatedInternet
        }
    }
    
    /**
     * Inicializa el monitoreo de la red
     */
    fun startNetworkMonitoring() {
        try {
            // Configuración para monitorear cualquier tipo de red
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
            
            // Verificar el estado actual de la conectividad
            _isNetworkAvailable.value = isCurrentlyConnected()
            
            Timber.i("Monitoreo de red iniciado. Estado inicial: ${_isNetworkAvailable.value}")
        } catch (e: Exception) {
            Timber.e(e, "Error al iniciar el monitoreo de red")
        }
    }
    
    /**
     * Detiene el monitoreo de la red
     */
    fun stopNetworkMonitoring() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
            Timber.i("Monitoreo de red detenido")
        } catch (e: Exception) {
            Timber.e(e, "Error al detener el monitoreo de red")
        }
    }
    
    /**
     * Verifica si hay conectividad actualmente
     */
    fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
} 