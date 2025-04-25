package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.util.UsuarioUtils
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
    val centros: List<Centro> = emptyList(),
    val isAdminApp: Boolean = false,
    val activo: Boolean = true,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nombreError: String? = null,
    val descripcionError: String? = null,
    val edadMinimaError: String? = null,
    val edadMaximaError: String? = null,
    val anioAcademicoError: String? = null,
    val centroError: String? = null,
    val isEditMode: Boolean = false
)

/**
 * ViewModel para la pantalla de añadir/editar curso
 * Maneja la lógica de validación y guardado de cursos
 */
@HiltViewModel
class AddCursoViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val centroRepository: CentroRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddCursoUiState())
    val uiState: StateFlow<AddCursoUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Determinar si el usuario es admin de la app
            val usuario = usuarioRepository.obtenerUsuarioActual()
            val isAdminApp = usuario?.perfiles?.any { it.tipo == TipoUsuario.ADMIN_APP } ?: false
            _uiState.update { it.copy(isAdminApp = isAdminApp) }

            if (isAdminApp) {
                // Si es admin de la app, cargar la lista de centros
                cargarCentros()
            } else {
                // Si no es admin, obtener el centro del usuario actual
                val centroId = UsuarioUtils.obtenerCentroIdDelUsuarioActual(authRepository, usuarioRepository)
                if (!centroId.isNullOrEmpty()) {
                    _uiState.update { it.copy(centroId = centroId) }
                    Timber.d("CentroId inicial establecido: $centroId")
                } else {
                    Timber.e("No se pudo obtener el centroId del usuario actual")
                }
            }
            
            // Verificar si estamos en modo edición
            val cursoId = savedStateHandle.get<String>("cursoId")
            if (!cursoId.isNullOrEmpty()) {
                cargarCurso(cursoId)
            }
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
                            error = "Error al cargar curso: ${result.exception?.message}",
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
     * Carga la lista de centros educativos
     */
    private suspend fun cargarCentros() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            centroRepository.getCentros().collect { centros ->
                _uiState.update { 
                    it.copy(
                        centros = centros,
                        isLoading = false
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar centros: ${e.message}")
            _uiState.update { 
                it.copy(
                    error = "Error al cargar centros: ${e.message}",
                    isLoading = false
                )
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
                                error = "Error al guardar curso: ${result.exception?.message}"
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
     * Valida el formulario antes de guardar
     */
    private fun validarFormulario(): Boolean {
        var isValid = true
        val state = _uiState.value

        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(nombreError = "El nombre es obligatorio") }
            isValid = false
        }

        if (state.descripcion.isBlank()) {
            _uiState.update { it.copy(descripcionError = "La descripción es obligatoria") }
            isValid = false
        }

        if (state.edadMinima.isBlank() || state.edadMinima.toIntOrNull() == null) {
            _uiState.update { it.copy(edadMinimaError = "La edad mínima debe ser un número válido") }
            isValid = false
        }

        if (state.edadMaxima.isBlank() || state.edadMaxima.toIntOrNull() == null) {
            _uiState.update { it.copy(edadMaximaError = "La edad máxima debe ser un número válido") }
            isValid = false
        }

        if (state.anioAcademico.isBlank()) {
            _uiState.update { it.copy(anioAcademicoError = "El año académico es obligatorio") }
            isValid = false
        }

        if (state.isAdminApp && state.centroId.isBlank()) {
            _uiState.update { it.copy(centroError = "Debe seleccionar un centro") }
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