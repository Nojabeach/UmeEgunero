package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.UnifiedInboxUiState
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.UnifiedInboxViewModel
import com.tfg.umeegunero.ui.components.EmptyContent
import com.tfg.umeegunero.ui.components.ErrorContent
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info

/**
 * Pantalla unificada de bandeja de entrada para todos los tipos de comunicaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedInboxScreen(
    onNavigateToMessage: (String) -> Unit,
    onNavigateToNewMessage: () -> Unit,
    onBack: () -> Unit,
    viewModel: UnifiedInboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadMessages()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sistema de Comunicación Unificado") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                viewModel.filterByType(null)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Incidencias") },
                            onClick = {
                                viewModel.filterByType(MessageType.INCIDENT)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Comunicados") },
                            onClick = {
                                viewModel.filterByType(MessageType.ANNOUNCEMENT)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Chats") },
                            onClick = {
                                viewModel.filterByType(MessageType.CHAT)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Asistencia") },
                            onClick = {
                                viewModel.filterByType(MessageType.ATTENDANCE)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Registros diarios") },
                            onClick = {
                                viewModel.filterByType(MessageType.DAILY_RECORD)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Notificaciones") },
                            onClick = {
                                viewModel.filterByType(MessageType.NOTIFICATION)
                                showFilterMenu = false
                            }
                        )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewMessage,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear nuevo mensaje",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
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
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.loadMessages() }
                    )
                }
                uiState.filteredMessages.isEmpty() && uiState.messages.isEmpty() -> {
                    EmptyContent(
                        title = "No hay mensajes",
                        message = "Tu bandeja de entrada está vacía"
                    )
                }
                else -> {
                    MessageList(
                        messages = uiState.filteredMessages.ifEmpty { uiState.messages },
                        onMessageClick = { message ->
                            viewModel.markAsRead(message.id)
                            onNavigateToMessage(message.id)
                        },
                        onDeleteClick = { message ->
                            viewModel.deleteMessage(message.id)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Mensaje eliminado"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageList(
    messages: List<UnifiedMessage>,
    onMessageClick: (UnifiedMessage) -> Unit,
    onDeleteClick: (UnifiedMessage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(messages) { message ->
            MessageItem(
                message = message,
                onClick = { onMessageClick(message) },
                onDeleteClick = { onDeleteClick(message) }
            )
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MessageItem(
    message: UnifiedMessage,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(message.timestamp.toDate())
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!message.isRead) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!message.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indicador de tipo de mensaje
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getMessageTypeColor(message.type)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.type.name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!message.isRead) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (!message.isRead) 
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Badge de prioridad
                    if (message.priority == MessagePriority.HIGH || 
                        message.priority == MessagePriority.URGENT) {
                        
                        val priorityText = if (message.priority == MessagePriority.URGENT) {
                            "Urgente"
                        } else {
                            "Alta"
                        }
                        
                        Text(
                            text = " • $priorityText",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.priority == MessagePriority.HIGH) Color(0xFFF57C00) else Color.Red
                        )
                    }
                }
            }
            
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
                        text = { Text("Eliminar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar"
                            )
                        },
                        onClick = {
                            onDeleteClick()
                            showOptions = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Obtiene el color asociado a un tipo de mensaje
 */
@Composable
fun getMessageTypeColor(type: MessageType): Color {
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
 * Determina el icono a mostrar según el tipo de mensaje
 */
@Composable
private fun getIconForMessageType(type: MessageType?): @Composable () -> Unit = {
    when (type) {
        MessageType.CHAT -> Icon(
            imageVector = Icons.Default.Message,
            contentDescription = "Mensaje",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.NOTIFICATION -> Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notificación",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.ANNOUNCEMENT -> Icon(
            imageVector = Icons.Default.Announcement,
            contentDescription = "Comunicado",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.INCIDENT -> Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Incidencia",
            tint = Color(0xFFE53935)
        )
        MessageType.ATTENDANCE -> Icon(
            imageVector = Icons.Default.Event,
            contentDescription = "Asistencia",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.DAILY_RECORD -> Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Registro diario",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.SYSTEM -> Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Sistema",
            tint = MaterialTheme.colorScheme.primary
        )
        null -> Icon(
            imageVector = Icons.Default.Mail,
            contentDescription = "Todos",
            tint = MaterialTheme.colorScheme.primary
        )
    }
} 