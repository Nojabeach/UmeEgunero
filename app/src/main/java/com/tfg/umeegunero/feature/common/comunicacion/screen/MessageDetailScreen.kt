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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.BuildConfig
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.MessageDetailViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import timber.log.Timber
import com.tfg.umeegunero.R

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Marcar automáticamente como leído cuando se abre el mensaje
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            if (!message.isRead) {
                viewModel.markAsRead()
            }
        }
    }
    
    // Navegar a la conversación con un mejor manejo de errores
    val navigateToChat = { conversationId: String ->
        scope.launch {
            try {
                Timber.d("Iniciando navegación a chat con conversationId: $conversationId")
                
                // Validar que tenemos un conversationId válido
                if (conversationId.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Error: No se encontró la conversación",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                // Intentar obtener un participante para la conversación
                val participantId = viewModel.getParticipantId(conversationId)
                
                Timber.d("ParticipantId obtenido: '$participantId'")
                
                if (conversationId.isNotEmpty() && participantId.isNotEmpty()) {
                    Timber.d("Navegando a conversación: $conversationId con participante: $participantId")
                    // Pasar ambos parámetros a la función de navegación
                    onNavigateToConversation("$conversationId/$participantId")
                } else {
                    // Dar más información sobre qué falló
                    val errorMsg = when {
                        conversationId.isEmpty() -> "No se pudo determinar la conversación"
                        participantId.isEmpty() -> "No se pudo determinar el participante"
                        else -> "Error desconocido al abrir la conversación"
                    }
                    
                    Toast.makeText(
                        context,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()
                    
                    Timber.e("Error en navegación - conversationId: '$conversationId', participantId: '$participantId'")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al preparar navegación a chat: ${e.message}")
                Toast.makeText(
                    context,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    // Mostrar un error si lo hay
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    // Determinar colores y estilo basado en el tipo de mensaje
    val messageTypeInfo = when (uiState.message?.type) {
        MessageType.NOTIFICATION -> MessageTypeInfo(
            color = MaterialTheme.colorScheme.tertiary,
            icon = Icons.Default.Notifications,
            title = "Notificación",
            gradient = listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
            )
        )
        MessageType.ANNOUNCEMENT -> MessageTypeInfo(
            color = Color(0xFF4CAF50),
            icon = Icons.Default.Announcement,
            title = "Comunicado",
            gradient = listOf(
                Color(0xFF4CAF50),
                Color(0xFF81C784)
            )
        )
        MessageType.INCIDENT -> MessageTypeInfo(
            color = Color(0xFFF44336),
            icon = Icons.Default.Warning,
            title = "Incidencia",
            gradient = listOf(
                Color(0xFFF44336),
                Color(0xFFE57373)
            )
        )
        MessageType.ATTENDANCE -> MessageTypeInfo(
            color = Color(0xFF2196F3),
            icon = Icons.Default.Event,
            title = "Registro de asistencia",
            gradient = listOf(
                Color(0xFF2196F3),
                Color(0xFF64B5F6)
            )
        )
        MessageType.DAILY_RECORD -> MessageTypeInfo(
            color = Color(0xFFFF9800),
            icon = Icons.Default.Assignment,
            title = "Registro diario",
            gradient = listOf(
                Color(0xFFFF9800),
                Color(0xFFFFB74D)
            )
        )
        MessageType.CHAT -> MessageTypeInfo(
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.AutoMirrored.Filled.Chat,
            title = "Mensaje de chat",
            gradient = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        )
        else -> MessageTypeInfo(
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Default.Mail,
            title = "Mensaje",
            gradient = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        )
    }
    
    Scaffold(
        topBar = {
            // Eliminamos la topBar para un diseño más limpio e integrado
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
                    
                    // Usar diseño mejorado para todos los tipos de mensajes
                    when (message.type) {
                        MessageType.ANNOUNCEMENT, MessageType.NOTIFICATION -> {
                            EnhancedMessageDetail(
                                message = message,
                                messageTypeInfo = messageTypeInfo,
                                onBack = onBack
                            )
                        }
                        MessageType.CHAT -> {
                            // Pantalla mejorada para mensajes de chat
                            EnhancedChatDetail(
                                message = message,
                                messageTypeInfo = messageTypeInfo,
                                onContinueChat = {
                                    navigateToChat(message.conversationId)
                                },
                                onBack = onBack
                            )
                        }
                        else -> {
                            // Vista genérica mejorada para otros tipos de mensajes
                            EnhancedMessageDetail(
                                message = message,
                                messageTypeInfo = messageTypeInfo,
                                onBack = onBack
                            )
                        }
                    }
                }
                else -> {
                    // Mensaje no encontrado con diseño mejorado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No se encontró el mensaje",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = onBack,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Volver")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista mejorada para todos los tipos de mensajes
 */
@Composable
fun EnhancedMessageDetail(
    message: UnifiedMessage,
    messageTypeInfo: MessageTypeInfo,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(message.timestamp.toDate())
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo del encabezado con degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = messageTypeInfo.gradient
                    )
                )
        ) {
            // Botón de retorno en la parte superior
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            
            // Información del tipo de mensaje
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = messageTypeInfo.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = messageTypeInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
        
        // Tarjeta principal del contenido
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 160.dp)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Título del mensaje
                if (message.title.isNotEmpty()) {
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Remitente y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(messageTypeInfo.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderName.firstOrNull()?.toString() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Contenido principal
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Información adicional
                if (message.metadata.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Información adicional",
                                style = MaterialTheme.typography.titleMedium,
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
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Indicador de estado
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EnhancedMessageStatus(
                        status = message.status,
                        timestamp = message.timestamp,
                        color = messageTypeInfo.color
                    )
                }
            }
        }
    }
}

/**
 * Vista mejorada específicamente para mensajes de chat
 */
@Composable
fun EnhancedChatDetail(
    message: UnifiedMessage,
    messageTypeInfo: MessageTypeInfo,
    onContinueChat: () -> Unit,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(message.timestamp.toDate())
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo del encabezado con degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = messageTypeInfo.gradient
                    )
                )
        ) {
            // Botón de retorno en la parte superior
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            
            // Información del tipo de mensaje
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = messageTypeInfo.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = messageTypeInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
        
        // Tarjeta principal del contenido
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 160.dp)
                .fillMaxHeight(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Remitente y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(messageTypeInfo.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderName.firstOrNull()?.toString() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Burbuja de mensaje de chat
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón para continuar la conversación
                Button(
                    onClick = onContinueChat,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = message.conversationId.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = messageTypeInfo.color
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Continuar conversación")
                }
                
                // Información de diagnóstico en modo debug
                if (BuildConfig.DEBUG && message.conversationId.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ Información de debug",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "ConversationId vacío. No se podrá continuar la conversación.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Indicador de estado
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EnhancedMessageStatus(
                        status = message.status,
                        timestamp = message.timestamp,
                        color = messageTypeInfo.color
                    )
                }
            }
        }
    }
}

/**
 * Componente mejorado para mostrar el estado del mensaje
 */
@Composable
fun EnhancedMessageStatus(
    status: MessageStatus,
    timestamp: Timestamp,
    color: Color
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(timestamp.toDate())
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            when (status) {
                MessageStatus.READ -> {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Mensaje leído",
                        tint = color,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Leído el $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                MessageStatus.UNREAD -> {
                    Icon(
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = "Mensaje no leído",
                        tint = color,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "No leído - Enviado el $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Mensaje enviado",
                        tint = color,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Enviado el $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Clase para almacenar información de estilo por tipo de mensaje
 */
data class MessageTypeInfo(
    val color: Color,
    val icon: ImageVector,
    val title: String,
    val gradient: List<Color>
)

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