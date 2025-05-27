package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo de datos para el registro de asistencia de alumnos
 *
 * Esta clase representa un registro de asistencia para un alumno específico
 * en una fecha determinada y dentro de una clase.
 */
data class Asistencia(
    @DocumentId
    val id: String = "",
    
    /**
     * Identificador del alumno al que corresponde este registro
     */
    val alumnoId: String = "",
    
    /**
     * Identificador de la clase a la que pertenece el alumno
     */
    val claseId: String = "",
    
    /**
     * Fecha del registro de asistencia
     */
    val fecha: Timestamp = Timestamp.now(),
    
    /**
     * Indica si el alumno estuvo presente
     */
    val presente: Boolean = false,
    
    /**
     * Observaciones adicionales sobre la asistencia (justificación, etc.)
     */
    val observaciones: String = "",
    
    /**
     * Identificador del profesor que registró la asistencia
     */
    val registradoPor: String = "",
    
    /**
     * Timestamp del momento en que se registró la asistencia
     */
    val timestampRegistro: Timestamp = Timestamp.now()
) 