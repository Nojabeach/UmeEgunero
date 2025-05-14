package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.layout.*
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
    
    // Manejar error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo mensaje") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Lista de destinatarios seleccionados
            if (uiState.selectedRecipients.isNotEmpty()) {
                Text(
                    text = "Destinatarios seleccionados (${uiState.selectedRecipients.size})",
                    style = MaterialTheme.typography.labelMedium
                )
                
                uiState.selectedRecipients.forEach { recipient ->
                    Surface(
                        modifier = Modifier
                            .padding(end = 4.dp, bottom = 4.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recipient.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(
                                onClick = { viewModel.removeReceiver(recipient.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de tipo de mensaje
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tipo de mensaje:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableMessageTypes.forEach { type ->
                        val isSelected = type == uiState.messageType
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateMessageType(type) },
                            label = { 
                                Text(
                                    text = when(type) {
                                        "CHAT" -> "Chat"
                                        "ANNOUNCEMENT" -> "Comunicado"
                                        "NOTIFICATION" -> "Notificación"
                                        "SYSTEM" -> "Sistema"
                                        else -> type
                                    }
                                ) 
                            },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
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
                    .height(200.dp)
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
                modifier = Modifier.fillMaxWidth()
            )
            
            // Resultados de búsqueda
            if (uiState.searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Resultados (${uiState.searchResults.size})",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                uiState.searchResults.forEach { result ->
                    val alreadySelected = uiState.selectedRecipients.any { it.id == result.id }
                    
                    ListItem(
                        headlineContent = { Text(result.name) },
                        supportingContent = { Text(result.description) },
                        trailingContent = {
                            IconButton(
                                onClick = { 
                                    if (alreadySelected) {
                                        viewModel.removeReceiver(result.id)
                                    } else {
                                        viewModel.addReceiver(result.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (alreadySelected) 
                                        Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = if (alreadySelected) 
                                        "Quitar" else "Añadir"
                                )
                            }
                        }
                    )
                    Divider()
                }
            }
            
            // Botón de enviar
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.sendMessage() },
                enabled = uiState.canSendMessage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar mensaje")
            }
        }
    }
} 