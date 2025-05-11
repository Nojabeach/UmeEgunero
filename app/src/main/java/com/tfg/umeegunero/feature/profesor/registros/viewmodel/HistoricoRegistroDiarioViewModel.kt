package com.tfg.umeegunero.feature.profesor.registros.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Estado de UI para la pantalla de histórico de registros diarios
 */
data class HistoricoRegistroUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profesorId: String = "",
    val clases: List<Clase> = emptyList(),
    val claseSeleccionada: Clase? = null,
    val alumnos: List<Alumno> = emptyList(),
    val alumnoSeleccionado: Alumno? = null,
    val registros: List<RegistroActividad> = emptyList(),
    val fechaSeleccionada: Date = Date()
)

/**
 * ViewModel para la pantalla de histórico de registros diarios
 */
@HiltViewModel
class HistoricoRegistroDiarioViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profesorRepository: ProfesorRepository,
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoRegistroUiState())
    val uiState: StateFlow<HistoricoRegistroUiState> = _uiState.asStateFlow()

    init {
        cargarDatosIniciales()
    }
    
    /**
     * Carga los datos iniciales del profesor: sus clases
     */
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener usuario actual
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    Timber.e("No se pudo obtener el usuario actual")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se pudo identificar al usuario actual"
                        )
                    }
                    return@launch
                }
                
                Timber.d("Usuario autenticado: ${usuario.dni}, nombre: ${usuario.nombre}")
                
                // Buscar el profesor - intentamos varias estrategias
                var usuarioProfesor = profesorRepository.getProfesorByUsuarioId(usuario.dni)
                if (usuarioProfesor == null) {
                    Timber.d("No se encontró profesor por usuarioId, intentando buscar por DNI")
                    usuarioProfesor = profesorRepository.getUsuarioProfesorByDni(usuario.dni)
                }
                
                if (usuarioProfesor == null) {
                    Timber.e("No se pudo encontrar información del profesor para usuario: ${usuario.dni}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se encontró información del profesor"
                        )
                    }
                    return@launch
                }
                
                // Verificar que el usuario tiene perfil de PROFESOR
                val perfilProfesor = usuarioProfesor.perfiles.firstOrNull { it.tipo == TipoUsuario.PROFESOR }
                if (perfilProfesor == null) {
                    Timber.e("El usuario no tiene perfil de PROFESOR")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "El usuario no tiene perfil de profesor"
                        )
                    }
                    return@launch
                }
                
                // Usar el DNI como profesorId
                val profesorId = usuarioProfesor.dni
                Timber.d("Profesor identificado: $profesorId, nombre: ${usuarioProfesor.nombre}")
                
                // Cargar clases del profesor
                var clasesResult = claseRepository.getClasesByProfesor(profesorId)
                
                // Si no se obtuvieron clases, intentar con el ID de usuario directamente
                if (clasesResult is Result.Success && clasesResult.data.isEmpty()) {
                    Timber.d("No se encontraron clases con profesorId: $profesorId, intentando con usuarioId: ${usuario.dni}")
                    clasesResult = claseRepository.getClasesByProfesor(usuario.dni)
                }
                
                if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                    val clases = clasesResult.data
                    val claseSeleccionada = clases.firstOrNull()
                    
                    Timber.d("Se encontraron ${clases.size} clases para el profesor")
                    clases.forEach { clase ->
                        Timber.d("Clase: ${clase.id} - ${clase.nombre}")
                    }
                    
                    _uiState.update { 
                        it.copy(
                            clases = clases,
                            claseSeleccionada = claseSeleccionada,
                            profesorId = profesorId,
                            isLoading = false
                        )
                    }
                    
                    // Si hay una clase seleccionada, cargar sus alumnos
                    claseSeleccionada?.let { clase ->
                        Timber.d("Cargando alumnos para la clase: ${clase.id} - ${clase.nombre}")
                        cargarAlumnosPorClase(clase.id)
                    }
                } else {
                    Timber.e("No se encontraron clases para el profesor: $profesorId")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se encontraron clases asignadas a este profesor"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos iniciales: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga los alumnos de una clase
     */
    private fun cargarAlumnosPorClase(claseId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, alumnos = emptyList(), alumnoSeleccionado = null) }
                
                val alumnosResult = usuarioRepository.getAlumnosByClase(claseId)
                
                if (alumnosResult is Result.Success && alumnosResult.data.isNotEmpty()) {
                    val alumnos = alumnosResult.data
                    val alumnoSeleccionado = alumnos.firstOrNull()
                    
                    _uiState.update { 
                        it.copy(
                            alumnos = alumnos,
                            alumnoSeleccionado = alumnoSeleccionado,
                            isLoading = false
                        )
                    }
                    
                    // Si hay un alumno seleccionado, cargar sus registros
                    alumnoSeleccionado?.let { alumno ->
                        cargarRegistrosAlumno(alumno.id)
                    }
                } else {
                    Timber.d("No se encontraron alumnos para la clase: $claseId")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            alumnos = emptyList(),
                            registros = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar alumnos por clase: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar alumnos: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Carga los registros de un alumno
     */
    fun cargarRegistrosAlumno(alumnoId: String, limite: Long = 30) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, registros = emptyList()) }
                
                registroDiarioRepository.obtenerRegistrosAlumno(alumnoId, limite)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val registros = result.data.sortedByDescending { it.fecha }
                                _uiState.update { 
                                    it.copy(
                                        registros = registros,
                                        isLoading = false
                                    )
                                }
                            }
                            is Result.Error -> {
                                Timber.e(result.exception, "Error al obtener registros")
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = "Error al cargar registros: ${result.exception?.message}"
                                    )
                                }
                            }
                            is Result.Loading -> {
                                _uiState.update { it.copy(isLoading = true) }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros del alumno: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar registros: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Selecciona una clase
     */
    fun seleccionarClase(claseId: String) {
        val clase = _uiState.value.clases.find { it.id == claseId }
        if (clase != null) {
            _uiState.update { it.copy(claseSeleccionada = clase) }
            cargarAlumnosPorClase(claseId)
        }
    }
    
    /**
     * Selecciona un alumno
     */
    fun seleccionarAlumno(alumnoId: String) {
        val alumno = _uiState.value.alumnos.find { it.id == alumnoId }
        if (alumno != null) {
            _uiState.update { it.copy(alumnoSeleccionado = alumno) }
            cargarRegistrosAlumno(alumnoId)
        }
    }
    
    /**
     * Selecciona una fecha para ver registros específicos
     */
    fun seleccionarFecha(fecha: Date) {
        _uiState.update { it.copy(fechaSeleccionada = fecha) }
        
        // Recargar los registros del alumno actual con la nueva fecha
        _uiState.value.alumnoSeleccionado?.let { alumno ->
            cargarRegistrosPorFecha(alumno.id, fecha)
        }
    }
    
    /**
     * Carga los registros por fecha específica
     */
    private fun cargarRegistrosPorFecha(alumnoId: String, fecha: Date) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, registros = emptyList()) }
                
                // Formato de fecha para la consulta
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaStr = formatoFecha.format(fecha)
                
                val inicio = Calendar.getInstance().apply {
                    time = fecha
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val fin = Calendar.getInstance().apply {
                    time = fecha
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                Timber.d("Buscando registros para alumno $alumnoId entre $inicio y $fin")
                
                val result = registroDiarioRepository.obtenerRegistrosPorFecha(alumnoId, inicio, fin)
                
                when (result) {
                    is Result.Success -> {
                        val registros = result.data
                        _uiState.update { 
                            it.copy(
                                registros = registros,
                                isLoading = false
                            )
                        }
                        
                        if (registros.isEmpty()) {
                            Timber.d("No se encontraron registros para la fecha seleccionada")
                        } else {
                            Timber.d("Se encontraron ${registros.size} registros para la fecha")
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al obtener registros por fecha")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar registros: ${result.exception?.message}"
                            )
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada, ya estamos mostrando la carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar registros por fecha: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar registros: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Refrescar todos los datos
     */
    fun refrescarDatos() {
        cargarDatosIniciales()
    }
    
    /**
     * Limpiar mensaje de error
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 