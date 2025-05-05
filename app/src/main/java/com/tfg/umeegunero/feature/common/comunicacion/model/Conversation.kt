package com.tfg.umeegunero.feature.common.comunicacion.model

import com.google.firebase.Timestamp

/**
 * Modelo que representa un participante en una conversación
 */
data class ParticipantDetail(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val userType: String = ""
)

/**
 * Modelo que representa una conversación en el sistema de mensajería unificado
 * 
 * @property id Identificador único de la conversación
 * @property title Título o nombre de la conversación (opcional)
 * @property participantIds Lista de IDs de participantes
 * @property participants Detalles de los participantes
 * @property lastMessage Último mensaje enviado en la conversación
 * @property lastMessageTimestamp Timestamp del último mensaje
 * @property lastMessageSenderId ID del remitente del último mensaje
 * @property entityId ID de la entidad relacionada (opcional, para conversaciones sobre alumnos, clases, etc.)
 * @property entityType Tipo de la entidad relacionada
 * @property unreadCount Cantidad de mensajes no leídos
 */
data class Conversation(
    val id: String,
    val title: String = "",
    val participantIds: List<String> = emptyList(),
    val participants: List<ParticipantDetail> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastMessageSenderId: String = "",
    val entityId: String = "",
    val entityType: String = "",
    val unreadCount: Int = 0
) {
    /**
     * Devuelve si la conversación es grupal (más de dos participantes)
     */
    val isGroup: Boolean
        get() = participantIds.size > 2
    
    /**
     * Compone un nombre para la conversación si no tiene título
     */
    val displayName: String
        get() = if (title.isNotBlank()) {
            title
        } else if (participants.isNotEmpty()) {
            participants.joinToString(", ") { it.name }
        } else {
            "Conversación sin título"
        }
} 