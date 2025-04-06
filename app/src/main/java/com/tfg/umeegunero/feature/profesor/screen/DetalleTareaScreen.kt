package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.feature.profesor.viewmodel.AlumnoEntrega
import com.tfg.umeegunero.feature.profesor.viewmodel.DetallesTareaViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.HorizontalDivider

/**
 * Pantalla de detalle de tarea para profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleTareaScreen(
    navController: NavController,
    tareaId: String,
    viewModel: DetallesTareaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Cargar datos cuando se inicia la pantalla
    LaunchedEffect(tareaId) {
        viewModel.cargarDetalleTarea(tareaId)
    }
    
    // Mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }
    
    // Mostrar mensajes de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Tarea") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Mostrar indicador de carga
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            } else if (uiState.tarea == null) {
                // Mostrar mensaje si no hay tarea
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se ha encontrado la tarea",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Mostrar contenido de la tarea
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Información de la tarea
                    item {
                        InformacionTareaCard(
                            titulo = uiState.tarea?.titulo ?: "",
                            descripcion = uiState.tarea?.descripcion ?: "",
                            fechaCreacion = uiState.tarea?.fechaCreacion?.toDate() ?: Date(),
                            fechaEntrega = uiState.tarea?.fechaEntrega?.toDate(),
                            estado = uiState.tarea?.estado ?: EstadoTarea.PENDIENTE,
                            clase = uiState.tarea?.nombreClase ?: "Sin clase asignada"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Progreso de las entregas
                    item {
                        ProgresoEntregasCard(
                            totalAlumnos = uiState.totalAlumnos,
                            alumnosEntregados = uiState.alumnosEntregados
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Título de la sección de alumnos
                    item {
                        Text(
                            text = "Entregas de alumnos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Lista de alumnos y sus entregas
                    items(uiState.alumnosEntregas) { alumnoEntrega ->
                        AlumnoEntregaItem(
                            alumnoEntrega = alumnoEntrega,
                            onClick = { viewModel.seleccionarEntrega(alumnoEntrega) }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para calificar entrega
    if (uiState.mostrarDialogoCalificacion) {
        DialogoCalificacion(
            alumnoEntrega = uiState.alumnoSeleccionado,
            onDismiss = { viewModel.ocultarDialogoCalificacion() },
            onCalificar = { entregaId, calificacion, feedback ->
                viewModel.calificarEntrega(entregaId, calificacion.toDouble(), feedback)
            }
        )
    }
}

@Composable
fun InformacionTareaCard(
    titulo: String,
    descripcion: String,
    fechaCreacion: Date,
    fechaEntrega: Date?,
    estado: EstadoTarea,
    clase: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Estado y título
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip de estado
                SuggestionChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = when (estado) {
                                EstadoTarea.PENDIENTE -> "Pendiente"
                                EstadoTarea.EN_PROGRESO -> "En progreso"
                                EstadoTarea.COMPLETADA -> "Completada"
                                EstadoTarea.CANCELADA -> "Cancelada"
                                EstadoTarea.VENCIDA -> "Vencida"
                            }
                        ) 
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = when (estado) {
                            EstadoTarea.PENDIENTE -> MaterialTheme.colorScheme.errorContainer
                            EstadoTarea.EN_PROGRESO -> MaterialTheme.colorScheme.secondaryContainer
                            EstadoTarea.COMPLETADA -> MaterialTheme.colorScheme.primaryContainer
                            EstadoTarea.CANCELADA -> MaterialTheme.colorScheme.surfaceVariant
                            EstadoTarea.VENCIDA -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Fechas y clase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Columna de fechas
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Creada: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaCreacion)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Entrega: ${fechaEntrega?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "No definida"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Clase
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clase: $clase",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgresoEntregasCard(
    totalAlumnos: Int,
    alumnosEntregados: Int
) {
    val porcentaje = if (totalAlumnos > 0) (alumnosEntregados.toFloat() / totalAlumnos) * 100 else 0f
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Progreso de entregas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progreso
            LinearProgressIndicator(
                progress = { porcentaje / 100 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$alumnosEntregados de $totalAlumnos alumnos",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "${porcentaje.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AlumnoEntregaItem(
    alumnoEntrega: AlumnoEntrega,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val tieneEntrega = alumnoEntrega.entrega != null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del alumno
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (alumnoEntrega.fotoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(alumnoEntrega.fotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de ${alumnoEntrega.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = alumnoEntrega.nombre.firstOrNull()?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del alumno
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alumnoEntrega.nombre} ${alumnoEntrega.apellidos}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (tieneEntrega) {
                        val fecha = alumnoEntrega.entrega?.fechaEntrega?.toDate()
                        "Entregado el ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha ?: Date())}"
                    } else {
                        "Pendiente de entrega"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tieneEntrega) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                
                if (tieneEntrega && alumnoEntrega.entrega?.calificacion != null) {
                    Text(
                        text = "Calificación: ${alumnoEntrega.entrega.calificacion}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Indicador de estado
            Icon(
                imageVector = if (tieneEntrega) {
                    if (alumnoEntrega.entrega?.calificacion != null) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Assignment
                    }
                } else {
                    Icons.Default.WarningAmber
                },
                contentDescription = null,
                tint = if (tieneEntrega) {
                    if (alumnoEntrega.entrega?.calificacion != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DialogoCalificacion(
    alumnoEntrega: AlumnoEntrega?,
    onDismiss: () -> Unit,
    onCalificar: (String, Double, String) -> Unit
) {
    if (alumnoEntrega == null) return
    
    val tieneEntrega = alumnoEntrega.entrega != null
    val calificacionInicial = alumnoEntrega.entrega?.calificacion ?: 0.0f
    var calificacion by remember { mutableStateOf(calificacionInicial) }
    var feedback by remember { mutableStateOf(alumnoEntrega.entrega?.comentarioProfesor ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Calificar entrega")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Alumno: ${alumnoEntrega.nombre} ${alumnoEntrega.apellidos}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!tieneEntrega) {
                    Text(
                        text = "El alumno aún no ha realizado la entrega",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Fecha de entrega
                    Text(
                        text = "Fecha de entrega: ${
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(alumnoEntrega.entrega?.fechaEntrega?.toDate() ?: Date())
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Comentario del alumno
                    if (alumnoEntrega.entrega?.comentario?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comentario del alumno:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = alumnoEntrega.entrega?.comentario ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Calificación
                    Text(
                        text = "Calificación (0-10):",
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    Slider(
                        value = calificacion.toFloat(),
                        onValueChange = { calificacion = it },
                        valueRange = 0f..10f,
                        steps = 20,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = String.format("%.1f", calificacion),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Feedback
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Comentarios") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tieneEntrega && alumnoEntrega.entrega?.id?.isNotEmpty() == true) {
                        onCalificar(alumnoEntrega.entrega.id, calificacion.toDouble(), feedback)
                    } else {
                        onDismiss()
                    }
                },
                enabled = tieneEntrega
            ) {
                Text("Calificar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 