package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de datos para los comunicados generales
 * 
 * Esta clase representa los comunicados oficiales enviados por administradores
 * y profesores a los diferentes tipos de usuarios del sistema educativo.
 * Incluye soporte para confirmación de lectura y seguimiento de usuarios.
 */
data class Comunicado(
    @DocumentId
    val id: String = "",
    
    /**
     * Título del comunicado
     */
    val titulo: String = "",
    
    /**
     * Contenido/mensaje del comunicado
     */
    val mensaje: String = "",
    
    /**
     * Destinatarios en formato texto para mostrar en la UI
     */
    val destinatarios: String = "",
    
    /**
     * Tipos de usuario a los que va dirigido el comunicado
     */
    val tiposDestinatarios: List<String> = emptyList(),
    
    /**
     * Fecha de creación del comunicado
     */
    @ServerTimestamp
    val fechaCreacion: Timestamp = Timestamp.now(),
    
    /**
     * Fecha formateada para mostrar en la UI
     */
    val fecha: String = "",
    
    /**
     * Nombre de quien creó el comunicado
     */
    val remitente: String = "",
    
    /**
     * Identificador de quien creó el comunicado
     */
    val creadoPor: String = "",
    
    /**
     * Indica si el comunicado está activo o ha sido archivado
     */
    val activo: Boolean = true,
    
    /**
     * Enlaces o recursos relacionados con el comunicado (opcional)
     */
    val recursos: List<String> = emptyList(),
    
    /**
     * Indica si el comunicado requiere confirmación explícita de lectura
     */
    val requiereConfirmacion: Boolean = false,
    
    /**
     * Lista de IDs de usuarios que han confirmado la lectura del comunicado
     */
    val usuariosConfirmados: List<String> = emptyList(),
    
    /**
     * Lista de IDs de usuarios que han leído el comunicado
     */
    val usuariosLeidos: List<String> = emptyList(),
    
    /**
     * Fecha límite para confirmar la lectura (opcional)
     */
    val fechaLimiteConfirmacion: Timestamp? = null
)
