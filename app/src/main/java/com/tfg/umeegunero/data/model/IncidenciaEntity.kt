package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Entidad que representa una incidencia reportada por un profesor sobre un alumno
 */
data class IncidenciaEntity(
    val id: String = "",
    val alumnoId: String = "",
    val profesorId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val urgente: Boolean = false,
    val estado: String = "PENDIENTE",
    val fechaResolucion: Timestamp? = null
) 