package com.tfg.umeegunero.data.model

import java.io.Serializable

/**
 * Modelo de datos para los mensajes de chat
 */
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false,
    val readTimestamp: Long? = null,
    val attachmentType: AttachmentType? = null,
    val attachmentUrl: String? = null,
    val interactionStatus: InteractionStatus = InteractionStatus.NONE,
    val isTranslated: Boolean = false,
    val originalText: String? = null
) : Serializable 