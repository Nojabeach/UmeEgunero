package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.NewMessageViewModel

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
    
    // Inicializar ViewModel
    LaunchedEffect(receiverId, messageType) {
        if (receiverId != null || messageType != null) {
            // No necesitamos llamar a initialize, el SavedStateHandle en el ViewModel ya maneja estos parámetros
        }
    }
    
    // Manejar error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }
    
    // Manejar mensaje enviado - utilizando la bandera explícita
    LaunchedEffect(uiState.messageSent) {
        if (uiState.messageSent) {
            viewModel.resetMessageSentFlag()
            onMessageSent()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo mensaje", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                        enabled = uiState.canSendMessage
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de destinatarios seleccionados
            if (uiState.selectedRecipients.isNotEmpty()) {
                Text(
                    text = "Destinatarios seleccionados (${uiState.selectedRecipients.size})",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.selectedRecipients) { recipient ->
                        ElevatedCard(
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = recipient.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                IconButton(
                                    onClick = { viewModel.removeReceiver(recipient.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Eliminar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Campo de asunto
            OutlinedTextField(
                value = uiState.subject,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de tipo de mensaje
            Text(
                text = "Tipo de mensaje",
                style = MaterialTheme.typography.labelMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableMessageTypes.forEach { type ->
                    val isSelected = type == uiState.messageType
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateMessageType(type) },
                        label = { 
                            Text(
                                when(type) {
                                    "CHAT" -> "Chat"
                                    "ANNOUNCEMENT" -> "Comunicado"
                                    "NOTIFICATION" -> "Notificación"
                                    "SYSTEM" -> "Sistema"
                                    else -> type
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de contenido
            OutlinedTextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 300.dp),
                minLines = 5,
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Búsqueda de destinatarios
            Text(
                text = "Buscar destinatarios",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Buscar por nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Resultados de búsqueda
            if (uiState.searchResults.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    Text(
                        text = "Resultados (${uiState.searchResults.size})",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.searchResults.forEach { result ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { viewModel.addReceiver(result.id, result.name) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = result.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Text(
                                        text = result.type,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                IconButton(
                                    onClick = { viewModel.addReceiver(result.id, result.name) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Añadir destinatario"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón de enviar
            Button(
                onClick = { viewModel.sendMessage() },
                enabled = uiState.canSendMessage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar mensaje")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
} 