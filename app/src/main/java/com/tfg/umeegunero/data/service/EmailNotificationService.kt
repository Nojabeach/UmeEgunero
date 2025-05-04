package com.tfg.umeegunero.data.service

import android.net.Uri
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ScriptResponse(
    val status: String,
    val message: String
)

enum class TipoPlantilla {
    NINGUNA, BIENVENIDA, APROBACION, RECHAZO, RECORDATORIO // Añadir otros tipos si es necesario
}

/**
 * Servicio para enviar notificaciones por email usando un Google Apps Script.
 */
@Singleton
class EmailNotificationService @Inject constructor(
    private val httpClient: HttpClient
) {

    // TODO: Externalizar URL y posible token de seguridad
    private val scriptUrlBase = "https://script.google.com/macros/s/AKfycbxvSugtN4a3LReAIYuZd6A2MIno8UkMGHleqIXsZg7vcGVGxTYMUP9efPbrkEbsxj6DLA/exec"

    suspend fun sendEmail(
        destinatario: String,
        nombre: String,
        tipoPlantilla: TipoPlantilla,
        asuntoPersonalizado: String? = null
    ): Boolean {
        if (tipoPlantilla == TipoPlantilla.NINGUNA) {
            Timber.w("Intento de envío de email sin plantilla seleccionada.")
            return false
        }

        val nombreParaEmail = nombre.ifBlank { "Usuario/a" }
        val asunto = asuntoPersonalizado ?: run {
            val tipoAsunto = tipoPlantilla.name.lowercase().replaceFirstChar { it.titlecase() }
            "UmeEgunero: ${tipoAsunto} para ${nombreParaEmail}"
        }

        val urlConParams = try {
            Uri.parse(scriptUrlBase)
                .buildUpon()
                .appendQueryParameter("destinatario", destinatario)
                .appendQueryParameter("asunto", asunto)
                .appendQueryParameter("nombre", nombreParaEmail)
                .appendQueryParameter("tipoPlantilla", tipoPlantilla.name)
                // Futuro: Añadir token de seguridad aquí
                // .appendQueryParameter("token", "TU_TOKEN_SECRETO")
                .build()
                .toString()
        } catch (e: Exception) {
            Timber.e(e, "Error construyendo URL para envío de email")
            return false
        }

        Timber.d("Intentando enviar email ($tipoPlantilla) a $destinatario vía Apps Script: $urlConParams")

        return try {
            val response: ScriptResponse = withContext(Dispatchers.IO) {
                Timber.d("Ejecutando llamada GET en ${Thread.currentThread().name}")
                httpClient.get(urlConParams).body()
            }
            Timber.i("Respuesta del script: Status=${response.status}, Message=${response.message}")
            response.status == "OK"
        } catch (e: Exception) {
            Timber.e(e, "Error en llamada a Apps Script para enviar email")
            false
        }
    }

    // Funciones específicas para plantillas comunes
    suspend fun sendWelcomeEmail(email: String, nombre: String): Boolean {
        return sendEmail(email, nombre, TipoPlantilla.BIENVENIDA)
    }

    suspend fun sendApprovalEmail(email: String, nombre: String): Boolean {
        return sendEmail(email, nombre, TipoPlantilla.APROBACION)
    }

    suspend fun sendRejectionEmail(email: String, nombre: String, motivo: String? = null): Boolean {
        // El asunto podría incluir el motivo si se pasa
        val asunto = "UmeEgunero: Solicitud Rechazada" + (motivo?.let { " - Motivo: $it" } ?: "")
        return sendEmail(email, nombre, TipoPlantilla.RECHAZO, asuntoPersonalizado = asunto)
    }

    suspend fun sendReminderEmail(email: String, nombre: String, mensajeRecordatorio: String): Boolean {
        val asunto = "UmeEgunero: Recordatorio Importante"
        // Podríamos pasar el mensajeRecordatorio como parámetro adicional al script si fuera necesario
        return sendEmail(email, nombre, TipoPlantilla.RECORDATORIO, asuntoPersonalizado = asunto)
    }
} 