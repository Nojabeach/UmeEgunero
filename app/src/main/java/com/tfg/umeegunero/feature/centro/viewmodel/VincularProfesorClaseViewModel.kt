package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de vinculación de profesores a clases
 */
data class VincularProfesorClaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val profesores: List<Usuario> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val clasesAsignadas: Map<String, List<Clase>> = emptyMap(),
    val profesorSeleccionado: Usuario? = null,
    val claseSeleccionada: Clase? = null,
    val cursoSeleccionado: Curso? = null,
    val centroId: String = "",
    val showAsignarClasesDialog: Boolean = false,
    val showConfirmarDesasignacionDialog: Boolean = false,
    val showSuccessMessage: Boolean = false
)

/**
 * ViewModel para la pantalla de vinculación de profesores a clases
 */
@HiltViewModel
class VincularProfesorClaseViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val claseRepository: ClaseRepository,
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VincularProfesorClaseUiState())
    val uiState: StateFlow<VincularProfesorClaseUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Obtenemos el ID del centro del usuario actual
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val usuarioActual = authRepository.getCurrentUser()
                
                if (usuarioActual == null) {
                    _uiState.update { 
                        it.copy(
                            error = "Usuario no autenticado",
                            isLoading = false
                        ) 
                    }
                    return@launch
                }
                
                Timber.d("Usuario actual: ${usuarioActual.documentId}")
                
                // Intentar obtener el centroId del usuario
                val centroId = obtenerCentroIdDelUsuarioActual(usuarioActual.documentId)
                
                if (centroId.isNullOrEmpty()) {
                    _uiState.update { 
                        it.copy(
                            error = "No se pudo determinar el centro educativo",
                            isLoading = false
                        ) 
                    }
                    return@launch
                }
                
                Timber.d("Centro ID obtenido: $centroId")
                _uiState.update { it.copy(centroId = centroId) }
                
                // Cargamos cursos y profesores en paralelo
                val cursosDeferred = async { cursoRepository.obtenerCursosPorCentroResult(centroId) }
                val profesoresDeferred = async { usuarioRepository.getProfesoresByCentro(centroId) }
                
                // Procesar resultados de cursos
                when (val cursoResult = cursosDeferred.await()) {
                    is Result.Success -> {
                        val cursos = cursoResult.data
                        Timber.d("Cursos cargados: ${cursos.size}")
                        
                        _uiState.update { it.copy(cursos = cursos) }
                        
                        // Si hay cursos, seleccionamos el primero y cargamos sus clases
                        if (cursos.isNotEmpty()) {
                            val primerCurso = cursos.first()
                            _uiState.update { it.copy(cursoSeleccionado = primerCurso) }
                            
                            // Cargar clases del primer curso
                            when (val clasesResult = claseRepository.getClasesByCursoId(primerCurso.id)) {
                                is Result.Success -> {
                                    _uiState.update { it.copy(clases = clasesResult.data) }
                                    Timber.d("Clases cargadas: ${clasesResult.data.size}")
                                }
                                is Result.Error -> {
                                    Timber.e(clasesResult.exception, "Error al cargar clases iniciales")
                                }
                                is Result.Loading -> { /* No hacer nada */ }
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e(cursoResult.exception, "Error al cargar cursos iniciales")
                        _uiState.update { 
                            it.copy(error = "Error al cargar cursos: ${cursoResult.exception?.message}")
                        }
                    }
                    is Result.Loading -> { /* No hacer nada */ }
                }
                
                // Procesar resultados de profesores
                when (val profesoresResult = profesoresDeferred.await()) {
                    is Result.Success -> {
                        val profesores = profesoresResult.data.sortedWith(
                            compareByDescending<Usuario> { it.activo }
                                .thenBy { it.nombre }
                                .thenBy { it.apellidos }
                        )
                        
                        Timber.d("Profesores cargados inicialmente: ${profesores.size}")
                        profesores.forEach { profesor ->
                            Timber.d("- Profesor: ${profesor.nombre} ${profesor.apellidos}, ID: ${profesor.documentId}")
                        }
                        
                        _uiState.update { it.copy(profesores = profesores) }
                        
                        // Si no hay profesores con el repositorio de usuarios, intentar con el de centro
                        if (profesores.isEmpty()) {
                            Timber.d("No se encontraron profesores en el primer intento, probando repositorio alternativo")
                            cargarProfesores(centroId)
                        }
                    }
                    is Result.Error -> {
                        Timber.e(profesoresResult.exception, "Error al cargar profesores iniciales")
                        // Intentamos con el método que ya tiene lógica de fallback
                        cargarProfesores(centroId)
                    }
                    is Result.Loading -> { /* No hacer nada */ }
                }
                
                // Finalizar carga
                _uiState.update { it.copy(isLoading = false) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error general en la inicialización")
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    ) 
                }
            }
        }
    }
    
    /**
     * Obtiene el ID del centro asociado al usuario actual
     */
    private suspend fun obtenerCentroIdDelUsuarioActual(usuarioId: String): String? {
        Timber.d("Intentando obtener centroId para el usuario: $usuarioId")
        
        // Primero intentamos obtener el usuario para ver sus perfiles
        return when (val result = usuarioRepository.getUsuarioById(usuarioId)) {
            is Result.Success -> {
                val usuario = result.data
                Timber.d("Usuario obtenido: ${usuario.nombre} ${usuario.apellidos}, perfiles: ${usuario.perfiles.size}")
                
                // Buscamos el primer perfil que tenga un centroId asignado
                val centroIdFromPerfiles = usuario.perfiles.firstOrNull { it.centroId.isNotEmpty() }?.centroId
                
                if (!centroIdFromPerfiles.isNullOrEmpty()) {
                    Timber.d("CentroId encontrado en perfiles: $centroIdFromPerfiles")
                    centroIdFromPerfiles
                } else {
                    // Si no hay centroId en los perfiles, intentamos obtenerlo de otro lugar
                    Timber.d("No se encontró centroId en perfiles, intentando método alternativo")
                    
                    try {
                        val centroIdFromRepository = usuarioRepository.getCentroIdUsuarioActual()
                        if (!centroIdFromRepository.isNullOrEmpty()) {
                            Timber.d("CentroId obtenido con método alternativo: $centroIdFromRepository")
                            centroIdFromRepository
                        } else {
                            // Último intento: buscar en centros donde el usuario es administrador o profesor
                            Timber.d("Buscando centros donde el usuario es admin o profesor")
                            
                            val centrosResult = centroRepository.getCentrosByAdminOrProfesor(usuarioId)
                            if (centrosResult is Result.Success && centrosResult.data.isNotEmpty()) {
                                val primerCentro = centrosResult.data.first()
                                Timber.d("Centro encontrado como admin/profesor: ${primerCentro.id} - ${primerCentro.nombre}")
                                primerCentro.id
                            } else {
                                Timber.d("No se encontró ningún centro para el usuario")
                                null
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al obtener centroId por métodos alternativos")
                        null
                    }
                }
            }
            is Result.Error -> {
                Timber.e(result.exception, "Error al obtener el usuario: ${result.exception?.message}")
                _uiState.update { 
                    it.copy(error = "Error al obtener el usuario: ${result.exception?.message}") 
                }
                null
            }
            is Result.Loading -> {
                // Manejo del estado de carga si es necesario
                null
            }
        }
    }
    
    /**
     * Carga los cursos disponibles para el centro
     */
    fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("=== DIAGNÓSTICO DE CARGA DE CURSOS ===")
            Timber.d("Iniciando carga de cursos para el centro: $centroId")
            
            // Cargar todos los cursos (sin filtrar por activo)
            when (val result = cursoRepository.obtenerCursosPorCentroResult(centroId, soloActivos = false)) {
                is Result.Success -> {
                    val cursos = result.data
                    Timber.d("Cursos cargados: ${cursos.size}")
                    
                    // Log detallado para cada curso encontrado
                    cursos.forEach { curso ->
                        Timber.d("Curso: ID=${curso.id}, Nombre=${curso.nombre}, CentroID=${curso.centroId}, Activo=${curso.activo}")
                    }
                    
                    // Actualizar UI con los cursos obtenidos
                    _uiState.update { 
                        it.copy(
                            cursos = cursos,
                            isLoading = false
                        )
                    }
                    
                    // Si hay cursos, cargar profesores
                    if (cursos.isNotEmpty()) {
                        cargarProfesores(centroId)
                    } else {
                        Timber.d("No se encontraron cursos para este centro ($centroId)")
                        // Intentar usar un método alternativo si no hay cursos
                        intentarCargarCursosAlternativo(centroId)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar cursos: ${result.exception?.message}")
                    // Intentar método alternativo en caso de error
                    intentarCargarCursosAlternativo(centroId)
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Intenta cargar cursos utilizando un método alternativo (getAllCursos + filtrado manual)
     */
    private suspend fun intentarCargarCursosAlternativo(centroId: String) {
        Timber.d("Intentando cargar cursos con método alternativo")
        
        when (val result = cursoRepository.getAllCursos()) {
            is Result.Success -> {
                val todosCursos = result.data
                Timber.d("Total de cursos en la base de datos: ${todosCursos.size}")
                
                val cursosDeCentro = todosCursos.filter { it.centroId == centroId }
                Timber.d("Cursos filtrados para centroId=$centroId: ${cursosDeCentro.size}")
                
                cursosDeCentro.forEach { curso ->
                    Timber.d("Método alternativo - Curso: ID=${curso.id}, Nombre=${curso.nombre}, CentroID=${curso.centroId}, Activo=${curso.activo}")
                }
                
                if (cursosDeCentro.isNotEmpty()) {
                    Timber.d("Recuperación exitosa con método alternativo")
                    _uiState.update { 
                        it.copy(
                            cursos = cursosDeCentro,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    // Cargar profesores
                    cargarProfesores(centroId)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No se encontraron cursos para este centro"
                        )
                    }
                }
            }
            is Result.Error -> {
                Timber.e(result.exception, "Error en método alternativo: ${result.exception?.message}")
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar cursos: ${result.exception?.message}",
                        isLoading = false
                    )
                }
            }
            is Result.Loading -> {
                // Manejar estado de carga si es necesario
            }
        }
    }
    
    /**
     * Carga los profesores del centro
     */
    fun cargarProfesores(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("Iniciando carga de profesores para el centro: $centroId")
            
            when (val result = usuarioRepository.getProfesoresByCentro(centroId)) {
                is Result.Success -> {
                    // Ordenamos los profesores: primero los activos y luego los inactivos
                    val profesoresOrdenados = result.data.sortedWith(
                        compareByDescending<Usuario> { it.activo }
                            .thenBy { it.nombre }
                            .thenBy { it.apellidos }
                    )
                    
                    Timber.d("Profesores cargados: ${profesoresOrdenados.size}")
                    profesoresOrdenados.forEach { profesor ->
                        Timber.d("- Profesor: ${profesor.nombre} ${profesor.apellidos}, ID: ${profesor.documentId}")
                    }
                    
                    _uiState.update { 
                        it.copy(
                            profesores = profesoresOrdenados,
                            isLoading = false
                        )
                    }
                    
                    // Si no hay profesores, intentar con el repositorio de centro
                    if (profesoresOrdenados.isEmpty()) {
                        Timber.d("No se encontraron profesores en UsuarioRepository, probando CentroRepository")
                        
                        val resultCentro = centroRepository.getProfesoresByCentro(centroId)
                        if (resultCentro is Result.Success && resultCentro.data.isNotEmpty()) {
                            val profesoresCentro = resultCentro.data.sortedWith(
                                compareByDescending<Usuario> { it.activo }
                                    .thenBy { it.nombre }
                                    .thenBy { it.apellidos }
                            )
                            
                            Timber.d("Profesores encontrados en CentroRepository: ${profesoresCentro.size}")
                            
                            _uiState.update { 
                                it.copy(
                                    profesores = profesoresCentro,
                                    isLoading = false
                                )
                            }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    error = "No hay profesores disponibles para este centro"
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesores: ${result.exception?.message}")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar profesores: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                    
                    // Intento alternativo con CentroRepository si falla UsuarioRepository
                    val resultCentro = centroRepository.getProfesoresByCentro(centroId)
                    if (resultCentro is Result.Success && resultCentro.data.isNotEmpty()) {
                        Timber.d("Recuperación exitosa con CentroRepository tras error")
                        
                        val profesoresCentro = resultCentro.data.sortedWith(
                            compareByDescending<Usuario> { it.activo }
                                .thenBy { it.nombre }
                                .thenBy { it.apellidos }
                        )
                        
                        _uiState.update { 
                            it.copy(
                                profesores = profesoresCentro,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Selecciona un curso y carga sus clases
     */
    fun seleccionarCurso(curso: Curso) {
        _uiState.update { it.copy(cursoSeleccionado = curso) }
        cargarClases(curso.id)
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(
                            clases = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar clases: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Selecciona un profesor y carga sus clases asignadas
     */
    fun seleccionarProfesor(profesor: Usuario) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    profesorSeleccionado = profesor,
                    isLoading = true,
                    error = null
                ) 
            }
            
            when (val result = claseRepository.getClasesByProfesor(profesor.documentId)) {
                is Result.Success -> {
                    val clasesAsignadas = result.data
                    _uiState.update { state ->
                        val clasesMap = mutableMapOf<String, List<Clase>>()
                        clasesMap[profesor.documentId] = clasesAsignadas
                        
                        state.copy(
                            clasesAsignadas = state.clasesAsignadas + clasesMap,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases del profesor")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar clases del profesor: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Selecciona una clase para asignar o desasignar
     */
    fun seleccionarClase(clase: Clase) {
        _uiState.update { it.copy(claseSeleccionada = clase) }
    }
    
    /**
     * Asigna un profesor a una clase
     */
    fun asignarProfesorAClase() {
        val profesor = _uiState.value.profesorSeleccionado
        val clase = _uiState.value.claseSeleccionada
        
        if (profesor == null || clase == null) {
            _uiState.update { 
                it.copy(error = "Selecciona un profesor y una clase") 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = claseRepository.asignarProfesorAClase(profesor.documentId, clase.id)) {
                is Result.Success -> {
                    // Actualizamos la lista de clases asignadas
                    seleccionarProfesor(profesor)
                    
                    _uiState.update { 
                        it.copy(
                            mensaje = "Profesor asignado correctamente",
                            showSuccessMessage = true,
                            isLoading = false,
                            showAsignarClasesDialog = false
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al asignar profesor a clase")
                    _uiState.update { 
                        it.copy(
                            error = "Error al asignar profesor: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Desasigna un profesor de una clase
     */
    fun desasignarProfesorDeClase() {
        val profesor = _uiState.value.profesorSeleccionado
        val clase = _uiState.value.claseSeleccionada
        
        if (profesor == null || clase == null) {
            _uiState.update { 
                it.copy(error = "Selecciona un profesor y una clase") 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = claseRepository.desasignarProfesorDeClase(profesor.documentId, clase.id)) {
                is Result.Success -> {
                    // Actualizamos la lista de clases asignadas
                    seleccionarProfesor(profesor)
                    
                    _uiState.update { 
                        it.copy(
                            mensaje = "Profesor desasignado correctamente",
                            showSuccessMessage = true,
                            isLoading = false,
                            showConfirmarDesasignacionDialog = false
                        ) 
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al desasignar profesor de clase")
                    _uiState.update { 
                        it.copy(
                            error = "Error al desasignar profesor: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Verifica si un profesor está asignado a una clase específica
     */
    fun isProfesorAsignadoAClase(profesorId: String, claseId: String): Boolean {
        val clasesAsignadas = _uiState.value.clasesAsignadas[profesorId] ?: emptyList()
        return clasesAsignadas.any { it.id == claseId }
    }
    
    /**
     * Muestra el diálogo de asignación de clases
     */
    fun mostrarDialogoAsignarClases(mostrar: Boolean) {
        _uiState.update { it.copy(showAsignarClasesDialog = mostrar) }
    }
    
    /**
     * Muestra el diálogo de confirmación de desasignación
     */
    fun mostrarDialogoConfirmarDesasignacion(mostrar: Boolean) {
        _uiState.update { it.copy(showConfirmarDesasignacionDialog = mostrar) }
    }
    
    /**
     * Limpia los mensajes de éxito
     */
    fun limpiarMensajeExito() {
        _uiState.update { 
            it.copy(
                mensaje = null,
                showSuccessMessage = false
            ) 
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
} 