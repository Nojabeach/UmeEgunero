package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Modelo unificado de mensaje para el sistema de comunicación
 * 
 * Esta clase representa un modelo unificado para todos los tipos de comunicaciones
 * en la aplicación, incluyendo notificaciones, mensajes, chats y comunicados.
 */
data class UnifiedMessage(
    @DocumentId 
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverType: String = "",
    val title: String = "",
    val content: String = "",
    val type: MessageType = MessageType.CHAT,
    val timestamp: Timestamp = Timestamp.now(),
    val status: MessageStatus = MessageStatus.UNREAD,
    val attachments: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val conversationId: String = "",
    val priority: MessagePriority = MessagePriority.NORMAL,
    val receiversIds: List<String> = emptyList(),
    val relatedEntityId: String = "",
    val relatedEntityType: String = ""
) {
    /**
     * Convierte el mensaje a un mapa para Firestore
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "senderId" to senderId,
        "senderName" to senderName,
        "receiverId" to receiverId,
        "receiverType" to receiverType,
        "title" to title,
        "content" to content,
        "type" to type.name,
        "timestamp" to timestamp,
        "status" to status.name,
        "attachments" to attachments,
        "metadata" to metadata,
        "isRead" to isRead,
        "isArchived" to isArchived,
        "isDeleted" to isDeleted,
        "conversationId" to conversationId,
        "priority" to priority.value,
        "receiversIds" to receiversIds,
        "relatedEntityId" to relatedEntityId,
        "relatedEntityType" to relatedEntityType
    )
    
    /**
     * Retorna el color asociado al tipo de mensaje,
     * útil para la interfaz de usuario
     */
    val typeColor: String
        get() = when(type) {
            MessageType.ANNOUNCEMENT -> "#43a047" // Verde
            MessageType.CHAT -> "#8e24aa" // Púrpura
            MessageType.GROUP_CHAT -> "#673ab7" // Morado
            MessageType.NOTIFICATION -> "#546e7a" // Gris azulado
            MessageType.SYSTEM -> "#757575" // Gris
            MessageType.TASK -> "#f57c00" // Naranja
            MessageType.EVENT -> "#039be5" // Azul claro
            MessageType.INCIDENT -> "#d32f2f" // Rojo
            MessageType.ATTENDANCE -> "#fbc02d" // Amarillo
            MessageType.DAILY_RECORD -> "#1976d2" // Azul
        }
    
    /**
     * Retorna el icono asociado al tipo de mensaje,
     * útil para la interfaz de usuario
     */
    val typeIcon: String
        get() = when(type) {
            MessageType.ANNOUNCEMENT -> "announcement"
            MessageType.CHAT -> "chat"
            MessageType.GROUP_CHAT -> "group"
            MessageType.NOTIFICATION -> "notifications"
            MessageType.SYSTEM -> "system_update"
            MessageType.TASK -> "assignment"
            MessageType.EVENT -> "event"
            MessageType.INCIDENT -> "warning"
            MessageType.ATTENDANCE -> "people"
            MessageType.DAILY_RECORD -> "book"
        }
    
    companion object {
        /**
         * Crea un mensaje unificado a partir de un mapa de datos de Firestore
         */
        fun fromMap(id: String, data: Map<String, Any?>): UnifiedMessage {
            val typeStr = data["type"] as? String ?: MessageType.CHAT.name
            val statusStr = data["status"] as? String ?: MessageStatus.UNREAD.name
            val priorityValue = (data["priority"] as? Number)?.toInt() ?: MessagePriority.NORMAL.value
            
            // Determinar si el mensaje está leído basado en el status o el campo explícito
            val status = try { MessageStatus.valueOf(statusStr) } catch (e: Exception) { MessageStatus.UNREAD }
            val explicitIsRead = data["isRead"] as? Boolean ?: false
            val readTimestamp = data["readTimestamp"] as? Timestamp
            val readByMap = data["readBy"] as? Map<*, *>
            
            // Un mensaje está leído si:
            // 1. Su status es READ, o
            // 2. El campo isRead es true, o
            // 3. Tiene un readTimestamp, o
            // 4. Tiene entradas en el mapa readBy
            val isRead = status == MessageStatus.READ || 
                         explicitIsRead || 
                         readTimestamp != null ||
                         (readByMap != null && readByMap.isNotEmpty())
            
            return UnifiedMessage(
                id = id,
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "",
                receiverId = data["receiverId"] as? String ?: "",
                receiverType = data["receiverType"] as? String ?: "",
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                type = try { MessageType.valueOf(typeStr) } catch (e: Exception) { MessageType.CHAT },
                timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                status = status,
                attachments = getStringList(data["attachments"]),
                metadata = getStringMap(data["metadata"]),
                isRead = isRead,
                isArchived = data["isArchived"] as? Boolean ?: false,
                isDeleted = data["isDeleted"] as? Boolean ?: false,
                conversationId = data["conversationId"] as? String ?: "",
                priority = MessagePriority.fromInt(priorityValue),
                receiversIds = getStringList(data["receiversIds"]),
                relatedEntityId = data["relatedEntityId"] as? String ?: "",
                relatedEntityType = data["relatedEntityType"] as? String ?: ""
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
    }
} 