package com.tfg.umeegunero.feature.common.mensajeria

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.local.MensajeEntity
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.UsuarioRepository
import com.tfg.umeegunero.data.service.NotificationService
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
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.FirebaseFunctionsException

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
    val error: String? = null,
    val esFamiliar: Boolean = false
)

/**
 * ViewModel para la gestión de la pantalla de chat
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val chatRepository: ChatRepository,
    private val notificacionRepository: NotificacionRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val TAG = "ChatViewModel"
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
                if (conversacionId.isNotEmpty()) {
                    marcarMensajesComoLeidos(conversacionId)
                }
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
                        val usuario = result.data as Usuario
                        val esFamiliar = usuario.perfiles.any { it.tipo == TipoUsuario.FAMILIAR }
                        
                        _uiState.update { it.copy(
                            usuario = usuario,
                            esFamiliar = esFamiliar
                        ) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = "Error al cargar el usuario: ${result.exception?.message}") }
                    }
                    is Result.Loading<*> -> {
                        // Mantener estado de carga
                    }
                    else -> {
                        // No hacer nada para otros casos
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
        if (conversacionId.isEmpty()) {
            _uiState.update { it.copy(mensajes = emptyList(), isLoading = false) }
            return
        }
        
        try {
            val mensajesEntities = chatRepository.getMensajesByConversacionId(conversacionId)
            
            // Convertir entidades a modelo Mensaje
            val mensajes = mensajesEntities.map { entity ->
                Mensaje(
                    id = entity.id,
                    emisorId = entity.emisorId,
                    receptorId = entity.receptorId,
                    timestamp = Timestamp(java.util.Date(entity.timestamp)),
                    texto = entity.texto,
                    leido = entity.leido,
                    fechaLeido = entity.fechaLeido?.let { timestamp -> Timestamp(java.util.Date(timestamp)) },
                    conversacionId = entity.conversacionId,
                    alumnoId = entity.alumnoId,
                    tipoMensaje = entity.tipoAdjunto ?: "TEXTO",
                    adjuntos = entity.adjuntos
                )
            }
            
            _uiState.update { it.copy(
                mensajes = mensajes.sortedBy { mensaje -> mensaje.timestamp },
                isLoading = false
            ) }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                error = "Error al cargar los mensajes: ${e.message}",
                isLoading = false
            ) }
            Log.e(TAG, "Error al cargar mensajes", e)
        }
    }

    /**
     * Marca los mensajes como leídos
     */
    private suspend fun marcarMensajesComoLeidos(conversacionId: String) {
        val currentUser = _uiState.value.usuario ?: return
        
        try {
            // Marcar todos los mensajes de la conversación como leídos
            chatRepository.marcarTodosComoLeidos(conversacionId, currentUser.dni)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al marcar mensajes como leídos: ${e.message}") }
            Log.e(TAG, "Error al marcar mensajes como leídos", e)
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
     * Envía un mensaje en la conversación actual
     */
    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.conversacionId.isEmpty()) return
        
        // actualizar estado
        _uiState.update { it.copy(textoMensaje = "", enviandoMensaje = true) }
        
        viewModelScope.launch {
            try {
                // Preparar mensaje
                val mensaje = MensajeEntity(
                    id = "",  // Se generará en el repositorio
                    emisorId = _uiState.value.usuario?.dni ?: "",
                    receptorId = _uiState.value.participanteId,
                    timestamp = System.currentTimeMillis(),
                    texto = text,
                    leido = false,
                    fechaLeido = null,
                    conversacionId = _uiState.value.conversacionId,
                    alumnoId = _uiState.value.alumnoId,
                    tipoAdjunto = "TEXTO",
                    adjuntos = emptyList()
                )
                
                // Enviar mensaje usando ChatRepository
                val result = chatRepository.enviarMensaje(mensaje)
                
                when (result) {
                    is Result.Success -> {
                        // Enviar notificación
                        enviarNotificacion(
                            _uiState.value.participanteId, 
                            text, 
                            _uiState.value.alumnoId
                        )
                        
                        // Recargar mensajes
                        cargarMensajes(_uiState.value.conversacionId)
                        
                        // Limpiar adjuntos
                        _uiState.update { it.copy(
                            adjuntos = emptyList(),
                            enviandoMensaje = false
                        ) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al enviar el mensaje: ${result.exception?.message}",
                            enviandoMensaje = false
                        ) }
                        Log.e(TAG, "Error al enviar mensaje", result.exception)
                    }
                    else -> {
                        _uiState.update { it.copy(enviandoMensaje = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Error al enviar el mensaje: ${e.message}",
                    enviandoMensaje = false
                ) }
                Log.e(TAG, "Error al enviar mensaje", e)
            }
        }
    }

    /**
     * Envía notificación al receptor del mensaje
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
                    participante?.nombreAlumno ?: "el alumno" 
                } ?: "el alumno"
                "Nuevo mensaje de $remitente ${usuario.nombre} sobre $alumnoNombre"
            } else {
                "Nuevo mensaje de $remitente ${usuario.nombre}"
            }
            
            // Limitar la longitud del texto para la notificación
            val mensajeCorto = if (texto.length > 100) texto.substring(0, 100) + "..." else texto
            
            // Usar el servicio de notificaciones local en vez de Cloud Functions
            notificationService.enviarNotificacionChat(
                receptorId = receptorId,
                conversacionId = _uiState.value.conversacionId ?: "",
                titulo = titulo,
                mensaje = mensajeCorto,
                remitente = usuario.nombre,
                remitenteId = usuario.dni,
                alumnoId = alumnoId ?: "",
                onCompletion = { exito, mensaje ->
                    if (!exito) {
                        Timber.e("Error al enviar notificación de chat: $mensaje")
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar notificación")
        }
    }

    /**
     * Borra el error actual
     */
    fun borrarError() {
        _uiState.update { it.copy(error = null) }
    }
} 