package com.tfg.umeegunero.feature.common.welcome.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.repository.SyncRepository
import com.tfg.umeegunero.util.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para la pantalla de bienvenida.
 * 
 * Se encarga de:
 * - Iniciar la sincronización cuando sea necesario
 * - Verificar si hay operaciones pendientes
 * - Gestionar el estado de la pantalla de bienvenida
 */
@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val syncRepository: SyncRepository
) : ViewModel() {
    
    /**
     * Inicia la sincronización si hay operaciones pendientes.
     * Solo se ejecuta cuando la app está en primer plano (desde la WelcomeScreen).
     */
    fun iniciarSincronizacionSiEsNecesario() {
        viewModelScope.launch {
            try {
                val operacionesPendientes = syncRepository.obtenerNumeroOperacionesPendientes()
                
                if (operacionesPendientes > 0) {
                    Timber.d("Hay $operacionesPendientes operaciones pendientes, iniciando sincronización desde WelcomeScreen")
                    // Como estamos en la WelcomeScreen, la app está en primer plano
                    // Es seguro iniciar el servicio de sincronización
                    syncManager.iniciarServicioSincronizacion()
                } else {
                    Timber.d("No hay operaciones pendientes de sincronización")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar operaciones pendientes")
            }
        }
    }
} 