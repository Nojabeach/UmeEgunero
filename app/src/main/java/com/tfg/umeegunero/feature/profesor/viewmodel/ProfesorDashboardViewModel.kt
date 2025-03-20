package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
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

/**
 * Estado UI para la pantalla de dashboard del profesor
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
 * Gestiona la lógica de negocio y el estado de la UI para la pantalla de dashboard del profesor,
 * incluyendo la carga de datos del profesor, sus clases, alumnos, registros de actividad y mensajes.
 */
@HiltViewModel
class ProfesorDashboardViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfesorDashboardUiState())
    val uiState: StateFlow<ProfesorDashboardUiState> = _uiState.asStateFlow()

    init {
        cargarDatosProfesor()
    }

    /**
     * Carga los datos del profesor actual
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
                    is Result.Success -> {
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
                                error = "Error al cargar datos del profesor: ${profesorResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(profesorResult.exception, "Error al cargar profesor")
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
                Timber.e(e, "Error inesperado al cargar profesor")
            }
        }
    }

    /**
     * Carga las clases asignadas al profesor
     */
    private fun cargarClasesProfesor(profesorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val clasesResult = usuarioRepository.getClasesByProfesor(profesorId)

                when (clasesResult) {
                    is Result.Success -> {
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
                                error = "Error al cargar clases: ${clasesResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(clasesResult.exception, "Error al cargar clases")
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
                Timber.e(e, "Error inesperado al cargar clases")
            }
        }
    }

    /**
     * Carga los alumnos de una clase específica
     */
    fun cargarAlumnosClase(claseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val alumnosResult = usuarioRepository.getAlumnosByClase(claseId)

                when (alumnosResult) {
                    is Result.Success -> {
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
                                error = "Error al cargar alumnos: ${alumnosResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(alumnosResult.exception, "Error al cargar alumnos")
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
                Timber.e(e, "Error inesperado al cargar alumnos")
            }
        }
    }

    /**
     * Carga los registros de actividad pendientes para los alumnos
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
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                alumnosPendientes = alumnosPendientesResult.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al cargar alumnos pendientes: ${alumnosPendientesResult.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(alumnosPendientesResult.exception, "Error al cargar alumnos pendientes")
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
                Timber.e(e, "Error inesperado al cargar alumnos pendientes")
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
                                error = "Error al crear registro: ${resultadoCreacion.exception.message}",
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
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            authRepository.signOut()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    navigateToWelcome = true
                )
            }
        }
    }
}