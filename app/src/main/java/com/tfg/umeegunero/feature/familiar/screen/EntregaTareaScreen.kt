package com.tfg.umeegunero.feature.familiar.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.PrioridadTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.feature.familiar.viewmodel.EntregaTareaViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

/**
 * Pantalla para la entrega de tareas por parte del familiar
 *
 * Permite al familiar subir la entrega de una tarea para su hijo.
 *
 * @param navController Controlador de navegación para volver atrás
 * @param tareaId ID de la tarea a entregar
 *
 * @author Equipo UmeEgunero
 * @version 4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregaTareaScreen(
    navController: NavController,
    tareaId: String,
    alumnoId: String,
    viewModel: EntregaTareaViewModel = hiltViewModel()
) {
    // Inicializar ViewModel
    LaunchedEffect(tareaId, alumnoId) {
        viewModel.inicializar(tareaId, alumnoId)
    }
    
    // Estado para la UI
    val uiState by viewModel.uiState.collectAsState()
    val tarea = uiState.tarea
    
    // Estado para el comentario
    var comentario by remember { mutableStateOf("") }
    
    // Estado para los archivos adjuntos
    val archivosSeleccionados = remember { mutableStateListOf<ArchivoAdjunto>() }
    
    // Snackbar para mensajes
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
                // Si se completó la entrega correctamente, volver a la pantalla anterior
                if (it.contains("exitosamente")) {
                    navController.popBackStack()
                }
            }
        }
    }
    
    // Para seleccionar archivos
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val nombre = uri.lastPathSegment ?: "archivo_${archivosSeleccionados.size + 1}"
            val nuevoArchivo = ArchivoAdjunto(uri = uri.toString(), nombre = nombre)
            archivosSeleccionados.add(nuevoArchivo)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entregar Tarea") },
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
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Mostrar indicador de carga si estamos cargando
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.archivosCargando) 
                            "Subiendo archivos (${(uiState.progresoCarga * 100).toInt()}%)" 
                        else 
                            "Enviando entrega...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
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
            // Contenido principal - Formulario de entrega
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Información básica de la tarea
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicador de prioridad
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
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = tarea.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = tarea.asignatura ?: "Sin asignatura",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fecha límite
                if (tarea.fechaEntrega != null) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val fechaEntrega = tarea.fechaEntrega.toDate()
                    val esRetrasada = fechaEntrega.before(Date())
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (esRetrasada) Color.Red else MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = "Fecha límite: ${dateFormat.format(fechaEntrega)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (esRetrasada) Color.Red else Color.Unspecified
                            )
                            
                            if (esRetrasada) {
                                Text(
                                    text = "¡Entrega retrasada!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Formulario de entrega
                Text(
                    text = "Datos de la Entrega",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Comentario
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario (opcional)") },
                    placeholder = { Text("Añade un comentario a la entrega") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sección de archivos adjuntos
                Text(
                    text = "Archivos Adjuntos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lista de archivos seleccionados
                if (archivosSeleccionados.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Archivos seleccionados (${archivosSeleccionados.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            archivosSeleccionados.forEachIndexed { index, archivo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = archivo.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    IconButton(
                                        onClick = { archivosSeleccionados.removeAt(index) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar archivo",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                
                                if (index < archivosSeleccionados.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón para agregar archivos
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar Archivo")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón para enviar la entrega
                Button(
                    onClick = {
                        val archivosUris = archivosSeleccionados.map { it.uri.toString() }
                        viewModel.enviarEntrega(comentario, archivosUris)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = true // En una implementación real, verificaríamos si hay archivos o comentario
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "ENVIAR TAREA", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Modelo para representar un archivo adjunto seleccionado
 */
data class ArchivoAdjunto(
    val uri: String,
    val nombre: String
)

/**
 * Vista previa de la pantalla de entrega de tarea para familiares
 */
@Preview(showBackground = true)
@Composable
fun EntregaTareaScreenPreview() {
    EntregaTareaScreen(
        navController = rememberNavController(),
        tareaId = "tarea123",
        alumnoId = "alumno123"
    )
} 