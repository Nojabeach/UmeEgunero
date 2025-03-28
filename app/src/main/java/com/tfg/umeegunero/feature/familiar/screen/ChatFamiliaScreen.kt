package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatFamiliaScreen(
    navController: NavController,
    profesorId: String = "prof123" // Parámetro de ejemplo, en una app real vendría de la navegación
) {
    // Estado para el mensaje que se está escribiendo
    var mensaje by remember { mutableStateOf("") }
    
    // Datos de ejemplo para el profesor
    val profesor = remember {
        Profesor(
            id = profesorId,
            nombre = "Laura",
            apellidos = "García Martínez",
            asignatura = "Tutora",
            fotoPerfil = "" // URL vacía para este ejemplo
        )
    }
    
    // Datos de ejemplo para la conversación
    val mensajes = remember {
        listOf(
            Mensaje(
                id = "1",
                remitente = TipoRemitente.PROFESOR,
                texto = "Buenos días, quería comentarle que Ana ha participado muy activamente en clase hoy. Estamos muy contentos con su progreso.",
                timestamp = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1); set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 30) }.timeInMillis,
                leido = true
            ),
            Mensaje(
                id = "2",
                remitente = TipoRemitente.FAMILIA,
                texto = "Muchas gracias por la información. En casa también está muy motivada con las actividades de clase.",
                timestamp = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1); set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 15) }.timeInMillis,
                leido = true
            ),
            Mensaje(
                id = "3",
                remitente = TipoRemitente.PROFESOR,
                texto = "¿Podría Ana traer mañana sus materiales para el proyecto de ciencias? Vamos a comenzar a trabajar en ello.",
                timestamp = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0) }.timeInMillis,
                leido = true
            ),
            Mensaje(
                id = "4",
                remitente = TipoRemitente.FAMILIA,
                texto = "Claro, me aseguraré de que lleve todo preparado. ¿Necesita algo especial además de los materiales que indicó la semana pasada?",
                timestamp = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 30) }.timeInMillis,
                leido = true
            ),
            Mensaje(
                id = "5",
                remitente = TipoRemitente.PROFESOR,
                texto = "Con los materiales indicados será suficiente. Gracias por su colaboración.",
                timestamp = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 11); set(Calendar.MINUTE, 0) }.timeInMillis,
                leido = false
            )
        )
    }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
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
                                text = profesor.nombre.first().toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "${profesor.nombre} ${profesor.apellidos}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = profesor.asignatura,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { /* Opciones del chat */ }) {
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
                // Los mensajes en orden cronológico inverso para que el más reciente esté abajo
                items(mensajes.sortedByDescending { it.timestamp }) { mensaje ->
                    MensajeItem(mensaje = mensaje)
                    
                    Spacer(modifier = Modifier.height(8.dp))
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
                        onClick = { /* Adjuntar archivos */ }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    TextField(
                        value = mensaje,
                        onValueChange = { mensaje = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                // Enviar mensaje
                                if (mensaje.isNotEmpty()) {
                                    // Aquí iría la lógica para enviar el mensaje
                                    mensaje = ""
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
                            // Enviar mensaje
                            if (mensaje.isNotEmpty()) {
                                // Aquí iría la lógica para enviar el mensaje
                                mensaje = ""
                                keyboardController?.hide()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MensajeItem(
    mensaje: Mensaje
) {
    val isRemitente = mensaje.remitente == TipoRemitente.FAMILIA
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isRemitente) Alignment.End else Alignment.Start
    ) {
        // Burbuja del mensaje
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isRemitente) 16.dp else 4.dp,
                        bottomEnd = if (isRemitente) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isRemitente) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = mensaje.texto,
                color = if (isRemitente) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Hora del mensaje y estado de lectura
        Row(
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Formato de la hora
            val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaFormateada = dateFormatter.format(Date(mensaje.timestamp))
            
            Text(
                text = horaFormateada,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            if (isRemitente) {
                Spacer(modifier = Modifier.width(4.dp))
                
                Icon(
                    imageVector = if (mensaje.leido) Icons.Default.Done else Icons.Default.DoneAll,
                    contentDescription = if (mensaje.leido) "Leído" else "Enviado",
                    modifier = Modifier.size(16.dp),
                    tint = if (mensaje.leido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Modelos de datos para el chat
data class Profesor(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val asignatura: String,
    val fotoPerfil: String
)

data class Mensaje(
    val id: String,
    val remitente: TipoRemitente,
    val texto: String,
    val timestamp: Long,
    val leido: Boolean
)

enum class TipoRemitente {
    PROFESOR,
    FAMILIA
} 