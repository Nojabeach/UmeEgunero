package com.tfg.umeegunero.feature.centro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.CursoRepository
import com.tfg.umeegunero.data.repository.CentroRepository
import com.tfg.umeegunero.data.repository.SolicitudRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import com.google.firebase.Timestamp

/**
 * Estado UI para la pantalla de dashboard del centro educativo.
 * 
 * Esta clase representa el estado completo de la interfaz de usuario para
 * la gestión a nivel de centro educativo, conteniendo toda la información
 * necesaria para renderizar correctamente la pantalla del dashboard.
 * 
 * Sigue el patrón de UI State en Jetpack Compose, donde el estado se mantiene
 * de forma inmutable y cada cambio genera un nuevo objeto de estado.
 * 
 * @property isLoading Indica si hay operaciones de carga en progreso
 * @property error Mensaje de error a mostrar, null si no hay errores
 * @property currentUser Usuario administrador del centro actual
 * @property cursos Lista de cursos pertenecientes al centro educativo
 * @property navigateToWelcome Flag para controlar la navegación a la pantalla de bienvenida
 * @property nombreCentro Nombre del centro educativo a mostrar en el dashboard
 * @property centroId ID del centro educativo asociado al usuario actual
 *
 * @see CentroDashboardViewModel
 * @see CentroDashboardScreen
 */
data class CentroDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: Usuario? = null,
    val cursos: List<Curso> = emptyList(),
    val navigateToWelcome: Boolean = false,
    val nombreCentro: String = "Centro Educativo",
    val centroId: String = "",
    val notificacionesPendientes: Int = 0
)

// Data class para datos del Intent de email
data class EmailIntentData(
    val destinatario: String,
    val asunto: String,
    val cuerpo: String
)

data class SolicitudVinculacion(
    val id: String = "",
    val familiarId: String = "",
    val alumnoId: String = "",
    val nombreFamiliar: String = "",
    val nombreAlumno: String = "",
    val tipoRelacion: String = "",
    val estado: String = "pendiente",
    val fechaSolicitud: Timestamp = Timestamp.now()
)

/**
 * ViewModel para la gestión del dashboard de centro educativo.
 * 
 * Este ViewModel implementa la lógica de negocio relacionada con la administración
 * a nivel de centro educativo, proporcionando datos y funcionalidades para:
 * - Visualización de información global del centro
 * - Gestión de cursos y clases
 * - Configuración general del centro
 * - Monitorización de actividades y estadísticas
 * 
 * Utiliza el patrón MVVM junto con Flows para exponer el estado de forma reactiva
 * y corrutinas para manejar operaciones asíncronas.
 * 
 * ## Características principales
 * - Gestión del estado de la UI mediante [StateFlow]
 * - Carga automática de datos al inicializar
 * - Manejo de errores y estados de carga
 * - Integración con múltiples repositorios
 * 
 * ## Flujo de datos
 * 1. Inicialización y carga del usuario actual
 * 2. Obtención del centro asociado
 * 3. Carga de cursos y datos relacionados
 * 4. Actualización del estado UI
 * 
 * @constructor Crea una instancia del ViewModel con las dependencias necesarias
 * @param cursoRepository Repositorio para acceder a los datos de cursos
 * @param usuarioRepository Repositorio para acceder a los datos de usuarios
 * @param authRepository Repositorio para gestionar la autenticación
 * @param centroRepository Repositorio para acceder a los datos de centros
 * @param solicitudRepository Repositorio para acceder a los datos de solicitudes
 * @param familiarRepository Repositorio para acceder a los datos de familiares
 * @param alumnoRepository Repositorio para acceder a los datos de alumnos
 * 
 * @see CentroDashboardUiState
 * @see CentroDashboardScreen
 */
@HiltViewModel
class CentroDashboardViewModel @Inject constructor(
    private val cursoRepository: CursoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val centroRepository: CentroRepository,
    private val solicitudRepository: SolicitudRepository,
    private val familiarRepository: FamiliarRepository,
    private val alumnoRepository: AlumnoRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {
    // Estado mutable internamente para modificaciones dentro del ViewModel
    private val _uiState = MutableStateFlow(CentroDashboardUiState())
    
    // Estado inmutable expuesto a la UI siguiendo el principio de encapsulamiento
    val uiState: StateFlow<CentroDashboardUiState> = _uiState.asStateFlow()
    
    /**
     * Propiedad derivada que expone directamente los cursos para facilitar
     * su uso en la UI sin necesidad de acceder a todo el estado
     */
    val cursos = _uiState.asStateFlow().map { it.cursos }
    
    /**
     * Propiedad derivada que expone el nombre del centro educativo
     * para facilitar su uso en la UI
     */
    val nombreCentro = _uiState.asStateFlow().map { it.nombreCentro }
    
    // Flujo para las solicitudes de vinculación pendientes
    private val _solicitudesPendientes = MutableStateFlow<List<SolicitudVinculacion>>(emptyList())
    val solicitudesPendientes = _solicitudesPendientes.asStateFlow()
    
    // Estado para controlar el envío de emails
    private val _emailStatus = MutableStateFlow<String?>(null)
    val emailStatus = _emailStatus.asStateFlow()
    
    // Flow para eventos de lanzamiento de Intent de email
    private val _lanzarEmailIntentEvent = MutableSharedFlow<EmailIntentData>()
    val lanzarEmailIntentEvent: SharedFlow<EmailIntentData> = _lanzarEmailIntentEvent
    
    /**
     * Inicialización del ViewModel
     * 
     * Carga automáticamente los datos necesarios para el dashboard al crearse
     * la instancia, evitando que la UI tenga que solicitar esta carga explícitamente.
     */
    init {
        loadCurrentUser()
    }
    
    /**
     * Actualiza las solicitudes cuando el centroId cambia
     */
    private fun checkCentroIdAndLoadData() {
        val centroId = _uiState.value.centroId
        if (centroId.isNotEmpty()) {
            // Cargar solicitudes pendientes cuando tengamos el centroId
            cargarSolicitudesPendientes()
            // Cargar notificaciones pendientes
            cargarNotificacionesPendientes()
        }
    }
    
    /**
     * Carga los datos del usuario actual (administrador del centro)
     * 
     * Este método obtiene la información completa del administrador de centro
     * logueado actualmente a través del repositorio de usuarios, utilizando
     * el email como identificador.
     * 
     * La información del usuario es fundamental para:
     * - Personalizar el dashboard
     * - Filtrar acciones según sus permisos
     * - Proporcionar contexto sobre qué centro se está gestionando
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Intentar obtener el usuario directamente del repositorio de usuario
                // usando el ID del usuario autenticado actualmente
                val currentFirebaseUser = authRepository.getCurrentUser()
                
                if (currentFirebaseUser != null) {
                    // Aquí podríamos necesitar buscar el perfil completo del usuario
                    // usando algún campo identificador como el email o ID
                    when (val userResult = usuarioRepository.getUsuarioByEmail(currentFirebaseUser.email)) {
                        is Result.Success -> {
                            val usuario = userResult.data
                            // Buscar el perfil ADMIN_CENTRO para obtener el centroId
                            val perfilCentro = usuario.perfiles.find { it.tipo == TipoUsuario.ADMIN_CENTRO }
                            
                            // Usar el centroId del perfil de administrador de centro
                            val centroId = perfilCentro?.centroId ?: ""
                            
                            Timber.d("Usando centroId del perfil del administrador: $centroId")
                            
                            // Verificar que tenemos un centroId válido
                            if (centroId.isEmpty()) {
                                _uiState.update { it.copy(
                                    error = "No se encontró centro asociado a este administrador",
                                    isLoading = false
                                ) }
                                return@launch
                            }
                            
                            // Cargar los datos del centro
                            when (val centroResult = centroRepository.getCentroById(centroId)) {
                                is Result.Success -> {
                                    val centro = centroResult.data
                                    _uiState.update { it.copy(
                                        currentUser = usuario,
                                        nombreCentro = centro.nombre,
                                        centroId = centroId,
                                        isLoading = false
                                    ) }
                                    
                                    // Una vez que tenemos el centroId, cargamos los cursos
                                    loadCursos(centroId)
                                    
                                    // También cargamos las solicitudes pendientes
                                    checkCentroIdAndLoadData()
                                }
                                is Result.Error -> {
                                    Timber.e(centroResult.exception, "Error al cargar datos del centro")
                                    _uiState.update { it.copy(
                                        error = "No se pudo cargar la información del centro",
                                        isLoading = false
                                    ) }
                                }
                                else -> { /* Ignorar estado loading */ }
                            }
                        }
                        is Result.Error -> {
                            Timber.e(userResult.exception, "Error al cargar perfil de usuario")
                            _uiState.update { it.copy(
                                error = "Error al cargar el perfil de usuario",
                                isLoading = false
                            ) }
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                } else {
                    // No hay usuario autenticado
                    _uiState.update { it.copy(
                        error = "No hay usuario autenticado",
                        isLoading = false,
                        navigateToWelcome = true
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar el usuario actual")
                _uiState.update { it.copy(
                    error = e.message ?: "Error al cargar datos del usuario",
                    isLoading = false
                ) }
            }
        }
    }
    
    /**
     * Carga todos los cursos del centro educativo
     * 
     * Este método recupera la lista completa de cursos disponibles en el
     * centro educativo, lo que proporciona una visión general de la estructura
     * académica del centro y permite acceder a información más detallada.
     * 
     * Los cursos son la base para:
     * - Organizar clases y grupos
     * - Asignar profesores y alumnos
     * - Estructurar el contenido académico
     * - Generar informes y estadísticas
     * 
     * @param centroId ID del centro del cual cargar los cursos
     */
    private fun loadCursos(centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                if (centroId.isNotEmpty()) {
                    // Usar la función suspendida que devuelve directamente la lista
                    val cursosList = cursoRepository.obtenerCursosPorCentro(centroId)
                    Timber.d("Cursos cargados: ${cursosList.size}")
                    _uiState.update { it.copy(cursos = cursosList, isLoading = false) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No hay centroId disponible"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error inesperado al cargar los cursos"
                    )
                }
                Timber.e(e, "Error inesperado al cargar los cursos")
            } finally {
                 _uiState.update { it.copy(isLoading = false) } // Asegurar que isLoading se ponga a false
            }
        }
    }
    
    /**
     * Limpia el mensaje de error actual
     * 
     * Este método restablece el estado de error a null, generalmente
     * después de que el error ha sido mostrado al usuario o cuando
     * se inicia una nueva operación.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Cierra la sesión del usuario actual
     * 
     * Este método ejecuta el proceso de logout mediante el repositorio
     * de autenticación y actualiza el estado para redirigir al usuario
     * a la pantalla de bienvenida/login.
     * 
     * Se asegura de:
     * - Eliminar tokens de autenticación
     * - Limpiar el estado actual
     * - Configurar la navegación hacia la pantalla inicial
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update { it.copy(navigateToWelcome = true, currentUser = null) }
            } catch (e: Exception) {
                Timber.e(e, "Error al cerrar sesión")
                _uiState.update { 
                    it.copy(error = e.message ?: "Error al cerrar sesión")
                }
            }
        }
    }
    
    /**
     * Carga las solicitudes de vinculación pendientes para el centro
     * 
     * Este método recupera todas las solicitudes que los familiares han enviado
     * para vincularse con alumnos del centro y que están pendientes de aprobación.
     */
    fun cargarSolicitudesPendientes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val centroId = _uiState.value.centroId
                if (centroId.isNotEmpty()) {
                    when (val result = solicitudRepository.getSolicitudesPendientesByCentroId(centroId)) {
                        is Result.Success -> {
                            _solicitudesPendientes.value = result.data
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        is Result.Error -> {
                            Timber.e(result.exception, "Error al cargar solicitudes pendientes")
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = "Error al cargar solicitudes: ${result.exception?.message}"
                            ) }
                        }
                        else -> { /* Ignorar estado loading */ }
                    }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No hay centroId disponible para cargar solicitudes"
                    ) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar solicitudes pendientes")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado al cargar solicitudes"
                ) }
            }
        }
    }
    
    /**
     * Procesa una solicitud de vinculación (aprobar o rechazar)
     * Los emails se envían automáticamente a través del SolicitudRepository
     * 
     * @param solicitudId ID de la solicitud a procesar
     * @param aprobar true para aprobar, false para rechazar
     * @param observaciones Observaciones adicionales sobre la decisión (opcional)
     */
    fun procesarSolicitud(
        solicitudId: String, 
        aprobar: Boolean, 
        observaciones: String = ""
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            var errorProceso: String? = null

            try {
                val nuevoEstado = if (aprobar) EstadoSolicitud.APROBADA else EstadoSolicitud.RECHAZADA
                
                // Obtener información del administrador actual
                val adminId = _uiState.value.currentUser?.dni ?: ""
                val nombreAdmin = _uiState.value.currentUser?.nombre ?: "Administrador"
                
                // Procesar la solicitud (el email se envía automáticamente en el repositorio)
                val result = solicitudRepository.procesarSolicitud(
                    solicitudId = solicitudId,
                    nuevoEstado = nuevoEstado,
                    adminId = adminId,
                    nombreAdmin = nombreAdmin,
                    observaciones = observaciones
                )

                if (result is Result.Success) {
                    val solicitud = _solicitudesPendientes.value.find { it.id == solicitudId }

                    if (aprobar && solicitud != null) {
                        // Lógica de vinculación
                        vincularFamiliarSiAprobado(solicitud)
                    }
                    
                    // Actualizar lista de solicitudes pendientes
                    cargarSolicitudesPendientes()
                    
                    _uiState.update { it.copy(
                        error = if (aprobar) "Solicitud aprobada correctamente" else "Solicitud rechazada correctamente"
                    ) }
                    
                    Timber.d("Solicitud ${solicitudId} ${if (aprobar) "aprobada" else "rechazada"} correctamente. " +
                             "Email enviado automáticamente por el repositorio.")
                } else if (result is Result.Error) {
                    Timber.e(result.exception, "Error al actualizar estado de solicitud")
                    errorProceso = "Error al procesar solicitud: ${result.exception?.message}"
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al procesar solicitud")
                errorProceso = "Error inesperado: ${e.message}"
            }
            
            // Actualizar estado de la UI
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = errorProceso
                )
            }
        }
    }

    /**
     * Realiza la vinculación entre un familiar y un alumno cuando se aprueba una solicitud.
     * 
     * @param solicitud La solicitud de vinculación aprobada
     */
    private suspend fun vincularFamiliarSiAprobado(solicitud: SolicitudVinculacion) {
        try {
            // Validar que tenemos la información necesaria
            if (solicitud.familiarId.isBlank() || solicitud.alumnoDni.isBlank()) {
                Timber.w("No se puede vincular: datos insuficientes. FamiliarId: ${solicitud.familiarId}, AlumnoDni: ${solicitud.alumnoDni}")
                return
            }
            
            val nombreAlumnoMostrar = solicitud.alumnoNombre.ifBlank { "el alumno" }
            Timber.d("Vinculando familiar ${solicitud.nombreFamiliar} con alumno $nombreAlumnoMostrar")
            
            // Realizar la vinculación utilizando el FamiliarRepository
            val resultado = familiarRepository.vincularFamiliarAlumno(
                familiarId = solicitud.familiarId,
                alumnoId = solicitud.alumnoDni, // Usar alumnoDni como ID del alumno
                parentesco = solicitud.tipoRelacion
            )
            
            when (resultado) {
                is Result.Success -> {
                    Timber.d("✅ Vinculación exitosa entre familiar ${solicitud.nombreFamiliar} y alumno $nombreAlumnoMostrar")
                }
                is Result.Error -> {
                    Timber.e(resultado.exception, "❌ Error al vincular familiar con alumno: ${resultado.exception?.message}")
                }
                else -> { /* Ignorar resultado Loading */ }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al vincular familiar: ${e.message}")
        }
    }
    
    /**
     * Busca el email de un familiar por su ID
     * 
     * @param familiarId ID del familiar
     * @return Email del familiar o null si no se encuentra
     */
    private suspend fun buscarEmailFamiliar(familiarId: String): String? {
        if (familiarId.isEmpty()) return null
        
        return try {
            // Intentamos obtener el usuario a partir del ID del familiar
            val usuarioResult = usuarioRepository.getUsuarioById(familiarId)
            
            if (usuarioResult is Result.Success) {
                return usuarioResult.data.email
            }
            
            null
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar email de familiar: $familiarId")
            null
        }
    }
    
    /**
     * Limpia el estado de envío de email
     */
    fun limpiarEstadoEmail() {
        _emailStatus.value = null
    }
    
    /**
     * Carga el número de notificaciones pendientes para el centro
     */
    private fun cargarNotificacionesPendientes() {
        viewModelScope.launch {
            try {
                val centroId = _uiState.value.centroId
                val currentUser = _uiState.value.currentUser
                
                if (centroId.isNotEmpty() && currentUser != null) {
                    // Obtener notificaciones no leídas para este centro
                    val notificacionesFlow = notificacionRepository.getNotificacionesCentro(centroId)
                    
                    notificacionesFlow.collect { result ->
                        when (result) {
                            is Result.Success -> {
                                val notificacionesNoLeidas = result.data.count { !it.leida }
                                _uiState.update { 
                                    it.copy(notificacionesPendientes = notificacionesNoLeidas) 
                                }
                                Timber.d("📊 Notificaciones pendientes para centro $centroId: $notificacionesNoLeidas")
                            }
                            is Result.Error -> {
                                Timber.e(result.exception, "Error al cargar notificaciones pendientes")
                            }
                            else -> { /* Ignorar loading */ }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar notificaciones pendientes")
            }
        }
    }
    
    /**
     * Marca una notificación como leída y actualiza el contador
     */
    fun marcarNotificacionComoLeida(notificacionId: String) {
        viewModelScope.launch {
            try {
                val result = notificacionRepository.marcarComoLeida(notificacionId)
                if (result is Result.Success) {
                    // Recargar el contador de notificaciones pendientes
                    cargarNotificacionesPendientes()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar notificación como leída")
            }
        }
    }
    
    /**
     * Actualiza el contador de notificaciones pendientes manualmente
     */
    fun actualizarContadorNotificaciones() {
        cargarNotificacionesPendientes()
    }
} 