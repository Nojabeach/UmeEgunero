package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Representa una tarea asignada por un profesor a una clase
 */
data class Tarea(
    @DocumentId val id: String = "",
    val profesorId: String = "",
    val claseId: String = "",
    val nombreClase: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaEntrega: Timestamp? = null,
    val adjuntos: List<String> = emptyList(), // URLs de archivos adjuntos
    val entregada: Boolean = false
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