package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de datos para los comunicados generales
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
     * Identificadores de usuario destinatarios específicos (opcional)
     */
    val destinatarios: List<String> = emptyList(),
    
    /**
     * Tipos de usuario a los que va dirigido el comunicado
     */
    val tiposDestinatarios: List<TipoUsuario> = emptyList(),
    
    /**
     * Fecha de creación del comunicado
     */
    @ServerTimestamp
    val fechaCreacion: Timestamp = Timestamp.now(),
    
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
    val recursos: List<String> = emptyList()
) 