package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de datos para los comunicados generales
 */
data class Comunicado(
    @DocumentId
    val id: String? = null,
    
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
     * Registro de lecturas por usuario
     * Mapa donde la clave es el ID del usuario y el valor es la fecha de lectura
     */
    val lecturas: Map<String, Timestamp> = emptyMap(),

    /**
     * Firma digital del remitente en formato Base64 (opcional)
     * Esta es la representación textual de la firma
     */
    val firmaDigital: String? = null,
    
    /**
     * URL de la imagen de la firma almacenada en Firebase Storage
     */
    val firmaDigitalUrl: String? = null,
    
    /**
     * Hash de verificación de la firma digital
     * Se utiliza para verificar la autenticidad de la firma
     */
    val firmaDigitalHash: String? = null,
    
    /**
     * Marca de tiempo de cuando se firmó el comunicado
     */
    val firmaTimestamp: Timestamp? = null,

    /**
     * Indica si el comunicado requiere confirmación de lectura
     */
    val requiereConfirmacion: Boolean = false,

    /**
     * Lista de usuarios que han confirmado la lectura
     * Mapa donde la clave es el ID del usuario y el valor es la fecha de confirmación
     */
    val confirmacionesLectura: Map<String, Timestamp> = emptyMap(),
    
    /**
     * Indica si el comunicado requiere firma digital por parte del destinatario
     */
    val requiereFirmaDestinatario: Boolean = false,
    
    /**
     * Registro de firmas digitales de los destinatarios
     * Mapa donde la clave es el ID del usuario y el valor es un mapa con información de la firma
     */
    val firmasDestinatarios: Map<String, Map<String, Any>> = emptyMap(),
    
    /**
     * URLs de las firmas de los destinatarios
     * Mapa donde la clave es el ID del usuario y el valor es la URL de la firma
     */
    val firmasUrlsDestinatarios: Map<String, String> = emptyMap()
) 