package com.tfg.umeegunero.util

import android.content.Context
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

/**
 * Clase de diagnóstico para problemas de SendGrid
 */
object SendGridDiagnostic {
    
    data class DiagnosticResult(
        val isInternetAvailable: Boolean = false,
        val canResolveHost: Boolean = false,
        val canConnectToSendGrid: Boolean = false,
        val apiKeyValid: Boolean = false,
        val senderVerified: Boolean = false,
        val error: String? = null
    )
    
    /**
     * Realiza un diagnóstico completo de la conexión y configuración de SendGrid
     */
    suspend fun runDiagnostic(context: Context, apiKey: String, sender: String): DiagnosticResult {
        return withContext(Dispatchers.IO) {
            val result = DiagnosticResult()
            val diagnosticBuilder = StringBuilder()
            
            try {
                // 1. Verificar conexión a Internet
                val internetAvailable = NetworkUtils.isNetworkAvailable(context)
                diagnosticBuilder.append("Conexión a Internet: ${if(internetAvailable) "OK" else "FALLO"}\n")
                
                if (!internetAvailable) {
                    return@withContext result.copy(
                        isInternetAvailable = false,
                        error = "No hay conexión a Internet disponible"
                    )
                }
                
                // 2. Verificar resolución DNS
                val canResolveHost = try {
                    val address = InetSocketAddress("api.sendgrid.com", 443)
                    !address.isUnresolved
                } catch (e: Exception) {
                    Timber.e(e, "Error al resolver DNS para SendGrid")
                    false
                }
                
                diagnosticBuilder.append("Resolución DNS SendGrid: ${if(canResolveHost) "OK" else "FALLO"}\n")
                
                if (!canResolveHost) {
                    return@withContext result.copy(
                        isInternetAvailable = true,
                        canResolveHost = false,
                        error = "No se puede resolver el dominio api.sendgrid.com"
                    )
                }
                
                // 3. Verificar conectividad TCP con SendGrid
                val canConnectToSendGrid = try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("api.sendgrid.com", 443), 5000)
                        true
                    }
                } catch (e: IOException) {
                    Timber.e(e, "Error al conectar con SendGrid")
                    false
                }
                
                diagnosticBuilder.append("Conexión TCP a SendGrid: ${if(canConnectToSendGrid) "OK" else "FALLO"}\n")
                
                if (!canConnectToSendGrid) {
                    return@withContext result.copy(
                        isInternetAvailable = true,
                        canResolveHost = true,
                        canConnectToSendGrid = false,
                        error = "No se puede establecer conexión TCP con SendGrid"
                    )
                }
                
                // 4. Verificar API Key
                val apiKeyValid = try {
                    val sg = SendGrid(apiKey)
                    val request = Request()
                    request.method = Method.GET
                    request.endpoint = "scopes"
                    
                    val response = sg.api(request)
                    Timber.d("API Key verificación: ${response.statusCode} - ${response.body}")
                    
                    response.statusCode in 200..299
                } catch (e: Exception) {
                    Timber.e(e, "Error al verificar API Key")
                    false
                }
                
                diagnosticBuilder.append("API Key válida: ${if(apiKeyValid) "OK" else "FALLO"}\n")
                
                if (!apiKeyValid) {
                    return@withContext result.copy(
                        isInternetAvailable = true,
                        canResolveHost = true,
                        canConnectToSendGrid = true,
                        apiKeyValid = false,
                        error = "La API Key no es válida o no tiene los permisos necesarios"
                    )
                }
                
                // 5. Verificar si el remitente está verificado
                val senderVerified = try {
                    val sg = SendGrid(apiKey)
                    val request = Request()
                    request.method = Method.GET
                    request.endpoint = "verified_senders"
                    
                    val response = sg.api(request)
                    Timber.d("Verificación de remitente: ${response.statusCode} - ${response.body}")
                    
                    // Esto es una simplificación, idealmente deberíamos parsear la respuesta JSON
                    response.statusCode in 200..299 && response.body.contains(sender)
                } catch (e: Exception) {
                    Timber.e(e, "Error al verificar remitente")
                    false
                }
                
                diagnosticBuilder.append("Remitente verificado: ${if(senderVerified) "OK" else "FALLO"}\n")
                
                // Resultado completo
                Timber.d("Diagnóstico SendGrid completo:\n$diagnosticBuilder")
                
                return@withContext DiagnosticResult(
                    isInternetAvailable = true,
                    canResolveHost = true,
                    canConnectToSendGrid = true,
                    apiKeyValid = apiKeyValid,
                    senderVerified = senderVerified,
                    error = if (!senderVerified) "El remitente $sender no está verificado en SendGrid" else null
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Error durante el diagnóstico de SendGrid")
                return@withContext DiagnosticResult(
                    error = "Error durante el diagnóstico: ${e.message}"
                )
            }
        }
    }
} 