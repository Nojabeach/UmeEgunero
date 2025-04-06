package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.repository.MensajeRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de bandeja de entrada
 */
data class BandejaEntradaUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val mensajeSeleccionado: Mensaje? = null,
    val mostrarDetalle: Boolean = false,
    val bandejaActiva: TipoBandeja = TipoBandeja.RECIBIDOS,
    val busqueda: String = "",
    val mensajesFiltrados: List<Mensaje> = emptyList(),
    val mensajeParaEliminar: Mensaje? = null,
    val mostrarAlertaEliminar: Boolean = false
)

/**
 * Tipos de bandeja para mostrar mensajes
 */
enum class TipoBandeja {
    RECIBIDOS,
    ENVIADOS,
    DESTACADOS
}

/**
 * ViewModel para la gestión de mensajes en la bandeja de entrada
 */
@HiltViewModel
class BandejaEntradaViewModel @Inject constructor(
    private val mensajeRepository: MensajeRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BandejaEntradaUiState())
    val uiState: StateFlow<BandejaEntradaUiState> = _uiState.asStateFlow()
    
    private var usuarioId: String? = null
    
    init {
        viewModelScope.launch {
            usuarioId = usuarioRepository.obtenerUsuarioActual()?.dni
            cargarMensajes()
        }
    }
    
    /**
     * Carga los mensajes según la bandeja activa
     */
    fun cargarMensajes() {
        if (usuarioId == null) {
            _uiState.update { it.copy(error = "No se ha podido identificar al usuario") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            
            when (_uiState.value.bandejaActiva) {
                TipoBandeja.RECIBIDOS -> {
                    mensajeRepository.getMensajesRecibidos(usuarioId!!).collect { mensajes ->
                        _uiState.update { 
                            it.copy(
                                mensajes = mensajes,
                                mensajesFiltrados = filtrarMensajes(mensajes, it.busqueda),
                                cargando = false,
                                error = null
                            ) 
                        }
                    }
                }
                TipoBandeja.ENVIADOS -> {
                    mensajeRepository.getMensajesEnviados(usuarioId!!).collect { mensajes ->
                        _uiState.update { 
                            it.copy(
                                mensajes = mensajes,
                                mensajesFiltrados = filtrarMensajes(mensajes, it.busqueda),
                                cargando = false,
                                error = null
                            ) 
                        }
                    }
                }
                TipoBandeja.DESTACADOS -> {
                    mensajeRepository.getMensajesDestacados(usuarioId!!).collect { mensajes ->
                        _uiState.update { 
                            it.copy(
                                mensajes = mensajes,
                                mensajesFiltrados = filtrarMensajes(mensajes, it.busqueda),
                                cargando = false,
                                error = null
                            ) 
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Filtra los mensajes según el texto de búsqueda
     */
    private fun filtrarMensajes(mensajes: List<Mensaje>, busqueda: String): List<Mensaje> {
        if (busqueda.isBlank()) return mensajes
        
        return mensajes.filter { mensaje ->
            mensaje.asunto.contains(busqueda, ignoreCase = true) ||
            mensaje.contenido.contains(busqueda, ignoreCase = true) ||
            mensaje.remitenteNombre.contains(busqueda, ignoreCase = true) ||
            mensaje.destinatarioNombre.contains(busqueda, ignoreCase = true)
        }
    }
    
    /**
     * Actualiza el texto de búsqueda y filtra los mensajes
     */
    fun actualizarBusqueda(texto: String) {
        _uiState.update { 
            it.copy(
                busqueda = texto,
                mensajesFiltrados = filtrarMensajes(it.mensajes, texto)
            ) 
        }
    }
    
    /**
     * Cambia la bandeja activa y carga los mensajes correspondientes
     */
    fun cambiarBandeja(tipoBandeja: TipoBandeja) {
        _uiState.update { it.copy(bandejaActiva = tipoBandeja) }
        cargarMensajes()
    }
    
    /**
     * Selecciona un mensaje para mostrar su detalle
     */
    fun seleccionarMensaje(mensaje: Mensaje) {
        viewModelScope.launch {
            // Si el mensaje no ha sido leído, marcarlo como leído
            if (!mensaje.leido && _uiState.value.bandejaActiva == TipoBandeja.RECIBIDOS) {
                try {
                    // Marcar mensaje como leído
                    mensajeRepository.toggleMensajeDestacado(mensaje.id, mensaje.destacado)
                    
                    // Actualizar UI inmediatamente
                    _uiState.update { state ->
                        val mensajesActualizados = state.mensajes.map { 
                            if (it.id == mensaje.id) it.copy(leido = true) else it 
                        }
                        state.copy(
                            mensajes = mensajesActualizados,
                            mensajesFiltrados = filtrarMensajes(mensajesActualizados, state.busqueda)
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al marcar mensaje como leído")
                }
            }
            
            _uiState.update { 
                it.copy(
                    mensajeSeleccionado = mensaje,
                    mostrarDetalle = true
                ) 
            }
        }
    }
    
    /**
     * Cierra el detalle del mensaje
     */
    fun cerrarDetalle() {
        _uiState.update { it.copy(mostrarDetalle = false) }
    }
    
    /**
     * Marca/desmarca un mensaje como destacado
     */
    fun toggleDestacado(mensaje: Mensaje) {
        viewModelScope.launch {
            try {
                mensajeRepository.toggleMensajeDestacado(mensaje.id, !mensaje.destacado)
            } catch (e: Exception) {
                Timber.e(e, "Error al actualizar estado destacado del mensaje")
                _uiState.update { it.copy(error = "Error al actualizar estado destacado") }
            }
        }
    }
    
    /**
     * Muestra la alerta de confirmación de eliminación
     */
    fun confirmarEliminarMensaje(mensaje: Mensaje) {
        _uiState.update { 
            it.copy(
                mensajeParaEliminar = mensaje,
                mostrarAlertaEliminar = true
            ) 
        }
    }
    
    /**
     * Cancela la eliminación del mensaje
     */
    fun cancelarEliminarMensaje() {
        _uiState.update { 
            it.copy(
                mensajeParaEliminar = null,
                mostrarAlertaEliminar = false
            ) 
        }
    }
    
    /**
     * Elimina un mensaje
     */
    fun eliminarMensaje() {
        val mensaje = _uiState.value.mensajeParaEliminar ?: return
        
        viewModelScope.launch {
            try {
                mensajeRepository.eliminarMensaje(mensaje.id)
                _uiState.update { 
                    it.copy(
                        mensajeParaEliminar = null,
                        mostrarAlertaEliminar = false
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar mensaje")
                _uiState.update { 
                    it.copy(
                        error = "Error al eliminar mensaje",
                        mensajeParaEliminar = null,
                        mostrarAlertaEliminar = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Limpia el error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 