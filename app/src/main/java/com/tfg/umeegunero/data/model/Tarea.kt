package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Representa una tarea asignada por un profesor a una clase o alumno específico
 */
data class Tarea(
    @DocumentId val id: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val claseId: String = "",
    val nombreClase: String = "",
    val alumnoId: String = "", // ID del alumno si es tarea individual
    val titulo: String = "",
    val descripcion: String = "",
    val asignatura: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaEntrega: Timestamp? = null,
    val adjuntos: List<String> = emptyList(), // URLs de archivos adjuntos
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val prioridad: PrioridadTarea = PrioridadTarea.MEDIA,
    val revisadaPorFamiliar: Boolean = false,
    val fechaRevision: Timestamp? = null,
    val comentariosFamiliar: String = "",
    val calificacion: Double? = null,
    val feedbackProfesor: String = ""
)

/**
 * Representa una entrega de tarea por parte de un alumno
 */
data class EntregaTarea(
    @DocumentId val id: String = "",
    val tareaId: String = "",
    val alumnoId: String = "",
    val fechaEntrega: Timestamp = Timestamp.now(),
    val archivos: List<String> = emptyList(), // URLs de archivos entregados
    val comentario: String = "",
    val calificacion: Double? = null,
    val feedbackProfesor: String = ""
)

/**
 * Enumeración para representar los diferentes estados de una tarea
 */
enum class EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA,
    CANCELADA,
    VENCIDA
}

/**
 * Enumeración para representar la prioridad de una tarea
 */
enum class PrioridadTarea {
    BAJA,
    MEDIA,
    ALTA,
    URGENTE
} 