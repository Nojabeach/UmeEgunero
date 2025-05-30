package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.tfg.umeegunero.feature.profesor.viewmodel.DetalleAlumnoProfesorViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.DetalleAlumnoProfesorUiState
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.DateFormat
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.InformacionCard
import com.tfg.umeegunero.ui.components.AccionRapida
import com.google.firebase.Timestamp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import timber.log.Timber
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope

/**
 * Pantalla de detalle de un alumno para profesores
 *
 * Muestra toda la información relevante del alumno seleccionado.
 *
 * @param navController Controlador de navegación para volver atrás
 * @param alumnoId ID del alumno a mostrar
 * @param viewModel ViewModel que gestiona el estado y la carga de datos del alumno
 *
 * @author Equipo UmeEgunero
 * @version 4.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAlumnoProfesorScreen(
    navController: NavController,
    alumnoId: String,
    viewModel: DetalleAlumnoProfesorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showProgramarReunionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(alumnoId) {
        viewModel.loadAlumno(alumnoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.alumno?.nombre ?: "Detalle de Alumno") },
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
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { snackbarHostState }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                         Spacer(Modifier.height(8.dp))
                         Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                         Spacer(Modifier.height(8.dp))
                         Button(onClick = { viewModel.loadAlumno(alumnoId) }) {
                             Text("Reintentar")
                         }
                    }
                }
                uiState.alumno != null -> {
                    DetalleAlumnoContent(
                        alumno = uiState.alumno!!,
                        navController = navController,
                        showProgramarReunionDialog = showProgramarReunionDialog,
                        scope = scope,
                        snackbarHostState = snackbarHostState,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Diálogo para crear reunión con el familiar
        if (showProgramarReunionDialog) {
            val now = LocalDateTime.now()
            val initialDate = now.plusDays(1).withHour(10).withMinute(0)
            var selectedDate by remember { mutableStateOf(initialDate) }
            var duracion by remember { mutableStateOf(30) } // En minutos
            var tipoReunion by remember { mutableStateOf("Tutoría") }
            var notas by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showProgramarReunionDialog = false },
                title = { Text("Programar reunión") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Selector de fecha y hora
                        OutlinedButton(
                            onClick = {
                                // Abrir DatePicker
                                viewModel.showDatePicker(selectedDate) { newDate ->
                                    selectedDate = newDate
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fecha: ${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        // Selector de duración
                        Text(
                            text = "Duración: $duracion minutos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = duracion.toFloat(),
                            onValueChange = { duracion = it.toInt() },
                            valueRange = 15f..60f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Selector de tipo de reunión
                        OutlinedTextField(
                            value = tipoReunion,
                            onValueChange = { tipoReunion = it },
                            label = { Text("Tipo de reunión") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = "Tipo de reunión"
                                )
                            },
                            singleLine = true
                        )
                        
                        // Notas
                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text("Notas adicionales") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Note,
                                    contentDescription = "Notas"
                                )
                            },
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Programar la reunión
                            viewModel.programarReunion(
                                fecha = selectedDate,
                                duracionMinutos = duracion,
                                tipo = tipoReunion,
                                notas = notas,
                                alumnoId = alumnoId,
                                alumnoNombre = uiState.alumno?.nombreCompleto ?: ""
                            )
                            showProgramarReunionDialog = false
                            // Mostrar mensaje de confirmación
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Reunión programada correctamente"
                                )
                            }
                        }
                    ) {
                        Text("Programar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showProgramarReunionDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Vista previa de la pantalla de detalle de alumno para profesores
 */
@Preview(showBackground = true)
@Composable
fun DetalleAlumnoProfesorScreenPreview() {
    // Ejemplo de uso para visualizar en el editor
    DetalleAlumnoProfesorScreen(
        navController = rememberNavController(),
        alumnoId = "12345678A"
    )
}

/**
 * Contenido principal de la pantalla de detalle del alumno.
 * Muestra la información formateada usando LazyColumn y componentes reutilizables.
 *
 * @param alumno Objeto Alumno con los datos a mostrar.
 * @param navController NavController para acciones (ej. ir al chat).
 */
@Composable
private fun DetalleAlumnoContent(
    alumno: Alumno,
    navController: NavController,
    showProgramarReunionDialog: Boolean,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    viewModel: DetalleAlumnoProfesorViewModel
) {
    // Formateador para YYYY-MM-DD
    val inputDateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    // Formateador para mostrar fecha larga en español
    val outputDateFormatter = remember { DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.LONG).withLocale(Locale("es", "ES")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (alumno.avatarUrl.isNotBlank()) {
                        // Cargar imagen real desde la URL
                        AsyncImage(
                            model = alumno.avatarUrl,
                            contentDescription = "Avatar de ${alumno.nombre}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback a icono para cuando no hay imagen
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar Alumno",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${alumno.nombre} ${alumno.apellidos}".trim(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Clase: ${alumno.clase.takeIf { it.isNotEmpty() } ?: "No asignada"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    val edad = calcularEdad(alumno.fechaNacimiento)
                     if (edad != null) {
                         Text(
                            text = "$edad ${if (edad == 1) "año" else "años"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AccionRapida(
                    title = "Registro Hoy",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    onClick = {
                        // Navegar al listado de pre-registro con el alumno preseleccionado
                        try {
                            // Primero navegamos a la pantalla de listado
                            navController.navigate(AppScreens.ListadoPreRegistroDiario.route) {
                                // Podríamos pasar el alumno como argumento si la ruta lo soportara
                                // Por ahora, el usuario tendrá que seleccionar el alumno en la pantalla
                            }
                            Timber.d("Navegando a Listado Pre-Registro Diario desde detalle de alumno: ${alumno.nombre}")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Listado Pre-Registro Diario")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                AccionRapida(
                    title = "Chat Familia",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    onClick = {
                        if (alumno.familiarIds.isNotEmpty()) {
                            val familiarId = alumno.familiarIds.first()
                            try {
                                // Extraer solo la parte base de la ruta, sin los parámetros
                                val chatRouteName = AppScreens.ChatProfesor.route.split("/")[0] // Extraer solo la parte "chat_profesor"
                                navController.navigate(AppScreens.ChatContacts.createRoute(chatRouteName = chatRouteName))
                                Timber.d("Navegando a ContactChat para inicio de chat con familiares de ${alumno.nombre}")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar a Chat: ${e.message}")
                            }
                        } else {
                            Timber.w("No hay familiares asociados a este alumno para iniciar chat")
                            // Aquí podrías mostrar un SnackBar o un diálogo informando del error
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val fechaFormateada = try {
                val localDate = LocalDate.parse(alumno.fechaNacimiento, inputDateFormatter)
                outputDateFormatter.format(localDate)
            } catch (e: DateTimeParseException) {
                alumno.fechaNacimiento.ifEmpty { "Fecha inválida" }
            }
            InformacionCard(title = "Fecha Nacimiento", icon = Icons.Default.Cake, content = fechaFormateada)
        }
        item {
            val alergiasContent = if (alumno.alergias.isNotEmpty()) alumno.alergias.joinToString() else "Ninguna especificada"
            InformacionCard(title = "Alergias", icon = Icons.Default.MedicalInformation, content = alergiasContent)
        }
        item { InformacionCard(title = "Observaciones Generales", icon = Icons.Default.Info, content = alumno.observaciones.takeIf { it.isNotEmpty() } ?: "Sin observaciones") }
        item {
             val familiaresContent = if (alumno.familiarIds.isNotEmpty()) alumno.familiarIds.joinToString() else "No asignados"
            InformacionCard(title = "Contactos Familiares (IDs)", icon = Icons.Default.Contacts, content = familiaresContent)
         }
        item {
             val medicacionContent = if (alumno.medicacion.isNotEmpty()) alumno.medicacion.joinToString() else "Ninguna especificada"
             InformacionCard(title = "Medicación", icon = Icons.Default.Medication, content = medicacionContent)
         }
         item { InformacionCard(title = "Necesidades Especiales", icon = Icons.Default.Accessible, content = alumno.necesidadesEspeciales.takeIf { it.isNotEmpty() } ?: "Ninguna especificada") }
         item { InformacionCard(title = "Observaciones Médicas", icon = Icons.Default.MedicalServices, content = alumno.observacionesMedicas.takeIf { it.isNotEmpty() } ?: "Ninguna especificada") }
         item { InformacionCard(title = "Número de Seguridad Social", icon = Icons.Default.HealthAndSafety, content = alumno.numeroSS.takeIf { it.isNotEmpty() } ?: "No especificado") }
         item { InformacionCard(title = "Condiciones Médicas", icon = Icons.Default.MonitorHeart, content = alumno.condicionesMedicas.takeIf { it.isNotEmpty() } ?: "Ninguna especificada") }

    }
}

/**
 * Calcula la edad en años a partir de un String de fecha (YYYY-MM-DD).
 * @param fechaNacimientoString String de la fecha de nacimiento en formato YYYY-MM-DD.
 * @return Edad en años, o null si el string es inválido o la fecha es futura.
 */
fun calcularEdad(fechaNacimientoString: String?): Int? {
    if (fechaNacimientoString.isNullOrEmpty()) return null
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val fechaNac = LocalDate.parse(fechaNacimientoString, formatter)
        val hoy = LocalDate.now()
        val periodo = Period.between(fechaNac, hoy)
        if (periodo.isNegative) null else periodo.years
    } catch (e: DateTimeParseException) {
        Timber.e(e, "Error al parsear fecha de nacimiento: $fechaNacimientoString")
        null
    }
}

/**
 * Vista previa de la pantalla de detalle de alumno para profesores
 */
@Preview(showBackground = true, name = "Detalle Alumno (Loading)")
@Composable
fun DetalleAlumnoProfesorScreenLoadingPreview() {
    UmeEguneroTheme {
        Scaffold(
             topBar = { TopAppBar(title = { Text("Detalle de Alumno") }) }
        ) { padding ->
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        }
    }
}

@Preview(showBackground = true, name = "Detalle Alumno (Error)")
@Composable
fun DetalleAlumnoProfesorScreenErrorPreview() {
    UmeEguneroTheme {
        Scaffold(
             topBar = { TopAppBar(title = { Text("Detalle de Alumno") }) }
        ) { padding ->
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                     Spacer(Modifier.height(8.dp))
                     Text(
                         text = "Error: No se pudo conectar",
                         color = MaterialTheme.colorScheme.error,
                         textAlign = TextAlign.Center
                     )
                     Spacer(Modifier.height(8.dp))
                     Button(onClick = { }) {
                         Text("Reintentar")
                     }
                 }
            }
        }
    }
}

@Preview(showBackground = true, name = "Detalle Alumno (Success)")
@Composable
fun DetalleAlumnoProfesorScreenSuccessPreview() {
    UmeEguneroTheme { 
         val previewAlumno = Alumno(
             id = "previewId",
             dni = "12345678A",
             nombre = "Ane",
             apellidos = "García López",
             fechaNacimiento = "2021-05-15",
             clase = "Infantil 3A",
             alergias = listOf("Frutos secos", "Lactosa"),
             observaciones = "Necesita ayuda para comer solo.",
             familiarIds = listOf("fam1", "fam2"),
             medicacion = listOf("Ventolin si precisa"),
             necesidadesEspeciales = "",
             observacionesMedicas = "Revisión anual otorrino"
         )
         Scaffold(
             topBar = { TopAppBar(title = { Text("${previewAlumno.nombre}") }) }
         ) { padding ->
             Box(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
                 DetalleAlumnoContent(
                    alumno = previewAlumno,
                    navController = rememberNavController(),
                    showProgramarReunionDialog = false,
                    scope = rememberCoroutineScope(),
                    snackbarHostState = SnackbarHostState(),
                    viewModel = hiltViewModel()
                 )
            }
        }
    }
} 