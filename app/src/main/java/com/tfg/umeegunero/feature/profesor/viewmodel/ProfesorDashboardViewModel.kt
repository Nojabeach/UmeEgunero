package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Estado UI para la pantalla de dashboard del profesor
 * 
 * Esta clase data contiene todas las propiedades necesarias para representar
 * el estado completo de la interfaz de usuario del dashboard del profesor.
 * Siguiendo el patrón de State en Jetpack Compose, esta clase inmutable 
 * permite una gestión eficiente y predecible del estado de la UI.
 *
 * @property isLoading Indica si hay alguna operación de carga en curso
 * @property error Mensaje de error a mostrar, null si no hay errores
 * @property profesor Datos del profesor logueado actualmente
 * @property clases Lista de todas las clases asignadas al profesor
 * @property claseActual Clase seleccionada actualmente para mostrar sus detalles
 * @property alumnos Lista de alumnos de la clase actualmente seleccionada
 * @property alumnosPendientes Alumnos que requieren atención o tienen tareas pendientes
 * @property registrosActividad Registros de actividad relacionados con la clase actual
 * @property mensajesNoLeidos Mensajes sin leer dirigidos al profesor
 * @property totalMensajesNoLeidos Contador total de mensajes no leídos para mostrar badge
 * @property selectedTab Pestaña seleccionada actualmente en el dashboard
 * @property navigateToWelcome Flag para controlar la navegación a la pantalla de inicio tras logout
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.2
 */
data class ProfesorDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profesor: Usuario? = null,
    val clases: List<Clase> = emptyList(),
    val claseActual: Clase? = null,
    val alumnos: List<Alumno> = emptyList(),
    val alumnosPendientes: List<Alumno> = emptyList(),
    val registrosActividad: List<RegistroActividad> = emptyList(),
    val mensajesNoLeidos: List<Mensaje> = emptyList(),
    val totalMensajesNoLeidos: Int = 0,
    val selectedTab: Int = 0,
    val navigateToWelcome: Boolean = false
)

/**
 * ViewModel para la pantalla de dashboard del profesor
 *
 * Este ViewModel implementa el patrón MVVM (Model-View-ViewModel) y es responsable 
 * de toda la lógica de negocio relacionada con el dashboard del profesor:
 * - Gestión del estado de la UI mediante StateFlow
 * - Carga de datos desde los repositorios
 * - Procesamiento de eventos del usuario
 * - Manejo de operaciones asíncronas mediante corrutinas
 *
 * El ViewModel actúa como intermediario entre la capa de datos (repositorios) 
 * y la capa de presentación (Composables), manteniendo la separación de 
 * responsabilidades y facilitando las pruebas unitarias.
 *
 * @property usuarioRepository Repositorio para acceder a datos de usuarios, alumnos y registros
 * @property authRepository Repositorio para gestionar la autenticación
 *
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.2
 */
@HiltViewModel
class ProfesorDashboardViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado mutable interno que solo el ViewModel puede modificar
    private val _uiState = MutableStateFlow(ProfesorDashboardUiState())
    
    // Estado inmutable expuesto a la UI, para seguir el principio de encapsulamiento
    val uiState: StateFlow<ProfesorDashboardUiState> = _uiState.asStateFlow()

    /**
     * Inicialización del ViewModel
     * 
     * Se ejecuta automáticamente al crear la instancia del ViewModel.
     * Inicia la carga de datos del profesor para poblar el dashboard.
     */
    init {
        cargarDatosProfesor()
    }

    /**
     * Carga los datos del profesor actual
     * 
     * Este método obtiene los datos completos del profesor logueado,
     * incluyendo información personal y profesional. Es el punto de 
     * partida para cargar todos los datos relacionados con el profesor.
     *
     * Secuencia de operaciones:
     * 1. Actualiza el estado para mostrar indicador de carga
     * 2. Obtiene el ID del usuario actual autenticado
     * 3. Consulta los datos completos del profesor desde el repositorio
     * 4. Actualiza el estado con los datos obtenidos
     * 5. Inicia la carga de clases asignadas al profesor
     *
     * En caso de error, actualiza el estado con el mensaje apropiado
     * y registra el error en el sistema de logging.
     */
    fun cargarDatosProfesor() {
        viewModelScope.launch {
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

                // Cargamos los datos del profesor
                val profesorResult = usuarioRepository.getUsuarioPorDni(userId)

                when (profesorResult) {
                    is Result.Success<Usuario> -> {
                        val profesor = profesorResult.data
                        _uiState.update {
                            it.copy(
                                profesor = profesor,
                                isLoading = false
                            )
                        }

                        // Una vez cargado el profesor, cargamos sus clases
                        cargarClasesProfesor(profesor.documentId)
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar datos del profesor: ${profesorResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(profesorResult.exception, "Error al cargar profesor")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar profesor")
            }
        }
    }

    /**
     * Carga las clases asignadas al profesor
     * 
     * Este método recupera todas las clases que el profesor tiene asignadas
     * y selecciona la primera como clase actual para mostrar sus detalles.
     * 
     * La información de las clases es fundamental para el dashboard ya que:
     * - Permite al profesor ver rápidamente sus grupos asignados
     * - Sirve como filtro para cargar alumnos específicos
     * - Estructura la información de asistencia y actividades
     *
     * @param profesorId Identificador único del profesor
     */
    private fun cargarClasesProfesor(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val clasesResult = usuarioRepository.getClasesByProfesor(profesorId)

                when (clasesResult) {
                    is Result.Success<List<Clase>> -> {
                        val clases = clasesResult.data
                        _uiState.update {
                            it.copy(
                                clases = clases,
                                claseActual = clases.firstOrNull(),
                                isLoading = false
                            )
                        }

                        // Si hay clases, cargamos los alumnos de la primera clase
                        if (clases.isNotEmpty()) {
                            cargarAlumnosClase(clases.first().id)
                        } else {
                            // Si no hay clases, actualizamos el estado para reflejar que no hay alumnos
                            _uiState.update {
                                it.copy(
                                    alumnos = emptyList(),
                                    alumnosPendientes = emptyList(),
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar clases: ${clasesResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(clasesResult.exception, "Error al cargar clases")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar clases")
            }
        }
    }

    /**
     * Carga los alumnos de una clase específica
     * 
     * Este método obtiene la lista completa de alumnos pertenecientes
     * a una clase específica y luego carga información adicional como
     * registros de actividad pendientes.
     * 
     * Esta información es crítica para:
     * - Mostrar la lista de alumnos en el dashboard
     * - Identificar alumnos que requieren atención especial
     * - Proporcionar acceso rápido a datos de cada alumno
     *
     * @param claseId Identificador único de la clase
     */
    fun cargarAlumnosClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val alumnosResult = usuarioRepository.getAlumnosByClase(claseId)

                when (alumnosResult) {
                    is Result.Success<List<Alumno>> -> {
                        val alumnos = alumnosResult.data
                        _uiState.update {
                            it.copy(
                                alumnos = alumnos,
                                isLoading = false
                            )
                        }

                        // Cargamos los registros de actividad pendientes
                        cargarRegistrosPendientes(alumnos.map { it.dni })
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar alumnos: ${alumnosResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(alumnosResult.exception, "Error al cargar alumnos")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar alumnos")
            }
        }
    }

    /**
     * Carga los registros de actividad pendientes para los alumnos
     * 
     * Este método identifica los alumnos que no tienen registros de actividad
     * para el día actual, lo que permite al profesor priorizar acciones.
     * 
     * Es especialmente importante para:
     * - Gestionar eficientemente el seguimiento de los alumnos
     * - Garantizar que todos los alumnos tengan sus registros diarios
     * - Proporcionar alertas visuales en el dashboard
     *
     * @param alumnosIds Lista de identificadores de alumnos a verificar
     */
    private fun cargarRegistrosPendientes(alumnosIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Obtenemos la fecha actual
                val hoy = Timestamp.now()

                // Filtramos los alumnos que no tienen registro para hoy
                val alumnosPendientesResult = usuarioRepository.getAlumnosSinRegistroHoy(alumnosIds, hoy)

                when (alumnosPendientesResult) {
                    is Result.Success<List<Alumno>> -> {
                        _uiState.update {
                            it.copy(
                                alumnosPendientes = alumnosPendientesResult.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        // No actualizamos el error en el estado porque este es un dato secundario
                        // y no queremos interrumpir la experiencia del usuario si falla
                        Timber.e(alumnosPendientesResult.exception, "Error al cargar alumnos pendientes")
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar alumnos pendientes")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Carga los mensajes no leídos del profesor
     */
    fun cargarMensajesNoLeidos() {
        viewModelScope.launch {
            val profesorId = _uiState.value.profesor?.documentId ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val mensajesResult = usuarioRepository.getMensajesNoLeidos(profesorId)

                when (mensajesResult) {
                    is Result.Success<List<Mensaje>> -> {
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
                                error = "Error al cargar mensajes: ${mensajesResult.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(mensajesResult.exception, "Error al cargar mensajes")
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
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
     * Crea un nuevo registro de actividad para un alumno
     */
    fun crearRegistroActividad(alumnoId: String) {
        viewModelScope.launch {
            val profesorId = _uiState.value.profesor?.documentId ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                // Creamos un registro básico
                val registro = RegistroActividad(
                    alumnoId = alumnoId,
                    profesorId = profesorId,
                    fecha = Timestamp.now()
                )

                val resultadoCreacion = usuarioRepository.crearRegistroActividad(registro)

                when (resultadoCreacion) {
                    is Result.Success -> {
                        // Actualizamos la lista de alumnos pendientes
                        val alumnosPendientesActualizados = _uiState.value.alumnosPendientes.filter { it.dni != alumnoId }

                        _uiState.update {
                            it.copy(
                                alumnosPendientes = alumnosPendientesActualizados,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al crear registro: ${resultadoCreacion.exception?.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(resultadoCreacion.exception, "Error al crear registro")
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
                Timber.e(e, "Error inesperado al crear registro")
            }
        }
    }

    /**
     * Cambia la pestaña seleccionada
     */
    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }

        // Cargamos datos específicos según la pestaña
        when (tab) {
            0 -> {
                // Home - Recargamos datos del profesor y alumnos pendientes
                val claseActual = _uiState.value.claseActual
                if (claseActual != null) {
                    cargarAlumnosClase(claseActual.id)
                }
            }
            1 -> {
                // Mis Alumnos - Cargamos todos los alumnos
                val claseActual = _uiState.value.claseActual
                if (claseActual != null) {
                    cargarAlumnosClase(claseActual.id)
                }
            }
            3 -> {
                // Mensajes - Cargamos los mensajes no leídos
                cargarMensajesNoLeidos()
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
     * Este método gestiona el proceso de cierre de sesión:
     * 1. Llama al repositorio de autenticación para cerrar sesión
     * 2. Actualiza el estado UI para activar la navegación a la pantalla de bienvenida
     * 3. Maneja posibles errores durante el proceso
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Intentamos cerrar sesión 
                val result = authRepository.signOut()
                
                if (result) {
                    _uiState.update { 
                        it.copy(
                            navigateToWelcome = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "Error al cerrar sesión",
                            isLoading = false
                        )
                    }
                    Timber.e("Error al cerrar sesión")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error inesperado al cerrar sesión: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cerrar sesión")
            }
        }
    }

    /**
     * Navega a la pantalla de registro diario de un alumno
     * 
     * @param navController Controlador de navegación para gestionar la navegación entre pantallas
     * @param alumno Alumno para el que se va a registrar la actividad
     * @param profesorId ID del profesor que realiza el registro
     * @param claseId ID de la clase donde se registra la actividad
     * @param claseNombre Nombre de la clase para mostrar en la pantalla de registro
     */
    fun navegarARegistroDiario(
        navController: NavController,
        alumno: Alumno,
        profesorId: String,
        claseId: String,
        claseNombre: String
    ) {
        navController.navigate("registro_diario/${alumno.dni}/$profesorId/$claseId/${claseNombre.replace(" ", "_")}")
    }
}