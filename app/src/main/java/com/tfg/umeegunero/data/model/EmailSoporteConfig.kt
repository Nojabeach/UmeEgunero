package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Configuración para el envío de emails de soporte técnico
 */
data class EmailSoporteConfig(
    @DocumentId val id: String = "config_email_soporte",
    val emailDestino: String = "maitanepruebas1@gmail.com",
    val ultimaActualizacion: Timestamp = Timestamp.now()
) 