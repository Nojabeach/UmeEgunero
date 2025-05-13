package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.MessageDetailViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.automirrored.filled.Reply

/**
 * Pantalla de detalle para cualquier tipo de mensaje
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(
    messageId: String,
    onBack: () -> Unit,
    onNavigateToConversation: (String) -> Unit = {},
    viewModel: MessageDetailViewModel = hiltViewModel()
) {
    // Cargar el mensaje
    LaunchedEffect(messageId) {
        viewModel.loadMessage(messageId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (uiState.message?.type) {
                            MessageType.CHAT -> "Mensaje de chat"
                            MessageType.NOTIFICATION -> "Notificación"
                            MessageType.ANNOUNCEMENT -> "Comunicado"
                            MessageType.INCIDENT -> "Incidencia"
                            MessageType.ATTENDANCE -> "Asistencia"
                            MessageType.DAILY_RECORD -> "Registro diario"
                            MessageType.SYSTEM -> "Mensaje del sistema"
                            null -> "Detalle de mensaje"
                        }
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        message = "Cargando mensaje..."
                    )
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.loadMessage(messageId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.message != null -> {
                    // Contenido del mensaje
                    val message = uiState.message!!
                    
                    // Manejar tipos específicos de mensajes
                    when (message.type) {
                        MessageType.ANNOUNCEMENT -> {
                            ComunicadoDetailScreen(
                                comunicadoId = message.id,
                                onBack = onBack
                            )
                        }
                        MessageType.CHAT -> {
                            // Pantalla simplificada para mensajes de chat
                            ChatMessageDetail(
                                message = message,
                                onContinueChat = {
                                    if (message.conversationId.isNotEmpty()) {
                                        onNavigateToConversation(message.conversationId)
                                    } else {
                                        // Log error
                                        println("No hay ID de conversación para continuar el chat")
                                    }
                                }
                            )
                        }
                        else -> {
                            // Vista genérica para otros tipos de mensajes
                            GenericMessageDetail(
                                message = message,
                                onMarkAsRead = {
                                    viewModel.markAsRead()
                                }
                            )
                        }
                    }
                }
                else -> {
                    // Mensaje no encontrado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontró el mensaje",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vista para mensajes de chat
 */
@Composable
fun ChatMessageDetail(
    message: UnifiedMessage,
    onContinueChat: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(message.timestamp.toDate())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales del emisor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.firstOrNull()?.toString() ?: "U",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono indicador de tipo de mensaje
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Chat",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Divider(modifier = Modifier.padding(bottom = 16.dp))
        
        // Contenido del mensaje
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Botón para continuar la conversación
        Button(
            onClick = onContinueChat,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Continuar conversación")
        }
    }
}

/**
 * Vista genérica para otros tipos de mensajes
 */
@Composable
fun GenericMessageDetail(
    message: UnifiedMessage,
    onMarkAsRead: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(message.timestamp.toDate())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Banner según tipo de mensaje
        MessageTypeBanner(messageType = message.type)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Título
        if (message.title.isNotEmpty()) {
            Text(
                text = message.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Info del remitente
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales del emisor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getColorForMessageType(message.type)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.firstOrNull()?.toString() ?: "U",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Divider(modifier = Modifier.padding(bottom = 16.dp))
        
        // Contenido
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Información adicional
        if (message.metadata.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información adicional",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    message.metadata.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(0.4f)
                            )
                            
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botón para marcar como leído
        if (!message.isRead) {
            Button(
                onClick = onMarkAsRead,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Marcar como leído")
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Mensaje leído",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Banner según tipo de mensaje
 */
@Composable
fun MessageTypeBanner(messageType: MessageType) {
    val (color, icon, title) = when (messageType) {
        MessageType.INCIDENT -> Triple(
            Color(0xFFE53935),
            Icons.Default.Warning,
            "Incidencia"
        )
        MessageType.ATTENDANCE -> Triple(
            Color(0xFF2196F3),
            Icons.Default.Event,
            "Registro de asistencia"
        )
        MessageType.DAILY_RECORD -> Triple(
            Color(0xFFFF9800),
            Icons.Default.Assignment,
            "Registro diario"
        )
        MessageType.NOTIFICATION -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Notifications,
            "Notificación"
        )
        MessageType.SYSTEM -> Triple(
            Color(0xFF9C27B0),
            Icons.Default.Info,
            "Mensaje del sistema"
        )
        else -> return // No mostrar para otros tipos
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

/**
 * Obtiene el color asociado a un tipo de mensaje
 */
@Composable
fun getColorForMessageType(type: MessageType): Color {
    return when (type) {
        MessageType.CHAT -> MaterialTheme.colorScheme.primary
        MessageType.NOTIFICATION -> MaterialTheme.colorScheme.tertiary
        MessageType.ANNOUNCEMENT -> Color(0xFF4CAF50) // Verde
        MessageType.INCIDENT -> Color(0xFFF44336) // Rojo
        MessageType.ATTENDANCE -> Color(0xFF2196F3) // Azul
        MessageType.DAILY_RECORD -> Color(0xFFFF9800) // Naranja
        MessageType.SYSTEM -> Color(0xFF9C27B0) // Púrpura
    }
}

/**
 * Obtiene el icono asociado a un tipo de mensaje
 */
@Composable
fun getIconForMessageType(type: MessageType): ImageVector {
    return when (type) {
        MessageType.CHAT -> Icons.AutoMirrored.Filled.Chat
        MessageType.NOTIFICATION -> Icons.Default.Notifications
        MessageType.ANNOUNCEMENT -> Icons.Default.Announcement
        MessageType.INCIDENT -> Icons.Default.Warning
        MessageType.ATTENDANCE -> Icons.Default.Event
        MessageType.DAILY_RECORD -> Icons.Default.Assignment
        MessageType.SYSTEM -> Icons.Default.Info
    }
}

/**
 * Vista de error mejorada con botón de reintentar
 */
@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 16.dp)
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Reintentar")
        }
    }
}

/**
 * Indicador de estado de mensaje mejorado
 */
@Composable
fun MessageStatusIndicator(
    status: MessageStatus,
    timestamp: Timestamp,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(timestamp.toDate())
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            MessageStatus.READ -> {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Mensaje leído",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Leído el $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            MessageStatus.UNREAD -> {
                Icon(
                    imageVector = Icons.Default.MarkEmailUnread,
                    contentDescription = "Mensaje no leído",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "No leído - Enviado el $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Mensaje enviado",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Enviado el $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Botón de respuesta mejorado para el chat
 */
@Composable
fun EnhancedReplyButton(
    onClick: () -> Unit,
    message: UnifiedMessage,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Responder a ${message.senderName.split(" ").firstOrNull() ?: ""}",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 