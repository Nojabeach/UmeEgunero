package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.Result
import com.tfg.umeegunero.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado UI para la pantalla de dashboard del familiar
 */
data class FamiliarDashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val familiar: Usuario? = null,
    val hijos: List<Alumno> = emptyList(),
    val hijoSeleccionado: Alumno? = null,
    val registrosActividad: List<RegistroActividad> = emptyList(),
    val registrosSinLeer: Int = 0,
    val mensajesNoLeidos: List<Mensaje> = emptyList(),
    val totalMensajesNoLeidos: Int = 0,
    val profesores: Map<String, Usuario> = emptyMap(), // Mapeo de profesorId -> Profesor
    val selectedTab: Int = 0
)

/**
 * ViewModel para la pantalla de dashboard del familiar
 */
@HiltViewModel
class FamiliarDashboardViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamiliarDashboardUiState())
    val uiState: StateFlow<FamiliarDashboardUiState> = _uiState.asStateFlow()

    init {
        cargarDatosFamiliar()
    }

    /**
     * Carga los datos del familiar actual
     */
    fun cargarDatosFamiliar() {
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

                // Cargamos los datos del familiar
                val familiarResult = usuarioRepository.getUsuarioPorDni(userId)

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
     */
    private fun cargarHijos(alumnosIds: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val hijos = mutableListOf<Alumno>()
                val profesoresIds = mutableSetOf<String>()

                // Cargamos cada hijo
                for (alumnoId in alumnosIds) {
                    // Aquí se debería llamar a un método para obtener los datos del alumno
                    // Por ahora, suponemos que existe un método getAlumnoPorDni
                    val alumnoResult = usuarioRepository.getAlumnoPorDni(alumnoId)

                    when (alumnoResult) {
                        is Result.Success -> {
                            val alumno = alumnoResult.data
                            hijos.add(alumno)

                            // Recopilamos IDs de profesores para cargarlos después
                            alumno.profesorIds?.let { profesoresIds.addAll(it) }
                        }
                        is Result.Error -> {
                            Timber.e(alumnoResult.exception, "Error al cargar hijo con ID: $alumnoId")
                        }
                        else -> { /* Ignorar estado Loading */ }
                    }
                }

                _uiState.update {
                    it.copy(
                        hijos = hijos,
                        hijoSeleccionado = hijos.firstOrNull(),
                        isLoading = false
                    )
                }

                // Cargamos los profesores asociados
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
     */
    private fun cargarProfesores(profesoresIds: List<String>) {
        viewModelScope.launch {
            try {
                val profesores = mutableMapOf<String, Usuario>()

                for (profesorId in profesoresIds) {
                    val profesorResult = usuarioRepository.getUsuarioPorDni(profesorId)

                    if (profesorResult is Result.Success) {
                        profesores[profesorId] = profesorResult.data
                    }
                }

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
     */
    fun cargarRegistrosActividad(alumnoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Suponemos que existe un método para obtener los registros de actividad del alumno
                val registrosResult = usuarioRepository.getRegistrosActividadByAlumno(alumnoId)

                when (registrosResult) {
                    is Result.Success -> {
                        val registros = registrosResult.data

                        // Contamos cuántos registros no han sido vistos por el familiar
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
     */
    fun cargarMensajesNoLeidos() {
        viewModelScope.launch {
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
     */
    fun marcarRegistroComoVisto(registroId: String) {
        viewModelScope.launch {
            try {
                // Suponemos que existe un método para marcar un registro como visto
                val result = usuarioRepository.marcarRegistroComoVistoPorFamiliar(registroId)

                if (result is Result.Success) {
                    // Actualizamos los registros localmente
                    val registrosActualizados = _uiState.value.registrosActividad.map { registro ->
                        if (registro.id == registroId) {
                            registro.copy(vistoPorFamiliar = true, fechaVisto = Timestamp.now())
                        } else {
                            registro
                        }
                    }

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
     * Envía un mensaje a un profesor
     */
    fun enviarMensaje(profesorId: String, texto: String, alumnoId: String) {
        viewModelScope.launch {
            val familiarId = _uiState.value.familiar?.documentId ?: return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val mensaje = Mensaje(
                    emisorId = familiarId,
                    receptorId = profesorId,
                    texto = texto,
                    timestamp = Timestamp.now(),
                    alumnoId = alumnoId
                )

                // Suponemos que existe un método para enviar un mensaje
                val result = usuarioRepository.enviarMensaje(mensaje)

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
     */
    fun seleccionarHijo(alumnoId: String) {
        val hijo = _uiState.value.hijos.find { it.dni == alumnoId }

        hijo?.let {
            _uiState.update { state -> state.copy(hijoSeleccionado = it) }
            cargarRegistrosActividad(alumnoId)
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
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}