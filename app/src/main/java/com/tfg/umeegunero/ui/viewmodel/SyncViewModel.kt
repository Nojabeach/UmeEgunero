package com.tfg.umeegunero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.OperacionPendiente
import com.tfg.umeegunero.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _estadoSincronizacion = MutableStateFlow<EstadoSincronizacion>(EstadoSincronizacion.NoSincronizando)
    val estadoSincronizacion: StateFlow<EstadoSincronizacion> = _estadoSincronizacion.asStateFlow()

    val operacionesPendientes: Flow<List<OperacionPendiente>> = syncRepository.obtenerOperacionesPendientes()

    init {
        viewModelScope.launch {
            operacionesPendientes.collect { operaciones ->
                if (operaciones.isNotEmpty()) {
                    _estadoSincronizacion.value = EstadoSincronizacion.SincronizacionPendiente(operaciones.size)
                } else {
                    _estadoSincronizacion.value = EstadoSincronizacion.NoSincronizando
                }
            }
        }
    }

    fun iniciarSincronizacion() {
        viewModelScope.launch {
            _estadoSincronizacion.value = EstadoSincronizacion.Sincronizando
            try {
                syncRepository.procesarOperacionesPendientes()
                _estadoSincronizacion.value = EstadoSincronizacion.SincronizacionCompletada
            } catch (e: Exception) {
                _estadoSincronizacion.value = EstadoSincronizacion.ErrorSincronizacion(e.message ?: "Error desconocido")
            }
        }
    }

    fun detenerSincronizacion() {
        // Implementar lógica para detener la sincronización si es necesario
        _estadoSincronizacion.value = EstadoSincronizacion.NoSincronizando
    }

    sealed class EstadoSincronizacion {
        object NoSincronizando : EstadoSincronizacion()
        object Sincronizando : EstadoSincronizacion()
        object SincronizacionCompletada : EstadoSincronizacion()
        data class SincronizacionPendiente(val cantidadOperaciones: Int) : EstadoSincronizacion()
        data class ErrorSincronizacion(val mensaje: String) : EstadoSincronizacion()
    }
} 