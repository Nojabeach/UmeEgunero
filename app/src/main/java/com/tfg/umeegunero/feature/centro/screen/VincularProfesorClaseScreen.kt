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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
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
import com.tfg.umeegunero.data.model.Centro
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
                                    viewModel.cargarClasesPorCurso(uiState.cursoSeleccionado!!.id)
                                }
                            } else if (uiState.isAdminApp) {
                                viewModel.cargarTodosCentros()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar datos",
                            modifier = Modifier.size(size = 32.dp)
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
                // Selector de centro (solo visible para admin de app)
                if (uiState.isAdminApp) {
                    CentroSelector(
                        centros = uiState.centros,
                        centroSeleccionado = uiState.centroSeleccionado,
                        onCentroSelected = { centro -> 
                            viewModel.seleccionarCentro(centro)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Solo mostrar el resto de la pantalla si hay un centro seleccionado
                if (uiState.centroId.isNotEmpty()) {
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
                                profesorClases = uiState.clasesAsignadas[uiState.profesorSeleccionado?.documentId] ?: emptyMap(),
                                onClick = { clase ->
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
                                        modifier = Modifier.size(size = 48.dp)
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
                        val clasesAsignadas = uiState.clasesAsignadas[uiState.profesorSeleccionado?.documentId]
                        val totalClasesAsignadas = clasesAsignadas?.count { it.value } ?: 0
                        
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
                                    text = "Clases asignadas: $totalClasesAsignadas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else if (uiState.isAdminApp && uiState.centros.isNotEmpty()) {
                    // Mensaje de selección de centro para admin app
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                                modifier = Modifier.size(size = 48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Selecciona un centro educativo para comenzar",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Como administrador de la aplicación, debes seleccionar primero un centro educativo para gestionar sus cursos, clases y profesores.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para asignar profesor
    if (uiState.showAsignarClasesDialog) {
        DialogoConfirmacion(
            show = uiState.showAsignarClasesDialog,
            onConfirm = {
                viewModel.asignarProfesorAClase()
            },
            onDismiss = {
                viewModel.mostrarDialogoAsignarClases(false)
            },
            profesorSeleccionado = uiState.profesorSeleccionado,
            claseSeleccionada = uiState.claseSeleccionada,
            isAsignada = false
        )
    }
    
    // Diálogo de confirmación para desasignar profesor
    if (uiState.showConfirmarDesasignacionDialog) {
        DialogoConfirmacion(
            show = uiState.showConfirmarDesasignacionDialog,
            onConfirm = {
                viewModel.desasignarProfesorDeClase()
            },
            onDismiss = {
                viewModel.mostrarDialogoConfirmarDesasignacion(false)
            },
            profesorSeleccionado = uiState.profesorSeleccionado,
            claseSeleccionada = uiState.claseSeleccionada,
            isAsignada = true
        )
    }
}

@Composable
fun CentroSelector(
    centros: List<Centro>,
    centroSeleccionado: Centro?,
    onCentroSelected: (Centro) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Centro Educativo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mostrar contador de centros
                Text(
                    text = "${centros.size} centros disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (centros.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Botón para recargar centros
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        // Aquí no podemos llamar directamente al ViewModel
                        onCentroSelected(Centro())
                    },
                    modifier = Modifier.size(size = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar centros",
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
                    .clickable { 
                        // Solo expandir si hay centros
                        if (centros.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (centros.isEmpty()) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (centros.isEmpty()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.outline
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
                        text = if (centros.isEmpty()) 
                            "No hay centros disponibles" 
                        else 
                            centroSeleccionado?.nombre ?: "Selecciona un centro educativo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (centros.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Icon(
                        imageVector = if (expanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) 
                            "Ocultar opciones" 
                        else 
                            "Mostrar opciones",
                        tint = if (centros.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            DropdownMenu(
                expanded = expanded && centros.isNotEmpty(),
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                centros.forEach { centro ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = centro.nombre,
                                    fontWeight = if (centro.activo) FontWeight.Normal else FontWeight.Light
                                )
                                
                                if (!centro.activo) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Inactivo",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            onCentroSelected(centro)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        // Mensaje de ayuda si no hay centros
        if (centros.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No se encontraron centros educativos. Contacta al administrador.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Botón adicional para recargar explícitamente
            Button(
                onClick = { onCentroSelected(Centro()) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        modifier = Modifier.size(size = 18.dp)
                    )
                    Text("Recargar datos")
                }
            }
        }
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
                // Mostrar contador de cursos con colores adecuados
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
                    modifier = Modifier.size(size = 32.dp)
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
                    .clickable { 
                        // Solo expandir si hay cursos
                        if (cursos.isNotEmpty()) {
                            expanded = !expanded
                        }
                    },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (cursos.isEmpty()) 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (cursos.isEmpty()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.outline
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
                        color = if (cursos.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    Icon(
                        imageVector = if (expanded) 
                            Icons.Default.KeyboardArrowUp 
                        else 
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) 
                            "Ocultar opciones" 
                        else 
                            "Mostrar opciones",
                        tint = if (cursos.isEmpty()) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(curso.nombre)
                                
                                if (!curso.activo) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Inactivo",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(size = 36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay cursos disponibles para este centro",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Los cursos deben crearse desde la sección de gestión de cursos",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onCursoSelected(Curso()) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar cursos",
                        modifier = Modifier.size(size = 18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recargar cursos")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cursos disponibles: ${cursos.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onCursoSelected(cursoSeleccionado ?: Curso()) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar cursos",
                        modifier = Modifier.size(size = 18.dp)
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
        // Título con contador de profesores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profesores",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Mostrar contador con color apropiado
            Text(
                text = "${profesores.size} disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = if (profesores.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (profesores.isEmpty()) 
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface
            )
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
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(size = 48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    "No hay profesores disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    "Comprueba que existan profesores asignados a este centro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    "Puedes añadir profesores en la sección 'Gestión de Personal'",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
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
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                if (profesor.activo) 
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${profesor.nombre} ${profesor.apellidos}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (!profesor.activo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Inactivo",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
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

/**
 * Lista de clases de un curso
 */
@Composable
fun ClasesList(
    profesorClases: Map<Clase, Boolean>,
    onClick: (Clase) -> Unit,
    modifier: Modifier = Modifier
) {
    if (profesorClases.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay clases disponibles para este curso",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(profesorClases.entries.toList()) { (clase, asignada) ->
                ClaseItem(
                    clase = clase,
                    isAsignada = asignada,
                    onClick = { onClick(clase) }
                )
                Divider()
            }
        }
    }
}

/**
 * Componente que representa un elemento de clase en la lista
 */
@Composable
fun ClaseItem(
    clase: Clase,
    isAsignada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isAsignada) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isAsignada) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Clase: ${clase.nombre}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aula: ${clase.aula}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (clase.horario.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Horario: ${clase.horario}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (isAsignada) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Profesor asignado",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Asignar profesor",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Diálogo de confirmación para asignar o desasignar un profesor a una clase
 */
@Composable
fun DialogoConfirmacion(
    show: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    profesorSeleccionado: Usuario?,
    claseSeleccionada: Clase?,
    isAsignada: Boolean
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = if (isAsignada) Icons.Filled.RemoveCircle else Icons.Filled.AddCircle,
                    contentDescription = null,
                    tint = if (isAsignada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = if (isAsignada) "Desasignar profesor" else "Asignar profesor",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isAsignada) 
                            "¿Estás seguro de que deseas desasignar a ${profesorSeleccionado?.nombre} ${profesorSeleccionado?.apellidos} de la clase ${claseSeleccionada?.nombre}?"
                        else 
                            "¿Estás seguro de que deseas asignar a ${profesorSeleccionado?.nombre} ${profesorSeleccionado?.apellidos} a la clase ${claseSeleccionada?.nombre}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (isAsignada) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esta acción eliminará al profesor de esta clase y los alumnos ya no podrán ver sus asistencias.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAsignada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = if (isAsignada) "Desasignar" else "Asignar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
} 