package com.tfg.umeegunero.feature.common.mensajeria

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.ProfesorColor
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla que muestra la lista de conversaciones disponibles para el usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversacionesScreen(
    esFamiliar: Boolean = false,
    viewModel: ConversacionesViewModel = hiltViewModel(),
    onNavigateToChat: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var textoBusqueda by remember { mutableStateOf("") }
    
    // Establecer el color del tema según el tipo de usuario
    val temaColor = if (esFamiliar) FamiliarColor else ProfesorColor
    
    // Manejar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes", color = Color.White) },
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
                    containerColor = temaColor,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                    viewModel.filtrarConversaciones(it)
                },
                placeholder = { Text("Buscar conversaciones...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            
            // Contenido principal
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    LoadingIndicator(fullScreen = true)
                } else if (uiState.conversaciones.isEmpty()) {
                    // Mensaje cuando no hay conversaciones
                    Text(
                        text = "No tienes conversaciones activas.\nInicia una conversación con un profesor o familiar.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    // Lista de conversaciones
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.conversaciones) { conversacion ->
                            ConversacionItem(
                                conversacion = conversacion,
                                onClick = {
                                    onNavigateToChat(conversacion.conversacionId, conversacion.participanteId)
                                },
                                esFamiliar = esFamiliar
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversacionItem(
    conversacion: ConversacionResumen,
    onClick: () -> Unit,
    esFamiliar: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (esFamiliar) ProfesorColor else FamiliarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información de la conversación
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversacion.participanteNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Fecha del último mensaje
                    Text(
                        text = formatTimestamp(conversacion.fechaUltimoMensaje),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Alumno relacionado, si existe
                if (conversacion.alumnoNombre != null) {
                    Text(
                        text = "Alumno: ${conversacion.alumnoNombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Último mensaje
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversacion.ultimoMensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (conversacion.mensajesNoLeidos > 0) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversacion.mensajesNoLeidos > 0) 
                            FontWeight.Medium 
                        else 
                            FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Contador de mensajes no leídos
                    if (conversacion.mensajesNoLeidos > 0) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversacion.mensajesNoLeidos.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modelo para mostrar información resumida de conversaciones en la UI
 */
data class ConversacionResumen(
    val id: String,
    val conversacionId: String,
    val participanteId: String,
    val participanteNombre: String,
    val participanteTipo: TipoUsuario,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: Timestamp,
    val mensajesNoLeidos: Int = 0,
    val alumnoId: String? = null,
    val alumnoNombre: String? = null
)

// Función para formatear la fecha del último mensaje
private fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = date }
    
    return when {
        isSameDay(now, messageTime) -> {
            // Si es hoy, mostrar la hora
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        isYesterday(now, messageTime) -> {
            // Si fue ayer, mostrar "Ayer"
            "Ayer"
        }
        isWithinOneWeek(now, messageTime) -> {
            // Si es dentro de la última semana, mostrar el día de la semana
            SimpleDateFormat("EEEE", Locale("es")).format(date)
        }
        else -> {
            // Si es anterior, mostrar fecha en formato corto
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(today: Calendar, other: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, other)
}

private fun isWithinOneWeek(today: Calendar, other: Calendar): Boolean {
    val oneWeekAgo = Calendar.getInstance().apply {
        timeInMillis = today.timeInMillis
        add(Calendar.DAY_OF_YEAR, -7)
    }
    return other.after(oneWeekAgo)
} 