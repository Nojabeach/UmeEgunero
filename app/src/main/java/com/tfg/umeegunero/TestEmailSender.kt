package com.tfg.umeegunero

import com.tfg.umeegunero.util.EmailService
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Clase utilitaria para probar el envío de correos electrónicos
 * Esta clase no forma parte de la aplicación principal y solo se usa para pruebas
 */
object TestEmailSender {
    private val emailService = EmailService()
    
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("Iniciando prueba de envío de correo...")
        
        try {
            val destinatario = "nojabeach@gmail.com"
            println("Enviando correo a $destinatario...")
            
            // Probar envío de email normal
            val resultado = withContext(Dispatchers.IO) {
                emailService.sendEmail(
                    to = destinatario,
                    subject = "Prueba de correo desde UmeEgunero",
                    message = """
                        <h1>¡Prueba exitosa!</h1>
                        <p>Este es un correo de prueba enviado desde UmeEgunero.</p>
                        <p>La configuración de SendGrid ha sido completada correctamente.</p>
                        <p>Saludos,<br>El equipo de UmeEgunero</p>
                    """.trimIndent()
                )
            }
            
            when (resultado) {
                is Result.Success -> {
                    println("¡Correo enviado exitosamente!")
                    println("Código de respuesta: ${resultado.data.statusCode}")
                    println("Cuerpo de respuesta: ${resultado.data.body}")
                }
                is Result.Error -> {
                    println("Error al enviar correo: ${resultado.exception?.message}")
                    resultado.exception?.printStackTrace()
                }
                else -> {
                    println("Resultado inesperado")
                }
            }
            
            // Probar envío de notificación de vinculación (aprobada)
            println("\nEnviando notificación de vinculación aprobada...")
            val resultadoVinculacion = withContext(Dispatchers.IO) {
                emailService.sendVinculacionNotification(
                    to = destinatario,
                    isApproved = true,
                    alumnoNombre = "Roberto García",
                    centroNombre = "IES UmeEgunero"
                )
            }
            
            when (resultadoVinculacion) {
                is Result.Success -> {
                    println("¡Notificación de vinculación enviada exitosamente!")
                    println("Código de respuesta: ${resultadoVinculacion.data.statusCode}")
                }
                is Result.Error -> {
                    println("Error al enviar notificación: ${resultadoVinculacion.exception?.message}")
                    resultadoVinculacion.exception?.printStackTrace()
                }
                else -> {
                    println("Resultado inesperado")
                }
            }
            
        } catch (e: Exception) {
            println("Excepción al enviar correo: ${e.message}")
            e.printStackTrace()
        }
        
        println("Prueba de envío de correo finalizada.")
    }
} 