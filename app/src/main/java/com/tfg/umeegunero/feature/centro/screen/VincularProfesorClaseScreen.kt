package com.tfg.umeegunero.feature.centro.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.VincularProfesorClaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
                        Icon(Icons.Default.Close, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                    onCursoSelected = { viewModel.seleccionarCurso(it) }
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
                        onProfesorSelected = { viewModel.seleccionarProfesor(it) },
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
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Selecciona un curso y un profesor para gestionar las asignaciones de clases",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        Text(
            text = "Curso",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cursoSeleccionado?.nombre ?: "Selecciona un curso",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Ocultar opciones" else "Mostrar opciones"
                )
            }
            
            DropdownMenu(
                expanded = expanded,
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
        
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(profesores) { profesor ->
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
                            Text("No hay profesores disponibles")
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onProfesorSelected(profesor) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
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
        
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAsignada) Color(0xFFE8F5E9).copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant
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
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                clase.aula?.let {
                    Text(
                        text = "Aula: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (isAsignada) {
                IconButton(
                    onClick = onAsignarClase
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Desasignar profesor",
                        tint = Color.Red
                    )
                }
            } else {
                IconButton(
                    onClick = onAsignarClase
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Asignar profesor",
                        tint = MaterialTheme.colorScheme.primary
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            onConfirm()
                            onDismiss()
                        }
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
} 