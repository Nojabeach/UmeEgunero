package com.tfg.umeegunero.feature.common.academico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para el gestor académico.
 * 
 * Contiene toda la información necesaria para representar el estado actual
 * de la pantalla de gestión académica, incluyendo listas de centros, cursos,
 * clases, selecciones actuales y estados de carga.
 * 
 * @property centros Lista de centros educativos disponibles
 * @property cursos Lista de cursos del centro seleccionado
 * @property clases Lista de clases del curso seleccionado
 * @property selectedCentro Centro actualmente seleccionado
 * @property selectedCurso Curso actualmente seleccionado
 * @property isLoadingCentros Indica si se están cargando los centros
 * @property isLoadingCursos Indica si se están cargando los cursos
 * @property isLoadingClases Indica si se están cargando las clases
 * @property error Mensaje de error en caso de fallo, o null si no hay error
 * @property centroMenuExpanded Indica si el menú desplegable de centros está expandido
 * @property cursoMenuExpanded Indica si el menú desplegable de cursos está expandido
 */
data class GestorAcademicoUiState(
    val centros: List<Centro> = emptyList(),
    val cursos: List<Curso> = emptyList(),
    val clases: List<Clase> = emptyList(),
    val selectedCentro: Centro? = null,
    val selectedCurso: Curso? = null,
    val isLoadingCentros: Boolean = false,
    val isLoadingCursos: Boolean = false,
    val isLoadingClases: Boolean = false,
    val error: String? = null,
    val centroMenuExpanded: Boolean = false,
    val cursoMenuExpanded: Boolean = false
)

/**
 * ViewModel para la gestión académica de centros, cursos y clases.
 * 
 * Proporciona funcionalidad para cargar y gestionar la estructura académica
 * de la aplicación, permitiendo navegar entre centros, cursos y clases, y
 * gestionar las relaciones entre estos elementos.
 * 
 * @property centroRepository Repositorio para operaciones relacionadas con centros educativos
 * @property cursoRepository Repositorio para operaciones relacionadas con cursos
 * @property claseRepository Repositorio para operaciones relacionadas con clases
 * @property usuarioRepository Repositorio para operaciones relacionadas con usuarios
 */
@HiltViewModel
class GestorAcademicoViewModel @Inject constructor(
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val claseRepository: ClaseRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GestorAcademicoUiState())
    
    /**
     * Estado observable de la UI para la gestión académica.
     */
    val uiState: StateFlow<GestorAcademicoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Verificar tipo de usuario para diagnóstico
            try {
                val usuario = usuarioRepository.obtenerUsuarioActual()
                val perfilAdminCentro = usuario?.perfiles?.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                val perfilAdminApp = usuario?.perfiles?.find { it.tipo == TipoUsuario.ADMIN_APP }
                
                when {
                    perfilAdminCentro != null -> {
                        val centroId = perfilAdminCentro.centroId ?: "No asignado"
                        Timber.d("⚠️⚠️⚠️ INICIO: Usuario detectado como ADMIN_CENTRO para centro: $centroId")
                    }
                    perfilAdminApp != null -> {
                        Timber.d("⚠️⚠️⚠️ INICIO: Usuario detectado como ADMIN_APP con acceso a todos los centros")
                    }
                    else -> {
                        Timber.w("⚠️⚠️⚠️ INICIO: No se pudo detectar un perfil de administrador en el usuario")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar el tipo de usuario")
            }
        }
        
        cargarCentros()
    }

    /**
     * Carga la lista de centros educativos desde el repositorio.
     */
    fun cargarCentros() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCentros = true) }
            try {
                // Primero verificamos si el usuario es ADMIN_CENTRO
                val usuario = usuarioRepository.obtenerUsuarioActual()
                
                if (usuario == null) {
                    Timber.e("❌❌ ERROR: obtenerUsuarioActual() devolvió null - No hay usuario autenticado")
                    _uiState.update { it.copy(error = "No hay usuario autenticado", isLoadingCentros = false) }
                    return@launch
                }
                
                Timber.d("✅ Usuario obtenido: ${usuario.email}, DNI: ${usuario.dni}")
                Timber.d("📋 Perfiles del usuario: ${usuario.perfiles.map { "${it.tipo} (centroId: ${it.centroId})" }}")
                
                val perfilAdminCentro = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                
                if (perfilAdminCentro != null) {
                    // Si es ADMIN_CENTRO, solo cargamos su centro específico
                    val centroId = perfilAdminCentro.centroId
                    Timber.d("👮 Usuario es ADMIN_CENTRO para centroId: $centroId - SOLO se mostrará este centro")
                    
                    if (!centroId.isNullOrEmpty()) {
                        when (val centroResult = centroRepository.getCentroById(centroId)) {
                            is Result.Success -> {
                                // Solo actualizar con el único centro permitido
                                val centro = centroResult.data
                                _uiState.update { it.copy(
                                    centros = listOf(centro), 
                                    selectedCentro = centro,  // Preseleccionar inmediatamente
                                    isLoadingCentros = false
                                )}
                                // Cargar cursos de este centro
                                Timber.d("🔒 ADMIN_CENTRO: Seleccionando único centro permitido: ${centro.nombre} (${centro.id})")
                                observarCursos(centroId)
                                return@launch // Aseguramos que no continúe con la carga de todos los centros
                            }
                            is Result.Error -> {
                                _uiState.update { it.copy(
                                    error = "Error al cargar centro: ${centroResult.exception?.message}", 
                                    isLoadingCentros = false
                                )}
                                return@launch
                            }
                            else -> { /* Loading state is handled */ }
                        }
                    } else {
                        _uiState.update { it.copy(error = "ADMIN_CENTRO sin centro asignado", isLoadingCentros = false) }
                        return@launch
                    }
                } 
                
                // Solo si NO es ADMIN_CENTRO, cargamos todos los centros (para ADMIN_APP)
                Timber.d("👑 Usuario es ADMIN_APP: Cargando TODOS los centros")
                when (val centrosResult = centroRepository.getAllCentros()) {
                    is Result.Success -> {
                        _uiState.update { it.copy(centros = centrosResult.data, isLoadingCentros = false) }
                        // Solo si es ADMIN_APP seleccionamos el primer centro de la lista
                        centrosResult.data.firstOrNull()?.let { primerCentro ->
                            Timber.d("👑 ADMIN_APP seleccionando primer centro: ${primerCentro.nombre} (${primerCentro.id})")
                            _uiState.update { it.copy(selectedCentro = primerCentro) }
                            observarCursos(primerCentro.id)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al cargar centros: ${centrosResult.exception?.message}", 
                            isLoadingCentros = false
                        )}
                    }
                    else -> { /* Loading state is handled */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al cargar centros")
                _uiState.update { it.copy(error = "Error inesperado al cargar centros: ${e.message}", isLoadingCentros = false) }
            }
        }
    }

    /**
     * Actualiza el centro seleccionado y carga sus cursos.
     * Verifica los permisos del usuario antes de realizar la selección.
     * 
     * @param centro Centro a seleccionar
     */
    fun onCentroSelected(centro: Centro) {
        viewModelScope.launch {
            try {
                Timber.d("🔄 Iniciando selección de centro: ${centro.nombre} (${centro.id})")
                
                // Verificar permisos - Si es ADMIN_CENTRO, solo puede seleccionar su centro asignado
                val usuario = usuarioRepository.obtenerUsuarioActual()
                val perfilAdminCentro = usuario?.perfiles?.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                
                if (perfilAdminCentro != null) {
                    val centroIdPermitido = perfilAdminCentro.centroId
                    Timber.d("🔒 Verificando permisos: Usuario ADMIN_CENTRO (centroId=${centroIdPermitido}) intenta seleccionar centro (${centro.id})")
                    
                    // Si es ADMIN_CENTRO intentando seleccionar otro centro, bloqueamos
                    if (centroIdPermitido != centro.id) {
                        Timber.w("❌ ACCESO DENEGADO: El ADMIN_CENTRO (centroId=${centroIdPermitido}) intenta seleccionar otro centro (${centro.id})")
                        _uiState.update { it.copy(
                            error = "No tienes permiso para seleccionar el centro ${centro.nombre}. Solo puedes gestionar tu centro asignado.",
                            centroMenuExpanded = false
                        )}
                        return@launch
                    } else {
                        Timber.d("✅ ACCESO PERMITIDO: El usuario tiene permiso para seleccionar su centro asignado ${centro.id}")
                    }
                } else {
                    Timber.d("🔓 Usuario es ADMIN_APP: Tiene permiso para seleccionar cualquier centro")
                }
                
                // Continuar con la selección normal del centro
                Timber.d("✅ Centro seleccionado: ${centro.nombre} (${centro.id})")
                _uiState.update { it.copy(
                    selectedCentro = centro, 
                    selectedCurso = null, 
                    cursos = emptyList(), 
                    clases = emptyList(),
                    centroMenuExpanded = false,  // Cerrar el menú desplegable
                    error = null  // Limpiar cualquier error previo
                )}
                
                // Usar el ID del centro seleccionado
                val centroId = centro.id
                Timber.d("🔄 Observando cursos del centro ID: $centroId")
                observarCursos(centroId)
            } catch (e: Exception) {
                Timber.e(e, "❌❌ Error al seleccionar centro: ${e.message}")
                _uiState.update { it.copy(error = "Error al seleccionar centro: ${e.message}") }
            }
        }
    }

    /**
     * Observa los cambios en los cursos asociados a un centro específico usando Flows.
     * 
     * @param centroId Identificador único del centro del que observar los cursos
     */
    private fun observarCursos(centroId: String) {
        _uiState.update { it.copy(isLoadingCursos = true, error = null) }
        
        cursoRepository.obtenerCursosPorCentroFlow(centroId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        Timber.d("Cursos actualizados para centro $centroId: ${result.data.size}")
                        _uiState.update { it.copy(cursos = result.data, isLoadingCursos = false) }
                        if (_uiState.value.selectedCurso == null && result.data.isNotEmpty()) {
                            onCursoSelected(result.data.first())
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al observar cursos del centro $centroId")
                        _uiState.update { it.copy(error = result.exception?.message ?: "Error al cargar cursos", isLoadingCursos = false) }
                    }
                    is Result.Loading -> {
                         _uiState.update { it.copy(isLoadingCursos = true) }
                    }
                }
            }
            .catch { e -> 
                Timber.e(e, "Excepción en el Flow de cursos del centro $centroId")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoadingCursos = false) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Actualiza el curso seleccionado y carga sus clases.
     * 
     * @param curso Curso a seleccionar
     */
    fun onCursoSelected(curso: Curso) {
        _uiState.update { it.copy(selectedCurso = curso, clases = emptyList()) }
        observarClases(curso.id)
    }

    /**
     * Inicializa el viewModel con un cursoId específico, cargando el curso y sus clases.
     * Útil cuando se llega a la pantalla directamente con un cursoId sin tener el objeto Curso.
     * 
     * @param cursoId ID del curso a seleccionar
     */
    fun inicializarConCursoId(cursoId: String) {
        viewModelScope.launch {
            try {
                Timber.d("🚀 Inicializando con cursoId: $cursoId")
                
                // Log de estado actual
                Timber.d("📊 Estado actual: cursos=${_uiState.value.cursos.size}, selectedCurso=${_uiState.value.selectedCurso}")
                
                // Primero comprobamos si ya tenemos el curso cargado
                val cursoActual = _uiState.value.cursos.find { it.id == cursoId }
                
                if (cursoActual != null) {
                    Timber.d("📚 Curso encontrado en lista actual: ${cursoActual.nombre}")
                    onCursoSelected(cursoActual)
                    return@launch
                }
                
                // Si no está en la lista, intentamos obtenerlo del repositorio
                Timber.d("🔍 Curso no encontrado en lista, consultando repositorio...")
                when (val result = cursoRepository.getCursoById(cursoId)) {
                    is Result.Success -> {
                        val curso = result.data
                        Timber.d("✅ Curso obtenido del repositorio: ${curso.nombre}")
                        onCursoSelected(curso)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌ Error al obtener curso por ID")
                        _uiState.update { it.copy(error = "Error al obtener curso: ${result.exception?.message}") }
                    }
                    else -> { Timber.d("⏳ Esperando resultado de consulta de curso...") }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌❌ Excepción al inicializar con cursoId")
                _uiState.update { it.copy(error = "Error inesperado: ${e.message}") }
            }
        }
    }

    /**
     * Observa los cambios en las clases asociadas a un curso específico usando Flows.
     * 
     * @param cursoId Identificador único del curso del que observar las clases
     */
    private fun observarClases(cursoId: String) {
        _uiState.update { it.copy(isLoadingClases = true, error = null) }
        
        // Comprobación de seguridad - Asegurarse de que estamos utilizando el servicio correcto para obtener las clases
        Timber.d("🔍🔍 INICIANDO observación de clases para el curso ID: '$cursoId'")
        Timber.d("📊 Estado actual: clases=${_uiState.value.clases.size}, isLoadingClases=${_uiState.value.isLoadingClases}")
        
        // Verificar que el ID no esté vacío
        if (cursoId.isBlank()) {
            Timber.e("❌❌ ERROR: Se intentó observar clases con un cursoId vacío!")
            _uiState.update { it.copy(error = "ID de curso no válido", isLoadingClases = false) }
            return
        }
        
        // Añadir log adicional para verificar el curso actual
        val selectedCurso = _uiState.value.selectedCurso
        Timber.d("🔍 Curso seleccionado actual: ${selectedCurso?.id}, ${selectedCurso?.nombre}")
        
        claseRepository.obtenerClasesPorCursoFlow(cursoId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        Timber.d("✅✅ SUCCESS: Clases actualizadas para curso $cursoId: ${result.data.size}")
                        result.data.forEach { clase ->
                            Timber.d("📝 Clase encontrada: id=${clase.id}, nombre=${clase.nombre}, aula=${clase.aula}, cursoId=${clase.cursoId}")
                        }
                        _uiState.update { it.copy(clases = result.data, isLoadingClases = false) }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌❌ ERROR: Error al observar clases del curso $cursoId")
                        _uiState.update { it.copy(error = result.exception?.message ?: "Error al cargar clases", isLoadingClases = false) }
                    }
                    is Result.Loading -> {
                         Timber.d("⏳ LOADING: Cargando clases para curso $cursoId")
                         _uiState.update { it.copy(isLoadingClases = true) }
                    }
                }
            }
            .catch { e -> 
                Timber.e(e, "❌❌❌ CATCH: Excepción en el Flow de clases del curso $cursoId")
                _uiState.update { it.copy(error = e.message ?: "Error inesperado", isLoadingClases = false) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Actualiza el estado de expansión del menú de centros.
     * Para un ADMIN_CENTRO, no permite expandir el menú ya que solo tiene acceso a su centro asignado.
     * 
     * @param expanded Estado de expansión del menú
     */
    fun onCentroMenuExpandedChanged(expanded: Boolean) {
        viewModelScope.launch {
            try {
                // Si están intentando expandir el menú (no cerrarlo), verificamos permisos
                if (expanded) {
                    val usuario = usuarioRepository.obtenerUsuarioActual()
                    val perfilAdminCentro = usuario?.perfiles?.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                    
                    if (perfilAdminCentro != null) {
                        // Un ADMIN_CENTRO no debería poder cambiar de centro
                        Timber.d("🔒 Bloqueando expansión del menú de centros para ADMIN_CENTRO")
                        _uiState.update { it.copy(
                            centroMenuExpanded = false,
                            error = "Como administrador de centro, solo puedes gestionar tu centro asignado."
                        )}
                        return@launch
                    }
                }
                
                // Solo ADMIN_APP puede expandir/contraer el menú libremente
                _uiState.update { it.copy(centroMenuExpanded = expanded) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cambiar estado del menú de centros")
                _uiState.update { it.copy(centroMenuExpanded = false) }
            }
        }
    }

    /**
     * Actualiza el estado de expansión del menú de cursos.
     * 
     * @param expanded Estado de expansión del menú
     */
    fun onCursoMenuExpandedChanged(expanded: Boolean) {
        _uiState.update { it.copy(cursoMenuExpanded = expanded) }
    }

    /**
     * Elimina un curso específico del sistema.
     * 
     * @param cursoId Identificador único del curso a eliminar
     */
    fun eliminarCurso(cursoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCursos = true) }
            try {
                when (val result = cursoRepository.deleteCurso(cursoId)) {
                    is Result.Success -> {
                         _uiState.update { it.copy(isLoadingCursos = false) }
                        Timber.d("Curso $cursoId eliminado correctamente.")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar curso: ${result.exception?.message}", isLoadingCursos = false) }
                    }
                    else -> { _uiState.update { it.copy(isLoadingCursos = false) } }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al eliminar curso")
                _uiState.update { it.copy(error = "Error inesperado al eliminar curso: ${e.message}", isLoadingCursos = false) }
            }
        }
    }

    /**
     * Elimina una clase específica del sistema.
     * 
     * @param claseId Identificador único de la clase a eliminar
     */
    fun eliminarClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingClases = true) }
            try {
                when (val result = claseRepository.eliminarClase(claseId)) {
                    is Result.Success<*> -> {
                         _uiState.update { it.copy(isLoadingClases = false) }
                         Timber.d("Clase $claseId eliminada correctamente.")
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al eliminar clase: ${result.exception?.message}", isLoadingClases = false) }
                    }
                    else -> { _uiState.update { it.copy(isLoadingClases = false) } }
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción al eliminar clase")
                _uiState.update { it.copy(error = "Error inesperado al eliminar clase: ${e.message}", isLoadingClases = false) }
            }
        }
    }

    /**
     * Carga un centro específico por su ID y verifica los permisos.
     * Si el usuario es ADMIN_CENTRO, solo puede cargar su centro asignado.
     * 
     * @param centroId Identificador único del centro a cargar
     */
    fun cargarCentroPorId(centroId: String) {
        viewModelScope.launch {
            try {
                Timber.d("🔍 Cargando centro por ID: $centroId")
                
                // Verificar permisos - Si es ADMIN_CENTRO, solo puede ver su centro asignado
                val usuario = usuarioRepository.obtenerUsuarioActual()
                val perfilAdminCentro = usuario?.perfiles?.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                
                if (perfilAdminCentro != null) {
                    val centroPerfil = perfilAdminCentro.centroId
                    Timber.d("🔒 Verificando permisos: Usuario ADMIN_CENTRO (centroId=${centroPerfil}) intenta acceder a centro (${centroId})")
                    
                    // Si es ADMIN_CENTRO intentando ver otro centro, bloqueamos
                    if (centroPerfil != centroId) {
                        Timber.w("❌ ACCESO DENEGADO: El ADMIN_CENTRO (centroId=${centroPerfil}) intenta ver otro centro (${centroId})")
                        _uiState.update { it.copy(
                            error = "No tienes permiso para acceder al centro $centroId. Solo puedes gestionar el centro $centroPerfil", 
                            isLoadingCentros = false
                        ) }
                        return@launch
                    } else {
                        Timber.d("✅ ACCESO PERMITIDO: El usuario tiene permiso para ver el centro $centroId")
                    }
                }
                
                // Continuar con la carga del centro
                when (val result = centroRepository.getCentroById(centroId)) {
                    is Result.Success -> {
                        val centro = result.data
                        Timber.d("✅ Centro cargado exitosamente: ${centro.nombre} (${centro.id})")
                        _uiState.update { it.copy(selectedCentro = centro, cursos = emptyList(), clases = emptyList()) }
                        observarCursos(centroId)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "❌ Error al cargar centro por ID: $centroId")
                        _uiState.update { it.copy(error = "Error al cargar centro: ${result.exception?.message}") }
                    }
                    else -> { Timber.d("⏳ Esperando resultado de consulta de centro...") }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌❌ Excepción al cargar centro por ID")
                _uiState.update { it.copy(error = "Error inesperado: ${e.message}") }
            }
        }
    }
} 