package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Asistencia
import com.tfg.umeegunero.data.model.RegistroAsistencia
import com.tfg.umeegunero.data.model.Resultado
import com.tfg.umeegunero.data.repository.AsistenciaRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.ResultadoUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de asistencia
 */
data class AsistenciaUiState(
    val fechaActual: Date = Date(),
    val claseId: String = "",
    val profesorId: String = "",
    val nombreClase: String = "Clase sin nombre",
    val alumnos: List<Alumno> = emptyList(),
    val estadosAsistencia: Map<String, Asistencia> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

/**
 * ViewModel para la gestión de asistencia
 */
@HiltViewModel
class AsistenciaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val claseRepository: ClaseRepository,
    private val usuarioRepository: UsuarioRepository,
    private val asistenciaRepository: AsistenciaRepository,
    private val authRepository: AuthRepository,
    private val alumnoRepository: AlumnoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsistenciaUiState())
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    init {
        // Obtener ID de la clase de los argumentos de navegación
        val claseId = savedStateHandle.get<String>("claseId") ?: ""
        
        if (claseId.isNotEmpty()) {
            _uiState.update { it.copy(claseId = claseId, isLoading = true) }
            cargarDatosClase(claseId)
        } else {
            _uiState.update { it.copy(error = "No se especificó una clase válida") }
        }
        
        // Obtener el ID del profesor autenticado
        viewModelScope.launch {
            try {
                val usuarioActual = authRepository.getCurrentUser()
                if (usuarioActual != null) {
                    _uiState.update { it.copy(profesorId = usuarioActual.documentId) }
                } else {
                    _uiState.update { it.copy(error = "No se pudo obtener la información del profesor") }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener usuario actual")
                _uiState.update { it.copy(error = "Error al obtener información del profesor: ${e.message}") }
            }
        }
    }

    /**
     * Carga los datos de la clase y sus alumnos
     */
    private fun cargarDatosClase(claseId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener información de la clase
                val claseResult = claseRepository.getClaseById(claseId)
                
                when (claseResult) {
                    is Result.Success -> {
                        val clase = claseResult.data
                        _uiState.update { it.copy(nombreClase = clase.nombre) }
                        
                        // Obtener alumnos de la clase
                        val alumnosIds = clase.alumnosIds ?: emptyList()
                        val alumnos = mutableListOf<Alumno>()
                        
                        for (alumnoId in alumnosIds) {
                            val alumnoResultado = alumnoRepository.getAlumnoById(alumnoId)
                            
                            // Convertir de Resultado a Result si es necesario
                            val alumnoResult = when (alumnoResultado) {
                                is Resultado<*> -> ResultadoUtil.convertirResultadoAResult(alumnoResultado as Resultado<Alumno>)
                                is Result<*> -> alumnoResultado as Result<Alumno>
                                else -> Result.Error(Exception("Tipo de resultado no soportado"))
                            }
                            
                            if (alumnoResult is Result.Success) {
                                alumnoResult.data?.let { alumnos.add(it) }
                            }
                        }
                        
                        // Inicializar todos los estados de asistencia como PRESENTE por defecto
                        val estadosIniciales = alumnos.associate { alumno -> 
                            alumno.id to Asistencia.PRESENTE 
                        }
                        
                        // Cargar registro de asistencia previo para hoy si existe
                        val fechaActual = _uiState.value.fechaActual
                        val registroPrevio = asistenciaRepository.obtenerRegistroAsistencia(
                            claseId = claseId,
                            fecha = fechaActual
                        )
                        
                        val estadosFinales = if (registroPrevio != null) {
                            // Si hay un registro previo, usamos esos estados
                            registroPrevio.estadosAsistencia
                        } else {
                            estadosIniciales
                        }
                        
                        _uiState.update { it.copy(
                            alumnos = alumnos,
                            estadosAsistencia = estadosFinales,
                            isLoading = false
                        )}
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al cargar clase: ${claseResult.exception?.message}",
                            isLoading = false
                        )}
                        Timber.e(claseResult.exception, "Error al cargar clase")
                    }
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error inesperado al cargar datos: ${e.message}",
                    isLoading = false
                )}
                Timber.e(e, "Error inesperado al cargar datos")
            }
        }
    }

    /**
     * Actualiza el estado de asistencia de un alumno
     */
    fun actualizarEstadoAsistencia(alumnoId: String, nuevoEstado: Asistencia) {
        val estadosActualizados = _uiState.value.estadosAsistencia.toMutableMap()
        estadosActualizados[alumnoId] = nuevoEstado
        _uiState.update { it.copy(estadosAsistencia = estadosActualizados) }
    }

    /**
     * Guarda el registro de asistencia en Firestore
     */
    fun guardarAsistencia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val claseId = _uiState.value.claseId
                val profesorId = _uiState.value.profesorId
                val fecha = _uiState.value.fechaActual
                val estadosAsistencia = _uiState.value.estadosAsistencia
                
                if (claseId.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No se especificó una clase válida",
                        isLoading = false
                    )}
                    return@launch
                }
                
                if (profesorId.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No se pudo identificar al profesor",
                        isLoading = false
                    )}
                    return@launch
                }
                
                // Crear el objeto de registro de asistencia
                val registroAsistencia = RegistroAsistencia(
                    id = "",  // Se generará automáticamente en el repositorio
                    claseId = claseId,
                    profesorId = profesorId,
                    fecha = Timestamp(fecha),
                    estadosAsistencia = estadosAsistencia,
                    observaciones = ""
                )
                
                // Guardar en Firestore
                val resultado = asistenciaRepository.guardarRegistroAsistencia(registroAsistencia)
                
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update { it.copy(
                            mensaje = "Asistencia guardada correctamente",
                            isLoading = false
                        )}
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al guardar asistencia: ${resultado.exception?.message}",
                            isLoading = false
                        )}
                        Timber.e(resultado.exception, "Error al guardar asistencia")
                    }
                    is Result.Loading -> {
                        // Estado de carga ya actualizado
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error inesperado al guardar asistencia: ${e.message}",
                    isLoading = false
                )}
                Timber.e(e, "Error inesperado al guardar asistencia")
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
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 