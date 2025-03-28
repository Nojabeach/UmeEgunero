package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.ComunicadoRepository
import com.tfg.umeegunero.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de comunicados
 */
data class ComunicadosUiState(
    val comunicados: List<Comunicado> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

/**
 * ViewModel para la gestión de comunicados generales
 */
@HiltViewModel
class ComunicadosViewModel @Inject constructor(
    private val comunicadoRepository: ComunicadoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComunicadosUiState(isLoading = true))
    val uiState: StateFlow<ComunicadosUiState> = _uiState.asStateFlow()

    /**
     * Carga la lista de comunicados
     */
    fun cargarComunicados() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = comunicadoRepository.getComunicados()) {
                    is Result.Success -> {
                        val comunicadosOrdenados = result.data.sortedByDescending { it.fechaCreacion }
                        _uiState.update { 
                            it.copy(
                                comunicados = comunicadosOrdenados,
                                isLoading = false
                            ) 
                        }
                        Timber.d("Comunicados cargados: ${result.data.size}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar comunicados: ${result.exception.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al cargar comunicados")
                    }
                    is Result.Loading -> {
                        // Este estado ya lo manejamos al inicio
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al cargar comunicados")
            }
        }
    }

    /**
     * Crea un nuevo comunicado
     */
    fun crearComunicado(
        titulo: String, 
        mensaje: String, 
        tiposDestinatarios: List<TipoUsuario>
    ) {
        if (titulo.isBlank() || mensaje.isBlank() || tiposDestinatarios.isEmpty()) {
            _uiState.update { 
                it.copy(error = "Todos los campos son obligatorios") 
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val nuevoComunicado = Comunicado(
                    id = UUID.randomUUID().toString(),
                    titulo = titulo,
                    mensaje = mensaje,
                    tiposDestinatarios = tiposDestinatarios,
                    fechaCreacion = Timestamp(Date()),
                    creadoPor = "admin", // TODO: Obtener usuario actual
                    activo = true
                )
                
                when (val result = comunicadoRepository.crearComunicado(nuevoComunicado)) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                success = "Comunicado creado correctamente"
                            ) 
                        }
                        cargarComunicados() // Recargamos la lista
                        Timber.d("Comunicado creado con ID: ${nuevoComunicado.id}")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al crear comunicado: ${result.exception.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al crear comunicado")
                    }
                    is Result.Loading -> {
                        // Este estado ya lo manejamos al inicio
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al crear comunicado")
            }
        }
    }

    /**
     * Elimina un comunicado por su ID
     */
    fun eliminarComunicado(comunicadoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (val result = comunicadoRepository.eliminarComunicado(comunicadoId)) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                success = "Comunicado eliminado correctamente"
                            ) 
                        }
                        cargarComunicados() // Recargamos la lista
                        Timber.d("Comunicado eliminado con ID: $comunicadoId")
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                error = "Error al eliminar comunicado: ${result.exception.message}",
                                isLoading = false
                            ) 
                        }
                        Timber.e(result.exception, "Error al eliminar comunicado")
                    }
                    is Result.Loading -> {
                        // Este estado ya lo manejamos al inicio
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error inesperado al eliminar comunicado")
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Limpia el mensaje de éxito
     */
    fun clearSuccess() {
        _uiState.update { it.copy(success = null) }
    }
} 