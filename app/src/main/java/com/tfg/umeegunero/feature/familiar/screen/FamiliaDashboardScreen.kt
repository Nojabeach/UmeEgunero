package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.StatsOverviewCard
import com.tfg.umeegunero.ui.components.StatItem
import com.tfg.umeegunero.ui.components.StatsOverviewRow
import com.tfg.umeegunero.ui.components.charts.LineChart
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import android.content.res.Configuration
import android.widget.Toast
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.ui.components.CategoriaCardData
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCardBienvenida
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import com.tfg.umeegunero.util.DateUtils
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import com.tfg.umeegunero.data.model.NotificacionAusencia
import com.tfg.umeegunero.data.model.EstadoNotificacionAusencia
import java.time.LocalDate
import java.time.ZoneId

/**
 * Modelo para representar una solicitud de vinculación pendiente
 */
data class SolicitudPendienteUI(
    val id: String,
    val alumnoNombre: String? = null,
    val alumnoDni: String,
    val centroId: String,
    val centroNombre: String? = null,
    val fechaSolicitud: Date,
    val estado: EstadoSolicitud
)

/**
 * Diálogo para crear una nueva solicitud de vinculación
 *
 * @param centros Lista de centros disponibles para vinculación
 * @param onDismiss Callback al cerrar el diálogo
 * @param onSubmit Callback al enviar la solicitud (DNI, CentroID)
 * @param isLoading Indica si se está procesando el envío
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSolicitudDialog(
    centros: List<Centro>,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    isLoading: Boolean = false
) {
    var alumnoDni by remember { mutableStateOf("") }
    var centroSeleccionado by remember { mutableStateOf<Centro?>(null) }
    var showCentrosDropdown by remember { mutableStateOf(false) }
    var dniError by remember { mutableStateOf<String?>(null) }
    
    // Función de validación de DNI
    fun validateDni(dni: String): Boolean {
        val dniPattern = Regex("^\\d{8}[A-HJ-NP-TV-Z]$")
        if (!dniPattern.matches(dni.uppercase())) return false
        val letras = "TRWAGMYFPDXBNJZSQVHLCKE"
        val numero = dni.substring(0, 8).toIntOrNull() ?: return false
        return dni.uppercase()[8] == letras[numero % 23]
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = FamiliarColor,
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Nueva solicitud de vinculación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Introduce los datos del alumno que deseas vincular",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Campo DNI
                OutlinedTextField(
                    value = alumnoDni,
                    onValueChange = { 
                        alumnoDni = it.uppercase()
                        dniError = if (it.isNotBlank() && !validateDni(it)) {
                            "Formato de DNI inválido"
                        } else null
                    },
                    label = { Text("DNI del alumno") },
                    placeholder = { Text("Ejemplo: 12345678A") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Badge, 
                            contentDescription = null
                        )
                    },
                    isError = dniError != null,
                    supportingText = { 
                        if (dniError != null) {
                            Text(dniError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de centro
                ExposedDropdownMenuBox(
                    expanded = showCentrosDropdown,
                    onExpandedChange = { showCentrosDropdown = !showCentrosDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = centroSeleccionado?.nombre ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Centro educativo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCentrosDropdown) },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.School, 
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        placeholder = { Text("Selecciona un centro") },
                        singleLine = true
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCentrosDropdown,
                        onDismissRequest = { showCentrosDropdown = false }
                    ) {
                        centros.forEach { centro ->
                            DropdownMenuItem(
                                text = { Text(centro.nombre) },
                                onClick = {
                                    centroSeleccionado = centro
                                    showCentrosDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            // Verificar que el centro está seleccionado
                            centroSeleccionado?.let { centro ->
                                onSubmit(alumnoDni, centro.id)
                            } ?: run {
                                // Manejar el caso de centro no seleccionado
                                Timber.e("No se ha seleccionado un centro")
                            }
                        },
                        enabled = alumnoDni.isNotBlank() && validateDni(alumnoDni) && centroSeleccionado != null && !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FamiliarColor)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para notificar una ausencia
 *
 * @param alumno Alumno que estará ausente
 * @param onDismiss Callback al cerrar el diálogo
 * @param onSubmit Callback al enviar la notificación (alumnoId, alumnoNombre, fecha, motivo, duración, claseId, claseCurso)
 * @param isLoading Indica si se está procesando el envío
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificarAusenciaDialog(
    alumno: Alumno,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Date, String, Int, String, String) -> Unit,
    isLoading: Boolean = false
) {
    var motivo by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf(Date()) }
    var duracion by remember { mutableStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Control de errores
    var motivoError by remember { mutableStateOf<String?>(null) }
    
    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaSeleccionada.time
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            fechaSeleccionada = Date(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Sick,
                    contentDescription = null,
                    tint = FamiliarColor,
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Notificar ausencia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Alumno: ${alumno.nombre} ${alumno.apellidos ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Selector de fecha
                OutlinedTextField(
                    value = dateFormatter.format(fechaSeleccionada),
                    onValueChange = { /* No editable manualmente */ },
                    readOnly = true,
                    label = { Text("Fecha de ausencia") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.DateRange, 
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de duración
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duración:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        IconButton(
                            onClick = { if (duracion > 1) duracion-- },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Disminuir días"
                            )
                        }
                        
                        Text(
                            text = "$duracion día${if (duracion > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        IconButton(
                            onClick = { if (duracion < 14) duracion++ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aumentar días"
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo motivo
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { 
                        motivo = it
                        motivoError = if (it.isBlank()) "El motivo es obligatorio" else null
                    },
                    label = { Text("Motivo de la ausencia") },
                    placeholder = { Text("Ejemplo: Enfermedad, cita médica...") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null
                        )
                    },
                    isError = motivoError != null,
                    supportingText = { 
                        if (motivoError != null) {
                            Text(motivoError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (motivo.isBlank()) {
                                motivoError = "El motivo es obligatorio"
                                return@Button
                            }
                            
                            onSubmit(
                                alumno.id,
                                "${alumno.nombre} ${alumno.apellidos ?: ""}",
                                fechaSeleccionada,
                                motivo,
                                duracion,
                                alumno.claseId,
                                alumno.curso ?: ""
                            )
                        },
                        enabled = !isLoading && motivo.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Notificar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta para mostrar las ausencias pendientes
 */
@Composable
fun AusenciasPendientesCard(
    ausencias: List<NotificacionAusencia>,
    modifier: Modifier = Modifier
) {
    if (ausencias.isEmpty()) return
    
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                verticalAlignment = Alignment.CenterVertically
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
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ausencias.take(3).forEach { ausencia ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = ausencia.alumnoNombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Fecha: ${dateFormatter.format(ausencia.fechaAusencia.toDate())}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            if (ausencia.duracion > 1) {
                                Text(
                                    text = "Duración: ${ausencia.duracion} días",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = when (ausencia.estado) {
                                        EstadoNotificacionAusencia.PENDIENTE.name -> "Pendiente"
                                        EstadoNotificacionAusencia.ACEPTADA.name -> "Aceptada"
                                        EstadoNotificacionAusencia.RECHAZADA.name -> "Rechazada"
                                        else -> "Completada"
                                    }
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (ausencia.estado) {
                                    EstadoNotificacionAusencia.PENDIENTE.name -> MaterialTheme.colorScheme.primaryContainer
                                    EstadoNotificacionAusencia.ACEPTADA.name -> MaterialTheme.colorScheme.tertiaryContainer
                                    EstadoNotificacionAusencia.RECHAZADA.name -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )
                    }
                }
            }
            
            if (ausencias.size > 3) {
                Text(
                    text = "Y ${ausencias.size - 3} más...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Pantalla principal del dashboard para familiares
 * 
 * Esta pantalla muestra toda la información relevante para los familiares de los alumnos:
 * - Lista de hijos asociados a la cuenta
 * - Resumen de registros diarios recientes
 * - Métricas de actividad y progreso
 * - Acceso rápido a funcionalidades principales
 * - Solicitudes de vinculación de nuevos hijos
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona los datos del dashboard
 * 
 * @author Equipo UmeEgunero
 * @version 5.1
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaDashboardScreen(
    navController: NavController,
    viewModel: FamiliarDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Si ocurrió un error, mostramos un Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Variables para control de animaciones
    var showContent by remember { mutableStateOf(false) }
    
    // Estado para controlar el diálogo de nueva solicitud
    var showNuevaSolicitudDialog by remember { mutableStateOf(false) }
    
    // Estados para diálogos de confirmación
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNavigateToNotificacionesDialog by remember { mutableStateOf(false) }
    var showNavigateToCalendarioDialog by remember { mutableStateOf(false) }
    var showNavigateToMensajesDialog by remember { mutableStateOf(false) }
    var showNavigateToConfiguracionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    // Estado para controlar el diálogo de notificación de ausencia
    var showNotificarAusenciaDialog by remember { mutableStateOf(false) }
    
    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
        // Cargar datos del familiar al inicio
        viewModel.cargarDatosFamiliar()
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Observamos si debemos navegar a la pantalla de welcome
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.FamiliarDashboard.route) { inclusive = true }
            }
        }
    }
    
    // Mostrar un mensaje cuando una solicitud es enviada con éxito
    LaunchedEffect(uiState.solicitudEnviada) {
        if (uiState.solicitudEnviada) {
            Toast.makeText(context, "Solicitud enviada con éxito", Toast.LENGTH_LONG).show()
            viewModel.resetSolicitudEnviada()
        }
    }
    
    // Diálogos de confirmación
    if (showNuevaSolicitudDialog) {
        NuevaSolicitudDialog(
            centros = uiState.centros,
            onDismiss = { showNuevaSolicitudDialog = false },
            onSubmit = { dni, centroId ->
                viewModel.crearSolicitudVinculacion(dni, centroId)
                showNuevaSolicitudDialog = false
            },
            isLoading = uiState.isLoadingSolicitud
        )
    }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    if (showNavigateToNotificacionesDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateToNotificacionesDialog = false },
            title = { Text("Notificaciones") },
            text = { Text("¿Quieres ver tus notificaciones pendientes?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showNavigateToNotificacionesDialog = false
                        navController.navigate(AppScreens.NotificacionesFamiliar.route)
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNavigateToNotificacionesDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    if (showNavigateToCalendarioDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateToCalendarioDialog = false },
            title = { Text("Calendario") },
            text = { Text("¿Quieres ver el calendario de eventos del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showNavigateToCalendarioDialog = false
                        navController.navigate(AppScreens.CalendarioFamilia.route)
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNavigateToCalendarioDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    if (showNavigateToMensajesDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateToMensajesDialog = false },
            title = { Text("Chat") },
            text = { Text("¿Quieres abrir el chat para comunicarte con el centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showNavigateToMensajesDialog = false
                        scope.launch {
                            try {
                                navController.navigate(AppScreens.ChatContacts.createRoute(AppScreens.ChatFamilia.route))
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar a ChatContacts: ${e.message}")
                                snackbarHostState.showSnackbar("Error al abrir chat: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNavigateToMensajesDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    if (showNavigateToConfiguracionDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateToConfiguracionDialog = false },
            title = { Text("Configuración") },
            text = { Text("¿Quieres ir a la configuración de tu perfil?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showNavigateToConfiguracionDialog = false
                        navController.navigate(AppScreens.EditProfile.route)
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNavigateToConfiguracionDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo para cambiar el tema
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Cambiar tema") },
            text = { Text("¿Quieres cambiar el tema de la aplicación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showThemeDialog = false
                        navController.navigate(AppScreens.CambiarTema.route)
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de notificación de ausencia
    val hijoSeleccionado = uiState.hijoSeleccionado
    if (showNotificarAusenciaDialog && hijoSeleccionado != null) {
        NotificarAusenciaDialog(
            alumno = hijoSeleccionado,
            onDismiss = { showNotificarAusenciaDialog = false },
            onSubmit = { alumnoId, alumnoNombre, fecha, motivo, duracion, claseId, claseCurso ->
                viewModel.notificarAusencia(
                    alumnoId = alumnoId,
                    alumnoNombre = alumnoNombre,
                    fechaAusencia = fecha,
                    motivo = motivo,
                    duracion = duracion,
                    claseId = claseId,
                    claseCurso = claseCurso
                )
                showNotificarAusenciaDialog = false
            },
            isLoading = uiState.isNotificandoAusencia
        )
    }
    
    // Notificación de éxito
    LaunchedEffect(uiState.ausenciaNotificada) {
        if (uiState.ausenciaNotificada) {
            val mensaje = uiState.mensajeExitoAusencia
            if (mensaje != null) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = mensaje,
                        duration = SnackbarDuration.Short
                    )
                    viewModel.resetAusenciaNotificada()
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Dashboard Familiar",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de mensajes sin badge
                    IconButton(
                        onClick = {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Timber.d("Intentando navegar a mensajes unificados directamente")
                                scope.launch {
                                    try {
                                        // Navegamos directamente a la bandeja de mensajes unificados en lugar del chat
                                        navController.navigate(AppScreens.UnifiedInbox.route)
                                        
                                        // Actualizamos los contadores de mensajes cuando volvamos
                                        viewModel.marcarMensajesComoLeidos()
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al navegar a mensajes: ${e.message}")
                                        snackbarHostState.showSnackbar("Error al abrir mensajes: ${e.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al realizar feedback háptico o navegar: ${e.message}")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Ver mensajes",
                            tint = Color.White
                        )
                    }
                    
                    // Icono de perfil
                    IconButton(onClick = { 
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Navegar directamente sin intentar obtener el DNI del familiar
                            navController.navigate(AppScreens.Perfil.createRoute(false))
                            Timber.d("Navegando a perfil del familiar")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Perfil: ${e.message}")
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al abrir perfil: ${e.message}")
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                    
                    // Cerrar sesión
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Mostrar un indicador de carga mientras se cargan los datos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FamiliarColor)
            }
        } else {
            // Mostrar el contenido principal con animación cuando los datos estén cargados
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
                ) {
                    // Header de bienvenida
                    item {
                        BienvenidaCard(
                            nombreFamiliar = uiState.familiar?.nombre ?: "Familiar",
                            totalHijos = uiState.hijos.size,
                            ultimaActualizacion = uiState.ultimaActualizacion ?: Date()
                        )
                    }
                    
                    // Selector de hijos en formato dropdown
                    item {
                        HijosDropdownSelector(
                            hijos = uiState.hijos,
                            hijoSeleccionado = uiState.hijoSeleccionado,
                            onHijoSelected = { viewModel.seleccionarHijo(it) },
                            onAddHijo = { showNuevaSolicitudDialog = true }
                        )
                    }
                    
                    // PRIMERA SECCIÓN: Registro diario de actividad (con prioridad)
                    if (uiState.hijoSeleccionado != null) {
                        item {
                            Text(
                                text = "Registro de Actividad",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = FamiliarColor,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            HorizontalDivider(thickness = 2.dp, color = FamiliarColor.copy(alpha = 0.2f))
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            uiState.hijoSeleccionado?.let { hijo ->
                                ResumenActividadCard(
                                    alumno = hijo,
                                    registrosActividad = uiState.registrosActividad,
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                        }
                    } else if (uiState.hijos.isEmpty()) {
                        // Mensaje cuando no hay hijos vinculados
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "No tienes hijos vinculados",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Usa el botón + para solicitar vincular un hijo a tu cuenta",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    
                    // SEGUNDA SECCIÓN: Accesos rápidos - Eliminar esta sección
                    item {
                        Text(
                            text = "Gestión y Acciones",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = FamiliarColor,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        HorizontalDivider(thickness = 2.dp, color = FamiliarColor.copy(alpha = 0.2f))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Calendario y Eventos
                    item {
                        OpcionDashboardCard(
                            titulo = "Calendario y Eventos",
                            descripcion = "Consulta el calendario escolar y los próximos eventos importantes",
                            icono = Icons.Default.CalendarMonth,
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(AppScreens.CalendarioFamilia.route)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al navegar a Calendario: ${e.message}")
                                }
                            }
                        )
                    }
                    
                    // Comunicación
                    item {
                        OpcionDashboardCard(
                            titulo = "Comunicación",
                            descripcion = "Mensajes, notificaciones y comunicados del centro",
                            icono = Icons.Default.Email,
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(AppScreens.UnifiedInbox.route)
                                    // Actualizamos los contadores de mensajes cuando se navegue a Comunicación
                                    viewModel.marcarMensajesComoLeidos()
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al navegar a Comunicación: ${e.message}")
                                }
                            }
                        )
                    }
                    
                    // Notificar Ausencia
                    item {
                        OpcionDashboardCard(
                            titulo = "Notificar Ausencia",
                            descripcion = "Informa al centro que tu hijo/a no asistirá",
                            icono = Icons.Default.Sick,
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (uiState.hijoSeleccionado != null) {
                                        showNotificarAusenciaDialog = true
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Por favor, selecciona un hijo primero")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al abrir diálogo de ausencia: ${e.message}")
                                }
                            }
                        )
                    }
                    
                    // La actividad de Actividades Preescolares ha sido eliminada
                    
                    // Añadir card de gestión de notificaciones
                    item {
                        Text(
                            text = "Notificaciones y Preferencias",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = FamiliarColor,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        HorizontalDivider(thickness = 2.dp, color = FamiliarColor.copy(alpha = 0.2f))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Card de Notificaciones (una por línea, ocupando todo el ancho)
                    item {
                        OpcionDashboardCard(
                            titulo = "Gestión de Notificaciones",
                            descripcion = "Configura qué notificaciones deseas recibir y cómo",
                            icono = Icons.Default.Notifications,
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(AppScreens.Notificaciones.route)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al navegar a Gestión de Notificaciones: ${e.message}")
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Card de Tema
                        OpcionDashboardCard(
                            titulo = "Tema de la Aplicación",
                            descripcion = "Personaliza el aspecto visual de la aplicación",
                            icono = Icons.Default.Palette,
                            onClick = { showThemeDialog = true }
                        )
                    }
                    
                    // Espaciador final
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * Selector de hijos en formato dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijosDropdownSelector(
    hijos: List<Alumno>,
    hijoSeleccionado: Alumno?,
    onHijoSelected: (Alumno) -> Unit,
    onAddHijo: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Selecciona un hijo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FamiliarColor
                )
                
                // Nuevo botón para añadir hijo
                Button(
                    onClick = onAddHijo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FamiliarColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Añadir hijo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir hijo")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (hijos.isEmpty()) {
                Text(
                    text = "No tienes hijos vinculados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = hijoSeleccionado?.nombre ?: "Selecciona un hijo",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = FamiliarColor,
                            unfocusedBorderColor = FamiliarColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        hijos.forEach { hijo ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Avatar circular con inicial
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(FamiliarColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = hijo.nombre.first().toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Text(hijo.nombre)
                                    }
                                },
                                onClick = {
                                    onHijoSelected(hijo)
                                    expanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta para una acción rápida en el dashboard
 */
@Composable
fun AccionRapidaCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    badgeCount: Int = 0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con badge si es necesario
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge {
                            Text(
                                text = badgeCount.toString(),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FamiliarColor.copy(alpha = if (enabled) 1f else 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ir a $titulo",
                tint = if (enabled) 
                    FamiliarColor 
                else 
                    FamiliarColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FamiliarColor,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Tarjeta de bienvenida personalizada para el dashboard con información del familiar
 * y resumen de hijos vinculados.
 *
 * @param nombreFamiliar Nombre del familiar
 * @param totalHijos Número total de hijos vinculados
 * @param ultimaActualizacion Fecha de la última actualización de datos
 */
@Composable
fun BienvenidaCard(
    nombreFamiliar: String,
    totalHijos: Int,
    ultimaActualizacion: Date
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información de bienvenida
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "¡Hola, $nombreFamiliar!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (totalHijos > 0) "$totalHijos ${if (totalHijos == 1) "hijo" else "hijos"} vinculados" else "Aún no tienes hijos vinculados",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Centro Infantil UmeEgunero",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Última actualización: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(ultimaActualizacion)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FamiliarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Nuevo footer compacto para mostrar las solicitudes pendientes
 */
@Composable
fun SolicitudesPendientesFooter(
    solicitudes: List<SolicitudPendienteUI>
) {
    val pendientes = solicitudes.count { it.estado == EstadoSolicitud.PENDIENTE }
    val aprobadas = solicitudes.count { it.estado == EstadoSolicitud.APROBADA }
    val rechazadas = solicitudes.count { it.estado == EstadoSolicitud.RECHAZADA }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp), // Mayor altura mínima para evitar que se corte
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Bordes más redondeados
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Mayor elevación
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Más padding vertical
            verticalArrangement = Arrangement.spacedBy(8.dp) // Más espacio entre elementos
        ) {
            // Título con icono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Solicitudes pendientes",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp) // Icono más grande
                )
                
                Spacer(modifier = Modifier.width(12.dp)) // Más espacio
                
                Text(
                    text = "Solicitudes de vinculación",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ), // Texto más destacado
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Contadores de estado en fila con más espacio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp), // Más espacio superior
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (pendientes > 0) {
                    StatusBadge(
                        count = pendientes,
                        label = "pendientes",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                if (aprobadas > 0) {
                    StatusBadge(
                        count = aprobadas,
                        label = "aprobadas",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (rechazadas > 0) {
                    StatusBadge(
                        count = rechazadas,
                        label = "rechazadas",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (pendientes == 0 && aprobadas == 0 && rechazadas == 0) {
                    Text(
                        text = "No hay solicitudes pendientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Pequeña insignia para mostrar conteo de solicitudes por estado
 */
@Composable
fun StatusBadge(
    count: Int,
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)) // Bordes más redondeados
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp) // Más padding
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.bodyLarge, // Texto más grande
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Spacer(modifier = Modifier.width(6.dp)) // Más espacio
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium, // Texto más grande
                color = color
            )
        }
    }
}

/**
 * Tarjeta de resumen de actividad de un alumno
 * 
 * @param alumno Alumno del que se muestra la actividad
 * @param registrosActividad Lista de registros de actividad del alumno
 * @param viewModel ViewModel para gestionar la navegación
 * @param navController Controlador de navegación para navegar a otras pantallas
 */
@Composable
fun ResumenActividadCard(
    alumno: Alumno,
    registrosActividad: List<RegistroActividad>,
    viewModel: FamiliarDashboardViewModel,
    navController: NavController
) {
    val ultimoRegistro = registrosActividad.maxByOrNull { it.fecha }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (ultimoRegistro != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fecha = ultimoRegistro.fecha
                    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(fecha.seconds * 1000))
                    
                    Text(
                        text = "Último registro: $fechaFormateada",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Button(
                        onClick = { 
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                Timber.d("Navegando a DetalleRegistroScreen con registroId: ${ultimoRegistro.id}")
                                
                                // Navegar directamente a DetalleRegistroScreen con el ID del registro
                                navController.navigate(AppScreens.DetalleRegistro.createRoute(ultimoRegistro.id))
                                
                                // Mostrar Toast como indicador de que se está procesando
                                Toast.makeText(context, "Abriendo detalle del registro...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar al detalle del registro: ${e.message}")
                                Toast.makeText(context, "Error al abrir el detalle: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FamiliarColor
                        )
                    ) {
                        Text("Ver detalle")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar información básica del registro en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActividadInfoItem(
                        icon = Icons.Default.Restaurant,
                        title = "Alimentación",
                        value = when {
                            ultimoRegistro.comidas.primerPlato.estadoComida == EstadoComida.COMPLETO -> "Completa"
                            ultimoRegistro.comidas.primerPlato.estadoComida == EstadoComida.PARCIAL -> "Parcial"
                            else -> "No servido"
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bedtime,
                        title = "Siesta",
                        value = if (ultimoRegistro.haSiestaSiNo) 
                            "Sí" 
                        else 
                            "No"
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bathroom,
                        title = "Necesidades",
                        value = if (ultimoRegistro.haHechoCaca) "Sí" else "No"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Observaciones
                if (ultimoRegistro.observacionesGenerales.isNotBlank()) {
                    Text(
                        text = "Observaciones:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = ultimoRegistro.observacionesGenerales,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No hay observaciones para este registro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                // Estado mejorado cuando no hay registros
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Sin registros",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay registros de actividad disponibles",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Los registros se actualizarán cuando el centro los añada",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Elemento de información individual para la actividad
 * 
 * @param icon Icono representativo
 * @param title Título del elemento
 * @param value Valor a mostrar
 */
@Composable
fun ActividadInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = FamiliarColor,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Renderiza una tarjeta para una opción del dashboard
 */
@Composable
private fun OpcionDashboardCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono en un círculo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FamiliarColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = FamiliarColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Flecha indicadora
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = FamiliarColor
            )
        }
    }
}

// La función HistorialCard ha sido eliminada porque ahora se usa OpcionDashboardCard

private fun showErrorMessage(snackbarHostState: SnackbarHostState, scope: CoroutineScope, mensaje: String) {
    scope.launch {
        snackbarHostState.showSnackbar(
            message = mensaje,
            duration = SnackbarDuration.Short
        )
    }
}

@Composable
@Preview(showBackground = true)
fun FamiliaDashboardScreenPreview() {
    UmeEguneroTheme {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun FamiliaDashboardScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
}

