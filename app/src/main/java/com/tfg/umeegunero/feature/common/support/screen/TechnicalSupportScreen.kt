package com.tfg.umeegunero.feature.common.support.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.EmailSoporteConstants
import com.tfg.umeegunero.feature.common.support.viewmodel.SupportViewModel
import kotlinx.coroutines.launch

@Composable
fun TechnicalSupportScreen(
    onNavigateBack: () -> Unit,
    viewModel: SupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showEmailDialog by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    // Observar el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Efectos para mostrar diálogos según el estado
    LaunchedEffect(uiState.success, uiState.error) {
        if (uiState.success) {
            showEmailDialog = false
            // Limpiar los campos después de enviar
            name = ""
            email = ""
            selectedTopic = ""
            message = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soporte Técnico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de contacto directo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "¿Necesitas ayuda?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Nuestro equipo técnico está aquí para ayudarte",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showEmailDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contactar con Soporte")
                    }
                }
            }

            // Tarjeta de horario de atención
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Horario de Atención",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Lunes a Viernes: 9:00 - 18:00",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Sábados: 10:00 - 14:00",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Domingos: Cerrado",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Tarjeta de información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Información Adicional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Tiempo de respuesta estimado: < 24h",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Soporte técnico disponible en español e inglés",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Diálogo para enviar email
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!uiState.isLoading) {
                    showEmailDialog = false 
                    viewModel.clearState()
                }
            },
            title = { Text("Contactar con Soporte") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = name.isBlank() && uiState.error != null,
                        enabled = !uiState.isLoading
                    )
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isBlank() && uiState.error != null,
                        enabled = !uiState.isLoading
                    )
                    
                    OutlinedTextField(
                        value = selectedTopic,
                        onValueChange = { selectedTopic = it },
                        label = { Text("Asunto") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = selectedTopic.isBlank() && uiState.error != null,
                        enabled = !uiState.isLoading
                    )
                    
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Mensaje") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        isError = message.isBlank() && uiState.error != null,
                        enabled = !uiState.isLoading
                    )
                    
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    if (uiState.success) {
                        Text(
                            text = "¡Mensaje enviado correctamente! Te contactaremos lo antes posible.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.sendEmailSoporte(
                                emailUsuario = email, 
                                nombre = name, 
                                asunto = selectedTopic, 
                                mensaje = message
                            )
                        }
                    },
                    enabled = !uiState.isLoading && name.isNotBlank() && email.isNotBlank() && 
                             selectedTopic.isNotBlank() && message.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isLoading) "Enviando..." else "Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEmailDialog = false 
                        viewModel.clearState()
                    },
                    enabled = !uiState.isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de éxito
    if (uiState.success && !showEmailDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.clearState() },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Éxito")
                }
            },
            text = { Text("Tu mensaje ha sido enviado correctamente. Te responderemos lo antes posible.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearState() }) {
                    Text("OK")
                }
            }
        )
    }
} 