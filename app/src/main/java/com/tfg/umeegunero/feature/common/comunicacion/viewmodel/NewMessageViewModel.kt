package com.tfg.umeegunero.feature.common.comunicacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Curso
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    val canSendMessage: Boolean = false,
    val messageSent: Boolean = false
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
    private val firestore: FirebaseFirestore,
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
     * 1. Administradores de centro SIEMPRE aparecen para todos los usuarios
     * 2. Para administrador de centro: todos los profesores y familiares de su centro, organizados por curso y clase
     * 3. Para profesor: profesores del mismo curso y padres de sus alumnos
     * 4. Para familiar: profesores de sus hijos y otros padres de las mismas clases
     */
    private fun loadRealDestinations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = mutableListOf<SearchResultItem>()
                
                // Obtener el usuario actual
                val currentUser = authRepository.getCurrentUser() ?: throw Exception("Usuario no encontrado")
                val userProfile = currentUser.perfiles.firstOrNull() ?: throw Exception("Tipo de usuario no definido")
                val userType = userProfile.tipo
                
                // Obtener el centro del usuario actual (basado en su primer perfil)
                val centroId = userProfile.centroId
                if (centroId.isEmpty()) {
                    throw Exception("Usuario sin centro asignado")
                }
                
                // 1. Cargar administradores del centro (excepto el usuario actual)
                val adminResults = loadCentroAdmins(centroId)
                    .filter { it.id != currentUser.dni } // Excluir al usuario actual
                results.addAll(adminResults)
                
                // 2, 3 y 4. Dependiendo del tipo de usuario
                when (userType) {
                    TipoUsuario.ADMIN_CENTRO -> {
                        // Admin ve a todos los profesores y familiares de su centro, organizados por curso y clase
                        try {
                            // Cargar cursos del centro
                            val cursosResult = cursoRepository.getCursosPorCentro(centroId)
                            
                            if (cursosResult is com.tfg.umeegunero.util.Result.Success<List<Curso>>) {
                                val cursos = cursosResult.data
                                
                                // Primero añadir todos los profesores (excepto el admin actual si es profesor)
                                val profesoresResult = usuarioRepository.getUsuariosByCentroId(centroId)
                                if (profesoresResult is com.tfg.umeegunero.util.Result.Success) {
                                    // Filtrar por tipo de usuario PROFESOR y excluir al usuario actual
                                    val profesores = profesoresResult.data
                                        .filter { usuario -> 
                                            usuario.perfiles.any { perfil -> perfil.tipo == TipoUsuario.PROFESOR } && 
                                            usuario.dni != currentUser.dni 
                                        }
                                    
                                    // Añadir encabezado para todos los profesores
                                    results.add(SearchResultItem(
                                        id = "header_profesores",
                                        name = "👨‍🏫 Todos los profesores",
                                        type = "HEADER",
                                        description = "Profesores del centro"
                                    ))
                                    
                                    // Agrupar profesores por curso/clase
                                    val profesoresPorClase = mutableMapOf<String, MutableList<SearchResultItem>>()
                                    
                                    // Inicializar sección "Sin asignación específica" para profesores sin clase asignada
                                    profesoresPorClase["sin_asignacion"] = mutableListOf()
                                    
                                    // Recorrer todos los cursos y sus clases
                                    for (curso in cursos) {
                                        val clasesResult = claseRepository.getClasesByCursoId(curso.id)
                                        if (clasesResult is com.tfg.umeegunero.util.Result.Success) {
                                            val clases = clasesResult.data
                                            
                                            for (clase in clases) {
                                                // Crear clave para esta clase
                                                val claveClase = "${curso.nombre}_${clase.nombre}"
                                                profesoresPorClase[claveClase] = mutableListOf()
                                                
                                                // Si la clase tiene profesor asignado, añadirlo a esta clase
                                                if (!clase.profesorId.isNullOrEmpty()) {
                                                    val profesorAsignado = profesores.find { it.dni == clase.profesorId }
                                                    if (profesorAsignado != null) {
                                                        profesoresPorClase[claveClase]?.add(
                                                            SearchResultItem(
                                                                id = profesorAsignado.dni,
                                                                name = "${profesorAsignado.nombre} ${profesorAsignado.apellidos}",
                                                                type = TipoUsuario.PROFESOR.toString(),
                                                                description = "Profesor de ${curso.nombre} - ${clase.nombre}"
                                                            )
                                                        )
                                                        // Marcar este profesor como procesado - usar una copia local
                                                        val profeEncontrado = profesores.find { it.dni == profesorAsignado.dni }
                                                        profeEncontrado?.let {
                                                            // Crear una copia del perfil para modificarlo
                                                            val perfilCopia = it.perfiles.firstOrNull()?.copy(centroId = "procesado")
                                                            // No podemos modificar la lista original, pero podemos marcar de otra forma
                                                            // Este es un workaround para el error de compilación
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Añadir profesores no asignados a ninguna clase específica
                                    // Cambiar la lógica para no depender de la modificación anterior
                                    val profesoresYaProcesados = profesoresPorClase.values.flatten().map { it.id }.toSet()
                                    profesores.filter { !profesoresYaProcesados.contains(it.dni) }.forEach { profesor ->
                                        profesoresPorClase["sin_asignacion"]?.add(
                                            SearchResultItem(
                                                id = profesor.dni,
                                                name = "${profesor.nombre} ${profesor.apellidos}",
                                                type = TipoUsuario.PROFESOR.toString(),
                                                description = "Profesor del centro (sin asignación específica)"
                                            )
                                        )
                                    }
                                    
                                    // Añadir todos los profesores al resultado final, agrupados por clase
                                    profesoresPorClase.forEach { (claveClase, profesoresDeClase) ->
                                        if (profesoresDeClase.isNotEmpty()) {
                                            // Añadir encabezado si no es "sin_asignacion"
                                            if (claveClase != "sin_asignacion") {
                                                // Obtener nombre del curso y clase de la clave
                                                val partes = claveClase.split("_")
                                                if (partes.size >= 2) {
                                                    val cursoNombre = partes[0]
                                                    val claseNombre = partes[1]
                                                    
                                                    // Añadir un "encabezado" para este grupo
                                                    results.add(SearchResultItem(
                                                        id = "header_$claveClase",
                                                        name = "👨‍🏫 Profesores de $cursoNombre - $claseNombre",
                                                        type = "HEADER",
                                                        description = "Profesores asignados a esta clase"
                                                    ))
                                                }
                                            } else {
                                                // Encabezado para profesores sin asignación
                                                results.add(SearchResultItem(
                                                    id = "header_sin_asignacion",
                                                    name = "👨‍🏫 Otros profesores del centro",
                                                    type = "HEADER",
                                                    description = "Profesores sin asignación específica"
                                                ))
                                            }
                                            
                                            // Añadir los profesores de esta clase
                                            results.addAll(profesoresDeClase)
                                        }
                                    }
                                }
                                
                                // Añadir encabezado principal para familiares
                                results.add(SearchResultItem(
                                    id = "header_familiares",
                                    name = "👪 Familiares por cursos y clases",
                                    type = "HEADER",
                                    description = "Todos los familiares organizados por curso y clase"
                                ))
                                
                                // Ahora cargar todos los alumnos y sus familiares, agrupados por curso y clase
                                for (curso in cursos) {
                                    // Añadir encabezado para el curso
                                    results.add(SearchResultItem(
                                        id = "header_curso_${curso.id}",
                                        name = "📚 ${curso.nombre}",
                                        type = "HEADER",
                                        description = "Clases y alumnos de ${curso.nombre}"
                                    ))
                                    
                                    val clasesResult = claseRepository.getClasesByCursoId(curso.id)
                                    if (clasesResult is com.tfg.umeegunero.util.Result.Success) {
                                        val clases = clasesResult.data
                                        
                                        for (clase in clases) {
                                            // Obtener alumnos de esta clase
                                            val alumnosIds = clase.alumnosIds ?: emptyList()
                                            
                                            if (alumnosIds.isNotEmpty()) {
                                                // Añadir encabezado para los familiares de esta clase
                                                results.add(SearchResultItem(
                                                    id = "header_familia_${curso.id}_${clase.id}",
                                                    name = "👪 ${clase.nombre}",
                                                    type = "HEADER",
                                                    description = "Familiares de alumnos de ${clase.nombre}"
                                                ))
                                                
                                                // Para cada alumno, buscar sus familiares
                                                for (alumnoId in alumnosIds) {
                                                    // Obtener alumno para nombre
                                                    val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                                                    val alumnoNombre = if (alumnoResult is com.tfg.umeegunero.util.Result.Success) {
                                                        alumnoResult.data.nombre
                                                    } else {
                                                        "Alumno"
                                                    }
                                                    
                                                    // Buscar vinculaciones
                                                    val vinculacionesSnapshot = firestore.collection("vinculaciones_familiar_alumno")
                                                        .whereEqualTo("alumnoId", alumnoId)
                                                        .get()
                                                        .await()
                                                    
                                                    val familiaresIds = vinculacionesSnapshot.documents.mapNotNull { doc ->
                                                        doc.getString("familiarId")
                                                    }
                                                    
                                                    for (familiarId in familiaresIds) {
                                                        // No mostrar el admin actual como familiar
                                                        if (familiarId == currentUser.dni) continue
                                                        
                                                        val familiarResult = usuarioRepository.getUsuarioById(familiarId)
                                                        if (familiarResult is com.tfg.umeegunero.util.Result.Success) {
                                                            val familiar = familiarResult.data
                                                            
                                                            // Obtener parentesco
                                                            var parentesco = ""
                                                            try {
                                                                val parentescoDoc = vinculacionesSnapshot.documents.find { doc ->
                                                                    doc.getString("familiarId") == familiarId && doc.getString("alumnoId") == alumnoId
                                                                }
                                                                parentesco = parentescoDoc?.getString("parentesco") ?: ""
                                                            } catch (e: Exception) {
                                                                Timber.e(e, "Error al obtener parentesco")
                                                            }
                                                            
                                                            // Descripción con parentesco
                                                            val descripcion = if (parentesco.isNotEmpty()) {
                                                                "$parentesco de $alumnoNombre"
                                                            } else {
                                                                "Familiar de $alumnoNombre"
                                                            }
                                                            
                                                            results.add(SearchResultItem(
                                                                id = familiar.dni,
                                                                name = "${familiar.nombre} ${familiar.apellidos}",
                                                                type = TipoUsuario.FAMILIAR.toString(),
                                                                description = descripcion
                                                            ))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al cargar usuarios del centro: $centroId")
                        }
                    }
                    TipoUsuario.PROFESOR -> {
                        // Obtener las clases donde el profesor da clases
                        val clasesProfesor = claseRepository.getClasesByProfesor(currentUser.dni)
                        if (clasesProfesor is com.tfg.umeegunero.util.Result.Success) {
                            val clases = clasesProfesor.data
                            
                            if (clases.isNotEmpty()) {
                                // Profesores del mismo curso
                                val cursosIds = clases.map { it.cursoId }.distinct()
                                val profesoresResults = loadProfesoresByCursos(cursosIds, currentUser.dni)
                                results.addAll(profesoresResults)
                                
                                // Padres de los alumnos de las clases
                                val padresResults = loadPadresByClases(clases.map { it.id })
                                results.addAll(padresResults)
                            }
                        }
                    }
                    TipoUsuario.FAMILIAR -> {
                        // Obtener los alumnos vinculados al familiar usando vinculaciones_familiar_alumno
                        val hijosIds = getAlumnosByFamiliarFromVinculaciones(currentUser.dni)
                        
                        if (hijosIds.isNotEmpty()) {
                            val profesoresSet = mutableSetOf<SearchResultItem>()
                            val padresSet = mutableSetOf<SearchResultItem>()
                            
                            for (alumnoId in hijosIds) {
                                try {
                                    // Obtener información del alumno
                                    val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                                    if (alumnoResult is com.tfg.umeegunero.util.Result.Success) {
                                        val alumno = alumnoResult.data
                                        val claseId = alumno.claseId
                                        
                                        if (!claseId.isNullOrEmpty()) {
                                            // Obtener la clase para conocer el profesor y otros datos
                                            val claseResult = claseRepository.getClaseById(claseId)
                                            if (claseResult is com.tfg.umeegunero.util.Result.Success) {
                                                val clase = claseResult.data
                                                
                                                // Obtener curso para mostrar información más completa
                                                val cursoResult = cursoRepository.getCursoById(clase.cursoId)
                                                val cursoNombre = if (cursoResult is com.tfg.umeegunero.util.Result.Success) {
                                                    cursoResult.data.nombre
                                                } else {
                                                    "Curso"
                                                }
                                                
                                                // Obtener profesor titular
                                                if (!clase.profesorId.isNullOrEmpty()) {
                                                    val profesorResult = usuarioRepository.getUsuarioById(clase.profesorId)
                                                    if (profesorResult is com.tfg.umeegunero.util.Result.Success) {
                                                        val profesor = profesorResult.data
                                                        profesoresSet.add(
                                                            SearchResultItem(
                                                                id = profesor.dni,
                                                                name = "${profesor.nombre} ${profesor.apellidos}",
                                                                type = TipoUsuario.PROFESOR.toString(),
                                                                description = "Profesor de $cursoNombre - ${clase.nombre}"
                                                            )
                                                        )
                                                    }
                                                }
                                                
                                                // También obtener otros profesores del mismo curso
                                                val clasesDelCursoResult = claseRepository.getClasesByCursoId(clase.cursoId)
                                                if (clasesDelCursoResult is com.tfg.umeegunero.util.Result.Success) {
                                                    for (otraClase in clasesDelCursoResult.data) {
                                                        if (!otraClase.profesorId.isNullOrEmpty() && 
                                                            otraClase.profesorId != clase.profesorId) {
                                                            val otroProfesorResult = usuarioRepository.getUsuarioById(otraClase.profesorId)
                                                            if (otroProfesorResult is com.tfg.umeegunero.util.Result.Success) {
                                                                val otroProfesor = otroProfesorResult.data
                                                                profesoresSet.add(
                                                                    SearchResultItem(
                                                                        id = otroProfesor.dni,
                                                                        name = "${otroProfesor.nombre} ${otroProfesor.apellidos}",
                                                                        type = TipoUsuario.PROFESOR.toString(),
                                                                        description = "Profesor de $cursoNombre - ${otraClase.nombre}"
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                // Cargar otros padres de la misma clase
                                                val otrosPadres = loadPadresByClases(listOf(claseId))
                                                    .filter { it.id != currentUser.dni }
                                                otrosPadres.forEach { padresSet.add(it) }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al cargar información del alumno $alumnoId")
                                }
                            }
                            
                            // Añadir profesores y padres a los resultados
                            results.addAll(profesoresSet)
                            results.addAll(padresSet)
                        }
                    }
                    else -> {
                        // Para otros tipos de usuario, solo mostrar administradores (ya cargados)
                    }
                }
                
                // Eliminar duplicados por ID
                val distinctResults = results.distinctBy { it.id }
                
                _uiState.update { it.copy(
                    searchResults = distinctResults,
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
            if (result is com.tfg.umeegunero.util.Result.Success) {
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
                val cursoNombre = if (cursoResult is com.tfg.umeegunero.util.Result.Success) cursoResult.data.nombre else "Curso"
                
                val clasesResult = claseRepository.getClasesByCursoId(cursoId)
                if (clasesResult is com.tfg.umeegunero.util.Result.Success) {
                    val clases = clasesResult.data
                    for (clase in clases) {
                        // Agregar profesor titular si existe y no es el mismo que el profesor actual
                        if (!clase.profesorId.isNullOrEmpty() && clase.profesorId != currentProfesorId) {
                            val profesorResult = usuarioRepository.getUsuarioById(clase.profesorId)
                            if (profesorResult is com.tfg.umeegunero.util.Result.Success) {
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
            if (claseResult is com.tfg.umeegunero.util.Result.Success) {
                val clase = claseResult.data
                
                // Obtener curso para mostrar información más completa
                val cursoResult = cursoRepository.getCursoById(clase.cursoId)
                val cursoNombre = if (cursoResult is com.tfg.umeegunero.util.Result.Success) cursoResult.data.nombre else "Curso"
                
                // Obtener profesor titular
                if (!clase.profesorId.isNullOrEmpty()) {
                    val profesorResult = usuarioRepository.getUsuarioById(clase.profesorId)
                    if (profesorResult is com.tfg.umeegunero.util.Result.Success) {
                        profesores.add(SearchResultItem(
                            id = profesorResult.data.dni,
                            name = "${profesorResult.data.nombre} ${profesorResult.data.apellidos}",
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
     * Carga los padres de los alumnos de las clases especificadas, utilizando la colección vinculaciones_familiar_alumno
     */
    private suspend fun loadPadresByClases(clasesIds: List<String>): List<SearchResultItem> {
        val padres = mutableListOf<SearchResultItem>()
        
        try {
            // Para cada clase
            for (claseId in clasesIds) {
                // 1. Obtener la clase y sus alumnos
                val claseResult = claseRepository.getClaseById(claseId)
                if (claseResult is com.tfg.umeegunero.util.Result.Success) {
                    val clase = claseResult.data
                    
                    // 2. Obtener los alumnos directamente de la clase
                    val alumnosIds = clase.alumnosIds ?: emptyList()
                    Timber.d("Clase $claseId tiene ${alumnosIds.size} alumnos")
                    
                    // 3. Para cada alumno de la clase, buscar sus familiares
                    for (alumnoId in alumnosIds) {
                        // 3.1 Buscar las vinculaciones para este alumno
                        try {
                            // Buscar en vinculaciones_familiar_alumno
                            val vinculacionesSnapshot = firestore.collection("vinculaciones_familiar_alumno")
                                .whereEqualTo("alumnoId", alumnoId)
                                .get()
                                .await()
                            
                            val familiaresIds = vinculacionesSnapshot.documents.mapNotNull { doc ->
                                doc.getString("familiarId")
                            }
                            
                            Timber.d("Alumno $alumnoId tiene ${familiaresIds.size} familiares vinculados")
                            
                            // 3.2 Obtener información de cada familiar
                            for (familiarId in familiaresIds) {
                                val familiarResult = usuarioRepository.getUsuarioById(familiarId)
                                if (familiarResult is com.tfg.umeegunero.util.Result.Success) {
                                    val familiar = familiarResult.data
                                    
                                    // Obtener el nombre del alumno para mostrarlo en la descripción
                                    val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                                    val alumnoNombre = if (alumnoResult is com.tfg.umeegunero.util.Result.Success) {
                                        alumnoResult.data.nombre
                                    } else {
                                        "Alumno"
                                    }
                                    
                                    // Obtener el parentesco si está disponible
                                    var parentesco = ""
                                    try {
                                        val parentescoDoc = vinculacionesSnapshot.documents.find { doc ->
                                            doc.getString("familiarId") == familiarId && doc.getString("alumnoId") == alumnoId
                                        }
                                        parentesco = parentescoDoc?.getString("parentesco") ?: ""
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al obtener parentesco")
                                    }
                                    
                                    // Crear descripción con parentesco si está disponible
                                    val descripcion = if (parentesco.isNotEmpty()) {
                                        "$parentesco de $alumnoNombre"
                                    } else {
                                        "Familiar de $alumnoNombre"
                                    }
                                    
                                    padres.add(SearchResultItem(
                                        id = familiar.dni,
                                        name = "${familiar.nombre} ${familiar.apellidos}",
                                        type = TipoUsuario.FAMILIAR.toString(),
                                        description = descripcion
                                    ))
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al obtener familiares para el alumno $alumnoId")
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
                val currentState = _uiState.value
                _uiState.update { it.copy(
                    selectedRecipients = currentRecipients,
                    recipients = currentRecipients,
                    canSendMessage = currentState.subject.isNotBlank() && currentState.content.isNotBlank() && currentRecipients.isNotEmpty()
                )}
            }
        }
    }
    
    fun addReceiver(id: String, name: String) {
        val recipient = uiState.value.searchResults.find { it.id == id }
        if (recipient != null) {
            val currentRecipients = _uiState.value.selectedRecipients.toMutableList()
            if (!currentRecipients.any { it.id == id }) {
                currentRecipients.add(RecipientItem(
                    id = id,
                    name = name,
                    type = recipient.type
                ))
                val currentState = _uiState.value
                _uiState.update { it.copy(
                    selectedRecipients = currentRecipients,
                    recipients = currentRecipients,
                    canSendMessage = currentState.subject.isNotBlank() && currentState.content.isNotBlank() && currentRecipients.isNotEmpty()
                )}
            }
        }
    }
    
    fun removeReceiver(id: String) {
        val currentRecipients = _uiState.value.selectedRecipients.toMutableList()
        currentRecipients.removeIf { it.id == id }
        val currentState = _uiState.value
        _uiState.update { it.copy(
            selectedRecipients = currentRecipients,
            recipients = currentRecipients,
            canSendMessage = currentState.subject.isNotBlank() && currentState.content.isNotBlank() && currentRecipients.isNotEmpty()
        )}
    }
    
    fun updateTitle(title: String) {
        val newTitle = title
        val currentState = _uiState.value
        _uiState.update { it.copy(
            subject = newTitle,
            canSendMessage = newTitle.isNotBlank() && currentState.content.isNotBlank() && currentState.selectedRecipients.isNotEmpty()
        ) }
    }
    
    fun updateContent(content: String) {
        val newContent = content
        val currentState = _uiState.value
        _uiState.update { it.copy(
            content = newContent,
            canSendMessage = currentState.subject.isNotBlank() && newContent.isNotBlank() && currentState.selectedRecipients.isNotEmpty()
        ) }
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
                
                if (result is com.tfg.umeegunero.util.Result.Success<*>) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        subject = "",
                        content = "",
                        selectedRecipients = emptyList(),
                        messageSent = true
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
    
    fun resetMessageSentFlag() {
        _uiState.update { it.copy(messageSent = false) }
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

    /**
     * Obtiene los IDs de alumnos vinculados a un familiar desde la colección vinculaciones_familiar_alumno
     */
    private suspend fun getAlumnosByFamiliarFromVinculaciones(familiarId: String): List<String> {
        try {
            val result = familiarRepository.getAlumnoIdsByVinculaciones(familiarId)
            return if (result is com.tfg.umeegunero.util.Result.Success) {
                result.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener vinculaciones para familiar: $familiarId")
            return emptyList()
        }
    }
} 