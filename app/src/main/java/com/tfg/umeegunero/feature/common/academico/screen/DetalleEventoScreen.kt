package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.common.academico.viewmodel.DetalleEventoViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.feature.common.academico.viewmodel.DetalleEventoUiState
import com.tfg.umeegunero.util.toLocalDate
import com.tfg.umeegunero.util.toLocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Pantalla para visualizar y editar los detalles de un evento
 *
 * @param navController Controlador de navegación
 * @param eventoId ID del evento a visualizar/editar
 * @param onEventUpdated Callback ejecutado cuando el evento se actualiza
 * @param viewModel ViewModel para la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    navController: NavController,
    eventoId: String,
    onEventUpdated: () -> Unit = {},
    viewModel: DetalleEventoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Cargar evento cuando cambia el ID
    LaunchedEffect(eventoId) {
        viewModel.loadEvento(eventoId)
    }
    
    // Mostrar mensajes de error en Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearError()
        }
    }
    
    // Manejar navegación hacia atrás después de guardar
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onEventUpdated()
            navController.popBackStack()
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar evento" else "Detalle del evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (!uiState.isEditing) {
                        // Botón para editar
                        IconButton(onClick = { viewModel.startEditing() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar evento"
                            )
                        }
                        
                        // Botón para eliminar
                        IconButton(onClick = { viewModel.showDeleteConfirmation() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar evento",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // Botón para guardar
                        IconButton(onClick = { viewModel.saveEvento() }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar cambios"
                            )
                        }
                        
                        // Botón para cancelar
                        IconButton(onClick = { viewModel.cancelEditing() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(isLoading = true, message = "Cargando evento...")
            }
        } else if (uiState.evento != null) {
            val evento = checkNotNull(uiState.evento)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Contenido del evento
                if (uiState.isEditing) {
                    // Modo edición
                    EditEventContent(
                        uiState = uiState,
                        onTituloChange = viewModel::updateTitulo,
                        onDescripcionChange = viewModel::updateDescripcion,
                        onTipoChange = viewModel::updateTipo,
                        onFechaChange = viewModel::updateFecha,
                        onTimeChange = viewModel::updateHora,
                        onUbicacionChange = viewModel::updateUbicacion,
                        onRecordatorioChange = viewModel::updateRecordatorio,
                        onTiempoRecordatorioChange = viewModel::updateTiempoRecordatorio,
                        onPublicoChange = viewModel::updatePublico
                    )
                } else {
                    // Modo visualización
                    ViewEventContent(evento = evento)
                }
            }
        } else {
            // No se encontró el evento
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "El evento no existe o ha sido eliminado",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    // Mostrar diálogo de confirmación para eliminar evento
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = { Text("Eliminar evento") },
            text = { Text("¿Estás seguro de que quieres eliminar este evento? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteEvento() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Componente para visualizar los detalles de un evento
 */
@Composable
private fun ViewEventContent(evento: Evento) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("es", "ES"))
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Tipo de evento (chip con color)
        EventTypeChip(tipo = evento.tipo)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Título
        Text(
            text = evento.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Fecha y hora
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Fecha",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = dateFormatter.format(evento.fecha.toLocalDate()),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = timeFormatter.format(evento.fecha.toLocalTime()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Ubicación (si existe)
        if (evento.ubicacion.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = evento.ubicacion,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Recordatorio (si está activado)
        if (evento.recordatorio) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Recordatorio",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Recordatorio ${evento.tiempoRecordatorioMinutos} minutos antes",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Visibilidad
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (evento.publico) Icons.Default.Public else Icons.Default.Lock,
                contentDescription = "Visibilidad",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = if (evento.publico) "Evento público" else "Evento privado",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Descripción
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Componente para editar los detalles de un evento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEventContent(
    uiState: DetalleEventoUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTipoChange: (TipoEvento) -> Unit,
    onFechaChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onRecordatorioChange: (Boolean) -> Unit,
    onTiempoRecordatorioChange: (Int) -> Unit,
    onPublicoChange: (Boolean) -> Unit
) {
    val evento = uiState.evento ?: return
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Tipo de evento
        Text(
            text = "Tipo de evento",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TipoEvento.values()) { tipo ->
                val isSelected = uiState.tipoEvento == tipo
                
                SuggestionChip(
                    onClick = { onTipoChange(tipo) },
                    label = { Text(tipo.nombre) },
                    modifier = Modifier.padding(end = 4.dp),
                    icon = { 
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(tipo.color, RoundedCornerShape(4.dp))
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
        
        // Título
        OutlinedTextField(
            value = uiState.titulo,
            onValueChange = onTituloChange,
            label = { Text("Título") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Fecha
        OutlinedTextField(
            value = uiState.fechaTexto,
            onValueChange = onFechaChange,
            label = { Text("Fecha (dd/mm/yyyy)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Fecha")
            }
        )
        
        // Hora
        OutlinedTextField(
            value = uiState.horaTexto,
            onValueChange = onTimeChange,
            label = { Text("Hora (hh:mm)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.AccessTime, contentDescription = "Hora")
            }
        )
        
        // Ubicación
        OutlinedTextField(
            value = uiState.ubicacion,
            onValueChange = onUbicacionChange,
            label = { Text("Ubicación (opcional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = "Ubicación")
            }
        )
        
        // Recordatorio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.recordatorio,
                onCheckedChange = onRecordatorioChange
            )
            
            Text(
                text = "Activar recordatorio",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Tiempo de recordatorio (si está activado)
        if (uiState.recordatorio) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recordatorio",
                    modifier = Modifier.width(120.dp)
                )
                
                val opciones = listOf(5, 10, 15, 30, 60, 120, 1440) // 1440 = 1 día
                val opcionesTexto = listOf("5 min", "10 min", "15 min", "30 min", "1 hora", "2 horas", "1 día")
                
                var expanded by remember { mutableStateOf(false) }
                
                val selectedIndex = opciones.indexOf(uiState.tiempoRecordatorioMinutos).let { 
                    if (it >= 0) it else 2 // Valor por defecto: 15 min
                }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = opcionesTexto[selectedIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(150.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        opcionesTexto.forEachIndexed { index, text ->
                            DropdownMenuItem(
                                text = { Text(text) },
                                onClick = {
                                    onTiempoRecordatorioChange(opciones[index])
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Visibilidad
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.publico,
                onCheckedChange = onPublicoChange
            )
            
            Text(
                text = "Evento público",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Descripción
        OutlinedTextField(
            value = uiState.descripcion,
            onValueChange = onDescripcionChange,
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp),
            maxLines = 10
        )
    }
}

/**
 * Componente para mostrar el tipo de evento como un chip
 */
@Composable
fun EventTypeChip(tipo: TipoEvento) {
    Surface(
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = tipo.color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(tipo.color)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = tipo.nombre,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 