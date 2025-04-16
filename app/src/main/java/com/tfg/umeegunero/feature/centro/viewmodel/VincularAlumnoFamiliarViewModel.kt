package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
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
 * Estado UI para la pantalla de vinculación de alumnos a familiares
 */
data class VincularAlumnoFamiliarUiState(
    // Datos
    val alumnos: List<Alumno> = emptyList(),
    val familiares: List<Usuario> = emptyList(),
    val vinculaciones: Map<String, List<String>> = emptyMap(), // Map<alumnoId, List<familiarId>>
    
    // Estado UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    
    // Filtros y selección
    val cursoId: String = "",
    val claseId: String = "",
    val alumnoSeleccionadoId: String = "",
    val familiarSeleccionadoId: String = "",
    val busquedaAlumno: String = "",
    val busquedaFamiliar: String = "",
    
    // Variables para crear nuevo alumno
    val nuevoAlumnoNombre: String = "",
    val nuevoAlumnoApellidos: String = "",
    val nuevoAlumnoDni: String = "",
    val nuevoAlumnoFechaNacimiento: String = "",
    
    // Variables para crear nuevo familiar
    val nuevoFamiliarNombre: String = "",
    val nuevoFamiliarApellidos: String = "",
    val nuevoFamiliarDni: String = "",
    val nuevoFamiliarEmail: String = "",
    val nuevoFamiliarTelefono: String = "",
    
    // Modo de visualización
    val mostrarFormularioNuevoAlumno: Boolean = false,
    val mostrarFormularioNuevoFamiliar: Boolean = false
)

/**
 * Relación de parentesco entre familiar y alumno
 */
enum class TipoParentesco {
    PADRE, MADRE, TUTOR, HERMANO, ABUELO, OTRO
}

/**
 * ViewModel para la vinculación de alumnos a familiares
 */
@HiltViewModel
class VincularAlumnoFamiliarViewModel @Inject constructor(
    private val alumnoRepository: AlumnoRepository,
    private val familiarRepository: FamiliarRepository,
    private val usuarioRepository: UsuarioRepository,
    private val cursoRepository: CursoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VincularAlumnoFamiliarUiState())
    val uiState: StateFlow<VincularAlumnoFamiliarUiState> = _uiState.asStateFlow()
    
    init {
        cargarAlumnos()
        cargarFamiliares()
    }
    
    /**
     * Carga todos los alumnos disponibles
     */
    fun cargarAlumnos() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = alumnoRepository.getAlumnos()) {
                is Result.Success<List<Alumno>> -> {
                    _uiState.update { 
                        it.copy(
                            alumnos = result.data,
                            isLoading = false
                        )
                    }
                    
                    // Cargar vinculaciones para cada alumno
                    result.data.forEach { alumno ->
                        cargarFamiliaresDeAlumno(alumno.id)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar alumnos")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar alumnos: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga alumnos por curso
     */
    fun cargarAlumnosPorCurso(cursoId: String) {
        if (cursoId.isEmpty()) return
        
        _uiState.update { it.copy(
            isLoading = true,
            cursoId = cursoId
        )}
        
        viewModelScope.launch {
            when (val result = alumnoRepository.getAlumnosByCursoId(cursoId)) {
                is Result.Success<List<Alumno>> -> {
                    _uiState.update { 
                        it.copy(
                            alumnos = result.data,
                            isLoading = false
                        )
                    }
                    
                    // Cargar vinculaciones para cada alumno
                    result.data.forEach { alumno ->
                        cargarFamiliaresDeAlumno(alumno.id)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar alumnos por curso")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar alumnos: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga alumnos por clase
     */
    fun cargarAlumnosPorClase(claseId: String) {
        if (claseId.isEmpty()) return
        
        _uiState.update { it.copy(
            isLoading = true,
            claseId = claseId
        )}
        
        viewModelScope.launch {
            when (val result = alumnoRepository.getAlumnosByClaseId(claseId)) {
                is Result.Success<List<Alumno>> -> {
                    _uiState.update { 
                        it.copy(
                            alumnos = result.data,
                            isLoading = false
                        )
                    }
                    
                    // Cargar vinculaciones para cada alumno
                    result.data.forEach { alumno ->
                        cargarFamiliaresDeAlumno(alumno.id)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar alumnos por clase")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar alumnos: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga todos los familiares disponibles
     */
    fun cargarFamiliares() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = familiarRepository.getFamiliares()) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            familiares = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar familiares")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar familiares: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Carga los familiares asignados a un alumno específico
     */
    fun cargarFamiliaresDeAlumno(alumnoId: String) {
        if (alumnoId.isEmpty()) return
        
        viewModelScope.launch {
            when (val result = familiarRepository.getFamiliaresByAlumnoId(alumnoId)) {
                is Result.Success<List<Usuario>> -> {
                    val familiarIds = result.data.map { it.dni }
                    _uiState.update { currentState ->
                        val updatedMap = currentState.vinculaciones.toMutableMap()
                        updatedMap[alumnoId] = familiarIds
                        currentState.copy(
                            vinculaciones = updatedMap
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar familiares de alumno $alumnoId")
                    // No actualizamos el error en UI para no interrumpir la experiencia
                }
                is Result.Loading -> {
                    // No necesitamos mostrar indicador de carga para cada alumno
                }
            }
        }
    }
    
    /**
     * Selecciona un alumno para editar sus familiares
     */
    fun seleccionarAlumno(alumnoId: String) {
        _uiState.update { it.copy(alumnoSeleccionadoId = alumnoId) }
    }
    
    /**
     * Vincula un familiar a un alumno con un tipo de parentesco
     */
    fun vincularFamiliarAlumno(familiarId: String, alumnoId: String, tipoParentesco: TipoParentesco) {
        if (familiarId.isEmpty() || alumnoId.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = familiarRepository.vincularFamiliarAlumno(familiarId, alumnoId, tipoParentesco.name)) {
                is Result.Success -> {
                    // Actualizar la lista de familiares asignados a este alumno
                    val familiaresActuales = _uiState.value.vinculaciones[alumnoId] ?: emptyList()
                    val nuevaLista = if (familiaresActuales.contains(familiarId)) {
                        familiaresActuales
                    } else {
                        familiaresActuales + familiarId
                    }
                    
                    val mapaActualizado = _uiState.value.vinculaciones.toMutableMap()
                    mapaActualizado[alumnoId] = nuevaLista
                    
                    _uiState.update { 
                        it.copy(
                            vinculaciones = mapaActualizado,
                            isLoading = false,
                            mensaje = "Familiar vinculado correctamente"
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al vincular familiar a alumno")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al vincular familiar: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Desvincular un familiar de un alumno
     */
    fun desvincularFamiliarAlumno(familiarId: String, alumnoId: String) {
        if (familiarId.isEmpty() || alumnoId.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = familiarRepository.desvincularFamiliarAlumno(familiarId, alumnoId)) {
                is Result.Success -> {
                    // Actualizar la lista de familiares asignados a este alumno
                    val familiaresActuales = _uiState.value.vinculaciones[alumnoId] ?: emptyList()
                    val nuevaLista = familiaresActuales.filter { it != familiarId }
                    
                    val mapaActualizado = _uiState.value.vinculaciones.toMutableMap()
                    mapaActualizado[alumnoId] = nuevaLista
                    
                    _uiState.update { 
                        it.copy(
                            vinculaciones = mapaActualizado,
                            isLoading = false,
                            mensaje = "Familiar desvinculado correctamente"
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al desvincular familiar de alumno")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al desvincular familiar: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Crea un nuevo alumno
     */
    fun crearNuevoAlumno() {
        val nombre = _uiState.value.nuevoAlumnoNombre
        val apellidos = _uiState.value.nuevoAlumnoApellidos
        val dni = _uiState.value.nuevoAlumnoDni
        val fechaNacimiento = _uiState.value.nuevoAlumnoFechaNacimiento
        val cursoId = _uiState.value.cursoId
        
        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || fechaNacimiento.isEmpty() || cursoId.isEmpty()) {
            _uiState.update { it.copy(error = "Todos los campos son obligatorios") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = alumnoRepository.crearAlumno(nombre, apellidos, dni, fechaNacimiento, cursoId)) {
                is Result.Success<String> -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            mensaje = "Alumno creado correctamente",
                            mostrarFormularioNuevoAlumno = false,
                            nuevoAlumnoNombre = "",
                            nuevoAlumnoApellidos = "",
                            nuevoAlumnoDni = "",
                            nuevoAlumnoFechaNacimiento = ""
                        )
                    }
                    // Recargar la lista de alumnos
                    cargarAlumnosPorCurso(cursoId)
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al crear alumno")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al crear alumno: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Crea un nuevo familiar
     */
    fun crearNuevoFamiliar() {
        val nombre = _uiState.value.nuevoFamiliarNombre
        val apellidos = _uiState.value.nuevoFamiliarApellidos
        val dni = _uiState.value.nuevoFamiliarDni
        val email = _uiState.value.nuevoFamiliarEmail
        val telefono = _uiState.value.nuevoFamiliarTelefono
        
        if (nombre.isEmpty() || apellidos.isEmpty() || dni.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            _uiState.update { it.copy(error = "Todos los campos son obligatorios") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            when (val result = familiarRepository.crearFamiliar(nombre, apellidos, dni, email, telefono)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            mensaje = "Familiar creado correctamente",
                            mostrarFormularioNuevoFamiliar = false,
                            nuevoFamiliarNombre = "",
                            nuevoFamiliarApellidos = "",
                            nuevoFamiliarDni = "",
                            nuevoFamiliarEmail = "",
                            nuevoFamiliarTelefono = ""
                        )
                    }
                    // Recargar la lista de familiares
                    cargarFamiliares()
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al crear familiar")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al crear familiar: ${result.exception?.message ?: "Desconocido"}"
                        ) 
                    }
                }
                is Result.Loading -> {
                    // Estado de carga ya establecido
                }
            }
        }
    }
    
    /**
     * Actualiza el filtro de búsqueda de alumnos
     */
    fun actualizarBusquedaAlumno(busqueda: String) {
        _uiState.update { it.copy(busquedaAlumno = busqueda) }
    }
    
    /**
     * Actualiza el filtro de búsqueda de familiares
     */
    fun actualizarBusquedaFamiliar(busqueda: String) {
        _uiState.update { it.copy(busquedaFamiliar = busqueda) }
    }
    
    /**
     * Actualiza los campos del formulario de nuevo alumno
     */
    fun actualizarNuevoAlumnoNombre(nombre: String) {
        _uiState.update { it.copy(nuevoAlumnoNombre = nombre) }
    }
    
    fun actualizarNuevoAlumnoApellidos(apellidos: String) {
        _uiState.update { it.copy(nuevoAlumnoApellidos = apellidos) }
    }
    
    fun actualizarNuevoAlumnoDni(dni: String) {
        _uiState.update { it.copy(nuevoAlumnoDni = dni) }
    }
    
    fun actualizarNuevoAlumnoFechaNacimiento(fechaNacimiento: String) {
        _uiState.update { it.copy(nuevoAlumnoFechaNacimiento = fechaNacimiento) }
    }
    
    /**
     * Actualiza los campos del formulario de nuevo familiar
     */
    fun actualizarNuevoFamiliarNombre(nombre: String) {
        _uiState.update { it.copy(nuevoFamiliarNombre = nombre) }
    }
    
    fun actualizarNuevoFamiliarApellidos(apellidos: String) {
        _uiState.update { it.copy(nuevoFamiliarApellidos = apellidos) }
    }
    
    fun actualizarNuevoFamiliarDni(dni: String) {
        _uiState.update { it.copy(nuevoFamiliarDni = dni) }
    }
    
    fun actualizarNuevoFamiliarEmail(email: String) {
        _uiState.update { it.copy(nuevoFamiliarEmail = email) }
    }
    
    fun actualizarNuevoFamiliarTelefono(telefono: String) {
        _uiState.update { it.copy(nuevoFamiliarTelefono = telefono) }
    }
    
    /**
     * Controla la visibilidad del formulario de nuevo alumno
     */
    fun mostrarFormularioNuevoAlumno(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarFormularioNuevoAlumno = mostrar) }
    }
    
    /**
     * Controla la visibilidad del formulario de nuevo familiar
     */
    fun mostrarFormularioNuevoFamiliar(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarFormularioNuevoFamiliar = mostrar) }
    }
    
    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun clearMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }
} 