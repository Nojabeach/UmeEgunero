package com.tfg.umeegunero.feature.familiar.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.feature.familiar.viewmodel.ChatViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf("") }

    // Estado para la lista de mensajes
    val listState = rememberLazyListState()
    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1
        }
    }

    // Scroll al último mensaje cuando se carga la pantalla o hay nuevos mensajes
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.receptorNombre ?: "Chat",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Lista de mensajes
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState
                ) {
                    if (uiState.mensajes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay mensajes aún. Envía un mensaje para comenzar la conversación.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.mensajes) { mensaje ->
                            MessageItem(
                                mensaje = mensaje,
                                esMio = mensaje.emisorId == uiState.familiarId,
                                nombreEmisor = if (mensaje.emisorId == uiState.familiarId)
                                    "Tú"
                                else
                                    uiState.receptorNombre ?: "Profesor"
                            )
                        }

                        // Espacio al final para que el último mensaje sea más visible
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }}

                // Campo para escribir mensajes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mensaje,
                        onValueChange = { mensaje = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { Text("Escribe un mensaje...") },
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (mensaje.isNotBlank()) {
                                    viewModel.enviarMensaje(mensaje)
                                    mensaje = ""
                                }
                            }
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (mensaje.isNotBlank()) {
                                viewModel.enviarMensaje(mensaje)
                                mensaje = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(FamiliarColor, CircleShape),
                        enabled = mensaje.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            tint = Color.White
                        )
                    }
                }
            }

            // Indicador de carga
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FamiliarColor)
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    mensaje: Mensaje,
    esMio: Boolean,
    nombreEmisor: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Text(
            text = nombreEmisor,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )

        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (esMio) 16.dp else 4.dp,
                topEnd = if (esMio) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (esMio)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = mensaje.texto,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatMessageTime(mensaje.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun MensajeItem(
    mensaje: Mensaje,
    isFromCurrentUser: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (isFromCurrentUser) 16.dp else 4.dp,
                topEnd = if (isFromCurrentUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromCurrentUser)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = mensaje.texto,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatMessageTime(mensaje.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

fun formatMessageTime(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(date)
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    UmeEguneroTheme {
        ChatScreenPreviewContent()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ChatScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ChatScreenPreviewContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenPreviewContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }
    
    val mensajes = listOf(
        Mensaje(
            id = "1",
            emisorId = "profesor1",
            receptorId = "familiar1",
            timestamp = com.google.firebase.Timestamp.now(),
            texto = "Buenos días, quería informarle sobre el progreso de su hijo en clase",
            leido = true
        ),
        Mensaje(
            id = "2",
            emisorId = "familiar1",
            receptorId = "profesor1",
            timestamp = com.google.firebase.Timestamp.now(),
            texto = "Gracias por la información. ¿Ha mejorado en matemáticas?",
            leido = true
        ),
        Mensaje(
            id = "3",
            emisorId = "profesor1",
            receptorId = "familiar1",
            timestamp = com.google.firebase.Timestamp.now(),
            texto = "Sí, ha mejorado bastante. Especialmente en la resolución de problemas.",
            leido = false
        )
    )
    
    val usuarioActualId = "familiar1"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Profesor García",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Escribe un mensaje...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                
                IconButton(
                    onClick = { },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar mensaje",
                        tint = FamiliarColor
                    )
                }
            }
        }
    ) { paddingValues ->
        if (mensajes.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                state = scrollState,
                reverseLayout = false
            ) {
                items(mensajes) { mensaje ->
                    MensajeItem(
                        mensaje = mensaje,
                        isFromCurrentUser = mensaje.emisorId == usuarioActualId
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay mensajes. Envía un mensaje para comenzar la conversación.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}