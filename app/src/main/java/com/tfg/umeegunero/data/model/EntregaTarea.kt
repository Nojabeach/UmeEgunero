package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa la entrega de una tarea por parte de un alumno
 */
data class EntregaTarea(
    @DocumentId
    val id: String = "",
    val tareaId: String = "",
    val alumnoId: String = "",
    val fechaEntrega: Timestamp = Timestamp.now(),
    val archivos: List<String> = emptyList(),
    val comentario: String = "",
    val calificacion: Float? = null,
    val comentarioProfesor: String? = null,
    val fechaCalificacion: Timestamp? = null
) 