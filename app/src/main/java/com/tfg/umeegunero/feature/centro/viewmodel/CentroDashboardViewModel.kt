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
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.EmailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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
    val centroId: String = ""
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
 * @param emailService Servicio para enviar correos electrónicos
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
    private val emailService: EmailService
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
                            val centroId = perfilCentro?.centroId ?: ""
                            
                            if (centroId.isNotEmpty()) {
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
                            } else {
                                // No hay perfil de centro o no tiene centroId
                                _uiState.update { it.copy(
                                    currentUser = usuario,
                                    error = "El usuario no tiene un centro asignado",
                                    isLoading = false
                                ) }
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
                    _uiState.update { it.copy(
                        error = "No hay usuario autenticado",
                        isLoading = false
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
     * 
     * @param solicitudId ID de la solicitud a procesar
     * @param aprobar true para aprobar, false para rechazar
     * @param enviarEmail true para enviar email de confirmación
     * @param emailFamiliar email del familiar para enviar la confirmación
     */
    fun procesarSolicitud(solicitudId: String, aprobar: Boolean, enviarEmail: Boolean = true, emailFamiliar: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val nuevoEstado = if (aprobar) EstadoSolicitud.APROBADA else EstadoSolicitud.RECHAZADA
                
                // Actualizar estado en la base de datos
                when (val result = solicitudRepository.actualizarEstadoSolicitud(solicitudId, nuevoEstado.name)) {
                    is Result.Success -> {
                        // Buscar información detallada de la solicitud
                        val solicitud = _solicitudesPendientes.value.find { it.id == solicitudId }
                        
                        // Si la solicitud fue aprobada, crear la vinculación entre familiar y alumno
                        if (aprobar && solicitud != null) {
                            // Si la solicitud fue aprobada, crear la vinculación entre familiar y alumno
                            if (solicitud.alumnoDni.isNotEmpty()) {
                                try {
                                    // Buscar el alumno por su DNI
                                    // Nota: No hay un método específico getAlumnoByDni, así que usamos la obtención de todos los alumnos
                                    // y filtramos por DNI
                                    when (val alumnosResult = alumnoRepository.getAlumnos()) {
                                        is Result.Success -> {
                                            // Encontrar el alumno con el DNI correspondiente
                                            val alumno = alumnosResult.data.find { it.dni == solicitud.alumnoDni }
                                            
                                            if (alumno != null) {
                                                // Vincular familiar-alumno usando el método disponible
                                                // Usamos "FAMILIAR" como tipo de parentesco genérico
                                                when (val vinculacionResult = familiarRepository.vincularFamiliarAlumno(
                                                    familiarId = solicitud.familiarId,
                                                    alumnoId = alumno.id,
                                                    parentesco = "FAMILIAR"
                                                )) {
                                                    is Result.Success -> {
                                                        Timber.d("Vinculación creada exitosamente entre familiar ${solicitud.familiarId} y alumno ${alumno.id}")
                                                    }
                                                    is Result.Error -> {
                                                        val error = vinculacionResult.exception
                                                        Timber.e(error, "Error al crear vinculación entre familiar y alumno")
                                                        _uiState.update { it.copy(
                                                            error = "Error al crear vinculación: ${error?.message}"
                                                        ) }
                                                    }
                                                    else -> {
                                                        // Caso loading, no debería ocurrir aquí
                                                    }
                                                }
                                            } else {
                                                // No se encontró alumno con ese DNI
                                                Timber.w("No se encontró alumno con DNI ${solicitud.alumnoDni}")
                                                _uiState.update { it.copy(
                                                    error = "No se encontró alumno con DNI ${solicitud.alumnoDni}"
                                                ) }
                                            }
                                        }
                                        is Result.Error -> {
                                            val error = alumnosResult.exception
                                            Timber.e(error, "Error al obtener la lista de alumnos")
                                            _uiState.update { it.copy(
                                                error = "Error al buscar alumno: ${error?.message}"
                                            ) }
                                        }
                                        else -> {
                                            // Caso loading, no debería ocurrir aquí
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Excepción inesperada al procesar vinculación familiar-alumno")
                                    _uiState.update { it.copy(
                                        error = "Error inesperado al vincular familiar: ${e.message}"
                                    ) }
                                }
                            } else {
                                Timber.w("No se puede crear vinculación: DNI de alumno vacío en la solicitud ${solicitud.id}")
                                _uiState.update { it.copy(
                                    error = "No se puede vincular: falta DNI del alumno"
                                ) }
                            }
                        }
                        
                        // Buscar el email del familiar si no se proporcionó
                        val emailDestino = emailFamiliar ?: buscarEmailFamiliar(
                            solicitud?.familiarId ?: ""
                        )
                        
                        // Enviar email de confirmación si se solicitó y se encontró el email
                        if (enviarEmail && emailDestino != null && solicitud != null) {
                            try {
                                // Obtener el nombre del centro para personalizar el email
                                val centroNombre = _uiState.value.nombreCentro
                                
                                // Usar el nombre del alumno proporcionado en la solicitud o uno genérico
                                val alumnoNombre = solicitud.alumnoNombre ?: "alumno/a"
                                
                                // Enviar email usando el servicio
                                when (val emailResult = emailService.sendVinculacionNotification(
                                    to = emailDestino,
                                    isApproved = aprobar,
                                    alumnoNombre = alumnoNombre,
                                    centroNombre = centroNombre
                                )) {
                                    is Result.Success -> {
                                        _emailStatus.value = "Email enviado correctamente a $emailDestino"
                                        Timber.d("Email de vinculación enviado correctamente a $emailDestino")
                                    }
                                    is Result.Error -> {
                                        val error = emailResult.exception
                                        _emailStatus.value = "Error al enviar email: ${error?.message}"
                                        Timber.e(error, "Error al enviar email de vinculación")
                                    }
                                    else -> {
                                        // Caso loading, no debería ocurrir aquí
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Excepción al enviar email de vinculación")
                                _emailStatus.value = "Error al enviar email: ${e.message}"
                            }
                        } else if (enviarEmail) {
                            _emailStatus.value = "No se pudo enviar el email: dirección de correo no disponible"
                        }
                        
                        // Actualizar la lista de solicitudes pendientes
                        cargarSolicitudesPendientes()
                        
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error al procesar solicitud")
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Error al procesar solicitud: ${result.exception?.message}"
                        ) }
                    }
                    else -> { /* Ignorar estado loading */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al procesar solicitud")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado al procesar solicitud"
                ) }
            }
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
} 