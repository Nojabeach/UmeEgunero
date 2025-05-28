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
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
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
import kotlinx.coroutines.delay
import com.tfg.umeegunero.data.model.NotificacionAusencia
import com.tfg.umeegunero.data.repository.NotificacionAusenciaRepository
import com.tfg.umeegunero.data.model.EstadoNotificacionAusencia

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
    val centros: List<Centro> = emptyList(),
    
    // Timestamp de la última actualización de datos
    val ultimaActualizacion: Date? = null,
    
    // Notificaciones de ausencia pendientes
    val ausenciasPendientes: List<NotificacionAusencia> = emptyList(),
    
    // Estado de envío de notificación de ausencia
    val isNotificandoAusencia: Boolean = false,
    
    // Confirmación de ausencia notificada
    val ausenciaNotificada: Boolean = false,
    
    // Mensaje de éxito para ausencia notificada
    val mensajeExitoAusencia: String? = null
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
    private val usuarioRepository: UsuarioRepository,
    private val unifiedMessageRepository: UnifiedMessageRepository,
    private val notificacionAusenciaRepository: NotificacionAusenciaRepository
) : ViewModel() {

    // Estado mutable interno que solo el ViewModel puede modificar
    private val _uiState = MutableStateFlow(FamiliarDashboardUiState())
    
    // Estado inmutable expuesto a la UI, para seguir el principio de encapsulamiento
    val uiState: StateFlow<FamiliarDashboardUiState> = _uiState.asStateFlow()
    
    // Flow específico para el contador de mensajes no leídos
    private val _unreadMessageCount = MutableStateFlow(0)
    val unreadMessageCount: StateFlow<Int> = _unreadMessageCount.asStateFlow()

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
                Timber.d("ID de usuario obtenido: $usuarioId")
                
                if (usuarioId != null) {
                    // Intentar obtener el familiar directamente del repositorio de autenticación
                    val usuario = authRepository.getCurrentUser()
                    Timber.d("Usuario obtenido: ${usuario?.nombre}, DNI: ${usuario?.dni}")
                    
                    // Usar el DNI como ID del familiar (estrategia más confiable)
                    val familiarId = usuario?.dni
                    
                    if (familiarId.isNullOrBlank()) {
                        Timber.e("No se pudo obtener el DNI del usuario familiar")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No se pudo obtener la información de usuario"
                            )
                        }
                        return@launch
                    }
                    
                    // Cargar datos del familiar
                    val familiarResult = familiarRepository.getFamiliarByUsuarioId(familiarId)
                    if (familiarResult is Result.Success) {
                        val familiar = familiarResult.data
                        if (familiar != null) {
                            // Actualizar estado con el timestamp actual como hora de última actualización
                            _uiState.update { it.copy(
                                familiar = familiar,
                                ultimaActualizacion = Date() // Actualizar timestamp
                            ) }

                            // Cargar hijos vinculados usando el DNI como ID familiar
                            cargarHijosVinculados(familiarId)
                            
                            // Cargar solicitudes pendientes
                            cargarSolicitudesPendientes(familiarId)
                            
                            // Cargar mensajes no leídos
                            cargarTotalMensajesNoLeidos(familiarId)
                            
                            // Cargar registros sin leer
                            cargarRegistrosSinLeer(familiarId)
                            
                            // Cargar ausencias pendientes
                            cargarAusenciasPendientes()
                        } else {
                            Timber.e("Familiar nulo en resultado exitoso")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No se encontró el perfil de familiar"
                                )
                            }
                        }
                    } else if (familiarResult is Result.Error) {
                        Timber.e(familiarResult.exception, "Error al cargar familiar")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = familiarResult.exception?.message ?: "Error desconocido"
                            )
                        }
                    }
                } else {
                    Timber.e("ID de usuario nulo")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No hay usuario autenticado"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar datos del familiar")
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
        Timber.d("Cargando hijos vinculados para familiar ID: $familiarId")
        _uiState.update { it.copy(isLoading = true) } // Indicar que la carga ha comenzado

        try {
            Timber.d("Usando estrategia directa: Buscando vinculaciones en 'vinculaciones_familiar_alumno' para familiar: $familiarId")
            val vinculacionesResult = familiarRepository.getAlumnoIdsByVinculaciones(familiarId)

            if (vinculacionesResult is Result.Success) {
                val alumnosIds = vinculacionesResult.data
                Timber.d("Vinculaciones encontradas: ${alumnosIds.size} para el familiar $familiarId")

                if (alumnosIds.isNotEmpty()) {
                    val alumnosVinculados = mutableListOf<Alumno>()
                    for (alumnoId in alumnosIds) {
                        try {
                            // Asegurarse de que el alumnoId no esté vacío o sea nulo antes de buscar
                            if (alumnoId.isNotBlank()) {
                                val alumnoResult = alumnoRepository.getAlumnoById(alumnoId)
                                if (alumnoResult is Result.Success) {
                                    alumnosVinculados.add(alumnoResult.data)
                                    Timber.d("Alumno cargado desde vinculación: ${alumnoResult.data.nombre} ${alumnoResult.data.apellidos} (ID: $alumnoId)")
                                } else if (alumnoResult is Result.Error) {
                                    Timber.e(alumnoResult.exception, "Error al cargar datos del alumno con ID: $alumnoId desde vinculaciones.")
                                }
                            } else {
                                Timber.w("ID de alumno vacío o nulo encontrado en vinculaciones para familiar $familiarId")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Excepción al cargar alumno con ID: $alumnoId desde vinculaciones.")
                        }
                    }

                    if (alumnosVinculados.isNotEmpty()) {
                        Timber.d("Total de ${alumnosVinculados.size} alumnos cargados desde vinculaciones.")
                        actualizarEstadoConHijos(alumnosVinculados.sortedBy { it.nombre })
                    } else {
                        Timber.w("No se pudieron cargar datos de alumnos para los IDs obtenidos de vinculaciones para familiar $familiarId.")
                        _uiState.update {
                            it.copy(
                                hijos = emptyList(),
                                hijoSeleccionado = null,
                                isLoading = false,
                                error = if (it.error == null) "No se encontraron alumnos vinculados." else it.error // Mantener error previo si existe
                            )
                        }
                    }
                } else {
                    Timber.d("No se encontraron IDs de alumnos en la colección 'vinculaciones_familiar_alumno' para el familiar: $familiarId")
                    _uiState.update {
                        it.copy(
                            hijos = emptyList(),
                            hijoSeleccionado = null,
                            isLoading = false,
                            error = if (it.error == null) "No tiene hijos vinculados." else it.error
                        )
                    }
                }
            } else if (vinculacionesResult is Result.Error) {
                Timber.e(vinculacionesResult.exception, "Error al buscar vinculaciones para familiar: $familiarId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al obtener vinculaciones: ${vinculacionesResult.exception?.message}"
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Excepción general al cargar hijos vinculados para familiar $familiarId: ${e.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Error inesperado al cargar hijos: ${e.message}"
                )
            }
        }
    }

    /**
     * Intenta obtener los hijos vinculados al familiar mediante métodos alternativos
     * cuando la estrategia principal falla
     *
     * @param familiarId ID del familiar
     */
    private suspend fun obtenerHijosPorMetodosAlternativos(familiarId: String) {
        // Método alternativo 1: Buscar en el campo familiarIds de los alumnos
        Timber.d("Estrategia 2 - Buscando alumnos que tengan al familiar en su lista de familiarIds")
        val alumnosAlternativos = alumnoRepository.getAlumnosWithFamiliarId(familiarId)
        
        if (alumnosAlternativos is Result.Success && alumnosAlternativos.data.isNotEmpty()) {
            val hijosAlternativos = alumnosAlternativos.data.sortedBy { it.nombre }
            Timber.d("Estrategia 2 - Encontrados ${hijosAlternativos.size} hijos por método alternativo (familiarIds)")
            actualizarEstadoConHijos(hijosAlternativos)
            return
        }
        
        // Método alternativo 2: Buscar directamente en el documento del familiar
        Timber.d("Estrategia 2b - Buscando IDs de hijos directamente en el documento del familiar")
        val idsHijos = familiarRepository.obtenerHijosIdsPorFamiliarId(familiarId)
        if (idsHijos != null && idsHijos.isNotEmpty()) {
            Timber.d("Estrategia 2b - Encontrados ${idsHijos.size} IDs de hijos en documento familiar")
            val alumnos = mutableListOf<Alumno>()
            
            for (hijoId in idsHijos) {
                try {
                    val alumnoResult = alumnoRepository.getAlumnoByDni(hijoId)
                    if (alumnoResult is Result.Success) {
                        alumnos.add(alumnoResult.data)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al cargar alumno por DNI: $hijoId")
                }
            }
            
            if (alumnos.isNotEmpty()) {
                Timber.d("Estrategia 2b - Cargados ${alumnos.size} alumnos directamente del documento familiar")
                actualizarEstadoConHijos(alumnos)
                return
            }
        }
        
        // Método alternativo 3: Verificar si el familiar es en realidad un usuario que podría tener roles múltiples
        Timber.d("Estrategia 3 - Verificando si el familiar es un usuario que pudiera tener otro tipo de información")
        val usuarioResult = usuarioRepository.getUsuarioById(familiarId)
        if (usuarioResult is Result.Success) {
            val usuario = usuarioResult.data
            Timber.d("Estrategia 3 - Usuario encontrado: ${usuario.nombre} ${usuario.apellidos}, buscando vínculos...")
            
            // Verificar si el usuario tiene alguna estructura que podría contener IDs de alumnos
            // Intentar extraer IDs de otras fuentes de datos
            val posiblesIds = obtenerPosiblesIdsDeAlumnos(usuario)
            
            if (posiblesIds.isNotEmpty()) {
                Timber.d("Estrategia 3 - Se encontraron ${posiblesIds.size} posibles IDs de alumnos")
                val alumnos = mutableListOf<Alumno>()
                
                for (hijoId in posiblesIds) {
                    try {
                        val alumnoResult = alumnoRepository.getAlumnoByDni(hijoId)
                        if (alumnoResult is Result.Success) {
                            alumnos.add(alumnoResult.data)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al cargar alumno por DNI: $hijoId")
                    }
                }
                
                if (alumnos.isNotEmpty()) {
                    Timber.d("Estrategia 3 - Cargados ${alumnos.size} alumnos a partir de IDs extraídos")
                    actualizarEstadoConHijos(alumnos)
                    return
                }
            }
            
            // Verificar perfiles del usuario
            Timber.d("Estrategia 4 - Verificando perfiles del usuario: ${usuario.perfiles.size} perfiles encontrados")
            
            // Los perfiles podrían contener información sobre alumnos asociados
            for (perfil in usuario.perfiles) {
                // Extraer IDs de alumnos del campo alumnos del perfil
                if (perfil.tipo == TipoUsuario.FAMILIAR && perfil.alumnos.isNotEmpty()) {
                    Timber.d("Encontrados ${perfil.alumnos.size} alumnos en perfil FAMILIAR")
                    for (alumnoId in perfil.alumnos) {
                        posiblesIds.add(alumnoId)
                    }
                }
            }
        }
        
        // Si llegamos aquí, no se encontraron hijos por ningún método
        Timber.w("No se encontraron hijos vinculados al familiar $familiarId por ningún método")
        _uiState.update { 
            it.copy(
                hijos = emptyList(),
                hijoSeleccionado = null,
                isLoading = false
            )
        }
    }
    
    /**
     * Actualiza el estado con la lista de hijos encontrados
     */
    private suspend fun actualizarEstadoConHijos(hijos: List<Alumno>) {
        val primerHijo = hijos.firstOrNull()
        
        _uiState.update { 
            it.copy(
                hijos = hijos,
                hijoSeleccionado = primerHijo,
                isLoading = false, // Asegurarse de que isLoading se ponga a false
                error = null // Limpiar errores si se cargaron hijos correctamente
            )
        }
        
        // Si hay un hijo seleccionado, cargar sus registros
        primerHijo?.let { alumno ->
            Timber.d("Cargando registros para hijo: ${alumno.nombre}")
            cargarRegistrosActividad(alumno.dni)
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
        try {
            // Aquí deberíamos consultar la base de datos para obtener los mensajes no leídos
            // Este es un ejemplo de implementación. Deberías reemplazarlo con la llamada real al repositorio.
            val mensajesNoLeidosCount = obtenerContadorMensajesNoLeidos(familiarId)
            
            // Actualizar el estado general
            _uiState.update { 
                it.copy(totalMensajesNoLeidos = mensajesNoLeidosCount)
            }
            
            // Actualizar también el flow específico para la UI
            _unreadMessageCount.update { mensajesNoLeidosCount }
            
            // Programar actualizaciones periódicas de este contador
            iniciarActualizacionesPeriodicasMensajes(familiarId)
        } catch (e: Exception) {
            Timber.e(e, "Error al cargar mensajes no leídos: ${e.message}")
        }
    }
    
    /**
     * Obtiene el contador de mensajes no leídos para un familiar
     * Esta función consulta Firestore para obtener los mensajes no leídos reales
     */
    private suspend fun obtenerContadorMensajesNoLeidos(familiarId: String): Int {
        // En un sistema real, aquí deberías consultar tu repositorio de mensajes
        // Por ejemplo:
        // val result = mensajeRepository.getMensajesNoLeidosCount(familiarId)
        // return if (result is Result.Success) result.data else 0
        
        // Simulamos una consulta para pruebas (reemplazar con tu lógica real)
        return 5 // Valor de prueba
    }
    
    /**
     * Inicia actualizaciones periódicas del contador de mensajes no leídos
     */
    private fun iniciarActualizacionesPeriodicasMensajes(familiarId: String) {
        viewModelScope.launch {
            while(true) {
                // Esperar 5 minutos antes de actualizar de nuevo
                delay(5 * 60 * 1000)
                
                // Actualizar contador de mensajes no leídos
                val nuevoContador = obtenerContadorMensajesNoLeidos(familiarId)
                _unreadMessageCount.update { nuevoContador }
                
                // También actualizar el estado general
                _uiState.update { 
                    it.copy(totalMensajesNoLeidos = nuevoContador)
                }
            }
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
        // También actualizamos la última hora de actualización
        _uiState.update { it.copy(ultimaActualizacion = Date()) }
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
            Timber.d("🚀 Iniciando creación de solicitud - FamiliarId: $familiarId, AlumnoDNI: $alumnoDni, CentroId: $centroId")
            
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
                    
                    Timber.d("📝 Solicitud creada: $solicitud")
                    
                    // Enviar solicitud
                    when (val result = solicitudRepository.crearSolicitudVinculacion(solicitud)) {
                        is Result.Success -> {
                            Timber.d("✅ Solicitud creada exitosamente: ${result.data.id}")
                            
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
                            Timber.e(result.exception, "❌ Error al crear solicitud")
                            
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
     * Actualiza el estado para mostrar un mensaje de confirmación tras crear una solicitud
     */
    fun resetSolicitudEnviada() {
        _uiState.update { 
            it.copy(
                solicitudEnviada = false,
                ultimaActualizacion = Date() // Actualizar también el timestamp
            )
        }
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
     * @param registroId ID opcional de un registro específico para ver su detalle
     */
    fun navegarAConsultaRegistroDiario(
        navController: NavController, 
        alumno: Alumno,
        registroId: String? = null
    ) {
        navController.navigate(
            AppScreens.ConsultaRegistroDiario.createRoute(
                alumnoId = alumno.dni,
                alumnoNombre = "${alumno.nombre} ${alumno.apellidos}",
                registroId = registroId
            )
        )
    }

    /**
     * Extrae posibles IDs de alumnos de diferentes propiedades de un usuario
     * 
     * @param usuario El usuario del que extraer información
     * @return Lista mutable de posibles IDs de alumnos
     */
    private fun obtenerPosiblesIdsDeAlumnos(usuario: Usuario): MutableList<String> {
        // Si el usuario tiene un nombreAlumno, registrarlo para depuración
        usuario.nombreAlumno?.let { nombre ->
            Timber.d("El usuario tiene un nombreAlumno: $nombre, podría estar relacionado con un alumno")
        }
        
        // Creamos una lista mutable para poder añadir elementos
        val posiblesIds = mutableListOf<String>()
        
        // Añadimos los IDs de alumnos de los perfiles de tipo FAMILIAR
        usuario.perfiles
            .filter { it.tipo == TipoUsuario.FAMILIAR && it.alumnos.isNotEmpty() }
            .forEach { perfil -> 
                Timber.d("Encontrados ${perfil.alumnos.size} alumnos en perfil FAMILIAR")
                posiblesIds.addAll(perfil.alumnos)
            }
            
        return posiblesIds
    }

    /**
     * Marca todos los mensajes como leídos al navegar a la bandeja de entrada
     * 
     * Este método actualiza el contador de mensajes no leídos a 0 cuando
     * el usuario navega a la pantalla de mensajes unificados.
     * También marca todos los mensajes no leídos como leídos en la base de datos.
     */
    fun marcarMensajesLeidos() {
        viewModelScope.launch {
            try {
                // Obtener el ID del familiar
                val familiarId = uiState.value.familiar?.id ?: return@launch
                val usuario = authRepository.getCurrentUser() ?: return@launch
                
                Timber.d("Marcando mensajes como leídos para el familiar $familiarId")
                
                // Intentar obtener todos los mensajes no leídos del usuario actual
                unifiedMessageRepository.getCurrentUserInbox().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val mensajesNoLeidos = result.data.filter { !it.isRead }
                            
                            // Marcar cada mensaje como leído
                            Timber.d("Marcando ${mensajesNoLeidos.size} mensajes como leídos")
                            mensajesNoLeidos.forEach { mensaje ->
                                try {
                                    unifiedMessageRepository.markAsRead(mensaje.id)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al marcar mensaje ${mensaje.id} como leído")
                                }
                            }
                            
                            // Actualizar el estado local inmediatamente para reflejar el cambio en la UI
                            _uiState.update { 
                                it.copy(
                                    registrosSinLeer = 0,
                                    totalMensajesNoLeidos = 0
                                )
                            }
                            
                            // Actualizar también el flow específico para la UI
                            _unreadMessageCount.update { 0 }
                            
                            Timber.d("Mensajes marcados como leídos para el familiar $familiarId")
                        }
                        is Result.Error -> {
                            Timber.e(result.exception, "Error al obtener mensajes para marcar como leídos")
                            
                            // Aún así, actualizamos el contador local para mejorar la experiencia de usuario
                            _uiState.update { 
                                it.copy(
                                    registrosSinLeer = 0,
                                    totalMensajesNoLeidos = 0
                                )
                            }
                            _unreadMessageCount.update { 0 }
                        }
                        is Result.Loading -> {
                            // No hacer nada durante la carga
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensajes como leídos")
                
                // En caso de error, actualizar igualmente el contador local
                _uiState.update { 
                    it.copy(
                        registrosSinLeer = 0,
                        totalMensajesNoLeidos = 0
                    )
                }
                _unreadMessageCount.update { 0 }
            }
        }
    }

    /**
     * Notifica una ausencia para un alumno específico
     */
    fun notificarAusencia(
        alumnoId: String,
        alumnoNombre: String,
        fechaAusencia: Date,
        motivo: String,
        duracion: Int,
        claseId: String,
        claseCurso: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isNotificandoAusencia = true,
                ausenciaNotificada = false,
                mensajeExitoAusencia = null,
                error = null
            ) }

            try {
                // Obtener datos del usuario familiar
                val usuario = authRepository.getCurrentUser()
                if (usuario == null) {
                    _uiState.update { it.copy(
                        isNotificandoAusencia = false,
                        error = "No se pudo obtener información del usuario"
                    ) }
                    return@launch
                }

                // Crear notificación de ausencia
                val notificacion = NotificacionAusencia(
                    alumnoId = alumnoId,
                    alumnoNombre = alumnoNombre,
                    claseId = claseId,
                    claseCurso = claseCurso,
                    familiarId = usuario.dni ?: "", // Usar DNI como ID
                    familiarNombre = "${usuario.nombre} ${usuario.apellidos}",
                    fechaAusencia = Timestamp(fechaAusencia),
                    fechaNotificacion = Timestamp.now(),
                    motivo = motivo,
                    duracion = duracion,
                    estado = EstadoNotificacionAusencia.PENDIENTE.name
                )

                // Guardar notificación
                val resultado = notificacionAusenciaRepository.registrarAusencia(notificacion)
                
                when (resultado) {
                    is Result.Success -> {
                        _uiState.update { it.copy(
                            isNotificandoAusencia = false,
                            ausenciaNotificada = true,
                            mensajeExitoAusencia = "Ausencia notificada correctamente"
                        ) }
                        
                        // Recargar ausencias pendientes
                        cargarAusenciasPendientes()
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            isNotificandoAusencia = false,
                            error = "Error al notificar ausencia: ${resultado.exception?.message ?: "Error desconocido"}"
                        ) }
                    }
                    else -> { /* Estado de carga, no hacemos nada */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al notificar ausencia")
                _uiState.update { it.copy(
                    isNotificandoAusencia = false,
                    error = "Error al notificar ausencia: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Carga las ausencias pendientes notificadas por el familiar
     */
    fun cargarAusenciasPendientes() {
        viewModelScope.launch {
            try {
                val usuario = authRepository.getCurrentUser()
                if (usuario != null) {
                    val familiarId = usuario.dni ?: return@launch // Usar DNI como ID
                    val resultado = notificacionAusenciaRepository.obtenerAusenciasPorFamiliar(familiarId)
                    
                    if (resultado is Result.Success) {
                        val ausencias = resultado.data.filter { 
                            it.estado == EstadoNotificacionAusencia.PENDIENTE.name ||
                            it.estado == EstadoNotificacionAusencia.ACEPTADA.name
                        }
                        _uiState.update { it.copy(ausenciasPendientes = ausencias) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar ausencias pendientes")
                // No actualizamos el estado de error para no interrumpir la experiencia
            }
        }
    }

    /**
     * Reinicia el estado de notificación de ausencia
     */
    fun resetAusenciaNotificada() {
        _uiState.update { it.copy(
            ausenciaNotificada = false,
            mensajeExitoAusencia = null
        ) }
    }
}