package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Configuración para el envío de emails de soporte técnico
 */
data class EmailSoporteConfig(
    @DocumentId val id: String = "config_email_soporte",
    val emailDestino: String = "UmeEgunero@gmail.com",
    val ultimaActualizacion: Timestamp = Timestamp.now()
)

/**
 * Constantes para el soporte técnico por email
 */
object EmailSoporteConstants {
    /**
     * Email desde el que se envían los mensajes de soporte
     */
    const val EMAIL_SOPORTE = "soporte@umeegunero.com"
    
    /**
     * Email destinatario para mensajes de soporte
     */
    const val EMAIL_DESTINATARIO = "soporte@umeegunero.com"
    
    /**
     * Asuntos predefinidos para mensajes de soporte
     */
    val TEMAS_SOPORTE = listOf(
        "Problema técnico",
        "Pregunta sobre funcionalidad",
        "Sugerencia de mejora",
        "Reporte de error",
        "Otro"
    )
} 