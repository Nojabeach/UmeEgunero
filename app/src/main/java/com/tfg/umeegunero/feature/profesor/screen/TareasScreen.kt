package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.feature.profesor.viewmodel.TareasProfesorViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import kotlinx.coroutines.launch

/**
 * Pantalla para la gestión de tareas por parte del profesor.
 *
 * Permite al profesor ver las tareas asignadas a su clase,
 * crear nuevas tareas y ver el estado de las entregas.
 *
 * @param navController Controlador de navegación.
 * @param viewModel ViewModel que gestiona la lógica y el estado de las tareas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasProfesorScreen(
    navController: NavController,
    viewModel: TareasProfesorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Gestión de diálogos
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTarea by remember { mutableStateOf<Tarea?>(null) }

    // Mostrar snackbar con mensajes de error o éxito
    LaunchedEffect(uiState.error, uiState.mensaje) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMensaje()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DefaultTopAppBar(
                title = "Gestión de Tareas",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                containerColor = ProfesorColor,
                contentColor = Color.White
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }, // Abrir diálogo de añadir
                containerColor = ProfesorColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear Tarea"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (uiState.isLoading && uiState.tareas.isEmpty()) { // Mostrar solo si la lista está vacía inicialmente
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Mensaje si no hay tareas
                AnimatedVisibility(
                    visible = !uiState.isLoading && uiState.tareas.isEmpty(),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(
                                Icons.Filled.Assignment, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                             )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay tareas asignadas",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Lista de tareas
                AnimatedVisibility(
                    visible = !uiState.isLoading || uiState.tareas.isNotEmpty(), // Mostrar si no está cargando o si ya hay tareas
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.tareas, key = { it.id }) { tarea ->
                            TareaItem(
                                tarea = tarea,
                                onEditClick = {
                                    selectedTarea = tarea
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    selectedTarea = tarea
                                    showDeleteDialog = true
                                },
                                onVerEntregasClick = {
                                     // Navegar a la pantalla de detalle de tarea (profesor)
                                     navController.navigate(
                                         AppScreens.DetalleTareaProfesor.createRoute(tarea.id)
                                     )
                                 }
                            )
                        }
                         item { Spacer(modifier = Modifier.height(72.dp)) } // Espacio para FAB
                    }
                }
            }
        }

        // Diálogo para añadir tarea
        if (showAddDialog) {
            TareaDialog(
                titulo = "Nueva Tarea",
                onDismiss = { showAddDialog = false },
                onConfirm = { nuevaTarea ->
                    viewModel.crearTarea(nuevaTarea)
                    showAddDialog = false
                },
                clases = uiState.clases // Pasar la lista de clases
            )
        }

        // Diálogo para editar tarea
        if (showEditDialog && selectedTarea != null) {
            TareaDialog(
                titulo = "Editar Tarea",
                onDismiss = { 
                    showEditDialog = false 
                    selectedTarea = null // Limpiar selección
                },
                onConfirm = { tareaEditada ->
                    viewModel.actualizarTarea(tareaEditada)
                    showEditDialog = false
                    selectedTarea = null // Limpiar selección
                },
                clases = uiState.clases, // Pasar la lista de clases
                tareaInicial = selectedTarea
            )
        }

        // Diálogo para confirmar eliminación
        if (showDeleteDialog && selectedTarea != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    selectedTarea = null // Limpiar selección
                },
                title = { Text("Eliminar tarea") },
                text = { 
                    Text(
                        "¿Estás seguro de que deseas eliminar la tarea '${selectedTarea?.titulo}'? Esta acción no se puede deshacer."
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedTarea?.let { viewModel.eliminarTarea(it.id) }
                            showDeleteDialog = false
                            selectedTarea = null // Limpiar selección
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { 
                        showDeleteDialog = false
                        selectedTarea = null // Limpiar selección
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Item que representa una tarea en la lista
 */
@Composable
fun TareaItem(
    tarea: Tarea,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVerEntregasClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera con título y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tarea.titulo ?: "Sin título",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Botones de acciones
                Row {
                    IconButton(
                        onClick = onVerEntregasClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Ver entregas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar tarea",
                            tint = ProfesorColor
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar tarea",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Detalles de la tarea
            Text(
                text = tarea.descripcion ?: "Sin descripción",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha de entrega
                Column {
                    Text(
                        text = "Fecha de entrega",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tarea.fechaEntrega?.toDate() ?: Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Clase asignada
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Clase",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tarea.nombreClase ?: "No asignada",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de estado
            // EstadoTareaChip(estado = tarea.estado) // Comentado si EstadoTareaChip no está definido
        }
    }
}

/* Comentado si EstadoTareaChip no está definido o causa problemas
@Composable
fun EstadoTareaChip(estado: EstadoTarea) {
    val (color, texto) = when (estado) {
        EstadoTarea.PENDIENTE -> Pair(MaterialTheme.colorScheme.errorContainer, "Pendiente")
        EstadoTarea.EN_PROGRESO -> Pair(MaterialTheme.colorScheme.secondaryContainer, "En progreso")
        EstadoTarea.COMPLETADA -> Pair(MaterialTheme.colorScheme.primaryContainer, "Completada")
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, estado.name)
    }
    
    SuggestionChip(
        onClick = { },
        label = { Text(texto) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color
        )
    )
}
*/

/* Comentado si FiltroChip no se usa o causa problemas
@Composable
fun FiltroChip(
    texto: String,
    seleccionado: Boolean,
    onSeleccionado: () -> Unit
) {
    ElevatedFilterChip(
        selected = seleccionado,
        onClick = onSeleccionado,
        label = { Text(texto) },
        leadingIcon = {
            if (seleccionado) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        },
        colors = FilterChipDefaults.elevatedFilterChipColors(
            selectedContainerColor = ProfesorColor,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaDialog(
    titulo: String,
    onDismiss: () -> Unit,
    onConfirm: (Tarea) -> Unit,
    clases: List<Clase>,
    tareaInicial: Tarea? = null
) {
    var tituloTarea by remember { mutableStateOf(tareaInicial?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tareaInicial?.descripcion ?: "") }
    var fechaEntrega by remember { 
        mutableStateOf(
            tareaInicial?.fechaEntrega?.toDate() ?: Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time // Default a 1 semana
        ) 
    }
    var claseSeleccionadaId by remember { mutableStateOf(tareaInicial?.claseId ?: "") }
    
    // Validación
    val isValid = tituloTarea.isNotBlank() && descripcion.isNotBlank() && claseSeleccionadaId.isNotBlank()
    
    // Para DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaEntrega.time,
        yearRange = IntRange(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.YEAR) + 5)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Título del diálogo
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Título de la tarea
                OutlinedTextField(
                    value = tituloTarea,
                    onValueChange = { tituloTarea = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = tituloTarea.isBlank()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                     isError = descripcion.isBlank()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Selector de clase
                val claseSeleccionada = clases.find { it.id == claseSeleccionadaId }
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded && clases.isNotEmpty() } // Solo expandir si hay clases
                ) {
                    OutlinedTextField(
                        value = claseSeleccionada?.nombre ?: "Selecciona una clase",
                        onValueChange = {}, // No editable directamente
                        readOnly = true,
                        label = { Text("Clase") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = claseSeleccionadaId.isBlank()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (clases.isEmpty()) {
                             DropdownMenuItem(
                                text = { Text("No hay clases disponibles") },
                                onClick = { expanded = false },
                                enabled = false
                            )
                        } else {
                            clases.forEach { clase ->
                                DropdownMenuItem(
                                    text = { Text(clase.nombre) },
                                    onClick = {
                                        claseSeleccionadaId = clase.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Selector de fecha
                 OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaEntrega),
                    onValueChange = { },
                    label = { Text("Fecha de entrega") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                )

                if (showDatePicker) {
                     DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { 
                                datePickerState.selectedDateMillis?.let {
                                    fechaEntrega = Date(it)
                                }
                                showDatePicker = false 
                            }) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) { 
                         DatePicker(state = datePickerState) 
                     }
                 }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            // Crear o actualizar tarea
                            val nuevaTarea = tareaInicial?.copy(
                                titulo = tituloTarea,
                                descripcion = descripcion,
                                fechaEntrega = com.google.firebase.Timestamp(fechaEntrega),
                                claseId = claseSeleccionadaId,
                                nombreClase = clases.find { it.id == claseSeleccionadaId }?.nombre ?: ""
                            ) ?: Tarea(
                                id = "", // Firestore generará el ID si está vacío
                                titulo = tituloTarea,
                                descripcion = descripcion,
                                fechaEntrega = com.google.firebase.Timestamp(fechaEntrega),
                                claseId = claseSeleccionadaId,
                                nombreClase = clases.find { it.id == claseSeleccionadaId }?.nombre ?: "",
                                // profesorId se asignará en el ViewModel
                                estado = EstadoTarea.PENDIENTE,
                                fechaCreacion = com.google.firebase.Timestamp.now()
                                // Añadir prioridad si es necesario
                            )
                            
                            onConfirm(nuevaTarea)
                        },
                        enabled = isValid
                    ) {
                        Text(if (tareaInicial == null) "Crear" else "Guardar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TareasProfesorScreenPreview() {
    UmeEguneroTheme {
        TareasProfesorScreen(navController = rememberNavController())
    }
}

// Eliminar Preview oscura si TareasScreenDarkPreview fue renombrada o eliminada
// @Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
// @Composable
// private fun TareasProfesorScreenDarkPreview() {
//     UmeEguneroTheme {
//         TareasProfesorScreen(navController = rememberNavController())
//     }
// } 