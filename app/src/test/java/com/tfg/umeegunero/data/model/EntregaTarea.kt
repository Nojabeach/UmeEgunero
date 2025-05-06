package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de EntregaTarea para tests
 */
data class EntregaTarea(
    val id: String = "",
    val tareaId: String = "",
    val alumnoId: String = "",
    val comentario: String = "",
    val archivos: List<String> = emptyList(),
    val fechaEntrega: Timestamp = Timestamp.now(),
    val calificacion: Double? = null,
    val comentarioProfesor: String? = null,
    val fechaCalificacion: Timestamp? = null
) 