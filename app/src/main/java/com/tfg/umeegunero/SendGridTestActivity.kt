package com.tfg.umeegunero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.EmailService
import com.tfg.umeegunero.util.NetworkUtils
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.SendGridDiagnostic
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Actividad dedicada al diagnóstico y pruebas de SendGrid
 * 
 * Esta actividad permite realizar pruebas exhaustivas de conectividad con SendGrid
 * y diagnóstico de posibles problemas para el envío de correos electrónicos.
 */
class SendGridTestActivity : ComponentActivity() {
    
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
                    SendGridTestScreen(emailService)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendGridTestScreen(emailService: EmailService) {
    val context = LocalContext.current
    
    // Estados para la UI
    var apiKey by remember { mutableStateOf(emailService.getApiKey()) }
    var fromEmail by remember { mutableStateOf(emailService.getFromEmail()) }
    var destinatario by remember { mutableStateOf("nojabeach@gmail.com") }
    var logs by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var diagnosticResult by remember { mutableStateOf<SendGridDiagnostic.DiagnosticResult?>(null) }
    
    // Estado para controlar las operaciones
    var executeDiagnostic by remember { mutableStateOf(false) }
    var sendTestEmail by remember { mutableStateOf(false) }
    
    // Función para agregar entradas de log
    fun addLog(message: String) {
        logs = "$message\n$logs"
        Timber.d(message)
    }
    
    LaunchedEffect(Unit) {
        // Verificar estado de la red al iniciar
        addLog("Iniciando verificación de red...")
        connectionStatus = if (NetworkUtils.isNetworkAvailable(context)) {
            addLog("✓ Conexión a Internet disponible")
            "Conectado a Internet"
        } else {
            addLog("✗ Sin conexión a Internet")
            "Sin conexión a Internet"
        }
    }
    
    // LaunchedEffect para ejecutar el diagnóstico
    LaunchedEffect(executeDiagnostic) {
        if (executeDiagnostic) {
            try {
                addLog("Iniciando diagnóstico completo...")
                val result = SendGridDiagnostic.runDiagnostic(
                    context = context,
                    apiKey = apiKey,
                    sender = fromEmail
                )
                
                diagnosticResult = result
                
                addLog("--- Resultados del diagnóstico ---")
                addLog("Internet: ${if(result.isInternetAvailable) "✓" else "✗"}")
                addLog("DNS: ${if(result.canResolveHost) "✓" else "✗"}")
                addLog("TCP: ${if(result.canConnectToSendGrid) "✓" else "✗"}")
                addLog("API Key: ${if(result.apiKeyValid) "✓" else "✗"}")
                addLog("Remitente: ${if(result.senderVerified) "✓" else "✗"}")
                
                if (result.error != null) {
                    addLog("ERROR: ${result.error}")
                } else {
                    addLog("✓ Diagnóstico completado sin errores")
                }
            } catch (e: Exception) {
                addLog("✗ Error en diagnóstico: ${e.message}")
                Timber.e(e, "Error en diagnóstico")
            } finally {
                loading = false
                executeDiagnostic = false
            }
        }
    }
    
    // LaunchedEffect para enviar el email de prueba
    LaunchedEffect(sendTestEmail) {
        if (sendTestEmail) {
            try {
                addLog("Enviando email de prueba a $destinatario...")
                // Crear un servicio temporal con la configuración actual
                val tempService = EmailService(apiKey, fromEmail)
                
                val resultado = tempService.sendEmail(
                    to = destinatario,
                    subject = "Prueba de diagnóstico SendGrid",
                    message = """
                        <h1>Prueba de diagnóstico SendGrid</h1>
                        <p>Este es un correo de prueba enviado desde la herramienta de diagnóstico de SendGrid.</p>
                        <p>Si estás recibiendo este correo, significa que la configuración de SendGrid es correcta.</p>
                        <p>Timestamp: ${System.currentTimeMillis()}</p>
                    """.trimIndent()
                )
                
                when (resultado) {
                    is Result.Success -> {
                        addLog("✓ Correo enviado exitosamente")
                        addLog("Código: ${resultado.data.statusCode}")
                        addLog("Respuesta: ${resultado.data.body}")
                    }
                    is Result.Error -> {
                        addLog("✗ Error al enviar correo")
                        addLog("${resultado.exception?.message}")
                    }
                    else -> {
                        addLog("? Resultado inesperado")
                    }
                }
            } catch (e: Exception) {
                addLog("✗ Excepción: ${e.message}")
                Timber.e(e, "Error al enviar email de prueba")
            } finally {
                loading = false
                sendTestEmail = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "SendGrid Test & Diagnostics",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mostrar estado de conexión
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (connectionStatus.contains("Sin conexión")) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = connectionStatus,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (connectionStatus.contains("Sin conexión"))
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Campos para configuración
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = fromEmail,
            onValueChange = { fromEmail = it },
            label = { Text("Email Remitente") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = destinatario,
            onValueChange = { destinatario = it },
            label = { Text("Email Destinatario") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botones de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón de diagnóstico
            Button(
                onClick = {
                    loading = true
                    executeDiagnostic = true
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Ejecutar diagnóstico")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botón de envío de prueba
            Button(
                onClick = {
                    loading = true
                    sendTestEmail = true
                },
                enabled = !loading && destinatario.contains("@"),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
                Spacer(Modifier.width(4.dp))
                Text("Enviar prueba")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mostrar resultado de diagnóstico si existe
        diagnosticResult?.let { result ->
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
                    
                    DiagnosticResultRow("Internet disponible", result.isInternetAvailable)
                    DiagnosticResultRow("Resolución DNS OK", result.canResolveHost)
                    DiagnosticResultRow("Conexión a SendGrid", result.canConnectToSendGrid)
                    DiagnosticResultRow("API Key válida", result.apiKeyValid)
                    DiagnosticResultRow("Remitente verificado", result.senderVerified)
                    
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Área de logs
        Text(
            text = "Logs:",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = logs,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun DiagnosticResultRow(label: String, isSuccess: Boolean) {
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