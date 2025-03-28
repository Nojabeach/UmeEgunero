package com.tfg.umeegunero.feature.profesor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.Result
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
 * Estado UI para la pantalla de chat
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajes: List<Mensaje> = emptyList(),
    val profesorId: String = "",
    val receptorId: String = "",
    val receptorNombre: String? = null,
    val alumnoId: String = "" // El alumno sobre el que se está hablando
)

/**
 * ViewModel para la pantalla de chat entre profesor y familiar
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Obtener el ID del familiar y del alumno de la navegación
        val familiarId = savedStateHandle.get<String>("familiarId")
        val alumnoId = savedStateHandle.get<String>("alumnoId")

        if (familiarId != null) {
            _uiState.update {
                it.copy(
                    receptorId = familiarId,
                    alumnoId = alumnoId ?: ""
                )
            }

            inicializarChat(familiarId, alumnoId)
        } else {
            _uiState.update {
                it.copy(error = "No se pudo obtener la información del chat")
            }
        }
    }

    /**
     * Inicializa el chat cargando los datos necesarios
     */
    private fun inicializarChat(familiarId: String, alumnoId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Obtener el ID del profesor actual
                val profesorId = usuarioRepository.getUsuarioActualId()

                if (profesorId.isBlank()) {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo obtener la información del usuario actual",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                _uiState.update { it.copy(profesorId = profesorId) }

                // Cargar información del familiar
                cargarInformacionFamiliar(familiarId)

                // Cargar mensajes de la conversación
                cargarMensajes(profesorId, familiarId, alumnoId)

                // Iniciar escucha de mensajes en tiempo real
                iniciarEscuchaMensajes(profesorId, familiarId, alumnoId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al inicializar chat")
            }
        }
    }

    /**
     * Carga la información del familiar
     */
    private fun cargarInformacionFamiliar(familiarId: String) {
        viewModelScope.launch {
            try {
                val familiarResult = usuarioRepository.getUsuarioPorDni(familiarId)

                when (familiarResult) {
                    is Result.Success -> {
                        val familiar = familiarResult.data
                        _uiState.update {
                            it.copy(
                                receptorNombre = "${familiar.nombre} ${familiar.apellidos}"
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(familiarResult.exception, "Error al cargar familiar")
                    }
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar información del familiar")
            }
        }
    }

    /**
     * Carga los mensajes de la conversación
     */
    private fun cargarMensajes(profesorId: String, familiarId: String, alumnoId: String?) {
        viewModelScope.launch {
            try {
                // Obtener los mensajes basados en el alumno o directamente entre usuarios
                val mensajesResult = if (alumnoId != null) {
                    usuarioRepository.getMensajesByAlumno(profesorId, familiarId, alumnoId)
                } else {
                    usuarioRepository.getMensajesBetweenUsers(profesorId, familiarId)
                }

                when (mensajesResult) {
                    is Result.Success -> {
                        val mensajes = mensajesResult.data.sortedBy { it.timestamp }
                        _uiState.update {
                            it.copy(
                                mensajes = mensajes,
                                isLoading = false
                            )
                        }

                        // Marcar mensajes como leídos
                        marcarMensajesComoLeidos(mensajes.filter {
                            it.receptorId == profesorId && !it.leido
                        })
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
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al cargar mensajes: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al cargar mensajes")
            }
        }
    }

    /**
     * Inicia la escucha de mensajes en tiempo real
     */
    private fun iniciarEscuchaMensajes(profesorId: String, familiarId: String, alumnoId: String?) {
        viewModelScope.launch {
            try {
                usuarioRepository.escucharMensajes(profesorId, familiarId, alumnoId) { mensaje ->
                    val mensajesList = _uiState.value.mensajes.toMutableList()
                    mensajesList.add(mensaje)
                    
                    _uiState.update { currentState ->
                        currentState.copy(
                            mensajes = mensajesList
                        )
                    }

                    // Marcar el mensaje como leído si el profesor es el receptor
                    if (mensaje.receptorId == profesorId && !mensaje.leido) {
                        marcarMensajesComoLeidos(listOf(mensaje))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al iniciar escucha de mensajes")
            }
        }
    }

    /**
     * Marca los mensajes como leídos
     */
    private fun marcarMensajesComoLeidos(mensajes: List<Mensaje>) {
        viewModelScope.launch {
            try {
                mensajes.forEach { mensaje ->
                    usuarioRepository.marcarMensajeComoLeido(mensaje.id)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensajes como leídos")
            }
        }
    }

    /**
     * Envía un mensaje al familiar
     */
    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return

        viewModelScope.launch {
            val state = _uiState.value

            if (state.profesorId.isBlank() || state.receptorId.isBlank()) {
                _uiState.update {
                    it.copy(error = "No se pudo enviar el mensaje: información de chat incompleta")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                val mensaje = Mensaje(
                    id = "", // Se generará al guardar
                    emisorId = state.profesorId,
                    receptorId = state.receptorId,
                    alumnoId = state.alumnoId,
                    texto = texto,
                    timestamp = Timestamp.now(),
                    leido = false
                )

                val result = usuarioRepository.enviarMensaje(mensaje)

                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                mensajes = it.mensajes + mensaje,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                error = "Error al enviar mensaje: ${result.exception.message}",
                                isLoading = false
                            )
                        }
                        Timber.e(result.exception, "Error al enviar mensaje")
                    }
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error inesperado al enviar mensaje: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al enviar mensaje")
            }
        }
    }
} 