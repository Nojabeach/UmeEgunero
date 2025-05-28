package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.feature.common.mensajeria.ChatViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import android.widget.Toast
import java.time.Month

/**
 * Tipos de adjuntos soportados
 */
enum class AttachmentType {
    IMAGE,
    PDF,
    DOCUMENT
}

/**
 * Pantalla de chat para profesores
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatProfesorScreen(
    navController: NavController,
    familiarId: String,
    conversacionId: String,
    alumnoId: String? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var inputMessage by remember { mutableStateOf("") }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showAttachmentPreview by remember { mutableStateOf(false) }
    var previewAttachmentType by remember { mutableStateOf<AttachmentType?>(null) }
    var previewAttachmentUrl by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Inicializar el ViewModel con los datos de la conversación
    LaunchedEffect(familiarId, conversacionId) {
        Timber.d("🔄 ChatProfesorScreen: Inicializando chat con conversacionId=$conversacionId, familiarId=$familiarId")
        viewModel.inicializar(conversacionId, familiarId, alumnoId)
        
        // Actualización periódica mientras la pantalla esté visible
        while(true) {
            // Esperar 15 segundos antes de la siguiente actualización
            kotlinx.coroutines.delay(15000)
            Timber.d("🔄 ChatProfesorScreen: Actualización periódica de mensajes")
            viewModel.inicializar(conversacionId, familiarId, alumnoId)
        }
    }
    
    // Receptor de broadcast para actualizar cuando lleguen nuevos mensajes
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                Timber.d("📬 ChatProfesorScreen: Broadcast recibido para actualizar mensajes")
                
                // Verificar si el mensaje es para esta conversación
                val messageConversationId = intent?.getStringExtra("conversationId") ?: ""
                if (messageConversationId.isNotEmpty() && messageConversationId != conversacionId) {
                    Timber.d("📬 ChatProfesorScreen: Mensaje para otra conversación: $messageConversationId vs $conversacionId")
                    return
                }
                
                // Actualizar la conversación
                viewModel.inicializar(conversacionId, familiarId, alumnoId)
                
                // Feedback táctil para notificar al usuario
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico en broadcast")
                }
            }
        }
        
        // Registrar para recibir broadcasts de nuevos mensajes
        val filter = android.content.IntentFilter().apply {
            addAction("com.tfg.umeegunero.NUEVO_MENSAJE_UNIFICADO")
            addAction("com.tfg.umeegunero.NUEVO_MENSAJE_CHAT")
        }
        context.registerReceiver(receiver, filter)
        
        // Limpiar al destruir
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Timber.e(e, "Error al deregistrar receptor de broadcast en ChatProfesorScreen")
            }
        }
    }
    
    // Observador del ciclo de vida para actualizar cuando la pantalla vuelva a primer plano
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                Timber.d("🔄 ChatProfesorScreen: Actualizando al volver a pantalla visible")
                viewModel.inicializar(conversacionId, familiarId, alumnoId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Observar el estado de la UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Lista de plantillas de mensajes
    val messageTemplates = remember {
        listOf(
            "Buenos días, le informo que hoy su hijo/a ha utilizado todos los pañales que tenía disponibles. Por favor, recuerde traer más para mañana.",
            "Hoy su hijo/a ha comido muy bien durante la hora de la comida, ha probado todos los alimentos y ha terminado casi todo.",
            "Su hijo/a ha tenido dificultad para comer hoy. Solo ha probado un poco de la comida y no ha querido el postre.",
            "Le comunico que hoy su hijo/a ha comenzado a decir nuevas palabras como \"agua\" y \"más\". Estamos muy contentos con su progreso.",
            "Hoy su hijo/a ha mordido a un compañero durante el juego. Hemos hablado con él/ella sobre esta conducta y le agradeceríamos reforzar en casa que no debe morder.",
            "Su hijo/a ha tenido un pequeño accidente durante el patio y se ha hecho una pequeña herida. La hemos curado y ha estado bien el resto del día.",
            "Le comunico que hoy su hijo/a ha tenido un episodio de llanto durante la siesta. Lo hemos calmado y finalmente ha podido descansar.",
            "¡Buenas noticias! Su hijo/a ha empezado a mostrar interés en el orinal. Sería bueno reforzar este comportamiento también en casa.",
            "Hoy hemos trabajado con colores y formas, y su hijo/a ha mostrado mucho interés y habilidad reconociendo el color rojo y el círculo.",
            "Su hijo/a ha estado más irritable de lo normal hoy. Ha tenido varios momentos de llanto y frustración. ¿Ha notado algo similar en casa?"
        )
    }
    
    // Agrupar mensajes por fecha para mostrar encabezados
    val groupedMessages = remember(uiState.mensajes) {
        uiState.mensajes.groupBy { message ->
            val timestamp = message.timestamp
            val calendar = Calendar.getInstance()
            calendar.time = timestamp.toDate()
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            Triple(year, month, day)
        }
    }
    
    // Mostrar un diálogo de carga mientras se cargan los datos
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ProfesorColor)
        }
        return
    }
    
    // Mostrar un mensaje de error si hay algún problema
    uiState.error?.let { error ->
        val context = LocalContext.current
        LaunchedEffect(error) {
            Timber.e("Error en ChatProfesorScreen: $error")
            
            // Si es un error de conexión, mostrar opción de reintentar
            if (error.contains("Error al cargar mensajes", ignoreCase = true) ||
                error.contains("network", ignoreCase = true) ||
                error.contains("conexión", ignoreCase = true)) {
                // No volver atrás automáticamente para errores de conexión
                Toast.makeText(
                    context, 
                    "$error\nIntentando reconectar automáticamente...", 
                    Toast.LENGTH_LONG
                ).show()
            } else if (error.contains("No se pudo cargar la conversación", ignoreCase = true) ||
                       error.contains("Error al cargar el participante", ignoreCase = true)) {
                // Para otros errores críticos, sí volver atrás
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                kotlinx.coroutines.delay(1500)
                navController.popBackStack()
            } else {
                // Para otros errores, solo mostrar el mensaje
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.participante?.nombre?.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = uiState.participante?.nombre ?: "Usuario",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = uiState.alumnoId?.let { "Familiar de alumno" } ?: "Familiar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        navController.popBackStack() 
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { 
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        /* Opciones del chat */
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                // Iterar sobre los mensajes agrupados por fecha
                for ((date, messagesForDate) in groupedMessages) {
                    // Mostrar los mensajes de la fecha
                    items(messagesForDate.sortedByDescending { it.timestamp }) { message ->
                        val isFromMe = message.senderId == uiState.usuario?.dni
                        
                        MessageItem(
                            message = message,
                            isFromMe = isFromMe,
                            onAttachmentClick = { url, type ->
                                previewAttachmentUrl = url
                                previewAttachmentType = type
                                showAttachmentPreview = true
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Mostrar el encabezado de la fecha
                    item {
                        DateHeader(date = getFormattedDate(date))
                    }
                }
                
                item {
                    // Fecha de inicio de conversación
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
            
            // Campo de texto para escribir el mensaje
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
                    IconButton(
                        onClick = { 
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (e: Exception) {
                                Timber.e(e, "Error al realizar feedback háptico")
                            }
                            showAttachmentOptions = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntar",
                            tint = ProfesorColor
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (e: Exception) {
                                Timber.e(e, "Error al realizar feedback háptico")
                            }
                            showTemplatesDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Plantillas",
                            tint = ProfesorColor
                        )
                    }
                    
                    TextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al realizar feedback háptico")
                                }
                                if (inputMessage.isNotEmpty()) {
                                    viewModel.sendMessage(inputMessage)
                                    inputMessage = ""
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 3
                    )
                    
                    IconButton(
                        onClick = {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } catch (e: Exception) {
                                Timber.e(e, "Error al realizar feedback háptico")
                            }
                            if (inputMessage.isNotEmpty()) {
                                viewModel.sendMessage(inputMessage)
                                inputMessage = ""
                                keyboardController?.hide()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = ProfesorColor
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para seleccionar plantillas de mensaje
    if (showTemplatesDialog) {
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = { Text("Plantillas de mensajes") },
            text = {
                LazyColumn {
                    items(messageTemplates) { template ->
                        TemplateItem(
                            template = template,
                            onTemplateSelected = {
                                inputMessage = template
                                showTemplatesDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
    
    // Diálogo para seleccionar opciones de adjuntos
    if (showAttachmentOptions) {
        AttachmentOptionsDialog(
            onDismiss = { showAttachmentOptions = false },
            onImageSelected = { uri ->
                // Implementar lógica para manejar la selección de imágenes
                showAttachmentOptions = false
            },
            onDocumentSelected = { uri ->
                // Implementar lógica para manejar la selección de documentos
                showAttachmentOptions = false
            }
        )
    }
    
    // Diálogo de vista previa de adjuntos
    if (showAttachmentPreview && previewAttachmentType != null) {
        AttachmentPreviewDialog(
            attachmentUrl = previewAttachmentUrl,
            attachmentType = previewAttachmentType!!,
            onDismiss = { showAttachmentPreview = false }
        )
    }
}

@Composable
fun MessageItem(
    message: UnifiedMessage,
    isFromMe: Boolean,
    onAttachmentClick: (String, AttachmentType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Burbuja del mensaje
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isFromMe) ProfesorColor
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = if (isFromMe) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Mostrar adjunto si existe
                if (message.attachments.isNotEmpty()) {
                    val url = message.attachments.first()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val attachmentType = when {
                        url.endsWith(".jpg", true) || 
                        url.endsWith(".jpeg", true) || 
                        url.endsWith(".png", true) -> AttachmentType.IMAGE
                        url.endsWith(".pdf", true) -> AttachmentType.PDF
                        else -> AttachmentType.DOCUMENT
                    }
                    
                    AttachmentItem(
                        attachmentUrl = url,
                        attachmentType = attachmentType,
                        onAttachmentClick = { onAttachmentClick(url, attachmentType) }
                    )
                }
            }
        }
        
        // Hora del mensaje y estado de lectura
        Row(
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Formato de la hora
            val timestamp = message.timestamp.toDate()
            val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaFormateada = dateFormatter.format(timestamp)
            
            Text(
                text = horaFormateada,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            if (isFromMe) {
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = if (message.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Done,
                    contentDescription = if (message.status == MessageStatus.READ) "Leído" else "Enviado",
                    modifier = Modifier.size(16.dp),
                    tint = if (message.status == MessageStatus.READ) ProfesorColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AttachmentItem(
    attachmentUrl: String,
    attachmentType: AttachmentType,
    onAttachmentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.1f))
            .clickable { onAttachmentClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (attachmentType) {
                AttachmentType.IMAGE -> Icons.Default.Image
                AttachmentType.PDF -> Icons.Default.PictureAsPdf
                AttachmentType.DOCUMENT -> Icons.Default.Description
            },
            contentDescription = "Adjunto",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (attachmentType) {
                    AttachmentType.IMAGE -> "Imagen"
                    AttachmentType.PDF -> "Documento PDF"
                    AttachmentType.DOCUMENT -> "Documento"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = attachmentUrl.substringAfterLast('/'),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Descargar",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AttachmentOptionsDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onDocumentSelected: (Uri) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjuntar archivo") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttachmentOption(
                    icon = Icons.Default.Image,
                    title = "Imagen",
                    onClick = {
                        // Implementar lógica para seleccionar imagen
                        onDismiss()
                    }
                )
                
                AttachmentOption(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Documento PDF",
                    onClick = {
                        // Implementar lógica para seleccionar PDF
                        onDismiss()
                    }
                )
                
                AttachmentOption(
                    icon = Icons.Default.Description,
                    title = "Documento",
                    onClick = {
                        // Implementar lógica para seleccionar documento
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AttachmentOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ProfesorColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun AttachmentPreviewDialog(
    attachmentUrl: String,
    attachmentType: AttachmentType,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Vista previa")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (attachmentType) {
                    AttachmentType.IMAGE -> {
                        // Implementar vista previa de imagen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Vista previa de imagen no disponible")
                        }
                    }
                    AttachmentType.PDF -> {
                        // Implementar vista previa de PDF
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                modifier = Modifier.size(48.dp),
                                tint = ProfesorColor
                            )
                            
                            Text(
                                text = "Vista previa de PDF no disponible",
                                modifier = Modifier.padding(top = 64.dp)
                            )
                        }
                    }
                    AttachmentType.DOCUMENT -> {
                        // Implementar vista previa de documento
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Documento",
                                modifier = Modifier.size(48.dp),
                                tint = ProfesorColor
                            )
                            
                            Text(
                                text = "Vista previa de documento no disponible",
                                modifier = Modifier.padding(top = 64.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = attachmentUrl.substringAfterLast('/'),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun TemplateItem(
    template: String,
    onTemplateSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTemplateSelected() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = "Plantilla",
                tint = ProfesorColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = template,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Función para formatear la fecha para los encabezados
fun getFormattedDate(date: Triple<Int, Int, Int>): String {
    val (year, month, day) = date
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)
    
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)
    
    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Hoy"
        
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Ayer"
        
        else -> {
            val formatter = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES"))
            formatter.format(calendar.time)
        }
    }
} 