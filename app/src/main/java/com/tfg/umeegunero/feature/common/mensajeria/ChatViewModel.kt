package com.tfg.umeegunero.feature.common.mensajeria

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.repository.MensajeRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de chat
 */
data class ChatUiState(
    val isLoading: Boolean = true,
    val usuario: Usuario? = null,
    val participante: Usuario? = null,
    val participanteId: String = "",
    val alumnoId: String? = null,
    val mensajes: List<Mensaje> = emptyList(),
    val conversacionId: String = "",
    val textoMensaje: String = "",
    val adjuntos: List<Uri> = emptyList(),
    val enviandoMensaje: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la gestión de la pantalla de chat
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val mensajeRepository: MensajeRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Inicializa el ViewModel con los datos necesarios
     */
    fun inicializar(conversacionId: String, participanteId: String, alumnoId: String? = null) {
        _uiState.update { it.copy(
            isLoading = true,
            conversacionId = conversacionId,
            participanteId = participanteId,
            alumnoId = alumnoId
        ) }
        
        viewModelScope.launch {
            try {
                // Cargar usuario actual
                cargarUsuarioActual()
                
                // Cargar participante
                cargarParticipante(participanteId)
                
                // Cargar mensajes
                cargarMensajes(conversacionId)
                
                // Marcar mensajes como leídos
                marcarMensajesComoLeidos(conversacionId)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al cargar la conversación: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Carga el usuario actual
     */
    private suspend fun cargarUsuarioActual() {
        try {
            usuarioRepository.getUsuarioActual().collectLatest<Result<Usuario>> { result ->
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.update { it.copy(usuario = result.data as Usuario) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar el usuario: ${result.exception?.message}") }
                    }
                    is Result.Loading<*> -> {
                        // Mantener estado de carga
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al cargar el usuario: ${e.message}") }
        }
    }

    /**
     * Carga los datos del participante en la conversación
     */
    private suspend fun cargarParticipante(participanteId: String) {
        try {
            val result = usuarioRepository.obtenerUsuarioPorId(participanteId)
            when (result) {
                is Result.Success<*> -> {
                    _uiState.update { it.copy(participante = result.data as Usuario) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = "Error al cargar el participante: ${result.exception?.message}") }
                }
                else -> {}
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al cargar el participante: ${e.message}") }
        }
    }

    /**
     * Carga los mensajes de la conversación
     */
    private suspend fun cargarMensajes(conversacionId: String) {
        try {
            mensajeRepository.obtenerMensajes(conversacionId).collectLatest { mensajes ->
                _uiState.update { it.copy(
                    mensajes = mensajes.sortedBy { mensaje -> mensaje.timestamp },
                    isLoading = false
                ) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                error = "Error al cargar los mensajes: ${e.message}",
                isLoading = false
            ) }
        }
    }

    /**
     * Marca los mensajes como leídos
     */
    private suspend fun marcarMensajesComoLeidos(conversacionId: String) {
        val currentUser = _uiState.value.usuario ?: return
        
        try {
            _uiState.value.mensajes
                .filter { !it.leido && it.receptorId == currentUser.dni }
                .forEach { mensaje ->
                    mensajeRepository.marcarMensajeComoLeido(mensaje.id)
                }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al marcar mensajes como leídos: ${e.message}") }
        }
    }

    /**
     * Actualiza el texto del mensaje
     */
    fun actualizarTextoMensaje(texto: String) {
        _uiState.update { it.copy(textoMensaje = texto) }
    }

    /**
     * Añade un adjunto al mensaje
     */
    fun añadirAdjunto(uri: Uri) {
        _uiState.update { it.copy(adjuntos = it.adjuntos + uri) }
    }

    /**
     * Elimina un adjunto del mensaje
     */
    fun eliminarAdjunto(uri: Uri) {
        _uiState.update { it.copy(adjuntos = it.adjuntos.filter { adjunto -> adjunto != uri }) }
    }

    /**
     * Envía un mensaje
     */
    fun enviarMensaje() {
        val currentState = _uiState.value
        val texto = currentState.textoMensaje.trim()
        
        if (texto.isEmpty() && currentState.adjuntos.isEmpty()) {
            return
        }
        
        val usuario = currentState.usuario ?: return
        val conversacionId = currentState.conversacionId
        val receptorId = currentState.participanteId
        val alumnoId = currentState.alumnoId
        
        _uiState.update { it.copy(enviandoMensaje = true) }
        
        viewModelScope.launch {
            try {
                // Enviar mensaje
                val mensaje = Mensaje(
                    id = "",
                    emisorId = usuario.dni,
                    receptorId = receptorId,
                    timestamp = Timestamp.now(),
                    texto = texto,
                    leido = false,
                    fechaLeido = null,
                    alumnoId = alumnoId,
                    adjuntos = emptyList() // Los adjuntos se subirán después
                )
                
                val mensajeId = mensajeRepository.enviarMensaje(conversacionId, mensaje)
                
                // Subir adjuntos si hay
                if (currentState.adjuntos.isNotEmpty()) {
                    val adjuntosUrls = mensajeRepository.subirAdjuntos(
                        mensajeId,
                        currentState.adjuntos
                    )
                    
                    // Actualizar el mensaje con las URLs de los adjuntos
                    mensajeRepository.actualizarAdjuntosMensaje(mensajeId, adjuntosUrls)
                }
                
                // Enviar notificación
                enviarNotificacion(receptorId, texto, alumnoId)
                
                // Limpiar el estado
                _uiState.update { it.copy(
                    textoMensaje = "",
                    adjuntos = emptyList(),
                    enviandoMensaje = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al enviar el mensaje: ${e.message}",
                    enviandoMensaje = false
                ) }
            }
        }
    }

    /**
     * Envía una notificación al receptor del mensaje
     */
    private suspend fun enviarNotificacion(receptorId: String, texto: String, alumnoId: String?) {
        try {
            val usuario = _uiState.value.usuario ?: return
            val participante = _uiState.value.participante
            val tipoUsuario = usuario.perfiles.firstOrNull()?.tipo ?: TipoUsuario.DESCONOCIDO
            val remitente = if (tipoUsuario == TipoUsuario.PROFESOR) "el profesor" else "la familia"
            
            // Construir título según el contexto
            val titulo = if (alumnoId != null) {
                val alumnoNombre = _uiState.value.alumnoId?.let { 
                    // Si tenemos el estado del alumno en el ViewModel, usar ese nombre
                    "sobre ${participante?.nombre ?: "el alumno"}"
                } ?: ""
                "Nuevo mensaje de $remitente ${usuario.nombre} $alumnoNombre"
            } else {
                "Nuevo mensaje de $remitente ${usuario.nombre}"
            }
            
            // Crear una nueva notificación con los datos requeridos
            val notificacion = com.tfg.umeegunero.data.model.Notificacion(
                titulo = titulo,
                mensaje = texto.take(100) + if (texto.length > 100) "..." else "",
                usuarioDestinatarioId = receptorId,
                leida = false,
                fecha = Timestamp.now(),
                tipo = TipoNotificacion.MENSAJE,
                remitente = usuario.nombre,
                remitenteId = usuario.dni,
                accion = "chat/${_uiState.value.conversacionId}",
                metadata = mapOf(
                    "conversacionId" to _uiState.value.conversacionId,
                    "alumnoId" to (alumnoId ?: "")
                )
            )
            
            // Enviar la notificación
            val resultado = notificacionRepository.crearNotificacion(notificacion)
            
            when (resultado) {
                is Result.Success<*> -> {
                    Timber.d("Notificación enviada al usuario $receptorId: ${notificacion.titulo}")
                }
                is Result.Error -> {
                    Timber.e("Error al enviar notificación: ${resultado.exception?.message}")
                }
                else -> {}
            }
        } catch (e: Exception) {
            // Error al enviar notificación no impide el envío del mensaje
            _uiState.update { it.copy(error = "Error al enviar la notificación: ${e.message}") }
            Timber.e("Error al enviar notificación: ${e.message}")
        }
    }

    /**
     * Borra el error actual
     */
    fun borrarError() {
        _uiState.update { it.copy(error = null) }
    }
} 