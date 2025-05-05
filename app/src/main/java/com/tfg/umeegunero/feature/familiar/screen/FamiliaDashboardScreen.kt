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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.tfg.umeegunero.data.model.NivelConsumo
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
 * @version 5.0
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
    var showNavigateToHistorialDialog by remember { mutableStateOf(false) }
    
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
    
    // Diálogo para crear una nueva solicitud de vinculación
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
    
    // Diálogo de confirmación para cerrar sesión
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
    
    // Diálogo de confirmación para ir a notificaciones
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
                        navController.navigate(AppScreens.NotificacionesFamilia.route)
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
    
    // Diálogo de confirmación para ir a calendario
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
    
    // Diálogo de confirmación para ir a mensajes
    if (showNavigateToMensajesDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateToMensajesDialog = false },
            title = { Text("Mensajes Unificados") },
            text = { Text("¿Quieres acceder a tu bandeja de mensajes unificada?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showNavigateToMensajesDialog = false
                        navController.navigate(AppScreens.UnifiedInbox.route)
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
    
    // Diálogo de confirmación para ir a configuración
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
                        navController.navigate(AppScreens.Perfil.route)
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
    
    // Diálogo de confirmación para ir a historial
    if (showNavigateToHistorialDialog) {
        AlertDialog(
            onDismissRequest = { showNavigateToHistorialDialog = false },
            title = { Text("Historial") },
            text = { 
                if (uiState.hijoSeleccionado != null) {
                    Text("¿Quieres ver el historial de ${uiState.hijoSeleccionado!!.nombre}?")
                } else {
                    Text("Necesitas seleccionar un hijo primero")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showNavigateToHistorialDialog = false
                        if (uiState.hijoSeleccionado != null) {
                            val alumnoId = uiState.hijoSeleccionado!!.dni
                            val alumnoNombre = uiState.hijoSeleccionado!!.nombre
                            navController.navigate(
                                AppScreens.ConsultaRegistroDiario.createRoute(
                                    alumnoId = alumnoId,
                                    alumnoNombre = alumnoNombre
                                )
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Selecciona un hijo primero")
                            }
                        }
                    },
                    enabled = uiState.hijoSeleccionado != null
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNavigateToHistorialDialog = false }) {
                    Text("No")
                }
            }
        )
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
                    // Botón de mensajes con badge para notificaciones
                    BadgedBox(
                        badge = {
                            if (uiState.totalMensajesNoLeidos > 0) {
                                Badge { Text(uiState.totalMensajesNoLeidos.toString()) }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showNavigateToMensajesDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Mensajes"
                            )
                        }
                    }
                    // Icono de perfil
                    IconButton(onClick = { showNavigateToConfiguracionDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al realizar feedback háptico")
                    }
                    showNuevaSolicitudDialog = true 
                },
                containerColor = FamiliarColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Solicitar vinculación de nuevo hijo"
                )
            }
        }
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
                val cards = listOf(
                    CategoriaCardData("Mensajes", "Mensajería unificada y comunicados del centro", Icons.AutoMirrored.Filled.Chat, MaterialTheme.colorScheme.primary, onClick = { 
                        navController.navigate(AppScreens.UnifiedInbox.route)
                    }),
                    CategoriaCardData("Calendario", "Revisa eventos y fechas importantes", Icons.Default.CalendarMonth, MaterialTheme.colorScheme.tertiary, onClick = { showNavigateToCalendarioDialog = true }),
                    CategoriaCardData("Historial", "Consulta el registro diario de tu hijo/a", Icons.AutoMirrored.Filled.Assignment, MaterialTheme.colorScheme.secondary, onClick = { showNavigateToHistorialDialog = true }),
                    CategoriaCardData("Notificaciones", "Revisa todas las notificaciones pendientes", Icons.Default.Notifications, MaterialTheme.colorScheme.primary, onClick = { showNavigateToNotificacionesDialog = true }),
                    CategoriaCardData("Configuración", "Ajusta preferencias de tu perfil y notificaciones", Icons.Default.Settings, MaterialTheme.colorScheme.tertiary, onClick = { showNavigateToConfiguracionDialog = true })
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
                ) {
                    // Header de bienvenida
                    item(span = { GridItemSpan(2) }) {
                        BienvenidaCard(
                            nombreFamiliar = uiState.familiar?.nombre ?: "Familiar",
                            totalHijos = uiState.hijos.size
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Selector de hijos
                    item(span = { GridItemSpan(2) }) {
                        HijosMultiSelector(
                            hijos = uiState.hijos,
                            hijoSeleccionado = uiState.hijoSeleccionado,
                            onHijoSelected = { viewModel.seleccionarHijo(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Sección de solicitudes pendientes
                    if (uiState.solicitudesPendientes.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = "Solicitudes pendientes",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = FamiliarColor,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            SolicitudesPendientesCard(
                                solicitudes = uiState.solicitudesPendientes
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    // Divider y sección de accesos rápidos
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Accesos rápidos",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = FamiliarColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        HorizontalDivider(thickness = 2.dp, color = FamiliarColor.copy(alpha = 0.2f))
                    }
                    
                    items(cards.size) { index ->
                        val card = cards[index]
                        CategoriaCard(
                            titulo = card.titulo,
                            descripcion = card.descripcion,
                            icono = card.icono,
                            color = FamiliarColor,
                            iconTint = card.iconTint,
                            border = true,
                            onClick = card.onClick,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    
                    // Divider y sección de resumen de actividad (solo si hay un hijo seleccionado)
                    if (uiState.hijoSeleccionado != null) {
                        item(span = { GridItemSpan(2) }) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Resumen de Actividad",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = FamiliarColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            HorizontalDivider(thickness = 2.dp, color = FamiliarColor.copy(alpha = 0.2f))
                        }
                        
                        item(span = { GridItemSpan(2) }) {
                            ResumenActividadCard(
                                alumno = uiState.hijoSeleccionado!!,
                                registrosActividad = uiState.registrosActividad,
                                onVerDetalles = { registroId ->
                                    try {
                                        navController.navigate(
                                            AppScreens.DetalleRegistro.createRoute(registroId)
                                        )
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("No se pudo navegar a los detalles")
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Divider y sección de notificaciones
                    item(span = { GridItemSpan(2) }) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = FamiliarColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        HorizontalDivider(thickness = 2.dp, color = FamiliarColor.copy(alpha = 0.2f))
                    }
                    
                    item(span = { GridItemSpan(2) }) {
                        NotificacionesCard(
                            totalNoLeidas = uiState.registrosSinLeer,
                            onVerTodas = { navController.navigate(AppScreens.NotificacionesFamilia.route) }
                        )
                    }
                    
                    // Espaciador final
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
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
 */
@Composable
fun BienvenidaCard(
    nombreFamiliar: String,
    totalHijos: Int
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
                    text = "Última actualización: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
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
 * Componente que permite seleccionar entre múltiples hijos vinculados a la cuenta familiar.
 * 
 * @param hijos Lista de alumnos vinculados al familiar
 * @param hijoSeleccionado Alumno actualmente seleccionado
 * @param onHijoSelected Callback cuando se selecciona un hijo
 */
@Composable
fun HijosMultiSelector(
    hijos: List<Alumno>,
    hijoSeleccionado: Alumno?,
    onHijoSelected: (Alumno) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    if (hijos.isEmpty()) {
        // Si no hay hijos, mostrar un mensaje informativo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Usa el botón + para solicitar vincular un hijo a tu cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    } else {
        // Vista normal con selección de hijos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Mis hijos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(hijos) { hijo ->
                        val isSelected = hijo.dni == hijoSeleccionado?.dni
                        
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(140.dp)
                                .clickable { 
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al realizar feedback háptico")
                                    }
                                    onHijoSelected(hijo) 
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    FamiliarColor.copy(alpha = 0.2f) 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) 
                                BorderStroke(2.dp, FamiliarColor)
                            else 
                                null
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Avatar/Inicial del alumno
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) FamiliarColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hijo.nombre.first().toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = hijo.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                
                                if (hijo.fechaNacimiento != null) {
                                    Text(
                                        text = "${DateUtils.calcularEdad(hijo.fechaNacimiento!!)} años",
                                        style = MaterialTheme.typography.bodySmall,
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
}

/**
 * Tarjeta que muestra las solicitudes de vinculación pendientes
 * 
 * @param solicitudes Lista de solicitudes pendientes
 */
@Composable
fun SolicitudesPendientesCard(
    solicitudes: List<SolicitudPendienteUI>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(solicitudes) { solicitud ->
                    SolicitudItem(solicitud = solicitud)
                }
            }
        }
    }
}

/**
 * Elemento individual de una solicitud de vinculación
 * 
 * @param solicitud Datos de la solicitud
 */
@Composable
fun SolicitudItem(
    solicitud: SolicitudPendienteUI
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (solicitud.estado) {
                            EstadoSolicitud.PENDIENTE -> MaterialTheme.colorScheme.tertiary
                            EstadoSolicitud.APROBADA -> MaterialTheme.colorScheme.primary
                            EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (solicitud.estado) {
                        EstadoSolicitud.PENDIENTE -> Icons.Default.Pending
                        EstadoSolicitud.APROBADA -> Icons.Default.Check
                        EstadoSolicitud.RECHAZADA -> Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = solicitud.alumnoNombre ?: "DNI: ${solicitud.alumnoDni}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Centro: ${solicitud.centroNombre ?: solicitud.centroId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Estado: ${
                        when (solicitud.estado) {
                            EstadoSolicitud.PENDIENTE -> "Pendiente"
                            EstadoSolicitud.APROBADA -> "Aprobada"
                            EstadoSolicitud.RECHAZADA -> "Rechazada"
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (solicitud.estado) {
                        EstadoSolicitud.PENDIENTE -> MaterialTheme.colorScheme.tertiary
                        EstadoSolicitud.APROBADA -> MaterialTheme.colorScheme.primary
                        EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error
                    }
                )
            }
            
            Text(
                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(solicitud.fechaSolicitud),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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
                            if (alumnoDni.isNotBlank() && validateDni(alumnoDni) && centroSeleccionado != null) {
                                onSubmit(alumnoDni, centroSeleccionado!!.id)
                            }
                        },
                        enabled = alumnoDni.isNotBlank() && dniError == null && centroSeleccionado != null && !isLoading,
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
 * Tarjeta de resumen de actividad de un alumno
 * 
 * @param alumno Alumno del que se muestra la actividad
 * @param registrosActividad Lista de registros de actividad del alumno
 * @param onVerDetalles Callback cuando se quiere ver el detalle de un registro
 */
@Composable
fun ResumenActividadCard(
    alumno: Alumno,
    registrosActividad: List<RegistroActividad>,
    onVerDetalles: (String) -> Unit
) {
    val ultimoRegistro = registrosActividad.maxByOrNull { it.fecha }
    val haptic = LocalHapticFeedback.current
    
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ultimoRegistro != null) {
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
                            } catch (e: Exception) {
                                Timber.e(e, "Error al realizar feedback háptico")
                            }
                            onVerDetalles(ultimoRegistro.id)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FamiliarColor
                        )
                    ) {
                        Text("Ver detalle")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (ultimoRegistro != null) {
                // Mostrar información básica del registro en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActividadInfoItem(
                        icon = Icons.Default.Restaurant,
                        title = "Alimentación",
                        value = when {
                            ultimoRegistro.comidas.primerPlato.nivelConsumo == NivelConsumo.valueOf("COMPLETO") -> "Completa"
                            ultimoRegistro.comidas.primerPlato.nivelConsumo == NivelConsumo.valueOf("PARCIAL") -> "Parcial"
                            else -> "No servido"
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bedtime,
                        title = "Siesta",
                        value = if (ultimoRegistro.siesta?.duracion ?: 0 > 0) 
                            "${ultimoRegistro.siesta?.duracion} min" 
                        else 
                            "No"
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bathroom,
                        title = "Necesidades",
                        value = if (ultimoRegistro.necesidadesFisiologicas.caca) "Sí" else "No"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Observaciones
                if (!ultimoRegistro.observaciones.isNullOrBlank()) {
                    Text(
                        text = "Observaciones:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = ultimoRegistro.observaciones ?: "",
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
                // No hay registros disponibles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros de actividad disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
 * Tarjeta de notificaciones para mostrar resumen de notificaciones pendientes
 * 
 * @param totalNoLeidas Total de notificaciones o registros sin leer
 * @param onVerTodas Callback cuando se quiere ver todas las notificaciones
 */
@Composable
fun NotificacionesCard(
    totalNoLeidas: Int,
    onVerTodas: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalNoLeidas > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = totalNoLeidas.toString(),
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = if (totalNoLeidas > 0) 
                            "Tienes $totalNoLeidas ${if (totalNoLeidas == 1) "registro" else "registros"} sin leer"
                        else
                            "No hay notificaciones pendientes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (totalNoLeidas > 0) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al realizar feedback háptico")
                    }
                    onVerTodas() 
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FamiliarColor
                )
            ) {
                Text("Ver todas")
            }
        }
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

