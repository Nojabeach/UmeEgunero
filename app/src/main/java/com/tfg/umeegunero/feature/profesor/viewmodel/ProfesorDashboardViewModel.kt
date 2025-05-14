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
import com.tfg.umeegunero.data.repository.CalendarioRepository
import com.tfg.umeegunero.data.repository.RegistroDiarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.DayOfWeek
import java.util.Date
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
 * @property profesorNombre Nombre completo del profesor para saludo
 * @property clases Lista de todas las clases asignadas al profesor
 * @property claseActual Clase seleccionada actualmente para mostrar sus detalles
 * @property claseInfo Texto descriptivo de la clase/alumnos a cargo
 * @property alumnos Lista de alumnos de la clase actualmente seleccionada
 * @property alumnosPendientes Alumnos que requieren atención o registro diario pendiente
 * @property esFestivoHoy Booleano que indica si el día actual es festivo
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
    val profesorNombre: String? = null,
    val clases: List<Clase> = emptyList(),
    val claseActual: Clase? = null,
    val claseInfo: String? = null,
    val alumnos: List<Alumno> = emptyList(),
    val alumnosPendientes: List<Alumno> = emptyList(),
    val esFestivoHoy: Boolean = false,
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
 * @property calendarioRepository Repositorio para festivos
 * @property registroRepository Repositorio para registros diarios
 * @property registroDiarioRepository Repositorio para registros diarios
 *
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.3
 */
@HiltViewModel
class ProfesorDashboardViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val calendarioRepository: CalendarioRepository,
    private val registroDiarioRepository: RegistroDiarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfesorDashboardUiState())
    val uiState: StateFlow<ProfesorDashboardUiState> = _uiState.asStateFlow()

    // Flow específico para el contador de mensajes no leídos
    private val _unreadMessageCount = MutableStateFlow(0)
    val unreadMessageCount: StateFlow<Int> = _unreadMessageCount.asStateFlow()

    init {
        cargarDatosInicialesDashboard()
    }

    /**
     * Carga todos los datos iniciales necesarios para el dashboard.
     * Orquesta la carga del profesor, sus clases, alumnos y el estado de festivo.
     */
    private fun cargarDatosInicialesDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Usuario no autenticado.") }
                return@launch
            }

            try {
                // 1. Cargar Profesor
                val profesor = cargarProfesor(userId)
                if (profesor == null) return@launch

                // 2. Cargar Clases y Alumnos (y construir claseInfo)
                val claseActualId = cargarClasesYAlumnos(profesor.dni)

                // 3. Determinar si es festivo (Lógica REAL)
                val hoyEsFestivo = esDiaFestivoHoy()
                _uiState.update { it.copy(esFestivoHoy = hoyEsFestivo) }

                // 4. Cargar Alumnos Pendientes (solo si hay clase actual y no es festivo)
                if (claseActualId != null && !hoyEsFestivo) {
                    cargarAlumnosPendientes(claseActualId)
                } else if (hoyEsFestivo) {
                    // Si es festivo, asegurarse de que la lista de pendientes esté vacía
                    _uiState.update { it.copy(alumnosPendientes = emptyList()) }
                }

                // 5. Cargar mensajes no leídos (opcional, si se muestra contador)
                cargarMensajesNoLeidos()

            } catch (e: Exception) {
                Timber.e(e, "Error cargando datos iniciales del dashboard")
                _uiState.update { it.copy(isLoading = false, error = "Error inesperado al cargar datos.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Carga los datos del profesor y actualiza el estado.
     * @return El objeto Usuario del profesor o null si hay error.
     */
    private suspend fun cargarProfesor(userId: String): Usuario? {
        return when (val result = usuarioRepository.getUsuarioById(userId)) {
            is Result.Success -> {
                val profesor = result.data
                _uiState.update { it.copy(profesor = profesor, profesorNombre = "${profesor.nombre} ${profesor.apellidos}".trim()) }
                profesor
            }
            is Result.Error -> {
                Timber.e(result.exception, "Error al cargar profesor con ID: $userId")
                _uiState.update { it.copy(error = "Error al cargar datos del profesor.", isLoading = false) }
                null
            }
            is Result.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
                null // O manejar la espera si es necesario
            }
        }
    }

    /**
     * Carga las clases del profesor y los alumnos de la primera clase encontrada.
     * Actualiza claseActual y claseInfo.
     * @return El ID de la clase actual cargada, o null si no hay clases o hay error.
     */
    private suspend fun cargarClasesYAlumnos(profesorId: String): String? {
        return when (val resultClases = usuarioRepository.getClasesByProfesor(profesorId)) {
            is Result.Success -> {
                val clases = resultClases.data
                val claseActual = clases.firstOrNull()
                var alumnosDeClaseActual: List<Alumno> = emptyList()
                var infoClase: String? = null
                var claseActualId: String? = null

                if (claseActual != null) {
                    claseActualId = claseActual.id
                    when (val resultAlumnos = usuarioRepository.getAlumnosByClase(claseActualId)) {
                        is Result.Success -> {
                            alumnosDeClaseActual = resultAlumnos.data
                            infoClase = "${claseActual.nombre} - ${alumnosDeClaseActual.size} ${if(alumnosDeClaseActual.size == 1) "alumno" else "alumnos"}"
                        }
                        is Result.Error -> {
                            Timber.e(resultAlumnos.exception, "Error cargando alumnos para clase $claseActualId")
                            infoClase = "${claseActual.nombre} - Error al cargar alumnos"
                        }
                        is Result.Loading -> { /* No hacer nada */ }
                    }
                }

                _uiState.update {
                    it.copy(
                        clases = clases,
                        claseActual = claseActual,
                        alumnos = alumnosDeClaseActual,
                        claseInfo = infoClase
                    )
                }
                claseActualId // Devuelve el ID de la clase actual o null
            }
            is Result.Error -> {
                Timber.e(resultClases.exception, "Error al cargar clases para profesor $profesorId")
                _uiState.update { it.copy(error = "Error al cargar clases.", clases = emptyList(), claseActual = null, alumnos = emptyList(), claseInfo = null) }
                null
            }
            is Result.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
                null // O manejar la espera
            }
        }
    }

    /**
     * Carga la lista de alumnos que aún no tienen registro diario para hoy.
     * Utiliza el RegistroRepository para obtener los IDs de alumnos CON registro
     * Utiliza el RegistroDiarioRepository para obtener los IDs de alumnos CON registro
     * y los filtra de la lista total de alumnos de la clase.
     */
    private suspend fun cargarAlumnosPendientes(claseId: String) {
        val alumnosClase = _uiState.value.alumnos
        if (alumnosClase.isEmpty()) {
            _uiState.update { it.copy(alumnosPendientes = emptyList()) }
            return
        }

        try {
            when (val resultRegistrados = registroDiarioRepository.obtenerRegistrosPorClaseYFecha(claseId, Date())) {
                is Result.Success -> {
                    val idsRegistrados = resultRegistrados.data.map { it.alumnoId }.toSet()
                    val pendientes = alumnosClase.filter { alumno -> !idsRegistrados.contains(alumno.id) }
                    _uiState.update { it.copy(alumnosPendientes = pendientes) }
                    Timber.d("Alumnos pendientes cargados para clase $claseId: ${pendientes.size}")
                }
                is Result.Error -> {
                    Timber.e(resultRegistrados.exception, "Error obteniendo IDs de alumnos registrados hoy para clase $claseId")
                    _uiState.update { it.copy(error = "Error al comprobar registros diarios.", alumnosPendientes = emptyList()) }
                }
                is Result.Loading -> { /* No hacer nada */ }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inesperado cargando alumnos pendientes para clase $claseId")
            _uiState.update { it.copy(error = "Error al cargar alumnos pendientes.", alumnosPendientes = emptyList()) }
        }
    }

    /**
     * Determina si el día actual es festivo consultando el CalendarioRepository.
     * @return true si es festivo, false en caso contrario o si hay error.
     */
    private suspend fun esDiaFestivoHoy(): Boolean {
        return try {
            when (val result = calendarioRepository.esDiaFestivo(LocalDate.now())) {
                is Result.Success<Boolean> -> result.data
                is Result.Error -> {
                    Timber.e(result.exception, "Error al comprobar si hoy es festivo desde CalendarioRepository")
                    false
                }
                is Result.Loading<Boolean> -> {
                    Timber.d("Comprobando si es festivo...")
                    false
                }
                else -> {
                    Timber.w("Resultado inesperado al comprobar festivo: $result")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inesperado al llamar a CalendarioRepository.esDiaFestivo")
            false
        }
    }

    /**
     * Realiza el logout del usuario actual.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                Timber.d("Iniciando proceso de logout")
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                val logoutResult = authRepository.cerrarSesion()
                
                if (logoutResult) {
                    Timber.d("Logout exitoso, actualizando navigateToWelcome")
                    // Primero aseguramos que isLoading sea false
                    _uiState.update { it.copy(isLoading = false) }
                    // Luego en una actualización separada establecemos navigateToWelcome
                    _uiState.update { it.copy(navigateToWelcome = true) }
                } else {
                    Timber.e("Error en cerrarSesion(), resultado: $logoutResult")
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "No se pudo cerrar la sesión correctamente"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al realizar logout")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Error al cerrar sesión: ${e.message ?: "Error desconocido"}"
                    )
                }
                
                // Intentamos forzar la navegación aún con error
                _uiState.update { it.copy(navigateToWelcome = true) }
            }
        }
    }

    /**
     * Resetea el flag de navegación una vez que esta se ha completado.
     * Debe llamarse desde la UI después de observar navigateToWelcome = true.
     */
    fun onNavigationDone() {
        try {
            Timber.d("Reseteando flag de navegación")
            viewModelScope.launch {
                _uiState.update { it.copy(navigateToWelcome = false) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al resetear flag de navegación")
        }
    }

    /**
     * Carga los mensajes no leídos del profesor
     */
    fun cargarMensajesNoLeidos() {
        viewModelScope.launch {
            val profesorId = _uiState.value.profesor?.dni ?: run {
                Timber.w("Intentando cargar mensajes sin ID de profesor en el estado.")
                return@launch
            }

            try {
                when (val mensajesResult = usuarioRepository.getMensajesNoLeidos(profesorId)) {
                    is Result.Success -> {
                        val mensajes = mensajesResult.data
                        _uiState.update {
                            it.copy(
                                mensajesNoLeidos = mensajes,
                                totalMensajesNoLeidos = mensajes.size
                            )
                        }
                        
                        // Actualizar también el flow específico para la UI
                        _unreadMessageCount.update { mensajes.size }
                        
                        // Programar actualizaciones periódicas de este contador
                        iniciarActualizacionesPeriodicasMensajes(profesorId)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar mensajes: ${mensajesResult.exception?.message ?: "Error desconocido"}") }
                        Timber.e(mensajesResult.exception, "Error al cargar mensajes no leídos")
                    }
                    is Result.Loading -> { /* No hacer nada */ }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error inesperado al cargar mensajes: ${e.message}") }
                Timber.e(e, "Error inesperado al cargar mensajes no leídos")
            }
        }
    }
    
    /**
     * Inicia actualizaciones periódicas del contador de mensajes no leídos
     */
    private fun iniciarActualizacionesPeriodicasMensajes(profesorId: String) {
        viewModelScope.launch {
            while(true) {
                // Esperar 5 minutos antes de actualizar de nuevo
                kotlinx.coroutines.delay(5 * 60 * 1000)
                
                try {
                    when (val mensajesResult = usuarioRepository.getMensajesNoLeidos(profesorId)) {
                        is Result.Success -> {
                            val mensajes = mensajesResult.data
                            // Actualizar contador en el flow específico
                            _unreadMessageCount.update { mensajes.size }
                            
                            // También actualizar el estado general
                            _uiState.update { 
                                it.copy(
                                    mensajesNoLeidos = mensajes,
                                    totalMensajesNoLeidos = mensajes.size
                                )
                            }
                        }
                        else -> { /* No hacer nada en caso de error o loading */ }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error en actualización periódica de mensajes: ${e.message}")
                }
            }
        }
    }

    /**
     * Limpia el mensaje de error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Muestra un mensaje en el Snackbar
     * @param mensaje El mensaje a mostrar
     */
    fun showSnackbarMessage(mensaje: String) {
        _uiState.update { it.copy(error = mensaje) }
    }

    /**
     * Navega a la pantalla de registro diario de un alumno
     * NOTA: Esta función es un helper de navegación, está bien aquí o en un Navigator dedicado.
     */
    fun navegarARegistroDiario(
        navController: NavController,
        alumno: Alumno
    ) {
        navController.navigate("${AppScreens.RegistroDiarioProfesor.route}/${alumno.dni}")
    }
}