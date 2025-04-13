package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de detalle de clase
 * 
 * Este estado contiene toda la información necesaria para representar
 * la pantalla de detalle de una clase, incluyendo la información de la clase,
 * los alumnos asignados, el profesor titular y los profesores auxiliares.
 * 
 * @property clase Datos de la clase
 * @property alumnos Lista de alumnos asignados a la clase
 * @property profesorTitular Profesor responsable principal de la clase
 * @property profesoresAuxiliares Lista de profesores auxiliares asignados a la clase
 * @property isLoading Indica si se están cargando datos
 * @property error Mensaje de error, si existe
 */
data class DetalleClaseUiState(
    val clase: Clase? = null,
    val alumnos: List<Alumno> = emptyList(),
    val profesorTitular: Usuario? = null,
    val profesoresAuxiliares: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de detalle de clase
 * 
 * Este ViewModel se encarga de cargar y gestionar todos los datos
 * relacionados con una clase específica, incluyendo sus alumnos y profesores.
 * 
 * @param savedStateHandle Handle para acceder a los argumentos de navegación
 * @param claseRepository Repositorio para operaciones con clases
 * @param alumnoRepository Repositorio para operaciones con alumnos
 * @param usuarioRepository Repositorio para operaciones con usuarios
 */
@HiltViewModel
class DetalleClaseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleClaseUiState(isLoading = true))
    val uiState: StateFlow<DetalleClaseUiState> = _uiState.asStateFlow()

    private val claseId: String = savedStateHandle.get<String>("claseId") ?: ""

    init {
        if (claseId.isNotEmpty()) {
            cargarDetallesClase(claseId)
        } else {
            _uiState.update { it.copy(
                isLoading = false,
                error = "ID de clase no especificado"
            )}
        }
    }

    /**
     * Carga todos los detalles de la clase: datos de la clase, alumnos y profesores
     * 
     * @param claseId ID de la clase a cargar
     */
    private fun cargarDetallesClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // 1. Cargar información de la clase
            when (val resultClase = claseRepository.getClaseById(claseId)) {
                is Result.Success -> {
                    val clase = resultClase.data
                    _uiState.update { it.copy(clase = clase) }
                    
                    // 2. Cargar alumnos de la clase
                    cargarAlumnos(clase.alumnosIds)
                    
                    // 3. Cargar profesor titular
                    cargarProfesorTitular(clase.profesorTitularId)
                    
                    // 4. Cargar profesores auxiliares
                    cargarProfesoresAuxiliares(clase.profesoresAuxiliaresIds)
                    
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> {
                    Timber.e(resultClase.exception, "Error al cargar la clase $claseId")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al cargar la clase: ${resultClase.exception?.message ?: "Error desconocido"}"
                    )}
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    /**
     * Carga la información de los alumnos asignados a la clase
     * 
     * @param alumnosIds Lista de IDs de alumnos a cargar
     */
    private suspend fun cargarAlumnos(alumnosIds: List<String>) {
        if (alumnosIds.isEmpty()) return
        
        try {
            val alumnos = mutableListOf<Alumno>()
            
            for (alumnoId in alumnosIds) {
                when (val result = alumnoRepository.getAlumnoById(alumnoId)) {
                    is Result.Success -> {
                        alumnos.add(result.data)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar alumno $alumnoId")
                    }
                    is Result.Loading -> { /* No action needed */ }
                }
            }
            
            _uiState.update { it.copy(alumnos = alumnos) }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar los alumnos")
        }
    }

    /**
     * Carga la información del profesor titular de la clase
     * 
     * @param profesorId ID del profesor titular
     */
    private suspend fun cargarProfesorTitular(profesorId: String) {
        if (profesorId.isEmpty()) return
        
        try {
            when (val result = usuarioRepository.obtenerUsuarioPorId(profesorId)) {
                is Result.Success -> {
                    result.data?.let { profesor ->
                        _uiState.update { it.copy(profesorTitular = profesor) }
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesor titular $profesorId")
                }
                is Result.Loading -> { /* No action needed */ }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar el profesor titular")
        }
    }

    /**
     * Carga la información de los profesores auxiliares de la clase
     * 
     * @param profesoresIds Lista de IDs de profesores auxiliares
     */
    private suspend fun cargarProfesoresAuxiliares(profesoresIds: List<String>) {
        if (profesoresIds.isEmpty()) return
        
        try {
            val profesores = mutableListOf<Usuario>()
            
            for (profesorId in profesoresIds) {
                when (val result = usuarioRepository.obtenerUsuarioPorId(profesorId)) {
                    is Result.Success -> {
                        result.data?.let { profesores.add(it) }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar profesor auxiliar $profesorId")
                    }
                    is Result.Loading -> { /* No action needed */ }
                }
            }
            
            _uiState.update { it.copy(profesoresAuxiliares = profesores) }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar los profesores auxiliares")
        }
    }
} 