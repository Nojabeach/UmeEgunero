package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Enum que define los posibles estados de una notificación de ausencia
 */
enum class EstadoNotificacionAusencia {
    PENDIENTE,      // Notificación creada, no procesada
    ACEPTADA,       // Ausencia confirmada por el profesor
    RECHAZADA,      // Ausencia rechazada por el profesor
    COMPLETADA      // Ausencia procesada (registro completado)
}

/**
 * Modelo que representa una notificación de ausencia de un alumno.
 * 
 * Este modelo almacena la información relacionada con las ausencias
 * notificadas por los familiares.
 */
data class NotificacionAusencia(
    @DocumentId
    val id: String = "",
    
    // Datos del alumno
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val claseId: String = "",
    val claseCurso: String = "",
    
    // Datos del familiar que notifica
    val familiarId: String = "",
    val familiarNombre: String = "",
    
    // Fecha de ausencia y motivo
    val fechaAusencia: Timestamp = Timestamp.now(),
    val motivo: String = "",
    val duracion: Int = 1, // Duración en días
    
    // Datos de la notificación
    val fechaNotificacion: Timestamp = Timestamp.now(),
    val estado: String = EstadoNotificacionAusencia.PENDIENTE.name,
    
    // Control de visualización por profesor
    val vistaPorProfesor: Boolean = false,
    val profesorId: String = "",
    val fechaVistoPorProfesor: Timestamp? = null,
    
    // Documentación adjunta (opcional)
    val tieneDocumentacion: Boolean = false,
    val urlDocumentacion: String = "",
    
    // Comentarios adicionales del profesor
    val comentariosProfesor: String = ""
) 