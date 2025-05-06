package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Modelo unificado de mensaje para el sistema de comunicación
 * 
 * Esta clase representa un modelo unificado para todos los tipos de comunicaciones
 * en la aplicación, incluyendo notificaciones, mensajes, chats y comunicados.
 *
 * @property id Identificador único del mensaje
 * @property title Título o asunto del mensaje
 * @property content Contenido principal del mensaje
 * @property type Tipo de mensaje (chat, notificación, comunicado, etc.)
 * @property priority Prioridad del mensaje (normal, alta, urgente, etc.)
 * @property senderId ID del remitente
 * @property senderName Nombre del remitente
 * @property receiverId ID del destinatario principal (si es individual)
 * @property receiversIds Lista de IDs de destinatarios (si es grupal)
 * @property timestamp Fecha y hora del mensaje
 * @property status Estado del mensaje (leído, no leído, entregado, etc.)
 * @property readTimestamp Fecha y hora de lectura (si aplica)
 * @property metadata Datos adicionales específicos según el tipo de mensaje
 * @property relatedEntityId ID de la entidad relacionada (alumno, clase, etc.)
 * @property relatedEntityType Tipo de la entidad relacionada
 * @property attachments Lista de adjuntos (URLs, referencias, etc.)
 * @property actions Acciones disponibles para este mensaje
 * @property conversationId ID de la conversación (para mensajes de chat)
 * @property replyToId ID del mensaje al que responde (si es una respuesta)
 */
data class UnifiedMessage(
    @DocumentId val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val type: MessageType = MessageType.CHAT,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiversIds: List<String> = emptyList(),
    val timestamp: Timestamp = Timestamp.now(),
    val status: MessageStatus = MessageStatus.UNREAD,
    val readTimestamp: Timestamp? = null,
    val metadata: Map<String, String> = emptyMap(),
    val relatedEntityId: String = "",
    val relatedEntityType: String = "",
    val attachments: List<Map<String, String>> = emptyList(),
    val actions: List<MessageAction> = emptyList(),
    val conversationId: String = "",
    val replyToId: String = ""
) {
    /**
     * Convierte el mensaje a un mapa para Firestore
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "content" to content,
        "type" to type.name,
        "priority" to priority.name,
        "senderId" to senderId,
        "senderName" to senderName,
        "receiverId" to receiverId,
        "receiversIds" to receiversIds,
        "timestamp" to timestamp,
        "status" to status.name,
        "readTimestamp" to readTimestamp,
        "metadata" to metadata,
        "relatedEntityId" to relatedEntityId,
        "relatedEntityType" to relatedEntityType,
        "attachments" to attachments,
        "actions" to actions.map { it.toMap() },
        "conversationId" to conversationId,
        "replyToId" to replyToId
    )
    
    /**
     * Indica si el mensaje es personal (dirigido a un único usuario)
     */
    val isPersonal: Boolean
        get() = receiverId.isNotEmpty() && receiversIds.isEmpty()
    
    /**
     * Indica si el mensaje es grupal (dirigido a múltiples usuarios)
     */
    val isGroup: Boolean
        get() = receiversIds.isNotEmpty()
    
    /**
     * Indica si el mensaje es de alta prioridad
     */
    val isHighPriority: Boolean
        get() = priority == MessagePriority.HIGH || priority == MessagePriority.URGENT
    
    /**
     * Indica si el mensaje es urgente
     */
    val isUrgent: Boolean
        get() = priority == MessagePriority.URGENT
        
    /**
     * Indica si el mensaje está leído
     */
    val isRead: Boolean
        get() = status == MessageStatus.READ
    
    /**
     * Retorna el color asociado al tipo de mensaje,
     * útil para la interfaz de usuario
     */
    val typeColor: String
        get() = when(type) {
            MessageType.INCIDENT -> "#e53935" // Rojo
            MessageType.ATTENDANCE -> "#1e88e5" // Azul
            MessageType.ANNOUNCEMENT -> "#43a047" // Verde
            MessageType.DAILY_RECORD -> "#fb8c00" // Naranja
            MessageType.CHAT -> "#8e24aa" // Púrpura
            MessageType.NOTIFICATION -> "#546e7a" // Gris azulado
            MessageType.SYSTEM -> "#757575" // Gris
        }
    
    /**
     * Retorna el icono asociado al tipo de mensaje,
     * útil para la interfaz de usuario
     */
    val typeIcon: String
        get() = when(type) {
            MessageType.INCIDENT -> "incident"
            MessageType.ATTENDANCE -> "attendance"
            MessageType.ANNOUNCEMENT -> "announcement"
            MessageType.DAILY_RECORD -> "assignment"
            MessageType.CHAT -> "chat"
            MessageType.NOTIFICATION -> "notifications"
            MessageType.SYSTEM -> "system_update"
        }
    
    companion object {
        /**
         * Crea un mensaje unificado a partir de un mapa de datos de Firestore
         */
        fun fromMap(id: String, data: Map<String, Any?>): UnifiedMessage {
            val typeStr = data["type"] as? String ?: MessageType.CHAT.name
            val priorityStr = data["priority"] as? String ?: MessagePriority.NORMAL.name
            val statusStr = data["status"] as? String ?: MessageStatus.UNREAD.name
            
            @Suppress("UNCHECKED_CAST")
            val actionsData = data["actions"] as? List<Map<String, Any?>> ?: emptyList()
            val actions = actionsData.mapNotNull { MessageAction.fromMap(it) }
            
            return UnifiedMessage(
                id = id,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.CHAT },
                priority = try { MessagePriority.valueOf(priorityStr) } catch (e: Exception) { MessagePriority.NORMAL },
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "",
                receiverId = data["receiverId"] as? String ?: "",
                receiversIds = getStringList(data["receiversIds"]),
                timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                status = try { MessageStatus.valueOf(statusStr) } catch (e: Exception) { MessageStatus.UNREAD },
                readTimestamp = data["readTimestamp"] as? Timestamp,
                metadata = getStringMap(data["metadata"]),
                relatedEntityId = data["relatedEntityId"] as? String ?: "",
                relatedEntityType = data["relatedEntityType"] as? String ?: "",
                attachments = getMapList(data["attachments"]),
                actions = actions,
                conversationId = data["conversationId"] as? String ?: "",
                replyToId = data["replyToId"] as? String ?: ""
            )
        }
        
        /**
         * Convierte de forma segura un Any? a List<String>
         */
        private fun getStringList(data: Any?): List<String> {
            return try {
                @Suppress("UNCHECKED_CAST")
                data as? List<String> ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        /**
         * Convierte de forma segura un Any? a Map<String, String>
         */
        private fun getStringMap(data: Any?): Map<String, String> {
            return try {
                @Suppress("UNCHECKED_CAST")
                data as? Map<String, String> ?: emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }
        }
        
        /**
         * Convierte de forma segura un Any? a List<Map<String, String>>
         */
        private fun getMapList(data: Any?): List<Map<String, String>> {
            return try {
                @Suppress("UNCHECKED_CAST")
                data as? List<Map<String, String>> ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
} 