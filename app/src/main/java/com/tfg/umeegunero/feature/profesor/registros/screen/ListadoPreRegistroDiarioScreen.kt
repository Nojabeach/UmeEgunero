package com.tfg.umeegunero.feature.profesor.registros.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
import java.time.ZoneId
import java.util.Date
import timber.log.Timber
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * Extensión para convertir LocalDate a Date
 */
fun LocalDate.toDate(): Date {
    return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

/**
 * Extensión para convertir Date a LocalDate
 */
fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

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
    viewModel: ListadoPreRegistroDiarioViewModel = hiltViewModel(),
    profesorId: String? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Pasar el profesorId al ViewModel cuando se proporciona
    LaunchedEffect(profesorId) {
        if (!profesorId.isNullOrEmpty()) {
            Timber.d("Profesor ID recibido: $profesorId")
            viewModel.setProfesorId(profesorId)
        }
    }
    
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
    
    // Efecto para limpiar mensajes y errores cuando el composable se va
    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiarError()
            viewModel.limpiarMensajeExito()
        }
    }

    // Efecto para recargar datos cuando la pantalla se reanuda
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.cargarDatos() // Recargar datos al volver a la pantalla
        }
    }
    
    LaunchedEffect(uiState.navegarARegistroDiario) {
        if (uiState.navegarARegistroDiario && uiState.alumnosSeleccionados.isNotEmpty()) {
            val alumnosIds = uiState.alumnosSeleccionados.joinToString(",") { it.id }
            val fecha = uiState.fechaSeleccionada.toString()
            // Log detallado ANTES de navegar
            Timber.d("ListadoPreRegistroDiarioScreen: Intentando navegar a RegistroDiarioProfesor.")
            Timber.d("ListadoPreRegistroDiarioScreen: alumnosSeleccionados: ${uiState.alumnosSeleccionados.map { "ID: " + it.id + " Nombre: " + it.nombre }}")
            Timber.d("ListadoPreRegistroDiarioScreen: alumnosIds para la ruta: '$alumnosIds'")
            Timber.d("ListadoPreRegistroDiarioScreen: fecha para la ruta: '$fecha'")

            if (alumnosIds.isBlank()) {
                Timber.e("ListadoPreRegistroDiarioScreen: alumnosIds está vacío. No se puede navegar. Revisar lógica de selección o IDs de alumnos.")
                // Podrías mostrar un Snackbar de error aquí
                viewModel.limpiarError() // Limpia cualquier error anterior
                viewModel.mostrarError("No se pudo iniciar el registro: ID de alumno no válido.")
                viewModel.resetearNavegacion() // Resetea el flag de navegación
                return@LaunchedEffect // No navegar
            }

            // Usar el método de creación de ruta definido en AppScreens
            val route = AppScreens.RegistroDiarioProfesor.createRouteWithParams(
                alumnosIds = alumnosIds,
                fecha = fecha
            )
            Timber.d("ListadoPreRegistroDiarioScreen: Ruta construida: '$route'")
            navController.navigate(route)
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
                PresentesFAB(
                    selectedCount = uiState.alumnosSeleccionados.size,
                    totalCount = uiState.alumnos.size,
                    onFabClick = { viewModel.iniciarRegistroDiario() }
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
            
            // Acciones rápidas
            AccionesRapidas(
                onSelectAll = { viewModel.seleccionarTodosLosAlumnos() },
                onDeselectAll = { viewModel.deseleccionarTodosLosAlumnos() }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Lista de alumnos
            LazyColumn {
                items(uiState.alumnos) { alumno ->
                    AlumnoSelectionChip(
                        alumno = alumno,
                        isSelected = uiState.alumnosSeleccionados.contains(alumno),
                        onSelectionChanged = { selected ->
                            if (selected) {
                                viewModel.seleccionarAlumno(alumno)
                            } else {
                                viewModel.deseleccionarAlumno(alumno)
                            }
                        },
                        tieneRegistro = uiState.alumnosConRegistro.contains(alumno.id)
                    )
                }
            }
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
                selectedDate = fechaSeleccionada.toDate(),
                onDateSelected = { fecha -> 
                    onFechaSeleccionada(fecha.toLocalDate())
                },
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
 * Componente que representa un chip de selección de alumno
 */
@Composable
fun AlumnoSelectionChip(
    alumno: Alumno,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    tieneRegistro: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = when {
            tieneRegistro -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !tieneRegistro) { onSelectionChanged(!isSelected) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar con inicial
                Surface(
                    shape = CircleShape,
                    color = when {
                        tieneRegistro -> Color.Gray
                        isSelected -> ProfesorColor
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = alumno.nombre.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected && !tieneRegistro) 
                                Color.White 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Información del alumno
                Column {
                    Text(
                        text = "${alumno.nombre} ${alumno.apellidos}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (tieneRegistro)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Mensaje de estado
                    Text(
                        text = if (tieneRegistro) 
                            "Ya registrado hoy • No seleccionable" 
                        else 
                            "DNI: ${alumno.dni}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (tieneRegistro)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Icono de selección
            if (tieneRegistro) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Registro bloqueado",
                    tint = MaterialTheme.colorScheme.error
                )
            } else {
                Icon(
                    imageVector = when {
                        isSelected -> Icons.Default.CheckCircle
                        else -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = when {
                        isSelected -> "Alumno seleccionado"
                        else -> "Alumno no seleccionado"
                    },
                    tint = when {
                        isSelected -> ProfesorColor
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Componente FAB con contador de seleccionados
 */
@Composable
fun PresentesFAB(
    selectedCount: Int,
    totalCount: Int,
    onFabClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.End
    ) {
        // Tooltip que aparece al expandir
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "$selectedCount de $totalCount alumnos seleccionados",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // FAB principal
        ExtendedFloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            expanded = true
                            try {
                                awaitRelease()
                            } finally {
                                expanded = false
                            }
                        }
                    )
                },
            containerColor = ProfesorColor,
            contentColor = Color.White
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
                Text(
                    text = "Registrar $selectedCount Presentes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Componente de acciones rápidas
 */
@Composable
fun AccionesRapidas(
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = onSelectAll,
            label = { Text("Seleccionar Todos") },
            leadingIcon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        FilterChip(
            selected = false,
            onClick = onDeselectAll,
            label = { Text("Deseleccionar Todos") },
            leadingIcon = {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
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
            
            fun seleccionarFecha(@Suppress("UNUSED_PARAMETER") fecha: LocalDate) {}
            fun seleccionarAlumno(@Suppress("UNUSED_PARAMETER") alumno: Alumno) {}
            fun deseleccionarAlumno(@Suppress("UNUSED_PARAMETER") alumno: Alumno) {}
            fun seleccionarTodosLosAlumnos() {}
            fun deseleccionarTodosLosAlumnos() {}
            fun iniciarRegistroDiario() {}
            fun resetearNavegacion() {}
            fun limpiarError() {}
            fun limpiarMensajeExito() {}
            fun mostrarError(@Suppress("UNUSED_PARAMETER") mensaje: String) {}
        }
        
        ListadoPreRegistroDiarioScreen(
            navController = navController, 
            viewModel = fakeViewModel as ListadoPreRegistroDiarioViewModel
        )
    }
} 