package com.tfg.umeegunero.feature.common.support.screen

import android.os.AsyncTask
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.tfg.umeegunero.data.model.EmailSoporteConfig
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender {
    companion object {
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "587"
        private const val REMOTE_CONFIG_KEY_PASSWORD = "soporte_password"
        private const val SENDER_EMAIL = "maitanepruebas1@gmail.com"
        private var config: EmailSoporteConfig = EmailSoporteConfig()
        private var remoteConfig: FirebaseRemoteConfig? = null
        private const val TAG = "EmailSender"

        fun initialize(remoteConfig: FirebaseRemoteConfig) {
            this.remoteConfig = remoteConfig
        }

        fun updateConfig(newConfig: EmailSoporteConfig) {
            config = newConfig
        }
    }

    class SendMailTask(
        private val userEmail: String,
        private val subject: String,
        private val message: String,
        private val onSuccess: () -> Unit,
        private val onFailure: (Exception) -> Unit
    ) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            var session: Session? = null
            var transport: Transport? = null
            
            return try {
                Log.d(TAG, "Iniciando configuración SMTP...")
                
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.ssl.trust", SMTP_HOST)
                    put("mail.smtp.timeout", "30000")
                    put("mail.smtp.connectiontimeout", "30000")
                }

                val password = remoteConfig?.getString(REMOTE_CONFIG_KEY_PASSWORD)
                    ?: throw Exception("Remote Config no inicializado")
                
                Log.d(TAG, "Intentando autenticar con email: $SENDER_EMAIL")
                Log.d(TAG, "Email destino: ${config.emailDestino}")

                session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, password)
                    }
                })

                Log.d(TAG, "Creando mensaje...")
                val fullMessage = """
                    $message
                    
                    Remitente: $userEmail
                """.trimIndent()

                val mimeMessage = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.emailDestino))
                    this.subject = subject
                    setText(fullMessage)
                }

                Log.d(TAG, "Enviando mensaje directamente...")
                Transport.send(mimeMessage)
                Log.d(TAG, "Mensaje enviado correctamente")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar email: ${e.message}", e)
                onFailure(e)
                false
            }
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                onSuccess()
            }
        }
    }
} 