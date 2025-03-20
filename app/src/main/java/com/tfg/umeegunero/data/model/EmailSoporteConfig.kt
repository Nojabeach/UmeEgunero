package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Configuración para el envío de emails de soporte técnico
 */
data class EmailSoporteConfig(
    @DocumentId val id: String = "config_email_soporte",
    val emailDestino: String = "maitaneibaira@gmail.com",
    val emailRemitente: String = "app.umeegunero@gmail.com",
    // La contraseña ahora se guarda en Firebase Remote Config
    val nombreRemitente: String = "Soporte UmeEgunero",
    val usarEmailUsuarioComoRemitente: Boolean = false,
    val ultimaActualizacion: Timestamp = Timestamp.now()
) 