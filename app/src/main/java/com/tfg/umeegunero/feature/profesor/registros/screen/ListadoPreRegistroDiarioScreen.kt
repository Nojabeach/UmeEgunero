package com.tfg.umeegunero.feature.profesor.registros.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.feature.profesor.registros.viewmodel.ListadoPreRegistroDiarioViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.calendar.DateSelector
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import com.tfg.umeegunero.feature.profesor.registros.viewmodel.ListadoPreRegistroDiarioUiState

/**
 * Pantalla de listado de alumnos para registro diario
 *
 * Esta pantalla permite al profesor seleccionar uno o varios alumnos para realizar
 * el registro diario. Incluye:
 * - Selector de fecha
 * - Lista de alumnos con indicador de asistencia
 * - Opción para registrar múltiples alumnos simultáneamente
 * - Botón para completar automáticamente todos los registros
 *
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona el estado de la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListadoPreRegistroDiarioScreen(
    navController: NavController,
    viewModel: ListadoPreRegistroDiarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado para el diálogo de confirmación
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.limpiarError()
        }
    }
    
    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensajeExito()
        }
    }
    
    LaunchedEffect(uiState.navegarARegistroDiario) {
        if (uiState.navegarARegistroDiario && uiState.alumnosSeleccionados.isNotEmpty()) {
            val alumnosIds = uiState.alumnosSeleccionados.joinToString(",") { it.id }
            val fecha = uiState.fechaSeleccionada.toString()
            navController.navigate(AppScreens.RegistroDiarioProfesor.createRouteWithParams(alumnosIds, fecha))
            viewModel.resetearNavegacion()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro Diario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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
            if (uiState.alumnosSeleccionados.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.iniciarRegistroDiario() },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Registrar ${uiState.alumnosSeleccionados.size} alumnos") },
                    containerColor = ProfesorColor
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Cabecera con selector de fecha
            SelectorFecha(
                fechaSeleccionada = uiState.fechaSeleccionada,
                onFechaSeleccionada = { viewModel.seleccionarFecha(it) },
                esFestivo = uiState.esFestivo,
                nombreDia = uiState.fechaSeleccionada.dayOfWeek.getDisplayName(
                    TextStyle.FULL,
                    Locale("es", "ES")
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información de la clase
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ProfesorColor.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = ProfesorColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.nombreClase,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${uiState.totalAlumnos} alumnos en total · ${uiState.alumnosPresentes} presentes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para seleccionar todos
                Button(
                    onClick = { viewModel.seleccionarTodosLosAlumnos() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfesorColor
                    )
                ) {
                    Icon(Icons.Default.SelectAll, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar todos")
                }
                
                // Botón para completar automáticamente
                OutlinedButton(
                    onClick = { mostrarDialogoConfirmacion = true },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, ProfesorColor)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ProfesorColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Auto-completar",
                        color = ProfesorColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Barra de filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alumnos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Filtro de asistencia
                AssistanceFilterChip(
                    seleccionado = uiState.mostrarSoloPresentes,
                    onSeleccionado = { viewModel.toggleFiltroPresentes() }
                )
            }
            
            // Lista de alumnos
            if (uiState.alumnosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay alumnos disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.alumnosFiltrados,
                        key = { it.id }
                    ) { alumno ->
                        AlumnoItem(
                            alumno = alumno,
                            seleccionado = uiState.alumnosSeleccionados.contains(alumno),
                            tieneRegistro = uiState.alumnosConRegistro.contains(alumno.id),
                            onSeleccionado = { viewModel.toggleSeleccionAlumno(alumno) },
                            modifier = Modifier.animateItemPlacement(
                                animationSpec = tween(durationMillis = 300)
                            )
                        )
                    }
                }
            }
        }
        
        // Diálogo de confirmación para auto-completar
        if (mostrarDialogoConfirmacion) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoConfirmacion = false },
                title = { Text("Completar automáticamente") },
                text = { 
                    Text(
                        "¿Deseas completar automáticamente el registro diario para todos los alumnos presentes? Esto creará registros con valores por defecto (comidas completas, sin siesta, sin deposiciones)."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.completarAutomaticamente()
                            mostrarDialogoConfirmacion = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfesorColor
                        )
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Componente para seleccionar la fecha del registro diario
 */
@Composable
fun SelectorFecha(
    fechaSeleccionada: LocalDate,
    onFechaSeleccionada: (LocalDate) -> Unit,
    esFestivo: Boolean,
    nombreDia: String
) {
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "ES"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esFestivo) 
                MaterialTheme.colorScheme.errorContainer
            else 
                ProfesorColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (esFestivo) 
                        MaterialTheme.colorScheme.onErrorContainer
                    else 
                        Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nombreDia.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (esFestivo) 
                        MaterialTheme.colorScheme.onErrorContainer
                    else 
                        Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = fechaSeleccionada.format(formatter).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                color = if (esFestivo) 
                    MaterialTheme.colorScheme.onErrorContainer
                else 
                    Color.White.copy(alpha = 0.9f)
            )
            
            if (esFestivo) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Día festivo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de fecha
            DateSelector(
                selectedDate = fechaSeleccionada,
                onDateSelected = onFechaSeleccionada,
                buttonColors = ButtonDefaults.buttonColors(
                    containerColor = if (esFestivo)
                        MaterialTheme.colorScheme.error
                    else
                        Color.White,
                    contentColor = if (esFestivo)
                        Color.White
                    else
                        ProfesorColor
                )
            )
        }
    }
}

/**
 * Filtro de asistencia para mostrar solo alumnos presentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistanceFilterChip(
    seleccionado: Boolean,
    onSeleccionado: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = onSeleccionado,
        label = { Text("Solo presentes") },
        leadingIcon = {
            Icon(
                imageVector = if (seleccionado) 
                    Icons.Default.CheckCircle 
                else 
                    Icons.Default.Person,
                contentDescription = null
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ProfesorColor,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}

/**
 * Elemento de la lista de alumnos
 */
@Composable
fun AlumnoItem(
    alumno: Alumno,
    seleccionado: Boolean,
    tieneRegistro: Boolean,
    onSeleccionado: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSeleccionado() },
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                ProfesorColor.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (seleccionado)
            BorderStroke(1.dp, ProfesorColor)
        else
            null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de selección
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (seleccionado)
                            ProfesorColor
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (seleccionado) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                } else {
                    Text(
                        text = alumno.nombre.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del alumno
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = alumno.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (alumno.presente) 
                            Icons.Default.CheckCircle 
                        else 
                            Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (alumno.presente)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alumno.presente) "Presente" else "Ausente",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (alumno.presente)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Indicador de registro existente
            AnimatedVisibility(
                visible = tieneRegistro,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Green.copy(alpha = 0.3f))
                        .border(1.dp, Color.Green.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = "Registro completado",
                        tint = Color.Green,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Checkbox de selección
            Checkbox(
                checked = seleccionado,
                onCheckedChange = { onSeleccionado() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ProfesorColor
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListadoPreRegistroDiarioScreenPreview() {
    UmeEguneroTheme {
        val navController = rememberNavController()
        
        // Datos de prueba
        val alumnosDePrueba = listOf(
            Alumno(
                id = "1", 
                nombre = "Ana García", 
                claseId = "clase_1",
                presente = true
            ),
            Alumno(
                id = "2", 
                nombre = "Pedro Martínez", 
                claseId = "clase_1",
                presente = true
            ),
            Alumno(
                id = "3", 
                nombre = "Sara López", 
                claseId = "clase_1",
                presente = false
            ),
            Alumno(
                id = "4", 
                nombre = "Carlos Rodríguez", 
                claseId = "clase_1",
                presente = true
            )
        )
        
        // Estado para la preview
        val previewState = ListadoPreRegistroDiarioUiState(
            alumnos = alumnosDePrueba,
            alumnosFiltrados = alumnosDePrueba,
            fechaSeleccionada = LocalDate.now(),
            esFestivo = false,
            nombreClase = "Infantil 2A",
            alumnosConRegistro = setOf("1"),
            totalAlumnos = alumnosDePrueba.size,
            alumnosPresentes = alumnosDePrueba.count { it.presente },
            isLoading = false
        )
        
        val fakeViewModel = object : ViewModel() {
            private val _uiState = MutableStateFlow(previewState)
            val uiState: StateFlow<ListadoPreRegistroDiarioUiState> = _uiState.asStateFlow()
            
            fun seleccionarFecha(fecha: LocalDate) {}
            fun toggleSeleccionAlumno(alumno: Alumno) {}
            fun seleccionarTodosLosAlumnos() {}
            fun toggleFiltroPresentes() {}
            fun completarAutomaticamente() {}
            fun iniciarRegistroDiario() {}
            fun resetearNavegacion() {}
            fun limpiarError() {}
            fun limpiarMensajeExito() {}
        }
        
        ListadoPreRegistroDiarioScreen(
            navController = navController, 
            viewModel = fakeViewModel as ListadoPreRegistroDiarioViewModel
        )
    }
} 