package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean,
    val isRead: Boolean = false,
    val mediaUrl: String? = null
)

/**
 * Pantalla de chat para profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatProfesorScreen(
    navController: NavController,
    familiarId: String? = null,
    nombreFamiliar: String? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    var message by remember { mutableStateOf("") }
    var showAttachOptions by remember { mutableStateOf(false) }
    
    // Datos de ejemplo - En producción deberían venir de un ViewModel
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hola, quería consultar sobre la evolución de mi hijo en clase", System.currentTimeMillis() - 3600000 * 10, false, true),
            ChatMessage("Claro, Miguel está avanzando bien en lectura y actividades grupales", System.currentTimeMillis() - 3600000 * 9, true),
            ChatMessage("¿Ha mejorado su comportamiento durante la siesta?", System.currentTimeMillis() - 3600000 * 8, false, true),
            ChatMessage("Sí, esta semana ha estado más tranquilo durante las siestas", System.currentTimeMillis() - 3600000 * 7, true),
            ChatMessage("Me alegra escuchar eso, hemos trabajado en su rutina de sueño en casa", System.currentTimeMillis() - 3600000 * 6, false, true),
            ChatMessage("Se nota el esfuerzo. También quería comentarte que la próxima semana haremos una actividad especial de pintura, por si quieres enviarle una camiseta que se pueda manchar", System.currentTimeMillis() - 3600000 * 5, true)
        )
    }
    
    // Efecto para desplazar automáticamente al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar del familiar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (nombreFamiliar?.firstOrNull() ?: "F").toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            // Nombre del familiar
                            Text(
                                text = nombreFamiliar ?: "Familiar de alumno",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            // Estado (en línea/última vez)
                            Text(
                                text = "En línea",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Abrir menú de opciones */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Column {
                // Panel de opciones para adjuntar archivos
                AnimatedVisibility(
                    visible = showAttachOptions,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AttachmentOption(
                            icon = Icons.Default.Image,
                            label = "Foto",
                            color = Color(0xFF34C759)
                        ) {
                            // Seleccionar imagen
                            showAttachOptions = false
                        }
                        
                        AttachmentOption(
                            icon = Icons.Default.AttachFile,
                            label = "Archivo",
                            color = Color(0xFF007AFF)
                        ) {
                            // Adjuntar archivo
                            showAttachOptions = false
                        }
                        
                        AttachmentOption(
                            icon = Icons.Default.Mic,
                            label = "Audio",
                            color = Color(0xFFFF9500)
                        ) {
                            // Grabar audio
                            showAttachOptions = false
                        }
                    }
                }
                
                // Campo de mensaje
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón para adjuntar
                    IconButton(
                        onClick = { showAttachOptions = !showAttachOptions }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adjuntar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Campo de texto
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        placeholder = { Text("Escribe un mensaje...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    
                    // Botón de enviar
                    IconButton(
                        onClick = {
                            if (message.isNotEmpty()) {
                                messages.add(
                                    ChatMessage(
                                        content = message,
                                        isFromMe = true
                                    )
                                )
                                message = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (messages.isEmpty()) {
                // Mensaje cuando no hay mensajes
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            tint = Color.Unspecified
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay mensajes",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Envía un mensaje para iniciar la conversación",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Lista de mensajes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Agrupar mensajes por fecha
                    val groupedMessages = messages.groupBy { 
                        val date = Date(it.timestamp)
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    }
                    
                    groupedMessages.forEach { (date, messagesForDate) ->
                        // Encabezado de fecha
                        item {
                            DateHeader(date)
                        }
                        
                        // Mensajes del día
                        items(messagesForDate) { message ->
                            MessageItem(message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatDateHeader(date),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

fun formatDateHeader(dateStr: String): String {
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val yesterday = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
        Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    )
    
    return when (dateStr) {
        today -> "Hoy"
        yesterday -> "Ayer"
        else -> dateStr
    }
}

@Composable
fun MessageItem(message: ChatMessage) {
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isFromMe) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.surfaceVariant
    
    val textColor = if (message.isFromMe) 
        MaterialTheme.colorScheme.onPrimary 
    else 
        MaterialTheme.colorScheme.onSurfaceVariant
    
    val maxWidth = 0.8f // 80% del ancho disponible
    
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = formatter.format(Date(message.timestamp))
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Burbuja de mensaje
        Box(
            modifier = Modifier
                .widthIn(max = (maxWidth * 400).dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromMe) 4.dp else 16.dp
                    )
                )
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Column {
                // Contenido del mensaje
                Text(
                    text = message.content,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Hora y estado
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.isFromMe) 
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    if (message.isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(
                                id = if (message.isRead) 
                                    R.drawable.app_icon // Debería ser un icono de doble check
                                else 
                                    R.drawable.app_icon // Debería ser un icono de check simple
                            ),
                            contentDescription = if (message.isRead) "Leído" else "Enviado",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatProfesorScreenPreview() {
    UmeEguneroTheme {
        ChatProfesorScreen(
            navController = rememberNavController(),
            familiarId = "123",
            nombreFamiliar = "Juan García"
        )
    }
} 