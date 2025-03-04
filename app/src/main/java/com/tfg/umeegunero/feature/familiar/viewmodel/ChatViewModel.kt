package com.tfg.umeegunero.feature.familiar.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
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
 * Estado UI para la pantalla de chat
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensajes: List<Mensaje> = emptyList(),
    val familiarId: String = "",
    val receptorId: String = "",
    val receptorNombre: String? = null,
    val alumnoId: String = "" // El hijo sobre el que se está hablando
)

/**
 * ViewModel para la pantalla de chat entre familiar y profesor
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Obtener el ID del profesor y del alumno de la navegación
        val profesorId = savedStateHandle.get<String>("profesorId")
        val alumnoId = savedStateHandle.get<String>("alumnoId")

        if (profesorId != null) {
            _uiState.update {
                it.copy(
                    receptorId = profesorId,
                    alumnoId = alumnoId ?: ""
                )
            }

            inicializarChat(profesorId, alumnoId)
        } else {
            _uiState.update {
                it.copy(error = "No se pudo obtener la información del chat")
            }
        }
    }

    /**
     * Inicializa el chat cargando los datos necesarios
     */
    private fun inicializarChat(profesorId: String, alumnoId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Obtener el ID del familiar actual
                val familiarId = usuarioRepository.getUsuarioActualId()

                if (familiarId.isBlank()) {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo obtener la información del usuario actual",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                _uiState.update { it.copy(familiarId = familiarId) }

                // Cargar información del profesor
                cargarInformacionProfesor(profesorId)

                // Cargar mensajes de la conversación
                cargarMensajes(familiarId, profesorId, alumnoId)

                // Iniciar escucha de mensajes en tiempo real
                iniciarEscuchaMensajes(familiarId, profesorId, alumnoId)

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
     * Carga la información del profesor
     */
    private fun cargarInformacionProfesor(profesorId: String) {
        viewModelScope.launch {
            try {
                val profesorResult = usuarioRepository.getUsuarioPorDni(profesorId)

                when (profesorResult) {
                    is Result.Success -> {
                        val profesor = profesorResult.data
                        _uiState.update {
                            it.copy(
                                receptorNombre = "${profesor.nombre} ${profesor.apellidos}"
                            )
                        }
                    }
                    is Result.Error -> {
                        Timber.e(profesorResult.exception, "Error al cargar profesor")
                    }
                    is Result.Loading -> { /* Ignorar estado Loading */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar información del profesor")
            }
        }
    }

    /**
     * Carga los mensajes de la conversación
     */
    private fun cargarMensajes(familiarId: String, profesorId: String, alumnoId: String?) {
        viewModelScope.launch {
            try {
                // Suponemos que existe un método para obtener los mensajes entre dos usuarios
                val mensajesResult = if (alumnoId != null) {
                    usuarioRepository.getMensajesByAlumno(familiarId, profesorId, alumnoId)
                } else {
                    usuarioRepository.getMensajesBetweenUsers(familiarId, profesorId)
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
                            it.receptorId == familiarId && !it.leido
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
                        error = "Error inesperado: ${e.message}",
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
    private fun iniciarEscuchaMensajes(familiarId: String, profesorId: String, alumnoId: String?) {
        viewModelScope.launch {
            try {
                // Aquí debería haber una implementación para escuchar mensajes en tiempo real
                // Por ahora, simplemente vamos a programar actualizaciones periódicas
                // Esto se reemplazaría con una verdadera implementación en tiempo real usando
                // Firestore listeners

                // Podría ser algo como:
                /*
                usuarioRepository.observeMensajes(familiarId, profesorId, alumnoId).collect { mensajes ->
                    _uiState.update {
                        it.copy(mensajes = mensajes.sortedBy { it.timestamp })
                    }

                    // Marcar mensajes como leídos
                    marcarMensajesComoLeidos(mensajes.filter {
                        it.receptorId == familiarId && !it.leido
                    })
                }
                */
            } catch (e: Exception) {
                Timber.e(e, "Error al iniciar escucha de mensajes")
            }
        }
    }

    /**
     * Marca mensajes como leídos
     */
    private fun marcarMensajesComoLeidos(mensajes: List<Mensaje>) {
        if (mensajes.isEmpty()) return

        viewModelScope.launch {
            try {
                for (mensaje in mensajes) {
                    usuarioRepository.marcarMensajeComoLeido(mensaje.id)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar mensajes como leídos")
            }
        }
    }

    /**
     * Envía un mensaje al profesor
     */
    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return

        viewModelScope.launch {
            val state = _uiState.value

            if (state.familiarId.isBlank() || state.receptorId.isBlank()) {
                _uiState.update {
                    it.copy(error = "No se pudo enviar el mensaje: información de chat incompleta")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            try {
                val mensaje = Mensaje(
                    id = "", // Se generará al guardar
                    emisorId = state.familiarId,
                    receptorId = state.receptorId,
                    alumnoId = state.alumnoId,
                    texto = texto,
                    timestamp = Timestamp.now(),
                    leido = false
                )

                val result = usuarioRepository.enviarMensaje(mensaje)

                when (result) {
                    is Result.Success -> {
                        // Actualizar lista de mensajes localmente
                        val nuevoMensaje = mensaje.copy(id = result.data)
                        _uiState.update {
                            it.copy(
                                mensajes = it.mensajes + nuevoMensaje,
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
                        error = "Error inesperado: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Error inesperado al enviar mensaje")
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