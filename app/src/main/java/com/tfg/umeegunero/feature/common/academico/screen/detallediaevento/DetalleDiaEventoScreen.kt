package com.tfg.umeegunero.feature.common.academico.screen.detallediaevento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.viewmodel.DetalleDiaEventoViewModel
import com.tfg.umeegunero.feature.common.academico.screen.detallediaevento.viewmodel.DetalleDiaEventoUiState
import com.tfg.umeegunero.ui.components.ErrorScreen
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Pantalla que muestra el detalle de los eventos de un día específico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDiaEventoScreen(
    navController: NavController,
    fecha: LocalDate,
    viewModel: DetalleDiaEventoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Cargar eventos para la fecha seleccionada
    LaunchedEffect(fecha) {
        viewModel.cargarEventosPorFecha(fecha)
    }
    
    // Mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.limpiarError()
        }
    }
    
    // Mostrar mensajes de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.limpiarMensaje()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = formatearFecha(fecha)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.mostrarDialogoCrearEvento() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear evento"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(
                        isLoading = true,
                        message = "Cargando eventos..."
                    )
                }
            }
            uiState.error != null && uiState.eventos.isEmpty() -> {
                ErrorScreen(
                    message = uiState.error ?: "Error desconocido",
                    onRetry = { viewModel.cargarEventosPorFecha(fecha) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.eventos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay eventos para este día",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.mostrarDialogoCrearEvento() }
                        ) {
                            Text("Crear evento")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Eventos del día",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    
                    items(uiState.eventos.sortedBy { it.fecha }) { evento ->
                        TarjetaEvento(
                            evento = evento,
                            onEditar = { viewModel.mostrarDialogoEditarEvento(evento) },
                            onEliminar = { viewModel.mostrarDialogoConfirmarEliminar(evento) }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Diálogo para crear evento
        if (uiState.mostrarDialogoCrearEvento) {
            DialogoCrearEvento(
                fecha = fecha,
                onDismiss = { viewModel.ocultarDialogoCrearEvento() },
                onGuardar = { titulo, descripcion, tipo, ubicacion, recordatorio, minutos ->
                    viewModel.crearEvento(
                        titulo = titulo,
                        descripcion = descripcion, 
                        tipo = tipo,
                        fecha = fecha,
                        ubicacion = ubicacion,
                        recordatorio = recordatorio,
                        tiempoRecordatorioMinutos = minutos
                    )
                }
            )
        }
        
        // Diálogo para editar evento
        if (uiState.eventoParaEditar != null) {
            DialogoEditarEvento(
                evento = uiState.eventoParaEditar!!,
                onDismiss = { viewModel.ocultarDialogoEditarEvento() },
                onGuardar = { eventoActualizado ->
                    viewModel.actualizarEvento(eventoActualizado)
                }
            )
        }
        
        // Diálogo para confirmar eliminación
        if (uiState.eventoParaEliminar != null) {
            AlertDialog(
                onDismissRequest = { viewModel.ocultarDialogoConfirmarEliminar() },
                title = { Text("Eliminar evento") },
                text = { Text("¿Estás seguro de que deseas eliminar este evento?") },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.eliminarEvento(uiState.eventoParaEliminar!!)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.ocultarDialogoConfirmarEliminar() }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Tarjeta que muestra la información de un evento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaEvento(
    evento: Evento,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val hora = evento.fecha.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()
    val horaFormateada = hora.format(DateTimeFormatter.ofPattern("HH:mm"))
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador del tipo de evento
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = evento.tipo.color,
                        shape = MaterialTheme.shapes.small
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del evento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = evento.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Chip con la hora
                    SuggestionChip(
                        onClick = { },
                        label = { Text(horaFormateada) }
                    )
                }
                
                if (evento.descripcion.isNotBlank()) {
                    Text(
                        text = evento.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (evento.ubicacion.isNotBlank()) {
                    Text(
                        text = "Ubicación: ${evento.ubicacion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Indicador de recordatorio
                if (evento.recordatorio) {
                    Text(
                        text = "Recordatorio: ${evento.tiempoRecordatorioMinutos} minutos antes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Botones de acción
            Row {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar evento"
                    )
                }
                
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar evento",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para crear un nuevo evento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearEvento(
    fecha: LocalDate,
    onDismiss: () -> Unit,
    onGuardar: (String, String, TipoEvento, String, Boolean, Int) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoEvento by remember { mutableStateOf(TipoEvento.CLASE) }
    var ubicacion by remember { mutableStateOf("") }
    var recordatorio by remember { mutableStateOf(false) }
    var tiempoRecordatorio by remember { mutableStateOf("30") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear nuevo evento") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Tipo de evento
                Text(
                    text = "Tipo de evento",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de tipo de evento
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(TipoEvento.values()) { tipo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { tipoEvento = tipo },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tipo == tipoEvento,
                                onClick = { tipoEvento = tipo }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = tipo.color,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = tipo.nombre,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ubicación
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recordatorio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordatorio,
                        onCheckedChange = { recordatorio = it }
                    )
                    
                    Text(
                        text = "Activar recordatorio",
                        modifier = Modifier.clickable { recordatorio = !recordatorio }
                    )
                }
                
                // Tiempo de recordatorio (solo visible si recordatorio está activado)
                AnimatedVisibility(visible = recordatorio) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tiempoRecordatorio,
                            onValueChange = { 
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    tiempoRecordatorio = it
                                }
                            },
                            label = { Text("Minutos") },
                            modifier = Modifier.width(100.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text("minutos antes")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        onGuardar(
                            titulo,
                            descripcion,
                            tipoEvento,
                            ubicacion,
                            recordatorio,
                            tiempoRecordatorio.toIntOrNull() ?: 30
                        )
                    }
                },
                enabled = titulo.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para editar un evento existente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEditarEvento(
    evento: Evento,
    onDismiss: () -> Unit,
    onGuardar: (Evento) -> Unit
) {
    var titulo by remember { mutableStateOf(evento.titulo) }
    var descripcion by remember { mutableStateOf(evento.descripcion) }
    var tipoEvento by remember { mutableStateOf(evento.tipo) }
    var ubicacion by remember { mutableStateOf(evento.ubicacion) }
    var recordatorio by remember { mutableStateOf(evento.recordatorio) }
    var tiempoRecordatorio by remember { mutableStateOf(evento.tiempoRecordatorioMinutos.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar evento") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Tipo de evento
                Text(
                    text = "Tipo de evento",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de tipo de evento
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(TipoEvento.values()) { tipo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { tipoEvento = tipo },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tipo == tipoEvento,
                                onClick = { tipoEvento = tipo }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = tipo.color,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = tipo.nombre,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ubicación
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recordatorio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordatorio,
                        onCheckedChange = { recordatorio = it }
                    )
                    
                    Text(
                        text = "Activar recordatorio",
                        modifier = Modifier.clickable { recordatorio = !recordatorio }
                    )
                }
                
                // Tiempo de recordatorio (solo visible si recordatorio está activado)
                AnimatedVisibility(visible = recordatorio) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tiempoRecordatorio,
                            onValueChange = { 
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    tiempoRecordatorio = it
                                }
                            },
                            label = { Text("Minutos") },
                            modifier = Modifier.width(100.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text("minutos antes")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        val eventoActualizado = evento.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            tipo = tipoEvento,
                            ubicacion = ubicacion,
                            recordatorio = recordatorio,
                            tiempoRecordatorioMinutos = tiempoRecordatorio.toIntOrNull() ?: 30
                        )
                        onGuardar(eventoActualizado)
                    }
                },
                enabled = titulo.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Formatea una fecha para mostrarla en la pantalla
 */
private fun formatearFecha(fecha: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return fecha.format(formatter).replaceFirstChar { it.uppercase() }
} 