package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person2
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.feature.profesor.viewmodel.MisAlumnosProfesorViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.feature.profesor.viewmodel.MisAlumnosUiState
import androidx.compose.material3.HorizontalDivider
import timber.log.Timber
import com.tfg.umeegunero.util.performHapticFeedbackSafely
import android.widget.Toast
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School

/**
 * Pantalla que muestra la lista de alumnos del profesor
 *
 * Permite ver y gestionar la información de todos los alumnos
 * asignados al profesor en su clase. Desde aquí, el profesor puede:
 * - Ver la lista completa de alumnos
 * - Buscar alumnos por nombre
 * - Acceder al perfil detallado de cada alumno
 * - Visualizar las vinculaciones familiares
 *
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que provee los datos de alumnos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisAlumnosProfesorScreen(
    navController: NavController,
    viewModel: MisAlumnosProfesorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    // Estado para el diálogo de programación de reunión
    var mostrarDialogoReunion by remember { mutableStateOf(false) }
    var alumnoSeleccionadoParaReunion by remember { mutableStateOf<Alumno?>(null) }
    
    // Estado para el diálogo de exportación
    var mostrarDialogoExportar by remember { mutableStateOf(false) }
    var filtroExportacion by remember { mutableStateOf("todos") } // "todos", "seleccionados", "filtrados"
    var formatoExportacion by remember { mutableStateOf("pdf") } // "pdf", "csv", "excel"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Alumnos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogoExportar = true }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exportar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (uiState.alumnos.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Lógica para generar un informe o exportar
                        mostrarDialogoExportar = true
                    },
                    containerColor = ProfesorColor,
                    contentColor = Color.White,
                    icon = { 
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Exportar informe"
                        )
                    },
                    text = { Text("Exportar") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Buscar alumno...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Selector de curso
            if (uiState.cursos.isNotEmpty()) {
                Text(
                    text = "Selecciona un curso:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.cursos) { curso ->
                        val isSelected = uiState.cursoSeleccionado?.id == curso.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.seleccionarCurso(curso) },
                            label = { 
                                Text(
                                    text = curso.nombre,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ProfesorColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            
            // Selector de clase (si hay un curso seleccionado y clases disponibles)
            if (uiState.cursoSeleccionado != null && uiState.clases.isNotEmpty()) {
                Text(
                    text = "Selecciona una clase:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.clases) { clase ->
                        val isSelected = uiState.claseSeleccionada?.id == clase.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.seleccionarClase(clase) },
                            label = { 
                                Text(
                                    text = clase.nombre,
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ProfesorColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            
            // Mostrar información relevante según el estado de selección
            if (uiState.cursoSeleccionado != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = ProfesorColor.copy(alpha = 0.3f)
                )
                
                if (uiState.claseSeleccionada != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = ProfesorColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.cursoSeleccionado?.nombre} - ${uiState.claseSeleccionada?.nombre}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (uiState.alumnos.isNotEmpty()) {
                            Text(
                                text = "${uiState.alumnos.size} alumnos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "Curso: ${uiState.cursoSeleccionado?.nombre}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selecciona una clase para ver sus alumnos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Contenido principal
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ProfesorColor)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                            
                            Button(
                                onClick = { viewModel.limpiarError() },
                                colors = ButtonDefaults.buttonColors(containerColor = ProfesorColor)
                            ) {
                                Text("Entendido")
                            }
                        }
                    }
                }
                uiState.cursoSeleccionado == null -> {
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
                                modifier = Modifier.size(64.dp),
                                tint = ProfesorColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Selecciona un curso para continuar",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                uiState.claseSeleccionada == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ProfesorColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Selecciona una clase para ver los alumnos",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                uiState.alumnos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ProfesorColor.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay alumnos en esta clase",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    // Lista de alumnos (filtrados por búsqueda)
                    val alumnosFiltrados = uiState.alumnos.filter {
                        searchQuery.isEmpty() || 
                        "${it.nombre} ${it.apellidos}".contains(searchQuery, ignoreCase = true) ||
                        it.dni.contains(searchQuery, ignoreCase = true)
                    }
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(alumnosFiltrados) { alumno ->
                            AlumnoCard(
                                alumno = alumno,
                                onClick = {
                                    navController.navigate(
                                        AppScreens.DetalleAlumnoProfesor.createRoute(alumno.dni)
                                    )
                                },
                                onProgramarReunionClick = {
                                    alumnoSeleccionadoParaReunion = alumno
                                    mostrarDialogoReunion = true
                                    haptic.performHapticFeedbackSafely()
                                }
                            )
                        }
                        // Añadir espacio al final para que el último elemento no quede oculto por el FAB
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // Diálogo para programar reunión
    if (mostrarDialogoReunion && alumnoSeleccionadoParaReunion != null) {
        ProgramarReunionDialog(
            alumno = alumnoSeleccionadoParaReunion!!,
            onDismiss = { 
                mostrarDialogoReunion = false 
                alumnoSeleccionadoParaReunion = null
            },
            onProgramar = { titulo, descripcion, fecha, hora ->
                viewModel.programarReunion(
                    alumnoSeleccionadoParaReunion!!.dni,
                    titulo,
                    fecha,
                    hora,
                    descripcion
                )
                mostrarDialogoReunion = false
                alumnoSeleccionadoParaReunion = null
            }
        )
    }

    // Diálogo de exportación de informe
    if (mostrarDialogoExportar) {
        Dialog(
            onDismissRequest = { mostrarDialogoExportar = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Cabecera
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GetApp,
                            contentDescription = null,
                            tint = ProfesorColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Exportar Informe de Alumnos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opciones de exportación
                    Text(
                        text = "Selecciona los alumnos a incluir:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filtroExportacion == "todos",
                            onClick = { filtroExportacion = "todos" },
                            label = { Text("Todos") },
                            leadingIcon = {
                                if (filtroExportacion == "todos") {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                        
                        FilterChip(
                            selected = filtroExportacion == "filtrados",
                            onClick = { filtroExportacion = "filtrados" },
                            label = { Text("Filtrados") },
                            leadingIcon = {
                                if (filtroExportacion == "filtrados") {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Formato de exportación
                    Text(
                        text = "Formato de exportación:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = formatoExportacion == "pdf",
                            onClick = { formatoExportacion = "pdf" },
                            label = { Text("PDF") },
                            leadingIcon = {
                                if (formatoExportacion == "pdf") {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                        
                        FilterChip(
                            selected = formatoExportacion == "excel",
                            onClick = { formatoExportacion = "excel" },
                            label = { Text("Excel") },
                            leadingIcon = {
                                if (formatoExportacion == "excel") {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                        
                        FilterChip(
                            selected = formatoExportacion == "csv",
                            onClick = { formatoExportacion = "csv" },
                            label = { Text("CSV") },
                            leadingIcon = {
                                if (formatoExportacion == "csv") {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vista previa del informe
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Vista previa del informe",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Cabecera de tabla
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ProfesorColor.copy(alpha = 0.1f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "Nombre", 
                                    fontWeight = FontWeight.Bold, 
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    "DNI", 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.3f)
                                )
                                Text(
                                    "Clase", 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.3f)
                                )
                            }

                            // Filas de ejemplo
                            val alumnos = uiState.alumnos.take(3)
                            if (alumnos.isNotEmpty()) {
                                alumnos.forEach { alumno ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            "${alumno.nombre} ${alumno.apellidos}",
                                            modifier = Modifier.weight(0.4f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            alumno.dni,
                                            modifier = Modifier.weight(0.3f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            alumno.clase,
                                            modifier = Modifier.weight(0.3f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    HorizontalDivider(thickness = 0.5.dp)
                                }
                                Text(
                                    "... y ${uiState.alumnos.size - 3} más",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "No hay datos para mostrar",
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información de filas a exportar
                    val totalFilas = when (filtroExportacion) {
                        "todos" -> uiState.alumnos.size
                        "filtrados" -> if (searchQuery.isNotEmpty()) {
                            uiState.alumnos.count { 
                                it.nombre.contains(searchQuery, ignoreCase = true) || 
                                it.apellidos.contains(searchQuery, ignoreCase = true)
                            }
                        } else uiState.alumnos.size
                        else -> 0
                    }

                    Text(
                        text = "Se exportarán $totalFilas filas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { mostrarDialogoExportar = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancelar")
                        }
                        
                        Button(
                            onClick = {
                                mostrarDialogoExportar = false
                                viewModel.generarInformeAlumnos(
                                    filtro = filtroExportacion,
                                    formato = formatoExportacion,
                                    terminoBusqueda = searchQuery
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfesorColor
                            )
                        ) {
                            Text("Exportar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta que muestra la información de un alumno
 */
@Composable
fun AlumnoCard(
    alumno: Alumno,
    onClick: () -> Unit,
    onProgramarReunionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar / Inicial del alumno
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = alumno.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${alumno.nombre} ${alumno.apellidos}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver detalle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para programar reunión
                    IconButton(
                        onClick = onProgramarReunionClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Programar reunión",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente de diálogo para programar una reunión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramarReunionDialog(
    alumno: Alumno,
    onDismiss: () -> Unit,
    onProgramar: (titulo: String, descripcion: String, fecha: String, hora: String) -> Unit
) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("Reunión con familiar de ${alumno.nombre}") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
    var hora by remember { mutableStateOf("16:00") }
    
    var mostrarDatePicker by remember { mutableStateOf(false) }
    
    // DatePicker para seleccionar la fecha
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fechaSeleccionada = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            fecha = fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Programar reunión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { },
                        label = { Text("Fecha") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { mostrarDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
                            }
                        }
                    )
                    
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora") },
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            onProgramar(titulo, descripcion, fecha, hora)
                            Toast.makeText(context, "Reunión programada", Toast.LENGTH_SHORT).show()
                        },
                        enabled = titulo.isNotBlank()
                    ) {
                        Text("Programar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MisAlumnosProfesorScreenPreview() {
    UmeEguneroTheme {
        // En modo preview, solo usamos un navController simulado
        // No podemos usar un viewModel real porque depende de inyección de dependencias
        // No importa ya que la preview solo es visual
        MisAlumnosProfesorScreen(
            navController = rememberNavController()
            // No pasamos viewModel, se usará el proporcionado por hiltViewModel()
        )
    }
} 