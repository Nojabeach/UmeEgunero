package com.tfg.umeegunero.feature.common.mensajeria

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.ProfesorColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal para el chat entre usuarios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversacionId: String,
    participanteId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    esFamiliar: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Inicializar ViewModel
    LaunchedEffect(conversacionId, participanteId) {
        viewModel.inicializar(conversacionId, participanteId)
    }
    
    // Mostrar errores en Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "OK"
                )
                viewModel.borrarError()
            }
        }
    }
    
    val mensajesListState = rememberLazyListState()
    
    // Scroll al último mensaje cuando llegan nuevos mensajes
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            mensajesListState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar del participante
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (esFamiliar) ProfesorColor else FamiliarColor,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            uiState.participante?.let { participante ->
                                if (false) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = participante.nombre.firstOrNull()?.toString() ?: "",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            } ?: Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Información del participante
                        Column {
                            uiState.participante?.let { participante ->
                                Text(
                                    text = "${participante.nombre} ${participante.apellidos}",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                // Si hay un alumno asociado a la conversación, mostrar su nombre
                                if (uiState.alumnoId != null) {
                                    Text(
                                        text = "Alumno: ${uiState.alumnoId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } ?: Text(
                                text = "Cargando...",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator(fullScreen = true)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Lista de mensajes
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        state = mensajesListState,
                        reverseLayout = true,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Mensajes en orden cronológico inverso (más reciente abajo)
                        items(uiState.mensajes.sortedByDescending { it.timestamp }) { mensaje ->
                            MensajeItem(
                                mensaje = mensaje,
                                esEmisor = mensaje.emisorId == uiState.usuario?.dni
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Si no hay mensajes, mostrar indicación
                        if (uiState.mensajes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay mensajes. ¡Envía el primero!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Indicador de inicio de conversación
                        if (uiState.mensajes.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "Inicio de conversación",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Área de adjuntos
                    AnimatedVisibility(
                        visible = uiState.adjuntos.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        AdjuntosPreview(
                            adjuntos = uiState.adjuntos,
                            onRemoveAdjunto = { viewModel.eliminarAdjunto(it) }
                        )
                    }
                    
                    // Barra de entrada de mensaje
                    MensajeInputBar(
                        texto = uiState.textoMensaje,
                        onTextoChange = { viewModel.actualizarTextoMensaje(it) },
                        onEnviar = { viewModel.enviarMensaje() },
                        onAdjuntoClick = { uri -> viewModel.añadirAdjunto(uri) },
                        enviando = uiState.enviandoMensaje
                    )
                }
            }
        }
    }
}

/**
 * Item que representa un mensaje individual
 */
@Composable
fun MensajeItem(
    mensaje: Mensaje,
    esEmisor: Boolean
) {
    val backgroundColor = if (esEmisor) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (esEmisor) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val alignment = if (esEmisor) Alignment.End else Alignment.Start
    
    val dateFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(mensaje.timestamp) {
        mensaje.timestamp?.toDate()?.let { dateFormatter.format(it) } ?: ""
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Contenido del mensaje
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esEmisor) 16.dp else 4.dp,
                bottomEnd = if (esEmisor) 4.dp else 16.dp
            ),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(12.dp)
            ) {
                Text(
                    text = mensaje.texto,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Mostrar adjuntos si hay
                if (mensaje.adjuntos?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mensaje.adjuntos.forEach { url ->
                            AdjuntoItem(url = url)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Información del mensaje (hora y estado)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    
                    if (esEmisor) {
                        Icon(
                            imageVector = if (mensaje.leido) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = if (mensaje.leido) "Leído" else "Enviado",
                            modifier = Modifier.size(14.dp),
                            tint = if (mensaje.leido) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de entrada de mensaje con soporte para texto y adjuntos
 */
@Composable
fun MensajeInputBar(
    texto: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onAdjuntoClick: (Uri) -> Unit,
    enviando: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Launcher para seleccionar archivos adjuntos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAdjuntoClick(it) }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón para añadir adjuntos
            IconButton(
                onClick = { 
                    filePickerLauncher.launch("*/*")
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Adjuntar archivo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Campo de texto
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(text = "Escribe un mensaje...")
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onEnviar()
                        focusManager.clearFocus()
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botón de enviar
            Button(
                onClick = {
                    onEnviar()
                    focusManager.clearFocus()
                },
                enabled = !enviando && (texto.isNotEmpty() || onAdjuntoClick != {}),
                shape = CircleShape,
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                if (enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar mensaje"
                    )
                }
            }
        }
    }
}

/**
 * Componente para previsualizar adjuntos antes de enviar
 */
@Composable
fun AdjuntosPreview(
    adjuntos: List<Uri>,
    onRemoveAdjunto: (Uri) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 120.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(adjuntos) { uri ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = uri.lastPathSegment ?: "Archivo adjunto",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                IconButton(
                    onClick = { onRemoveAdjunto(uri) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar adjunto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Componente para mostrar un adjunto en un mensaje
 */
@Composable
fun AdjuntoItem(url: String) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .clickable {
                // Implementar visualización del adjunto
                val intent = android.content.Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono basado en el tipo de archivo
        val fileIcon = when {
            url.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
            url.endsWith(".jpg", ignoreCase = true) || 
            url.endsWith(".jpeg", ignoreCase = true) || 
            url.endsWith(".png", ignoreCase = true) -> Icons.Default.Image
            url.endsWith(".doc", ignoreCase = true) || 
            url.endsWith(".docx", ignoreCase = true) -> Icons.Default.Description
            else -> Icons.Default.InsertDriveFile
        }
        
        Icon(
            imageVector = fileIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Nombre del archivo
        val fileName = url.substringAfterLast("/").substringBefore("?")
        Text(
            text = fileName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
} 