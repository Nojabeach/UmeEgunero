package com.tfg.umeegunero.util

import com.tfg.umeegunero.data.service.RemoteConfigService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Properties
import javax.inject.Inject
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Clase que gestiona el envío de emails usando JavaMail
 */
class EmailSender @Inject constructor(
    private val remoteConfigService: RemoteConfigService
) {
    /**
     * Envía un email usando el servicio SMTP de Gmail
     * @param from Email remitente
     * @param to Email destinatario
     * @param subject Asunto del mensaje
     * @param messageBody Cuerpo del mensaje
     * @param senderName Nombre del remitente
     * @return true si el email fue enviado correctamente
     */
    suspend fun sendEmail(
        from: String,
        to: String,
        subject: String,
        messageBody: String,
        senderName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Obtenemos la contraseña desde Firebase Remote Config
            val password = remoteConfigService.getSMTPPassword()
            
            if (password.isBlank()) {
                Timber.e("No se pudo obtener la contraseña SMTP desde Remote Config")
                return@withContext false
            }
            
            // Configuración del servidor SMTP (Gmail)
            val properties = Properties()
            properties["mail.smtp.host"] = "smtp.gmail.com"
            properties["mail.smtp.port"] = "587"
            properties["mail.smtp.auth"] = "true"
            properties["mail.smtp.starttls.enable"] = "true"
            
            // Autenticación con el servidor SMTP
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(from, password)
                }
            })
            
            // Creación del mensaje
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(from, senderName))
            message.setRecipient(Message.RecipientType.TO, InternetAddress(to))
            message.subject = subject
            message.setText(messageBody)
            
            // Envío del mensaje
            Transport.send(message)
            
            Timber.d("Email enviado correctamente a $to")
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar email: ${e.message}")
            return@withContext false
        }
    }
} 