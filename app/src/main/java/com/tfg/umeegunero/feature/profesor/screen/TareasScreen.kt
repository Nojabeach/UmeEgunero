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
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.feature.profesor.viewmodel.TareasViewModel
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
                tarea = null,
                clases = uiState.clases,
                onDismiss = { showAddDialog = false },
                onConfirm = { tarea ->
                    viewModel.crearTarea(tarea)
                    showAddDialog = false
                }
            )
        }
        
        // Diálogo para editar tarea
        if (showEditDialog && selectedTarea != null) {
            TareaDialog(
                tarea = selectedTarea,
                clases = uiState.clases,
                onDismiss = { showEditDialog = false },
                onConfirm = { tarea ->
                    viewModel.actualizarTarea(tarea)
                    showEditDialog = false
                }
            )
        }
        
        // Diálogo para confirmar eliminación
        if (showDeleteDialog && selectedTarea != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar tarea") },
                text = { Text("¿Está seguro de eliminar la tarea '${selectedTarea?.titulo}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedTarea?.let { viewModel.eliminarTarea(it.id) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun TareaItem(
    tarea: Tarea,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera con clase y fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clase
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Class,
                        contentDescription = null,
                        tint = ProfesorColor,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = tarea.nombreClase,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Fecha entrega
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = Color(0xFFF57C00), // Naranja
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = tarea.fechaEntrega?.let { dateFormat.format(it.toDate()) } ?: "Sin fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Título
            Text(
                text = tarea.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Descripción
            Text(
                text = tarea.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Editar
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar tarea"
                    )
                }
                
                // Eliminar
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar tarea",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaDialog(
    tarea: Tarea?,
    clases: List<Clase>,
    onDismiss: () -> Unit,
    onConfirm: (Tarea) -> Unit
) {
    val context = LocalContext.current
    
    // Estado para los campos
    var titulo by remember { mutableStateOf(tarea?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(tarea?.descripcion ?: "") }
    var claseSeleccionada by remember { mutableStateOf(
        clases.find { it.id == tarea?.claseId } ?: clases.firstOrNull()
    )}
    
    // Fecha
    val calendar = remember { Calendar.getInstance() }
    tarea?.fechaEntrega?.toDate()?.let {
        calendar.time = it
    }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    
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
                Text(
                    text = if (tarea == null) "Nueva Tarea" else "Editar Tarea",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Selector de clase
                if (clases.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = claseSeleccionada?.nombre ?: "Seleccione una clase",
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
                                        claseSeleccionada = clase
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
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
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Fecha de entrega
                OutlinedTextField(
                    value = dateFormatter.format(calendar.time),
                    onValueChange = {},
                    label = { Text("Fecha de entrega") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            // Validar campos
                            if (titulo.isBlank()) {
                                return@Button
                            }
                            
                            // Crear o actualizar tarea
                            val nuevaTarea = if (tarea == null) {
                                Tarea(
                                    id = "",
                                    profesorId = "", // Se asignará en el ViewModel
                                    claseId = claseSeleccionada?.id ?: "",
                                    nombreClase = claseSeleccionada?.nombre ?: "",
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    fechaCreacion = com.google.firebase.Timestamp.now(),
                                    fechaEntrega = com.google.firebase.Timestamp(calendar.time)
                                )
                            } else {
                                tarea.copy(
                                    claseId = claseSeleccionada?.id ?: tarea.claseId,
                                    nombreClase = claseSeleccionada?.nombre ?: tarea.nombreClase,
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    fechaEntrega = com.google.firebase.Timestamp(calendar.time)
                                )
                            }
                            
                            onConfirm(nuevaTarea)
                        },
                        enabled = titulo.isNotBlank() && claseSeleccionada != null
                    ) {
                        Text(if (tarea == null) "Crear" else "Actualizar")
                    }
                }
            }
        }
    }
    
    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            calendar.timeInMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
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

@Composable
private fun FiltroChip(
    texto: String,
    seleccionado: Boolean,
    onSeleccionado: (Boolean) -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = { onSeleccionado(!seleccionado) },
        label = { Text(texto) }
    )
} 