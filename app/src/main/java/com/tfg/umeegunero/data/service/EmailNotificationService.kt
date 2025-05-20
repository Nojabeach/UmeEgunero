package com.tfg.umeegunero.data.service

import android.content.Context
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
import com.tfg.umeegunero.util.Constants
import com.tfg.umeegunero.util.security.SecureTokenManager
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Respuesta serializable del script de Google Apps Script para las notificaciones por email.
 * 
 * @property status Estado de la respuesta ("OK" o "ERROR")
 * @property message Mensaje descriptivo del resultado
 */
@Serializable
data class ScriptResponse(
    val status: String,
    val message: String
)

/**
 * Tipos de plantillas de email disponibles en el sistema.
 * 
 * Cada tipo de plantilla corresponde a un formato predefinido en el script de Google Apps Script
 * que determina el diseño y contenido del correo electrónico enviado.
 */
enum class TipoPlantilla {
    /**
     * Sin plantilla, utiliza el contenido personalizado proporcionado
     */
    NINGUNA, 
    
    /**
     * Plantilla de bienvenida para nuevos usuarios
     */
    BIENVENIDA, 
    
    /**
     * Plantilla para notificar la aprobación de una solicitud
     */
    APROBACION, 
    
    /**
     * Plantilla para notificar el rechazo de una solicitud
     */
    RECHAZO, 
    
    /**
     * Plantilla para recordatorios importantes
     */
    RECORDATORIO 
}

/**
 * Servicio para enviar notificaciones por email usando un Google Apps Script.
 * 
 * Esta clase se encarga de la comunicación con un endpoint de Google Apps Script
 * que procesa y envía emails utilizando plantillas predefinidas.
 * Utiliza KTor Client para realizar las peticiones HTTP al servicio externo.
 * Implementa seguridad robusta con tokens firmados y encriptados.
 */
@Singleton
class EmailNotificationService @Inject constructor(
    private val httpClient: HttpClient,
    private val secureTokenManager: SecureTokenManager,
    @ApplicationContext private val context: Context
) {
    // URL del servicio desde Constants
    private val scriptUrlBase = Constants.EMAIL_SCRIPT_URL
    private val appId = Constants.APP_ID
    
    // Nombre de la clave para el email_token en manifest
    private val EMAIL_TOKEN_KEY = "com.tfg.umeegunero.EMAIL_TOKEN_FALLBACK"
    
    // Cache del token para evitar generarlo en cada petición
    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0

    /**
     * Envía un email utilizando el servicio de Google Apps Script.
     * 
     * Implementa generación segura de tokens y validación mejorada.
     * 
     * @param destinatario Dirección de email del destinatario
     * @param nombre Nombre del destinatario para personalizar el mensaje
     * @param tipoPlantilla Tipo de plantilla a utilizar para el formato del email
     * @param asuntoPersonalizado Asunto personalizado (opcional, si es null se genera automáticamente)
     * @param contenido Contenido personalizado del mensaje (opcional)
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun sendEmail(
        destinatario: String,
        nombre: String,
        tipoPlantilla: TipoPlantilla,
        asuntoPersonalizado: String? = null,
        contenido: String? = null
    ): Boolean {
        // Validación de parámetros
        if (destinatario.isBlank()) {
            Timber.w("Error: no se puede enviar email con destinatario vacío")
            return false
        }

        // Si el tipo es NINGUNA y no hay contenido, usar BIENVENIDA por defecto
        val plantillaEfectiva = if (tipoPlantilla == TipoPlantilla.NINGUNA && contenido.isNullOrBlank()) {
            Timber.w("Cambiando tipo de plantilla de NINGUNA a BIENVENIDA ya que no se proporcionó contenido")
            TipoPlantilla.BIENVENIDA
        } else {
            tipoPlantilla
        }

        val nombreParaEmail = nombre.ifBlank { "Usuario/a" }
        val asunto = asuntoPersonalizado ?: run {
            val tipoAsunto = plantillaEfectiva.name.lowercase().replaceFirstChar { it.titlecase() }
            "UmeEgunero: ${tipoAsunto} para ${nombreParaEmail}"
        }

        // Obtener token seguro con manejo de errores
        val securityToken = try {
            getSecurityToken()
        } catch (e: Exception) {
            Timber.e(e, "Error fatal al obtener token de seguridad, abortando envío de email")
            return false
        }
        
        // Construir URL con parámetros
        val urlConParams = try {
            Uri.parse(scriptUrlBase)
                .buildUpon()
                .appendQueryParameter("destinatario", destinatario)
                .appendQueryParameter("asunto", asunto)
                .appendQueryParameter("nombre", nombreParaEmail)
                .appendQueryParameter("tipoPlantilla", plantillaEfectiva.name)
                .apply {
                    // Añadir contenido personalizado si existe
                    contenido?.let { appendQueryParameter("contenido", it) }
                    // Añadir token de seguridad
                    appendQueryParameter("token", securityToken)
                    // Añadir timestamp para prevenir ataques de repetición
                    appendQueryParameter("ts", System.currentTimeMillis().toString())
                }
                .build()
                .toString()
        } catch (e: Exception) {
            Timber.e(e, "Error construyendo URL para envío de email")
            return false
        }

        Timber.d("Intentando enviar email ($plantillaEfectiva) a $destinatario vía Apps Script")

        return try {
            val response: ScriptResponse = withContext(Dispatchers.IO) {
                httpClient.get(urlConParams).body()
            }
            
            // Validar respuesta y registrar resultado
            val success = response.status == "OK"
            val logLevel = if (success) Log.INFO else Log.WARN
            
            Timber.log(logLevel, "Respuesta script email: Status=${response.status}, Message=${response.message}")
            
            success
        } catch (e: Exception) {
            Timber.e(e, "Error en llamada a Apps Script para enviar email: ${e.message}")
            false
        }
    }

    /**
     * Obtiene un token de seguridad válido para el servicio de email.
     * 
     * El método intenta usar el SecureTokenManager para generar un token
     * seguro. Si falla, utiliza un token de respaldo del manifest.
     * 
     * @return Token de seguridad válido
     */
    private suspend fun getSecurityToken(): String {
        // Si tenemos un token en caché y aún es válido, lo usamos
        val currentTime = System.currentTimeMillis()
        if (cachedToken != null && currentTime < tokenExpiryTime) {
            return cachedToken!!
        }
        
        return try {
            // Generar un nuevo token con el SecureTokenManager
            val token = secureTokenManager.generateEmailServiceToken(
                appId = appId,
                serviceId = "email_service"
            )
            
            // Guardar en caché (válido por 1 hora)
            cachedToken = token
            tokenExpiryTime = currentTime + (60 * 60 * 1000) // 1 hora
            
            token
        } catch (e: Exception) {
            // En caso de error, usar token de respaldo del manifest
            Timber.e(e, "Error generando token seguro. Usando token de respaldo")
            Constants.Security.getMetadataString(
                context, 
                EMAIL_TOKEN_KEY, 
                "fallback_token_umeegunero_2023"
            )
        }
    }

    /**
     * Envía un email de bienvenida utilizando la plantilla predefinida.
     * 
     * @param email Dirección de email del destinatario
     * @param nombre Nombre del destinatario para personalizar el mensaje
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun sendWelcomeEmail(email: String, nombre: String): Boolean {
        return sendEmail(email, nombre, TipoPlantilla.BIENVENIDA)
    }

    /**
     * Envía un email de aprobación de solicitud utilizando la plantilla predefinida.
     * 
     * @param email Dirección de email del destinatario
     * @param nombre Nombre del destinatario para personalizar el mensaje
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun sendApprovalEmail(email: String, nombre: String): Boolean {
        return sendEmail(email, nombre, TipoPlantilla.APROBACION)
    }

    /**
     * Envía un email de rechazo de solicitud utilizando la plantilla predefinida.
     * 
     * @param email Dirección de email del destinatario
     * @param nombre Nombre del destinatario para personalizar el mensaje
     * @param motivo Motivo opcional del rechazo para incluir en el asunto
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun sendRejectionEmail(email: String, nombre: String, motivo: String? = null): Boolean {
        // El asunto podría incluir el motivo si se pasa
        val asunto = "UmeEgunero: Solicitud Rechazada" + (motivo?.let { " - Motivo: $it" } ?: "")
        return sendEmail(email, nombre, TipoPlantilla.RECHAZO, asuntoPersonalizado = asunto)
    }

    /**
     * Envía un email de recordatorio utilizando la plantilla predefinida.
     * 
     * @param email Dirección de email del destinatario
     * @param nombre Nombre del destinatario para personalizar el mensaje
     * @param mensajeRecordatorio Mensaje específico para el recordatorio
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun sendReminderEmail(email: String, nombre: String, mensajeRecordatorio: String): Boolean {
        val asunto = "UmeEgunero: Recordatorio Importante"
        // Pasar el mensaje de recordatorio como contenido personalizado
        return sendEmail(
            destinatario = email,
            nombre = nombre,
            tipoPlantilla = TipoPlantilla.RECORDATORIO,
            asuntoPersonalizado = asunto,
            contenido = mensajeRecordatorio
        )
    }

    /**
     * Envía un email de notificación específico para el procesamiento de solicitudes.
     * 
     * @param destinatario Dirección de email del destinatario
     * @param nombre Nombre del destinatario para personalizar el mensaje
     * @param esAprobacion Si es true se envía con plantilla de aprobación, si es false con plantilla de rechazo
     * @param nombreAlumno Nombre del alumno para incluir en el asunto y contenido
     * @param observaciones Observaciones opcionales (para rechazos)
     * @param contenidoHtml Contenido HTML personalizado (opcional)
     * @return true si el email se envió correctamente, false en caso contrario
     */
    suspend fun enviarEmailSolicitudProcesada(
        destinatario: String,
        nombre: String,
        esAprobacion: Boolean,
        nombreAlumno: String,
        observaciones: String = "",
        contenidoHtml: String? = null
    ): Boolean {
        val tipoPlantilla = if (esAprobacion) TipoPlantilla.APROBACION else TipoPlantilla.RECHAZO
        val accion = if (esAprobacion) "Aprobada" else "Rechazada"
        val asunto = "Solicitud $accion en UmeEgunero - Vinculación con $nombreAlumno"
        
        // Registrar el intento de envío con logs apropiados
        Timber.d("Intentando enviar email de ${if (esAprobacion) "aprobación" else "rechazo"} a $destinatario para alumno $nombreAlumno")
        
        return sendEmail(
            destinatario = destinatario,
            nombre = nombre,
            tipoPlantilla = tipoPlantilla,
            asuntoPersonalizado = asunto,
            contenido = contenidoHtml
        )
    }
} 