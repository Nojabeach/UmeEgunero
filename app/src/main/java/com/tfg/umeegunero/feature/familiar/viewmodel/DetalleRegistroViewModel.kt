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
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

/**
 * Estado de UI para la pantalla de detalle de registro
 */
data class DetalleRegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registro: RegistroActividad? = null,
    val profesorNombre: String? = null,
    val fechaSeleccionada: Date = Date(),
    val registrosDisponibles: List<Date> = emptyList(),
    val alumnoId: String? = null,
    val alumnoNombre: String? = null,
    val mostrarSelectorFecha: Boolean = false
)

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
                Timber.d("Iniciando carga del registro con ID: $registroId")
                
                // Primero intentamos obtener el registro por ID
                val registroResult = usuarioRepository.getRegistroById(registroId)

                when (registroResult) {
                    is Result.Success -> {
                        val registro = registroResult.data
                        Timber.d("Registro encontrado correctamente: ${registro.id}")
                        
                        // Si hay profesor, cargar su nombre
                        var profesorNombre: String? = null
                        if (registro.profesorId.isNotBlank()) {
                            try {
                                val profesorResult = usuarioRepository.getUsuarioById(registro.profesorId)
                                
                                if (profesorResult is Result.Success) {
                                    val profesor = profesorResult.data
                                    profesorNombre = "${profesor.nombre} ${profesor.apellidos}"
                                    Timber.d("Nombre del profesor cargado: $profesorNombre")
                                } else { 
                                    Timber.w("No se pudo cargar el profesor con ID: ${registro.profesorId}")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al cargar información del profesor: ${e.message}")
                            }
                        }
                        
                        // Actualizar el estado con los datos obtenidos
                        _uiState.update { it.copy(
                            isLoading = false,
                            registro = registro,
                            profesorNombre = profesorNombre,
                            fechaSeleccionada = registro.fecha.toDate(),
                            alumnoId = registro.alumnoId,
                            alumnoNombre = registro.alumnoNombre,
                            error = null
                        ) }
                        
                        // Cargar fechas disponibles para este alumno
                        if (registro.alumnoId.isNotBlank()) {
                            cargarFechasDisponibles(registro.alumnoId)
                        }
                        
                        // Marcar como visto por el familiar
                        try {
                            marcarComoVistoPorFamiliar(registro)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al marcar como visto: ${e.message}")
                            // No mostramos este error al usuario ya que no es crítico
                        }
                    }
                    is Result.Error -> {
                        Timber.e(registroResult.exception, "Error al cargar registro: ${registroResult.exception?.message}")
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Error al cargar registro: ${registroResult.exception?.message ?: "Error desconocido"}"
                        ) }
                        
                        // Intentar una segunda estrategia: buscar por ID en RegistroDiarioRepository
                        try {
                            Timber.d("Intentando cargar registro con estrategia alternativa")
                            val resultadoAlternativo = registroDiarioRepository.obtenerRegistroDiarioPorId(registroId)
                            
                            if (resultadoAlternativo is Result.Success && resultadoAlternativo.data != null) {
                                Timber.d("Registro encontrado con estrategia alternativa")
                                val registro = resultadoAlternativo.data
                                
                                _uiState.update { it.copy(
                                    isLoading = false,
                                    registro = registro,
                                    fechaSeleccionada = registro.fecha.toDate(),
                                    alumnoId = registro.alumnoId,
                                    alumnoNombre = registro.alumnoNombre,
                                    error = null
                                ) }
                                
                                // Cargar fechas disponibles para este alumno
                                if (registro.alumnoId.isNotBlank()) {
                                    cargarFechasDisponibles(registro.alumnoId)
                                }
                                
                                marcarComoVistoPorFamiliar(registro)
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error en estrategia alternativa: ${e.message}")
                            // No actualizamos el estado ya que ya mostramos el error principal
                        }
                    }
                    is Result.Loading -> {
                        // Mantener estado de carga
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar el registro: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error inesperado: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga las fechas en que hay registros disponibles para un alumno
     */
    private fun cargarFechasDisponibles(alumnoId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Cargando fechas disponibles para alumno: $alumnoId")
                
                // Obtener todos los registros del alumno
                val result = registroDiarioRepository.obtenerRegistrosPorAlumno(alumnoId)
                
                when (result) {
                    is Result.Success<List<RegistroActividad>> -> {
                        val registros = result.data
                        val fechas = registros.map { it.fecha.toDate() }.sortedDescending()
                        
                        _uiState.update { it.copy(
                            registrosDisponibles = fechas
                        )}
                        
                        Timber.d("Fechas disponibles cargadas: ${fechas.size}")
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar fechas disponibles: ${result.message}")
                    }
                    is Result.Loading -> {
                        // Estado de carga, no hacemos nada
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar fechas disponibles: ${e.message}")
            }
        }
    }

    /**
     * Marca un registro como visto por el familiar actual
     */
    private suspend fun marcarComoVistoPorFamiliar(registro: RegistroActividad) {
        try {
            val usuarioActual = authRepository.getFirebaseUser()
            
            if (usuarioActual != null) {
                val uid = usuarioActual.uid
                
                // Crear registro de lectura
                val lecturaFamiliar = LecturaFamiliar(
                    familiarId = uid,
                    registroId = registro.id,
                    alumnoId = registro.alumnoId,
                    fechaLectura = Timestamp.now()
                )
                
                // Guardar la lectura
                val resultado = registroDiarioRepository.registrarLecturaFamiliar(lecturaFamiliar)
                
                if (resultado is Result.Success) {
                    Timber.d("Registro marcado como visto correctamente")
                } else if (resultado is Result.Error) {
                    Timber.e(resultado.exception, "Error al marcar registro como visto: ${resultado.message}")
                }
            } else {
                Timber.w("No se pudo obtener el usuario actual para marcar como visto")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como visto: ${e.message}")
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Maneja la selección de una fecha para cargar el registro correspondiente
     */
    fun seleccionarFecha(fecha: Date) {
        val currentAlumnoId = _uiState.value.alumnoId ?: return
        val currentAlumnoNombre = _uiState.value.alumnoNombre ?: return
        
        _uiState.update { it.copy(
            fechaSeleccionada = fecha,
            mostrarSelectorFecha = false
        )}
        
        cargarRegistroPorFecha(currentAlumnoId, fecha, currentAlumnoNombre)
    }

    /**
     * Abre o cierra el selector de fecha
     */
    fun toggleSelectorFecha() {
        _uiState.update { it.copy(
            mostrarSelectorFecha = !it.mostrarSelectorFecha
        )}
    }

    /**
     * Carga un registro para una fecha específica
     */
    private fun cargarRegistroPorFecha(alumnoId: String, fecha: Date, alumnoNombre: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = registroDiarioRepository.obtenerRegistroPorAlumnoYFecha(alumnoId, fecha)
                
                when (result) {
                    is Result.Success<RegistroActividad> -> {
                        val registro = result.data
                        
                        // Si hay profesor, cargar su nombre
                        var profesorNombre: String? = null
                        if (registro.profesorId.isNotBlank()) {
                            try {
                                val profesorResult = usuarioRepository.getUsuarioById(registro.profesorId)
                                
                                if (profesorResult is Result.Success) {
                                    val profesor = profesorResult.data
                                    profesorNombre = "${profesor.nombre} ${profesor.apellidos}"
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al cargar información del profesor: ${e.message}")
                            }
                        }
                        
                        _uiState.update { it.copy(
                            isLoading = false,
                            registro = registro,
                            profesorNombre = profesorNombre,
                            error = null
                        )}
                        
                        // Marcar como visto por el familiar
                        marcarComoVistoPorFamiliar(registro)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar registro por fecha: ${result.message}")
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "No se encontró registro para la fecha seleccionada."
                        )}
                    }
                    is Result.Loading -> {
                        // Mantener estado de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registro por fecha: ${e.message}")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar el registro: ${e.message}"
                )}
            }
        }
    }
}