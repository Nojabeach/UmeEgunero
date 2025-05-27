package com.tfg.umeegunero.data.model

/**
 * Tipos de mensajes en el sistema de mensajería unificada
 */
enum class MessageType {
    CHAT,           // Mensaje de chat individual
    GROUP_CHAT,     // Mensaje de chat grupal
    ANNOUNCEMENT,   // Anuncio o comunicado oficial
    NOTIFICATION,   // Notificación del sistema
    TASK,           // Tarea asignada
    EVENT,          // Evento o recordatorio
    SYSTEM,         // Mensaje del sistema
    INCIDENT,       // Incidencia
    ATTENDANCE,     // Asistencia
    DAILY_RECORD    // Registro diario
} 