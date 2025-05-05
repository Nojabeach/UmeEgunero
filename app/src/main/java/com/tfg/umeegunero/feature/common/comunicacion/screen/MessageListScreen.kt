package com.tfg.umeegunero.feature.common.comunicacion.screen

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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.MessageListViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla que muestra la lista de mensajes del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    navController: NavController,
    viewModel: MessageListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Lista de pestañas para los tipos de mensajes
    val tabs = listOf(
        "Todos",
        "Notificaciones",
        "Chats",
        "Comunicados",
        "Incidencias"
    )
    
    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = error)
                viewModel.clearError()
            }
        }
    }
    
    // Cargar mensajes al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadMessages()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Implementar búsqueda */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate(AppScreens.NewMessage.createRoute())
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo mensaje",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pestañas para filtrar por tipo de mensaje
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { 
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        }
                    )
                }
            }
            
            // Contenido principal
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.filteredMessages.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Icono según la categoría seleccionada
                            val icon = when (uiState.selectedTabIndex) {
                                1 -> Icons.Default.Notifications
                                2 -> Icons.Default.ChatBubble
                                3 -> Icons.Default.Person
                                4 -> Icons.Default.ErrorOutline
                                else -> Icons.Default.ChatBubble
                            }
                            
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            
                            Text(
                                text = "No hay mensajes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Los mensajes que recibas aparecerán aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = paddingValues
                        ) {
                            items(uiState.filteredMessages) { message ->
                                MessageItem(
                                    message = message,
                                    onClick = { 
                                        navController.navigate(AppScreens.MessageDetail.createRoute(message.id))
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
 * Elemento individual de mensaje
 */
@Composable
fun MessageItem(
    message: UnifiedMessage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.status == MessageStatus.UNREAD) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de prioridad
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (message.priority) {
                            MessagePriority.HIGH -> MaterialTheme.colorScheme.primary
                            MessagePriority.URGENT -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Icono según tipo de mensaje
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (message.type) {
                            MessageType.NOTIFICATION -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            MessageType.CHAT -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            MessageType.ANNOUNCEMENT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            MessageType.INCIDENT -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            MessageType.ATTENDANCE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            MessageType.DAILY_RECORD -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            MessageType.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (message.type) {
                        MessageType.NOTIFICATION -> Icons.Default.Notifications
                        MessageType.CHAT -> Icons.Default.ChatBubble
                        MessageType.ANNOUNCEMENT -> Icons.Default.Person
                        MessageType.INCIDENT -> Icons.Default.ErrorOutline
                        MessageType.ATTENDANCE -> Icons.Default.Person
                        MessageType.DAILY_RECORD -> Icons.Default.Person
                        MessageType.SYSTEM -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (message.type) {
                        MessageType.NOTIFICATION -> MaterialTheme.colorScheme.primary
                        MessageType.CHAT -> MaterialTheme.colorScheme.secondary
                        MessageType.ANNOUNCEMENT -> MaterialTheme.colorScheme.tertiary
                        MessageType.INCIDENT -> MaterialTheme.colorScheme.error
                        MessageType.ATTENDANCE -> MaterialTheme.colorScheme.primary
                        MessageType.DAILY_RECORD -> MaterialTheme.colorScheme.secondary
                        MessageType.SYSTEM -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Remitente y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (message.status == MessageStatus.UNREAD) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = formatDate(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Título
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (message.status == MessageStatus.UNREAD) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Contenido
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Formatea la fecha para mostrarla
 */
private fun formatDate(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Date()
    val diffMillis = now.time - date.time
    val diffHours = diffMillis / (1000 * 60 * 60)
    
    return when {
        diffHours < 24 -> {
            if (diffHours < 1) {
                val diffMinutes = diffMillis / (1000 * 60)
                if (diffMinutes < 1) "Ahora" else "Hace $diffMinutes min"
            } else {
                "Hace $diffHours h"
            }
        }
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
} 