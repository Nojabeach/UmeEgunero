package com.tfg.umeegunero.feature.common.mensajeria

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.local.MensajeEntity
import com.tfg.umeegunero.data.repository.ChatRepository
import com.tfg.umeegunero.data.repository.NotificacionRepository
import com.tfg.umeegunero.data.repository.UnifiedMessageRepository
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
import java.util.UUID

/**
 * Estado de la UI para la pantalla de chat
 */
data class ChatUiState(
    val isLoading: Boolean = true,
    val usuario: Usuario? = null,
    val participante: Usuario? = null,
    val participanteId: String = "",
    val alumnoId: String? = null,
    val mensajes: List<UnifiedMessage> = emptyList(),
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
    private val unifiedMessageRepository: UnifiedMessageRepository,
    private val notificacionRepository: NotificacionRepository,
    private val notificationService: NotificationService,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val TAG = "ChatViewModel"
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // Listener para mensajes en tiempo real
    private var messagesListener: ListenerRegistration? = null

    /**
     * Inicializa el ViewModel con los datos necesarios
     */
    fun inicializar(conversacionId: String, participanteId: String, alumnoId: String? = null) {
        Timber.d("Inicializando ChatViewModel - conversacionId: '$conversacionId', participanteId: '$participanteId', alumnoId: '$alumnoId'")
        
        _uiState.update { it.copy(
            isLoading = true,
            conversacionId = conversacionId,
            participanteId = participanteId,
            alumnoId = alumnoId,
            error = null
        ) }
        
        viewModelScope.launch {
            try {
                // Cargar usuario actual
                cargarUsuarioActual()
                
                // Si el participanteId es temporal o vacío, intentar obtenerlo de la conversación
                val realParticipanteId = if (participanteId.isEmpty() || participanteId == "loading") {
                    Timber.d("ParticipanteId temporal o vacío, intentando obtener de la conversación")
                    obtenerParticipanteDeConversacion(conversacionId)
                } else {
                    participanteId
                }
                
                if (realParticipanteId.isEmpty()) {
                    throw Exception("No se pudo determinar el participante de la conversación")
                }
                
                // Actualizar el participanteId real
                _uiState.update { it.copy(participanteId = realParticipanteId) }
                
                // Cargar participante
                cargarParticipante(realParticipanteId)
                
                // Cargar mensajes
                cargarMensajesUnificados(conversacionId)
                
                // Ya no necesitamos marcar mensajes aquí porque se hace automáticamente en el listener
            } catch (e: Exception) {
                Timber.e(e, "Error al inicializar chat: ${e.message}")
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
     * Carga los mensajes de la conversación desde el repositorio unificado
     */
    private fun cargarMensajesUnificados(conversacionId: String) {
        if (conversacionId.isEmpty()) {
            _uiState.update { it.copy(mensajes = emptyList(), isLoading = false) }
            return
        }
        
        // Cancelar listener anterior si existe
        messagesListener?.remove()
        
        try {
            Timber.d("Configurando listener para conversación: $conversacionId en collection unified_messages")
            
            // Configurar listener en tiempo real para los mensajes
            messagesListener = firestore.collection("unified_messages")
                .whereEqualTo("conversationId", conversacionId)
                .whereEqualTo("type", MessageType.CHAT.name)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error al escuchar mensajes: ${error.message}")
                        
                        // Si es un error de permisos o red, intentar reconectar después de un tiempo
                        if (error.message?.contains("permission", ignoreCase = true) == true ||
                            error.message?.contains("network", ignoreCase = true) == true ||
                            error.message?.contains("unavailable", ignoreCase = true) == true) {
                            
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(5000) // Esperar 5 segundos
                                if (_uiState.value.conversacionId == conversacionId) {
                                    Timber.d("Intentando reconectar al chat...")
                                    cargarMensajesUnificados(conversacionId)
                                }
                            }
                        }
                        
                        _uiState.update { it.copy(
                            error = "Error al cargar mensajes: ${error.message}",
                            isLoading = false
                        ) }
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        Timber.d("Snapshot recibido para conversación $conversacionId. Documentos: ${snapshot.documents.size}")
                        
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                Timber.d("Procesando mensaje ${doc.id}: $data")
                                UnifiedMessage.fromMap(doc.id, data)
                            } catch (e: Exception) {
                                Timber.e(e, "Error al parsear mensaje: ${doc.id}")
                                null
                            }
                        }
                        
                        _uiState.update { it.copy(
                            mensajes = messages.sortedBy { mensaje -> mensaje.timestamp },
                            isLoading = false,
                            error = null // Limpiar error si la conexión se restableció
                        ) }
                        
                        // Marcar mensajes nuevos como leídos
                        viewModelScope.launch {
                            marcarMensajesNuevosComoLeidos(messages)
                        }
                        
                        Timber.d("Mensajes actualizados: ${messages.size} mensajes en la conversación")
                    } else {
                        Timber.d("Snapshot nulo recibido para conversación $conversacionId")
                    }
                }
                
            Timber.d("Listener de mensajes configurado para conversación: $conversacionId")
        } catch (e: Exception) {
            _uiState.update { it.copy(
                error = "Error al configurar listener de mensajes: ${e.message}",
                isLoading = false
            ) }
            Timber.e(e, "Error al configurar listener de mensajes")
        }
    }
    
    /**
     * Marca solo los mensajes nuevos como leídos
     */
    private suspend fun marcarMensajesNuevosComoLeidos(messages: List<UnifiedMessage>) {
        val currentUser = _uiState.value.usuario ?: return
        
        try {
            // Filtrar solo mensajes no leídos que no son del usuario actual
            val mensajesNoLeidos = messages.filter { 
                it.status == MessageStatus.UNREAD && it.senderId != currentUser.dni 
            }
            
            mensajesNoLeidos.forEach { mensaje ->
                unifiedMessageRepository.markAsRead(mensaje.id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensajes como leídos: ${e.message}")
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
     * Envía un mensaje en la conversación actual usando el sistema unificado
     */
    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.conversacionId.isEmpty()) return
        
        // actualizar estado
        _uiState.update { it.copy(textoMensaje = "", enviandoMensaje = true) }
        
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.usuario ?: throw Exception("Usuario no disponible")
                
                // Preparar mensaje unificado
                val unifiedMessage = UnifiedMessage(
                    id = UUID.randomUUID().toString(),
                    title = "",  // Los chats no necesitan título
                    content = text,
                    senderId = currentUser.dni,
                    senderName = currentUser.nombre + " " + currentUser.apellidos,
                    receiverId = _uiState.value.participanteId,
                    receiversIds = emptyList(),  // Es un mensaje directo, no grupal
                    timestamp = Timestamp.now(),
                    type = MessageType.CHAT,
                    priority = MessagePriority.NORMAL,
                    status = MessageStatus.UNREAD,
                    conversationId = _uiState.value.conversacionId,
                    metadata = if (_uiState.value.alumnoId != null) 
                        mapOf("alumnoId" to _uiState.value.alumnoId!!) 
                    else 
                        emptyMap(),
                    attachments = emptyList()  // Manejo de adjuntos pendiente
                )
                
                // Enviar mensaje usando el repositorio unificado
                val result = unifiedMessageRepository.sendMessage(unifiedMessage)
                
                when (result) {
                    is Result.Success -> {
                        // No necesitamos recargar mensajes porque el listener lo hará automáticamente
                        
                        // Limpiar adjuntos
                        _uiState.update { it.copy(
                            adjuntos = emptyList(),
                            enviandoMensaje = false
                        ) }
                        
                        // La notificación ahora se envía automáticamente desde la Cloud Function
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(
                            error = "Error al enviar el mensaje: ${result.message}",
                            enviandoMensaje = false
                        ) }
                        Timber.e("Error al enviar mensaje: ${result.message}")
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
                Timber.e(e, "Error al enviar mensaje")
            }
        }
    }

    /**
     * Borra el error actual
     */
    fun borrarError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Reconecta el listener de mensajes manualmente
     */
    fun reconectarChat() {
        val conversacionId = _uiState.value.conversacionId
        if (conversacionId.isNotEmpty()) {
            Timber.d("Reconectando chat manualmente...")
            _uiState.update { it.copy(error = null, isLoading = true) }
            cargarMensajesUnificados(conversacionId)
        }
    }

    /**
     * Intenta obtener el ID del participante desde la conversación
     */
    private suspend fun obtenerParticipanteDeConversacion(conversacionId: String): String {
        return try {
            val currentUser = _uiState.value.usuario
            val currentUserId = currentUser?.dni ?: ""
            
            // Obtener participantes de la conversación
            val result = unifiedMessageRepository.getConversationParticipants(conversacionId)
            
            when (result) {
                is Result.Success -> {
                    val participants = result.data
                    Timber.d("Participantes encontrados en conversación: $participants")
                    
                    // Filtrar el usuario actual para obtener el otro participante
                    val otherParticipant = participants.firstOrNull { it != currentUserId }
                    
                    if (otherParticipant != null) {
                        Timber.d("Participante encontrado: $otherParticipant")
                        otherParticipant
                    } else {
                        // Si no hay otro participante, usar el primer participante disponible
                        participants.firstOrNull() ?: ""
                    }
                }
                is Result.Error -> {
                    Timber.e("Error al obtener participantes: ${result.message}")
                    ""
                }
                else -> ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener participante de conversación")
            ""
        }
    }

    /**
     * Limpia los recursos cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
        // Cancelar el listener de mensajes
        messagesListener?.remove()
        messagesListener = null
        Timber.d("ChatViewModel: Listener de mensajes cancelado")
    }
} 