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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.SearchResultItem

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
                        RecipientChip(
                            name = recipient.name,
                            type = recipient.type,
                            onRemove = { viewModel.removeReceiver(recipient.id) }
                        )
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
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.availableMessageTypes) { type ->
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
                Text(
                    text = "Resultados (${uiState.searchResults.count { it.type != "HEADER" }})",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Mostrar resultados en formato árbol
                SearchResultsTree(
                    results = uiState.searchResults,
                    searchQuery = uiState.searchQuery,
                    onAddRecipient = { id, name -> viewModel.addReceiver(id, name) }
                )
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

@Composable
fun SearchResultsTree(
    results: List<SearchResultItem>,
    searchQuery: String,
    onAddRecipient: (String, String) -> Unit
) {
    // Agrupar por categorías para crear un árbol
    val filteredResults = if (searchQuery.isNotEmpty()) {
        val query = searchQuery.lowercase()
        
        // Crear estructura de árbol con filtrado
        val filteredItems = results.filter { it.type != "HEADER" && 
            (it.name.lowercase().contains(query) || it.description.lowercase().contains(query)) 
        }
        
        // Ahora mantener solo los headers que tienen items
        val headers = results.filter { it.type == "HEADER" }
        
        // Asignar items a sus headers correspondientes
        val headersWithItems = mutableMapOf<String, MutableList<SearchResultItem>>()
        
        // Inicializar para cada header
        headers.forEach { header ->
            headersWithItems[header.id] = mutableListOf()
        }
        
        // Asignar items a sus headers
        filteredItems.forEach { item ->
            // Buscar el header apropiado según el tipo del item
            when (item.type) {
                TipoUsuario.ADMIN_CENTRO.toString() -> {
                    headers.find { it.id == "header_profesores" }?.let { header ->
                        headersWithItems[header.id]?.add(item)
                    }
                }
                TipoUsuario.PROFESOR.toString() -> {
                    // Buscar el header más específico basado en la descripción
                    val matchingHeader = headers.find { header ->
                        if (header.id.startsWith("header_sin_asignacion") && 
                            item.description.contains("sin asignación", ignoreCase = true)) {
                            return@find true
                        }
                        
                        if (!header.id.startsWith("header_familia_") && 
                            header.id != "header_profesores" && 
                            header.id != "header_familiares") {
                            // Extraer información del header
                            val headerInfo = header.name.substringAfter("👨‍🏫 ")
                            // Verificar si la descripción del item contiene esta info
                            if (item.description.contains(headerInfo, ignoreCase = true)) {
                                return@find true
                            }
                        }
                        false
                    }
                    
                    matchingHeader?.let {
                        headersWithItems[it.id]?.add(item)
                    } ?: run {
                        // Si no encontramos header específico, usar el general
                        headers.find { it.id == "header_profesores" }?.let { header ->
                            headersWithItems[header.id]?.add(item)
                        }
                    }
                }
                TipoUsuario.FAMILIAR.toString() -> {
                    // Buscar header basado en la clase/curso del alumno
                    var found = false
                    headers.forEach { header ->
                        if (header.id.startsWith("header_familia_")) {
                            val claseCursoInfo = header.name.substringAfter("👪 ")
                            if (item.description.contains(claseCursoInfo, ignoreCase = true)) {
                                headersWithItems[header.id]?.add(item)
                                found = true
                            }
                        }
                    }
                    
                    if (!found) {
                        // Si no encontramos header específico, usar el general
                        headers.find { it.id == "header_familiares" }?.let { header ->
                            headersWithItems[header.id]?.add(item)
                        }
                    }
                }
            }
        }
        
        // Construir lista final con headers que tienen items
        val result = mutableListOf<SearchResultItem>()
        headers.forEach { header ->
            val items = headersWithItems[header.id] ?: emptyList()
            if (items.isNotEmpty()) {
                result.add(header)
                result.addAll(items)
            }
        }
        
        result
    } else {
        // Sin filtro, mostrar todo
        results
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(filteredResults.size) { index ->
            val item = filteredResults[index]
            
            if (item.type == "HEADER") {
                // Mostrar encabezado con padding según nivel
                CategoryHeader(
                    title = item.name,
                    description = item.description,
                    level = when {
                        item.id.startsWith("header_familia_") -> 2
                        item.id.startsWith("header_curso_") -> 1
                        else -> 0
                    }
                )
            } else {
                // Mostrar item con padding según nivel y categoría
                val level = when {
                    item.type == TipoUsuario.ADMIN_CENTRO.toString() -> 1
                    item.type == TipoUsuario.PROFESOR.toString() -> 2
                    item.type == TipoUsuario.FAMILIAR.toString() -> 3
                    else -> 1
                }
                
                RecipientItem(
                    item = item,
                    level = level,
                    onAdd = { onAddRecipient(item.id, item.name) }
                )
            }
        }
    }
}

@Composable
fun CategoryHeader(
    title: String,
    description: String,
    level: Int = 0
) {
    val paddingStart = 8.dp * level
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(start = paddingStart)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (description.isNotEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecipientItem(
    item: SearchResultItem,
    level: Int = 0,
    onAdd: () -> Unit
) {
    val startPadding = 8.dp * level
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding)
            .padding(vertical = 4.dp),
        onClick = onAdd,
        colors = CardDefaults.cardColors(
            containerColor = when(item.type) {
                TipoUsuario.ADMIN_CENTRO.toString() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                TipoUsuario.PROFESOR.toString() -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                TipoUsuario.FAMILIAR.toString() -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales del usuario
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when(item.type) {
                            TipoUsuario.ADMIN_CENTRO.toString() -> MaterialTheme.colorScheme.primary
                            TipoUsuario.PROFESOR.toString() -> MaterialTheme.colorScheme.secondary
                            TipoUsuario.FAMILIAR.toString() -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            FilledIconButton(
                onClick = onAdd,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = when(item.type) {
                        TipoUsuario.ADMIN_CENTRO.toString() -> MaterialTheme.colorScheme.primary
                        TipoUsuario.PROFESOR.toString() -> MaterialTheme.colorScheme.secondary
                        TipoUsuario.FAMILIAR.toString() -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir destinatario",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun RecipientChip(
    name: String,
    type: String,
    onRemove: () -> Unit
) {
    val backgroundColor = when(type) {
        TipoUsuario.ADMIN_CENTRO.toString() -> MaterialTheme.colorScheme.primaryContainer
        TipoUsuario.PROFESOR.toString() -> MaterialTheme.colorScheme.secondaryContainer
        TipoUsuario.FAMILIAR.toString() -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when(type) {
        TipoUsuario.ADMIN_CENTRO.toString() -> MaterialTheme.colorScheme.onPrimaryContainer
        TipoUsuario.PROFESOR.toString() -> MaterialTheme.colorScheme.onSecondaryContainer
        TipoUsuario.FAMILIAR.toString() -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val iconColor = when(type) {
        TipoUsuario.ADMIN_CENTRO.toString() -> MaterialTheme.colorScheme.primary
        TipoUsuario.PROFESOR.toString() -> MaterialTheme.colorScheme.secondary
        TipoUsuario.FAMILIAR.toString() -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    SuggestionChip(
        onClick = { /* No action */ },
        label = { 
            Text(
                text = name,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        border = null,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = backgroundColor
        )
    )
    
    // Botón de eliminar separado del chip
    IconButton(
        onClick = onRemove,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Quitar",
            modifier = Modifier.size(16.dp),
            tint = textColor
        )
    }
} 