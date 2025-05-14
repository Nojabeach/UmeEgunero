package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Period
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de alumnos del profesor
 */
data class MisAlumnosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val alumnos: List<Alumno> = emptyList(),
    val profesorActual: Usuario? = null,
    val claseActual: Clase? = null,
    val informeGenerado: Boolean = false,
    val informeContenido: String = "",
    val informeFormato: String = "pdf",
    val cursos: List<Curso> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val cursoSeleccionado: Curso? = null,
    val claseSeleccionada: Clase? = null
)

/**
 * ViewModel para la gestión de alumnos del profesor
 */
@HiltViewModel
class MisAlumnosProfesorViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val claseRepository: ClaseRepository,
    private val cursoRepository: CursoRepository,
    private val profesorRepository: ProfesorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisAlumnosUiState())
    val uiState: StateFlow<MisAlumnosUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener profesor actual
                val profesor = usuarioRepository.getCurrentUser()
                if (profesor != null) {
                    _uiState.update { it.copy(profesorActual = profesor) }
                    
                    // Cargar cursos disponibles
                    cargarCursos(profesor.dni)
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo obtener información del profesor actual",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos iniciales")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar datos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun cargarCursos(profesorId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Cargando cursos para el profesor: $profesorId")
                val cursosResult = cursoRepository.getCursosByProfesorId(profesorId)
                
                if (cursosResult is Result.Success<List<Curso>>) {
                    val cursos = cursosResult.data
                    Timber.d("Cursos encontrados: ${cursos.size}")
                    
                    _uiState.update { it.copy(
                        cursos = cursos,
                        isLoading = false
                    )}
                } else {
                    // Si no encontramos cursos específicos, intentar cargar todos los cursos
                    val todosCursosResult = cursoRepository.getAllCursos()
                    if (todosCursosResult is Result.Success && todosCursosResult.data.isNotEmpty()) {
                        _uiState.update { it.copy(
                            cursos = todosCursosResult.data,
                            isLoading = false
                        )}
                    } else {
                        _uiState.update { 
                            it.copy(
                                error = "No se encontraron cursos asignados",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar cursos")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar cursos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun seleccionarCurso(curso: Curso) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    cursoSeleccionado = curso,
                    claseSeleccionada = null,
                    alumnos = emptyList(),
                    isLoading = true
                )}
                
                // Cargar clases del curso seleccionado
                cargarClasesPorCurso(curso.id)
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar curso")
                _uiState.update { 
                    it.copy(
                        error = "Error al seleccionar curso: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Cargando clases para el curso: $cursoId")
                
                val profesorId = _uiState.value.profesorActual?.dni
                if (profesorId == null) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo obtener el ID del profesor",
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Obtener clases del curso filtradas por profesor
                val clasesResult = cursoRepository.getClasesByCursoAndProfesor(cursoId, profesorId)
                
                if (clasesResult is Result.Success<List<Clase>>) {
                    val clases = clasesResult.data
                    Timber.d("Clases encontradas: ${clases.size}")
                    
                    _uiState.update { it.copy(
                        clases = clases,
                        isLoading = false
                    )}
                } else {
                    // Intentar cargar todas las clases del curso
                    val todasClasesResult = claseRepository.getClasesByCursoId(cursoId)
                    if (todasClasesResult is Result.Success<List<Clase>>) {
                        _uiState.update { it.copy(
                            clases = todasClasesResult.data,
                            isLoading = false
                        )}
                    } else {
                        _uiState.update { 
                            it.copy(
                                error = "No se encontraron clases para este curso",
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar clases")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar clases: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun seleccionarClase(clase: Clase) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    claseSeleccionada = clase,
                    alumnos = emptyList(),
                    isLoading = true
                )}
                
                // Cargar alumnos de la clase seleccionada
                cargarAlumnosPorClase(clase.id)
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar clase")
                _uiState.update { 
                    it.copy(
                        error = "Error al seleccionar clase: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun cargarAlumnosPorClase(claseId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Cargando alumnos para la clase: $claseId")
                val alumnosResult = alumnoRepository.getAlumnosByClaseId(claseId)
                
                if (alumnosResult is Result.Success) {
                    val alumnos = alumnosResult.data
                    Timber.d("Alumnos encontrados: ${alumnos.size}")
                    
                    // Actualizar el profesorId para cada alumno si es necesario
                    val profesor = _uiState.value.profesorActual
                    if (profesor != null) {
                        for (alumno in alumnos) {
                            if (alumno.profesorId.isNullOrEmpty() || alumno.profesorId != profesor.dni) {
                                Timber.d("Actualizando profesor ${profesor.dni} para alumno ${alumno.dni}")
                                alumnoRepository.actualizarProfesor(alumno.dni, profesor.dni)
                            }
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            alumnos = alumnos,
                            isLoading = false
                        )
                    }
                } else if (alumnosResult is Result.Error) {
                    Timber.e(alumnosResult.exception, "Error al cargar alumnos")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar alumnos: ${alumnosResult.exception?.message ?: "Error desconocido"}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar alumnos")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar alumnos: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun programarReunion(
        alumnoDni: String,
        titulo: String,
        fecha: String,
        hora: String,
        descripcion: String
    ) {
        viewModelScope.launch {
            try {
                // Aquí iría la lógica para programar la reunión
                // Por ejemplo, guardar en Firestore
                val reunion = hashMapOf<String, Any>(
                    "alumnoDni" to alumnoDni,
                    "titulo" to titulo,
                    "fecha" to fecha,
                    "hora" to hora,
                    "descripcion" to descripcion,
                    "estado" to "pendiente"
                )
                
                // Guardar en Firestore
                profesorRepository.programarReunion(reunion)
                
                // Mostrar mensaje de éxito
                _uiState.update { it.copy(
                    mensaje = "Reunión programada correctamente",
                    error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al programar la reunión: ${e.message}",
                    mensaje = null
                )}
            }
        }
    }

    fun generarInformeAlumnos(
        filtro: String,
        formato: String,
        terminoBusqueda: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Filtrar alumnos según el criterio seleccionado
                val alumnosFiltrados = when (filtro) {
                    "todos" -> uiState.value.alumnos
                    "filtrados" -> if (terminoBusqueda.isNotEmpty()) {
                        uiState.value.alumnos.filter { 
                            it.nombre.contains(terminoBusqueda, ignoreCase = true) || 
                            it.apellidos.contains(terminoBusqueda, ignoreCase = true)
                        }
                    } else uiState.value.alumnos
                    else -> emptyList()
                }

                // Generar el informe según el formato seleccionado
                val informe = when (formato) {
                    "pdf" -> generarInformePDF(alumnosFiltrados)
                    "excel" -> generarInformeExcel(alumnosFiltrados)
                    "csv" -> generarInformeCSV(alumnosFiltrados)
                    else -> throw IllegalArgumentException("Formato no soportado")
                }

                // Guardar el informe
                profesorRepository.guardarInforme(informe, formato)

                _uiState.update { it.copy(
                    isLoading = false,
                    mensaje = "Informe generado correctamente",
                    error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al generar el informe: ${e.message}",
                    mensaje = null
                )}
            }
        }
    }

    private fun generarInformePDF(alumnos: List<Alumno>): String {
        // Implementar generación de PDF
        return "Informe PDF generado"
    }

    private fun generarInformeExcel(alumnos: List<Alumno>): String {
        // Implementar generación de Excel
        return "Informe Excel generado"
    }

    private fun generarInformeCSV(alumnos: List<Alumno>): String {
        // Implementar generación de CSV
        return "Informe CSV generado"
    }

    private fun calcularEdad(fechaNacimiento: String): Int {
        return try {
            val fecha = LocalDate.parse(fechaNacimiento, DateTimeFormatter.ISO_DATE)
            Period.between(fecha, LocalDate.now()).years
        } catch (e: Exception) {
            0
        }
    }

    private fun calcularPorcentaje(parte: Int, total: Int): String {
        return if (total > 0) {
            String.format("%.1f", (parte.toFloat() / total) * 100)
        } else "0.0"
    }
}
