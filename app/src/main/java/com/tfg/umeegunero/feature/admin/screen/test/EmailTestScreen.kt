package com.tfg.umeegunero.feature.admin.screen.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Message
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
import com.tfg.umeegunero.data.service.EmailNotificationService
import com.tfg.umeegunero.data.service.TipoPlantilla as ServiceTipoPlantilla
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import javax.inject.Inject

/**
 * Pantalla de pruebas para el envío de emails en la aplicación UmeEgunero.
 *
 * Esta pantalla permite probar diferentes plantillas de email HTML y previsualizarlas
 * antes de enviarlas. Incluye validación de email en tiempo real y múltiples plantillas
 * prediseñadas.
 *
 * @param onClose Callback que se ejecuta cuando el usuario quiere cerrar la pantalla
 * @param viewModel ViewModel que gestiona la lógica de la pantalla
 * @param emailService Service que gestiona la lógica de envío de emails
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prueba de Emails") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                    
                    // Campo adicional de mensaje para soporte, solo visible cuando se selecciona la plantilla SOPORTE
                    if (uiState.plantillaSeleccionada == TipoPlantilla.SOPORTE) {
                        OutlinedTextField(
                            value = uiState.mensajeSoporte,
                            onValueChange = { viewModel.updateMensajeSoporte(it) },
                            label = { Text("Mensaje de prueba para soporte") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE91E63),
                                focusedLabelColor = Color(0xFFE91E63),
                                focusedLeadingIconColor = Color(0xFFE91E63)
                            )
                        )
                    }
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
                    
                    ElevatedButton(
                        onClick = { viewModel.seleccionarPlantilla(TipoPlantilla.SOPORTE) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFE91E63)
                        )
                    ) { 
                        Icon(Icons.Default.SupportAgent, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Soporte Técnico")
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
                                        setOnTouchListener { v, _ ->
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

                        // Botón de envío
                        Button(
                            onClick = {
                                if (uiState.emailValido && uiState.plantillaSeleccionada != TipoPlantilla.NINGUNA) {
                                    scope.launch {
                                        val destinatario = uiState.destinatario
                                        val nombre = uiState.nombrePrueba
                                        val plantilla = uiState.plantillaSeleccionada
                                        val mensaje = uiState.mensajeSoporte

                                        var snackbarMsg = "Enviando email..."
                                        snackbarHostState.showSnackbar(snackbarMsg)

                                        val enviado = viewModel.enviarEmail(
                                            destinatario = destinatario,
                                            nombre = nombre,
                                            tipoPlantilla = plantilla,
                                            mensajeSoporte = mensaje
                                        )

                                        snackbarMsg = if (enviado) {
                                            "¡Email enviado correctamente!"
                                        } else {
                                            "Error al enviar el email."
                                        }
                                        snackbarHostState.showSnackbar(snackbarMsg)
                                    }
                                } else {
                                     scope.launch {
                                         if(!uiState.emailValido) snackbarHostState.showSnackbar("El email no es válido.")
                                         else snackbarHostState.showSnackbar("Selecciona una plantilla primero.")
                                     }
                                }
                            },
                            enabled = uiState.emailValido && uiState.plantillaSeleccionada != TipoPlantilla.NINGUNA,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AdminColor
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar Email")
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
        // En un preview no podemos usar viewModels reales que requieren Hilt
        // Simplemente mostramos la estructura de la pantalla
        EmailTestScreen(
            onClose = {}
        )
    }
} 