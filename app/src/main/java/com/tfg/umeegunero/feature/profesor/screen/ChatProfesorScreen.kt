package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri as AndroidUri

enum class AttachmentType {
    IMAGE, PDF, AUDIO, LOCATION
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean,
    val readTimestamp: Long?,
    val attachmentType: AttachmentType? = null,
    val attachmentUrl: String? = null
)

/**
 * Pantalla de chat para profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatProfesorScreen(
    navController: NavController,
    familiarId: String = "familiar123",
    familiarNombre: String = "María García (Madre de Miguel)"
) {
    var inputMessage by remember { mutableStateOf("") }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    
    // Estado para la simulación de notificaciones en tiempo real
    var notificationEnabled by remember { mutableStateOf(true) }
    var lastSeen by remember { mutableStateOf("Hoy 14:30") }
    var isTyping by remember { mutableStateOf(false) }
    
    // Simulación de adjuntos
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var selectedDocumentName by remember { mutableStateOf<String?>(null) }
    var hasAttachment by remember { mutableStateOf(false) }
    
    // Estado para la visualización de adjuntos
    var showAttachmentPreview by remember { mutableStateOf(false) }
    var previewAttachmentType by remember { mutableStateOf<AttachmentType?>(null) }
    var previewAttachmentUrl by remember { mutableStateOf("") }
    
    // Estado de los mensajes
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                senderId = "profesor456",
                text = "Buenos días, quería comentarle que Miguel ha mejorado mucho en matemáticas esta semana.",
                timestamp = System.currentTimeMillis() - 86400000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 80000000
            ),
            ChatMessage(
                id = "2",
                senderId = familiarId,
                text = "Me alegra mucho escuchar eso. Hemos estado practicando en casa con las operaciones básicas.",
                timestamp = System.currentTimeMillis() - 85000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 84000000
            ),
            ChatMessage(
                id = "3",
                senderId = "profesor456",
                text = "Se nota el esfuerzo. También quería comentarle que mañana tendremos una actividad especial de ciencias.",
                timestamp = System.currentTimeMillis() - 84000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 83000000
            ),
            ChatMessage(
                id = "4",
                senderId = familiarId,
                text = "Perfecto. ¿Necesita llevar algún material específico?",
                timestamp = System.currentTimeMillis() - 82000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 81000000
            ),
            ChatMessage(
                id = "5",
                senderId = "profesor456",
                text = "No será necesario, el colegio proporcionará todo lo necesario para la actividad.",
                timestamp = System.currentTimeMillis() - 80000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 79000000
            ),
            ChatMessage(
                id = "6",
                senderId = familiarId,
                text = "Entendido. Por cierto, ¿cuándo será la próxima reunión de padres?",
                timestamp = System.currentTimeMillis() - 70000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 65000000
            ),
            ChatMessage(
                id = "7",
                senderId = "profesor456",
                text = "Está programada para el próximo viernes a las 17:00. Le enviaré la información detallada por correo.",
                timestamp = System.currentTimeMillis() - 60000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 55000000
            ),
            ChatMessage(
                id = "8",
                senderId = familiarId,
                text = "Perfecto, ahí estaremos. ¿Hablará sobre el progreso individual de cada niño?",
                timestamp = System.currentTimeMillis() - 40000000,
                isRead = true,
                readTimestamp = System.currentTimeMillis() - 35000000
            ),
            ChatMessage(
                id = "9",
                senderId = "profesor456",
                text = "Sí, repasaremos el progreso general del grupo y luego habrá tiempo para consultas individuales.",
                timestamp = System.currentTimeMillis() - 30000000,
                isRead = false,
                readTimestamp = null,
                attachmentType = AttachmentType.PDF,
                attachmentUrl = "informe_reunion.pdf"
            )
        )
    }
    
    // Simulación de mensajes marcados como leídos automáticamente
    LaunchedEffect(messages) {
        val unreadMessages = messages.filter { !it.isRead && it.senderId == familiarId }
        if (unreadMessages.isNotEmpty()) {
            delay(2000) // Simular retraso en la lectura
            unreadMessages.forEach { message ->
                val index = messages.indexOf(message)
                if (index != -1) {
                    messages[index] = message.copy(isRead = true, readTimestamp = System.currentTimeMillis())
                }
            }
        }
    }
    
    // Agrupar mensajes por fecha para mostrar encabezados
    val groupedMessages = remember(messages) {
        messages.sortedBy { it.timestamp }.groupBy { message ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = message.timestamp
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(calendar.time)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = familiarNombre.first().toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = familiarNombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isTyping) {
                                    Text(
                                        text = "Escribiendo...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "Última vez: $lastSeen",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
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
                    IconButton(onClick = { /* Llamada */ }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Llamar"
                        )
                    }
                    
                    IconButton(onClick = { showNotificationsDialog = true }) {
                        Icon(
                            imageVector = if (notificationEnabled) 
                                Icons.Default.Notifications
                            else 
                                Icons.Default.NotificationsOff,
                            contentDescription = "Configurar notificaciones"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Visualización de vista previa de archivos adjuntos
                AnimatedVisibility(
                    visible = hasAttachment,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedImageUri != null) 
                                    Icons.Default.Image 
                                else 
                                    Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = selectedImageUri?.substringAfterLast('/') ?: selectedDocumentName ?: "Archivo adjunto",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Text(
                                text = "Toca para previsualizar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(
                            onClick = { 
                                selectedImageUri = null
                                selectedDocumentName = null
                                hasAttachment = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar archivo"
                            )
                        }
                    }
                }
                
                // Barra de entrada de mensajes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentOptions = !showAttachmentOptions }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntar archivo"
                        )
                    }
                    
                    TextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        placeholder = { Text("Mensaje") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = CircleShape,
                        maxLines = 4
                    )
                    
                    if (inputMessage.isNotEmpty() || hasAttachment) {
                        IconButton(
                            onClick = {
                                if (inputMessage.isNotEmpty() || hasAttachment) {
                                    val newMessage = ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        senderId = "profesor456",
                                        text = inputMessage,
                                        timestamp = System.currentTimeMillis(),
                                        isRead = false,
                                        readTimestamp = null,
                                        attachmentType = if (selectedImageUri != null) 
                                            AttachmentType.IMAGE 
                                        else if (selectedDocumentName != null) 
                                            AttachmentType.PDF 
                                        else 
                                            null,
                                        attachmentUrl = selectedImageUri ?: selectedDocumentName
                                    )
                                    messages.add(newMessage)
                                    inputMessage = ""
                                    selectedImageUri = null
                                    selectedDocumentName = null
                                    hasAttachment = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar mensaje"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { /* Agregar nota de voz */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Grabar audio"
                            )
                        }
                    }
                }
            }
            
            // Menú de opciones para adjuntar archivos
            AnimatedVisibility(
                visible = showAttachmentOptions,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Imagen",
                        onClick = { 
                            selectedImageUri = "content://media/sample_image.jpg"
                            selectedDocumentName = null
                            hasAttachment = true
                            showAttachmentOptions = false
                        }
                    )
                    
                    AttachmentOption(
                        icon = Icons.Default.Camera,
                        label = "Cámara",
                        onClick = {
                            selectedImageUri = "content://media/new_photo.jpg"
                            selectedDocumentName = null
                            hasAttachment = true
                            showAttachmentOptions = false
                        }
                    )
                    
                    AttachmentOption(
                        icon = Icons.Default.Description,
                        label = "Documento",
                        onClick = {
                            selectedImageUri = null
                            selectedDocumentName = "informe_alumno.pdf"
                            hasAttachment = true
                            showAttachmentOptions = false
                        }
                    )
                    
                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        label = "Ubicación",
                        onClick = {
                            inputMessage = "Mi ubicación actual: Centro Educativo UmeEgunero"
                            showAttachmentOptions = false
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            // Mostrar mensajes agrupados por fecha
            groupedMessages.entries.sortedByDescending { it.key }.forEach { (date, messagesForDate) ->
                items(messagesForDate.reversed()) { message ->
                    val isFromMe = message.senderId != familiarId
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
                    ) {
                        // Mostrar el mensaje
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    enabled = message.attachmentType != null,
                                    onClick = {
                                        if (message.attachmentType != null && message.attachmentUrl != null) {
                                            previewAttachmentType = message.attachmentType
                                            previewAttachmentUrl = message.attachmentUrl
                                            showAttachmentPreview = true
                                        }
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column {
                                // Contenido del mensaje (texto)
                                Text(
                                    text = message.text,
                                    color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                // Si hay un archivo adjunto, mostrar una vista previa
                                if (message.attachmentType != null && message.attachmentUrl != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(
                                                if (isFromMe) 
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) 
                                                else 
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (message.attachmentType) {
                                                AttachmentType.IMAGE -> Icons.Default.Image
                                                AttachmentType.PDF -> Icons.Default.Description
                                                AttachmentType.AUDIO -> Icons.Default.Mic
                                                AttachmentType.LOCATION -> Icons.Default.LocationOn
                                            },
                                            contentDescription = null,
                                            tint = if (isFromMe) 
                                                MaterialTheme.colorScheme.onPrimary 
                                            else 
                                                MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = when (message.attachmentType) {
                                                AttachmentType.IMAGE -> "Imagen"
                                                AttachmentType.PDF -> message.attachmentUrl
                                                AttachmentType.AUDIO -> "Nota de voz (1:30)"
                                                AttachmentType.LOCATION -> "Ubicación"
                                            },
                                            color = if (isFromMe) 
                                                MaterialTheme.colorScheme.onPrimary 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                
                                // Mostrar la hora del mensaje
                                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val messageTime = formatter.format(Date(message.timestamp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        text = messageTime,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isFromMe) 
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                                    )
                                    
                                    if (isFromMe) {
                                        Icon(
                                            imageVector = if (message.isRead) 
                                                Icons.Default.Check
                                            else 
                                                Icons.Default.Done,
                                            contentDescription = if (message.isRead) "Leído" else "Enviado",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (message.isRead) 
                                                Color(0xFF34B7F1) 
                                            else 
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Mostrar la hora de lectura si está leído
                        if (isFromMe && message.isRead && message.readTimestamp != null) {
                            val readFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val readTime = readFormatter.format(Date(message.readTimestamp))
                            
                            Text(
                                text = "Leído a las $readTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                            )
                        }
                    }
                }
                
                // Mostrar encabezado de fecha
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = formatDateHeader(date),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Diálogo para la previsualización de adjuntos
        if (showAttachmentPreview && previewAttachmentType != null) {
            AlertDialog(
                onDismissRequest = { showAttachmentPreview = false },
                text = {
                    AttachmentPreview(
                        attachmentType = previewAttachmentType!!,
                        attachmentUrl = previewAttachmentUrl,
                        onDismiss = { showAttachmentPreview = false }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showAttachmentPreview = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        
        // Diálogo para configurar notificaciones
        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = { Text("Configuración de notificaciones") },
                text = {
                    Column {
                        Text("Elige cómo quieres recibir notificaciones de este chat:")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = notificationEnabled,
                                onCheckedChange = { notificationEnabled = it }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = if (notificationEnabled) 
                                    "Activar notificaciones" 
                                else 
                                    "Silenciar notificaciones",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (notificationEnabled) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable { },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = true,
                                        onClick = { }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Column {
                                        Text("Todas las notificaciones")
                                        Text(
                                            text = "Recibirás todas las notificaciones de este chat",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable { },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = false,
                                        onClick = { }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Column {
                                        Text("Solo menciones")
                                        Text(
                                            text = "Solo recibirás notificaciones cuando seas mencionado",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showNotificationsDialog = false }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showNotificationsDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Efecto para simular el estado de "escribiendo..."
        LaunchedEffect(Unit) {
            while (true) {
                delay(8000)
                isTyping = true
                delay(3000)
                isTyping = false
                delay(2000)
                
                // Simular recepción de nuevo mensaje
                if (notificationEnabled) {
                    val newMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = familiarId,
                        text = "Le confirmo que asistiremos a la reunión del viernes. ¿Hay algún tema específico que debamos preparar de antemano?",
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        readTimestamp = null
                    )
                    messages.add(newMessage)
                    
                    // Actualizar hora de última conexión
                    lastSeen = "En línea"
                    delay(5000)
                    lastSeen = "Hace un momento"
                }
                
                delay(15000)
            }
        }
    }
}

@Composable
fun AttachmentOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// Burbuja de mensaje de chat
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isFromMe: Boolean
) {
    Column(
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Contenido del mensaje
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            color = if (isFromMe) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(12.dp)
            ) {
                // Mostrar adjunto si existe
                if (message.attachmentType != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFromMe) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (message.attachmentType) {
                            AttachmentType.IMAGE -> {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = if (isFromMe) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AttachmentType.PDF -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = if (isFromMe) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = message.attachmentUrl ?: "documento.pdf",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isFromMe) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Button(
                                        onClick = { /* Abrir documento */ },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFromMe) 
                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f) 
                                            else 
                                                MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            text = "Ver documento",
                                            color = if (isFromMe) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                            else -> {
                                // Otros tipos de adjuntos
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text,
                        color = if (isFromMe) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Hora y estado del mensaje
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFromMe) 
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    
                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Icon(
                            imageVector = if (message.isRead) 
                                Icons.Default.Check 
                            else 
                                Icons.Default.Done,
                            contentDescription = if (message.isRead) "Leído" else "Enviado",
                            modifier = Modifier.size(16.dp),
                            tint = if (message.isRead) 
                                Color(0xFF34B7F1) 
                            else 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Indicador de tiempo de lectura para mis mensajes
        if (isFromMe && message.isRead && message.readTimestamp != null) {
            Text(
                text = "Leído: ${formatTime(message.readTimestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
        }
    }
}

// Función para formatear la hora de un mensaje
private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

// Función para formatear el encabezado de fecha
private fun formatDateHeader(dateStr: String): String {
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val yesterday = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
        Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    )
    
    return when (dateStr) {
        today -> "Hoy"
        yesterday -> "Ayer"
        else -> {
            // Convertir de formato dd/MM/yyyy a un formato más legible
            val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val targetFormat = SimpleDateFormat("d 'de' MMMM", Locale("es"))
            val date = originalFormat.parse(dateStr)
            date?.let { targetFormat.format(it) } ?: dateStr
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatProfesorScreenPreview() {
    UmeEguneroTheme {
        ChatProfesorScreen(
            navController = rememberNavController(),
            familiarId = "123",
            familiarNombre = "Juan García"
        )
    }
}

@Composable
private fun AttachmentPreview(
    attachmentType: AttachmentType,
    attachmentUrl: String,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(1500) // Simular carga
        isLoading = false
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (attachmentType) {
                AttachmentType.IMAGE -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray)
                            ) {
                                // Simulación de una imagen cargada
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.Center),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                AttachmentType.PDF -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = attachmentUrl,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { /* Simulación de apertura */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text("Abrir documento")
                            }
                        }
                    }
                }
                
                AttachmentType.AUDIO -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Simulación de control de audio
                            Slider(
                                value = 0f,
                                onValueChange = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0:00")
                                Text("1:30")
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            IconButton(
                                onClick = { /* Reproducir audio */ },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                
                AttachmentType.LOCATION -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Centro Educativo UmeEgunero",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Av. Principal 123, Ciudad",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { /* Abrir mapa */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text("Ver en mapa")
                            }
                        }
                    }
                }
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
} 