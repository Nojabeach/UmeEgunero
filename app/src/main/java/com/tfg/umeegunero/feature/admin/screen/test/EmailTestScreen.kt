package com.tfg.umeegunero.feature.admin.screen.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.tfg.umeegunero.ui.theme.AdminColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.hilt.navigation.compose.hiltViewModel
import android.webkit.WebView
import android.webkit.WebViewClient
import android.os.Build
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.feature.admin.viewmodel.test.PruebaEmailViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.test.TipoPlantilla
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class ScriptResponse(
    val status: String,
    val message: String
)

/**
 * Pantalla de pruebas para el envío de emails en la aplicación UmeEgunero.
 *
 * Esta pantalla permite probar diferentes plantillas de email HTML y previsualizarlas
 * antes de enviarlas. Incluye validación de email en tiempo real y múltiples plantillas
 * prediseñadas.
 *
 * @param onClose Callback que se ejecuta cuando el usuario quiere cerrar la pantalla
 * @param viewModel ViewModel que gestiona la lógica de la pantalla
 *
 * @see PruebaEmailViewModel
 * @see PlantillaEmail
 *
 * @author Maitane (Estudiante 2º DAM)
 * @version 2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailTestScreen(
    onClose: () -> Unit,
    viewModel: PruebaEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val httpClient = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            httpClient.close()
            Log.d("EmailTestScreen", "Ktor HttpClient cerrado")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pruebas de Email",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de configuración
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Configuración del Email",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = uiState.destinatario,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("Email destinatario") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = uiState.mostrarError,
                        supportingText = if (uiState.mostrarError) {
                            { Text("Email no válido") }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminColor,
                            focusedLabelColor = AdminColor,
                            focusedLeadingIconColor = AdminColor,
                            errorBorderColor = Color.Red,
                            errorLabelColor = Color.Red,
                            errorLeadingIconColor = Color.Red
                        )
                    )
                    OutlinedTextField(
                        value = uiState.nombrePrueba,
                        onValueChange = { viewModel.updateNombrePrueba(it) },
                        label = { Text("Nombre para prueba") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminColor,
                            focusedLabelColor = AdminColor,
                            focusedLeadingIconColor = AdminColor
                        )
                    )
                }
            }

            // Sección de plantillas
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Plantillas de Email",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Botones de plantillas
                    ElevatedButton(
                        onClick = { viewModel.seleccionarPlantilla(TipoPlantilla.APROBACION) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) { 
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Aprobación")
                    }
                    
                    ElevatedButton(
                        onClick = { viewModel.seleccionarPlantilla(TipoPlantilla.RECHAZO) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) { 
                        Icon(Icons.Default.Cancel, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Rechazo")
                    }

                    ElevatedButton(
                        onClick = { viewModel.seleccionarPlantilla(TipoPlantilla.BIENVENIDA) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) { 
                        Icon(Icons.Default.Celebration, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bienvenida")
                    }

                    ElevatedButton(
                        onClick = { viewModel.seleccionarPlantilla(TipoPlantilla.RECORDATORIO) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) { 
                        Icon(Icons.Default.Notifications, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Recordatorio")
                    }
                }
            }

            // Sección de previsualización
            if (uiState.plantillaSeleccionada != TipoPlantilla.NINGUNA) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Previsualización",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // WebView para previsualizar el HTML con scroll interno
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.White)
                        ) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        webViewClient = WebViewClient()
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            allowContentAccess = true
                                            allowFileAccess = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            defaultTextEncodingName = "UTF-8"
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                // 0 = MIXED_CONTENT_ALWAYS_ALLOW
                                                mixedContentMode = 0
                                            }
                                        }
                                        isVerticalScrollBarEnabled = true
                                        isHorizontalScrollBarEnabled = false
                                        setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY)
                                        // Permitir scroll dentro del WebView
                                        setOnTouchListener { v, event ->
                                            v.parent.requestDisallowInterceptTouchEvent(true)
                                            false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                update = { webView ->
                                    webView.loadDataWithBaseURL(
                                        null,
                                        uiState.previsualizacionHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            )
                        }

                        // Botón de envío - Lógica modificada
                        Button(
                            onClick = {
                                if (uiState.emailValido && uiState.plantillaSeleccionada != TipoPlantilla.NINGUNA) {
                                    // Lanzar coroutine para llamada de red
                                    scope.launch {
                                        // --- Definir URL y parámetros ---
                                        val scriptUrlBase = "https://script.google.com/macros/s/AKfycbxvSugtN4a3LReAIYuZd6A2MIno8UkMGHleqIXsZg7vcGVGxTYMUP9efPbrkEbsxj6DLA/exec" // ¡TU URL!
                                        val destinatario = uiState.destinatario
                                        val nombre = uiState.nombrePrueba.ifBlank { "Usuario/a" }
                                        val tipoPlantilla = uiState.plantillaSeleccionada.name // "BIENVENIDA", etc.
                                        val asunto = "UmeEgunero: ${uiState.plantillaSeleccionada.name.lowercase().replaceFirstChar { it.titlecase() }} para ${nombre}" // Asunto más descriptivo

                                        // --- Construir URL con parámetros ---
                                        val urlConParams = try {
                                            Uri.parse(scriptUrlBase)
                                                .buildUpon()
                                                .appendQueryParameter("destinatario", destinatario)
                                                .appendQueryParameter("asunto", asunto)
                                                .appendQueryParameter("nombre", nombre)
                                                .appendQueryParameter("tipoPlantilla", tipoPlantilla)
                                                // Futuro: Añadir token de seguridad aquí
                                                // .appendQueryParameter("token", "TU_TOKEN_SECRETO")
                                                .build()
                                                .toString()
                                        } catch (e: Exception) {
                                            Log.e("EmailTestScreen", "Error construyendo URL", e)
                                            snackbarHostState.showSnackbar("Error interno al preparar el envío.")
                                            return@launch // Salir de la coroutine
                                        }


                                        Log.d("EmailTestScreen", "Llamando a Apps Script: $urlConParams")
                                        var snackbarMsg = "Enviando email..."
                                        snackbarHostState.showSnackbar(snackbarMsg) // Mensaje inicial

                                        try {
                                            // --- Realizar llamada HTTP en hilo IO ---
                                            val response: ScriptResponse = withContext(Dispatchers.IO) {
                                                 Log.d("EmailTestScreen", "Iniciando llamada GET en ${Thread.currentThread().name}")
                                                 httpClient.get(urlConParams).body() // Ktor analiza el JSON a ScriptResponse
                                            }

                                            Log.i("EmailTestScreen", "Respuesta del script: Status=${response.status}, Message=${response.message}")

                                            // --- Mostrar resultado ---
                                            if (response.status == "OK") {
                                                snackbarMsg = "¡Email enviado correctamente!"
                                            } else {
                                                // Usar mensaje de error del script si está disponible
                                                snackbarMsg = "Error del script: ${response.message}"
                                            }

                                        } catch (e: Exception) {
                                            Log.e("EmailTestScreen", "Error en llamada a Apps Script", e)
                                            snackbarMsg = "Error de red al enviar: ${e.message?.take(100) ?: "Desconocido"}"
                                        } finally {
                                            snackbarHostState.showSnackbar(snackbarMsg) // Mostrar mensaje final
                                        }
                                    }
                                } else {
                                     scope.launch {
                                         if(!uiState.emailValido) snackbarHostState.showSnackbar("El email no es válido.")
                                         else snackbarHostState.showSnackbar("Selecciona una plantilla primero.")
                                     }
                                }
                            },
                            enabled = uiState.emailValido && uiState.plantillaSeleccionada != TipoPlantilla.NINGUNA, // Habilitar solo si hay email y plantilla
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AdminColor
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar Email (vía Script)") // Texto del botón actualizado
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailTestScreenPreview() {
    UmeEguneroTheme {
        EmailTestScreen(
            onClose = {}
        )
    }
} 