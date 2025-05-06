package com.tfg.umeegunero.data.model

/**
 * Enumeración para representar los diferentes tipos de notificación
 */
enum class TipoNotificacion {
    GENERAL,     // Notificaciones generales
    ANUNCIO,     // Anuncios específicos
    EVENTO,      // Eventos del calendario, fechas importantes
    URGENTE,     // Notificaciones urgentes
    SISTEMA,     // Notificaciones del sistema (actualizaciones, mantenimiento, etc.)
    MENSAJE,     // Mensajes personales
    ACADEMICO,   // Relacionado con actividades académicas
    ALERTA       // Alertas importantes
} 