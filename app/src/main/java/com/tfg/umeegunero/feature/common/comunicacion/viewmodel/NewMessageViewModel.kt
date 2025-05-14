package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.model.RecipientGroupType
import com.tfg.umeegunero.data.model.RecipientItem
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.ClaseRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
import com.tfg.umeegunero.data.repository.ProfesorRepository
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.MessageStatus
import com.google.firebase.Timestamp
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.*
import timber.log.Timber

/**
 * Estructura para representar a un destinatario seleccionado
 */
data class ReceiverInfo(
    val id: String,
    val name: String
)

/**
 * Estructura para representar resultados de búsqueda de usuarios
 */
data class UserSearchResult(
    val id: String,
    val name: String,
    val userType: String
)

/**
 * Estado de la UI para la pantalla de nuevo mensaje
 */
data class NewMessageUiState(
    val recipientId: String? = null,
    val recipients: List<RecipientItem> = emptyList(),
    val selectedRecipients: List<RecipientItem> = emptyList(),
    val subject: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<SearchResultItem> = emptyList(),
    val searchQuery: String = "",
    val messageType: String = "CHAT", // Tipo de mensaje, por defecto CHAT
    val availableMessageTypes: List<String> = listOf("CHAT", "ANNOUNCEMENT", "NOTIFICATION", "SYSTEM"),
    val canSendMessage: Boolean = subject.isNotBlank() && content.isNotBlank() && (selectedRecipients.isNotEmpty())
)

data class SearchResultItem(
    val id: String,
    val name: String,
    val type: String,
    val description: String = ""
)

data class RecipientGroup(
    val id: String,
    val name: String,
    val type: RecipientGroupType
)

enum class RecipientGroupType {
    CENTRO,
    CURSO,
    CLASE,
    PROFESOR,
    FAMILIAR,
    ALUMNO
}

/**
 * ViewModel para la pantalla de creación de nuevo mensaje
 */
@HiltViewModel
class NewMessageViewModel @Inject constructor(
    private val messageRepository: UnifiedMessageRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val centroRepository: CentroRepository,
    private val cursoRepository: CursoRepository,
    private val claseRepository: ClaseRepository,
    private val alumnoRepository: AlumnoRepository,
    private val familiarRepository: FamiliarRepository,
    private val profesorRepository: ProfesorRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NewMessageUiState())
    val uiState: StateFlow<NewMessageUiState> = _uiState.asStateFlow()
    
    init {
        // Si se proporciona un ID de destinatario, precargarlo
        savedStateHandle.get<String>("receiverId")?.let { id ->
            if (id.isNotEmpty()) {
                // loadRecipient(id) // Eliminado porque no existe
            }
        }
        
        // Si se proporciona un tipo de mensaje, establecerlo
        savedStateHandle.get<String>("messageType")?.let { type ->
            if (type.isNotEmpty() && _uiState.value.availableMessageTypes.contains(type)) {
                _uiState.update { it.copy(messageType = type) }
            }
        }
        
        // Cargar usuarios disponibles
        loadRealDestinations()
    }
    
    /**
     * Carga destinatarios reales basados en el rol del usuario actual:
     * 1. Administradores de centro
     * 2. Profesores del mismo curso (para profesor) o profesores del hijo (para padre)
     * 3. Padres del aula (para profesor) o padres del aula del hijo (para padre)
     */
    private fun loadRealDestinations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = mutableListOf<SearchResultItem>()
                
                // Obtener el usuario actual
                val currentUser = authRepository.getCurrentUser() ?: throw Exception("Usuario no encontrado")
                val userType = currentUser.perfiles.firstOrNull()?.tipo ?: throw Exception("Tipo de usuario no definido")
                
                // Obtener el centro del usuario actual (basado en su primer perfil)
                val centroId = currentUser.perfiles.firstOrNull()?.centroId ?: ""
                if (centroId.isEmpty()) {
                    throw Exception("Usuario sin centro asignado")
                }
                
                // 1. Cargar administradores del centro
                val adminResults = loadCentroAdmins(centroId)
                results.addAll(adminResults)
                
                // 2 y 3. Dependiendo del tipo de usuario
                when (userType) {
                    TipoUsuario.PROFESOR -> {
                        // Obtener el curso y clase donde el profesor da clases
                        val clasesProfesor = claseRepository.getClasesByProfesor(currentUser.dni)
                        if (clasesProfesor is Result.Success) {
                            val clases = clasesProfesor.data
                            
                            if (clases.isNotEmpty()) {
                                // 2. Profesores del mismo curso
                                val cursosIds = clases.map { it.cursoId }.distinct()
                                val profesoresResults = loadProfesoresByCursos(cursosIds, currentUser.dni)
                                results.addAll(profesoresResults)
                                
                                // 3. Padres de los alumnos de las clases
                                val padresResults = loadPadresByClases(clases.map { it.id })
                                results.addAll(padresResults)
                            }
                        }
                    }
                    TipoUsuario.FAMILIAR -> {
                        // Obtener las clases donde estudian los hijos del familiar
                        val hijosResults = alumnoRepository.getAlumnosByFamiliarId(currentUser.dni)
                        if (hijosResults is Result.Success<List<Alumno>>) {
                            val hijos = hijosResults.data
                            
                            if (hijos.isNotEmpty()) {
                                // Obtener clases de los hijos
                                val clasesIds = mutableListOf<String>()
                                val profesorResults = mutableListOf<SearchResultItem>()
                                
                                for (alumno in hijos) {
                                    // Usamos la clase del alumno directamente si está disponible
                                    if (alumno.claseId.isNotEmpty()) {
                                        val claseId = alumno.claseId
                                        clasesIds.add(claseId)
                                        
                                        try {
                                            // Cargar la clase para obtener detalles
                                            val claseResult = claseRepository.getClaseById(claseId)
                                            if (claseResult is Result.Success) {
                                                val clase = claseResult.data
                                                
                                                // 2. Profesores del hijo
                                                val profesorItems = loadProfesorByClaseId(clase.id)
                                                profesorResults.addAll(profesorItems)
                                            }
                                        } catch (e: Exception) {
                                            Timber.e(e, "Error al cargar clase: $claseId para alumno: ${alumno.id}")
                                        }
                                    }
                                }
                                
                                results.addAll(profesorResults.distinctBy { it.id })
                                
                                // 3. Padres del aula de los hijos (excepto el usuario actual)
                                if (clasesIds.isNotEmpty()) {
                                    val padresResults = loadPadresByClases(clasesIds)
                                    // Filtramos para no incluirnos a nosotros mismos
                                    val filteredPadres = padresResults.filter { it.id != currentUser.dni }
                                    results.addAll(filteredPadres)
                                }
                            }
                        }
                    }
                    else -> {
                        // Para otros tipos de usuario, solo mostrar administradores (ya cargados)
                    }
                }
                
                _uiState.update { it.copy(
                    searchResults = results,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al cargar destinatarios",
                    isLoading = false
                )}
            }
        }
    }
    
    /**
     * Carga los administradores de un centro educativo
     */
    private suspend fun loadCentroAdmins(centroId: String): List<SearchResultItem> {
        val admins = mutableListOf<SearchResultItem>()
        
        try {
            val result = usuarioRepository.getAdminsByCentroId(centroId)
            if (result is Result.Success) {
                result.data.forEach { admin ->
                    admins.add(SearchResultItem(
                        id = admin.dni,
                        name = "${admin.nombre} ${admin.apellidos}",
                        type = TipoUsuario.ADMIN_CENTRO.toString(),
                        description = "Administrador del centro"
                    ))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar administradores del centro: $centroId")
        }
        
        return admins
    }
    
    /**
     * Carga profesores de los cursos especificados, excluyendo al profesor actual
     */
    private suspend fun loadProfesoresByCursos(cursosIds: List<String>, currentProfesorId: String): List<SearchResultItem> {
        val profesores = mutableListOf<SearchResultItem>()
        
        try {
            for (cursoId in cursosIds) {
                val cursoResult = cursoRepository.getCursoById(cursoId)
                val cursoNombre = if (cursoResult is Result.Success) cursoResult.data.nombre else "Curso"
                
                val clasesResult = claseRepository.getClasesByCursoId(cursoId)
                if (clasesResult is Result.Success) {
                    val clases = clasesResult.data
                    for (clase in clases) {
                        // Agregar profesor titular si existe y no es el mismo que el profesor actual
                        if (!clase.profesorId.isNullOrEmpty() && clase.profesorId != currentProfesorId) {
                            val profesorResult = usuarioRepository.getUsuarioById(clase.profesorId)
                            if (profesorResult is Result.Success) {
                                val profesor = profesorResult.data
                                profesores.add(SearchResultItem(
                                    id = profesor.dni,
                                    name = "${profesor.nombre} ${profesor.apellidos}",
                                    type = TipoUsuario.PROFESOR.toString(),
                                    description = "Profesor de $cursoNombre - ${clase.nombre}"
                                ))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar profesores de cursos: ${cursosIds.joinToString()}")
        }
        
        return profesores.distinctBy { it.id }
    }
    
    /**
     * Carga los profesores de una clase específica
     */
    private suspend fun loadProfesorByClaseId(claseId: String): List<SearchResultItem> {
        val profesores = mutableListOf<SearchResultItem>()
        
        try {
            val claseResult = claseRepository.getClaseById(claseId)
            if (claseResult is Result.Success) {
                val clase = claseResult.data
                
                // Obtener curso para mostrar información más completa
                val cursoResult = cursoRepository.getCursoById(clase.cursoId)
                val cursoNombre = if (cursoResult is Result.Success) cursoResult.data.nombre else "Curso"
                
                // Obtener profesor titular
                if (!clase.profesorId.isNullOrEmpty()) {
                    val profesor = usuarioRepository.getUsuarioById(clase.profesorId)
                    if (profesor is Result.Success) {
                        profesores.add(SearchResultItem(
                            id = profesor.data.dni,
                            name = "${profesor.data.nombre} ${profesor.data.apellidos}",
                            type = TipoUsuario.PROFESOR.toString(),
                            description = "Profesor de $cursoNombre - ${clase.nombre}"
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar profesor de la clase: $claseId")
        }
        
        return profesores
    }
    
    /**
     * Carga los padres de los alumnos de las clases especificadas
     */
    private suspend fun loadPadresByClases(clasesIds: List<String>): List<SearchResultItem> {
        val padres = mutableListOf<SearchResultItem>()
        
        try {
            for (claseId in clasesIds) {
                val alumnosResult = alumnoRepository.getAlumnosByClaseId(claseId)
                if (alumnosResult is Result.Success) {
                    val alumnos = alumnosResult.data
                    
                    for (alumno in alumnos) {
                        val familiaresResult = familiarRepository.getFamiliaresByAlumnoId(alumno.id)
                        if (familiaresResult is Result.Success) {
                            familiaresResult.data.forEach { familiar ->
                                padres.add(SearchResultItem(
                                    id = familiar.dni,
                                    name = "${familiar.nombre} ${familiar.apellidos}",
                                    type = TipoUsuario.FAMILIAR.toString(),
                                    description = "Familiar de ${alumno.nombre}"
                                ))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar padres de clases: ${clasesIds.joinToString()}")
        }
        
        return padres.distinctBy { it.id }
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, searchQuery = query) }
            try {
                // Filtrar los resultados que ya tenemos cargados
                val allResults = _uiState.value.searchResults
                val filteredResults = if (query.isNotEmpty()) {
                    allResults.filter { 
                        it.name.contains(query, ignoreCase = true) || 
                        it.description.contains(query, ignoreCase = true)
                    }
                } else {
                    allResults
                }
                
                _uiState.update { it.copy(
                    searchResults = filteredResults,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Error al buscar usuarios",
                    isLoading = false
                )}
            }
        }
    }
    
    fun addReceiver(id: String) {
        val recipient = uiState.value.searchResults.find { it.id == id }
        if (recipient != null) {
            val currentRecipients = _uiState.value.selectedRecipients.toMutableList()
            if (!currentRecipients.any { it.id == recipient.id }) {
                currentRecipients.add(RecipientItem(
                    id = recipient.id,
                    name = recipient.name,
                    type = recipient.type
                ))
                _uiState.update { it.copy(
                    selectedRecipients = currentRecipients,
                    recipients = currentRecipients
                )}
            }
        }
    }
    
    fun removeReceiver(id: String) {
        val currentRecipients = _uiState.value.selectedRecipients.toMutableList()
        currentRecipients.removeIf { it.id == id }
        _uiState.update { it.copy(
            selectedRecipients = currentRecipients,
            recipients = currentRecipients
        )}
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(subject = title) }
    }
    
    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchUsers(query)
    }
    
    fun sendMessage() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Verificar que tenemos lo mínimo necesario
                val state = _uiState.value
                if (state.subject.isBlank() || state.content.isBlank() || state.selectedRecipients.isEmpty()) {
                    _uiState.update { it.copy(
                        error = "Por favor completa todos los campos requeridos",
                        isLoading = false
                    ) }
                    return@launch
                }
                
                // Obtener usuario remitente
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.update { it.copy(
                        error = "No se pudo obtener información del usuario actual",
                        isLoading = false
                    ) }
                    return@launch
                }
                val sender = currentUser
                
                // Preparar destinatarios
                val recipientIds = state.selectedRecipients.map { it.id }
                
                // Crear el mensaje unificado
                val message = UnifiedMessage(
                    id = UUID.randomUUID().toString(),
                    title = state.subject,
                    content = state.content,
                    senderId = sender.dni,
                    senderName = "${sender.nombre} ${sender.apellidos}",
                    receiversIds = recipientIds,
                    timestamp = Timestamp(Date()),
                    type = when(state.messageType) {
                        "ANNOUNCEMENT" -> MessageType.ANNOUNCEMENT
                        "NOTIFICATION" -> MessageType.NOTIFICATION
                        "SYSTEM" -> MessageType.SYSTEM
                        else -> MessageType.CHAT
                    },
                    status = MessageStatus.UNREAD
                )
                
                // Guardar mensaje
                val result = messageRepository.sendMessage(message)
                
                if (result is Result.Success<*>) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        subject = "",
                        content = "",
                        selectedRecipients = emptyList()
                    ) }
                    
                    // Notificar éxito
                    // ...
                } else {
                    _uiState.update { it.copy(
                        error = "Error al enviar el mensaje",
                        isLoading = false
                    ) }
                }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al enviar el mensaje: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun mapUsuarioToRecipientItem(usuario: Usuario): RecipientItem {
        return RecipientItem(
            id = usuario.dni,
            name = usuario.nombre,
            type = usuario.perfiles.firstOrNull()?.tipo?.name ?: TipoUsuario.ALUMNO.name
        )
    }

    private fun mapAlumnoToRecipientItem(alumno: Alumno): RecipientItem {
        return RecipientItem(
            id = alumno.id,
            name = alumno.nombre,
            type = TipoUsuario.ALUMNO.name
        )
    }

    private fun mapFamiliarToRecipientItem(familiar: Familiar): RecipientItem {
        return RecipientItem(
            id = familiar.id,
            name = familiar.nombre,
            type = TipoUsuario.FAMILIAR.name
        )
    }

    /**
     * Actualiza el tipo de mensaje seleccionado
     */
    fun updateMessageType(type: String) {
        if (_uiState.value.availableMessageTypes.contains(type)) {
            _uiState.update { it.copy(messageType = type) }
        }
    }

    // Estos métodos no se usarán ahora, pero se mantienen como referencia para implementaciones futuras
    // cuando los repositorios tengan las funciones necesarias implementadas

    /**
     * Las siguientes funciones se implementarán en el futuro cuando los repositorios
     * tengan las APIs necesarias:
     * 
     * - loadCentroAdmins(centroId: String): List<SearchResultItem>
     *   Para cargar administradores del centro
     * 
     * - loadProfesoresByCursos(cursosIds: List<String>, currentProfesorId: String): List<SearchResultItem>
     *   Para cargar profesores de cursos específicos
     * 
     * - loadProfesorByClaseId(claseId: String): List<SearchResultItem>
     *   Para cargar profesores de una clase específica
     * 
     * - loadPadresByClases(clasesIds: List<String>): List<SearchResultItem>
     *   Para cargar padres de alumnos de las clases especificadas
     */
} 