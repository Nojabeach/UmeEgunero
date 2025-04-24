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
                
                // Intentar obtener el centroId del usuario con múltiples métodos
                val centroId = obtenerCentroIdDelUsuarioActual(usuarioActual.documentId)
                
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
                
                // Cargar cursos y profesores en paralelo (ambos incluyendo inactivos)
                Timber.d("Iniciando carga paralela de cursos y profesores")
                
                try {
                    // Cargar cursos sin filtrar por activos
                    when (val cursoResult = cursoRepository.obtenerCursosPorCentroResult(centroId, soloActivos = false)) {
                        is Result.Success -> {
                            val cursos = cursoResult.data
                            Timber.d("Cursos cargados: ${cursos.size}")
                            
                            // Mostrar todos los cursos para diagnóstico
                            cursos.forEach { curso ->
                                Timber.d("Curso: ${curso.nombre} (ID: ${curso.id}, activo: ${curso.activo})")
                            }
                            
                            _uiState.update { it.copy(cursos = cursos) }
                            
                            if (cursos.isNotEmpty()) {
                                // Si hay cursos, seleccionamos el primero y cargamos sus clases
                                val primerCurso = cursos.first()
                                Timber.d("Seleccionando primer curso: ${primerCurso.nombre}")
                                
                                _uiState.update { it.copy(cursoSeleccionado = primerCurso) }
                                
                                // Cargar clases del primer curso seleccionado
                                cargarClases(primerCurso.id)
                            } else {
                                Timber.w("No se encontraron cursos para el centro $centroId")
                            }
                        }
                        is Result.Error -> {
                            Timber.e(cursoResult.exception, "Error al cargar cursos: ${cursoResult.exception?.message}")
                            _uiState.update { 
                                it.copy(error = "Error al cargar cursos: ${cursoResult.exception?.message}")
                            }
                        }
                        is Result.Loading -> { /* No hacer nada */ }
                    }
                    
                    // Cargar profesores incluyendo inactivos
                    cargarProfesores(centroId, incluirInactivos = true)
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error durante la carga de datos inicial: ${e.message}")
                    _uiState.update {
                        it.copy(
                            error = "Error durante la carga de datos: ${e.message}",
                            isLoading = false
                        )
                    }
                }
                
                // Finalizar carga
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
     * Obtiene el ID del centro asociado al usuario actual
     * Intenta múltiples métodos para obtener el centroId
     */
    private suspend fun obtenerCentroIdDelUsuarioActual(usuarioId: String): String? {
        Timber.d("=== DIAGNÓSTICO DE OBTENCIÓN DE CENTRO ID ===")
        Timber.d("Intentando obtener centroId para el usuario: $usuarioId")
        
        // MÉTODO 1: Buscar en perfiles del usuario
        val result = usuarioRepository.getUsuarioById(usuarioId)
        if (result is Result.Success) {
            val usuario = result.data
            Timber.d("Usuario obtenido: ${usuario.nombre} ${usuario.apellidos}")
            Timber.d("Perfiles en el usuario: ${usuario.perfiles.size}")
            
            // Mostrar detalles de cada perfil para diagnóstico
            usuario.perfiles.forEachIndexed { index, perfil ->
                Timber.d("Perfil #$index: tipo=${perfil.tipo}, centroId=${perfil.centroId}")
            }
            
            // Buscar centroId en perfiles, probando diferentes tipos de usuario
            val perfilConCentroId = usuario.perfiles.find { it.centroId.isNotEmpty() }
            if (perfilConCentroId != null) {
                val centroId = perfilConCentroId.centroId
                Timber.d("✅ MÉTODO 1: CentroId encontrado en perfil: $centroId (tipo: ${perfilConCentroId.tipo})")
                return centroId
            }
            
            Timber.d("❌ MÉTODO 1: No se encontró centroId en ningún perfil del usuario")
        } else if (result is Result.Error) {
            Timber.e(result.exception, "❌ MÉTODO 1: Error obteniendo usuario: ${result.exception?.message}")
        }
        
        // MÉTODO 2: Usar método específico del repositorio
        try {
            val centroId = usuarioRepository.getCentroIdUsuarioActual()
            if (!centroId.isNullOrEmpty()) {
                Timber.d("✅ MÉTODO 2: CentroId obtenido con método alternativo: $centroId")
                return centroId
            } else {
                Timber.d("❌ MÉTODO 2: El método alternativo devolvió null o cadena vacía")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ MÉTODO 2: Error obteniendo centroId por método alternativo: ${e.message}")
        }
        
        // MÉTODO 3: Buscar centros donde el usuario es admin o profesor
        try {
            val centrosResult = centroRepository.getCentrosByAdminOrProfesor(usuarioId)
            if (centrosResult is Result.Success && centrosResult.data.isNotEmpty()) {
                val primerCentro = centrosResult.data.first()
                Timber.d("✅ MÉTODO 3: Centro encontrado como admin/profesor: ${primerCentro.id} - ${primerCentro.nombre}")
                return primerCentro.id
            } else {
                Timber.d("❌ MÉTODO 3: No se encontraron centros para el usuario como admin/profesor")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ MÉTODO 3: Error buscando centros del usuario: ${e.message}")
        }
        
        // MÉTODO 4: Intentar obtener todos los centros y buscar en cada uno
        try {
            val todosLosCentros = centroRepository.getAllCentros()
            if (todosLosCentros is Result.Success && todosLosCentros.data.isNotEmpty()) {
                Timber.d("MÉTODO 4: Verificando en ${todosLosCentros.data.size} centros...")
                
                // Buscar un centro donde el usuario sea profesor o admin
                for (centro in todosLosCentros.data) {
                    if (centro.adminIds.contains(usuarioId) || centro.profesorIds.contains(usuarioId)) {
                        Timber.d("✅ MÉTODO 4: Usuario encontrado en centro: ${centro.id} - ${centro.nombre}")
                        return centro.id
                    }
                }
                Timber.d("❌ MÉTODO 4: Usuario no encontrado en ningún centro")
            } else {
                Timber.d("❌ MÉTODO 4: No se pudieron obtener los centros o no hay centros")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ MÉTODO 4: Error verificando centros: ${e.message}")
        }
        
        Timber.e("❌ TODOS LOS MÉTODOS FALLARON: No se pudo determinar el centroId del usuario")
        return null
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
        cargarClases(curso.id)
    }
    
    /**
     * Carga las clases de un curso específico
     */
    fun cargarClases(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            Timber.d("=== DIAGNÓSTICO DE CARGA DE CLASES ===")
            Timber.d("Iniciando carga de clases para el curso: $cursoId")
            
            when (val result = claseRepository.getClasesByCursoId(cursoId)) {
                is Result.Success -> {
                    val clases = result.data
                    Timber.d("Clases cargadas exitosamente: ${clases.size}")
                    
                    // Mostrar detalles de cada clase para diagnóstico
                    clases.forEach { clase ->
                        Timber.d("- Clase: ${clase.nombre}, Aula: ${clase.aula}, ProfesorID: ${clase.profesorTitularId}")
                    }
                    
                    _uiState.update { 
                        it.copy(
                            clases = clases,
                            isLoading = false
                        )
                    }
                    
                    if (clases.isEmpty()) {
                        Timber.w("⚠️ No hay clases disponibles para el curso $cursoId")
                        // Intentar método alternativo si es necesario
                        cargarClasesAlternativo(cursoId)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error al cargar clases: ${result.exception?.message}")
                    _uiState.update { 
                        it.copy(
                            error = "Error al cargar clases: ${result.exception?.message}",
                            isLoading = false
                        )
                    }
                    
                    // Intentar método alternativo
                    cargarClasesAlternativo(cursoId)
                }
                is Result.Loading -> {
                    // Manejar estado de carga si es necesario
                }
            }
        }
    }
    
    /**
     * Método alternativo para cargar clases usando obtenerClasesPorCurso
     */
    private fun cargarClasesAlternativo(cursoId: String) {
        viewModelScope.launch {
            Timber.d("Intentando cargar clases con método alternativo")
            
            try {
                when (val result = cursoRepository.obtenerClasesPorCurso(cursoId)) {
                    is Result.Success<*> -> {
                        val clases = result.data as List<Clase>
                        Timber.d("Clases cargadas con método alternativo: ${clases.size}")
                        
                        _uiState.update { 
                            it.copy(
                                clases = clases,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error en método alternativo: ${result.exception?.message}")
                    }
                    else -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error en carga alternativa de clases: ${e.message}")
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