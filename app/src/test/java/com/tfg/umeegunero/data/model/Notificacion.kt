package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de Notificación para tests
 */
data class Notificacion(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val tipo: TipoNotificacion = TipoNotificacion.GENERAL,
    val usuarioId: String = "",
    val leida: Boolean = false,
    val accion: String? = null,
    val datos: Map<String, String> = emptyMap()
)

/**
 * Tipos de notificación disponibles en la aplicación para tests
 */
enum class TipoNotificacion {
    GENERAL,
    ACTIVIDAD,
    MENSAJE,
    TAREA,
    ASISTENCIA,
    SISTEMA
} 