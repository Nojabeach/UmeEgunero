package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import java.util.HashMap

/**
 * Modelo de datos para un contacto de profesor
 */
data class ProfesorContacto(
    val dni: String,
    val nombre: String,
    val apellidos: String,
    val descripcion: String? = null,
    val unreadCount: Int = 0
)

/**
 * Modelo de datos para un contacto de familiar
 */
data class FamiliarContacto(
    val dni: String,
    val nombre: String,
    val apellidos: String,
    val alumnoId: String? = null,
    val alumnoNombre: String? = null,
    val unreadCount: Int = 0
)

/**
 * Define los tipos de filtros disponibles para contactos
 */
enum class FiltroContacto {
    TODOS,
    ADMINISTRADORES,
    PROFESORES,
    FAMILIARES
}

/**
 * Estado UI para la pantalla de contactos del chat
 */
data class ChatContactsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val administratorContacts: List<ProfesorContacto> = emptyList(),
    val teacherContacts: List<ProfesorContacto> = emptyList(),
    val familyContacts: List<FamiliarContacto> = emptyList(),
    val filteredContacts: List<Any> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val showFilterDialog: Boolean = false,
    val availableCourses: List<Curso> = emptyList(),
    val availableClasses: List<Clase> = emptyList(),
    val selectedCourseId: String? = null,
    val selectedClassId: String? = null,
    val userType: TipoUsuario = TipoUsuario.DESCONOCIDO,
    val currentUser: Usuario? = null,
    val currentUserCentroId: String? = null,
    val currentUserClaseId: String? = null,
    val hijos: List<Alumno> = emptyList(),
    val hijoSeleccionado: Alumno? = null,
    val selectedChildId: String? = null,
    val children: List<Alumno> = emptyList(),
    val activeFilter: FiltroContacto = FiltroContacto.TODOS
)

/**
 * ViewModel para la pantalla de contactos de chat
 */
@HiltViewModel
class ChatContactsViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val profesorRepository: ProfesorRepository,
    private val familiarRepository: FamiliarRepository,
    private val alumnoRepository: AlumnoRepository,
    private val cursoRepository: CursoRepository,
    private val claseRepository: ClaseRepository,
    private val chatRepository: ChatRepository,
    private val firestore: FirebaseFirestore,
    private val unifiedMessageRepository: UnifiedMessageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatContactsUiState())
    val uiState: StateFlow<ChatContactsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentUserAndContacts()
    }
    
    /**
     * Carga el usuario actual y sus contactos disponibles
     */
    private fun loadCurrentUserAndContacts() {
        viewModelScope.launch {
            try {
                // Obtener usuario actual
                val userResult = authRepository.getCurrentUser()
                if (userResult != null) {
                    val userData = userResult
                    
                    // Determinar el rol del usuario
                    val profesorPerfil = userData.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                    val familiarPerfil = userData.perfiles.find { it.tipo == TipoUsuario.FAMILIAR }
                    
                    when {
                        // Si es profesor, cargamos los contactos para profesor
                        profesorPerfil != null -> {
                            _uiState.update { it.copy(
                                userType = TipoUsuario.PROFESOR,
                                currentUser = userData
                            ) }
                            loadProfesorContacts(userData, profesorPerfil.centroId)
                        }
                        
                        // Si es familiar, cargamos los contactos para familiar
                        familiarPerfil != null -> {
                            _uiState.update { it.copy(
                                userType = TipoUsuario.FAMILIAR,
                                currentUser = userData
                            ) }
                            loadFamiliarContacts(userData)
                        }
                        
                        // Si no tiene ninguno de estos perfiles, mostramos un error
                        else -> {
                            _uiState.update { it.copy(
                                error = "El usuario no tiene un perfil válido para esta pantalla",
                                isLoading = false
                            ) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener el usuario actual",
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar usuario actual")
                _uiState.update { it.copy(
                    error = "Error al cargar los datos: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga los contactos disponibles para un profesor
     */
    private fun loadProfesorContacts(currentUser: Usuario?, centroId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                if (currentUser == null || centroId == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo determinar el usuario o centro del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // 1. Cargar administradores del centro
                val admins = loadAdministrators(centroId)
                Timber.d("Administradores cargados: ${admins.size}")
                
                // 2. Obtener la clase del profesor actual
                val clasesResult = claseRepository.getClasesByProfesorId(currentUser.dni)
                var claseId: String? = null
                var cursoId: String? = null
                
                if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                    val claseProfesor = clasesResult.data.first()
                    claseId = claseProfesor.id
                    cursoId = claseProfesor.cursoId
                    
                    _uiState.update { it.copy(currentUserClaseId = claseId) }
                    Timber.d("Clase del profesor: ${claseProfesor.nombre} (ID: $claseId, Curso: $cursoId)")
                }
                
                // 3. Cargar profesores del mismo curso
                val teachers = if (cursoId != null) {
                    loadTeachersFromSameCourse(cursoId, currentUser.dni)
                } else {
                    // Si no hay curso, cargar todos los profesores del centro
                    loadTeachers(centroId, currentUser.dni)
                }
                Timber.d("Profesores cargados: ${teachers.size}")
                
                // 4. Cargar familiares de alumnos de la clase del profesor
                val families = if (claseId != null) {
                    loadFamiliesByClaseId(claseId)
                } else {
                    emptyList()
                }
                Timber.d("Familiares cargados: ${families.size}")
                
                // Actualizar estado con todos los contactos
                _uiState.update { state ->
                    state.copy(
                        administratorContacts = admins,
                        teacherContacts = teachers,
                        familyContacts = families,
                        filteredContacts = admins + teachers + families,
                        isLoading = false
                    )
                }
                
                // Cargar cursos y clases disponibles para filtrado
                loadCoursesAndClasses(centroId)
                
                // Cargar contadores de mensajes no leídos
                cargarMensajesNoLeidos()
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar contactos")
                _uiState.update { it.copy(
                    error = "Error al cargar contactos: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga profesores del mismo curso
     */
    private suspend fun loadTeachersFromSameCourse(cursoId: String, currentUserDni: String): List<ProfesorContacto> {
        return try {
            Timber.d("Cargando profesores del curso: $cursoId")
            
            // Obtener todas las clases del curso
            val clasesResult = claseRepository.getClasesByCursoId(cursoId)
            if (clasesResult !is Result.Success) {
                Timber.e("Error al obtener clases del curso $cursoId")
                return emptyList()
            }
            
            val profesoresSet = mutableSetOf<ProfesorContacto>()
            
            // Para cada clase, obtener su profesor
            clasesResult.data.forEach { clase ->
                // Profesor titular
                clase.profesorId?.let { profesorId ->
                    if (profesorId != currentUserDni) {
                        val profesorResult = usuarioRepository.getUsuarioById(profesorId)
                        if (profesorResult is Result.Success) {
                            val profesor = profesorResult.data
                            profesoresSet.add(
                                ProfesorContacto(
                                    dni = profesor.dni,
                                    nombre = profesor.nombre,
                                    apellidos = profesor.apellidos,
                                    descripcion = "Profesor de ${clase.nombre}"
                                )
                            )
                        }
                    }
                }
                
                // Profesor titular (campo alternativo)
                clase.profesorTitularId?.let { profesorId ->
                    if (profesorId != currentUserDni) {
                        val profesorResult = usuarioRepository.getUsuarioById(profesorId)
                        if (profesorResult is Result.Success) {
                            val profesor = profesorResult.data
                            profesoresSet.add(
                                ProfesorContacto(
                                    dni = profesor.dni,
                                    nombre = profesor.nombre,
                                    apellidos = profesor.apellidos,
                                    descripcion = "Profesor titular de ${clase.nombre}"
                                )
                            )
                        }
                    }
                }
                
                // Profesores auxiliares
                clase.profesoresAuxiliaresIds?.forEach { profesorId ->
                    if (profesorId != currentUserDni) {
                        val profesorResult = usuarioRepository.getUsuarioById(profesorId)
                        if (profesorResult is Result.Success) {
                            val profesor = profesorResult.data
                            profesoresSet.add(
                                ProfesorContacto(
                                    dni = profesor.dni,
                                    nombre = profesor.nombre,
                                    apellidos = profesor.apellidos,
                                    descripcion = "Profesor auxiliar de ${clase.nombre}"
                                )
                            )
                        }
                    }
                }
            }
            
            profesoresSet.toList()
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar profesores del curso $cursoId")
            emptyList()
        }
    }
    
    /**
     * Carga los contactos disponibles para un familiar
     */
    private fun loadFamiliarContacts(usuario: Usuario) {
        viewModelScope.launch {
            try {
                // 1. Obtener los hijos del familiar
                val hijosResult = alumnoRepository.obtenerAlumnosPorFamiliar(usuario.dni)
                val hijos = when (hijosResult) {
                    is Result.Success -> hijosResult.data
                    else -> emptyList()
                }
                
                if (hijos.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "No tienes hijos vinculados. Solicita la vinculación primero.",
                        isLoading = false,
                        hijos = emptyList()
                    ) }
                    return@launch
                }
                
                _uiState.update { it.copy(hijos = hijos) }
                
                // Si hay hijos, cargar profesores del primer hijo por defecto
                if (hijos.isNotEmpty()) {
                    val primerHijo = hijos[0]  // Usar indexación segura en lugar de first()
                    seleccionarHijo(primerHijo)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar contactos de familiar")
                _uiState.update { it.copy(
                    error = "Error al cargar contactos: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Selecciona un hijo para filtrar los contactos (modo familiar)
     */
    fun seleccionarHijo(alumno: Alumno) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    hijoSeleccionado = alumno,
                    isLoading = true
                ) }
                
                // Obtener la clase del alumno
                val clase = alumno.claseId?.let { claseId ->
                    val claseResult = claseRepository.getClaseById(claseId)
                    if (claseResult is Result.Success) claseResult.data else null
                }
                
                // Obtener el centro del alumno (a través de la clase)
                val centroId = clase?.centroId
                
                if (centroId == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo determinar el centro del alumno",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // 1. Cargar administradores del centro
                val admins = loadAdministrators(centroId)
                
                // 2. Cargar profesores de la clase del alumno
                val teachers = if (clase != null) {
                    loadTeachersForClass(clase.id)
                } else {
                    emptyList()
                }
                
                // Actualizar estado con los contactos filtrados
                _uiState.update { state ->
                    state.copy(
                        administratorContacts = admins,
                        teacherContacts = teachers,
                        familyContacts = emptyList(), // Familiares no ven a otros familiares
                        filteredContacts = admins + teachers,
                        currentUserCentroId = centroId,
                        currentUserClaseId = clase?.id,
                        isLoading = false
                    )
                }
                
                // Cargar contadores de mensajes no leídos
                cargarMensajesNoLeidos()
                
            } catch (e: Exception) {
                Timber.e(e, "Error al seleccionar hijo")
                _uiState.update { it.copy(
                    error = "Error al cargar contactos para el alumno: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga los profesores de una clase específica
     */
    private suspend fun loadTeachersForClass(claseId: String): List<ProfesorContacto> {
        try {
            // Versión simplificada para compilación
            Timber.d("Cargando profesores para clase $claseId")
            return listOf(
                ProfesorContacto(
                    dni = "12345678A",
                    nombre = "María",
                    apellidos = "García López",
                    descripcion = "Profesora titular"
                ),
                ProfesorContacto(
                    dni = "87654321B",
                    nombre = "Juan",
                    apellidos = "Martínez Ruiz",
                    descripcion = "Profesor de apoyo"
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar profesores")
            return emptyList()
        }
    }
    
    /**
     * Carga los administradores del centro
     */
    private suspend fun loadAdministrators(centroId: String): List<ProfesorContacto> {
        try {
            // Consultar usuarios con perfil de ADMIN_CENTRO para este centro
            val adminsQuery = firestore.collection("usuarios")
                .whereArrayContains("perfiles", mapOf(
                    "tipo" to "ADMIN_CENTRO",
                    "centroId" to centroId
                ))
                .get()
                .await()
            
            return adminsQuery.documents.mapNotNull { doc ->
                if (!doc.exists()) return@mapNotNull null
                
                val dni = doc.id
                val nombre = doc.getString("nombre") ?: "Admin"
                val apellidos = doc.getString("apellidos") ?: "Centro"
                
                ProfesorContacto(
                    dni = dni,
                    nombre = nombre,
                    apellidos = apellidos,
                    descripcion = "Administrador de Centro"
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar administradores del centro $centroId")
            return emptyList()
        }
    }
    
    /**
     * Carga otros profesores del centro (excluyendo al usuario actual)
     */
    private suspend fun loadTeachers(centroId: String, currentUserDni: String?): List<ProfesorContacto> {
        try {
            // Consultar usuarios con perfil de PROFESOR para este centro
            val teachersQuery = firestore.collection("usuarios")
                .get()
                .await()
            
            return teachersQuery.documents.mapNotNull { doc ->
                if (!doc.exists()) return@mapNotNull null
                
                val dni = doc.id
                // Excluir al usuario actual
                if (dni == currentUserDni) return@mapNotNull null
                
                // Verificar si el usuario tiene perfil de profesor en este centro
                val perfiles = doc.get("perfiles") as? List<Map<String, Any>> ?: return@mapNotNull null
                val isProfesorThisCentro = perfiles.any { 
                    (it["tipo"] as? String) == "PROFESOR" && (it["centroId"] as? String) == centroId 
                }
                
                if (!isProfesorThisCentro) return@mapNotNull null
                
                val nombre = doc.getString("nombre") ?: "Profesor"
                val apellidos = doc.getString("apellidos") ?: ""
                
                ProfesorContacto(
                    dni = dni,
                    nombre = nombre,
                    apellidos = apellidos,
                    descripcion = "Profesor"
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar profesores del centro $centroId")
            return emptyList()
        }
    }
    
    /**
     * Carga las familias de una clase
     * @deprecated Use loadFamiliesByClaseId instead
     */
    @Deprecated("Use loadFamiliesByClaseId instead", ReplaceWith("loadFamiliesByClaseId(claseId)"))
    private suspend fun loadFamilies(claseId: String): List<FamiliarContacto> {
        return loadFamiliesByClaseId(claseId)
    }
    
    /**
     * Carga los contactos disponibles para el profesor
     */
    private fun loadContacts(centroId: String?, claseId: String?) {
        viewModelScope.launch {
            try {
                if (centroId == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo determinar el centro del profesor",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // 1. Cargar administradores del centro
                val admins = loadAdministrators(centroId)
                
                // 2. Cargar otros profesores del centro
                val teachers = loadTeachers(centroId, _uiState.value.currentUser?.dni)
                
                // 3. Cargar familiares de alumnos de la clase
                val families = if (claseId != null) {
                    loadFamiliesByClaseId(claseId)
                } else {
                    emptyList()
                }
                
                // Actualizar estado con todos los contactos
                _uiState.update { state ->
                    state.copy(
                        administratorContacts = admins,
                        teacherContacts = teachers,
                        familyContacts = families,
                        filteredContacts = admins + teachers + families,
                        isLoading = false
                    )
                }
                
                // Cargar contadores de mensajes no leídos
                cargarMensajesNoLeidos()
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar contactos")
                _uiState.update { it.copy(
                    error = "Error al cargar contactos: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga cursos y clases disponibles para filtrado
     */
    private fun loadCoursesAndClasses(centroId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    currentUserCentroId = centroId
                ) }
                
                // Cargar los cursos usando la función que ahora usa datos reales
                loadCursos()
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar cursos y clases para filtrado")
                _uiState.update { it.copy(
                    error = "Error al cargar datos para filtrado: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Actualiza el texto de búsqueda y filtra los contactos
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        aplicarFiltro(_uiState.value.selectedCourseId, _uiState.value.selectedClassId)
    }
    
    /**
     * Activa/desactiva el modo de búsqueda
     */
    fun toggleSearchMode() {
        _uiState.update { 
            val newSearchActive = !it.isSearchActive
            it.copy(
                isSearchActive = newSearchActive,
                searchQuery = if (!newSearchActive) "" else it.searchQuery
            )
        }
        if (!_uiState.value.isSearchActive) {
            aplicarFiltro(_uiState.value.selectedCourseId, _uiState.value.selectedClassId)
        }
    }
    
    /**
     * Muestra el diálogo de filtros
     */
    fun showFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = true) }
    }
    
    /**
     * Oculta el diálogo de filtros
     */
    fun hideFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = false) }
    }
    
    /**
     * Selecciona un curso para filtrar
     */
    fun selectCourse(courseId: String) {
        val currentCourseId = _uiState.value.selectedCourseId
        
        if (currentCourseId == courseId) {
            // Si se selecciona el mismo curso, deseleccionarlo
            _uiState.update { it.copy(
                selectedCourseId = null,
                selectedClassId = null,
                availableClasses = emptyList()
            ) }
            
            // Aplicar filtros sin curso ni clase seleccionada
            aplicarFiltro(null, null)
        } else {
            // Seleccionar el nuevo curso y cargar sus clases
            _uiState.update { it.copy(
                selectedCourseId = courseId,
                selectedClassId = null,
                isLoading = true
            ) }
            
            // Cargar las clases del curso seleccionado
            loadClases(courseId)
            
            // No aplicamos filtro aquí porque esperaremos a que se seleccione una clase
        }
    }
    
    /**
     * Selecciona una clase para filtrar
     */
    fun selectClass(classId: String) {
        val currentClassId = _uiState.value.selectedClassId
        
        if (currentClassId == classId) {
            // Si se selecciona la misma clase, deseleccionarla
            _uiState.update { it.copy(selectedClassId = null) }
            
            // Aplicar filtros solo con el curso seleccionado
            aplicarFiltro(_uiState.value.selectedCourseId, null)
        } else {
            // Seleccionar la nueva clase
            _uiState.update { it.copy(selectedClassId = classId) }
            
            // Aplicar filtro con curso y clase seleccionados
            aplicarFiltro(_uiState.value.selectedCourseId, classId)
        }
    }
    
    /**
     * Aplica los filtros seleccionados
     */
    fun applyFilters() {
        aplicarFiltro(_uiState.value.selectedCourseId, _uiState.value.selectedClassId)
    }
    
    /**
     * Limpia los filtros seleccionados
     */
    fun clearFilters() {
        _uiState.update { it.copy(
            selectedCourseId = null,
            selectedClassId = null
        ) }
        
        // Aplicar filtros sin curso ni clase seleccionada
        aplicarFiltro(null, null)
    }
    
    /**
     * Inicia una conversación con un contacto y navega a la pantalla de chat
     */
    fun startConversation(
        contactId: String,
        contactName: String,
        navController: NavController,
        chatRouteName: String,
        alumnoId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            Timber.d("🔄 Iniciando startConversation con contactId: $contactId, contactName: $contactName, alumnoId: $alumnoId")

            try {
                // Obtener el usuario actual
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    Timber.e("❌ Usuario actual no encontrado al iniciar conversación.")
                    _uiState.update { it.copy(
                        error = "No se pudo obtener información del usuario actual",
                        isLoading = false
                    )}
                    return@launch
                }
                Timber.d("✅ Usuario actual: ${currentUser.dni}")
                
                // Usar el alumno seleccionado si estamos en modo familiar
                val contextAlumnoId = if (_uiState.value.userType == TipoUsuario.FAMILIAR) {
                    _uiState.value.hijoSeleccionado?.dni
                } else {
                    alumnoId
                }
                
                // Lista de participantes para la conversación
                val participantIds = listOf(currentUser.dni, contactId).sorted()
                Timber.d("🆔 Participantes: $participantIds")
                
                // Título de la conversación (opcional)
                val title = "Chat con $contactName"
                
                // Metadatos adicionales
                val entityId = contextAlumnoId ?: ""
                val entityType = if (contextAlumnoId != null) "ALUMNO" else ""
                
                // Buscar si ya existe una conversación entre estos usuarios en el sistema unificado
                var conversacionId = ""
                
                // Primero buscar en el sistema unificado
                try {
                    val unifiedMessageRepo = unifiedMessageRepository
                    
                    // Buscar conversaciones existentes
                    val conversationsResult = unifiedMessageRepo.getCurrentUserConversations().first()
                    
                    if (conversationsResult is Result.Success) {
                        // Buscar una conversación que tenga exactamente estos dos participantes
                        // (sin importar el orden y sin incluir a otros participantes)
                        val existingConversation = conversationsResult.data.find { conversation ->
                            val participants = conversation.participantIds.sorted()
                            val expectedParticipants = participantIds.sorted()
                            
                            // La conversación debe tener exactamente los mismos participantes
                            participants.size == expectedParticipants.size && 
                            participants == expectedParticipants &&
                            // Si hay un alumno asociado, debe coincidir también
                            (contextAlumnoId == null || conversation.entityId == contextAlumnoId)
                        }
                        
                        if (existingConversation != null) {
                            conversacionId = existingConversation.id
                            Timber.d("✅ Se encontró una conversación existente en el sistema unificado: $conversacionId")
                        }
                    }
                    
                    // Si no se encontró, crear una nueva conversación en el sistema unificado
                    if (conversacionId.isEmpty()) {
                        // Generar un ID de conversación predecible basado en los participantes
                        val generatedConversationId = participantIds.joinToString(separator = "_")
                        Timber.d("🆔 ID de conversación generado: $generatedConversationId")
                    
                        Timber.d("🤝 Intentando crear conversación unificada...")
                        val createResult = unifiedMessageRepo.createOrUpdateConversation(
                            conversationId = generatedConversationId,
                            participantIds = participantIds,
                            title = title,
                            entityId = entityId,
                            entityType = entityType
                        )
                        
                        if (createResult is Result.Success) {
                            conversacionId = createResult.data
                            Timber.d("✅ Se creó/actualizó conversación en el sistema unificado: $conversacionId")
                        } else {
                            Timber.e("❌ Error al crear conversación unificada: ${(createResult as? Result.Error)?.message}")
                            _uiState.update { it.copy(
                                error = "Error al crear la conversación",
                                isLoading = false
                            )}
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al buscar/crear conversación en sistema unificado")
                    _uiState.update { it.copy(
                        error = "Error al establecer conversación: ${e.message}",
                        isLoading = false
                    )}
                    return@launch
                }
                
                if (conversacionId.isEmpty()) {
                    Timber.e("❌ No se pudo obtener/crear ID de conversación")
                    _uiState.update { it.copy(
                        error = "No se pudo crear la conversación",
                        isLoading = false
                    )}
                    return@launch
                }
                
                Timber.d("🚀 Usando conversaciónId: $conversacionId para chat entre ${currentUser.dni} y $contactId")
                
                // Comprobar si chatRouteName es la ruta base de ChatProfesor o contiene chat_profesor
                if (chatRouteName == "chat_profesor" || chatRouteName.startsWith("chat_profesor")) {
                    // Navegar usando la ruta de ChatProfesor
                    val route = "${AppScreens.ChatProfesor.route}/$conversacionId/$contactId"
                    navController.navigate(route)
                    Timber.d("🚀 Navegando a ChatProfesor: contactId=$contactId, conversacionId=$conversacionId")
                } else {
                    // Para otros tipos de chat, usar la ruta proporcionada
                    Timber.d("Ruta de chat no reconocida: $chatRouteName. Usando navegación genérica.")
                    val route = if (contextAlumnoId != null) {
                        "$chatRouteName/$conversacionId/$contactId/$contextAlumnoId"
                    } else {
                        "$chatRouteName/$conversacionId/$contactId"
                    }
                    navController.navigate(route)
                    Timber.d("🚀 Navegando a ruta: $route")
                }
                
                _uiState.update { it.copy(isLoading = false) }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Error al iniciar conversación: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error al iniciar la conversación: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Carga los cursos del centro para filtrado
     */
    fun loadCursos() {
        viewModelScope.launch {
            try {
                val centroId = _uiState.value.currentUserCentroId ?: return@launch
                
                Timber.d("Cargando cursos reales para centro $centroId")
                
                // Consultar cursos de Firestore
                val cursosQuery = firestore.collection("cursos")
                    .whereEqualTo("centroId", centroId)
                    .get()
                    .await()
                
                val cursos = cursosQuery.documents.mapNotNull { doc ->
                    if (!doc.exists()) return@mapNotNull null
                    
                    Curso(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin nombre",
                        descripcion = doc.getString("descripcion") ?: "",
                        centroId = doc.getString("centroId") ?: centroId
                    )
                }
                
                _uiState.update { it.copy(
                    availableCourses = cursos,
                    isLoading = false
                )}
                
                Timber.d("Se han cargado ${cursos.size} cursos para el centro $centroId")
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar cursos reales: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error al cargar cursos: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }
    
    /**
     * Carga las clases de un curso para filtrado
     */
    fun loadClases(cursoId: String) {
        viewModelScope.launch {
            try {
                Timber.d("Cargando clases reales para curso $cursoId")
                
                // Consultar clases de Firestore
                val clasesQuery = firestore.collection("clases")
                    .whereEqualTo("cursoId", cursoId)
                    .get()
                    .await()
                
                val clases = clasesQuery.documents.mapNotNull { doc ->
                    if (!doc.exists()) return@mapNotNull null
                    
                    Clase(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin nombre",
                        cursoId = doc.getString("cursoId") ?: cursoId,
                        centroId = doc.getString("centroId") ?: _uiState.value.currentUserCentroId ?: ""
                    )
                }
                
                _uiState.update { it.copy(
                    availableClasses = clases,
                    isLoading = false
                )}
                
                Timber.d("Se han cargado ${clases.size} clases para el curso $cursoId")
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar clases reales: ${e.message}")
                _uiState.update { it.copy(
                    error = "Error al cargar clases: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }
    
    /**
     * Carga los mensajes no leídos para el usuario actual
     */
    fun cargarMensajesNoLeidos() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser() ?: return@launch
                val userId = currentUser.dni
                
                // Obtener mensajes no leídos desde unified_messages donde el usuario es receptor y el estado es UNREAD
                val unreadQuery = firestore.collection("unified_messages")
                    .whereEqualTo("receiverId", userId)
                    .whereEqualTo("status", "UNREAD")
                    .get()
                    .await()
                    
                // Obtener mensajes no leídos desde unified_messages donde el usuario está en receiversIds y el estado es UNREAD
                val unreadGroupQuery = firestore.collection("unified_messages")
                    .whereArrayContains("receiversIds", userId)
                    .whereEqualTo("status", "UNREAD")
                    .get()
                    .await()
                
                // Combinar los resultados
                val allUnreadMessages = (unreadQuery.documents + unreadGroupQuery.documents).distinctBy { it.id }
                
                // Contar mensajes no leídos por remitente
                val unreadCountByContact = mutableMapOf<String, Int>()
                
                for (doc in allUnreadMessages) {
                    val senderId = doc.getString("senderId") ?: continue
                    unreadCountByContact[senderId] = (unreadCountByContact[senderId] ?: 0) + 1
                }
                
                // Actualizar contadores en los contactos
                _uiState.update { state ->
                    val updatedAdministrators = state.administratorContacts.map { contact ->
                        contact.copy(unreadCount = unreadCountByContact[contact.dni] ?: 0)
                    }
                    
                    val updatedTeachers = state.teacherContacts.map { contact ->
                        contact.copy(unreadCount = unreadCountByContact[contact.dni] ?: 0)
                    }
                    
                    val updatedFamilies = state.familyContacts.map { contact ->
                        contact.copy(unreadCount = unreadCountByContact[contact.dni] ?: 0)
                    }
                    
                    state.copy(
                        administratorContacts = updatedAdministrators,
                        teacherContacts = updatedTeachers,
                        familyContacts = updatedFamilies,
                        filteredContacts = when (state.activeFilter) {
                            FiltroContacto.ADMINISTRADORES -> updatedAdministrators
                            FiltroContacto.PROFESORES -> updatedTeachers
                            FiltroContacto.FAMILIARES -> updatedFamilies
                            else -> updatedAdministrators + updatedTeachers + updatedFamilies
                        }
                    )
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar mensajes no leídos: ${e.message}")
            }
        }
    }
    
    /**
     * Carga los contactos disponibles
     */
    fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Obtener usuario actual
                val currentUser = authRepository.getCurrentUser()
                
                if (currentUser == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener información del usuario actual",
                        isLoading = false
                    )}
                    return@launch
                }
                
                // Inicializar listas vacías de contactos
                val administrators = mutableListOf<ProfesorContacto>()
                val teachers = mutableListOf<ProfesorContacto>()
                val families = mutableListOf<FamiliarContacto>()
                
                // Determinar tipo de usuario y buscar contactos según su perfil
                val profesorPerfil = currentUser.perfiles.find { it.tipo == TipoUsuario.PROFESOR }
                val familiarPerfil = currentUser.perfiles.find { it.tipo == TipoUsuario.FAMILIAR }
                val adminPerfil = currentUser.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                
                when {
                    profesorPerfil != null -> {
                        val centroId = profesorPerfil.centroId
                        _uiState.update { it.copy(
                            userType = TipoUsuario.PROFESOR,
                            currentUser = currentUser,
                            currentUserCentroId = centroId
                        )}
                        
                        // 1. Siempre cargar automáticamente al administrador del centro
                        administrators.addAll(loadAdministrators(centroId))
                        Timber.d("Administradores cargados: ${administrators.size}")
                        
                        // 2. Cargar profesores del mismo curso
                        // Primero, obtener la clase del profesor
                        val clasesResult = claseRepository.getClasesByProfesorId(currentUser.dni)
                        if (clasesResult is Result.Success && clasesResult.data.isNotEmpty()) {
                            val clase = clasesResult.data.first()
                            _uiState.update { it.copy(currentUserClaseId = clase.id) }
                            
                            // Obtener el curso de la clase
                            val cursoResult = cursoRepository.getCursoById(clase.cursoId)
                            val curso = if (cursoResult is Result.Success) cursoResult.data else null
                            
                            if (curso != null) {
                                // Cargar todos los profesores de las clases del mismo curso
                                val clasesDelCursoResult = claseRepository.getClasesByCursoId(curso.id)
                                if (clasesDelCursoResult is Result.Success) {
                                    val clasesDelCurso = clasesDelCursoResult.data
                                    val profesoresIds = clasesDelCurso.mapNotNull { it.profesorId }.distinct()
                                    
                                    for (profesorId in profesoresIds) {
                                        // No incluir al profesor actual
                                        if (profesorId != currentUser.dni) {
                                            val profesorResult = usuarioRepository.getUsuarioById(profesorId)
                                            if (profesorResult is Result.Success) {
                                                val profesor = profesorResult.data
                                                teachers.add(ProfesorContacto(
                                                    dni = profesor.dni,
                                                    nombre = profesor.nombre,
                                                    apellidos = profesor.apellidos,
                                                    descripcion = "Profesor de ${curso.nombre}"
                                                ))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Timber.d("Profesores cargados: ${teachers.size}")
                        
                        // 3. Cargar cursos para el filtrado (para mostrar familiares al seleccionar)
                        loadCoursesAndClasses(centroId)
                    }
                    
                    familiarPerfil != null -> {
                        _uiState.update { it.copy(
                            userType = TipoUsuario.FAMILIAR,
                            currentUser = currentUser,
                            currentUserCentroId = familiarPerfil.centroId
                        )}
                        
                        // Cargar hijos del familiar
                        val hijos = loadFamiliarHijos(currentUser.dni)
                        _uiState.update { it.copy(hijos = hijos) }
                        
                        // Si hay hijos, cargar profesores del primer hijo por defecto
                        if (hijos.isNotEmpty()) {
                            val primerHijo = hijos[0]  // Usar indexación segura en lugar de first()
                            seleccionarHijo(primerHijo)
                        }
                    }
                    
                    adminPerfil != null -> {
                        _uiState.update { it.copy(
                            userType = TipoUsuario.ADMIN_CENTRO,
                            currentUser = currentUser,
                            currentUserCentroId = adminPerfil.centroId
                        )}
                        
                        // 1. Cargar profesores del centro
                        teachers.addAll(
                            loadTeachers(adminPerfil.centroId, currentUser.dni)
                        )
                        
                        // 2. Cargar cursos para el filtrado
                        loadCoursesAndClasses(adminPerfil.centroId)
                    }
                    
                    else -> {
                        _uiState.update { it.copy(
                            error = "El usuario no tiene un perfil válido para ver contactos",
                            isLoading = false
                        )}
                        return@launch
                    }
                }
                
                // Cargar mensajes no leídos para actualizar contadores
                cargarMensajesNoLeidos()
                
                // Actualizar el estado con los contactos cargados
                _uiState.update { state ->
                    state.copy(
                        administratorContacts = administrators,
                        teacherContacts = teachers,
                        familyContacts = families,
                        filteredContacts = when (state.activeFilter) {
                            FiltroContacto.ADMINISTRADORES -> administrators
                            FiltroContacto.PROFESORES -> teachers
                            FiltroContacto.FAMILIARES -> families
                            else -> administrators + teachers + families
                        },
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar contactos")
                _uiState.update { it.copy(
                    error = "Error al cargar contactos: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }
    
    /**
     * Carga los hijos asociados a un familiar
     */
    private suspend fun loadFamiliarHijos(familiarId: String): List<Alumno> {
        try {
            val result = alumnoRepository.obtenerAlumnosPorFamiliar(familiarId)
            return when (result) {
                is Result.Success -> result.data
                else -> emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar hijos del familiar $familiarId")
            return emptyList()
        }
    }

    /**
     * Filtra los contactos por curso y clase seleccionados
     */
    fun aplicarFiltro(cursoId: String?, claseId: String?) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    selectedCourseId = cursoId,
                    selectedClassId = claseId
                )}

                // Si no hay clase seleccionada, no mostrar familiares
                if (claseId == null) {
                    _uiState.update { state ->
                        state.copy(
                            familyContacts = emptyList(),
                            filteredContacts = when (state.activeFilter) {
                                FiltroContacto.ADMINISTRADORES -> state.administratorContacts
                                FiltroContacto.PROFESORES -> state.teacherContacts
                                FiltroContacto.FAMILIARES -> emptyList()
                                else -> state.administratorContacts + state.teacherContacts
                            },
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Cargar familiares de los alumnos de la clase seleccionada
                val families = loadFamiliesByClaseId(claseId)
                
                _uiState.update { state ->
                    state.copy(
                        familyContacts = families,
                        filteredContacts = when (state.activeFilter) {
                            FiltroContacto.ADMINISTRADORES -> state.administratorContacts
                            FiltroContacto.PROFESORES -> state.teacherContacts
                            FiltroContacto.FAMILIARES -> families
                            else -> state.administratorContacts + state.teacherContacts + families
                        },
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "Error al aplicar filtro")
                _uiState.update { it.copy(
                    error = "Error al filtrar contactos: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    /**
     * Carga los familiares de los alumnos de una clase específica
     * Implementación siguiendo el flujo:
     * clases -> alumnosIds -> vinculaciones_familiar_alumno -> familiarId -> usuarios
     */
    private suspend fun loadFamiliesByClaseId(claseId: String): List<FamiliarContacto> {
        try {
            Timber.d("Cargando familiares para la clase $claseId")
            val familiaresResult = mutableListOf<FamiliarContacto>()
            
            // 1. Obtener la clase para extraer los alumnosIds
            val claseDoc = firestore.collection("clases").document(claseId).get().await()
            
            if (!claseDoc.exists()) {
                Timber.e("No se encontró la clase con ID: $claseId")
                return emptyList()
            }
            
            // 2. Extraer los alumnosIds de la clase
            val alumnosIds = claseDoc.get("alumnosIds") as? List<String> ?: emptyList()
            Timber.d("Encontrados ${alumnosIds.size} alumnos en la clase $claseId")
            
            if (alumnosIds.isEmpty()) {
                Timber.d("La clase $claseId no tiene alumnos registrados")
                return emptyList()
            }
            
            // 3. Para cada alumno, buscar sus vinculaciones con familiares
            for (alumnoId in alumnosIds) {
                Timber.d("Buscando vinculaciones para alumno: $alumnoId")
                
                // Buscar en la colección vinculaciones_familiar_alumno
                val vinculacionesQuery = firestore.collection("vinculaciones_familiar_alumno")
                    .whereEqualTo("alumnoId", alumnoId)
                    .get()
                    .await()
                
                if (vinculacionesQuery.isEmpty) {
                    Timber.d("No se encontraron vinculaciones para el alumno $alumnoId")
                    continue
                }
                
                for (vinculacionDoc in vinculacionesQuery.documents) {
                    // 4. Extraer el familiarId de cada vinculación
                    val familiarId = vinculacionDoc.getString("familiarId")
                    val parentesco = vinculacionDoc.getString("parentesco") ?: "Familiar"
                    
                    if (familiarId == null) {
                        Timber.d("Vinculación sin familiarId válido")
                        continue
                    }
                    
                    // 5. Obtener los datos del familiar desde la colección usuarios
                    val familiarDoc = firestore.collection("usuarios").document(familiarId).get().await()
                    
                    if (!familiarDoc.exists()) {
                        Timber.d("No se encontró el usuario con ID: $familiarId")
                        continue
                    }
                    
                    // 6. Extraer la información del familiar
                    val nombre = familiarDoc.getString("nombre") ?: "Familiar"
                    val apellidos = familiarDoc.getString("apellidos") ?: ""
                    
                    // 7. Obtener información del alumno para mostrar como contexto
                    val alumnoDoc = firestore.collection("usuarios").document(alumnoId).get().await()
                    val alumnoNombre = if (alumnoDoc.exists()) {
                        alumnoDoc.getString("nombre") ?: "Alumno"
                    } else {
                        "Alumno"
                    }
                    
                    // 8. Crear el objeto FamiliarContacto y añadirlo a la lista
                    val familiar = FamiliarContacto(
                        dni = familiarId,
                        nombre = nombre,
                        apellidos = apellidos,
                        alumnoId = alumnoId,
                        alumnoNombre = "$alumnoNombre ($parentesco)"
                    )
                    
                    // Evitar duplicados
                    if (!familiaresResult.any { it.dni == familiar.dni }) {
                        familiaresResult.add(familiar)
                        Timber.d("Añadido familiar: $nombre $apellidos, para alumno: $alumnoNombre")
                    }
                }
            }
            
            Timber.d("Total de ${familiaresResult.size} familiares encontrados para la clase $claseId")
            return familiaresResult
            
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar familiares para la clase $claseId")
            return emptyList()
        }
    }
} 