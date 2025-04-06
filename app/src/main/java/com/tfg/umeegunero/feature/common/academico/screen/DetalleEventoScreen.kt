package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.common.academico.viewmodel.DetalleEventoViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para ver los detalles de un evento
 * 
 * @param navController Controlador de navegación
 * @param eventoId ID del evento a mostrar
 * @param viewModel ViewModel para gestionar los datos del evento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    navController: NavController,
    eventoId: String,
    viewModel: DetalleEventoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dialogoConfirmacionVisible = remember { mutableStateOf(false) }
    
    // Cargar el evento al inicio
    LaunchedEffect(eventoId) {
        viewModel.cargarEvento(eventoId)
    }
    
    // Mostrar errores en el snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.limpiarError()
        }
    }
    
    // Observar cambios en el estado de navegación
    LaunchedEffect(uiState.navegarAtras) {
        if (uiState.navegarAtras) {
            navController.popBackStack()
            viewModel.resetearNavegacion()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalles del evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón para editar
                    IconButton(onClick = { viewModel.mostrarDialogoEdicion() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar evento"
                        )
                    }
                    
                    // Botón para eliminar
                    IconButton(onClick = { dialogoConfirmacionVisible.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar evento"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Cargando evento..."
                )
            }
        } else if (uiState.evento != null) {
            val evento = uiState.evento!!
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera con tipo y título
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = evento.tipo.color.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Indicador de tipo
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(evento.tipo.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(evento.tipo.color)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Título del evento
                        Text(
                            text = evento.titulo,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        // Tipo de evento
                        Text(
                            text = evento.tipo.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Sección de fecha y hora
                ItemInfoEvento(
                    icono = Icons.Default.CalendarMonth,
                    titulo = "Fecha y hora",
                    contenido = formatearFecha(evento.fecha)
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Sección de descripción
                ItemInfoEvento(
                    icono = Icons.Default.Description,
                    titulo = "Descripción",
                    contenido = evento.descripcion.ifEmpty { "Sin descripción" }
                )
                
                if (evento.ubicacion.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Sección de ubicación
                    ItemInfoEvento(
                        icono = Icons.Default.LocationOn,
                        titulo = "Ubicación",
                        contenido = evento.ubicacion
                    )
                }
                
                if (evento.recordatorio) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Sección de recordatorio
                    ItemInfoEvento(
                        icono = Icons.Default.Notifications,
                        titulo = "Recordatorio",
                        contenido = "${evento.tiempoRecordatorioMinutos} minutos antes"
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Información adicional
                Text(
                    text = "Información adicional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sección de creador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Creado por: Profesor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                if (!evento.publico) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Evento privado (${evento.destinatarios.size} destinatarios)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Diálogo de edición
            if (uiState.dialogoEdicionVisible) {
                DialogoEdicionEvento(
                    evento = evento,
                    onDismiss = { viewModel.ocultarDialogoEdicion() },
                    onGuardar = { eventoModificado -> 
                        viewModel.actualizarEvento(eventoModificado)
                    }
                )
            }
        } else {
            // Evento no encontrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Evento no encontrado",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Diálogo de confirmación para eliminar
        if (dialogoConfirmacionVisible.value) {
            AlertDialog(
                onDismissRequest = { dialogoConfirmacionVisible.value = false },
                title = { Text("Eliminar evento") },
                text = { Text("¿Estás seguro de que quieres eliminar este evento? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = { 
                            dialogoConfirmacionVisible.value = false
                            viewModel.eliminarEvento()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { dialogoConfirmacionVisible.value = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Componente que muestra un item de información del evento
 */
@Composable
private fun ItemInfoEvento(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    contenido: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = contenido,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Diálogo para editar un evento
 */
@Composable
private fun DialogoEdicionEvento(
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
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Editar evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo para el título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de tipo de evento
                Text(
                    text = "Tipo de evento",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoEvento.values().forEach { tipo ->
                        FilterChip(
                            selected = tipoEvento == tipo,
                            onClick = { tipoEvento = tipo },
                            label = { Text(tipo.nombre) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = tipo.color,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo para la descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo para la ubicación
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Opción de recordatorio
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
                
                if (recordatorio) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Campo para el tiempo de recordatorio
                    OutlinedTextField(
                        value = tiempoRecordatorio,
                        onValueChange = { tiempoRecordatorio = it },
                        label = { Text("Minutos antes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { 
                            val eventoActualizado = evento.copy(
                                titulo = titulo,
                                descripcion = descripcion,
                                tipo = tipoEvento,
                                ubicacion = ubicacion,
                                recordatorio = recordatorio,
                                tiempoRecordatorioMinutos = tiempoRecordatorio.toIntOrNull() ?: 30
                            )
                            onGuardar(eventoActualizado)
                        },
                        enabled = titulo.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

/**
 * Formatea una fecha de Timestamp a un formato legible
 */
private fun formatearFecha(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatoFecha = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale("es", "ES"))
    return formatoFecha.format(date).capitalize()
}

/**
 * Vista previa de la pantalla de detalle de evento
 */
@Preview
@Composable
fun DetalleEventoScreenPreview() {
    UmeEguneroTheme {
        val navController = rememberNavController()
        val evento = Evento(
            id = "1",
            titulo = "Reunión de padres",
            descripcion = "Reunión informativa sobre el próximo trimestre",
            tipo = TipoEvento.REUNION,
            ubicacion = "Sala de reuniones",
            recordatorio = true,
            tiempoRecordatorioMinutos = 30
        )
        
        DetalleEventoScreen(
            navController = navController,
            eventoId = "1"
        )
    }
} 