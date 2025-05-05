package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.MessageDetailViewModel
import com.tfg.umeegunero.ui.components.ErrorContent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pantalla de detalle de mensaje unificado
 * 
 * Muestra el contenido completo de un mensaje, incluyendo metadatos, contenido y opciones
 * de acción como responder, marcar como leído o eliminar.
 * 
 * @param messageId ID del mensaje a mostrar
 * @param onBack Callback para navegar hacia atrás
 * @param onReply Callback para responder al mensaje
 * @param viewModel ViewModel que gestiona la lógica de la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    messageId: String,
    onBack: () -> Unit,
    onReply: (String) -> Unit,
    viewModel: MessageDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    
    // Cargar el mensaje al iniciar
    LaunchedEffect(messageId) {
        viewModel.loadMessage(messageId)
    }
    
    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = error)
                viewModel.clearError()
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar mensaje?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMessage()
                        showDeleteDialog = false
                        // Volver a la pantalla anterior después de eliminar
                        onBack()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when(uiState.message?.type) {
                            MessageType.ANNOUNCEMENT -> "Comunicado"
                            MessageType.CHAT -> "Mensaje"
                            MessageType.NOTIFICATION -> "Notificación"
                            MessageType.INCIDENT -> "Incidencia"
                            MessageType.ATTENDANCE -> "Asistencia"
                            MessageType.DAILY_RECORD -> "Registro diario"
                            MessageType.SYSTEM -> "Sistema"
                            null -> "Detalle de Mensaje"
                        },
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Mostrar botón de responder solo para mensajes tipo CHAT o ANNOUNCEMENT
                    if (uiState.message?.type == MessageType.CHAT || uiState.message?.type == MessageType.ANNOUNCEMENT) {
                        IconButton(onClick = { messageId.let(onReply) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Responder"
                            )
                        }
                    }
                    
                    // Botón de opciones
                    Box {
                        IconButton(onClick = { showOptions = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showOptions,
                            onDismissRequest = { showOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Marcar como leído") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailRead,
                                        contentDescription = "Marcar como leído"
                                    )
                                },
                                onClick = {
                                    viewModel.markAsRead()
                                    showOptions = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar"
                                    )
                                },
                                onClick = {
                                    showDeleteDialog = true
                                    showOptions = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.loadMessage(messageId) }
                    )
                }
                uiState.message != null -> {
                    val message = uiState.message!!
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val formattedDate = dateFormat.format(message.timestamp.toDate())
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Título del mensaje con mejor formato
                        Text(
                            text = message.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Información del remitente y fecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Círculo con la inicial del remitente
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(getMessageTypeColor(message.type)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = message.senderName.first().toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Información del remitente y fecha
                                Column {
                                    Text(
                                        text = message.senderName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            // Badge de prioridad
                            if (message.priority == MessagePriority.HIGH || 
                                message.priority == MessagePriority.URGENT) {
                                
                                // Fondo adaptado según nivel de prioridad
                                val backgroundColor = when (message.priority) {
                                    MessagePriority.HIGH -> Color(0xFFFFA000)
                                    MessagePriority.URGENT -> Color(0xFFE53935)
                                    else -> Color(0xFF2E7D32) // Para LOW y NORMAL
                                }
                                
                                // Texto adaptado según nivel de prioridad
                                val priorityText = when (message.priority) {
                                    MessagePriority.HIGH -> "Alta"
                                    MessagePriority.URGENT -> "Urgente"
                                    else -> "Normal"
                                }
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = backgroundColor
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = priorityText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Separador
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Mostrar tipo de mensaje
                        Text(
                            text = when(message.type) {
                                MessageType.CHAT -> "Mensaje de chat"
                                MessageType.NOTIFICATION -> "Notificación"
                                MessageType.ANNOUNCEMENT -> "Comunicado oficial"
                                MessageType.INCIDENT -> "Reporte de incidencia"
                                MessageType.ATTENDANCE -> "Notificación de asistencia"
                                MessageType.DAILY_RECORD -> "Registro diario"
                                MessageType.SYSTEM -> "Mensaje del sistema"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )

                        // Mostrar icono específico según tipo
                        val isImportantType = message.type == MessageType.INCIDENT || 
                                             message.type == MessageType.ANNOUNCEMENT
                        
                        // Contenido del mensaje
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Si es una respuesta, mostrar información del mensaje original
                        if (!message.replyToId.isNullOrEmpty() && uiState.originalMessage != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Separador
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Mensaje original:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = uiState.originalMessage!!.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = uiState.originalMessage!!.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Botón de responder para mensajes chat
                        if (message.type == MessageType.CHAT || message.type == MessageType.ANNOUNCEMENT) {
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            FilledTonalButton(
                                onClick = { messageId.let(onReply) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = "Responder"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Responder")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
} 