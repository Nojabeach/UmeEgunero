package com.tfg.umeegunero.data.model

/**
 * Estados posibles para un mensaje en el sistema
 */
enum class MessageStatus {
    READ,       // Mensaje leído por el destinatario
    UNREAD,     // Mensaje no leído aún
    ARCHIVED,   // Mensaje archivado
    DELETED,    // Mensaje eliminado
    CONFIRMED   // Mensaje confirmado por el destinatario
} 