package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.IncidenciaEntity
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.IncidenciaRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Estado UI para la pantalla de creación de incidencias
 */
data class IncidenciaUiState(
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val esUrgente: Boolean = false,
    val enviarNotificacion: Boolean = true,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val alumnos: List<Pair<String, String>> = emptyList(),
    val profesorId: String = "",
    val profesorNombre: String = ""
)

/**
 * ViewModel para la gestión de incidencias por parte del profesor
 */
@HiltViewModel
class IncidenciaProfesorViewModel @Inject constructor(
    private val incidenciaRepository: IncidenciaRepository,
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IncidenciaUiState())
    val uiState: StateFlow<IncidenciaUiState> = _uiState.asStateFlow()
    
    init {
        cargarDatosIniciales()
    }
    
    /**
     * Carga los datos iniciales: profesor actual y lista de alumnos disponibles
     */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                // Obtener información del profesor actual
                val profesor = usuarioRepository.getCurrentUser()
                
                if (profesor != null) {
                    _uiState.update { 
                        it.copy(
                            profesorId = profesor.dni,
                            profesorNombre = profesor.nombre
                        )
                    }
                    
                    // Cargar lista de alumnos para el profesor
                    val alumnos = alumnoRepository.getAlumnosForProfesor(profesor.dni)
                    
                    val alumnosList = alumnos.map { alumno ->
                        Pair(alumno.id, alumno.nombreCompleto)
                    }
                    
                    _uiState.update { it.copy(alumnos = alumnosList) }
                } else {
                    _uiState.update { 
                        it.copy(error = "No se pudo obtener información del profesor") 
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos iniciales")
                _uiState.update { 
                    it.copy(error = "Error al cargar datos: ${e.message}") 
                }
            }
        }
    }
    
    /**
     * Actualiza el campo de título de la incidencia
     */
    fun onTituloChange(titulo: String) {
        _uiState.update { it.copy(titulo = titulo) }
    }
    
    /**
     * Actualiza el campo de descripción de la incidencia
     */
    fun onDescripcionChange(descripcion: String) {
        _uiState.update { it.copy(descripcion = descripcion) }
    }
    
    /**
     * Actualiza el alumno seleccionado
     */
    fun onAlumnoSelected(alumnoId: String, alumnoNombre: String) {
        _uiState.update { 
            it.copy(alumnoId = alumnoId, alumnoNombre = alumnoNombre) 
        }
    }
    
    /**
     * Actualiza la opción de urgencia
     */
    fun onUrgenteChange(esUrgente: Boolean) {
        _uiState.update { it.copy(esUrgente = esUrgente) }
    }
    
    /**
     * Actualiza la opción de enviar notificación
     */
    fun onEnviarNotificacionChange(enviar: Boolean) {
        _uiState.update { it.copy(enviarNotificacion = enviar) }
    }
    
    /**
     * Guarda la incidencia y envía la notificación si está habilitada
     */
    fun guardarIncidencia() {
        val currentState = _uiState.value
        
        // Validar datos
        if (currentState.alumnoId.isBlank()) {
            _uiState.update { it.copy(error = "Debe seleccionar un alumno") }
            return
        }
        
        if (currentState.titulo.isBlank()) {
            _uiState.update { it.copy(error = "El título no puede estar vacío") }
            return
        }
        
        if (currentState.descripcion.isBlank()) {
            _uiState.update { it.copy(error = "La descripción no puede estar vacía") }
            return
        }
        
        // Iniciar proceso de guardado
        _uiState.update { 
            it.copy(isSaving = true, error = null) 
        }
        
        viewModelScope.launch {
            try {
                // Crear objeto de incidencia
                val incidencia = IncidenciaEntity(
                    id = UUID.randomUUID().toString(),
                    alumnoId = currentState.alumnoId,
                    profesorId = currentState.profesorId,
                    titulo = currentState.titulo,
                    descripcion = currentState.descripcion,
                    fecha = Timestamp.now(),
                    urgente = currentState.esUrgente,
                    estado = "PENDIENTE",
                    fechaResolucion = null
                )
                
                // Guardar en la base de datos
                incidenciaRepository.createIncidencia(incidencia)
                
                // Enviar notificación si está habilitado
                if (currentState.enviarNotificacion) {
                    notificationHelper.enviarNotificacionIncidencia(
                        alumnoId = currentState.alumnoId,
                        profesorId = currentState.profesorId,
                        titulo = if (currentState.esUrgente) "¡URGENTE! ${currentState.titulo}" else currentState.titulo,
                        mensaje = currentState.descripcion,
                        urgente = currentState.esUrgente
                    ) { exito, mensaje ->
                        if (!exito) {
                            Timber.e("Error al enviar notificación: $mensaje")
                        }
                    }
                }
                
                // Actualizar estado
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        isSuccess = true,
                        titulo = "",
                        descripcion = "",
                        esUrgente = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar incidencia")
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        error = "Error al guardar incidencia: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Limpia el estado de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Reinicia el estado de éxito
     */
    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
} 