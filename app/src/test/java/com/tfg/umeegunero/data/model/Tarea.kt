package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de Tarea para tests
 */
data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaEntrega: Timestamp = Timestamp.now(),
    val claseId: String = "",
    val profesorId: String = "",
    val asignaturaId: String = "",
    val archivoAdjunto: String? = null,
    val estado: String = EstadoTarea.PENDIENTE.name
)

/**
 * Estados posibles de una tarea
 */
enum class EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA,
    VENCIDA,
    CALIFICADA
} 