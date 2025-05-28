package com.tfg.umeegunero.feature.profesor.registros.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import com.tfg.umeegunero.feature.profesor.registros.viewmodel.InformeAsistencia
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Intent
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.filled.Notifications
import com.tfg.umeegunero.data.model.NotificacionAusencia
import com.tfg.umeegunero.data.model.EstadoNotificacionAusencia
import java.text.SimpleDateFormat

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
    
    // Mostrar diálogo de informe si es necesario
    if (uiState.mostrarDialogoInforme) {
        InformeAsistenciaDialog(
            informe = uiState.datosInforme,
            onDismiss = { viewModel.cerrarDialogoInforme() }
        )
    }
    
    // Mostrar diálogo de detalle de ausencia si es necesario
    val ausenciaSeleccionada = uiState.ausenciaSeleccionada
    if (uiState.mostrarDialogoAusencia && ausenciaSeleccionada != null) {
        DetalleAusenciaDialog(
            ausencia = ausenciaSeleccionada,
            onDismiss = { viewModel.cerrarDetalleAusencia() },
            onAceptar = { ausencia -> viewModel.procesarAusencia(ausencia, true) },
            onRechazar = { ausencia -> viewModel.procesarAusencia(ausencia, false) }
        )
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
            val alumnosSeleccionadosConRegistro = uiState.alumnosSeleccionados.count { alumno ->
                uiState.alumnosConRegistro.contains(alumno.id)
            }
            val alumnosSeleccionadosSinRegistro = uiState.alumnosSeleccionados.count { alumno ->
                !uiState.alumnosConRegistro.contains(alumno.id)
            }
            
            if (uiState.alumnosSeleccionados.isNotEmpty()) {
                PresentesFAB(
                    selectedCount = uiState.alumnosSeleccionados.size,
                    totalCount = uiState.alumnos.size,
                    alumnosSinRegistro = uiState.alumnos.count { !uiState.alumnosConRegistro.contains(it.id) },
                    alumnosSeleccionadosConRegistro = alumnosSeleccionadosConRegistro,
                    alumnosSeleccionadosSinRegistro = alumnosSeleccionadosSinRegistro,
                    onFabClick = { 
                        if (alumnosSeleccionadosSinRegistro > 0) {
                            viewModel.iniciarRegistroDiario()
                        }
                    }
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
            // Notificación de nueva ausencia
            AnimatedVisibility(
                visible = uiState.hayNuevaNotificacionAusencia,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                NuevaAusenciaNotificacion(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Cabecera con selector de fecha
            SelectorFecha(
                fechaSeleccionada = uiState.fechaSeleccionada,
                onFechaSeleccionada = { viewModel.seleccionarFecha(it) },
                esFestivo = uiState.esFestivo,
                nombreDia = uiState.fechaSeleccionada.dayOfWeek.getDisplayName(
                    TextStyle.FULL,
                    Locale("es", "ES")
                ),
                onGenerarInforme = { viewModel.generarInforme() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información de la clase
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ProfesorColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
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
                    
                    // Indicador de registros completados
                    if (uiState.alumnosConRegistro.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${uiState.alumnosConRegistro.size} alumno${if (uiState.alumnosConRegistro.size != 1) "s" else ""} con registro completado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Acciones rápidas
            AccionesRapidas(
                onSelectAll = { viewModel.seleccionarTodosLosAlumnos() },
                onDeselectAll = { viewModel.deseleccionarTodosLosAlumnos() },
                alumnosSinRegistro = uiState.alumnos.count { !uiState.alumnosConRegistro.contains(it.id) },
                alumnosSeleccionados = uiState.alumnosSeleccionados.size
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
                        tieneRegistro = uiState.alumnosConRegistro.contains(alumno.id),
                        onEliminarRegistro = { id -> viewModel.eliminarRegistro(id) },
                        ausenciaJustificada = uiState.alumnosConAusenciaJustificada.contains(alumno.id)
                    )
                }
            }
            
            // Mostrar ausencias notificadas si hay alguna
            if (uiState.ausenciasNotificadas.isNotEmpty()) {
                AusenciasNotificadasCard(
                    ausencias = uiState.ausenciasNotificadas,
                    onVerDetalle = { ausencia -> viewModel.mostrarDetalleAusencia(ausencia) }
                )
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
    nombreDia: String,
    onGenerarInforme: () -> Unit = {}
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
                Spacer(modifier = Modifier.weight(1f))
                // Botón de informe de asistencia
                IconButton(
                    onClick = onGenerarInforme,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (esFestivo)
                                MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)
                            else
                                Color.White.copy(alpha = 0.2f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Generar informe de asistencia",
                        tint = if (esFestivo)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            Color.White
                    )
                }
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
 * Chip para seleccionar un alumno
 */
@Composable
fun AlumnoSelectionChip(
    alumno: Alumno,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    tieneRegistro: Boolean,
    onEliminarRegistro: (String) -> Unit,
    ausenciaJustificada: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Diálogo de confirmación para eliminar registro
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar registro") },
            text = { 
                Text("¿Estás seguro de que quieres eliminar el registro de ${alumno.nombre} para hoy?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onEliminarRegistro(alumno.id)
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp, max = 72.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectionChanged(!isSelected)
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                tieneRegistro && isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                tieneRegistro -> MaterialTheme.colorScheme.secondaryContainer
                isSelected -> ProfesorColor.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            tieneRegistro && isSelected -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            tieneRegistro -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            isSelected -> BorderStroke(1.dp, ProfesorColor)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox o indicador de estado
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    tieneRegistro -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Registro completado",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    isSelected -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Seleccionado",
                            tint = ProfesorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Información del alumno (limitada para mostrar lo esencial)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (tieneRegistro) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Registrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Indicador compacto de estado de lectura
                        Icon(
                            imageVector = if (alumno.registroDiarioLeido == true) 
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (alumno.registroDiarioLeido == true) 
                                "Visto" else "No visto",
                            tint = if (alumno.registroDiarioLeido == true) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // Indicador de estado o botón de eliminar (más compacto)
            if (tieneRegistro) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar registro",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        ausenciaJustificada -> MaterialTheme.colorScheme.tertiaryContainer
                        alumno.presente -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = when {
                            ausenciaJustificada -> "Ausente Justificado"
                            alumno.presente -> "Presente"
                            else -> "Ausente"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            ausenciaJustificada -> MaterialTheme.colorScheme.onTertiaryContainer
                            alumno.presente -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
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
    alumnosSinRegistro: Int,
    alumnosSeleccionadosConRegistro: Int,
    alumnosSeleccionadosSinRegistro: Int,
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
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (alumnosSeleccionadosSinRegistro > 0) {
                        Text(
                            text = "$alumnosSeleccionadosSinRegistro alumno${if (alumnosSeleccionadosSinRegistro != 1) "s" else ""} para crear registro",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (alumnosSeleccionadosConRegistro > 0) {
                        Text(
                            text = "$alumnosSeleccionadosConRegistro alumno${if (alumnosSeleccionadosConRegistro != 1) "s" else ""} ya con registro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (totalCount - alumnosSinRegistro > 0) {
                        Text(
                            text = "${totalCount - alumnosSinRegistro} en total ya tienen registro hoy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
            containerColor = if (alumnosSeleccionadosSinRegistro > 0) ProfesorColor else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (alumnosSeleccionadosSinRegistro > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = null
                )
                Text(
                    text = if (alumnosSeleccionadosSinRegistro > 0) {
                        "Crear Registro ($alumnosSeleccionadosSinRegistro)"
                    } else {
                        "Sin alumnos nuevos"
                    },
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
    onDeselectAll: () -> Unit,
    alumnosSinRegistro: Int = 0,
    alumnosSeleccionados: Int = 0
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
            enabled = alumnosSinRegistro > 0,
            label = { 
                Text(
                    if (alumnosSinRegistro > 0) 
                        "Seleccionar Todos ($alumnosSinRegistro)" 
                    else 
                        "Todos con registro"
                ) 
            },
            leadingIcon = {
                Icon(
                    if (alumnosSinRegistro > 0) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        
        FilterChip(
            selected = false,
            onClick = onDeselectAll,
            enabled = alumnosSeleccionados > 0,
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

/**
 * Diálogo para mostrar el informe de asistencia
 */
@Composable
fun InformeAsistenciaDialog(
    informe: InformeAsistencia,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "ES"))
    val context = LocalContext.current
    val viewModel: ListadoPreRegistroDiarioViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    
    // Estado para manejar la exportación de PDF
    var isExportingPdf by remember { mutableStateOf(false) }
    var pdfExportedMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Informe de Asistencia",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = informe.fecha.format(formatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = ProfesorColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Estadísticas de asistencia
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Asistencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Indicador de porcentaje
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(informe.porcentajeAsistencia / 100f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (informe.porcentajeAsistencia > 70f) 
                                            MaterialTheme.colorScheme.primary 
                                        else if (informe.porcentajeAsistencia > 40f) 
                                            MaterialTheme.colorScheme.tertiary 
                                        else 
                                            MaterialTheme.colorScheme.error
                                    )
                            )
                            
                            Text(
                                text = "${String.format("%.1f", informe.porcentajeAsistencia)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Datos de asistencia
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total alumnos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${informe.totalAlumnos}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Presentes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${informe.alumnosPresentes}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Ausentes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${informe.alumnosAusentes}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
                // Estadísticas de registro
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Registro diario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Con registro",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${informe.alumnosConRegistro}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Sin registro",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${informe.alumnosSinRegistro}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (informe.alumnosSinRegistro > 0) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Indicador de estado de exportación PDF
                if (isExportingPdf) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ProfesorColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generando PDF...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Mensaje de éxito o error en la exportación
                pdfExportedMessage?.let {
                    Surface(
                        color = if (it.startsWith("Error")) 
                            MaterialTheme.colorScheme.errorContainer
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (it.startsWith("Error")) 
                                    Icons.Default.Error
                                else 
                                    Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (it.startsWith("Error")) 
                                    MaterialTheme.colorScheme.error
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            Row {
                // Botón para compartir texto
                TextButton(
                    onClick = {
                        val textoInforme = viewModel.generarTextoInforme()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Informe de asistencia")
                            putExtra(Intent.EXTRA_TEXT, textoInforme)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir informe"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir como texto",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartir texto")
                }
                
                // Botón para exportar PDF
                TextButton(
                    onClick = {
                        // Evitar múltiples clics
                        if (isExportingPdf) return@TextButton
                        
                        scope.launch {
                            isExportingPdf = true
                            pdfExportedMessage = null
                            
                            try {
                                val pdfUri = viewModel.exportarInformeAsistenciaPDF(context)
                                
                                if (pdfUri != null) {
                                    // Crear intent para compartir PDF
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, pdfUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
                                    pdfExportedMessage = "PDF generado correctamente"
                                } else {
                                    // Android 10+: suponemos que se guardó pero no tenemos URI
                                    pdfExportedMessage = "PDF guardado en Descargas"
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al exportar PDF")
                                pdfExportedMessage = "Error: No se pudo generar el PDF"
                            } finally {
                                isExportingPdf = false
                            }
                        }
                    },
                    enabled = !isExportingPdf
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Exportar como PDF",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exportar PDF")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Diálogo para mostrar detalles y procesar una notificación de ausencia
 */
@Composable
fun DetalleAusenciaDialog(
    ausencia: NotificacionAusencia,
    onDismiss: () -> Unit,
    onAceptar: (NotificacionAusencia) -> Unit,
    onRechazar: (NotificacionAusencia) -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sick,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Notificación de ausencia",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Información del alumno
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Alumno",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = ausencia.alumnoNombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Clase: ${ausencia.claseCurso}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Detalles de la ausencia
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Detalles de la ausencia",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fecha:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = dateFormatter.format(ausencia.fechaAusencia.toDate()),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (ausencia.duracion > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Duración:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Text(
                                    text = "${ausencia.duracion} días",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = "Motivo",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = ausencia.motivo,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                
                // Información del familiar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Notificado por",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = ausencia.familiarNombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Fecha: ${dateFormatter.format(ausencia.fechaNotificacion.toDate())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAceptar(ausencia) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aceptar ausencia")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { onRechazar(ausencia) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rechazar")
            }
        }
    )
}

/**
 * Tarjeta para mostrar ausencias notificadas
 */
@Composable
fun AusenciasNotificadasCard(
    ausencias: List<NotificacionAusencia>,
    onVerDetalle: (NotificacionAusencia) -> Unit
) {
    if (ausencias.isEmpty()) return
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sick,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Ausencias notificadas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(text = ausencias.size.toString())
                }
            }
            
            Divider(
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(ausencias) { ausencia ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onVerDetalle(ausencia) },
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = ausencia.alumnoNombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    Text(
                                        text = "Fecha: ${dateFormatter.format(ausencia.fechaAusencia.toDate())}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                    )
                                    
                                    if (ausencia.duracion > 1) {
                                        Text(
                                            text = " • ${ausencia.duracion} días",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                
                                if (ausencia.motivo.length > 30) {
                                    Text(
                                        text = ausencia.motivo.take(30) + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = ausencia.motivo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { onVerDetalle(ausencia) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Ver detalles",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra un alumno en la lista con su estado de asistencia
 * 
 * Este componente muestra la información básica del alumno y su estado actual,
 * permitiendo seleccionarlo para crear un registro diario o eliminar un registro existente.
 * 
 * @param alumno Datos del alumno a mostrar
 * @param isSelected Indica si el alumno está seleccionado para crear registro
 * @param tieneRegistro Indica si el alumno ya tiene un registro para la fecha actual
 * @param onToggleSelection Callback para cambiar el estado de selección
 * @param onEliminarRegistro Callback para eliminar el registro existente
 * @param ausenciaJustificada Indica si el alumno tiene una ausencia justificada
 */
@Composable
fun AlumnoItem(
    alumno: Alumno,
    isSelected: Boolean,
    tieneRegistro: Boolean,
    onToggleSelection: () -> Unit,
    onEliminarRegistro: () -> Unit,
    ausenciaJustificada: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Diálogo de confirmación para eliminar registro
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar registro") },
            text = { 
                Text("¿Estás seguro de que quieres eliminar el registro de ${alumno.nombre} para hoy?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onEliminarRegistro()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp, max = 72.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleSelection()
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                tieneRegistro && isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                tieneRegistro -> MaterialTheme.colorScheme.secondaryContainer
                isSelected -> ProfesorColor.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            tieneRegistro && isSelected -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            tieneRegistro -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            isSelected -> BorderStroke(1.dp, ProfesorColor)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox o indicador de estado
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    tieneRegistro -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Registro completado",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    isSelected -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Seleccionado",
                            tint = ProfesorColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Información del alumno (limitada para mostrar lo esencial)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (tieneRegistro) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Registrado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Indicador compacto de estado de lectura
                        Icon(
                            imageVector = if (alumno.registroDiarioLeido == true) 
                                Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (alumno.registroDiarioLeido == true) 
                                "Visto" else "No visto",
                            tint = if (alumno.registroDiarioLeido == true) 
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            // Indicador de estado o botón de eliminar (más compacto)
            if (tieneRegistro) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar registro",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        ausenciaJustificada -> MaterialTheme.colorScheme.tertiaryContainer
                        alumno.presente -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = when {
                            ausenciaJustificada -> "Ausente Justificado"
                            alumno.presente -> "Presente"
                            else -> "Ausente"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            ausenciaJustificada -> MaterialTheme.colorScheme.onTertiaryContainer
                            alumno.presente -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
        }
    }
}

/**
 * Componente para mostrar una notificación cuando llega una nueva ausencia
 */
@Composable
fun NuevaAusenciaNotificacion(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Nueva notificación de ausencia recibida",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
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
            fun eliminarRegistro(@Suppress("UNUSED_PARAMETER") id: String) {}
            fun generarInforme() {}
        }
        
        ListadoPreRegistroDiarioScreen(
            navController = navController, 
            viewModel = fakeViewModel as ListadoPreRegistroDiarioViewModel
        )
    }
} 