package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.feature.centro.viewmodel.ListaCursosViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Pantalla para gestionar los cursos del centro educativo.
 * 
 * Esta pantalla proporciona una interfaz completa para la gestión de cursos,
 * permitiendo visualizar, crear, editar y eliminar cursos del centro educativo
 * seleccionado.
 * 
 * ## Características
 * - Lista de cursos del centro seleccionado
 * - Selector de centro educativo
 * - Creación de nuevos cursos
 * - Edición de cursos existentes
 * - Eliminación de cursos
 * - Gestión de estados de carga y errores
 * 
 * ## Estados
 * - Carga de datos
 * - Visualización de lista
 * - Diálogo de creación/edición
 * - Mensajes de error y éxito
 * 
 * ## Componentes principales
 * - TopAppBar con navegación
 * - Selector de centro
 * - Lista de cursos
 * - FloatingActionButton para añadir cursos
 * - Diálogos de creación/edición
 * - Snackbar para mensajes
 * 
 * @param navController Controlador de navegación para la gestión de rutas
 * @param viewModel ViewModel que gestiona la lógica de cursos
 * 
 * @see ListaCursosViewModel
 * @see ListaCursosUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCursosScreen(
    navController: NavController,
    viewModel: ListaCursosViewModel = hiltViewModel()
) {
    // Log distintivo para depuración
    Timber.e("🔵 ListaCursosScreen LANZADA")
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddCursoDialog by remember { mutableStateOf(false) }
    var cursoToEdit by remember { mutableStateOf<Curso?>(null) }
    val scope = rememberCoroutineScope()
    
    // Cargar centros y cursos al iniciar
    LaunchedEffect(Unit) {
        try {
        viewModel.cargarCentrosYSeleccionar()
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error al cargar los datos: ${e.message}")
        }
    }
    
    // Mostrar errores en snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cursos") },
                navigationIcon = {
                    IconButton(onClick = { 
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            // Si falla el popBackStack, intentamos navegar al dashboard
                            navController.navigate(AppScreens.CentroDashboard.route) {
                                popUpTo(AppScreens.CentroDashboard.route) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    cursoToEdit = null
                    showAddCursoDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Curso"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Selector de centro
            if (uiState.centros.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(uiState.centroSeleccionado?.nombre ?: "Selecciona un centro")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        uiState.centros.forEach { centro ->
                            DropdownMenuItem(
                                text = { Text(centro.nombre) },
                                onClick = {
                                    try {
                                    viewModel.seleccionarCentro(centro)
                                    expanded = false
                                    } catch (e: Exception) {
                                        expanded = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Error al seleccionar el centro: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.cursos.isEmpty()) {
                // Mostrar mensaje si no hay cursos
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay cursos disponibles",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Pulse el botón + para añadir un nuevo curso",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Encabezado con información
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Cursos Académicos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Desde aquí puede gestionar los cursos académicos del centro. Para cada curso podrá administrar sus clases.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    // Lista de cursos
                    items(uiState.cursos) { curso ->
                        CursoCard(
                            curso = curso,
                            onEditClick = {
                                cursoToEdit = curso
                                showAddCursoDialog = true
                            },
                            onDeleteClick = {
                                viewModel.eliminarCurso(curso.id)
                            },
                            onVerClasesClick = {
                                try {
                                // Garantizar que usamos el ID del centro correcto al navegar
                                val centroId = curso.centroId
                                navController.navigate("gestor_academico/CLASES?centroId=${centroId}&cursoId=${curso.id}&selectorCentroBloqueado=true&selectorCursoBloqueado=true&perfilUsuario=ADMIN_CENTRO")
                                } catch (e: Exception) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error al navegar a las clases: ${e.message}")
                                    }
                                }
                            }
                        )
                    }
                    
                    // Espacio adicional al final
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
    
    // Diálogo para añadir/editar curso
    if (showAddCursoDialog) {
        AddEditCursoDialog(
            curso = cursoToEdit,
            onDismiss = { showAddCursoDialog = false },
            onSave = { nombre, anioAcademico, descripcion, edadMinima, edadMaxima ->
                try {
                if (cursoToEdit == null) {
                    viewModel.crearCurso(nombre, anioAcademico, descripcion, edadMinima, edadMaxima)
                } else {
                    viewModel.actualizarCurso(
                        cursoToEdit!!.id,
                        nombre,
                        anioAcademico,
                        descripcion,
                        edadMinima,
                        edadMaxima
                    )
                }
                showAddCursoDialog = false
                } catch (e: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Error al guardar el curso: ${e.message}")
                    }
                }
            }
        )
    }
}

/**
 * Tarjeta para mostrar información de un curso
 */
@Composable
fun CursoCard(
    curso: Curso,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVerClasesClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono del curso
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Información del curso
                    Column {
                        Text(
                            text = curso.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Curso ${curso.anioAcademico}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Menú de opciones
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                expanded = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                expanded = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
            
            // Descripción del curso
            if (curso.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = curso.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Etiquetas informativas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rango de edad
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Edad: ${curso.edadMinima}-${curso.edadMaxima} años",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botón para ver clases
            Button(
                onClick = onVerClasesClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Class,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text("Ver Clases")
            }
        }
    }
}

/**
 * Diálogo para añadir o editar un curso
 */
@Composable
fun AddEditCursoDialog(
    curso: Curso?,
    onDismiss: () -> Unit,
    onSave: (nombre: String, anioAcademico: String, descripcion: String, edadMinima: Int, edadMaxima: Int) -> Unit
) {
    val isEditing = curso != null
    var nombre by remember { mutableStateOf(curso?.nombre ?: "") }
    var anioAcademico by remember { mutableStateOf(curso?.anioAcademico ?: "2023-2024") }
    var descripcion by remember { mutableStateOf(curso?.descripcion ?: "") }
    var edadMinima by remember { mutableStateOf(curso?.edadMinima?.toString() ?: "3") }
    var edadMaxima by remember { mutableStateOf(curso?.edadMaxima?.toString() ?: "5") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar Curso" else "Nuevo Curso") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del curso") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = anioAcademico,
                    onValueChange = { anioAcademico = it },
                    label = { Text("Año académico") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("p.ej. 2023-2024") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = edadMinima,
                        onValueChange = { edadMinima = it },
                        label = { Text("Edad mínima") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = edadMaxima,
                        onValueChange = { edadMaxima = it },
                        label = { Text("Edad máxima") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validaciones básicas
                    if (nombre.isBlank()) return@Button
                    if (anioAcademico.isBlank()) return@Button
                    
                    val minEdad = edadMinima.toIntOrNull() ?: 0
                    val maxEdad = edadMaxima.toIntOrNull() ?: 0
                    
                    onSave(nombre, anioAcademico, descripcion, minEdad, maxEdad)
                }
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