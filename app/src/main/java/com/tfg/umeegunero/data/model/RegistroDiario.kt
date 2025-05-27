package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo para representar el registro diario de un alumno
 */
data class RegistroDiario(
    @DocumentId
    val id: String = "",
    val alumnoId: String = "",
    val claseId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val presente: Boolean = false,
    val justificada: Boolean = false,
    val observaciones: String = "",
    val profesorId: String = "",
    val modificadoPor: String = "",
    val horaEntrada: Timestamp? = null,
    val horaSalida: Timestamp? = null,
    val comportamiento: String = "",
    val participacion: String = "",
    val metadatos: Map<String, Any> = emptyMap()
) 