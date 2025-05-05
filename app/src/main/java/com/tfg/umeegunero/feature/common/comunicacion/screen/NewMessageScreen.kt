package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.common.comunicacion.model.MessagePriority
import com.tfg.umeegunero.feature.common.comunicacion.model.MessageType
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.NewMessageUiState
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.NewMessageViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla para crear un nuevo mensaje unificado
 * 
 * @param receiverId ID del destinatario preseleccionado (opcional)
 * @param messageType Tipo de mensaje a crear (opcional)
 * @param onBack Callback para volver atrás
 * @param onMessageSent Callback invocado cuando se envía el mensaje
 * @param viewModel ViewModel que gestiona la lógica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    receiverId: String? = null,
    messageType: String? = null,
    onBack: () -> Unit,
    onMessageSent: () -> Unit,
    viewModel: NewMessageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
    // Manejar destinatario predefinido y tipo de mensaje
    LaunchedEffect(receiverId, messageType) {
        receiverId?.let { viewModel.setReceiverId(it) }
        
        messageType?.let { 
            // Si es una respuesta a un mensaje existente
            if (it.startsWith("REPLY_TO_")) {
                val originalMessageId = it.substringAfter("REPLY_TO_")
                viewModel.loadOriginalMessage(originalMessageId)
            } else {
                // Establecer tipo de mensaje normal
                try {
                    val type = MessageType.valueOf(it)
                    viewModel.setMessageType(type)
                } catch (e: Exception) {
                    // Tipo de mensaje inválido, usar el tipo por defecto
                }
            }
        }
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
    
    // Navegar de vuelta después de enviar el mensaje
    LaunchedEffect(uiState.messageSent) {
        if (uiState.messageSent) {
            onMessageSent()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when(uiState.messageType) {
                            MessageType.ANNOUNCEMENT -> "Nuevo comunicado"
                            MessageType.CHAT -> "Nuevo mensaje"
                            MessageType.NOTIFICATION -> "Nueva notificación"
                            MessageType.INCIDENT -> "Reportar incidencia"
                            MessageType.ATTENDANCE -> "Notificación de asistencia"
                            MessageType.DAILY_RECORD -> "Registro diario"
                            MessageType.SYSTEM -> "Mensaje del sistema"
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
                actions = {
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = uiState.canSendMessage && !uiState.isSending
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar"
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Si hay un mensaje original, mostrar información
            if (uiState.replyingTo != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "En respuesta a:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            IconButton(
                                onClick = { viewModel.clearReplyTo() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar respuesta",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Guardar en una variable local para evitar el smart cast problem
                        val replyingTo = uiState.replyingTo
                        
                        Text(
                            text = replyingTo?.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = "De: ${replyingTo?.senderName ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Selector de tipo de mensaje
            if (messageType == null) {
                ExposedDropdownMenuBox(
                    expanded = uiState.showTypeMenu,
                    onExpandedChange = { viewModel.toggleTypeMenu() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        value = uiState.messageType.toString(),
                        onValueChange = { },
                        label = { Text("Tipo de mensaje") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.showTypeMenu) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = uiState.showTypeMenu,
                        onDismissRequest = { viewModel.toggleTypeMenu() }
                    ) {
                        MessageType.values().forEach { type ->
                            // Filtrar tipos que no sean relevantes según el perfil del usuario
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        when(type) {
                                            MessageType.ANNOUNCEMENT -> "Comunicado"
                                            MessageType.CHAT -> "Mensaje"
                                            MessageType.NOTIFICATION -> "Notificación"
                                            MessageType.INCIDENT -> "Incidencia"
                                            MessageType.ATTENDANCE -> "Asistencia"
                                            MessageType.DAILY_RECORD -> "Registro diario"
                                            MessageType.SYSTEM -> "Sistema"
                                        }
                                    ) 
                                },
                                onClick = {
                                    viewModel.setMessageType(type)
                                    viewModel.toggleTypeMenu()
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Campo de búsqueda y selección de destinatario
            if (!uiState.isReply) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Buscar destinatario") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar"
                                )
                            }
                        }
                    }
                )
                
                // Lista de destinatarios seleccionados
                if (uiState.recipients.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Destinatarios:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (uiState.recipients.size > 2) 100.dp else 40.dp)
                        ) {
                            items(uiState.recipients) { receiver ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(receiver.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { viewModel.removeReceiver(receiver.id) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "Eliminar",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // Resultados de búsqueda
                if (uiState.searchResults.isNotEmpty() && uiState.searchQuery.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(200.dp),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.searchResults) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.addReceiver(user.id, user.name) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    if (uiState.recipients.any { it.id == user.id }) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionado",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Añadir",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Divider()
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Campo de título
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Título") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isError = uiState.titleError != null,
                supportingText = { 
                    uiState.titleError?.let { 
                        Text(it, color = MaterialTheme.colorScheme.error) 
                    } 
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de contenido
            OutlinedTextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                isError = uiState.contentError != null,
                supportingText = { 
                    uiState.contentError?.let { 
                        Text(it, color = MaterialTheme.colorScheme.error) 
                    } 
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de prioridad
            Text(
                text = "Prioridad:",
                style = MaterialTheme.typography.labelLarge
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.updatePriority(MessagePriority.NORMAL) }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = uiState.priority == MessagePriority.NORMAL,
                        onClick = { viewModel.updatePriority(MessagePriority.NORMAL) }
                    )
                    Text("Normal", modifier = Modifier.padding(start = 4.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.updatePriority(MessagePriority.HIGH) }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = uiState.priority == MessagePriority.HIGH,
                        onClick = { viewModel.updatePriority(MessagePriority.HIGH) }
                    )
                    Text("Alta", modifier = Modifier.padding(start = 4.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.updatePriority(MessagePriority.URGENT) }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = uiState.priority == MessagePriority.URGENT,
                        onClick = { viewModel.updatePriority(MessagePriority.URGENT) }
                    )
                    Text("Urgente", modifier = Modifier.padding(start = 4.dp))
                }
            }
            
            // Botón de enviar
            Button(
                onClick = { viewModel.sendMessage() },
                enabled = uiState.canSendMessage && !uiState.isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar")
                }
            }
        }
    }
} 