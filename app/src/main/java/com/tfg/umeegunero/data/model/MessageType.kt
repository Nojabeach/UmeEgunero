package com.tfg.umeegunero.data.model

/**
 * Tipo de mensaje unificado en el sistema
 */
enum class MessageType {
    CHAT,               // Mensajes de chat entre usuarios
    NOTIFICATION,       // Notificaciones del sistema
    ANNOUNCEMENT,       // Comunicados/anuncios oficiales
    INCIDENT,           // Incidencias reportadas
    ATTENDANCE,         // Notificaciones de asistencia/ausencia
    DAILY_RECORD,       // Actualizaciones de registro diario
    SYSTEM              // Mensajes del sistema
} 