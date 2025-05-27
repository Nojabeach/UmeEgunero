package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.SpeakerNotes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardUiState
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.PaddingValues
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.ui.components.CategoriaCardData
import timber.log.Timber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.tfg.umeegunero.util.performHapticFeedbackSafely
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox

/**
 * Componente para mostrar una tarjeta en el dashboard con título, descripción e icono
 */
@Composable
private fun CardDashboard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp, max = 200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono en la parte superior
            Box(
                modifier = Modifier
                    .background(color = color, shape = CircleShape)
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Espacio flexible
            Spacer(modifier = Modifier.weight(1f))
            
            // Título y descripción en la parte inferior
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Pantalla principal del profesor, contiene un dashboard con acceso a todas las funcionalidades.
 *
 * @param viewModel ViewModel que provee datos y funcionalidad a la pantalla
 * @param navController Controlador de navegación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDashboardScreen(
    viewModel: ProfesorDashboardViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    // --- Estado y Efectos ---
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showContent by remember { mutableStateOf(false) } // Para animación de entrada
    val haptic = LocalHapticFeedback.current
    
    // Estados para diálogos de confirmación
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRegistroDiarioDialog by remember { mutableStateOf(false) }
    var showChatFamiliasDialog by remember { mutableStateOf(false) }
    var showComunicadosDialog by remember { mutableStateOf(false) }
    var showMisAlumnosDialog by remember { mutableStateOf(false) }
    var showCalendarioDialog by remember { mutableStateOf(false) }
    var showPerfilDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
        // Podrías llamar a una función explícita del ViewModel para cargar/refrescar si es necesario
        // viewModel.cargarDatosDashboard()
    }

    // Efecto para mostrar mensajes de error en el snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }

    // Efecto para manejar la navegación al cerrar sesión
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            try {
                // Navega a la pantalla de bienvenida, limpiando el backstack hasta el dashboard
                navController.navigate(AppScreens.Welcome.route) {
                    popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
                    launchSingleTop = true
                }
                // Notifica al ViewModel que la navegación se ha realizado
                viewModel.onNavigationDone()
                Timber.d("Navegación a Welcome completada correctamente")
            } catch (e: Exception) {
                Timber.e(e, "Error en la navegación al cerrar sesión")
                // Intento alternativo de navegación en caso de error
                try {
                    navController.navigate(AppScreens.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                } catch (e2: Exception) {
                    Timber.e(e2, "Error en el segundo intento de navegación")
                }
            }
        }
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
    
    // Diálogo de confirmación para ir a Registro Diario
    if (showRegistroDiarioDialog) {
        AlertDialog(
            onDismissRequest = { showRegistroDiarioDialog = false },
            title = { Text("Registro Diario") },
            text = { Text("¿Quieres realizar el registro diario de tus alumnos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegistroDiarioDialog = false
                        // Usar profesorId del estado para pasarlo como parámetro a la pantalla
                        val profesorId = uiState.profesor?.dni ?: return@TextButton
                        val nombreProfesor = uiState.profesor?.nombre ?: return@TextButton
                        val nombreClase = uiState.claseInfo ?: return@TextButton

                        try {
                            navController.navigate(AppScreens.RegistroDiario.createRoute(
                                alumnoId = profesorId,
                                claseId = "", // TODO: Proporcionar el id real de la clase si es necesario
                                profesorId = profesorId,
                                alumnoNombre = nombreProfesor,
                                claseNombre = nombreClase
                            ))
                            Timber.d("Navegando a Registro Diario con profesorId: $profesorId")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Registro Diario")
                        }
                    },
                    enabled = !uiState.esFestivoHoy
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegistroDiarioDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Chat Familias
    if (showChatFamiliasDialog) {
        AlertDialog(
            onDismissRequest = { showChatFamiliasDialog = false },
            title = { Text("Chat con Familias") },
            text = { Text("¿Qué acción quieres realizar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChatFamiliasDialog = false
                        try {
                            navController.navigate(AppScreens.UnifiedInbox.route)
                            Timber.d("Navegando a Chat con Familias")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Chat con Familias")
                        }
                    }
                ) {
                    Text("Ver mensajes")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showChatFamiliasDialog = false
                    try {
                        // Pasamos solo el nombre de la ruta base, no la ruta completa con parámetros
                        val chatRouteName = AppScreens.ChatProfesor.route.split("/")[0] // Extraer solo la parte "chat_profesor"
                        navController.navigate(AppScreens.ChatContacts.createRoute(chatRouteName = chatRouteName))
                        Timber.d("Navegación a Contactos de Chat exitosa con ruta: $chatRouteName")
                    } catch (e: Exception) {
                        Timber.e(e, "Error al navegar a Contactos de Chat: ${e.message}")
                        viewModel.showSnackbarMessage("Error al navegar a Nuevo Chat")
                    }
                }) {
                    Text("Nuevo chat")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Comunicados
    if (showComunicadosDialog) {
        AlertDialog(
            onDismissRequest = { showComunicadosDialog = false },
            title = { Text("Comunicados") },
            text = { Text("¿Quieres ver y gestionar los comunicados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showComunicadosDialog = false
                        try {
                            navController.navigate(AppScreens.UnifiedInbox.route)
                            Timber.d("Navegando a Comunicados y Circulares")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Comunicados y Circulares")
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComunicadosDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Mis Alumnos
    if (showMisAlumnosDialog) {
        AlertDialog(
            onDismissRequest = { showMisAlumnosDialog = false },
            title = { Text("Mis Alumnos") },
            text = { Text("¿Quieres ver la información de tus alumnos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMisAlumnosDialog = false
                        try {
                            navController.navigate(AppScreens.MisAlumnosProfesor.route)
                            Timber.d("Navegando a Mis Alumnos")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Mis Alumnos")
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMisAlumnosDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Calendario
    if (showCalendarioDialog) {
        AlertDialog(
            onDismissRequest = { showCalendarioDialog = false },
            title = { Text("Calendario") },
            text = { Text("¿Quieres ver el calendario de eventos y festivos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCalendarioDialog = false
                        try {
                            navController.navigate(AppScreens.ProfesorCalendario.route)
                            Timber.d("Navegando a Calendario")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Calendario")
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarioDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Perfil
    if (showPerfilDialog) {
        AlertDialog(
            onDismissRequest = { showPerfilDialog = false },
            title = { Text("Perfil") },
            text = { Text("¿Quieres ir a tu perfil de usuario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPerfilDialog = false
                        navController.navigate(AppScreens.Perfil.route)
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPerfilDialog = false }) {
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

    // --- UI Principal (Scaffold) ---
    Scaffold(
        topBar = {
            ProfesorDashboardTopBar(
                onProfileClick = { showPerfilDialog = true },
                onLogoutClick = { showLogoutDialog = true },
                viewModel = viewModel,
                navController = navController
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface // Color de fondo base
    ) { paddingValues ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    slideInVertically(initialOffsetY = { it / 4 }), // Animación más sutil
            exit = fadeOut()
        ) {
            // Contenido principal dentro de la animación
            ProfesorDashboardContent(
                paddingValues = paddingValues,
                uiState = uiState,
                viewModel = viewModel,
                navController = navController,
                onChatFamiliasClick = { showChatFamiliasDialog = true },
                onComunicadosClick = { showComunicadosDialog = true },
                onMisAlumnosClick = { showMisAlumnosDialog = true },
                onCalendarioClick = { showCalendarioDialog = true },
                onRegistroDiarioClick = { showRegistroDiarioDialog = true },
                onThemeClick = { showThemeDialog = true },
                haptic = haptic
            )
        }
    }
}

/**
 * Barra superior personalizada para el Dashboard del Profesor
 *
 * @param onProfileClick Lambda que se ejecuta al pulsar el icono de perfil.
 * @param onLogoutClick Lambda que se ejecuta al pulsar el icono de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfesorDashboardTopBar(
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: ProfesorDashboardViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val haptic = LocalHapticFeedback.current
    // Observar el contador de mensajes no leídos
    val unreadMessageCount by viewModel.unreadMessageCount.collectAsState()
    
    CenterAlignedTopAppBar(
        title = { Text("Dashboard Profesor") },
        actions = {
            // Icono de mensajes/chat con badge si hay mensajes no leídos
            BadgedBox(
                badge = {
                    if (unreadMessageCount > 0) {
                        Badge { 
                            Text(
                                text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedbackSafely(HapticFeedbackType.LongPress)
                        navController.navigate(AppScreens.UnifiedInbox.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Mensajes"
                    )
                }
            }
            
            // Icono de perfil
            IconButton(onClick = { 
                haptic.performHapticFeedbackSafely(HapticFeedbackType.LongPress)
                onProfileClick() 
            }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil"
                )
            }
            
            // Icono de cerrar sesión
            IconButton(onClick = {
                haptic.performHapticFeedbackSafely(HapticFeedbackType.LongPress)
                onLogoutClick()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = ProfesorColor,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

/**
 * Contenido principal del Dashboard de Profesor, organizado en una cuadrícula.
 *
 * Muestra la tarjeta de bienvenida, secciones de acciones categorizadas y
 * un resumen de alumnos pendientes si los hubiera. Las acciones diarias se
 * deshabilitan si el día actual es festivo.
 *
 * @param paddingValues PaddingValues para aplicar padding al contenido principal.
 * @param uiState Estado actual de la UI, proporcionado por el ViewModel.
 * @param viewModel ViewModel que gestiona el estado y la lógica de negocio del dashboard.
 * @param navController Controlador de navegación para las acciones de las tarjetas.
 * @param onChatFamiliasClick Callback para ir a Chat con Familias
 * @param onComunicadosClick Callback para ir a Comunicados
 * @param onMisAlumnosClick Callback para ir a Mis Alumnos
 * @param onCalendarioClick Callback para ir a Calendario 
 * @param onRegistroDiarioClick Callback para ir a Registro Diario
 * @param onThemeClick Callback para ir a Cambiar Tema
 * @param haptic HapticFeedback para proporcionar feedback táctil
 */
@Composable
fun ProfesorDashboardContent(
    paddingValues: PaddingValues,
    uiState: ProfesorDashboardUiState,
    viewModel: ProfesorDashboardViewModel,
    navController: NavController,
    onChatFamiliasClick: () -> Unit,
    onComunicadosClick: () -> Unit,
    onMisAlumnosClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onRegistroDiarioClick: () -> Unit,
    onThemeClick: () -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val hoyEsFestivo = uiState.esFestivoHoy

    // --- Definición de las tarjetas de acciones (usar AppScreens correctos) ---
    val accionesDiariasCards = listOf(
        CategoriaCardData(
            titulo = "Registro Diario",
            descripcion = "Registra asistencia, comidas, siestas y otras actividades",
            icono = Icons.AutoMirrored.Filled.ListAlt,
            color = ProfesorColor,
            onClick = { 
                haptic.performHapticFeedbackSafely()
                onRegistroDiarioClick()
            }
        )
    )
    val comunicacionCards = listOf(
        CategoriaCardData(
            titulo = "Mensajes",
            descripcion = "Comunicación unificada con padres y centro",
            icono = Icons.AutoMirrored.Filled.Chat,
            color = ProfesorColor,
            onClick = { 
                haptic.performHapticFeedbackSafely()
                navController.navigate(AppScreens.UnifiedInbox.route)
            }
        ),
        CategoriaCardData(
            titulo = "Nuevo Chat",
            descripcion = "Iniciar conversación con familiares o profesores",
            icono = Icons.AutoMirrored.Filled.SpeakerNotes,
            color = ProfesorColor,
            onClick = { 
                haptic.performHapticFeedbackSafely()
                try {
                    // Pasamos solo el nombre de la ruta base, no la ruta completa con parámetros
                    val chatRouteName = AppScreens.ChatProfesor.route.split("/")[0] // Extraer solo la parte "chat_profesor"
                    navController.navigate(AppScreens.ChatContacts.createRoute(chatRouteName = chatRouteName))
                    Timber.d("Navegación a Contactos de Chat exitosa con ruta: $chatRouteName")
                } catch (e: Exception) {
                    Timber.e(e, "Error al navegar a Contactos de Chat: ${e.message}")
                    viewModel.showSnackbarMessage("Error al navegar a Nuevo Chat")
                }
            }
        )
    )
    val gestionPlanificacionCards = listOf(
        CategoriaCardData(
            titulo = "Mis Alumnos",
            descripcion = "Consulta fichas e información",
            icono = Icons.Default.ChildCare,
            color = ProfesorColor,
            onClick = { 
                haptic.performHapticFeedbackSafely()
                onMisAlumnosClick()
            }
        ),
        CategoriaCardData(
            titulo = "Calendario",
            descripcion = "Eventos escolares y festivos",
            icono = Icons.Default.CalendarMonth,
            color = ProfesorColor,
            onClick = { 
                haptic.performHapticFeedbackSafely()
                onCalendarioClick()
            }
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // --- Bienvenida ---
        item {
            BienvenidaProfesorCard(
                nombreProfesor = uiState.profesorNombre,
                infoClase = uiState.claseInfo,
                esFestivo = hoyEsFestivo
            )
        }

        // --- Sección de acciones diarias - Solo si no es festivo
        if (!hoyEsFestivo) {
            item { SeccionTitulo("Acciones Diarias") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tarjeta para registro diario de alumnos
                    CardDashboard(
                        titulo = "Registro diario",
                        descripcion = "Registra la actividad y necesidades diarias de tus alumnos",
                        icono = Icons.AutoMirrored.Filled.ListAlt,
                        color = ProfesorColor,
                        onClick = {
                            haptic.performHapticFeedbackSafely()
                            try {
                                // Primero navegamos a la pantalla de listado y envolvemos en un try-catch para manejar errores
                                navController.navigate(AppScreens.ListadoPreRegistroDiario.route)
                                Timber.d("Navegación a Listado Pre-Registro Diario exitosa")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar a Listado Pre-Registro Diario")
                                viewModel.showSnackbarMessage("Error al navegar a Registro Diario")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Tarjeta para histórico de registros diarios
                    CardDashboard(
                        titulo = "Histórico de registros",
                        descripcion = "Consulta registros anteriores de actividad de tus alumnos",
                        icono = Icons.Default.History,
                        color = ProfesorColor,
                        onClick = {
                            haptic.performHapticFeedbackSafely()
                            try {
                                // Navegación directa envuelta en try-catch
                                navController.navigate(AppScreens.HistoricoRegistroDiario.route)
                                Timber.d("Navegación a Histórico Registro Diario exitosa")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar a Histórico Registro Diario")
                                viewModel.showSnackbarMessage("Error al navegar al historial de registros")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Comunicación ---
        item { SeccionTitulo("Comunicación") }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Solo 2 elementos, una fila es suficiente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (card in comunicacionCards) {
                        Box(modifier = Modifier.weight(1f)) {
                            CategoriaCard(
                                titulo = card.titulo,
                                descripcion = card.descripcion,
                                icono = card.icono,
                                color = card.color,
                                iconTint = card.iconTint,
                                border = true,
                                onClick = {
                                    haptic.performHapticFeedbackSafely()
                                    try {
                                        // Esta navegación debe ir al inbox unificado
                                        if (card.titulo == "Mensajes") {
                                            // Navegando explícitamente al Inbox y no usando el callback
                                            navController.navigate(AppScreens.UnifiedInbox.route)
                                            Timber.d("Navegación a Inbox Unificado exitosa")
                                        } else if (card.titulo == "Nuevo Chat") {
                                            // Navegación a contactos para nuevo chat
                                            try {
                                                // Pasamos solo el nombre de la ruta base, no la ruta completa con parámetros
                                                val chatRouteName = AppScreens.ChatProfesor.route.split("/")[0] // Extraer solo la parte "chat_profesor"
                                                navController.navigate(AppScreens.ChatContacts.createRoute(chatRouteName = chatRouteName))
                                                Timber.d("Navegación a Contactos de Chat exitosa con ruta: $chatRouteName")
                                            } catch (e: Exception) {
                                                Timber.e(e, "Error al navegar a Contactos de Chat: ${e.message}")
                                                viewModel.showSnackbarMessage("Error al navegar a Nuevo Chat")
                                            }
                                        } else {
                                            // Para cualquier otro caso
                                            card.onClick()
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al navegar desde tarjeta de comunicación: ${card.titulo}")
                                        viewModel.showSnackbarMessage("Error al navegar a ${card.titulo}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- Gestión y Planificación ---
        item { SeccionTitulo("Gestión y Planificación") }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ahora solo hay 2 cards, mostrarlas en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primera card - 50% del ancho - Mis Alumnos
                    Box(modifier = Modifier.weight(1f)) {
                        CategoriaCard(
                            titulo = gestionPlanificacionCards[0].titulo,
                            descripcion = gestionPlanificacionCards[0].descripcion,
                            icono = gestionPlanificacionCards[0].icono,
                            color = gestionPlanificacionCards[0].color,
                            iconTint = gestionPlanificacionCards[0].iconTint,
                            border = true,
                            onClick = {
                                haptic.performHapticFeedbackSafely()
                                try {
                                    // Navegando explícitamente a Mis Alumnos y no usando el callback
                                    navController.navigate(AppScreens.MisAlumnosProfesor.route)
                                    Timber.d("Navegación a Mis Alumnos Profesor exitosa")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al navegar a Mis Alumnos Profesor")
                                    viewModel.showSnackbarMessage("Error al navegar a Mis Alumnos")
                                }
                            }
                        )
                    }
                    // Segunda card - 50% del ancho - Calendario
                    Box(modifier = Modifier.weight(1f)) {
                        CategoriaCard(
                            titulo = gestionPlanificacionCards[1].titulo,
                            descripcion = gestionPlanificacionCards[1].descripcion,
                            icono = gestionPlanificacionCards[1].icono,
                            color = gestionPlanificacionCards[1].color,
                            iconTint = gestionPlanificacionCards[1].iconTint,
                            border = true,
                            onClick = {
                                haptic.performHapticFeedbackSafely()
                                try {
                                    // Navegando explícitamente a Calendario
                                    navController.navigate(AppScreens.ProfesorCalendario.route)
                                    Timber.d("Navegación a Calendario Profesor exitosa")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al navegar a Calendario Profesor")
                                    viewModel.showSnackbarMessage("Error al navegar al Calendario")
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- Añadir la nueva sección después de Gestión y Planificación
        item { SeccionTitulo("Notificaciones y Preferencias") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card de Notificaciones
                Box(modifier = Modifier.weight(1f)) {
                    CategoriaCard(
                        titulo = "Gestión de Notificaciones",
                        descripcion = "Configura qué notificaciones deseas recibir y cómo",
                        icono = Icons.Default.Notifications,
                        color = ProfesorColor,
                        iconTint = Color.White.copy(alpha = 0.9f),
                        border = true,
                        onClick = {
                            haptic.performHapticFeedbackSafely()
                            try {
                                navController.navigate(AppScreens.Notificaciones.route)
                                Timber.d("Navegación a Gestión de Notificaciones exitosa")
                            } catch (e: Exception) {
                                Timber.e(e, "Error al navegar a Gestión de Notificaciones")
                                viewModel.showSnackbarMessage("Error al navegar a Gestión de Notificaciones")
                            }
                        }
                    )
                }
                
                // Card de Tema
                Box(modifier = Modifier.weight(1f)) {
                    CategoriaCard(
                        titulo = "Tema",
                        descripcion = "Cambiar el tema de la aplicación",
                        icono = Icons.Default.Palette,
                        color = ProfesorColor,
                        iconTint = Color.White.copy(alpha = 0.9f),
                        border = true,
                        onClick = { onThemeClick() }
                    )
                }
            }
        }

    }
}

/**
 * Composable para mostrar la cabecera de una sección en el dashboard.
 *
 * @param titulo Título de la sección.
 * @param color Color principal para el título y el divisor.
 */
@Composable
private fun SeccionTitulo(titulo: String) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ProfesorColor
        )
        HorizontalDivider(thickness = 1.dp, color = ProfesorColor.copy(alpha = 0.3f))
    }
}

/**
 * Tarjeta de bienvenida personalizada para el profesor.
 *
 * Muestra un saludo, la fecha, información de la clase y un indicador si es día festivo.
 *
 * @param nombreProfesor Nombre del profesor (puede ser null si aún no ha cargado).
 * @param infoClase Información de la clase (ej: "Infantil 2B - 15 alumnos") (puede ser null).
 * @param esFestivo Booleano que indica si hoy es festivo.
 */
@Composable
private fun BienvenidaProfesorCard(
    nombreProfesor: String?,
    infoClase: String?,
    esFestivo: Boolean
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    }
    val fechaHoy = LocalDate.now().format(dateFormatter).replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ProfesorColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola, ${nombreProfesor ?: "Profesor/a"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fechaHoy,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                if (!infoClase.isNullOrBlank()) {
                    Text(
                        text = infoClase,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            if (esFestivo) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Festival,
                        contentDescription = "Día Festivo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Festivo",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                 Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/**
 * Composable que muestra un resumen de los alumnos pendientes de registro diario.
 *
 * Aparece solo si hay alumnos pendientes y ofrece un botón para ir a la sección correspondiente.
 *
 * @param alumno Alumno pendiente.
 * @param onClick Lambda que se ejecuta al pulsar el botón "Registrar".
 */
@Composable
private fun AlumnoPendienteCard(
    alumno: Alumno,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${alumno.nombre} sin registro hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Button(
                onClick = {
                    onClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Registrar", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// --- Preview ---

@Preview(showBackground = true, name = "Dashboard Profesor - Día Normal")
@Composable
fun ProfesorDashboardPreview() {
    UmeEguneroTheme {
        val previewState = ProfesorDashboardUiState(
            profesorNombre = "Ainhoa Martinez",
            claseInfo = "Infantil 2B - 15 alumnos",
            alumnosPendientes = List(3) { Alumno(dni = "$it", nombre = "Alumno $it") },
            esFestivoHoy = false
        )
        Box(modifier = Modifier.padding(8.dp)) {
             ProfesorDashboardContent(
                paddingValues = PaddingValues(),
                uiState = previewState,
                viewModel = hiltViewModel(),
                navController = rememberNavController(),
                onChatFamiliasClick = {},
                onComunicadosClick = {},
                onMisAlumnosClick = {},
                onCalendarioClick = {},
                onRegistroDiarioClick = {},
                onThemeClick = {},
                haptic = LocalHapticFeedback.current
            )
        }
    }
}

@Preview(showBackground = true, name = "Dashboard Profesor - Día Festivo", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfesorDashboardFestivoDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
         val previewStateFestivo = ProfesorDashboardUiState(
            profesorNombre = "Ainhoa Martinez",
            claseInfo = "Infantil 2B - 15 alumnos",
            alumnosPendientes = emptyList(),
            esFestivoHoy = true
        )
        Box(modifier = Modifier.padding(8.dp)) {
            ProfesorDashboardContent(
                paddingValues = PaddingValues(),
                uiState = previewStateFestivo,
                viewModel = hiltViewModel(),
                navController = rememberNavController(),
                onChatFamiliasClick = {},
                onComunicadosClick = {},
                onMisAlumnosClick = {},
                onCalendarioClick = {},
                onRegistroDiarioClick = {},
                onThemeClick = {},
                haptic = LocalHapticFeedback.current
            )
        }
    }
}