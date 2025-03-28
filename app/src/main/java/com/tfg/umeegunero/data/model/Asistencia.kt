package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Enum para representar el estado de asistencia de un alumno
 */
enum class Asistencia {
    PRESENTE,
    AUSENTE,
    RETRASADO
}

/**
 * Modelo que representa un registro de asistencia para una clase en una fecha específica
 */
data class RegistroAsistencia(
    @DocumentId val id: String = "",
    val claseId: String = "",
    val profesorId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val estadosAsistencia: Map<String, Asistencia> = emptyMap(), // DNI del alumno -> Estado de asistencia
    val observaciones: String = ""
) 