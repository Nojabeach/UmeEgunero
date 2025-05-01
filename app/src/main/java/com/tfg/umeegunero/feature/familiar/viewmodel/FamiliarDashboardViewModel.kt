package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.AlumnoRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import com.tfg.umeegunero.data.repository.FamiliarRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import com.tfg.umeegunero.data.repository.SolicitudRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.feature.familiar.screen.SolicitudPendienteUI
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import java.util.Date

/**
 * Estado UI para la pantalla de dashboard del familiar
 * 
 * Esta clase define un estado inmutable que representa toda la información
 * necesaria para mostrar la interfaz de usuario del dashboard familiar.
 * 
 * El uso de data classes para representar estados de UI es una práctica recomendada
 * en el patrón MVVM ya que:
 * - Proporciona inmutabilidad (cada cambio genera un nuevo objeto)
 * - Permite comparaciones sencillas entre estados
 * - Facilita la depuración al tener un toString() automático
 * - Simplifica la gestión del ciclo de vida de la UI
 */
data class FamiliarDashboardUiState(
    // Estado de carga, indica si hay una operación en progreso
    val isLoading: Boolean = true,
    
    // Mensaje de error, null si no hay errores
    val error: String? = null,
    
    // Datos del usuario familiar logueado
    val familiar: Familiar? = null,
    
    // Lista de alumnos (hijos) asociados al familiar
    val hijos: List<Alumno> = emptyList(),
    
    // Hijo actualmente seleccionado para mostrar sus detalles
    val hijoSeleccionado: Alumno? = null,
    
    // Registros de actividad del hijo seleccionado
    val registrosActividad: List<RegistroActividad> = emptyList(),
    
    // Contador de registros no leídos, para mostrar badge
    val registrosSinLeer: Int = 0,
    
    // Mensajes no leídos, para mostrar en la bandeja de entrada
    val mensajesNoLeidos: List<Mensaje> = emptyList(),
    
    // Contador total de mensajes no leídos, para mostrar badge
    val totalMensajesNoLeidos: Int = 0,
    
    // Mapa de profesores asociados a los hijos, para mostrar sus nombres
    val profesores: Map<String, Usuario> = emptyMap(), // Mapeo de profesorId -> Profesor
    
    // Pestaña seleccionada actualmente en el dashboard
    val selectedTab: Int = 0,
    
    // Flag para controlar la navegación a la pantalla de bienvenida (tras logout)
    val navigateToWelcome: Boolean = false,
    
    // Lista de solicitudes de vinculación pendientes
    val solicitudesPendientes: List<SolicitudPendienteUI> = emptyList(),
    
    // Indica si se está procesando el envío de una solicitud
    val isLoadingSolicitud: Boolean = false,
    
    // Indica si una solicitud acaba de ser enviada con éxito
    val solicitudEnviada: Boolean = false,
    
    // Lista de centros educativos disponibles
    val centros: List<Centro> = emptyList()
)

/**
 * ViewModel para la pantalla de dashboard del familiar
 * 
 * Este ViewModel implementa el patrón MVVM (Model-View-ViewModel), actuando como
 * intermediario entre la vista (Composables) y el modelo (Repositories).
 * 
 * Responsabilidades principales:
 * - Mantener y actualizar el estado de la UI (FamiliarDashboardUiState)
 * - Manejar la lógica de negocio relacionada con el dashboard familiar
 * - Procesar eventos de la UI y ejecutar las acciones correspondientes
 * - Gestionar operaciones asíncronas mediante corrutinas
 * 
 * Se utiliza @HiltViewModel para permitir la inyección de dependencias
 * automática mediante Hilt, siguiendo el principio de inversión de dependencias.
 */
@HiltViewModel
class FamiliarDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val familiarRepository: FamiliarRepository,
    private val alumnoRepository: AlumnoRepository,
    private val registroDiarioRepository: RegistroDiarioRepository,
    private val solicitudRepository: SolicitudRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Estado mutable interno que solo el ViewModel puede modificar
    private val _uiState = MutableStateFlow(FamiliarDashboardUiState())
    
    // Estado inmutable expuesto a la UI, para seguir el principio de encapsulamiento
    val uiState: StateFlow<FamiliarDashboardUiState> = _uiState.asStateFlow()

    /**
     * Inicialización del ViewModel, se ejecuta al crear la instancia
     * 
     * El bloque init es el primer código que se ejecuta cuando se crea el ViewModel,
     * ideal para inicializar datos o configurar observadores.
     */
    init {
        cargarCentros()
    }

    /**
     * Carga los datos del familiar actual
     * 
     * Este método realiza la carga inicial de datos para el dashboard y es la base
     * para mostrar la información personalizada del familiar y sus hijos.
     * 
     * Flujo de la operación:
     * 1. Actualiza el estado para mostrar el indicador de carga
     * 2. Obtiene el ID del usuario autenticado
     * 3. Carga los datos completos del familiar desde el repositorio
     * 4. Extrae información sobre los hijos asociados
     * 5. Actualiza el estado con los datos obtenidos o maneja el error
     */
    fun cargarDatosFamiliar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val usuarioId = authRepository.getCurrentUserId()
                if (usuarioId != null) {
                    // Cargar datos del familiar
                    val familiarResult = familiarRepository.getFamiliarByUsuarioId(usuarioId)
                    if (familiarResult is Result.Success) {
                        val familiar = familiarResult.data
                        if (familiar != null) {
                            _uiState.update { it.copy(familiar = familiar) }

                            // Cargar hijos vinculados
                            cargarHijosVinculados(familiar.id)
                            
                            // Cargar solicitudes pendientes
                            cargarSolicitudesPendientes(familiar.id)
                            
                            // Cargar mensajes no leídos
                            cargarTotalMensajesNoLeidos(familiar.id)
                            
                            // Cargar registros sin leer
                            cargarRegistrosSinLeer(familiar.id)
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No se encontró información del familiar"
                                )
                            }
                        }
                    } else if (familiarResult is Result.Error) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error al cargar datos: ${familiarResult.exception?.message}"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Usuario no identificado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error inesperado: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Carga los hijos vinculados al familiar y selecciona el primero por defecto
     *
     * @param familiarId ID del familiar
     */
    private suspend fun cargarHijosVinculados(familiarId: String) {
        when (val result = alumnoRepository.getAlumnosByFamiliarId(familiarId)) {
            is Result.Success -> {
                val hijos = result.data.sortedBy { it.nombre }
                val primerHijo = hijos.firstOrNull()
                
                _uiState.update { 
                    it.copy(
                        hijos = hijos,
                        hijoSeleccionado = primerHijo,
                        isLoading = false
                    )
                }
                
                // Si hay un hijo seleccionado, cargar sus registros
                primerHijo?.let { alumno ->
                    cargarRegistrosActividad(alumno.dni)
                }
            }
            is Result.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar hijos: ${result.exception?.message}"
                    )
                }
            }
            else -> { /* No hacer nada en caso de loading */ }
        }
    }

    /**
     * Carga las solicitudes de vinculación pendientes del familiar
     *
     * @param familiarId ID del familiar
     */
    private suspend fun cargarSolicitudesPendientes(familiarId: String) {
        try {
            when (val result = solicitudRepository.getSolicitudesByFamiliarId(familiarId)) {
                is Result.Success -> {
                    val solicitudesUI = result.data.map { solicitud ->
                        // Obtener nombre del centro si es posible
                        val centroNombre = obtenerNombreCentro(solicitud.centroId)
                        
                        // Convertir a modelo UI
                        SolicitudPendienteUI(
                            id = solicitud.id,
                            alumnoDni = solicitud.alumnoDni,
                            alumnoNombre = solicitud.alumnoNombre,
                            centroId = solicitud.centroId,
                            centroNombre = centroNombre,
                            fechaSolicitud = Date(solicitud.fechaSolicitud.seconds * 1000),
                            estado = solicitud.estado
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(solicitudesPendientes = solicitudesUI)
                    }
                }
                is Result.Error -> {
                    // No mostrar error, solo log
                    println("Error al cargar solicitudes: ${result.exception?.message}")
                }
                else -> { /* No hacer nada en caso de loading */ }
            }
        } catch (e: Exception) {
            // Log del error
            println("Error al cargar solicitudes: ${e.message}")
        }
    }
    
    /**
     * Obtiene el nombre de un centro por su ID
     *
     * @param centroId ID del centro
     * @return Nombre del centro o null si no se encuentra
     */
    private suspend fun obtenerNombreCentro(centroId: String): String? {
        try {
            when (val result = usuarioRepository.getCentroById(centroId)) {
                is Result.Success -> return result.data.nombre
                else -> return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Carga los registros de actividad de un alumno
     *
     * @param alumnoDni DNI del alumno
     */
    private suspend fun cargarRegistrosActividad(alumnoDni: String) {
        try {
            when (val result = registroDiarioRepository.getRegistrosActividadByAlumnoId(alumnoDni)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(registrosActividad = result.data)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            error = "Error al cargar registros: ${result.exception?.message}"
                        )
                    }
                }
                else -> { /* No hacer nada en caso de loading */ }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(error = "Error al cargar registros: ${e.message}")
            }
        }
    }

    /**
     * Carga el total de mensajes no leídos del familiar
     *
     * @param familiarId ID del familiar
     */
    private suspend fun cargarTotalMensajesNoLeidos(familiarId: String) {
        // Implementación simplificada (en realidad debería llamar al repositorio apropiado)
        // Por ahora establecemos un valor de ejemplo
        _uiState.update { 
            it.copy(totalMensajesNoLeidos = 0)
        }
    }

    /**
     * Carga el total de registros sin leer por el familiar
     *
     * @param familiarId ID del familiar
     */
    private suspend fun cargarRegistrosSinLeer(familiarId: String) {
        try {
            when (val result = registroDiarioRepository.getRegistrosSinLeerCount(familiarId)) {
                is Result.Success -> {
                    _uiState.update { 
                        it.copy(registrosSinLeer = result.data)
                    }
                }
                is Result.Error -> {
                    // No mostrar error, solo log
                    println("Error al cargar registros sin leer: ${result.exception?.message}")
                }
                else -> { /* No hacer nada en caso de loading */ }
            }
        } catch (e: Exception) {
            // Log del error
            println("Error al cargar registros sin leer: ${e.message}")
        }
    }

    /**
     * Selecciona un hijo específico para mostrar sus detalles en el dashboard
     * 
     * Este método actualiza el hijo actualmente seleccionado y carga sus
     * registros de actividad asociados para mostrarlos en la interfaz.
     * 
     * @param hijo El alumno (hijo) que ha sido seleccionado
     */
    fun seleccionarHijo(hijo: Alumno) {
        if (hijo.dni != _uiState.value.hijoSeleccionado?.dni) {
            _uiState.update { 
                it.copy(hijoSeleccionado = hijo)
            }
            
            viewModelScope.launch {
                cargarRegistrosActividad(hijo.dni)
            }
        }
    }

    /**
     * Crea una nueva solicitud de vinculación para un alumno
     *
     * @param alumnoDni DNI del alumno a vincular
     * @param centroId ID del centro educativo
     */
    fun crearSolicitudVinculacion(alumnoDni: String, centroId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSolicitud = true, error = null) }
            
            val familiarId = _uiState.value.familiar?.id
            if (familiarId != null) {
                try {
                    // Crear objeto de solicitud
                    val solicitud = SolicitudVinculacion(
                        id = "", // Se generará automáticamente
                        familiarId = familiarId,
                        alumnoDni = alumnoDni.uppercase(),
                        centroId = centroId,
                        fechaSolicitud = Timestamp.now(),
                        estado = EstadoSolicitud.PENDIENTE
                    )
                    
                    // Enviar solicitud
                    when (val result = solicitudRepository.crearSolicitudVinculacion(solicitud)) {
                        is Result.Success -> {
                            // Recargar solicitudes
                            cargarSolicitudesPendientes(familiarId)
                            
                            _uiState.update { 
                                it.copy(
                                    isLoadingSolicitud = false,
                                    solicitudEnviada = true
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoadingSolicitud = false,
                                    error = "Error al enviar solicitud: ${result.exception?.message}"
                                )
                            }
                        }
                        else -> { /* No hacer nada en caso de loading */ }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoadingSolicitud = false,
                            error = "Error al enviar solicitud: ${e.message}"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingSolicitud = false,
                        error = "Error: familiar no identificado"
                    )
                }
            }
        }
    }

    /**
     * Carga los centros educativos disponibles
     */
    private fun cargarCentros() {
        viewModelScope.launch {
            try {
                when (val result = usuarioRepository.getCentrosEducativos()) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(centros = result.data)
                        }
                    }
                    is Result.Error -> {
                        // No mostrar error, solo log
                        println("Error al cargar centros: ${result.exception?.message}")
                    }
                    else -> { /* No hacer nada en caso de loading */ }
                }
            } catch (e: Exception) {
                // Log del error
                println("Error al cargar centros: ${e.message}")
            }
        }
    }

    /**
     * Marca como consumido el flag de solicitud enviada
     */
    fun resetSolicitudEnviada() {
        _uiState.update { it.copy(solicitudEnviada = false) }
    }

    /**
     * Cierra la sesión del usuario actual
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { it.copy(navigateToWelcome = true) }
        }
    }

    /**
     * Función para navegar a la consulta de registros diarios de un alumno
     * 
     * Este método utiliza el NavController para navegar a la pantalla de
     * consulta de registros diarios, pasando los parámetros necesarios en la ruta.
     * 
     * @param navController Controlador de navegación de Jetpack Compose
     * @param alumno Objeto Alumno cuyos registros se quieren consultar
     */
    fun navegarAConsultaRegistroDiario(navController: NavController, alumno: Alumno) {
        navController.navigate(
            AppScreens.ConsultaRegistroDiario.createRoute(
                alumnoId = alumno.dni,
                alumnoNombre = "${alumno.nombre} ${alumno.apellidos}"
            )
        )
    }
}