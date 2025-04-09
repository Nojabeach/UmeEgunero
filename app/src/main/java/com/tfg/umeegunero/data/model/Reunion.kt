package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de datos para las reuniones
 */
data class Reunion(
    @DocumentId
    val id: String = "",
    
    /**
     * Título de la reunión
     */
    val titulo: String = "",
    
    /**
     * Descripción detallada de la reunión
     */
    val descripcion: String = "",
    
    /**
     * Fecha y hora de inicio de la reunión
     */
    val fechaInicio: Timestamp = Timestamp.now(),
    
    /**
     * Fecha y hora de fin de la reunión
     */
    val fechaFin: Timestamp = Timestamp.now(),
    
    /**
     * ID del organizador de la reunión
     */
    val organizadorId: String = "",
    
    /**
     * Nombre del organizador de la reunión
     */
    val organizadorNombre: String = "",
    
    /**
     * Lista de IDs de los participantes
     */
    val participantesIds: List<String> = emptyList(),
    
    /**
     * Lista de nombres de los participantes
     */
    val participantesNombres: List<String> = emptyList(),
    
    /**
     * Estado de la reunión
     */
    val estado: EstadoReunion = EstadoReunion.PENDIENTE,
    
    /**
     * Tipo de reunión
     */
    val tipo: TipoReunion = TipoReunion.GENERAL,
    
    /**
     * Ubicación de la reunión (opcional)
     */
    val ubicacion: String? = null,
    
    /**
     * Enlace para reunión virtual (opcional)
     */
    val enlaceVirtual: String? = null,
    
    /**
     * Lista de archivos adjuntos
     */
    val adjuntos: List<String> = emptyList(),
    
    /**
     * Notas o agenda de la reunión
     */
    val notas: String = "",
    
    /**
     * Recordatorios configurados
     */
    val recordatorios: List<Recordatorio> = emptyList(),
    
    /**
     * Fecha de creación del registro
     */
    @ServerTimestamp
    val fechaCreacion: Timestamp = Timestamp.now(),
    
    /**
     * Fecha de última modificación
     */
    @ServerTimestamp
    val fechaModificacion: Timestamp = Timestamp.now()
)

/**
 * Estados posibles de una reunión
 */
enum class EstadoReunion {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    COMPLETADA
}

/**
 * Tipos de reunión
 */
enum class TipoReunion {
    GENERAL,
    PADRES_PROFESORES,
    EQUIPO_DOCENTE,
    REUNION_ADMINISTRATIVA,
    OTRA
}

/**
 * Modelo para los recordatorios de reuniones
 */
data class Recordatorio(
    val tipo: TipoRecordatorio,
    val tiempoAntes: Int, // en minutos
    val enviado: Boolean = false
)

/**
 * Tipos de recordatorio
 */
enum class TipoRecordatorio {
    EMAIL,
    NOTIFICACION_PUSH,
    AMBOS
} 