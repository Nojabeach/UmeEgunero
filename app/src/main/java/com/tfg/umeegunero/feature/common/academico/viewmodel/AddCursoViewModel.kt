package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
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
 * Estado UI para la pantalla de añadir/editar curso
 * Contiene todos los campos del formulario y sus posibles errores
 */
data class AddCursoUiState(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val edadMinima: String = "",
    val edadMaxima: String = "",
    val anioAcademico: String = "",
    val centroId: String = "",
    val activo: Boolean = true,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nombreError: String? = null,
    val descripcionError: String? = null,
    val edadMinimaError: String? = null,
    val edadMaximaError: String? = null,
    val anioAcademicoError: String? = null,
    val isEditMode: Boolean = false
)

/**
 * ViewModel para la pantalla de añadir/editar curso
 * Maneja la lógica de validación y guardado de cursos
 */
@HiltViewModel
class AddCursoViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddCursoUiState())
    val uiState: StateFlow<AddCursoUiState> = _uiState.asStateFlow()
    
    init {
        // Obtener el centro del usuario actual
        viewModelScope.launch {
            val centroId = obtenerCentroIdDelUsuarioActual()
            if (centroId != null) {
                _uiState.update { it.copy(centroId = centroId) }
            }
            
            // Verificar si estamos en modo edición
            val cursoId = savedStateHandle.get<String>("cursoId")
            if (!cursoId.isNullOrEmpty()) {
                cargarCurso(cursoId)
            }
        }
    }
    
    /**
     * Obtiene el ID del centro del usuario actual
     */
    private suspend fun obtenerCentroIdDelUsuarioActual(): String? {
        // Obtener el usuario actual de Firebase Auth
        val firebaseUser = usuarioRepository.auth.currentUser
        if (firebaseUser == null) {
            Timber.d("No hay usuario autenticado")
            return null
        }
        
        try {
            // Obtener el perfil del usuario directamente desde Firestore
            // Asumimos que existe un método que obtiene el usuario por uid
            val usuarioId = firebaseUser.uid
            when (val result = usuarioRepository.getUsuarioById(usuarioId)) {
                is Result.Success -> {
                    val usuario = result.data
                    // Obtener el primer centroId de los perfiles del usuario
                    val centroId = usuario.perfiles.firstOrNull()?.centroId
                    Timber.d("Centro ID obtenido: $centroId")
                    return centroId
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al obtener usuario")
                    return null
                }
                is Result.Loading -> {
                    Timber.d("Cargando usuario...")
                    return null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro del usuario")
            return null
        }
    }
    
    /**
     * Carga los datos de un curso existente para edición
     */
    private fun cargarCurso(cursoId: String) {
        _uiState.update { it.copy(isLoading = true, isEditMode = true) }
        
        viewModelScope.launch {
            when (val result = cursoRepository.getCursoById(cursoId)) {
                is Result.Success -> {
                    val curso = result.data
                    _uiState.update { 
                        it.copy(
                            id = curso.id,
                            nombre = curso.nombre,
                            descripcion = curso.descripcion,
                            edadMinima = curso.edadMinima.toString(),
                            edadMaxima = curso.edadMaxima.toString(),
                            anioAcademico = curso.anioAcademico,
                            activo = curso.activo,
                            centroId = curso.centroId,
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar curso: ${result.exception.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya fue establecido
                }
            }
        }
    }
    
    /**
     * Actualiza el ID del centro educativo
     */
    fun updateCentroId(centroId: String) {
        _uiState.update { it.copy(centroId = centroId) }
    }
    
    /**
     * Actualiza el nombre del curso
     */
    fun updateNombre(nombre: String) {
        _uiState.update { 
            it.copy(
                nombre = nombre,
                nombreError = null
            )
        }
    }
    
    /**
     * Actualiza la descripción del curso
     */
    fun updateDescripcion(descripcion: String) {
        _uiState.update { 
            it.copy(
                descripcion = descripcion,
                descripcionError = null
            )
        }
    }
    
    /**
     * Actualiza la edad mínima del curso
     */
    fun updateEdadMinima(edadMinima: String) {
        _uiState.update { 
            it.copy(
                edadMinima = edadMinima,
                edadMinimaError = null
            )
        }
    }
    
    /**
     * Actualiza la edad máxima del curso
     */
    fun updateEdadMaxima(edadMaxima: String) {
        _uiState.update { 
            it.copy(
                edadMaxima = edadMaxima,
                edadMaximaError = null
            )
        }
    }
    
    /**
     * Actualiza el año académico del curso
     */
    fun updateAnioAcademico(anioAcademico: String) {
        _uiState.update { 
            it.copy(
                anioAcademico = anioAcademico,
                anioAcademicoError = null
            )
        }
    }
    
    /**
     * Actualiza el estado activo del curso
     */
    fun updateActivo(activo: Boolean) {
        _uiState.update { it.copy(activo = activo) }
    }
    
    /**
     * Guarda el curso en la base de datos
     */
    fun guardarCurso() {
        if (!validarFormulario()) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val state = _uiState.value
                
                // Convertir las edades a enteros
                val edadMinima = state.edadMinima.toIntOrNull() ?: 0
                val edadMaxima = state.edadMaxima.toIntOrNull() ?: 0
                
                // Crear objeto curso
                val curso = Curso(
                    id = if (state.isEditMode) state.id else UUID.randomUUID().toString(),
                    nombre = state.nombre,
                    descripcion = state.descripcion,
                    edadMinima = edadMinima,
                    edadMaxima = edadMaxima,
                    anioAcademico = state.anioAcademico,
                    centroId = state.centroId,
                    fechaCreacion = Timestamp.now(),
                    activo = state.activo,
                    clases = emptyList() // Inicialmente sin clases
                )
                
                // Guardar el curso
                when (val result = if (state.isEditMode) {
                    cursoRepository.modificarCurso(curso)
                } else {
                    cursoRepository.agregarCurso(curso)
                }) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isSuccess = true,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al guardar curso: ${result.exception.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Estado de carga ya fue establecido
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al procesar datos del curso")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al procesar datos: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Valida todos los campos del formulario antes de guardar
     * @return true si todos los campos son válidos
     */
    private fun validarFormulario(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        // Validar nombre
        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre es obligatorio") }
            isValid = false
        }
        
        // Validar edades
        val edadMinima = state.edadMinima.toIntOrNull()
        if (edadMinima == null) {
            _uiState.update { it.copy(edadMinimaError = "Introduce una edad válida") }
            isValid = false
        }
        
        val edadMaxima = state.edadMaxima.toIntOrNull()
        if (edadMaxima == null) {
            _uiState.update { it.copy(edadMaximaError = "Introduce una edad válida") }
            isValid = false
        }
        
        // Comprobar que la edad mínima no sea mayor que la máxima
        if (edadMinima != null && edadMaxima != null && edadMinima > edadMaxima) {
            _uiState.update { 
                it.copy(
                    edadMinimaError = "La edad mínima no puede ser mayor que la máxima",
                    edadMaximaError = "La edad máxima no puede ser menor que la mínima"
                ) 
            }
            isValid = false
        }
        
        // Validar año académico
        if (state.anioAcademico.isBlank()) {
            _uiState.update { it.copy(anioAcademicoError = "El año académico es obligatorio") }
            isValid = false
        } else if (!state.anioAcademico.matches(Regex("^\\d{4}-\\d{4}$"))) {
            _uiState.update { it.copy(anioAcademicoError = "Formato incorrecto. Debe ser YYYY-YYYY") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Limpia el error general del estado
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 