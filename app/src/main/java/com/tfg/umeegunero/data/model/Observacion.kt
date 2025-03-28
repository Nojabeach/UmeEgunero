package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo para representar una observación sobre un alumno
 */
data class Observacion(
    val mensaje: String = "",
    val tipo: TipoObservacion = TipoObservacion.OTRO,
    val timestamp: Timestamp = Timestamp.now()
) 