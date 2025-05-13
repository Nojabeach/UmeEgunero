package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.MessagePriority
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.ComunicadoDetailViewModel
import com.tfg.umeegunero.ui.components.ErrorContent
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para mostrar los detalles de un comunicado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadoDetailScreen(
    comunicadoId: String,
    onBack: () -> Unit,
    viewModel: ComunicadoDetailViewModel = hiltViewModel()
) {
    // Inicializar el ViewModel con el ID del comunicado
    LaunchedEffect(comunicadoId) {
        viewModel.loadComunicado(comunicadoId)
    }

    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Comunicado") },
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
                        message = "Cargando comunicado..."
                    )
                }
                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error ?: "Error desconocido",
                        onRetry = { viewModel.loadComunicado(comunicadoId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.comunicado != null -> {
                    ComunicadoContent(
                        comunicado = uiState.comunicado!!,
                        onConfirmRead = { viewModel.confirmRead() }
                    )
                }
                else -> {
                    // Estado vacío o no encontrado
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontró el comunicado",
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
 * Contenido principal del comunicado
 */
@Composable
fun ComunicadoContent(
    comunicado: UnifiedMessage,
    onConfirmRead: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = comunicado.timestamp.toDate().let { dateFormat.format(it) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Cabecera del comunicado
        ComunicadoHeader(comunicado, formattedDate)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Contenido del comunicado
        Text(
            text = comunicado.content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Adjuntos (si hay)
        if (comunicado.attachments.isNotEmpty()) {
            Text(
                text = "Archivos adjuntos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            comunicado.attachments.forEach { attachment ->
                AdjuntoItem(attachment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información adicional y metadatos
        if (comunicado.metadata.isNotEmpty()) {
            Text(
                text = "Información adicional",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            comunicado.metadata.forEach { (key, value) ->
                if (key !in listOf("requireConfirmation") && value.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botón de confirmación si se requiere
        if (comunicado.metadata["requireConfirmation"] == "true" && !comunicado.isRead) {
            Button(
                onClick = onConfirmRead,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Confirmar lectura")
            }
        } else if (comunicado.isRead) {
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
                    text = "Comunicado leído",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Cabecera del comunicado con título, remitente, fecha y prioridad
 */
@Composable
fun ComunicadoHeader(
    comunicado: UnifiedMessage,
    formattedDate: String
) {
    Column {
        // Título del comunicado
        Text(
            text = comunicado.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Información del remitente y fecha
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales del remitente
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comunicado.senderName.firstOrNull()?.toString() ?: "U",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = comunicado.senderName,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Indicador de prioridad
            if (comunicado.priority != MessagePriority.NORMAL) {
                PriorityLabel(comunicado.priority)
            }
        }
        
        // Divider
        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Etiqueta de prioridad
 */
@Composable
fun PriorityLabel(priority: MessagePriority) {
    val (color, text) = when (priority) {
        MessagePriority.HIGH -> Pair(Color(0xFFF57C00), "Alta")
        MessagePriority.URGENT -> Pair(Color(0xFFE53935), "Urgente")
        MessagePriority.LOW -> Pair(Color(0xFF4CAF50), "Baja")
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, "Normal")
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (priority == MessagePriority.URGENT) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
            }
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

/**
 * Item de un archivo adjunto
 */
@Composable
fun AdjuntoItem(attachment: Map<String, String>) {
    val nombre = attachment["name"] ?: "Archivo adjunto"
    val url = attachment["url"] ?: ""
    val tipo = attachment["type"] ?: "application/octet-stream"
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono según tipo de archivo
            val icon = when {
                tipo.contains("pdf") -> Icons.Default.PictureAsPdf
                tipo.contains("image") -> Icons.Default.Image
                tipo.contains("word") || tipo.contains("document") -> Icons.Default.Description
                else -> Icons.Default.InsertDriveFile
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            FilledTonalIconButton(
                onClick = { /* Implementar descarga o visualización */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Descargar"
                )
            }
        }
    }
} 