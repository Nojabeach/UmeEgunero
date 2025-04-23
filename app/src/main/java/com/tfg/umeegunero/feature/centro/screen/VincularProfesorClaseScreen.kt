package com.tfg.umeegunero.feature.centro.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.VincularProfesorClaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Pantalla para vincular profesores a clases
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VincularProfesorClaseScreen(
    onBack: () -> Unit,
    viewModel: VincularProfesorClaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Manejo de mensajes y errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.limpiarError()
        }
    }
    
    LaunchedEffect(uiState.showSuccessMessage, uiState.mensaje) {
        if (uiState.showSuccessMessage && uiState.mensaje != null) {
            Toast.makeText(context, uiState.mensaje, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajeExito()
            // Esperamos un poco para que el usuario vea el mensaje
            delay(300)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vincular Profesores a Clases") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Recargar datos
                        scope.launch {
                            val centroId = uiState.centroId
                            if (centroId.isNotEmpty()) {
                                viewModel.cargarCursos(centroId)
                                viewModel.cargarProfesores(centroId)
                                if (uiState.cursoSeleccionado != null) {
                                    viewModel.cargarClases(uiState.cursoSeleccionado!!.id)
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar datos"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Cargando datos...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Selector de curso
                CursoSelector(
                    cursos = uiState.cursos,
                    cursoSeleccionado = uiState.cursoSeleccionado,
                    onCursoSelected = { curso -> 
                        if (curso.id.isNotEmpty()) {
                            viewModel.seleccionarCurso(curso)
                        } else {
                            // Si se recibe un curso vacío, recargar los datos
                            val centroId = uiState.centroId
                            if (centroId.isNotEmpty()) {
                                scope.launch {
                                    viewModel.cargarCursos(centroId)
                                }
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contenido principal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Lista de profesores
                    ProfesorList(
                        profesores = uiState.profesores,
                        profesorSeleccionado = uiState.profesorSeleccionado,
                        onProfesorSelected = { 
                            Timber.d("Profesor seleccionado: ${it.nombre} ${it.apellidos}")
                            viewModel.seleccionarProfesor(it) 
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    
                    // Lista de clases y asignaciones
                    if (uiState.profesorSeleccionado != null && uiState.cursoSeleccionado != null) {
                        ClasesList(
                            clases = uiState.clases,
                            clasesAsignadas = uiState.clasesAsignadas[uiState.profesorSeleccionado?.documentId] ?: emptyList(),
                            profesorSeleccionado = uiState.profesorSeleccionado,
                            onAsignarClase = { clase ->
                                viewModel.seleccionarClase(clase)
                                
                                val profesorId = uiState.profesorSeleccionado?.documentId ?: ""
                                val claseId = clase.id
                                
                                if (viewModel.isProfesorAsignadoAClase(profesorId, claseId)) {
                                    viewModel.mostrarDialogoConfirmarDesasignacion(true)
                                } else {
                                    viewModel.mostrarDialogoAsignarClases(true)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    } else {
                        // Mensaje de selección
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Selecciona un curso y un profesor para gestionar las asignaciones de clases",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Información y estadísticas
                if (uiState.profesorSeleccionado != null) {
                    val clasesAsignadas = uiState.clasesAsignadas[uiState.profesorSeleccionado?.documentId] ?: emptyList()
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Profesor: ${uiState.profesorSeleccionado?.nombre} ${uiState.profesorSeleccionado?.apellidos}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Clases asignadas: ${clasesAsignadas.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para asignar profesor
    if (uiState.showAsignarClasesDialog) {
        ConfirmacionDialog(
            title = "Asignar Profesor",
            message = "¿Estás seguro de que deseas asignar a ${uiState.profesorSeleccionado?.nombre} ${uiState.profesorSeleccionado?.apellidos} a la clase ${uiState.claseSeleccionada?.nombre}?",
            onConfirm = {
                viewModel.asignarProfesorAClase()
            },
            onDismiss = {
                viewModel.mostrarDialogoAsignarClases(false)
            }
        )
    }
    
    // Diálogo de confirmación para desasignar profesor
    if (uiState.showConfirmarDesasignacionDialog) {
        ConfirmacionDialog(
            title = "Desasignar Profesor",
            message = "¿Estás seguro de que deseas desasignar a ${uiState.profesorSeleccionado?.nombre} ${uiState.profesorSeleccionado?.apellidos} de la clase ${uiState.claseSeleccionada?.nombre}?",
            onConfirm = {
                viewModel.desasignarProfesorDeClase()
            },
            onDismiss = {
                viewModel.mostrarDialogoConfirmarDesasignacion(false)
            }
        )
    }
}

@Composable
fun CursoSelector(
    cursos: List<Curso>,
    cursoSeleccionado: Curso?,
    onCursoSelected: (Curso) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Curso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mostrar contador de cursos
                Text(
                    text = "${cursos.size} cursos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (cursos.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Botón para recargar cursos
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        // Aquí no podemos llamar directamente al ViewModel, 
                        // pero esto será manejado por el composable padre
                        onCursoSelected(cursoSeleccionado ?: Curso())
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar cursos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (cursos.isEmpty()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (cursos.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (cursos.isEmpty()) 
                            "No hay cursos disponibles" 
                        else 
                            cursoSeleccionado?.nombre ?: "Selecciona un curso",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (cursos.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Ocultar opciones" else "Mostrar opciones",
                        tint = if (cursos.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded && cursos.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                cursos.forEach { curso ->
                    DropdownMenuItem(
                        text = { Text(curso.nombre) },
                        onClick = {
                            onCursoSelected(curso)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        // Mensaje de ayuda si no hay cursos
        if (cursos.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Primero debe crear cursos en la sección de 'Gestión de Cursos'",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun ProfesorList(
    profesores: List<Usuario>,
    profesorSeleccionado: Usuario?,
    onProfesorSelected: (Usuario) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Profesores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(profesores) { profesor ->
                    Timber.d("Renderizando profesor: ${profesor.nombre} ${profesor.apellidos}, documentId: ${profesor.documentId}")
                    ProfesorItem(
                        profesor = profesor,
                        isSelected = profesor.documentId == profesorSeleccionado?.documentId,
                        onProfesorSelected = onProfesorSelected
                    )
                }
                
                if (profesores.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No hay profesores disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Comprueba tu conexión a Firebase",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfesorItem(
    profesor: Usuario,
    isSelected: Boolean,
    onProfesorSelected: (Usuario) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onProfesorSelected(profesor) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${profesor.nombre} ${profesor.apellidos}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = profesor.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ClasesList(
    clases: List<Clase>,
    clasesAsignadas: List<Clase>,
    profesorSeleccionado: Usuario?,
    onAsignarClase: (Clase) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Clases",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(clases) { clase ->
                    val isAsignada = clasesAsignadas.any { it.id == clase.id }
                    
                    ClaseItem(
                        clase = clase,
                        isAsignada = isAsignada,
                        onAsignarClase = { onAsignarClase(clase) }
                    )
                }
                
                if (clases.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay clases disponibles")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClaseItem(
    clase: Clase,
    isAsignada: Boolean,
    onAsignarClase: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isAsignada) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = clase.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isAsignada) 
                        MaterialTheme.colorScheme.onTertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                clase.aula?.let {
                    Text(
                        text = "Aula: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAsignada) 
                            MaterialTheme.colorScheme.onTertiaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isAsignada) {
                FilledTonalIconButton(
                    onClick = onAsignarClase,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Desasignar profesor",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                FilledIconButton(
                    onClick = onAsignarClase
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Asignar profesor"
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmacionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
} 