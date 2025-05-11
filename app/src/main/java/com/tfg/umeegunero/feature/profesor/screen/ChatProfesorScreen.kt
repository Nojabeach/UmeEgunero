package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Analytics
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
import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.InteractionStatus
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isRead: Boolean,
    val readTimestamp: Long?,
    val attachmentType: AttachmentType? = null,
    val attachmentUrl: String? = null,
    val interactionStatus: InteractionStatus = InteractionStatus.NONE,
    val isTranslated: Boolean = false,
    val originalText: String? = null,
    // Propiedades para resaltado de texto en búsquedas
    val highlightedText: String? = null,
    val highlightQuery: String? = null
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
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showTranslationDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var autoTranslateEnabled by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Español") }
    
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
    
    // Lista de plantillas de mensajes
    val messageTemplates = remember {
        listOf(
            "Buenos días, le informo que hoy su hijo/a ha completado todas las tareas correctamente.",
            "Estimado/a familiar, quería comunicarle que mañana tendremos una actividad especial en clase. No es necesario traer material adicional.",
            "Le recuerdo que la próxima semana tendremos reuniones individuales. Puede reservar su cita a través de la aplicación.",
            "Su hijo/a ha mostrado una gran mejoría en el área de matemáticas esta semana.",
            "Le comunico que su hijo/a no ha completado los deberes asignados para hoy."
        )
    }
    
    // Programación de mensajes
    var scheduledDate by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("") }
    var scheduledMessage by remember { mutableStateOf("") }
    var hasScheduledMessages by remember { mutableStateOf(false) }
    
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
    
    // Simulación de interacciones con mensajes
    LaunchedEffect(messages) {
        // Buscar el mensaje más reciente enviado por el profesor que tenga un adjunto
        val lastMessageWithAttachment = messages.findLast { 
            it.senderId == "profesor456" && it.attachmentType != null && it.isRead &&
            it.interactionStatus == InteractionStatus.NONE
        }
        
        if (lastMessageWithAttachment != null) {
            val index = messages.indexOf(lastMessageWithAttachment)
            // Simular que el familiar está interactuando con el adjunto
            delay(5000)
            if (index != -1) {
                messages[index] = lastMessageWithAttachment.copy(
                    interactionStatus = InteractionStatus.DOWNLOADING
                )
                
                delay(3000)
                messages[index] = messages[index].copy(
                    interactionStatus = InteractionStatus.INTERACTION
                )
            }
        }
        
        // Simular tiempo de lectura para mensajes largos
        val unreadLongMessages = messages.filter { 
            !it.isRead && it.senderId == familiarId && it.text.length > 100
        }
        
        unreadLongMessages.forEach { message ->
            val index = messages.indexOf(message)
            if (index != -1) {
                // Simular que está tomando tiempo leer el mensaje
                messages[index] = message.copy(
                    interactionStatus = InteractionStatus.READING
                )
                delay(4000)
                messages[index] = messages[index].copy(
                    isRead = true,
                    readTimestamp = System.currentTimeMillis(),
                    interactionStatus = InteractionStatus.VIEWED
                )
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
    
    // Mapa con traducciones de mensajes (simulado)
    val translations = remember {
        mapOf(
            "6" to mapOf(
                "original" to "Entendido. Por cierto, ¿cuándo será la próxima reunión de padres?",
                "es" to "Entendido. Por cierto, ¿cuándo será la próxima reunión de padres?",
                "en" to "Understood. By the way, when will the next parent-teacher meeting be?",
                "fr" to "Compris. Au fait, quand aura lieu la prochaine réunion des parents?",
                "ar" to "مفهوم. بالمناسبة ، متى سيكون الاجتماع القادم للآباء والمعلمين؟"
            ),
            "8" to mapOf(
                "original" to "Perfecto, ahí estaremos. ¿Hablará sobre el progreso individual de cada niño?",
                "es" to "Perfecto, ahí estaremos. ¿Hablará sobre el progreso individual de cada niño?",
                "en" to "Perfect, we'll be there. Will you talk about each child's individual progress?",
                "fr" to "Parfait, nous serons présents. Parlerez-vous des progrès individuels de chaque enfant?",
                "ar" to "ممتاز ، سنكون هناك. هل ستتحدث عن التقدم الفردي لكل طفل؟"
            )
        )
    }
    
    // Mapear códigos de idioma a nombre completo
    val languageNames = remember {
        mapOf(
            "es" to "Español",
            "en" to "English",
            "fr" to "Français",
            "ar" to "العربية"
        )
    }
    
    // Detectar idioma y traducir mensajes automáticamente si está habilitado
    var translatedMessages = remember(messages, autoTranslateEnabled, selectedLanguage) {
        if (!autoTranslateEnabled || selectedLanguage == "Español") {
            messages
        } else {
            messages.map { message ->
                val langCode = languageNames.entries.find { it.value == selectedLanguage }?.key ?: "es"
                
                // Solo traducimos mensajes del familiar (simulado)
                if (message.senderId == familiarId && translations.containsKey(message.id)) {
                    val translationsMap = translations[message.id] ?: mapOf("original" to message.text)
                    val translatedText = translationsMap[langCode] ?: message.text
                    
                    // Si hay traducción disponible
                    if (translatedText != message.text) {
                        message.copy(
                            text = translatedText,
                            isTranslated = true,
                            originalText = translationsMap["original"] ?: message.text
                        )
                    } else {
                        message
                    }
                } else {
                    message
                }
            }
        }
    }
    
    // Estado para la búsqueda
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Filtrar mensajes según la búsqueda
    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isEmpty()) {
            messages
        } else {
            val query = searchQuery.lowercase()
            messages.filter { message ->
                message.text.lowercase().contains(query) ||
                message.id.contains(query) // También podríamos buscar por otros campos relevantes
            }
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                // Barra de navegación normal
                if (!isSearchActive) {
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
                            // Icono de búsqueda
                            IconButton(onClick = { 
                                isSearchActive = true
                                // Dar tiempo para la composición
                                kotlinx.coroutines.MainScope().launch {
                                    delay(100)
                                    focusRequester.requestFocus()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar mensajes"
                                )
                            }
                        
                            // Icono de estadísticas
                            IconButton(onClick = { showStatsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Analytics,
                                    contentDescription = "Estadísticas de comunicación"
                                )
                            }
                            
                            IconButton(onClick = { showTranslationDialog = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Translate,
                                    contentDescription = "Traducir mensajes"
                                )
                            }
                            
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
                } else {
                    // Barra de búsqueda
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                placeholder = { Text("Buscar en la conversación...") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Search,
                                    keyboardType = KeyboardType.Text
                                ),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        keyboardController?.hide()
                                    }
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false
                                searchQuery = ""
                                keyboardController?.hide()
                            }) {
                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Cerrar búsqueda"
                                )
                            }
                        },
                        actions = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                
                // Mostrar contador de resultados si hay búsqueda activa
                if (isSearchActive && searchQuery.isNotEmpty()) {
                    val resultsCount = filteredMessages.size
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$resultsCount resultados encontrados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
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
                        Row {
                            IconButton(
                                onClick = { showTemplatesDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Bookmark,
                                    contentDescription = "Plantillas de mensajes"
                                )
                            }
                            
                            IconButton(
                                onClick = { /* Agregar nota de voz */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Grabar audio"
                                )
                            }
                            
                            IconButton(
                                onClick = { showScheduleDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = "Programar mensaje"
                                )
                            }
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
            // Usar los mensajes filtrados en lugar de los originales
            val messagesSource = if (isSearchActive) filteredMessages else messages
            
            // Mostrar mensaje de "No se encontraron resultados" si es necesario
            if (isSearchActive && searchQuery.isNotEmpty() && filteredMessages.isEmpty()) {
                item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                            .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                                imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                                text = "No se encontraron mensajes con \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        }
                    }
                }
            } else {
                // Agrupar y mostrar mensajes como antes, pero usando la fuente filtrada
                val groupedMessages = messagesSource.groupBy { message ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = message.timestamp
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.format(calendar.time)
                }
                
                groupedMessages.entries.sortedByDescending { it.key }.forEach { (date, messagesForDate) ->
                    items(messagesForDate.reversed()) { message ->
                        val isFromMe = message.senderId != familiarId
                        
                        // Buscar si hay traducción disponible
                        val translatedMessage = if (!isFromMe && autoTranslateEnabled && selectedLanguage != "Español") {
                            translatedMessages.find { it.id == message.id } ?: message
                        } else {
                            message
                        }
                        
                        // Resaltar texto que coincide con la búsqueda si hay búsqueda activa
                        val displayMessage = if (isSearchActive && searchQuery.isNotEmpty()) {
                            translatedMessage.copy(
                                highlightQuery = searchQuery
                            )
                        } else {
                            translatedMessage
                        }
                        
                        // Mostrar mensaje con/sin resaltado según corresponda
                        MessageItem(
                            message = displayMessage,
                            isFromMe = isFromMe,
                            onImageClick = { url, type ->
                                previewAttachmentUrl = url ?: ""
                                previewAttachmentType = type
                                showAttachmentPreview = true
                            }
                        )
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
        
        // Diálogo para plantillas de mensajes
        if (showTemplatesDialog) {
            AlertDialog(
                onDismissRequest = { showTemplatesDialog = false },
                title = { Text("Plantillas de mensajes") },
                text = {
                    Column {
                        Text(
                            "Seleccione una plantilla para enviar un mensaje rápido:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(messageTemplates) { template ->
                                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                                        .clickable {
                                            inputMessage = template
                                            showTemplatesDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Text(
                                        text = template,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                
                                if (messageTemplates.indexOf(template) < messageTemplates.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedButton(
                                    onClick = { /* Añadir nueva plantilla */ },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text("Crear nueva plantilla")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showTemplatesDialog = false }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        }
        
        // Diálogo para programar mensajes
        if (showScheduleDialog) {
            var selectedDate by remember { mutableStateOf("") }
            var selectedTime by remember { mutableStateOf("") }
            var messageText by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showScheduleDialog = false },
                title = { Text("Programar mensaje") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Configure cuándo desea enviar el mensaje:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Selector de fecha
                            OutlinedButton(onClick = { /* Mostrar selector de fecha */ }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Seleccionar fecha"
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = if (selectedDate.isEmpty()) "Fecha" else selectedDate
                                    )
                                }
                            }
                            
                            // Selector de hora
                            OutlinedButton(onClick = { /* Mostrar selector de hora */ }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Seleccionar hora"
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = if (selectedTime.isEmpty()) "Hora" else selectedTime
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Campo para el mensaje
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Mensaje a enviar") },
                            minLines = 3,
                            maxLines = 5
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Opción para usar plantilla
                        OutlinedButton(
                            onClick = { 
                                showScheduleDialog = false
                                showTemplatesDialog = true
                                // Lógica para volver a abrir el diálogo de programación después
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text("Usar plantilla")
                        }
                        
                        if (hasScheduledMessages) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Tienes un mensaje programado",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        
                                        Text(
                                            text = "Para el $scheduledDate a las $scheduledTime",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    
                                    IconButton(onClick = { hasScheduledMessages = false }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar recordatorio",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            // Simulación de programación
                            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty() && messageText.isNotEmpty()) {
                                scheduledDate = selectedDate
                                scheduledTime = selectedTime
                                scheduledMessage = messageText
                                hasScheduledMessages = true
                                showScheduleDialog = false
                            }
                        },
                        enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty() && messageText.isNotEmpty()
                    ) {
                        Text("Programar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showScheduleDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Simular la selección de fecha y hora (en una app real esto usaría DatePicker y TimePicker)
        LaunchedEffect(showScheduleDialog) {
            if (showScheduleDialog) {
                delay(500) // Simular tiempo de carga
                // En una app real, esto sería manejado por un DatePicker
                // y los valores vendrían de la selección del usuario
                if (scheduledDate.isEmpty()) {
                    scheduledDate = "25/06/2023"
                    scheduledTime = "16:30"
                }
            }
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
        
        // Diálogo de traducción automática
        if (showTranslationDialog) {
            AlertDialog(
                onDismissRequest = { showTranslationDialog = false },
                title = { Text("Configurar traducción") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = autoTranslateEnabled,
                                onCheckedChange = { autoTranslateEnabled = it }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Column {
                                Text(
                                    text = "Traducción automática",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Text(
                                    text = "Traducir automáticamente los mensajes recibidos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Idioma de traducción:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Lista de idiomas disponibles
                        Column {
                            languageNames.values.forEach { language ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedLanguage = language }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedLanguage == language,
                                        onClick = { selectedLanguage = language }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = language,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Información sobre la traducción
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "Las traducciones son generadas automáticamente y pueden no ser precisas al 100%.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showTranslationDialog = false }
                    ) {
                        Text("Aplicar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            autoTranslateEnabled = false
                            selectedLanguage = "Español"
                            showTranslationDialog = false
                        }
                    ) {
                        Text("Restablecer")
                    }
                }
            )
        }
        
        // Diálogo de estadísticas de comunicación
        if (showStatsDialog) {
            AlertDialog(
                onDismissRequest = { showStatsDialog = false },
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text("Estadísticas de comunicación")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Calcular estadísticas básicas
                        val totalMessages = messages.size
                        val sentMessages = messages.count { it.senderId == "profesor456" }
                        val receivedMessages = totalMessages - sentMessages
                        val readMessages = messages.count { it.senderId == "profesor456" && it.isRead }
                        val unreadMessages = sentMessages - readMessages
                        val responseRate = if (sentMessages > 0) 
                            (receivedMessages.toFloat() / sentMessages) * 100 
                        else 
                            0f
                        val avgResponseTime = "2.5 horas" // Simulado
                        
                        // Mostrar resumen general
                        Text(
                            text = "Resumen de comunicación con ${familiarNombre}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Métricas clave
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.AutoMirrored.Filled.Send,
                                value = sentMessages.toString(),
                                label = "Enviados"
                            )
                            
                            StatItem(
                                icon = Icons.Default.Inbox,
                                value = receivedMessages.toString(),
                                label = "Recibidos"
                            )
                            
                            StatItem(
                                icon = Icons.Default.Check,
                                value = readMessages.toString(),
                                label = "Leídos"
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Indicadores de rendimiento
                        Text(
                            text = "Indicadores de comunicación",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            // Tasa de respuesta
                            PerformanceIndicator(
                                label = "Tasa de respuesta",
                                value = "%.1f%%".format(responseRate),
                                progress = responseRate / 100
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Tiempo medio de respuesta
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Tiempo medio de respuesta:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Text(
                                    text = avgResponseTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Indicador de mensajes sin leer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MarkEmailUnread,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (unreadMessages > 0) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Mensajes por leer:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Text(
                                    text = unreadMessages.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unreadMessages > 0) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Actividad reciente
                        Text(
                            text = "Actividad reciente",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Gráfico simplificado de actividad (simulación)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Simulación de gráfico de barras de actividad
                                ActivityBar(height = 0.3f, label = "L")
                                ActivityBar(height = 0.5f, label = "M")
                                ActivityBar(height = 0.2f, label = "X")
                                ActivityBar(height = 0.8f, label = "J")
                                ActivityBar(height = 1.0f, label = "V", isHighlighted = true)
                                ActivityBar(height = 0.4f, label = "S")
                                ActivityBar(height = 0.1f, label = "D")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Consejos para mejorar la comunicación
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "Consejo: Se observa mayor actividad los viernes. Considere programar recordatorios semanales para ese día para aumentar el engagement.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showStatsDialog = false }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                
                else -> {
                    // Caso para NONE u otros tipos futuros
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Archivo desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
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

// Componente para mostrar una métrica individual en las estadísticas
@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
            Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
                    Text(
            text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
            text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
    }
}

// Componente para mostrar un indicador de rendimiento con barra de progreso
@Composable
private fun PerformanceIndicator(
    label: String,
    value: String,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = value,
                        style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// Componente para barra de actividad en gráfico
@Composable
private fun ActivityBar(
    height: Float,
    label: String,
    isHighlighted: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
                        Box(
                            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(height)
                .background(
                    if (isHighlighted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Función para resaltar texto de búsqueda
@Composable
fun HighlightedText(
    text: String,
    query: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    highlightColor: Color = MaterialTheme.colorScheme.primary
) {
    if (query.isEmpty()) {
        Text(text = text, style = style, color = color)
        return
    }
    
    val sections = mutableListOf<Pair<String, Boolean>>() // (texto, resaltado)
    var remainingText = text
    val queryLowercase = query.lowercase()
    
    while (remainingText.isNotEmpty()) {
        val startIndex = remainingText.lowercase().indexOf(queryLowercase)
        if (startIndex == -1) {
            sections.add(Pair(remainingText, false))
            break
        }
        
        // Añadir texto antes de la coincidencia
        if (startIndex > 0) {
            sections.add(Pair(remainingText.substring(0, startIndex), false))
        }
        
        // Añadir la coincidencia
        val match = remainingText.substring(startIndex, startIndex + query.length)
        sections.add(Pair(match, true))
        
        // Actualizar texto restante
        remainingText = if (startIndex + query.length < remainingText.length) {
            remainingText.substring(startIndex + query.length)
        } else {
            ""
        }
    }
    
    Row(modifier = Modifier.fillMaxWidth()) {
        sections.forEach { (sectionText, isHighlighted) ->
            Box(
                modifier = Modifier
                    .background(
                        if (isHighlighted) highlightColor.copy(alpha = 0.2f) else Color.Transparent
                    )
                        ) {
                            Text(
                    text = sectionText,
                    style = style,
                    color = color,
                    fontWeight = if (isHighlighted) FontWeight.Bold else null
                )
            }
        }
    }
}

// Componente para mostrar un mensaje individual
@Composable
fun MessageItem(
    message: ChatMessage,
    isFromMe: Boolean,
    onImageClick: (String?, AttachmentType?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Contenido del mensaje
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
        ) {
            // Burbuja de mensaje
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isFromMe) 12.dp else 0.dp,
                    topEnd = if (isFromMe) 0.dp else 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                ),
                color = if (isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    // Contenido del mensaje (texto)
                    if (message.highlightQuery != null && message.highlightQuery.isNotEmpty()) {
                        HighlightedText(
                            text = message.text,
                            query = message.highlightQuery,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = message.text,
                            color = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Mostrar si es un mensaje traducido
                    if (message.isTranslated && message.originalText != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Traducido",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Si hay un archivo adjunto, mostrar una vista previa
                    if (message.attachmentType != null && message.attachmentUrl != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    if (isFromMe) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                                .clickable {
                                    onImageClick(message.attachmentUrl, message.attachmentType)
                                }
                        ) {
                            Icon(
                                imageVector = when (message.attachmentType) {
                                    AttachmentType.IMAGE -> Icons.Default.Image
                                    AttachmentType.PDF -> Icons.Default.Description
                                    AttachmentType.AUDIO -> Icons.Default.Mic
                                    AttachmentType.LOCATION -> Icons.Default.LocationOn
                                    else -> Icons.Default.InsertDriveFile
                                },
                                contentDescription = null,
                                tint = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = when (message.attachmentType) {
                                    AttachmentType.IMAGE -> "Imagen"
                                    AttachmentType.PDF -> message.attachmentUrl
                                    AttachmentType.AUDIO -> "Nota de voz"
                                    AttachmentType.LOCATION -> "Ubicación"
                                    else -> "Archivo adjunto"
                                },
                                color = if (isFromMe) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Mostrar la hora y estado del mensaje
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        // Mostrar la hora del mensaje
                        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val messageTime = formatter.format(Date(message.timestamp))
                        
                        Text(
                            text = messageTime,
                                style = MaterialTheme.typography.bodySmall,
                            color = if (isFromMe) 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Si es un mensaje mío, mostrar indicador de leído/entregado
                        if (isFromMe) {
                            Icon(
                                imageVector = if (message.isRead) 
                                    Icons.Default.DoneAll 
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
        }
        
        // Mostrar la información de lectura si es un mensaje mío y ha sido leído
        if (isFromMe && message.isRead && message.readTimestamp != null) {
            val readFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val readTime = readFormatter.format(Date(message.readTimestamp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
            ) {
                when (message.interactionStatus) {
                    InteractionStatus.READING -> {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Leyendo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Leyendo ahora...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    InteractionStatus.DOWNLOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Descargando archivo...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    InteractionStatus.INTERACTION -> {
                        val attachmentType = message.attachmentType
                        Icon(
                            imageVector = when (attachmentType) {
                                AttachmentType.PDF -> Icons.Default.Description
                                AttachmentType.IMAGE -> Icons.Default.Image
                                AttachmentType.AUDIO -> Icons.Default.Mic
                                AttachmentType.LOCATION -> Icons.Default.LocationOn
                                else -> Icons.Default.Info
                            },
                            contentDescription = "Interactuando",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Viendo ${
                                when (attachmentType) {
                                    AttachmentType.PDF -> "documento"
                                    AttachmentType.IMAGE -> "imagen"
                                    AttachmentType.AUDIO -> "audio"
                                    AttachmentType.LOCATION -> "ubicación"
                                    else -> "contenido"
                                }
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    InteractionStatus.VIEWED -> {
                        Text(
                            text = "Leído a las $readTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    else -> {
                        Text(
                            text = "Leído a las $readTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
} 