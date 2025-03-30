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
import com.tfg.umeegunero.feature.profesor.viewmodel.TareasViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.FilterChip

/**
 * Pantalla de gestión de tareas para profesores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(
    navController: NavController,
    viewModel: TareasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Gestión de diálogos
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedTarea by remember { mutableStateOf<Tarea?>(null) }
    
    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Tareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Filtro por clase
                    IconButton(onClick = { viewModel.mostrarFiltroClases() }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar por clase"
                        )
                    }
                    
                    // Ordenar tareas
                    IconButton(onClick = { viewModel.cambiarOrden() }) {
                        Icon(
                            imageVector = if (uiState.ordenAscendente) 
                                Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Cambiar orden"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ProfesorColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir tarea"
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
            if (uiState.isLoading) {
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
                // Filtro seleccionado
                if (uiState.claseSeleccionada != null) {
                    FiltroChip(
                        texto = "Clase: ${uiState.claseSeleccionada?.nombre ?: ""}",
                        seleccionado = true,
                        onSeleccionado = { viewModel.clearFiltro() }
                    )
                }
                
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
                        Text(
                            text = if (uiState.claseSeleccionada != null)
                                "No hay tareas para esta clase"
                            else
                                "No hay tareas asignadas",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = !uiState.isLoading && uiState.tareas.isNotEmpty(),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    // Lista de tareas
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.tareas) { tarea ->
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
                                    navController.navigate(
                                        AppScreens.DetalleTareaProfesor.createRoute(tarea.id)
                                    )
                                }
                            )
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Diálogo para filtrar por clase
        if (uiState.mostrarFiltroClasesDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.mostrarFiltroClases() },
                title = { Text("Seleccionar clase") },
                text = {
                    LazyColumn {
                        items(uiState.clases) { clase ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.aplicarFiltroClase(clase)
                                        viewModel.mostrarFiltroClases()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.claseSeleccionada?.id == clase.id,
                                    onClick = {
                                        viewModel.aplicarFiltroClase(clase)
                                        viewModel.mostrarFiltroClases()
                                    }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = clase.nombre,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.mostrarFiltroClases() }) {
                        Text("Cerrar")
                    }
                }
            )
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
                clases = uiState.clases
            )
        }
        
        // Diálogo para editar tarea
        if (showEditDialog && selectedTarea != null) {
            TareaDialog(
                titulo = "Editar Tarea",
                onDismiss = { showEditDialog = false },
                onConfirm = { tareaEditada ->
                    viewModel.actualizarTarea(tareaEditada)
                    showEditDialog = false
                },
                clases = uiState.clases,
                tareaInicial = selectedTarea
            )
        }
        
        // Diálogo para confirmar eliminación
        if (showDeleteDialog && selectedTarea != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
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
                    text = tarea.titulo,
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
                text = tarea.descripcion,
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
            EstadoTareaChip(estado = tarea.estado)
        }
    }
}

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
            tareaInicial?.fechaEntrega?.toDate() ?: Date()
        ) 
    }
    var claseSeleccionadaId by remember { mutableStateOf(tareaInicial?.claseId ?: "") }
    
    // Validación
    val isValid = tituloTarea.isNotEmpty() && descripcion.isNotEmpty() && claseSeleccionadaId.isNotEmpty()
    
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
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Selector de clase
                val claseSeleccionada = clases.find { it.id == claseSeleccionadaId }
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = claseSeleccionada?.nombre ?: "Selecciona una clase",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Clase") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Selector de fecha (simplificado)
                Text("Fecha de entrega: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaEntrega)}")
                
                // Aquí podría implementarse un DatePicker, pero para simplicidad usamos un botón
                Button(
                    onClick = {
                        // Mostrar un DatePicker y actualizar fechaEntrega
                        // Por simplicidad, sumamos una semana a la fecha actual
                        val calendar = Calendar.getInstance()
                        calendar.time = fechaEntrega
                        calendar.add(Calendar.DAY_OF_MONTH, 7)
                        fechaEntrega = calendar.time
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cambiar fecha")
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
                                claseId = claseSeleccionadaId
                            ) ?: Tarea(
                                id = "",
                                titulo = tituloTarea,
                                descripcion = descripcion,
                                fechaEntrega = com.google.firebase.Timestamp(fechaEntrega),
                                claseId = claseSeleccionadaId,
                                profesorId = "",
                                estado = EstadoTarea.PENDIENTE,
                                fechaCreacion = com.google.firebase.Timestamp.now()
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
fun TareasScreenPreview() {
    UmeEguneroTheme {
        TareasScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TareasScreenDarkPreview() {
    UmeEguneroTheme {
        TareasScreen(navController = rememberNavController())
    }
} 