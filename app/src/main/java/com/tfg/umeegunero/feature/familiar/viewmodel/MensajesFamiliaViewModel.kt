package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.MensajeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de mensajes de familia.
 * Gestiona el estado de la UI y la lógica de negocio relacionada con los mensajes.
 */
@HiltViewModel
class MensajesFamiliaViewModel @Inject constructor(
    private val mensajeRepository: MensajeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MensajesFamiliaUiState())
    val uiState: StateFlow<MensajesFamiliaUiState> = _uiState.asStateFlow()

    /**
     * Carga los mensajes del usuario actual desde el repositorio.
     */
    fun cargarMensajes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val usuarioActual = authRepository.getUsuarioActual()
                if (usuarioActual != null) {
                    val mensajes = mensajeRepository.getMensajesForUsuario(usuarioActual.dni)
                    _uiState.update { it.copy(
                        mensajes = mensajes.filterNotNull(),
                        isLoading = false,
                        error = null
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No se ha podido obtener el usuario actual"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar los mensajes: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Selecciona un mensaje para mostrar su detalle y lo marca como leído si es necesario.
     */
    fun seleccionarMensaje(mensaje: Mensaje) {
        viewModelScope.launch {
            // Si el mensaje no está leído, lo marcamos como leído
            if (!mensaje.leido) {
                try {
                    val usuarioActual = authRepository.getUsuarioActual()
                    if (usuarioActual != null) {
                        val mensajeActualizado = mensajeRepository.marcarMensajeComoLeido(
                            mensajeId = mensaje.id,
                            usuarioDni = usuarioActual.dni
                        )
                        
                        // Actualizamos la lista de mensajes
                        _uiState.update { 
                            val mensajesActualizados = it.mensajes.map { m ->
                                if (m.id == mensaje.id) mensajeActualizado ?: m else m
                            }
                            it.copy(
                                mensajes = mensajesActualizados,
                                mensajeSeleccionado = mensajeActualizado
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                        error = "Error al marcar el mensaje como leído: ${e.message}"
                    ) }
                }
            } else {
                // Si ya está leído, simplemente lo seleccionamos
                _uiState.update { it.copy(mensajeSeleccionado = mensaje) }
            }
            
            // Mostramos el detalle
            _uiState.update { it.copy(mostrarDetalle = true) }
        }
    }

    /**
     * Cierra el detalle del mensaje seleccionado.
     */
    fun cerrarDetalle() {
        _uiState.update { it.copy(mostrarDetalle = false) }
    }

    /**
     * Cambia el estado de destacado de un mensaje.
     */
    fun toggleDestacado(mensaje: Mensaje) {
        viewModelScope.launch {
            try {
                val usuarioActual = authRepository.getUsuarioActual()
                if (usuarioActual != null) {
                    val nuevoEstado = !mensaje.destacado
                    val mensajeActualizado = mensajeRepository.toggleMensajeDestacado(
                        mensajeId = mensaje.id,
                        usuarioDni = usuarioActual.dni,
                        destacado = nuevoEstado
                    )
                    
                    // Actualizamos la lista de mensajes
                    _uiState.update { 
                        val mensajesActualizados = it.mensajes.map { m ->
                            if (m.id == mensaje.id) mensajeActualizado ?: m else m
                        }
                        it.copy(
                            mensajes = mensajesActualizados,
                            mensajeSeleccionado = if (it.mensajeSeleccionado?.id == mensaje.id) 
                                mensajeActualizado else it.mensajeSeleccionado
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al actualizar el mensaje: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Prepara un mensaje para ser eliminado, mostrando el diálogo de confirmación.
     */
    fun confirmarEliminarMensaje(mensaje: Mensaje) {
        _uiState.update { it.copy(mensajeParaEliminar = mensaje) }
    }

    /**
     * Cancela la eliminación del mensaje, cerrando el diálogo de confirmación.
     */
    fun cancelarEliminarMensaje() {
        _uiState.update { it.copy(mensajeParaEliminar = null) }
    }

    /**
     * Elimina el mensaje seleccionado.
     */
    fun eliminarMensaje() {
        val mensajeParaEliminar = _uiState.value.mensajeParaEliminar ?: return
        
        viewModelScope.launch {
            try {
                val usuarioActual = authRepository.getUsuarioActual()
                if (usuarioActual != null) {
                    val exito = mensajeRepository.eliminarMensaje(
                        mensajeId = mensajeParaEliminar.id,
                        usuarioDni = usuarioActual.dni
                    )
                    
                    if (exito) {
                        // Actualizamos la lista de mensajes
                        _uiState.update { 
                            val mensajesActualizados = it.mensajes.filter { m ->
                                m.id != mensajeParaEliminar.id
                            }
                            it.copy(
                                mensajes = mensajesActualizados,
                                mensajeParaEliminar = null,
                                // Si el mensaje eliminado era el seleccionado, cerramos el detalle
                                mostrarDetalle = if (it.mensajeSeleccionado?.id == mensajeParaEliminar.id) 
                                    false else it.mostrarDetalle,
                                mensajeSeleccionado = if (it.mensajeSeleccionado?.id == mensajeParaEliminar.id) 
                                    null else it.mensajeSeleccionado
                            )
                        }
                    } else {
                        _uiState.update { it.copy(
                            error = "No se ha podido eliminar el mensaje",
                            mensajeParaEliminar = null
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al eliminar el mensaje: ${e.message}",
                    mensajeParaEliminar = null
                ) }
            }
        }
    }

    /**
     * Actualiza el filtro para mostrar solo mensajes no leídos.
     */
    fun actualizarFiltroNoLeidos(mostrarSoloNoLeidos: Boolean) {
        _uiState.update { it.copy(
            mostrarSoloNoLeidos = mostrarSoloNoLeidos,
            // Si activamos este filtro, desactivamos el otro para evitar confusión
            mostrarSoloDestacados = if (mostrarSoloNoLeidos) false else it.mostrarSoloDestacados
        ) }
    }

    /**
     * Actualiza el filtro para mostrar solo mensajes destacados.
     */
    fun actualizarFiltroDestacados(mostrarSoloDestacados: Boolean) {
        _uiState.update { it.copy(
            mostrarSoloDestacados = mostrarSoloDestacados,
            // Si activamos este filtro, desactivamos el otro para evitar confusión
            mostrarSoloNoLeidos = if (mostrarSoloDestacados) false else it.mostrarSoloNoLeidos
        ) }
    }
}

/**
 * Estado de la UI para la pantalla de mensajes de familia.
 */
data class MensajesFamiliaUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val mostrarSoloNoLeidos: Boolean = false,
    val mostrarSoloDestacados: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajeSeleccionado: Mensaje? = null,
    val mostrarDetalle: Boolean = false,
    val mensajeParaEliminar: Mensaje? = null
) {
    /**
     * Lista de mensajes filtrados según los filtros aplicados.
     */
    val mensajesFiltrados: List<Mensaje> = when {
        mostrarSoloNoLeidos -> mensajes.filter { !it.leido }
        mostrarSoloDestacados -> mensajes.filter { it.destacado }
        else -> mensajes
    }
    
    /**
     * Indica si hay filtros activos.
     */
    val filtrosActivos: Boolean = mostrarSoloNoLeidos || mostrarSoloDestacados
} 