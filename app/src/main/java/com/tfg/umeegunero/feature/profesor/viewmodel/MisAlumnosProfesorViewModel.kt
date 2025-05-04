package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla "Mis Alumnos".
 */
data class MisAlumnosUiState(
    val isLoading: Boolean = false,
    val alumnos: List<Alumno> = emptyList(),
    val error: String? = null,
    val clase: String = ""
)

/**
 * ViewModel para la pantalla "Mis Alumnos" del profesor.
 *
 * Gestiona la carga y filtrado de alumnos asignados al profesor.
 * Proporciona acceso a la información detallada de cada alumno,
 * incluyendo sus vinculaciones familiares.
 */
@HiltViewModel
class MisAlumnosProfesorViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val profesorRepository: ProfesorRepository,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository,
    private val calendarioRepository: CalendarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisAlumnosUiState())
    val uiState: StateFlow<MisAlumnosUiState> = _uiState.asStateFlow()

    init {
        cargarAlumnos()
    }

    /**
     * Carga la lista de alumnos asignados al profesor actual
     */
    private fun cargarAlumnos() {
        viewModelScope.launch {
            try {
                _uiState.value = MisAlumnosUiState(isLoading = true)
                
                // Obtener ID del usuario actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.value = MisAlumnosUiState(
                        isLoading = false,
                        error = "No se pudo obtener el usuario actual"
                    )
                    return@launch
                }
                
                // Obtener datos del profesor
                val profesor = profesorRepository.getProfesorPorUsuarioId(usuario.dni)
                if (profesor == null) {
                    _uiState.value = MisAlumnosUiState(
                        isLoading = false,
                        error = "No se encontró información del profesor"
                    )
                    return@launch
                }
                
                // Lista para almacenar todos los alumnos
                val todosLosAlumnos = mutableListOf<Alumno>()
                var nombreClase = ""
                
                // 1. Obtener alumnos donde este profesor es el asignado directamente
                Timber.d("Buscando alumnos con profesor asignado: ${profesor.id}")
                val alumnosDirectos = alumnoRepository.getAlumnosForProfesor(profesor.id)
                todosLosAlumnos.addAll(alumnosDirectos)
                Timber.d("Encontrados ${alumnosDirectos.size} alumnos asignados directamente")
                
                // 2. Obtener clases asignadas al profesor
                val clasesResult = claseRepository.getClasesByProfesor(profesor.id)
                if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                    // Para cada clase, obtener sus alumnos
                    for (clase in clasesResult.data) {
                        // Guardamos el nombre de la primera clase (para simplificar)
                        if (nombreClase.isEmpty()) {
                            nombreClase = clase.nombre
                        }
                        
                        val alumnosResult = alumnoRepository.getAlumnosByClaseId(clase.id)
                        if (alumnosResult is Result.Success) {
                            todosLosAlumnos.addAll(alumnosResult.data)
                            Timber.d("Añadidos ${alumnosResult.data.size} alumnos de la clase ${clase.nombre}")
                        }
                    }
                } else {
                    Timber.d("No hay clases asignadas al profesor ${profesor.id}")
                }
                
                // Eliminar duplicados
                val alumnosSinDuplicados = todosLosAlumnos.distinctBy { it.dni }
                
                // Si no hay alumnos, mostrar mensaje específico
                if (alumnosSinDuplicados.isEmpty()) {
                    _uiState.value = MisAlumnosUiState(
                        isLoading = false,
                        error = "No hay alumnos asignados a este profesor",
                        alumnos = emptyList(),
                        clase = nombreClase
                    )
                } else {
                    _uiState.value = MisAlumnosUiState(
                        isLoading = false,
                        alumnos = alumnosSinDuplicados,
                        clase = nombreClase
                    )
                    Timber.d("Cargados ${alumnosSinDuplicados.size} alumnos para el profesor")
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar alumnos")
                _uiState.value = MisAlumnosUiState(
                    isLoading = false,
                    error = "Error al cargar alumnos: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Refresca la lista de alumnos
     */
    fun refrescarAlumnos() {
        cargarAlumnos()
    }
} 