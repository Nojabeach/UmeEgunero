package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.feature.familiar.screen.DetalleRegistroUiState
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.LecturaFamiliar
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.util.Date

/**
 * ViewModel para la pantalla de detalle de registro de actividad de un alumno
 */
@HiltViewModel
class DetalleRegistroViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleRegistroUiState())
    val uiState: StateFlow<DetalleRegistroUiState> = _uiState.asStateFlow()

    init {
        // Obtener el ID del registro de la navegación
        val registroId = savedStateHandle.get<String>("registroId")

        if (registroId != null) {
            cargarRegistro(registroId)
        } else {
            _uiState.update {
                it.copy(error = "No se pudo obtener el ID del registro")
            }
        }
    }

    /**
     * Carga los datos del registro de actividad
     */
    fun cargarRegistro(registroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val registroResult = usuarioRepository.getRegistroById(registroId)

                when (registroResult) {
                    is Result.Success -> {
                        val registro = registroResult.data
                        // Si hay profesor, cargar su nombre
                        var profesorNombre: String? = null
                        if (registro.profesorId.isNotBlank()) {
                            val profesorResult = usuarioRepository.getUsuarioById(registro.profesorId)
                            
                            when (profesorResult) {
                                is Result.Success<*> -> {
                                    val profesor = profesorResult.data as Usuario
                                    profesorNombre = "${profesor.nombre} ${profesor.apellidos}"
                                }
                                else -> { /* No hacer nada, es un profesor opcional */ }
                            }
                        }
                        
                        // Actualizar el estado con los datos obtenidos
                        _uiState.update { it.copy(
                            isLoading = false,
                            registro = registro,
                            profesorNombre = profesorNombre
                        ) }
                        
                        // Marcar como visto por el familiar
                        marcarComoVistoPorFamiliar(registro)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Error al cargar registro: ${registroResult.exception?.message ?: "Error desconocido"}"
                        ) }
                    }
                    is Result.Loading -> {
                        // Mantener estado de carga
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar el registro")
                _uiState.update { it.copy(
                    error = "Error inesperado: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Marca el registro como visto por el familiar actual
     */
    private fun marcarComoVistoPorFamiliar(registro: RegistroActividad) {
        viewModelScope.launch {
            try {
                // Solo actualizar si no está ya marcado como visto
                if (registro.vistoPorFamiliar) {
                    Timber.d("El registro ya fue marcado como visto por un familiar")
                    return@launch
                }
                
                // Obtener el usuario actual (familiar)
                val usuarioActual = authRepository.getCurrentUser()
                if (usuarioActual == null) {
                    Timber.e("No se pudo obtener el usuario actual para marcar el registro como visto")
                    return@launch
                }
                
                Timber.d("Marcando registro ${registro.id} como visto por familiar ${usuarioActual.dni}")
                
                // Crear la información de lectura
                val lecturaFamiliar = LecturaFamiliar(
                    familiarId = usuarioActual.dni,
                    nombreFamiliar = "${usuarioActual.nombre} ${usuarioActual.apellidos}",
                    fechaLectura = Timestamp(Date())
                )
                
                // Actualizar el registro con la información de lectura
                val registroActualizado = registro.copy(
                    vistoPorFamiliar = true,
                    fechaVisto = Timestamp(Date()),
                    // Agregar a las lecturas existentes
                    lecturasPorFamiliar = registro.lecturasPorFamiliar.toMutableMap().apply {
                        this[usuarioActual.dni] = lecturaFamiliar
                    }
                )
                
                // Guardar en la base de datos
                val resultado = registroDiarioRepository.actualizarRegistroDiario(registroActualizado)
                
                when (resultado) {
                    is Result.Success<*> -> {
                        Timber.d("Registro marcado como visto correctamente")
                        // Actualizar el estado con el registro actualizado
                        _uiState.update { it.copy(registro = registroActualizado) }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al marcar registro como visto: ${resultado.exception?.message}")
                    }
                    is Result.Loading<*> -> {
                        // No hacer nada en este caso
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al marcar registro como visto: ${e.message}")
            }
        }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}