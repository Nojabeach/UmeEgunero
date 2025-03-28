package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.centro.viewmodel.GestionNotificacionesCentroViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para gestionar las notificaciones del centro
 * Permite enviar notificaciones a diferentes perfiles de usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionNotificacionesCentroScreen(
    navController: NavController,
    viewModel: GestionNotificacionesCentroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNewNotificationDialog by remember { mutableStateOf(false) }
    var showHistorialTab by remember { mutableStateOf(true) }
    
    // Mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Mostrar mensajes de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.clearMensaje()
        }
    }
    
    // Cargar notificaciones al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarNotificaciones()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones del Centro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistorialTab = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = if (showHistorialTab) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = { showHistorialTab = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Nueva Notificación",
                            tint = if (!showHistorialTab) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showHistorialTab) {
                FloatingActionButton(
                    onClick = { showNewNotificationDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva Notificación"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Contenido principal según la pestaña seleccionada
            if (showHistorialTab) {
                // Historial de notificaciones
                if (uiState.notificaciones.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No hay notificaciones",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Pulse el botón '+' para crear una nueva notificación",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.notificaciones) { notificacion ->
                            NotificacionItem(
                                notificacion = notificacion,
                                onItemClick = {
                                    // Mostrar detalles de la notificación
                                }
                            )
                        }
                        
                        // Espacio para el FAB
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            } else {
                // Pantalla para enviar nueva notificación
                NuevaNotificacionContent(
                    titulo = uiState.tituloNueva,
                    onTituloChange = { viewModel.updateTituloNueva(it) },
                    mensaje = uiState.mensajeNueva,
                    onMensajeChange = { viewModel.updateMensajeNueva(it) },
                    tiposPerfil = uiState.perfilesSeleccionados,
                    onTipoPerfilToggle = { tipo -> viewModel.togglePerfilSeleccionado(tipo) },
                    grupos = uiState.gruposSeleccionados,
                    onGrupoToggle = { grupo -> viewModel.toggleGrupoSeleccionado(grupo) },
                    onSendNotification = { viewModel.enviarNotificacion() },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
    
    // Diálogo para nueva notificación
    if (showNewNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNewNotificationDialog = false },
            title = { Text("Nueva Notificación") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("¿Quieres crear una nueva notificación o usar la pantalla de creación avanzada?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHistorialTab = false
                        showNewNotificationDialog = false
                    }
                ) {
                    Text("Avanzada")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.resetNuevaNotificacion()
                        
                        // Abrir un diálogo simple
                        viewModel.updateTituloNueva("")
                        viewModel.updateMensajeNueva("")
                        viewModel.togglePerfilSeleccionado(TipoUsuario.PROFESOR)
                        
                        showNewNotificationDialog = false
                    }
                ) {
                    Text("Simple")
                }
            }
        )
    }
}

@Composable
fun NotificacionItem(
    notificacion: com.tfg.umeegunero.data.model.Notificacion,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de notificación
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información de la notificación
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notificacion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Enviado el ${
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(notificacion.fecha.toDate())
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Destinatarios
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Destinatarios: ",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = notificacion.tipoDestinatarios.joinToString(", ") { 
                                when (it) {
                                    TipoUsuario.PROFESOR -> "Profesores"
                                    TipoUsuario.FAMILIAR -> "Familiares"
                                    TipoUsuario.ALUMNO -> "Alumnos"
                                    else -> it.toString()
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mensaje de la notificación
            Text(
                text = notificacion.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NuevaNotificacionContent(
    titulo: String,
    onTituloChange: (String) -> Unit,
    mensaje: String,
    onMensajeChange: (String) -> Unit,
    tiposPerfil: Set<TipoUsuario>,
    onTipoPerfilToggle: (TipoUsuario) -> Unit,
    grupos: Set<String>,
    onGrupoToggle: (String) -> Unit,
    onSendNotification: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nueva Notificación",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Título
        OutlinedTextField(
            value = titulo,
            onValueChange = onTituloChange,
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
        
        // Mensaje
        OutlinedTextField(
            value = mensaje,
            onValueChange = onMensajeChange,
            label = { Text("Mensaje") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            minLines = 5
        )
        
        // Selector de perfiles destinatarios
        Text(
            text = "Destinatarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = tiposPerfil.contains(TipoUsuario.PROFESOR),
                onClick = { onTipoPerfilToggle(TipoUsuario.PROFESOR) },
                label = { Text("Profesores") },
                leadingIcon = {
                    if (tiposPerfil.contains(TipoUsuario.PROFESOR)) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            
            FilterChip(
                selected = tiposPerfil.contains(TipoUsuario.FAMILIAR),
                onClick = { onTipoPerfilToggle(TipoUsuario.FAMILIAR) },
                label = { Text("Familiares") },
                leadingIcon = {
                    if (tiposPerfil.contains(TipoUsuario.FAMILIAR)) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
            
            FilterChip(
                selected = tiposPerfil.contains(TipoUsuario.ALUMNO),
                onClick = { onTipoPerfilToggle(TipoUsuario.ALUMNO) },
                label = { Text("Alumnos") },
                leadingIcon = {
                    if (tiposPerfil.contains(TipoUsuario.ALUMNO)) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                }
            )
        }
        
        // Selector de grupos (cursos, clases, etc.)
        if (tiposPerfil.isNotEmpty()) {
            Text(
                text = "Grupos específicos (opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Mostrar chips para seleccionar grupos específicos
            // En un caso real, obtendríamos esto de la base de datos
            val gruposEjemplo = listOf("1º Infantil", "2º Infantil", "3º Infantil", "1º Primaria", "2º Primaria")
            
            // Mostrar en filas de 3
            val filasGrupos = gruposEjemplo.chunked(3)
            
            filasGrupos.forEach { filaGrupos ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filaGrupos.forEach { grupo ->
                        FilterChip(
                            selected = grupos.contains(grupo),
                            onClick = { onGrupoToggle(grupo) },
                            label = { Text(grupo) },
                            leadingIcon = {
                                if (grupos.contains(grupo)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Rellenar con espacios vacíos si hay menos de 3 elementos
                    repeat(3 - filaGrupos.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Botón de enviar
        Button(
            onClick = onSendNotification,
            modifier = Modifier.fillMaxWidth(),
            enabled = titulo.isNotBlank() && mensaje.isNotBlank() && tiposPerfil.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Enviar Notificación")
                }
            }
        }
    }
} 