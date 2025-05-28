package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.toLocalDate
import com.tfg.umeegunero.util.toLocalTime
import com.tfg.umeegunero.util.toTimestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de detalle de evento
 */
data class DetalleEventoUiState(
    val evento: Evento? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val dialogoEdicionVisible: Boolean = false,
    val navegarAtras: Boolean = false,
    val mensajeExito: String? = null
)

/**
 * ViewModel para la pantalla de detalle de evento
 * 
 * Gestiona la carga, edición y eliminación de un evento del calendario
 */
@HiltViewModel
class DetalleEventoViewModel @Inject constructor(
    private val calendarioRepository: CalendarioRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetalleEventoUiState())
    val uiState: StateFlow<DetalleEventoUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    /**
     * Carga un evento por su ID
     */
    fun cargarEvento(eventoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            
            try {
                // Obtenemos el evento directamente de Firestore por su ID
                val resultado = calendarioRepository.getEventoById(eventoId)
                
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                evento = resultado.data,
                                cargando = false,
                                error = null
                            ) 
                        }
                        Timber.d("Evento cargado: ${resultado.data.id}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                cargando = false,
                                error = resultado.exception?.message ?: "Evento no encontrado" 
                            ) 
                        }
                        Timber.e(resultado.exception, "Error al cargar el evento con ID $eventoId")
                    }
                    is Result.Loading -> { /* Estado ya actualizado */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = e.message ?: "Error al cargar el evento"
                    ) 
                }
                Timber.e(e, "Error al cargar el evento")
            }
        }
    }
    
    /**
     * Actualiza un evento existente
     */
    fun actualizarEvento(evento: Evento) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            
            try {
                val resultado = calendarioRepository.updateEvento(evento)
                
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                evento = evento,
                                cargando = false,
                                dialogoEdicionVisible = false,
                                mensajeExito = "Evento actualizado correctamente"
                            ) 
                        }
                        Timber.d("Evento actualizado: ${evento.id}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                cargando = false,
                                error = resultado.exception?.message ?: "Error al actualizar el evento"
                            ) 
                        }
                        Timber.e(resultado.exception, "Error al actualizar el evento")
                    }
                    is Result.Loading -> { /* Estado ya actualizado */ }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = e.message ?: "Error inesperado al actualizar el evento"
                    ) 
                }
                Timber.e(e, "Error inesperado al actualizar el evento")
            }
        }
    }
    
    /**
     * Elimina el evento actual
     */
    fun eliminarEvento() {
        val eventoActual = _uiState.value.evento
        if (eventoActual == null) {
            Timber.e("Intento de eliminar evento, pero no hay evento actual")
            _uiState.update { it.copy(
                error = "No se puede eliminar: no hay evento seleccionado"
            ) }
            return
        }
        
        Timber.d("Iniciando eliminación del evento: ${eventoActual.id}, título: ${eventoActual.titulo}")
        
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            
            try {
                Timber.d("Llamando a repositorio para eliminar evento: ${eventoActual.id}")
                val resultado = calendarioRepository.deleteEvento(eventoActual.id)
                
                when (resultado) {
                    is Result.Success -> {
                        Timber.d("Evento eliminado exitosamente: ${eventoActual.id}")
                        _uiState.update { 
                            it.copy(
                                cargando = false,
                                navegarAtras = true,
                                mensajeExito = "Evento eliminado correctamente"
                            ) 
                        }
                    }
                    is Result.Error -> {
                        val errorMsg = resultado.exception?.message ?: "Error desconocido al eliminar"
                        Timber.e(resultado.exception, "Error al eliminar evento: $errorMsg")
                        _uiState.update { 
                            it.copy(
                                cargando = false,
                                error = "Error al eliminar el evento: $errorMsg"
                            ) 
                        }
                    }
                    is Result.Loading -> { /* Estado ya actualizado */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al eliminar el evento: ${eventoActual.id}")
                _uiState.update { 
                    it.copy(
                        cargando = false,
                        error = "Error inesperado al eliminar el evento: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Muestra el diálogo de edición
     */
    fun mostrarDialogoEdicion() {
        _uiState.update { it.copy(dialogoEdicionVisible = true) }
    }
    
    /**
     * Oculta el diálogo de edición
     */
    fun ocultarDialogoEdicion() {
        _uiState.update { it.copy(dialogoEdicionVisible = false) }
    }
    
    /**
     * Limpia los mensajes de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Resetea el estado de navegación
     */
    fun resetearNavegacion() {
        _uiState.update { it.copy(navegarAtras = false) }
    }
} 