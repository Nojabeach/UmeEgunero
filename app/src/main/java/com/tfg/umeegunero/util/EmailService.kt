package com.tfg.umeegunero.util

import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Attachments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.IOException

/**
 * Servicio para el envío de correos electrónicos utilizando SendGrid.
 *
 * Este servicio encapsula la funcionalidad de envío de emails y proporciona
 * métodos para enviar correos electrónicos de notificación y comunicación con
 * los usuarios del sistema.
 *
 * @property apiKey API Key de SendGrid para autenticación
 * @property fromEmail Dirección de correo electrónico del remitente
 * @property fromName Nombre del remitente que aparecerá en los correos
 */
@Singleton
class EmailService @Inject constructor() {
    
    // Configuración de SendGrid
    private var apiKey = "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" // API Key configurada
    private var fromEmail = "umeegunero@gmail.com" // Correo oficial de la empresa
    private val fromName = "Centro Educativo UmeEgunero" // Nombre que aparecerá como remitente
    
    // Constructor para pruebas y diagnóstico
    constructor(customApiKey: String, customFromEmail: String) : this() {
        apiKey = customApiKey
        fromEmail = customFromEmail
    }
    
    // IMPORTANTE: Antes de usar SendGrid en producción:
    // 1. Accede a tu cuenta de SendGrid: https://app.sendgrid.com
    // 2. Ve a Settings > Sender Authentication
    // 3. Verifica tu dirección de correo electrónico (Single Sender Verification) o configura
    //    la verificación de dominio completo para mayor seguridad y evitar problemas de spam
    // 4. Configura los registros SPF y DKIM para mejorar la entregabilidad de los correos
    // 5. Asegúrate de cumplir con las políticas anti-spam y la normativa RGPD
    
    /**
     * Envía un correo electrónico simple con asunto y mensaje.
     *
     * @param to Dirección de correo electrónico del destinatario
     * @param subject Asunto del correo electrónico
     * @param message Contenido del mensaje en formato HTML o texto plano
     * @param attachments Lista de archivos a adjuntar (opcional)
     * @return Resultado de la operación (éxito o error)
     */
    suspend fun sendEmail(
        to: String, 
        subject: String, 
        message: String,
        attachments: List<File>? = null
    ): Result<Response> {
        return withContext(Dispatchers.IO) {
            try {
                // Log inicio de la operación
                Timber.d("Iniciando envío de email a $to con asunto '$subject'")
                
                // Configurar el remitente y destinatario
                val from = Email(fromEmail, fromName)
                val recipient = Email(to)
                
                // Crear el contenido del correo (HTML para mejor formato)
                val content = Content("text/html", formatMessage(message))
                
                // Construir el objeto Mail de SendGrid
                val mail = Mail(from, subject, recipient, content)
                Timber.d("Mail objeto creado correctamente")
                
                // Añadir archivos adjuntos si existen
                attachments?.forEach { file ->
                    try {
                        if (file.exists() && file.isFile) {
                            val attachmentContent = file.readBytes()
                            val attachment = Attachments()
                            
                            // Configurar el adjunto
                            attachment.setContent(Base64.getEncoder().encodeToString(attachmentContent))
                            attachment.setFilename(file.name)
                            attachment.setType(getMimeType(file.name))
                            attachment.setDisposition("attachment")
                            
                            // Añadir al email
                            mail.addAttachments(attachment)
                            Timber.d("Archivo adjunto añadido: ${file.name}")
                        } else {
                            Timber.w("No se pudo adjuntar archivo: ${file.absolutePath} (no existe o no es archivo)")
                        }
                    } catch (e: IOException) {
                        Timber.e(e, "Error al adjuntar archivo: ${file.name}")
                    }
                }
                
                // Configurar y ejecutar la solicitud a la API de SendGrid
                Timber.d("Configurando SendGrid con API key: ${apiKey.substring(0, 15)}...")
                val sendGrid = SendGrid(apiKey)
                val request = Request()
                request.method = Method.POST
                request.endpoint = "mail/send"
                request.body = mail.build()
                
                // Log antes de enviar
                Timber.d("Enviando solicitud a SendGrid. Endpoint: ${request.endpoint}, Método: ${request.method}")
                
                try {
                    // Enviar el correo y obtener la respuesta
                    val response = sendGrid.api(request)
                    
                    // Registrar el resultado detallado
                    Timber.d("Respuesta de SendGrid recibida. Código: ${response.statusCode}")
                    Timber.d("Cuerpo de respuesta: ${response.body}")
                    Timber.d("Headers de respuesta: ${response.headers}")
                    
                    if (response.statusCode in 200..299) {
                        Timber.d("Email enviado correctamente a $to")
                        Result.Success(response)
                    } else {
                        Timber.e("Error al enviar email: ${response.statusCode} - ${response.body}")
                        Result.Error(Exception("Error al enviar email: Código ${response.statusCode}. Detalle: ${response.body}"))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Excepción en la comunicación con SendGrid API")
                    Result.Error(e)
                }
            } catch (e: Exception) {
                Timber.e(e, "Excepción general al enviar email a $to")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Envía un email de notificación de solicitud de vinculación.
     *
     * @param to Email del destinatario (familiar)
     * @param isApproved True si la solicitud fue aprobada, false si fue rechazada
     * @param alumnoNombre Nombre del alumno (opcional)
     * @param centroNombre Nombre del centro educativo
     * @return Resultado de la operación
     */
    suspend fun sendVinculacionNotification(
        to: String,
        isApproved: Boolean,
        alumnoNombre: String? = null,
        centroNombre: String = "Centro Educativo UmeEgunero"
    ): Result<Response> {
        val subject = if (isApproved) 
            "Solicitud de vinculación aprobada - $centroNombre" 
        else 
            "Solicitud de vinculación rechazada - $centroNombre"
        
        val studentInfo = alumnoNombre?.let { "con el alumno/a <strong>$it</strong> " } ?: ""
        
        val message = if (isApproved) {
            """
            <h2>¡Su solicitud ha sido aprobada!</h2>
            <p>Estimado/a familiar,</p>
            <p>Nos complace informarle que su solicitud de vinculación ${studentInfo}ha sido <strong>aprobada</strong>.</p>
            <p>A partir de ahora, podrá acceder a toda la información del alumno desde la aplicación UmeEgunero.</p>
            <p>Si tiene alguna duda, no dude en contactar con el centro educativo.</p>
            <p>Un cordial saludo,<br>
            El equipo de $centroNombre</p>
            """
        } else {
            """
            <h2>Su solicitud ha sido rechazada</h2>
            <p>Estimado/a familiar,</p>
            <p>Lamentamos informarle que su solicitud de vinculación ${studentInfo}ha sido <strong>rechazada</strong>.</p>
            <p>Esto puede deberse a diversos motivos. Le recomendamos contactar directamente con el centro educativo
            para obtener más información al respecto.</p>
            <p>Un cordial saludo,<br>
            El equipo de $centroNombre</p>
            """
        }
        
        return sendEmail(to, subject, message)
    }
    
    /**
     * Envía un email de bienvenida al usuario recién registrado.
     *
     * @param to Email del destinatario
     * @param nombreUsuario Nombre del usuario
     * @param tipoUsuario Tipo de usuario (Profesor, Familiar, etc.)
     * @param centroNombre Nombre del centro educativo
     * @return Resultado de la operación
     */
    suspend fun sendWelcomeEmail(
        to: String,
        nombreUsuario: String,
        tipoUsuario: String,
        centroNombre: String = "Centro Educativo UmeEgunero"
    ): Result<Response> {
        val subject = "Bienvenido/a a UmeEgunero - $centroNombre"
        
        val message = """
        <h2>¡Bienvenido/a a UmeEgunero!</h2>
        <p>Estimado/a <strong>$nombreUsuario</strong>,</p>
        <p>Le damos la bienvenida a la plataforma UmeEgunero de $centroNombre. Su cuenta ha sido creada exitosamente como <strong>$tipoUsuario</strong>.</p>
        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <h3 style="margin-top: 0; color: #0066cc;">¿Qué puede hacer ahora?</h3>
            <ul>
                <li>Iniciar sesión en la aplicación con su correo electrónico</li>
                <li>Completar su perfil con información adicional</li>
                <li>Explorar las funcionalidades disponibles según su rol</li>
                <li>Contactar con el centro si necesita más información</li>
            </ul>
        </div>
        <p>Si tiene cualquier duda o necesita asistencia, no dude en contactar con el soporte técnico o la administración del centro.</p>
        <p>Un cordial saludo,<br>
        El equipo de $centroNombre</p>
        """
        
        return sendEmail(to, subject, message)
    }
    
    /**
     * Envía un email para recuperación de contraseña.
     *
     * @param to Email del destinatario
     * @param nombreUsuario Nombre del usuario
     * @param resetToken Token de restablecimiento o enlace (opcional)
     * @param centroNombre Nombre del centro educativo
     * @return Resultado de la operación
     */
    suspend fun sendPasswordRecoveryEmail(
        to: String,
        nombreUsuario: String,
        resetToken: String? = null,
        centroNombre: String = "Centro Educativo UmeEgunero"
    ): Result<Response> {
        val subject = "Recuperación de contraseña - $centroNombre"
        
        val tokenInfo = resetToken?.let {
            """
            <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: center;">
                <p>Utilice el siguiente código para restablecer su contraseña:</p>
                <h2 style="color: #0066cc; letter-spacing: 5px; font-family: monospace; padding: 10px; background: #e6e6e6; display: inline-block; border-radius: 4px;">$it</h2>
                <p><small>Este código es válido durante 24 horas.</small></p>
            </div>
            """
        } ?: ""
        
        val message = """
        <h2>Recuperación de contraseña</h2>
        <p>Estimado/a <strong>$nombreUsuario</strong>,</p>
        <p>Hemos recibido una solicitud para restablecer la contraseña de su cuenta en la plataforma UmeEgunero.</p>
        $tokenInfo
        <p>Si usted no ha solicitado este cambio, puede ignorar este mensaje o contactar con el soporte técnico.</p>
        <p>Un cordial saludo,<br>
        El equipo de $centroNombre</p>
        """
        
        return sendEmail(to, subject, message)
    }
    
    /**
     * Envía una notificación de incidencia a los padres.
     *
     * @param to Email del destinatario (familiar)
     * @param alumnoNombre Nombre del alumno
     * @param tipoIncidencia Tipo de incidencia (Comportamiento, Salud, etc.)
     * @param descripcion Descripción detallada de la incidencia
     * @param fecha Fecha de la incidencia en formato legible
     * @param profesorNombre Nombre del profesor que registra la incidencia
     * @param centroNombre Nombre del centro educativo
     * @return Resultado de la operación
     */
    suspend fun sendIncidenciaNotification(
        to: String,
        alumnoNombre: String,
        tipoIncidencia: String,
        descripcion: String,
        fecha: String,
        profesorNombre: String,
        centroNombre: String = "Centro Educativo UmeEgunero"
    ): Result<Response> {
        val subject = "Notificación de incidencia: $tipoIncidencia - $centroNombre"
        
        val message = """
        <h2>Notificación de incidencia</h2>
        <p>Estimado/a familiar,</p>
        <p>Le informamos que se ha registrado una incidencia relacionada con el alumno/a <strong>$alumnoNombre</strong>.</p>
        
        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <h3 style="margin-top: 0; color: #e63946;">Detalles de la incidencia:</h3>
            <p><strong>Tipo:</strong> $tipoIncidencia</p>
            <p><strong>Fecha:</strong> $fecha</p>
            <p><strong>Profesor/a:</strong> $profesorNombre</p>
            <p><strong>Descripción:</strong></p>
            <p style="background-color: white; padding: 10px; border-left: 4px solid #e63946; margin-left: 10px;">$descripcion</p>
        </div>
        
        <p>Por favor, revise esta información y considere contactar con el centro educativo si desea discutir este asunto o necesita más detalles.</p>
        <p>Un cordial saludo,<br>
        El equipo de $centroNombre</p>
        """
        
        return sendEmail(to, subject, message)
    }
    
    /**
     * Envía una notificación de nueva tarea asignada a los padres.
     *
     * @param to Email del destinatario (familiar)
     * @param alumnoNombre Nombre del alumno
     * @param tituloTarea Título de la tarea
     * @param descripcionTarea Descripción de la tarea
     * @param fechaEntrega Fecha límite de entrega en formato legible
     * @param profesorNombre Nombre del profesor que asigna la tarea
     * @param asignatura Nombre de la asignatura
     * @param centroNombre Nombre del centro educativo
     * @param attachments Archivos adjuntos relacionados con la tarea
     * @return Resultado de la operación
     */
    suspend fun sendNuevaTareaNotification(
        to: String,
        alumnoNombre: String,
        tituloTarea: String,
        descripcionTarea: String,
        fechaEntrega: String,
        profesorNombre: String,
        asignatura: String,
        centroNombre: String = "Centro Educativo UmeEgunero",
        attachments: List<File>? = null
    ): Result<Response> {
        val subject = "Nueva tarea: $tituloTarea - $asignatura"
        
        val message = """
        <h2>Nueva tarea asignada</h2>
        <p>Estimado/a familiar de <strong>$alumnoNombre</strong>,</p>
        <p>Le informamos que se ha asignado una nueva tarea para su hijo/a:</p>
        
        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
            <h3 style="margin-top: 0; color: #0066cc;">Detalles de la tarea:</h3>
            <p><strong>Título:</strong> $tituloTarea</p>
            <p><strong>Asignatura:</strong> $asignatura</p>
            <p><strong>Profesor/a:</strong> $profesorNombre</p>
            <p><strong>Fecha de entrega:</strong> $fechaEntrega</p>
            <p><strong>Descripción:</strong></p>
            <div style="background-color: white; padding: 10px; border-left: 4px solid #0066cc; margin-left: 10px;">
                $descripcionTarea
            </div>
        </div>
        
        <p>Puede consultar todos los detalles y materiales relacionados con esta tarea en la aplicación UmeEgunero.</p>
        <p>Un cordial saludo,<br>
        El equipo de $centroNombre</p>
        """
        
        return sendEmail(to, subject, message, attachments)
    }
    
    /**
     * Da formato al mensaje con una plantilla básica HTML.
     *
     * @param message Contenido del mensaje
     * @return Mensaje formateado con HTML
     */
    private fun formatMessage(message: String): String {
        // Si el mensaje ya tiene formato HTML, lo devolvemos tal cual
        if (message.trim().startsWith("<")) {
            return message
        }
        
        // Si no, lo formateamos con una plantilla simple
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { 
                    font-family: Arial, sans-serif; 
                    margin: 0; 
                    padding: 20px; 
                    color: #333; 
                    line-height: 1.5;
                }
                .container { 
                    max-width: 600px; 
                    margin: 0 auto; 
                    background-color: #f9f9f9; 
                    padding: 20px; 
                    border-radius: 5px;
                    border: 1px solid #e0e0e0;
                }
                .header {
                    background-color: #0066cc;
                    color: white;
                    padding: 10px 20px;
                    border-radius: 4px 4px 0 0;
                    margin: -20px -20px 20px -20px;
                }
                .footer { 
                    margin-top: 30px; 
                    font-size: 12px; 
                    color: #666; 
                    text-align: center;
                    border-top: 1px solid #e0e0e0;
                    padding-top: 20px;
                }
                h2 { 
                    color: #0066cc; 
                    margin-top: 0;
                }
                a { 
                    color: #0066cc; 
                    text-decoration: none;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h2 style="color: white; margin: 0;">UmeEgunero</h2>
                </div>
                $message
                <div class="footer">
                    <p>Este correo ha sido enviado automáticamente por el Centro Educativo UmeEgunero.</p>
                    <p>&copy; ${java.time.Year.now().value} UmeEgunero. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
        """
    }
    
    /**
     * Determina el tipo MIME basado en la extensión del archivo.
     *
     * @param fileName Nombre del archivo
     * @return Tipo MIME correspondiente
     */
    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        
        return when (extension) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "txt" -> "text/plain"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            else -> "application/octet-stream" // Tipo genérico
        }
    }

    /**
     * Obtiene la API key de SendGrid para diagnóstico
     * @return API key
     */
    fun getApiKey(): String = apiKey

    /**
     * Obtiene el email del remitente para diagnóstico
     * @return Dirección de email del remitente
     */
    fun getFromEmail(): String = fromEmail
} 