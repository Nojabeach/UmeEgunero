package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Conversation
import com.tfg.umeegunero.data.model.MessageAction
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.ParticipantDetail
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Repositorio unificado para gestionar todas las comunicaciones en la aplicación
 * 
 * Este repositorio centraliza la lógica de todos los tipos de mensajes: notificaciones,
 * chats, comunicados, incidencias, etc. en una interfaz común para simplificar
 * la interacción con los diferentes sistemas de comunicación.
 */
@Singleton
class UnifiedMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepositoryProvider: Provider<AuthRepository>,
    private val usuarioRepository: UsuarioRepository
) {
    companion object {
        private const val MESSAGES_COLLECTION = "unified_messages"
        private const val CONVERSATIONS_COLLECTION = "conversations"
        private const val USERS_COLLECTION = "usuarios"
    }
    
    // Usar get() para obtener la instancia del Provider cuando sea necesario
    private val authRepository: AuthRepository
        get() = authRepositoryProvider.get()
    
    /**
     * Envía un mensaje unificado
     */
    suspend fun sendMessage(message: UnifiedMessage): Result<String> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val messageWithId = message.copy(id = messageId)
            
            // Guardar el mensaje en Firestore
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .set(messageWithId)
                .await()
            
            Result.Success(messageId)
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar mensaje unificado")
            Result.Error(e)
        }
    }
    
    /**
     * Marca un mensaje como leído
     */
    suspend fun markAsRead(messageId: String): Result<Boolean> {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                return Result.Error("Usuario no autenticado")
            }
            
            // Primero obtenemos el mensaje para saber su tipo
            val messageDoc = firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .get()
                .await()
                
            if (!messageDoc.exists()) {
                return Result.Error("Mensaje no encontrado")
            }
            
            val messageData = messageDoc.data ?: return Result.Error("Datos del mensaje no válidos")
            val messageType = MessageType.valueOf(messageData["type"] as? String ?: MessageType.CHAT.name)
            val relatedEntityId = messageData["relatedEntityId"] as? String
            
            // Actualizar en la colección unificada
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update(
                    mapOf(
                        "status" to MessageStatus.READ.name,
                        "readTimestamp" to Timestamp.now(),
                        "readBy.${currentUser.dni}" to Timestamp.now()
                    )
                )
                .await()
            
            // Actualizar también en la colección original según el tipo
            when (messageType) {
                MessageType.CHAT -> {
                    // Para mensajes de chat, actualizar en la colección de mensajes
                    if (!relatedEntityId.isNullOrEmpty()) {
                        try {
                            firestore.collection("mensajes")
                                .document(relatedEntityId)
                                .update(
                                    mapOf(
                                        "leido" to true,
                                        "fechaLectura" to Timestamp.now()
                                    )
                                )
                                .await()
                        } catch (e: Exception) {
                            Timber.w(e, "No se pudo actualizar el mensaje original en 'mensajes'")
                        }
                    }
                }
                MessageType.ANNOUNCEMENT -> {
                    // Para comunicados, actualizar en la colección de comunicados
                    if (!relatedEntityId.isNullOrEmpty()) {
                        try {
                            firestore.collection("comunicados")
                                .document(relatedEntityId)
                                .update(
                                    mapOf(
                                        "lecturasPor.${currentUser.dni}" to Timestamp.now()
                                    )
                                )
                                .await()
                        } catch (e: Exception) {
                            Timber.w(e, "No se pudo actualizar el comunicado original")
                        }
                    }
                }
                MessageType.NOTIFICATION -> {
                    // Para notificaciones, actualizar si es necesario
                    if (!relatedEntityId.isNullOrEmpty()) {
                        try {
                            firestore.collection("notificaciones")
                                .document(relatedEntityId)
                                .update(
                                    mapOf(
                                        "leida" to true,
                                        "fechaLectura" to Timestamp.now()
                                    )
                                )
                                .await()
                        } catch (e: Exception) {
                            Timber.w(e, "No se pudo actualizar la notificación original")
                        }
                    }
                }
                MessageType.DAILY_RECORD -> {
                    // Para registros de actividad diaria, marcar como leído por el familiar
                    if (!relatedEntityId.isNullOrEmpty()) {
                        try {
                            firestore.collection("registrosActividad")
                                .document(relatedEntityId)
                                .update(
                                    mapOf(
                                        "lecturasPorFamiliar.${currentUser.dni}" to mapOf(
                                            "familiarId" to currentUser.dni,
                                            "fechaLectura" to Timestamp.now()
                                        )
                                    )
                                )
                                .await()
                        } catch (e: Exception) {
                            Timber.w(e, "No se pudo actualizar el registro de actividad original")
                        }
                    }
                }
                MessageType.GROUP_CHAT,
                MessageType.TASK,
                MessageType.EVENT,
                MessageType.SYSTEM,
                MessageType.INCIDENT,
                MessageType.ATTENDANCE -> {
                    // Para estos tipos, no se requiere actualización adicional por ahora
                    Timber.d("Tipo de mensaje $messageType marcado como leído, sin actualización adicional requerida")
                }
            }
            
            Timber.d("Mensaje $messageId marcado como leído exitosamente")
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensaje como leído: $messageId")
            Result.Error(e.message ?: "Error al marcar como leído")
        }
    }
    
    /**
     * Obtiene un mensaje por su ID
     */
    suspend fun getMessageById(messageId: String): Result<UnifiedMessage> {
        return try {
            val docSnapshot = firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .get()
                .await()
            
            if (docSnapshot.exists()) {
                val message = UnifiedMessage.fromMap(docSnapshot.id, docSnapshot.data ?: emptyMap())
                Result.Success(message)
            } else {
                Result.Error("Mensaje no encontrado")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensaje: $messageId")
            Result.Error(e.message ?: "Error al obtener mensaje")
        }
    }
    
    /**
     * Obtiene todos los mensajes de un usuario
     */
    fun getMessagesForUser(userId: String): Flow<Result<List<UnifiedMessage>>> = flow {
        try {
            emit(Result.Loading())
            
            val receiverQuery = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                
            val receiversQuery = firestore.collection(MESSAGES_COLLECTION)
                .whereArrayContains("receiversIds", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
            
            val receiverMessages = receiverQuery.get().await().documents
                .mapNotNull { createMessageFromDocument(it) }
                
            val receiversMessages = receiversQuery.get().await().documents
                .mapNotNull { createMessageFromDocument(it) }
                
            // Combinar y ordenar por timestamp
            val allMessages = (receiverMessages + receiversMessages)
                .sortedByDescending { it.timestamp.seconds }
                
            emit(Result.Success(allMessages))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensajes para usuario: $userId")
            emit(Result.Error(e.message ?: "Error al obtener mensajes"))
        }
    }
    
    /**
     * Obtiene todos los mensajes del usuario actual
     */
    fun getCurrentUserInbox(): Flow<Result<List<UnifiedMessage>>> = flow {
        try {
            emit(Result.Loading())
            
            val currentUser = authRepository.getCurrentUser() ?: run {
                Timber.w("Usuario no autenticado al intentar obtener mensajes")
                emit(Result.Error("Usuario no autenticado"))
                return@flow
            }
            
            val userId = currentUser.dni
            Timber.d("Obteniendo mensajes para usuario: $userId")
            
            try {
                // 1. Obtener mensajes unificados
                val combinedMessages = mutableListOf<UnifiedMessage>()
                
                // Consulta para mensajes donde el usuario es destinatario directo
                val receiverQuery = firestore.collection(MESSAGES_COLLECTION)
                    .whereEqualTo("receiverId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    
                val directMessages = receiverQuery.get().await().documents
                    .mapNotNull { createMessageFromDocument(it) }
                
                Timber.d("Mensajes directos encontrados: ${directMessages.size}")
                combinedMessages.addAll(directMessages)
                
                // Consulta para mensajes donde el usuario está en la lista de destinatarios
                try {
                    val receiversQuery = firestore.collection(MESSAGES_COLLECTION)
                        .whereArrayContains("receiversIds", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                    
                    val listMessages = receiversQuery.get().await().documents
                        .mapNotNull { createMessageFromDocument(it) }
                    
                    Timber.d("Mensajes en lista de destinatarios encontrados: ${listMessages.size}")
                    combinedMessages.addAll(listMessages)
                } catch (e: Exception) {
                    // Si falla por falta de índice, lo registramos pero continuamos con los mensajes directos
                    if (e.message?.contains("FAILED_PRECONDITION") == true || 
                        e.message?.contains("requires an index") == true) {
                        Timber.e(e, "Error al obtener mensajes combinados (falta índice en Firestore)")
                        Timber.d("Fallback: Recuperados ${directMessages.size} mensajes directos")
                    } else {
                        throw e // Re-lanzar si es un error diferente
                    }
                }
                
                // Eliminar duplicados si los hubiera
                val uniqueMessages = combinedMessages.distinctBy { it.id }
                
                // Ordenar por fecha (más recientes primero)
                val sortedMessages = uniqueMessages.sortedByDescending { it.timestamp }
                
                emit(Result.Success(sortedMessages))
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener mensajes para el usuario: $userId")
                emit(Result.Error(e.message ?: "Error desconocido al obtener mensajes"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error general al obtener mensajes")
            emit(Result.Error(e.message ?: "Error general al obtener mensajes"))
        }
    }
    
    /**
     * Obtiene los mensajes de un usuario filtrados por tipo
     */
    fun getMessagesByType(userId: String, type: MessageType): Flow<Result<List<UnifiedMessage>>> = flow {
        try {
            emit(Result.Loading())
            
            try {
                // Intentamos primero con la consulta combinada original
                val combinedMessages = mutableListOf<UnifiedMessage>()
                
                // 1. Consulta para mensajes donde el usuario es destinatario directo
                val receiverQuery = firestore.collection(MESSAGES_COLLECTION)
                    .whereEqualTo("receiverId", userId)
                    .whereEqualTo("type", type.name)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    
                combinedMessages.addAll(
                    receiverQuery.get().await().documents
                        .mapNotNull { createMessageFromDocument(it) }
                )
                
                // 2. Consulta para mensajes donde el usuario está en la lista de destinatarios
                val receiversQuery = firestore.collection(MESSAGES_COLLECTION)
                    .whereArrayContains("receiversIds", userId)
                    .whereEqualTo("type", type.name)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                
                combinedMessages.addAll(
                    receiversQuery.get().await().documents
                        .mapNotNull { createMessageFromDocument(it) }
                )
                
                // Ordenar todos los mensajes por timestamp
                val sortedMessages = combinedMessages
                    .sortedByDescending { it.timestamp.seconds }
                
                emit(Result.Success(sortedMessages))
            } catch (e: Exception) {
                Timber.e(e, "Error en consulta con índice para tipo $type: ${e.message}")
                
                if (e.message?.contains("FAILED_PRECONDITION") == true && 
                    e.message?.contains("requires an index") == true) {
                    
                    // Si el error es por falta de índice, usamos un enfoque alternativo
                    Timber.w("Se requiere crear un índice en Firestore para tipo $type. Usando consulta alternativa.")
                    
                    // Consulta alternativa: Obtener todos los mensajes del tipo y filtrar en memoria
                    val allMessagesQuery = firestore.collection(MESSAGES_COLLECTION)
                        .whereEqualTo("type", type.name)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()
                        
                    val filteredMessages = allMessagesQuery.documents
                        .mapNotNull { createMessageFromDocument(it) }
                        .filter { message -> 
                            message.receiverId == userId || message.receiversIds.contains(userId)
                        }
                        .sortedByDescending { it.timestamp.seconds }
                        
                    emit(Result.Success(filteredMessages))
                } else {
                    // Si es otro tipo de error, lo propagamos
                    throw e
                }
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("FAILED_PRECONDITION") == true && 
                e.message?.contains("requires an index") == true -> 
                    "Se requiere crear un índice en Firestore para tipo $type. Contacte al administrador."
                    
                e.message?.contains("PERMISSION_DENIED") == true ->
                    "No tiene permisos para acceder a los mensajes."
                    
                e.message?.contains("UNAVAILABLE") == true || 
                e.message?.contains("network") == true ->
                    "Error de conexión. Compruebe su conexión a Internet."
                    
                else -> "Error al obtener mensajes de tipo $type: ${e.message}"
            }
            
            Timber.e(e, "Error al obtener mensajes por tipo: $type para usuario: $userId - $errorMsg")
            emit(Result.Error(errorMsg))
        }
    }
    
    /**
     * Obtiene mensajes de una conversación
     */
    fun getMessagesFromConversation(conversationId: String): Flow<Result<List<UnifiedMessage>>> = flow {
        try {
            emit(Result.Loading())
            
            val query = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("conversationId", conversationId)
                .whereEqualTo("type", MessageType.CHAT.name)
                .orderBy("timestamp", Query.Direction.ASCENDING)
            
            val messages = query.get().await().documents
                .mapNotNull { createMessageFromDocument(it) }
                
            emit(Result.Success(messages))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensajes de conversación: $conversationId")
            emit(Result.Error(e.message ?: "Error al obtener mensajes de conversación"))
        }
    }
    
    /**
     * Elimina un mensaje
     */
    suspend fun deleteMessage(messageId: String): Result<Boolean> {
        return try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .delete()
                .await()
                
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar mensaje: $messageId")
            Result.Error(e.message ?: "Error al eliminar mensaje")
        }
    }
    
    /**
     * Crea o actualiza una conversación
     */
    suspend fun createOrUpdateConversation(
        conversationId: String,
        participantIds: List<String>,
        title: String = "",
        entityId: String = "",
        entityType: String = ""
    ): Result<String> {
        return try {
            val convId = conversationId.ifEmpty { UUID.randomUUID().toString() }
            val conversationData = mapOf(
                "participantIds" to participantIds,
                "title" to title,
                "lastMessageTimestamp" to Timestamp.now(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "entityId" to entityId,
                "entityType" to entityType
            )
            
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(convId)
                .set(conversationData)
                .await()
                
            Result.Success(convId)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear/actualizar conversación")
            Result.Error(e.message ?: "Error al gestionar conversación")
        }
    }
    
    /**
     * Crea un objeto UnifiedMessage a partir de un DocumentSnapshot
     */
    private fun createMessageFromDocument(doc: DocumentSnapshot): UnifiedMessage? {
        return try {
            if (!doc.exists()) return null
            
            val data = doc.data ?: return null
            UnifiedMessage.fromMap(doc.id, data)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear mensaje desde documento: ${doc.id}")
            null
        }
    }
    
    /**
     * Actualiza una conversación con un nuevo mensaje
     */
    private suspend fun updateConversationWithNewMessage(message: UnifiedMessage) {
        try {
            if (message.conversationId.isEmpty()) return
            
            val conversationUpdates = mapOf(
                "lastMessage" to message.content,
                "lastMessageSenderId" to message.senderId,
                "lastMessageTimestamp" to message.timestamp,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(message.conversationId)
                .update(conversationUpdates)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar conversación con nuevo mensaje")
        }
    }
    
    /**
     * Obtiene las notificaciones no leídas para un usuario
     */
    suspend fun getUnreadNotificationsCount(userId: String): Int {
        return try {
            val receiverQuery = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", MessageStatus.UNREAD.name)
                
            val receiversQuery = firestore.collection(MESSAGES_COLLECTION)
                .whereArrayContains("receiversIds", userId)
                .whereEqualTo("status", MessageStatus.UNREAD.name)
                
            val receiverCount = receiverQuery.get().await().size()
            val receiversCount = receiversQuery.get().await().size()
            
            receiverCount + receiversCount
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener conteo de notificaciones no leídas")
            0
        }
    }
    
    /**
     * Envía un comunicado oficial a múltiples destinatarios
     */
    suspend fun sendAnnouncement(
        title: String,
        content: String,
        receiversIds: List<String> = emptyList(),
        receiverTypes: List<TipoUsuario> = emptyList(),
        priority: MessagePriority = MessagePriority.NORMAL,
        requireConfirmation: Boolean = false
    ): Result<String> {
        return try {
            val currentUser = authRepository.getCurrentUser() ?: 
                return Result.Error("Usuario no autenticado")
            
            val userSnapshot = firestore.collection(USERS_COLLECTION)
                .document(currentUser.dni)
                .get()
                .await()
            
            if (!userSnapshot.exists()) {
                return Result.Error("Información de usuario no encontrada")
            }
            
            val userName = userSnapshot.getString("nombre") ?: "Usuario"
            
            // Construir el mensaje unificado
            val message = UnifiedMessage(
                title = title,
                content = content,
                type = MessageType.ANNOUNCEMENT,
                priority = priority,
                senderId = currentUser.dni,
                senderName = userName,
                receiversIds = receiversIds,
                timestamp = Timestamp.now(),
                status = MessageStatus.UNREAD,
                metadata = mapOf(
                    "requireConfirmation" to requireConfirmation.toString(),
                    "receiverTypes" to receiverTypes.joinToString(",") { it.name }
                )
            )
            
            sendMessage(message)
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar comunicado")
            Result.Error(e.message ?: "Error al enviar comunicado")
        }
    }

    /**
     * Obtiene las conversaciones del usuario actual
     */
    fun getCurrentUserConversations(): Flow<Result<List<Conversation>>> = flow {
        try {
            emit(Result.Loading())
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                emit(Result.Error("Usuario no autenticado"))
                return@flow
            }
            
            val query = firestore.collection(CONVERSATIONS_COLLECTION)
                .whereArrayContains("participantIds", currentUser.dni)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            
            val snapshot = query.get().await()
            
            val conversations = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                
                val participantIds = (data["participantIds"] as? List<String>) ?: emptyList()
                
                // Obtener información de los participantes (excepto el usuario actual)
                val otherParticipantIds = participantIds.filter { it != currentUser.dni }
                val participantDetails = mutableListOf<ParticipantDetail>()
                
                for (participantId in otherParticipantIds) {
                    // Obtener detalles del participante
                    val participantResult = getParticipantInfo(participantId)
                    participantResult?.let { participantDetails.add(it) }
                }
                
                Conversation(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    participantIds = participantIds,
                    participants = participantDetails,
                    lastMessage = data["lastMessage"] as? String ?: "",
                    lastMessageTimestamp = data["lastMessageTimestamp"] as? Timestamp ?: Timestamp.now(),
                    lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                    entityId = data["entityId"] as? String ?: "",
                    entityType = data["entityType"] as? String ?: ""
                )
            }
            
            emit(Result.Success(conversations))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener conversaciones del usuario actual")
            emit(Result.Error(e.message ?: "Error al obtener conversaciones"))
        }
    }

    /**
     * Obtiene la información de un participante de conversación
     */
    private suspend fun getParticipantInfo(participantId: String): ParticipantDetail? {
        return try {
            val userResult = usuarioRepository.getUsuarioById(participantId)
            if (userResult is Result.Success<*>) {
                val user = userResult.data
                val userType = try {
                    user?.javaClass?.getMethod("getPerfiles")?.invoke(user)?.toString() ?: ""
                } catch (e: Exception) {
                    Timber.e(e, "Error al acceder al campo perfiles")
                    ""
                }
                
                val id = try { user?.javaClass?.getMethod("getDni")?.invoke(user)?.toString() ?: "" } 
                          catch (e: Exception) { "" }
                
                val nombre = try { user?.javaClass?.getMethod("getNombre")?.invoke(user)?.toString() ?: "" } 
                             catch (e: Exception) { "" }
                
                val apellidos = try { user?.javaClass?.getMethod("getApellidos")?.invoke(user)?.toString() ?: "" } 
                                catch (e: Exception) { "" }
                
                val avatarUrl = try { user?.javaClass?.getMethod("getAvatarUrl")?.invoke(user)?.toString() } 
                                catch (e: Exception) { null }
                
                ParticipantDetail(
                    id = id,
                    name = "$nombre $apellidos".trim(),
                    avatarUrl = avatarUrl,
                    userType = userType
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener información del participante: $participantId")
            null
        }
    }

    suspend fun getUnreadMessageCount(userId: String): Flow<Result<Int>> = flow {
        try {
            emit(Result.Loading())
            
            val receiverQuery = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", MessageStatus.UNREAD.name)
                
            val receiversQuery = firestore.collection(MESSAGES_COLLECTION)
                .whereArrayContains("receiversIds", userId)
                .whereEqualTo("status", MessageStatus.UNREAD.name)
                
            val receiverCount = receiverQuery.get().await().size()
            val receiversCount = receiversQuery.get().await().size()
            
            val unreadCount = receiverCount + receiversCount
            
            emit(Result.Success(unreadCount))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener conteo de mensajes no leídos")
            emit(Result.Error(e.message ?: "Error al obtener conteo de mensajes no leídos"))
        }
    }
} 