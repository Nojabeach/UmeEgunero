package com.tfg.umeegunero.data.model

/**
 * Enum que representa los posibles estados de asistencia de un alumno.
 */
enum class EstadoAsistencia {
    /**
     * El alumno está presente en clase
     */
    PRESENTE,
    
    /**
     * El alumno está ausente sin justificación
     */
    AUSENTE,
    
    /**
     * El alumno está ausente pero con justificación
     */
    AUSENTE_JUSTIFICADO,
    
    /**
     * El alumno ha llegado tarde
     */
    RETRASADO,
    
    /**
     * El alumno ha llegado tarde
     */
    TARDE
} 