package com.tfg.umeegunero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.EmailService
import com.tfg.umeegunero.util.NetworkUtils
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.SendGridDiagnostic
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

class EmailTestActivity : ComponentActivity() {
    
    @Inject
    lateinit var emailService: EmailService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar manualmente el servicio si no se puede inyectar
        if (!::emailService.isInitialized) {
            emailService = EmailService()
        }
        
        setContent {
            UmeEguneroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EmailTestScreen()
                }
            }
        }
    }
    
    @Composable
    fun EmailTestScreen() {
        var destinatario by remember { mutableStateOf("nojabeach@gmail.com") }
        var estadoEnvio by remember { mutableStateOf("") }
        var enviando by remember { mutableStateOf(false) }
        var networkStatus by remember { mutableStateOf("") }
        var diagnosticResult by remember { mutableStateOf<SendGridDiagnostic.DiagnosticResult?>(null) }
        var runningDiagnostic by remember { mutableStateOf(false) }
        
        // Estados para controlar las operaciones
        var doSendEmail by remember { mutableStateOf(false) }
        var doSendNotification by remember { mutableStateOf(false) }
        var doRunDiagnostic by remember { mutableStateOf(false) }
        
        val context = LocalContext.current
        
        LaunchedEffect(Unit) {
            // Verificar estado de red al iniciar
            networkStatus = if (NetworkUtils.isNetworkAvailable(context)) {
                "Conectado a Internet"
            } else {
                "Sin conexión a Internet"
            }
            
            // Verificar conectividad con SendGrid
            if (NetworkUtils.checkSendGridConnectivity()) {
                networkStatus += " - SendGrid accesible"
            } else {
                networkStatus += " - SendGrid no accesible"
            }
        }
        
        // LaunchedEffect para enviar email simple
        LaunchedEffect(doSendEmail) {
            if (doSendEmail) {
                try {
                    // Verificar conectividad con SendGrid
                    val sendGridConnected = NetworkUtils.checkSendGridConnectivity()
                    if (!sendGridConnected) {
                        estadoEnvio = "Error: No se puede conectar con los servidores de SendGrid"
                        enviando = false
                        doSendEmail = false
                        return@LaunchedEffect
                    }
                    
                    Timber.d("Iniciando envío de email a $destinatario")
                    val resultado = emailService.sendEmail(
                        to = destinatario,
                        subject = "Prueba de correo desde UmeEgunero",
                        message = """
                            <h1>¡Prueba exitosa!</h1>
                            <p>Este es un correo de prueba enviado desde UmeEgunero.</p>
                            <p>La configuración de SendGrid ha sido completada correctamente.</p>
                            <p>Saludos,<br>El equipo de UmeEgunero</p>
                        """.trimIndent()
                    )
                    
                    when (resultado) {
                        is Result.Success -> {
                            estadoEnvio = "¡Correo enviado exitosamente!\nCódigo: ${resultado.data.statusCode}"
                        }
                        is Result.Error -> {
                            val errorMsg = resultado.exception?.message ?: "Error desconocido"
                            estadoEnvio = "Error al enviar correo: $errorMsg"
                            Timber.e(resultado.exception, "Error al enviar email")
                        }
                        else -> {
                            estadoEnvio = "Resultado inesperado"
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Excepción durante el envío de email")
                    estadoEnvio = "Excepción: ${e.message}"
                } finally {
                    enviando = false
                    doSendEmail = false
                }
            }
        }
        
        // LaunchedEffect para enviar notificación
        LaunchedEffect(doSendNotification) {
            if (doSendNotification) {
                try {
                    val resultado = emailService.sendVinculacionNotification(
                        to = destinatario,
                        isApproved = true,
                        alumnoNombre = "Roberto García",
                        centroNombre = "IES UmeEgunero"
                    )
                    
                    when (resultado) {
                        is Result.Success -> {
                            estadoEnvio = "¡Notificación enviada exitosamente!\nCódigo: ${resultado.data.statusCode}"
                        }
                        is Result.Error -> {
                            estadoEnvio = "Error al enviar notificación: ${resultado.exception?.message}"
                        }
                        else -> {
                            estadoEnvio = "Resultado inesperado"
                        }
                    }
                } catch (e: Exception) {
                    estadoEnvio = "Excepción: ${e.message}"
                } finally {
                    enviando = false
                    doSendNotification = false
                }
            }
        }
        
        // LaunchedEffect para ejecutar diagnóstico
        LaunchedEffect(doRunDiagnostic) {
            if (doRunDiagnostic) {
                try {
                    // Usar la misma API key y dirección que el servicio de correo
                    val result = SendGridDiagnostic.runDiagnostic(
                        context = context, 
                        apiKey = emailService.getApiKey(),
                        sender = emailService.getFromEmail()
                    )
                    diagnosticResult = result
                    
                    if (result.error != null) {
                        estadoEnvio = "Diagnóstico completado - Error: ${result.error}"
                    } else {
                        estadoEnvio = "Diagnóstico completado - Todo OK"
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al ejecutar diagnóstico")
                    estadoEnvio = "Error en diagnóstico: ${e.message}"
                } finally {
                    runningDiagnostic = false
                    doRunDiagnostic = false
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Prueba de Envío de Email",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estado de la red
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (networkStatus.contains("no accesible") || networkStatus.contains("Sin conexión")) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = networkStatus,
                    modifier = Modifier.padding(16.dp),
                    color = if (networkStatus.contains("no accesible") || networkStatus.contains("Sin conexión"))
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = destinatario,
                onValueChange = { destinatario = it },
                label = { Text("Dirección de correo") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    // Verificar conectividad antes de enviar
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        estadoEnvio = "Error: No hay conexión a Internet"
                        return@Button
                    }
                    
                    enviando = true
                    estadoEnvio = "Verificando conexión y enviando correo a $destinatario..."
                    doSendEmail = true
                },
                enabled = !enviando && destinatario.contains("@"),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar correo simple")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    enviando = true
                    estadoEnvio = "Enviando notificación a $destinatario..."
                    doSendNotification = true
                },
                enabled = !enviando && destinatario.contains("@"),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar notificación vinculación")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón para ejecutar diagnóstico
            Button(
                onClick = {
                    runningDiagnostic = true
                    estadoEnvio = "Ejecutando diagnóstico completo de SendGrid..."
                    doRunDiagnostic = true
                },
                enabled = !enviando && !runningDiagnostic,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Ejecutar diagnóstico completo")
            }
            
            // Mostrar resultado del diagnóstico si existe
            diagnosticResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.error != null) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultado del diagnóstico:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        DiagnosticResultItem("Internet disponible", result.isInternetAvailable)
                        DiagnosticResultItem("Resolución DNS OK", result.canResolveHost)
                        DiagnosticResultItem("Conexión a SendGrid", result.canConnectToSendGrid)
                        DiagnosticResultItem("API Key válida", result.apiKeyValid)
                        DiagnosticResultItem("Remitente verificado", result.senderVerified)
                        
                        if (result.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error: ${result.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (enviando || runningDiagnostic) {
                CircularProgressIndicator()
            }
            
            Text(
                text = estadoEnvio,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
    
    @Composable
    private fun DiagnosticResultItem(label: String, isSuccess: Boolean) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) 
                    Icons.Default.CheckCircle 
                else 
                    Icons.Default.Cancel,
                contentDescription = if (isSuccess) "Éxito" else "Fallo",
                tint = if (isSuccess) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 