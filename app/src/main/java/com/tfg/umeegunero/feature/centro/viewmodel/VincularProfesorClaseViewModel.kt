package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.UsuarioUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore

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
    val centros: List<Centro> = emptyList(),
    val clasesAsignadas: Map<String, Map<Clase, Boolean>> = emptyMap(),
    val profesorSeleccionado: Usuario? = null,
    val claseSeleccionada: Clase? = null,
    val cursoSeleccionado: Curso? = null,
    val centroSeleccionado: Centro? = null,
    val centroId: String = "",
    val showAsignarClasesDialog: Boolean = false,
    val showConfirmarDesasignacionDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isAdminApp: Boolean = false
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
            Timber.d("=== INICIALIZANDO VINCULAR PROFESOR CLASE VIEWMODEL ===")
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Obtener el usuario actual
                val usuarioActual = authRepository.getCurrentUser()
                if (usuarioActual == null) {
                    Timber.e("Error: Usuario no autenticado")
                    _uiState.update { 
                        it.copy(
                            error = "Usuario no autenticado. Inicie sesión nuevamente.",
                            isLoading = false
                        ) 
                    }
                    return@launch
                }
                
                Timber.d("Usuario actual: ${usuarioActual.email} (ID: ${usuarioActual.documentId})")
                
                // Verificar si el usuario es admin de app
                val esAdminApp = verificarSiEsAdminApp(usuarioActual.documentId)
                _uiState.update { it.copy(isAdminApp = esAdminApp) }
                
                if (esAdminApp) {
                    // Si es admin de app, cargar todos los centros disponibles
                    Timber.d("Usuario es ADMIN_APP, cargando todos los centros")
                    cargarTodosCentros()
                } else {
                    // Si no es admin de app, obtener solo su centro asignado
                    Timber.d("Usuario no es ADMIN_APP, buscando su centro asignado")
                    
                    // Intentar obtener el centroId del usuario usando la utilidad centralizada
                    val centroId = UsuarioUtils.obtenerCentroIdDelUsuarioActual(authRepository, usuarioRepository)
                    
                    if (centroId.isNullOrEmpty()) {
                        Timber.e("Error: No se pudo determinar el centro educativo del usuario actual")
                        _uiState.update { 
                            it.copy(
                                error = "No se pudo determinar tu centro educativo. Contacta al administrador.",
                                isLoading = false
                            ) 
                        }
                        return@launch
                    }
                    
                    Timber.d("Centro ID obtenido: $centroId - Actualizando estado")
                    _uiState.update { it.copy(centroId = centroId) }
                    
                    // Cargar información del centro
                    val centro = obtenerCentroPorId(centroId)
                    if (centro != null) {
                        _uiState.update { it.copy(centroSeleccionado = centro) }
                        // Cargar cursos y profesores del centro automáticamente
                        cargarDatosCentro(centroId)
                    } else {
                        _uiState.update { 
                            it.copy(
                                error = "No se pudo cargar la información del centro.",
                                isLoading = false
                            ) 
                        }
                    }
                }
                
                // Finalizar carga inicial
                _uiState.update { it.copy(isLoading = false) }
                
            } catch (e: Exception) {
                Timber.e(e, "Error general en la inicialización: ${e.message}")
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
     * Verifica si el usuario es administrador de la aplicación
     */
    private suspend fun verificarSiEsAdminApp(usuarioId: String): Boolean {
        return try {
            // Obtener perfil completo del usuario
            val result = usuarioRepository.getUsuarioById(usuarioId)
            if (result is Result.Success) {
                // Verificar si tiene perfil de ADMIN_APP
                result.data.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP }
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar si es admin app: ${e.message}")
            false
        }
    }
    
    /**
     * Carga todos los centros disponibles
     */
    fun cargarTodosCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("Cargando todos los centros educativos")
            
            try {
                when (val result = centroRepository.getAllCentros()) {
                    is Result.Success -> {
                        val centros = result.data
                        Timber.d("Centros cargados: ${centros.size}")
                        
                        centros.forEach { centro ->
                            Timber.d("Centro: ${centro.nombre} (ID: ${centro.id})")
                        }
                        
                        _uiState.update { 
                            it.copy(
                                centros = centros,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al cargar centros: ${result.exception?.message}")
                        _uiState.update { 
                            it.copy(
                                error = "Error al cargar centros: ${result.exception?.message}",
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        // No hacer nada para el estado Loading
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar centros: ${e.message}")
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cargar centros: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Obtiene un centro por su ID
     */
    private suspend fun obtenerCentroPorId(centroId: String): Centro? {
        return try {
            when (val result = centroRepository.getCentroById(centroId)) {
                is Result.Success -> {
                    val centro = result.data
                    Timber.d("Centro obtenido: ${centro.nombre} (ID: ${centro.id})")
                    centro
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al obtener centro: ${result.exception?.message}")
                    null
                }
                is Result.Loading -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro: ${e.message}")
            null
        }
    }
    
    /**
     * Selecciona un centro y carga sus datos (cursos y profesores)
     */
    fun seleccionarCentro(centro: Centro) {
        Timber.d("Centro seleccionado: ${centro.nombre} (ID: ${centro.id})")
        
        _uiState.update { 
            it.copy(
                centroSeleccionado = centro,
                centroId = centro.id,
                cursoSeleccionado = null,
                cursos = emptyList(),
                claseSeleccionada = null,
                clases = emptyList(),
                profesorSeleccionado = null,
                profesores = emptyList()
            )
        }
        
        // Cargar datos del centro seleccionado
        cargarDatosCentro(centro.id)
    }
    
    /**
     * Carga todos los datos relacionados con un centro (cursos y profesores)
     */
    private fun cargarDatosCentro(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Cargar cursos sin filtrar por activos usando la función suspendida
                val cursosList = cursoRepository.obtenerCursosPorCentro(centroId, soloActivos = false)
                Timber.d("Cursos cargados para centro $centroId: ${cursosList.size}")
                        
                cursosList.forEach { curso -> // No se necesita especificar el tipo aquí
                    Timber.d("Curso: ${curso.nombre} (ID: ${curso.id}, activo: ${curso.activo})")
                }
                        
                _uiState.update { it.copy(cursos = cursosList) }
                        
                if (cursosList.isNotEmpty()) {
                    val primerCurso = cursosList.first()
                    Timber.d("Seleccionando primer curso: ${primerCurso.nombre}")
                    _uiState.update { it.copy(cursoSeleccionado = primerCurso) }
                    cargarClasesPorCurso(primerCurso.id)
                } else {
                    Timber.w("No se encontraron cursos para el centro $centroId")
                }
                
                // Cargar profesores incluyendo inactivos
                cargarProfesores(centroId, incluirInactivos = true)
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar datos del centro: ${e.message}")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado al cargar datos del centro") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Obtiene el ID del centro asociado al usuario actual
     * Intenta múltiples métodos para obtener el centroId
     */
    private suspend fun obtenerCentroIdDelUsuarioActual(usuarioId: String): String? {
        Timber.d("Utilizando método centralizado para obtener el centroId")
        return UsuarioUtils.obtenerCentroIdDelUsuarioActual(authRepository, usuarioRepository)
    }
    
    /**
     * Carga los cursos disponibles para el centro
     */
    fun cargarCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("=== DIAGNÓSTICO DE CARGA DE CURSOS ===")
            Timber.d("Iniciando carga de cursos para el centro: $centroId")
            
            try {
                // Usar siempre el método directo (suspend fun)
                val todosLosCursos = cursoRepository.obtenerCursosPorCentro(centroId, soloActivos = false)
                Timber.d("MÉTODO DIRECTO: Encontrados ${todosLosCursos.size} cursos para centro $centroId")
                
                todosLosCursos.forEach { curso ->
                    Timber.d("Curso: ID=${curso.id}, Nombre=${curso.nombre}, CentroID=${curso.centroId}, Activo=${curso.activo}")
                }
                
                _uiState.update { 
                    it.copy(
                        cursos = todosLosCursos,
                        isLoading = false
                    )
                }
                
                if (todosLosCursos.isNotEmpty()) {
                    cargarProfesores(centroId) // Cargar profesores si hay cursos
                } else {
                    Timber.d("No se encontraron cursos para este centro ($centroId)")
                    // No es necesario llamar al método alternativo aquí ya que obtenerCursosPorCentro es fiable
                }

            } catch (e: Exception) {
                Timber.e(e, "Error al cargar cursos: ${e.message}")
                 _uiState.update { it.copy(error = e.message ?: "Error inesperado al cargar cursos", isLoading = false) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Carga los profesores del centro
     * @param centroId ID del centro
     * @param incluirInactivos Si true, incluye también profesores inactivos
     */
    fun cargarProfesores(centroId: String, incluirInactivos: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("=== DIAGNÓSTICO DE CARGA DE PROFESORES ===")
            Timber.d("Iniciando carga de profesores para el centro: $centroId (incluirInactivos: $incluirInactivos)")
            
            when (val result = usuarioRepository.getProfesoresByCentro(centroId)) {
                is Result.Success -> {
                    // Aplicamos filtro según el parámetro
                    val profesoresCompletos = result.data
                    val profesoresOrdenados = if (incluirInactivos) {
                        // Incluimos todos, ordenados: activos primero, luego por nombre
                        profesoresCompletos.sortedWith(
                            compareByDescending<Usuario> { it.activo }
                                .thenBy { it.nombre }
                                .thenBy { it.apellidos }
                        )
                    } else {
                        // Filtramos y solo mostramos activos, ordenados por nombre
                        profesoresCompletos.filter { it.activo }
                            .sortedWith(
                                compareBy<Usuario> { it.nombre }
                                    .thenBy { it.apellidos }
                            )
                    }
                    
                    Timber.d("Profesores totales: ${profesoresCompletos.size}, Profesores tras filtro: ${profesoresOrdenados.size}")
                    profesoresOrdenados.forEach { profesor ->
                        Timber.d("- Profesor: ${profesor.nombre} ${profesor.apellidos}, ID: ${profesor.documentId}, Activo: ${profesor.activo}")
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
                        cargarProfesoresAlternativos(centroId, incluirInactivos)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar profesores: ${result.exception?.message}")
                    // Intento alternativo con CentroRepository si falla UsuarioRepository
                    cargarProfesoresAlternativos(centroId, incluirInactivos)
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Método alternativo para cargar profesores usando CentroRepository
     */
    private fun cargarProfesoresAlternativos(centroId: String, incluirInactivos: Boolean = true) {
        viewModelScope.launch {
            Timber.d("Intentando cargar profesores con método alternativo (CentroRepository)")
            
            val resultCentro = centroRepository.getProfesoresByCentro(centroId)
            if (resultCentro is Result.Success) {
                val profesoresCompletos = resultCentro.data
                
                // Aplicamos el mismo filtro y ordenación
                val profesoresOrdenados = if (incluirInactivos) {
                    profesoresCompletos.sortedWith(
                        compareByDescending<Usuario> { it.activo }
                            .thenBy { it.nombre }
                            .thenBy { it.apellidos }
                    )
                } else {
                    profesoresCompletos.filter { it.activo }
                        .sortedWith(
                            compareBy<Usuario> { it.nombre }
                                .thenBy { it.apellidos }
                        )
                }
                
                Timber.d("Profesores encontrados con método alternativo: ${profesoresOrdenados.size}")
                profesoresOrdenados.forEach { profesor ->
                    Timber.d("- (Alt) Profesor: ${profesor.nombre} ${profesor.apellidos}, ID: ${profesor.documentId}, Activo: ${profesor.activo}")
                }
                
                _uiState.update { 
                    it.copy(
                        profesores = profesoresOrdenados,
                        isLoading = false,
                        error = if (profesoresOrdenados.isEmpty()) "No hay profesores disponibles para este centro" else null
                    )
                }
            } else if (resultCentro is Result.Error) {
                Timber.e(resultCentro.exception, "Error en método alternativo: ${resultCentro.exception?.message}")
                _uiState.update { 
                    it.copy(
                        error = "No se pudieron cargar los profesores: ${resultCentro.exception?.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * Selecciona un curso y carga sus clases
     */
    fun seleccionarCurso(curso: Curso) {
        _uiState.update { it.copy(cursoSeleccionado = curso) }
        
        if (curso.id.isNotEmpty()) {
            Timber.d("Curso seleccionado: ${curso.nombre} (${curso.id})")
            cargarClasesPorCurso(curso.id)
        }
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClasesPorCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("Cargando clases del curso: $cursoId")
            
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                is Result.Success -> {
                    val clases = result.data
                    Timber.d("Clases cargadas: ${clases.size}")
                    
                    _uiState.update { it.copy(
                        clases = clases,
                        isLoading = false
                    ) }
                    
                    // Si hay un profesor seleccionado, recargamos sus clases
                    val profesorSeleccionado = _uiState.value.profesorSeleccionado
                    if (profesorSeleccionado != null) {
                        seleccionarProfesor(profesorSeleccionado)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases del curso")
                    _uiState.update { it.copy(
                        error = "Error al cargar clases: ${result.exception?.message}",
                        isLoading = false,
                        clases = emptyList()
                    ) }
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
            
            val cursoActual = _uiState.value.cursoSeleccionado
            if (cursoActual == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            
            // Obtener las clases del curso actual
            val clasesDelCurso = _uiState.value.clases
            
            // Obtener las clases asignadas al profesor
            when (val result = claseRepository.getClasesByProfesor(profesor.documentId)) {
                is Result.Success<*> -> {
                    val clasesAsignadasAlProfesor = result.data as List<Clase> // Cast seguro
                    
                    // Crear un mapa donde cada clase del curso tiene un valor booleano
                    // que indica si está asignada al profesor o no
                    val clasesMap = clasesDelCurso.associateWith { clase ->
                        clasesAsignadasAlProfesor.any { it.id == clase.id }
                    }
                    
                    Timber.d("Profesor ${profesor.nombre}: ${clasesMap.count { it.value }} clases asignadas de ${clasesMap.size} disponibles")
                    
                    // Actualizar el estado con el mapa de clases
                    _uiState.update { state ->
                        val profesoresClasesMap = state.clasesAsignadas.toMutableMap()
                        profesoresClasesMap[profesor.documentId] = clasesMap
                        
                        state.copy(
                            clasesAsignadas = profesoresClasesMap,
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
        val clasesAsignadas = _uiState.value.clasesAsignadas[profesorId] ?: emptyMap()
        return clasesAsignadas.any { (clase, _) -> clase.id == claseId }
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