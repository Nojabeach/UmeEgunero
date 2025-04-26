package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.feature.centro.viewmodel.GestionCursosYClasesViewModel
import com.tfg.umeegunero.navigation.AppScreens
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType

/**
 * Pantalla para gestionar cursos y clases del centro
 * Permite al administrador visualizar, crear, editar y eliminar cursos y clases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCursosYClasesScreen(
    navController: NavController,
    viewModel: GestionCursosYClasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCursoDialog by remember { mutableStateOf(false) }
    var showDeleteCursoDialog by remember { mutableStateOf(false) }
    var cursoSeleccionado by remember { mutableStateOf<Curso?>(null) }
    
    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarCursos()
    }
    
    // Mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Mostrar mensajes de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.clearMensaje()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cursos y Clases") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCursoDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Curso"
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
            
            // Contenido principal: lista de cursos
            if (uiState.cursos.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay cursos disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Pulse el botón '+' para añadir un nuevo curso",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.cursos) { curso ->
                        CursoItem(
                            curso = curso,
                            onItemClick = { 
                                // Navegar a la pantalla de clases del curso
                                navController.navigate("gestor_academico/CLASES?centroId=${curso.centroId}&cursoId=${curso.id}&selectorCentroBloqueado=true&selectorCursoBloqueado=true&perfilUsuario=ADMIN_CENTRO")
                            },
                            onEditClick = {
                                cursoSeleccionado = curso
                                showCursoDialog = true
                            },
                            onDeleteClick = {
                                cursoSeleccionado = curso
                                showDeleteCursoDialog = true
                            }
                        )
                    }
                    
                    // Espacio para el FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    
    // Diálogo para añadir/editar curso
    if (showCursoDialog) {
        var nombre by remember { mutableStateOf(cursoSeleccionado?.nombre ?: "") }
        var anioAcademico by remember { mutableStateOf(cursoSeleccionado?.anioAcademico ?: "${getCurrentYear()}-${getCurrentYear() + 1}") }
        var edadMinima by remember { mutableStateOf(cursoSeleccionado?.edadMinima?.toString() ?: "3") }
        var edadMaxima by remember { mutableStateOf(cursoSeleccionado?.edadMaxima?.toString() ?: "5") }
        var descripcion by remember { mutableStateOf(cursoSeleccionado?.descripcion ?: "") }
        
        AlertDialog(
            onDismissRequest = { 
                showCursoDialog = false
                cursoSeleccionado = null
            },
            title = { Text(if (cursoSeleccionado == null) "Añadir Curso" else "Editar Curso") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del curso") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = anioAcademico,
                        onValueChange = { anioAcademico = it },
                        label = { Text("Año académico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = edadMinima,
                            onValueChange = { edadMinima = it },
                            label = { Text("Edad mínima") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        
                        OutlinedTextField(
                            value = edadMaxima,
                            onValueChange = { edadMaxima = it },
                            label = { Text("Edad máxima") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                    
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validar que no haya campos vacíos
                        if (nombre.isBlank() || anioAcademico.isBlank() || 
                            edadMinima.isBlank() || edadMaxima.isBlank()) {
                            return@Button
                        }
                        
                        val edadMin = edadMinima.toIntOrNull() ?: 0
                        val edadMax = edadMaxima.toIntOrNull() ?: 0
                        
                        if (edadMin > edadMax) {
                            return@Button
                        }
                        
                        // Guardar el curso (crearlo o actualizarlo)
                        if (cursoSeleccionado == null) {
                            viewModel.crearCurso(
                                nombre = nombre,
                                anioAcademico = anioAcademico,
                                edadMinima = edadMin,
                                edadMaxima = edadMax,
                                descripcion = descripcion
                            )
                        } else {
                            viewModel.actualizarCurso(
                                cursoId = cursoSeleccionado!!.id,
                                nombre = nombre,
                                anioAcademico = anioAcademico,
                                edadMinima = edadMin,
                                edadMaxima = edadMax,
                                descripcion = descripcion
                            )
                        }
                        
                        showCursoDialog = false
                        cursoSeleccionado = null
                    }
                ) {
                    Text(if (cursoSeleccionado == null) "Crear" else "Actualizar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCursoDialog = false
                        cursoSeleccionado = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para confirmar eliminación de curso
    if (showDeleteCursoDialog && cursoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteCursoDialog = false
                cursoSeleccionado = null
            },
            title = { Text("Eliminar Curso") },
            text = {
                Text("¿Está seguro de que desea eliminar el curso '${cursoSeleccionado?.nombre}'? Esta acción no se puede deshacer y eliminará todas las clases asociadas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarCurso(cursoSeleccionado!!.id)
                        showDeleteCursoDialog = false
                        cursoSeleccionado = null
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
                    onClick = { 
                        showDeleteCursoDialog = false
                        cursoSeleccionado = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoItem(
    curso: Curso,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono del curso
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información del curso
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = curso.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Año académico: ${curso.anioAcademico}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Edad: ${curso.edadMinima} - ${curso.edadMaxima} años",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Botones de acción
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar curso",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar curso",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Descripción si está disponible
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
            
            // Botón para ver clases
            Button(
                onClick = onItemClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Class,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text("Ver Clases")
            }
        }
    }
}

// Función auxiliar para obtener el año actual
private fun getCurrentYear(): Int {
    return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
} 