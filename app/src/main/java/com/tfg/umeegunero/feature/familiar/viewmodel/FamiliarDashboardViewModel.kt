package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
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
    val isLoading: Boolean = false,
    
    // Mensaje de error, null si no hay errores
    val error: String? = null,
    
    // Datos del usuario familiar logueado
    val familiar: Usuario? = null,
    
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
    val navigateToWelcome: Boolean = false
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
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
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
        cargarDatosFamiliar()
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
        // Lanzamos una corrutina en el scope del ViewModel
        // Al usar viewModelScope, la corrutina se cancelará automáticamente
        // cuando el ViewModel sea destruido, evitando memory leaks
        viewModelScope.launch {
            // Actualizamos el estado para mostrar el indicador de carga
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Obtenemos el ID del usuario actual
                val userId = usuarioRepository.getUsuarioActualId()

                if (userId.isBlank()) {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo obtener el usuario actual",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Cargamos los datos del familiar desde el repositorio
                val familiarResult = usuarioRepository.getUsuarioPorDni(userId)

                // Procesamos el resultado que viene encapsulado en un tipo Result<T>
                when (familiarResult) {
                    is Result.Success -> {
                        val familiar = familiarResult.data
                        _uiState.update {
                            it.copy(
                                familiar = familiar,
                                isLoading = false
                            )
                        }

                        // Obtenemos los perfiles del familiar
                        // Un usuario puede tener varios perfiles (familiar, profesor, etc.)
                        val perfilFamiliar = familiar.perfiles.firstOrNull { it.tipo == TipoUsuario.FAMILIAR }

                        // Si tenemos un perfil de familiar, cargamos sus hijos
                        if (perfilFamiliar != null && perfilFamiliar.alumnos.isNotEmpty()) {
                            cargarHijos(perfilFamiliar.alumnos)
                        } else {
                            _uiState.update {
                                it.copy(
                                    error = "No se encontraron datos de hijos asociados",
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        // Actualizamos el estado con el error y utilizamos Timber para logging
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar datos del familiar: ${familiarResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(familiarResult.exception, "Error al cargar familiar")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                // Capturamos cualquier excepción no manejada y actualizamos el estado
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar familiar")
            }
        }
    }

    /**
     * Carga los datos de los hijos del familiar
     * 
     * Este método recupera la información detallada de cada alumno (hijo)
     * asociado al familiar, incluyendo sus datos personales, académicos y profesores.
     * 
     * @param alumnosIds Lista de identificadores (DNIs) de los alumnos a cargar
     */
    private fun cargarHijos(alumnosIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val hijos = mutableListOf<Alumno>()
                val profesoresIds = mutableSetOf<String>()

                // Cargamos cada hijo mediante su identificador
                for (alumnoId in alumnosIds) {
                    // Obtener los datos del alumno desde el repositorio
                    val alumnoResult = usuarioRepository.getAlumnoPorDni(alumnoId)

                    when (alumnoResult) {
                        is Result.Success -> {
                            val alumno = alumnoResult.data
                            hijos.add(alumno)

                            // Recopilamos IDs de profesores para cargarlos después
                            // Esto optimiza las llamadas al repositorio al hacerlo en batch
                            alumno.profesorIds.let { profesoresIds.addAll(it) }
                        }
                        is Result.Error -> {
                            Timber.e(alumnoResult.exception, "Error al cargar hijo con ID: $alumnoId")
                        }
                        else -> { /* Ignorar estado Loading */ }
                    }
                }

                // Actualizamos el estado con la lista de hijos y seleccionamos el primero
                _uiState.update {
                    it.copy(
                        hijos = hijos,
                        hijoSeleccionado = hijos.firstOrNull(),
                        isLoading = false
                    )
                }

                // Cargamos los profesores asociados a los hijos
                if (profesoresIds.isNotEmpty()) {
                    cargarProfesores(profesoresIds.toList())
                }

                // Cargamos los registros de actividad del primer hijo
                hijos.firstOrNull()?.let { cargarRegistrosActividad(it.dni) }

                // Cargamos mensajes no leídos
                cargarMensajesNoLeidos()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al cargar datos de los hijos: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar hijos")
            }
        }
    }

    /**
     * Carga los profesores por sus IDs
     * 
     * Este método obtiene los datos completos de los profesores a partir
     * de sus identificadores y los almacena en un mapa para acceso rápido.
     * 
     * @param profesoresIds Lista de identificadores (DNIs) de los profesores a cargar
     */
    private fun cargarProfesores(profesoresIds: List<String>) {
        viewModelScope.launch {
            try {
                val profesores = mutableMapOf<String, Usuario>()

                // Iteramos por cada ID de profesor y cargamos sus datos
                for (profesorId in profesoresIds) {
                    val profesorResult = usuarioRepository.getUsuarioPorDni(profesorId)

                    if (profesorResult is Result.Success) {
                        // Guardamos en el mapa usando el ID como clave para acceso rápido
                        profesores[profesorId] = profesorResult.data
                    }
                }

                // Actualizamos el estado con los profesores obtenidos
                _uiState.update {
                    it.copy(profesores = profesores)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar profesores")
            }
        }
    }

    /**
     * Carga los registros de actividad de un hijo
     * 
     * Este método obtiene todos los registros de actividad (tareas, asistencia,
     * notas, etc.) asociados a un alumno específico para mostrarlos en el dashboard.
     * 
     * @param alumnoId Identificador (DNI) del alumno cuyos registros se quieren cargar
     */
    fun cargarRegistrosActividad(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Consultar los registros de actividad asociados al alumno
                val registrosResult = usuarioRepository.getRegistrosActividadByAlumno(alumnoId)

                when (registrosResult) {
                    is Result.Success -> {
                        val registros = registrosResult.data

                        // Contamos cuántos registros no han sido vistos por el familiar
                        // Esto nos permitirá mostrar notificaciones o badges en la UI
                        val noLeidos = registros.count { !it.vistoPorFamiliar }

                        _uiState.update {
                            it.copy(
                                registrosActividad = registros,
                                registrosSinLeer = noLeidos,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar registros: ${registrosResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(registrosResult.exception, "Error al cargar registros de actividad")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar registros de actividad")
            }
        }
    }

    /**
     * Carga los mensajes no leídos del familiar
     * 
     * Este método obtiene todos los mensajes que han sido enviados al familiar
     * pero aún no han sido marcados como leídos, para mostrarlos en la bandeja
     * de entrada y generar notificaciones.
     */
    fun cargarMensajesNoLeidos() {
        viewModelScope.launch {
            // Obtenemos el ID del familiar actual, si no está disponible terminamos
            val familiarId = _uiState.value.familiar?.documentId ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val mensajesResult = usuarioRepository.getMensajesNoLeidos(familiarId)

                when (mensajesResult) {
                    is Result.Success -> {
                        val mensajes = mensajesResult.data
                        _uiState.update {
                            it.copy(
                                mensajesNoLeidos = mensajes,
                                totalMensajesNoLeidos = mensajes.size,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar mensajes: ${mensajesResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(mensajesResult.exception, "Error al cargar mensajes")
                    }
                    else -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar mensajes")
            }
        }
    }

    /**
     * Marca un registro como visto por el familiar
     * 
     * Este método actualiza el estado de un registro de actividad para
     * indicar que ha sido visto por el familiar, tanto en la base de datos
     * como en el estado local.
     * 
     * @param registroId Identificador único del registro a marcar como visto
     */
    fun marcarRegistroComoVisto(registroId: String) {
        viewModelScope.launch {
            try {
                // Actualizar el estado del registro para indicar que ha sido visto por el familiar
                val result = usuarioRepository.marcarRegistroComoVistoPorFamiliar(registroId)

                if (result is Result.Success) {
                    // Actualizamos los registros localmente sin tener que volver a cargarlos
                    // Esto mejora el rendimiento y proporciona actualizaciones instantáneas en la UI
                    val registrosActualizados = _uiState.value.registrosActividad.map { registro ->
                        if (registro.id == registroId) {
                            // Creamos una copia del registro con los campos actualizados
                            registro.copy(vistoPorFamiliar = true, fechaVisto = Timestamp.now())
                        } else {
                            registro
                        }
                    }

                    // Actualizamos el estado con los registros modificados
                    _uiState.update {
                        it.copy(
                            registrosActividad = registrosActualizados,
                            registrosSinLeer = it.registrosSinLeer - 1
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar registro como visto")
            }
        }
    }

    /**
     * Envía un mensaje al profesor
     * 
     * Este método crea y envía un nuevo mensaje desde el familiar al profesor
     * seleccionado, relacionado con un alumno específico.
     * 
     * @param profesorId ID del profesor destinatario del mensaje
     * @param alumnoId ID del alumno relacionado con el mensaje, puede ser null si no aplica
     * @param texto Contenido del mensaje a enviar
     */
    fun enviarMensaje(profesorId: String, alumnoId: String?, texto: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val familiar = _uiState.value.familiar ?: throw Exception("No hay familiar logueado")

                // Creamos el objeto mensaje con todos sus campos
                val mensaje = Mensaje(
                    id = "", // Se generará al guardar en la base de datos
                    emisorId = familiar.documentId,
                    receptorId = profesorId,
                    alumnoId = alumnoId ?: "",
                    texto = texto,
                    timestamp = Timestamp.now(),
                    leido = false
                )

                // Enviar el mensaje al repositorio para su almacenamiento
                usuarioRepository.enviarMensaje(mensaje)
                
                // Terminamos la carga independientemente del resultado
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al enviar mensaje: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error al enviar mensaje")
            }
        }
    }

    /**
     * Selecciona un hijo para ver sus detalles
     * 
     * Este método actualiza el hijo seleccionado en el estado de la UI
     * y carga sus registros de actividad para mostrarlos en el dashboard.
     * 
     * @param alumnoId ID del alumno que se quiere seleccionar
     */
    fun seleccionarHijo(alumnoId: String) {
        // Buscamos el alumno en la lista de hijos por su ID
        val hijo = _uiState.value.hijos.find { it.dni == alumnoId }

        hijo?.let {
            // Actualizamos el estado con el hijo seleccionado
            _uiState.update { state -> state.copy(hijoSeleccionado = it) }
            // Cargamos sus registros de actividad
            cargarRegistrosActividad(alumnoId)
        }
    }

    /**
     * Cambia la pestaña seleccionada
     * 
     * Este método actualiza la pestaña activa en el dashboard y carga
     * los datos específicos necesarios para esa pestaña.
     * 
     * @param tab Índice de la pestaña a seleccionar (0: Home, 1: Alumnos, 2: Asistencia, 3: Mensajes)
     */
    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }

        // Cargamos datos específicos según la pestaña seleccionada
        when (tab) {
            0 -> {
                // Home - Recargamos datos del hijo seleccionado
                _uiState.value.hijoSeleccionado?.let {
                    cargarRegistrosActividad(it.dni)
                }
            }
            3 -> {
                // Mensajes - Cargamos los mensajes no leídos
                cargarMensajesNoLeidos()
            }
        }
    }

    /**
     * Limpia el error actual
     * 
     * Este método elimina cualquier mensaje de error del estado,
     * típicamente después de que ha sido mostrado al usuario.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Cierra la sesión del usuario
     * 
     * Este método realiza el proceso de logout mediante el AuthRepository
     * y actualiza el estado para redirigir al usuario a la pantalla de bienvenida.
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Utiliza el repositorio de autenticación para cerrar sesión
            authRepository.signOut()
            
            // Actualiza el estado para navegar a la pantalla de bienvenida
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    navigateToWelcome = true
                )
            }
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