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
                
                Timber.d("Cargando alumnos para usuario con DNI: ${usuario.dni}")
                
                // Obtener datos del profesor
                val profesor = profesorRepository.getProfesorPorUsuarioId(usuario.dni)
                if (profesor == null) {
                    Timber.e("No se encontró información de profesor para usuario con DNI: ${usuario.dni}")
                    
                    // Intento alternativo: buscar directamente por DNI
                    val profesorPorDni = profesorRepository.buscarProfesorPorDni(usuario.dni)
                    if (profesorPorDni == null) {
                        _uiState.value = MisAlumnosUiState(
                            isLoading = false,
                            error = "No se encontró información del profesor. Por favor, contacte al administrador."
                        )
                        return@launch
                    } else {
                        Timber.d("Se encontró profesor mediante búsqueda alternativa: ${profesorPorDni.id}")
                    }
                }
                
                // Obtener el profesor, ya sea por el método principal o alternativo
                val profesorActual = profesor ?: profesorRepository.buscarProfesorPorDni(usuario.dni)
                if (profesorActual == null) {
                    _uiState.value = MisAlumnosUiState(
                        isLoading = false,
                        error = "No se pudo identificar al profesor"
                    )
                    return@launch
                }
                
                Timber.d("Profesor encontrado con ID: ${profesorActual.id}, nombre: ${profesorActual.nombre}")
                
                // Lista para almacenar todos los alumnos
                val todosLosAlumnos = mutableListOf<Alumno>()
                var nombreClase = ""
                
                // 1. Obtener alumnos donde este profesor es el asignado directamente
                Timber.d("Buscando alumnos con profesor asignado: ${profesorActual.id}")
                val alumnosDirectos = alumnoRepository.getAlumnosForProfesor(profesorActual.id)
                todosLosAlumnos.addAll(alumnosDirectos)
                Timber.d("Encontrados ${alumnosDirectos.size} alumnos asignados directamente")
                
                // 2. Obtener clases asignadas al profesor
                val clasesResult = claseRepository.getClasesByProfesor(profesorActual.id)
                if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                    Timber.d("Encontradas ${clasesResult.data.size} clases para el profesor")
                    
                    // Para cada clase, obtener sus alumnos
                    for (clase in clasesResult.data) {
                        // Guardamos el nombre de la primera clase (para simplificar)
                        if (nombreClase.isEmpty()) {
                            nombreClase = clase.nombre
                        }
                        
                        Timber.d("Procesando clase: ${clase.id} - ${clase.nombre}")
                        
                        // Usar múltiples métodos para obtener alumnos
                        
                        // Método 1: Obtener por ID de clase
                        val alumnosResult = alumnoRepository.getAlumnosByClaseId(clase.id)
                        if (alumnosResult is Result.Success) {
                            Timber.d("Método 1 - Encontrados ${alumnosResult.data.size} alumnos en clase ${clase.nombre}")
                            todosLosAlumnos.addAll(alumnosResult.data)
                        }
                        
                        // Método 2: Usar alumnosIds de la clase
                        clase.alumnosIds?.let { ids ->
                            if (ids.isNotEmpty()) {
                                Timber.d("Método 2 - La clase tiene ${ids.size} IDs de alumnos")
                                for (alumnoId in ids) {
                                    try {
                                        val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                                        if (alumnoResult is Result.Success) {
                                            todosLosAlumnos.add(alumnoResult.data)
                                            Timber.d("Método 2 - Añadido alumno: ${alumnoResult.data.nombre}")
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al cargar alumno por ID: $alumnoId")
                                    }
                                }
                            }
                        }
                        
                        // Método 3: Obtener alumnos por documento
                        val alumnosPorClase = alumnoRepository.getAlumnosPorClase(clase.id)
                        if (alumnosPorClase.isNotEmpty()) {
                            Timber.d("Método 3 - Encontrados ${alumnosPorClase.size} alumnos por clase")
                            todosLosAlumnos.addAll(alumnosPorClase)
                        }
                    }
                } else {
                    // 3. Intentar con claseId del profesor
                    if (profesorActual.claseId.isNotEmpty()) {
                        Timber.d("Probando con claseId del profesor: ${profesorActual.claseId}")
                        val alumnosClase = alumnoRepository.getAlumnosByClaseId(profesorActual.claseId) 
                        if (alumnosClase is Result.Success) {
                            todosLosAlumnos.addAll(alumnosClase.data)
                            Timber.d("Encontrados ${alumnosClase.data.size} alumnos por claseId del profesor")
                            
                            // Intentar cargar el nombre de la clase
                            val claseResult = claseRepository.getClaseById(profesorActual.claseId)
                            if (claseResult is Result.Success) {
                                nombreClase = claseResult.data.nombre
                            }
                        }
                    } else {
                        Timber.d("No hay clases asignadas al profesor ${profesorActual.id}")
                    }
                }
                
                // 4. Buscar por centro educativo
                if (profesorActual.centroId.isNotEmpty() && todosLosAlumnos.isEmpty()) {
                    Timber.d("Buscando alumnos por centroId: ${profesorActual.centroId}")
                    try {
                        val alumnosDelCentro = cargarAlumnosPorCentro(profesorActual.centroId)
                        if (alumnosDelCentro.isNotEmpty()) {
                            Timber.d("Encontrados ${alumnosDelCentro.size} alumnos del centro")
                            todosLosAlumnos.addAll(alumnosDelCentro)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al cargar alumnos por centro: ${e.message}")
                    }
                }
                
                // Eliminar duplicados
                val alumnosSinDuplicados = todosLosAlumnos.distinctBy { it.dni }
                Timber.d("Total de alumnos sin duplicados: ${alumnosSinDuplicados.size}")
                
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
                Timber.e(e, "Error al cargar alumnos del profesor: ${e.message}")
                _uiState.value = MisAlumnosUiState(
                    isLoading = false,
                    error = "Error al cargar alumnos: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Carga alumnos por centro educativo
     */
    private suspend fun cargarAlumnosPorCentro(centroId: String): List<Alumno> {
        return try {
            // Intentar con el repositorio 
            val result = alumnoRepository.getAlumnosByCentroId(centroId)
            Timber.d("Resultado de carga por centro: ${result.size} alumnos")
            
            // Si no hay alumnos por el método directo, intentar con método alternativo
            if (result.isEmpty()) {
                Timber.d("Intentando método alternativo de carga por centro")
                val resultAlternativo = alumnoRepository.getAlumnosByCentro(centroId)
                if (resultAlternativo is Result.Success) {
                    Timber.d("Método alternativo: ${resultAlternativo.data.size} alumnos")
                    return resultAlternativo.data
                }
            }
            
            return result
        } catch (e: Exception) {
            Timber.e(e, "Error en cargarAlumnosPorCentro: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Refresca la lista de alumnos
     */
    fun refrescarAlumnos() {
        cargarAlumnos()
    }
} 