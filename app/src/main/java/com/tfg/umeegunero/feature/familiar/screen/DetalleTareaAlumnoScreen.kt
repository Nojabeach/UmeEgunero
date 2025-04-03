package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.PrioridadTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.feature.familiar.viewmodel.DetalleTareaViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTareaAlumnoScreen(
    navController: NavController,
    tareaId: String,
    viewModel: DetalleTareaViewModel = hiltViewModel()
) {
    // Estado para manejar el diálogo de revisión
    var mostrarDialogoRevision by remember { mutableStateOf(false) }
    var comentarioRevision by remember { mutableStateOf("") }
    
    // Inicializar ViewModel
    LaunchedEffect(tareaId) {
        viewModel.cargarTarea(tareaId)
    }
    
    // Observar estado de la UI
    val uiState by viewModel.uiState.collectAsState()
    val tarea = uiState.tarea
    
    // SnackBar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Mostrar mensajes de error o éxito
    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensajes()
            }
        }
        
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensajes()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Tarea") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón para revisar tarea (si no está revisada ya)
                    if (tarea != null && !tarea.revisadaPorFamiliar) {
                        IconButton(onClick = { mostrarDialogoRevision = true }) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Marcar como revisada",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Indicador de carga
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (tarea == null) {
            // Mensaje si no se encuentra la tarea
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró la tarea",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Contenido principal - Detalles de la tarea
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Encabezado con título y estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de prioridad y título
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when (tarea.prioridad) {
                                        PrioridadTarea.ALTA -> Color.Red
                                        PrioridadTarea.MEDIA -> Color(0xFFFFA500) // Orange
                                        PrioridadTarea.BAJA -> Color(0xFF4CAF50) // Green
                                        PrioridadTarea.URGENTE -> Color(0xFF5C0000) // Dark Red
                                        else -> Color.Gray
                                    }
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = tarea.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Etiqueta de estado
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            tarea.estado == EstadoTarea.COMPLETADA -> Color(0xFF4CAF50) // Green
                            tarea.estado == EstadoTarea.EN_PROGRESO -> Color(0xFF2196F3) // Blue
                            tarea.fechaEntrega?.toDate()?.before(Date()) == true -> Color.Red
                            else -> Color(0xFFFFA500) // Orange para PENDIENTE
                        }
                    ) {
                        Text(
                            text = when {
                                tarea.estado == EstadoTarea.COMPLETADA -> "Completada"
                                tarea.estado == EstadoTarea.EN_PROGRESO -> "En progreso"
                                tarea.fechaEntrega?.toDate()?.before(Date()) == true -> "Retrasada"
                                else -> "Pendiente"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sección: Descripción
                SectionTitle(title = "Descripción")
                Text(
                    text = tarea.descripcion.ifEmpty { "Sin descripción" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Información adicional
                SectionTitle(title = "Información General")
                
                // Asignatura
                InfoRow(
                    icon = Icons.Default.School,
                    label = "Asignatura:",
                    value = tarea.asignatura
                )
                
                // Profesor
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Profesor:",
                    value = tarea.profesorNombre
                )
                
                // Fechas
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                
                // Fecha de creación
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Asignada:",
                    value = dateFormat.format(tarea.fechaCreacion.toDate())
                )
                
                // Fecha de entrega
                if (tarea.fechaEntrega != null) {
                    val fechaEntrega = tarea.fechaEntrega.toDate()
                    val esRetrasada = fechaEntrega.before(Date()) && 
                                      tarea.estado != EstadoTarea.COMPLETADA
                    
                    InfoRow(
                        icon = Icons.Default.Event,
                        label = "Fecha de entrega:",
                        value = dateFormat.format(fechaEntrega),
                        valueColor = if (esRetrasada) Color.Red else Color.Unspecified
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sección: Estado de revisión
                SectionTitle(title = "Estado de Revisión")
                
                if (tarea.revisadaPorFamiliar) {
                    // Información de revisión
                    InfoRow(
                        icon = Icons.Default.Check,
                        label = "Revisada:",
                        value = "Sí"
                    )
                    
                    // Fecha de revisión
                    if (tarea.fechaRevision != null) {
                        InfoRow(
                            icon = Icons.Default.DateRange,
                            label = "Fecha de revisión:",
                            value = dateFormat.format(tarea.fechaRevision.toDate())
                        )
                    }
                    
                    // Comentarios del familiar
                    if (tarea.comentariosFamiliar.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comentarios:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tarea.comentariosFamiliar,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    // No revisada aún
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Esta tarea aún no ha sido revisada",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Button(
                        onClick = { mostrarDialogoRevision = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marcar como Revisada")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sección: Calificación (si existe)
                tarea.calificacion?.let { calificacion ->
                    SectionTitle(title = "Evaluación")
                    
                    InfoRow(
                        icon = Icons.Default.Star,
                        label = "Calificación:",
                        value = calificacion.toString()
                    )
                    
                    if (tarea.feedbackProfesor.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comentarios del profesor:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tarea.feedbackProfesor,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Sección: Archivos adjuntos
                if (tarea.adjuntos.isNotEmpty()) {
                    SectionTitle(title = "Archivos Adjuntos")
                    
                    tarea.adjuntos.forEachIndexed { index, url ->
                        val fileName = url.substringAfterLast("/").ifEmpty { "Archivo ${index + 1}" }
                        
                        OutlinedButton(
                            onClick = { /* Código para descargar o ver el archivo */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Espacio final
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón para entregar la tarea
                if (tarea.estado != EstadoTarea.COMPLETADA) {
                    val alumnoId = viewModel.getAlumnoIdParaTarea()
                    
                    Button(
                        onClick = { 
                            if (alumnoId.isNotEmpty()) {
                                navController.navigate(
                                    AppScreens.EntregaTarea.createRoute(tarea.id, alumnoId)
                                )
                            } else {
                                // Mostrar mensaje de error si no se puede determinar el alumnoId
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "No se pudo determinar el alumno para esta tarea"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entregar Tarea")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Diálogo para revisar tarea
    if (mostrarDialogoRevision) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRevision = false },
            title = { Text("Revisar tarea") },
            text = {
                Column {
                    Text("Marcar esta tarea como revisada por ti")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comentarioRevision,
                        onValueChange = { comentarioRevision = it },
                        label = { Text("Comentario (opcional)") },
                        placeholder = { Text("Añade tus observaciones o comentarios sobre la tarea") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.marcarTareaComoRevisada(tareaId, comentarioRevision)
                        mostrarDialogoRevision = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoRevision = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
} 