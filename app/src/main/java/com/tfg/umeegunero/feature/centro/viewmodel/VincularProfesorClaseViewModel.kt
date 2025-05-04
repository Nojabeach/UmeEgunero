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
import com.tfg.umeegunero.data.repository.AlumnoRepository
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
    private val authRepository: AuthRepository,
    private val alumnoRepository: AlumnoRepository
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
     * Método expuesto para que la pantalla pueda inicializar con un centroId
     */
    suspend fun obtenerCentroPorId(centroId: String): Centro? {
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
                        // Una clase está asignada si existe en la lista de clases del profesor
                        val asignada = clasesAsignadasAlProfesor.any { it.id == clase.id }
                        
                        // Registros detallados para depuración
                        if (asignada) {
                            Timber.d("Clase '${clase.nombre}' (${clase.id}) ASIGNADA a profesor ${profesor.nombre}")
                        } else {
                            Timber.d("Clase '${clase.nombre}' (${clase.id}) NO asignada a profesor ${profesor.nombre}")
                        }
                        
                        asignada
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
     * Método para asignar un profesor a una clase.
     * Esta implementación actualiza:
     * 1. La clase (añadiendo el profesorId)
     * 2. El profesor (añadiendo la clase a su lista)
     * 3. Todos los alumnos de esa clase (actualizando su profesorId)
     */
    fun asignarProfesorAClase() {
        val profesorId = _uiState.value.profesorSeleccionado?.documentId ?: return
        val claseId = _uiState.value.claseSeleccionada?.id ?: return
        val clase = _uiState.value.claseSeleccionada ?: return
        
        Timber.d("Iniciando asignación del profesor $profesorId a la clase ${clase.nombre} ($claseId)")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Asignar el profesor a la clase
                Timber.d("Asignando profesor a clase en repositorio...")
                claseRepository.asignarProfesor(claseId, profesorId)
                
                // 2. Actualizar el estado local
                Timber.d("Actualizando estado local...")
                val profesorClases = _uiState.value.clasesAsignadas.toMutableMap()
                val clases = profesorClases[profesorId]?.toMutableMap() ?: mutableMapOf()
                
                // Marcar esta clase como asignada (true)
                clases[clase] = true
                profesorClases[profesorId] = clases
                
                Timber.d("Estado actualizado: clase ${clase.nombre} marcada como asignada para profesor $profesorId")
                
                // 3. Actualizar todos los alumnos de esta clase con el profesor asignado
                Timber.d("Actualizando alumnos de la clase...")
                actualizarAlumnosDeClaseConProfesor(claseId, profesorId)
                
                // 4. Mostrar mensaje de éxito
                Timber.d("Asignación completada exitosamente")
                _uiState.update {
                    it.copy(
                        clasesAsignadas = profesorClases,
                        isLoading = false,
                        showAsignarClasesDialog = false,
                        showSuccessMessage = true,
                        mensaje = "Profesor asignado correctamente a la clase"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al asignar profesor a clase")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al asignar profesor: ${e.message}",
                        showAsignarClasesDialog = false
                    )
                }
            }
        }
    }
    
    /**
     * Método para desasignar un profesor de una clase.
     * Esta implementación actualiza:
     * 1. La clase (eliminando el profesorId)
     * 2. El profesor (eliminando la clase de su lista)
     * 3. Todos los alumnos de esa clase (eliminando su profesorId)
     */
    fun desasignarProfesorDeClase() {
        val profesorId = _uiState.value.profesorSeleccionado?.documentId ?: return
        val claseId = _uiState.value.claseSeleccionada?.id ?: return
        val clase = _uiState.value.claseSeleccionada ?: return
        
        Timber.d("Iniciando desasignación del profesor $profesorId de la clase ${clase.nombre} ($claseId)")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Desasignar el profesor de la clase
                Timber.d("Desasignando profesor de clase en repositorio...")
                claseRepository.desasignarProfesor(claseId)
                
                // 2. Actualizar el estado local
                Timber.d("Actualizando estado local...")
                val profesorClases = _uiState.value.clasesAsignadas.toMutableMap()
                val clases = profesorClases[profesorId]?.toMutableMap() ?: mutableMapOf()
                
                // Marcar esta clase como no asignada (false)
                clases[clase] = false
                profesorClases[profesorId] = clases
                
                Timber.d("Estado actualizado: clase ${clase.nombre} marcada como NO asignada para profesor $profesorId")
                
                // 3. Actualizar todos los alumnos de esta clase para eliminar la referencia al profesor
                Timber.d("Actualizando alumnos de la clase...")
                eliminarProfesorDeAlumnosDeClase(claseId, profesorId)
                
                // 4. Mostrar mensaje de éxito
                Timber.d("Desasignación completada exitosamente")
                _uiState.update {
                    it.copy(
                        clasesAsignadas = profesorClases,
                        isLoading = false,
                        showConfirmarDesasignacionDialog = false,
                        showSuccessMessage = true,
                        mensaje = "Profesor desasignado correctamente de la clase"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al desasignar profesor de clase")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al desasignar profesor: ${e.message}",
                        showConfirmarDesasignacionDialog = false
                    )
                }
            }
        }
    }
    
    /**
     * Método privado para actualizar todos los alumnos de una clase con un profesor específico.
     * @param claseId ID de la clase
     * @param profesorId ID del profesor a asignar a todos los alumnos
     */
    private suspend fun actualizarAlumnosDeClaseConProfesor(claseId: String, profesorId: String) {
        try {
            // 1. Obtener todos los alumnos de esta clase usando el método exhaustivo
            val resultAlumnos = alumnoRepository.getAlumnosByClaseId(claseId)
            
            if (resultAlumnos is Result.Success) {
                val alumnos = resultAlumnos.data
                
                Timber.d("Actualizando ${alumnos.size} alumnos con profesor $profesorId")
            
                if (alumnos.isEmpty()) {
                    Timber.w("No se encontraron alumnos para la clase $claseId")
                    return
                }
                
                // 2. Mantener registro de éxitos y fallos para informar
                var alumnosActualizados = 0
                var alumnosFallidos = 0
                
                // 3. Para cada alumno, actualizar el profesorId con verificación de éxito
                alumnos.forEach { alumno ->
                    Timber.d("Actualizando alumno ${alumno.nombre} ${alumno.apellidos} (${alumno.dni}) con profesor $profesorId")
                    
                    val resultado = alumnoRepository.actualizarProfesor(alumno.dni, profesorId)
                    
                    if (resultado is Result.Success) {
                        alumnosActualizados++
                        Timber.d("Alumno ${alumno.dni} actualizado exitosamente con profesor $profesorId")
                    } else if (resultado is Result.Error) {
                        alumnosFallidos++
                        Timber.e(resultado.exception, "Error al actualizar profesor para alumno ${alumno.dni}")
                        
                        // Reintento una vez más si falló la primera vez
                        Timber.d("Reintentando actualización para alumno ${alumno.dni}")
                        val reintentoResultado = alumnoRepository.actualizarProfesor(alumno.dni, profesorId)
                        
                        if (reintentoResultado is Result.Success) {
                            alumnosActualizados++
                            alumnosFallidos--
                            Timber.d("Reintento exitoso para alumno ${alumno.dni}")
                        }
                    }
                }
                
                Timber.d("Actualización de alumnos completada: $alumnosActualizados exitosos, $alumnosFallidos fallidos")
                
                if (alumnosFallidos > 0) {
                    Timber.w("Atención: No se pudieron actualizar $alumnosFallidos alumnos con el profesor")
                }
            } else {
                Timber.e("Error al obtener alumnos para la clase $claseId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar alumnos con profesor: ${e.message}")
            throw e
        }
    }
    
    /**
     * Método privado para eliminar la referencia a un profesor de todos los alumnos de una clase.
     * @param claseId ID de la clase
     * @param profesorId ID del profesor a eliminar (para verificación)
     */
    private suspend fun eliminarProfesorDeAlumnosDeClase(claseId: String, profesorId: String) {
        try {
            // 1. Obtener todos los alumnos de esta clase usando el método exhaustivo
            val resultAlumnos = alumnoRepository.getAlumnosByClaseId(claseId)
            
            if (resultAlumnos is Result.Success) {
                val alumnos = resultAlumnos.data
                
                Timber.d("Eliminando profesor $profesorId de ${alumnos.size} alumnos")
            
                if (alumnos.isEmpty()) {
                    Timber.w("No se encontraron alumnos para la clase $claseId")
                    return
                }
                
                // 2. Para cada alumno, eliminar el profesorId solo si coincide con el que estamos desvinculando
                var alumnosActualizados = 0
                var alumnosFallidos = 0
                
                alumnos.forEach { alumno ->
                    // Solo eliminamos si el profesor asignado es el que estamos desvinculando
                    if (alumno.profesorId == profesorId) {
                        Timber.d("Eliminando profesor de alumno ${alumno.nombre} ${alumno.apellidos} (${alumno.dni})")
                        
                        val resultado = alumnoRepository.eliminarProfesor(alumno.dni)
                        
                        if (resultado is Result.Success) {
                            alumnosActualizados++
                            Timber.d("Alumno ${alumno.dni} actualizado: profesor eliminado correctamente")
                        } else if (resultado is Result.Error) {
                            alumnosFallidos++
                            Timber.e(resultado.exception, "Error al eliminar profesor para alumno ${alumno.dni}")
                            
                            // Reintento una vez más si falló la primera vez
                            Timber.d("Reintentando eliminación para alumno ${alumno.dni}")
                            val reintentoResultado = alumnoRepository.eliminarProfesor(alumno.dni)
                            
                            if (reintentoResultado is Result.Success) {
                                alumnosActualizados++
                                alumnosFallidos--
                                Timber.d("Reintento exitoso para alumno ${alumno.dni}")
                            }
                        }
                    } else {
                        Timber.d("El alumno ${alumno.dni} no tiene asignado al profesor $profesorId (tiene: ${alumno.profesorId ?: "ninguno"})")
                    }
                }
                
                Timber.d("Eliminación de profesor completada: $alumnosActualizados exitosos, $alumnosFallidos fallidos")
                
                if (alumnosFallidos > 0) {
                    Timber.w("Atención: No se pudo eliminar el profesor de $alumnosFallidos alumnos")
                }
            } else {
                Timber.e("Error al obtener alumnos para la clase $claseId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar profesor de alumnos: ${e.message}")
            throw e
        }
    }
    
    /**
     * Verifica si un profesor está asignado a una clase específica
     */
    fun isProfesorAsignadoAClase(profesorId: String, claseId: String): Boolean {
        val clasesAsignadas = _uiState.value.clasesAsignadas[profesorId] ?: emptyMap()
        // Buscar en el mapa de clases asignadas la clase con el ID específico y verificar si el valor es true
        return clasesAsignadas.entries.any { (clase, asignada) -> 
            clase.id == claseId && asignada 
        }
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
    
    /**
     * Inicializa el ViewModel con una clase específica.
     * Este método se utiliza cuando se navega directamente a la vinculación de un profesor
     * a una clase específica desde la pantalla de detalle de clase.
     * 
     * @param claseId ID de la clase para inicializar
     */
    fun inicializarConClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Primero obtenemos la información de la clase
                when (val result = claseRepository.getClaseById(claseId)) {
                    is Result.Success -> {
                        val clase = result.data
                        
                        // Obtenemos el ID del curso y del centro
                        val cursoId = clase.cursoId
                        
                        // Cargamos el curso
                        val cursoResult = cursoRepository.getCursoById(cursoId)
                        if (cursoResult is Result.Success) {
                            val curso = cursoResult.data
                            val centroId = curso.centroId
                            
                            // Actualizamos el estado con el centro seleccionado
                            if (centroId.isNotEmpty()) {
                                // Cargamos el centro
                                val centroResult = centroRepository.getCentroById(centroId)
                                if (centroResult is Result.Success) {
                                    val centro = centroResult.data
                                    _uiState.update { it.copy(
                                        centroId = centroId,
                                        centroSeleccionado = centro
                                    )}
                                }
                                
                                // Cargamos los cursos del centro
                                cargarCursos(centroId)
                                
                                // Cargamos los profesores del centro
                                cargarProfesores(centroId)
                                
                                // Actualizamos el curso seleccionado
                                _uiState.update { it.copy(
                                    cursoSeleccionado = curso
                                )}
                                
                                // Cargamos las clases del curso
                                cargarClasesPorCurso(cursoId)
                                
                                // Seleccionamos la clase específica
                                _uiState.update { it.copy(
                                    claseSeleccionada = clase
                                )}
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al cargar la clase: ${result.exception?.message ?: "Error desconocido"}",
                            isLoading = false
                        )}
                    }
                    else -> {} // No hacemos nada en caso de Result.Loading
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error inesperado: ${e.message ?: "Error desconocido"}",
                    isLoading = false
                )}
                timber.log.Timber.e(e, "Error al inicializar con clase específica")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }
    
    /**
     * Actualiza manualmente el profesorId en todos los alumnos de una clase.
     * Este método puede ser llamado desde la UI para corregir situaciones donde
     * los alumnos no se actualizaron correctamente al vincular un profesor.
     * 
     * @param claseId ID de la clase
     * @param profesorId ID del profesor a asignar
     */
    fun actualizarManualmenteAlumnosClase(claseId: String, profesorId: String) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                Timber.d("Iniciando actualización manual de alumnos para la clase $claseId con profesor $profesorId")
                
                // Usa el método privado que ya implementamos
                actualizarAlumnosDeClaseConProfesor(claseId, profesorId)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        mensaje = "Alumnos actualizados correctamente",
                        showSuccessMessage = true
                    ) 
                }
            } catch (e: Exception) {
                Timber.e(e, "Error en actualización manual de alumnos: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al actualizar alumnos: ${e.message}"
                    ) 
                }
            }
        }
    }
} 