package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de Asistencia para tests
 */
data class Asistencia(
    val id: String = "",
    val alumnoId: String = "",
    val tipoAsistencia: String = TipoAsistencia.PRESENTE.name,
    val fecha: Timestamp = Timestamp.now(),
    val comentarios: String? = null,
    val registradoPor: String = ""
) {
    /**
     * Tipos posibles de asistencia
     */
    enum class TipoAsistencia {
        PRESENTE,
        AUSENTE,
        RETRASO
    }
} 