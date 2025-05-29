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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Assignment
import com.tfg.umeegunero.data.model.MessageStatus
import kotlinx.coroutines.delay
import timber.log.Timber

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
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Estado para controlar si es la primera carga
    var isInitialLoad by remember { mutableStateOf(true) }
    
    // Obtener el ID del usuario actual
    val currentUserId = viewModel.getCurrentUserId()
    
    // Agrupar mensajes de chat por conversación
    val groupedChatMessages = remember(uiState.filteredMessages.ifEmpty { uiState.messages }) {
        uiState.filteredMessages.ifEmpty { uiState.messages }
            .filter { it.type == MessageType.CHAT }
            .groupBy { it.conversationId }
            .mapValues { entry -> entry.value.maxByOrNull { it.timestamp } } // Obtenemos el más reciente de cada conversación
            .values
            .filterNotNull()
    }
    
    // Combinar mensajes agrupados de chat con el resto de mensajes
    val messagesForDisplay = remember(uiState.filteredMessages.ifEmpty { uiState.messages }, groupedChatMessages) {
        // Primero, filtramos para excluir los mensajes de chat (ya que los mostraremos agrupados)
        val nonChatMessages = uiState.filteredMessages.ifEmpty { uiState.messages }
            .filter { it.type != MessageType.CHAT }
        
        // Luego, combinamos con los mensajes de chat agrupados
        (nonChatMessages + groupedChatMessages).sortedByDescending { it.timestamp }
    }
    
    // Efecto para cargar mensajes al entrar y configurar actualización periódica
    LaunchedEffect(Unit) {
        if (isInitialLoad) {
            Timber.d("🔄 UnifiedInboxScreen: Cargando mensajes iniciales")
            viewModel.loadMessages()
            viewModel.loadMessageCount()
            isInitialLoad = false
        }
        
        // Configurar actualización periódica mientras la pantalla esté visible
        while(true) {
            // Esperar 30 segundos antes de la siguiente actualización
            delay(30000)
            Timber.d("🔄 UnifiedInboxScreen: Actualización periódica de mensajes")
            viewModel.loadMessages()
            viewModel.loadMessageCount()
        }
    }
    
    // Registrar receptor de broadcast para actualizar cuando lleguen nuevos mensajes
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                Timber.d("📬 UnifiedInboxScreen: Broadcast recibido para actualizar mensajes")
                scope.launch {
                    viewModel.loadMessages()
                    viewModel.loadMessageCount()
                    
                    // Mostrar snackbar informativo
                    snackbarHostState.showSnackbar(
                        message = "Nuevo mensaje recibido",
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
            }
        }
        
        // Registrar para recibir broadcasts de nuevos mensajes
        val filter = android.content.IntentFilter().apply {
            addAction("com.tfg.umeegunero.NUEVO_MENSAJE_UNIFICADO")
            addAction("com.tfg.umeegunero.NUEVO_MENSAJE_CHAT")
            addAction("com.tfg.umeegunero.NUEVA_INCIDENCIA")
            addAction("com.tfg.umeegunero.ASISTENCIA")
            addAction("com.tfg.umeegunero.ACTUALIZACION_REGISTRO")
        }
        
        // Para Android 14 (API 34) y superior, es obligatorio especificar si el receiver es exportado
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        
        // NO actualizar aquí para evitar carga duplicada
        // scope.launch {
        //     viewModel.loadMessages()
        // }
        
        // Limpiar al destruir
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    
    // Actualizar contador antes de salir de la pantalla
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // Solo actualizar si no es la carga inicial
                if (!isInitialLoad) {
                    Timber.d("🔄 UnifiedInboxScreen: Actualizando al volver a pantalla visible")
                    scope.launch {
                        viewModel.loadMessages()
                        viewModel.loadMessageCount()
                    }
                }
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                viewModel.loadMessageCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Mensajes y Comunicados",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Centro de comunicaciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
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
            }
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
        AnimatedContent(
            targetState = when {
                uiState.isLoading && uiState.messages.isEmpty() -> "loading"
                uiState.error != null -> "error"
                uiState.filteredMessages.isEmpty() && uiState.messages.isEmpty() -> "empty"
                else -> "content"
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { targetState ->
            when (targetState) {
                "loading" -> {
                    LoadingContent()
                }
                "error" -> {
                    ErrorContent(
                        message = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.loadMessages() }
                    )
                }
                "empty" -> {
                    EmptyContent(
                        title = "No hay mensajes",
                        message = "Tu bandeja de entrada está vacía"
                    )
                }
                "content" -> {
                    MessageList(
                        messages = messagesForDisplay,
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
                        },
                        currentUserId = currentUserId
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
    onDeleteClick: (UnifiedMessage) -> Unit,
    currentUserId: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(messages) { message ->
            MessageItem(
                message = message,
                onClick = { onMessageClick(message) },
                onDeleteClick = { onDeleteClick(message) },
                currentUserId = currentUserId
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun MessageItem(
    message: UnifiedMessage,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currentUserId: String
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(message.timestamp.toDate())
    var showOptions by remember { mutableStateOf(false) }
    
    // Determinar si el mensaje está realmente leído basándose en todos los indicadores disponibles
    val isReallyRead = message.isRead || message.status == MessageStatus.READ
    
    // Verificar si el usuario actual es el emisor del mensaje
    val isCurrentUserSender = message.senderId == currentUserId
    
    // Solo permitir borrar si es el emisor Y el mensaje no ha sido leído por el destinatario
    val canDelete = isCurrentUserSender && !isReallyRead
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!isReallyRead) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (message.type) {
                MessageType.ANNOUNCEMENT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (!isReallyRead) 1f else 0.7f)
                MessageType.NOTIFICATION -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = if (!isReallyRead) 1f else 0.7f)
                else -> if (!isReallyRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            }
        ),
        border = if (!isReallyRead) 
            BorderStroke(2.dp, getMessageTypeColor(message.type)) 
        else null
    ) {
        when (message.type) {
            MessageType.ANNOUNCEMENT, MessageType.NOTIFICATION -> {
                // Estilo tipo notificación para comunicados y notificaciones
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Cabecera con icono y título
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getMessageTypeColor(message.type)),
                            contentAlignment = Alignment.Center
                        ) {
                            when (message.type) {
                                MessageType.ANNOUNCEMENT -> Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Announcement,
                                    contentDescription = "Comunicado",
                                    tint = Color.White
                                )
                                MessageType.NOTIFICATION -> Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificación",
                                    tint = Color.White
                                )
                                else -> Text(
                                    text = message.type.name.first().toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = message.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                text = "De: ${message.senderName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (!isReallyRead) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        
                        // Solo mostrar botón de opciones si puede eliminar
                        if (canDelete) {
                            Box {
                                IconButton(onClick = { showOptions = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Más opciones",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                    
                    // Badge de prioridad
                    if (message.priority == MessagePriority.HIGH || 
                        message.priority == MessagePriority.URGENT) {
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val priorityColor = if (message.priority == MessagePriority.URGENT) 
                                Color.Red else Color(0xFFF57C00)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = priorityColor.copy(alpha = 0.2f),
                                contentColor = priorityColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PriorityHigh,
                                        contentDescription = null,
                                        tint = priorityColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (message.priority == MessagePriority.URGENT) "Urgente" else "Prioridad alta",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    // Contenido
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (!isReallyRead) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Fecha y hora al final, a la derecha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isReallyRead) {
                            Surface(
                                shape = CircleShape,
                                color = getMessageTypeColor(message.type)
                            ) {
                                Text(
                                    text = "Sin leer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            else -> {
                // Estilo original para otros tipos de mensajes
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
                            fontWeight = if (!isReallyRead) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (!isReallyRead) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (!isReallyRead) 
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = message.senderName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (!isReallyRead) FontWeight.Bold else FontWeight.Normal,
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
                                    fontWeight = if (!isReallyRead) FontWeight.Bold else FontWeight.Normal,
                                    color = if (message.priority == MessagePriority.HIGH) Color(0xFFF57C00) else Color.Red
                                )
                            }
                            
                            // Indicador de no leído
                            if (!isReallyRead) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(getMessageTypeColor(message.type))
                                )
                            }
                        }
                    }
                    
                    // Solo mostrar botón de opciones si puede eliminar
                    if (canDelete) {
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
        MessageType.GROUP_CHAT -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        MessageType.NOTIFICATION -> MaterialTheme.colorScheme.tertiary
        MessageType.ANNOUNCEMENT -> Color(0xFF4CAF50) // Verde
        MessageType.INCIDENT -> Color(0xFFF44336) // Rojo
        MessageType.ATTENDANCE -> Color(0xFF2196F3) // Azul
        MessageType.DAILY_RECORD -> Color(0xFFFF9800) // Naranja
        MessageType.SYSTEM -> Color(0xFF9C27B0) // Púrpura
        MessageType.TASK -> Color(0xFFF57C00) // Naranja
        MessageType.EVENT -> Color(0xFF039BE5) // Azul claro
    }
}

/**
 * Determina el icono a mostrar según el tipo de mensaje
 */
@Composable
private fun getIconForMessageType(type: MessageType?): @Composable () -> Unit = {
    when (type) {
        MessageType.CHAT -> Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "Mensajes",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.GROUP_CHAT -> Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Chat de grupo",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.NOTIFICATION -> Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notificación",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.ANNOUNCEMENT -> Icon(
            imageVector = Icons.AutoMirrored.Filled.Announcement,
            contentDescription = "Comunicados",
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
        MessageType.TASK -> Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = "Tarea",
            tint = MaterialTheme.colorScheme.primary
        )
        MessageType.EVENT -> Icon(
            imageVector = Icons.Default.Event,
            contentDescription = "Evento",
            tint = MaterialTheme.colorScheme.primary
        )
        null -> Icon(
            imageVector = Icons.Default.Mail,
            contentDescription = "Todos",
            tint = MaterialTheme.colorScheme.primary
        )
    }
} 