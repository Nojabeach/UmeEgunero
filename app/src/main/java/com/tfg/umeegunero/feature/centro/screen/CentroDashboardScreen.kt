/**
 * Módulo de panel de control para centros educativos de UmeEgunero.
 * 
 * Este módulo implementa la interfaz principal del panel de control
 * para los administradores de centros educativos, proporcionando
 * acceso rápido a todas las funcionalidades del sistema.
 * 
 * ## Características principales
 * - Interfaz moderna y responsive
 * - Navegación intuitiva
 * - Gestión de estado con Compose
 * - Integración con ViewModel
 * 
 * @see CentroDashboardViewModel
 * @see CentroDashboardUiState
 */
package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCardBienvenida
import com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.CategoriaCardData
import com.tfg.umeegunero.ui.components.BadgedBox
import com.tfg.umeegunero.ui.theme.AppColors
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.res.Configuration
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.BorderStroke
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.view.HapticFeedbackConstants
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import timber.log.Timber

/**
 * Pantalla principal del panel de control para administradores de centro.
 * 
 * Esta pantalla proporciona una interfaz completa para la gestión del centro
 * educativo, con acceso rápido a todas las funcionalidades principales y
 * visualización de información relevante.
 * 
 * ## Características
 * - Panel de control intuitivo
 * - Accesos directos a funciones principales
 * - Estadísticas en tiempo real
 * - Notificaciones y alertas
 * 
 * ## Secciones principales
 * - Gestión académica (cursos y clases)
 * - Gestión de personal (profesores)
 * - Gestión de alumnado
 * - Comunicaciones y notificaciones
 * 
 * ## Estados
 * - Carga de datos
 * - Visualización normal
 * - Diálogo de cierre de sesión
 * - Errores y mensajes
 * 
 * ## Navegación
 * La pantalla proporciona acceso a:
 * - Lista de cursos
 * - Lista de clases
 * - Gestión de profesores
 * - Gestión de alumnos
 * - Vinculación profesor-clase
 * - Vinculación familiar
 * - Creación rápida de usuarios
 * - Calendario
 * - Notificaciones
 * 
 * @param navController Controlador de navegación para la gestión de rutas
 * @param viewModel ViewModel que gestiona la lógica del panel
 * 
 * @see CentroDashboardViewModel
 * @see CentroDashboardUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentroDashboardScreen(
    navController: NavController,
    viewModel: CentroDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val nombreCentro by viewModel.nombreCentro.collectAsState(initial = "Centro Educativo")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    // Actualizar contador de notificaciones al inicializar
    LaunchedEffect(Unit) {
        viewModel.actualizarContadorNotificaciones()
    }
    
    // Diálogos de confirmación
    var showLogoutDialog by remember { mutableStateOf(false) }
    var mostrarDialogoSolicitudes by remember { mutableStateOf(false) }
    var showCursosDialog by remember { mutableStateOf(false) }
    var showClasesDialog by remember { mutableStateOf(false) }
    var showProfesoresDialog by remember { mutableStateOf(false) }
    var showAlumnosDialog by remember { mutableStateOf(false) }
    var showVincularProfesorDialog by remember { mutableStateOf(false) }
    var showVincularFamiliarDialog by remember { mutableStateOf(false) }
    var showCrearUsuarioDialog by remember { mutableStateOf(false) }
    var showCalendarioDialog by remember { mutableStateOf(false) }
    var showNotificacionesDialog by remember { mutableStateOf(false) }
    var showPerfilDialog by remember { mutableStateOf(false) }
    var showMensajesUnificadosDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val solicitudesPendientes by viewModel.solicitudesPendientes.collectAsState()
    val currentDate = remember { 
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).replaceFirstChar { it.uppercase() } 
    }
    // Animación de entrada
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearError()
            }
        }
    }
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.CentroDashboard.route) { inclusive = true }
            }
        }
    }
    // Cargar solicitudes de vinculación cuando se abre el diálogo
    LaunchedEffect(mostrarDialogoSolicitudes) {
        if (mostrarDialogoSolicitudes) {
            viewModel.cargarSolicitudesPendientes()
        }
    }
    
    // Los emails se envían automáticamente a través del SolicitudRepository
    // No se necesita código adicional para gestionar el envío de correos
    
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
    
    // Obtener el ID del centro del usuario actual
    val centroId = uiState.currentUser?.perfiles?.firstOrNull { perfil -> 
        perfil.tipo == TipoUsuario.ADMIN_CENTRO 
    }?.centroId ?: ""

    // Añadir una variable para controlar la visibilidad del diálogo de error de índice
    var showIndexErrorDialog by remember { mutableStateOf(false) }
    var indexErrorException by remember { mutableStateOf<Exception?>(null) }

    // Verificar si hay errores de índice en las colecciones clave
    LaunchedEffect(Unit) {
        val errorMessages = listOf(
            "FAILED_PRECONDITION: The query requires an index",
            "requires an index"
        )
        
        viewModel.errorEvents.collect { error ->
            if (error != null && errorMessages.any { error.message?.contains(it) == true }) {
                indexErrorException = error
                showIndexErrorDialog = true
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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
    
    // Diálogo de confirmación para ir a Cursos
    if (showCursosDialog) {
        AlertDialog(
            onDismissRequest = { showCursosDialog = false },
            title = { Text("Gestión de Cursos") },
            text = { Text("¿Quieres ir a la gestión de cursos del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showCursosDialog = false
                        try {
                            navController.navigate("gestor_academico/CURSOS?centroId=$centroId&selectorCentroBloqueado=true&perfilUsuario=ADMIN_CENTRO")
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a cursos: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCursosDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Clases
    if (showClasesDialog) {
        AlertDialog(
            onDismissRequest = { showClasesDialog = false },
            title = { Text("Gestión de Clases") },
            text = { Text("¿Quieres ir a la gestión de clases del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showClasesDialog = false
                        try {
                            navController.navigate("gestor_academico/CLASES?centroId=$centroId&selectorCentroBloqueado=true&perfilUsuario=ADMIN_CENTRO")
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a clases: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClasesDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Profesores
    if (showProfesoresDialog) {
        AlertDialog(
            onDismissRequest = { showProfesoresDialog = false },
            title = { Text("Gestión de Profesores") },
            text = { Text("¿Quieres ir a la gestión de profesores del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showProfesoresDialog = false
                        try {
                            if (centroId.isNotBlank()) {
                                navController.navigate(AppScreens.ProfesorList.createRoute(centroId))
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No se pudo obtener el ID del centro para ver profesores.")
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a profesores: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfesoresDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para ir a Alumnos
    if (showAlumnosDialog) {
        AlertDialog(
            onDismissRequest = { showAlumnosDialog = false },
            title = { Text("Gestión de Alumnos") },
            text = { Text("¿Quieres ir a la gestión de alumnos del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAlumnosDialog = false
                        try {
                            if (centroId.isNotBlank()) {
                                navController.navigate(AppScreens.AlumnoList.createRoute(centroId))
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No se pudo obtener el ID del centro para ver alumnos.")
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a alumnos: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlumnosDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para Vincular Profesor a Clase
    if (showVincularProfesorDialog) {
        AlertDialog(
            onDismissRequest = { showVincularProfesorDialog = false },
            title = { Text("Vincular Profesor a Clase") },
            text = { Text("¿Quieres vincular profesores a clases?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showVincularProfesorDialog = false
                        try {
                            if (centroId.isNotBlank()) {
                                navController.navigate(AppScreens.VincularProfesorClase.createRoute(centroId))
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No se pudo obtener el ID del centro.")
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a vinculación profesor-clase: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVincularProfesorDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para Vincular Familiar
    if (showVincularFamiliarDialog) {
        AlertDialog(
            onDismissRequest = { showVincularFamiliarDialog = false },
            title = { Text("Vincular Familiar") },
            text = { Text("¿Quieres vincular familiares a alumnos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showVincularFamiliarDialog = false
                        try {
                            navController.navigate(AppScreens.VincularAlumnoFamiliar.route)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a vinculación familiar: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVincularFamiliarDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    
    // Diálogo de confirmación para Crear Usuario
    if (showCrearUsuarioDialog) {
        AlertDialog(
            onDismissRequest = { showCrearUsuarioDialog = false },
            title = { Text("Crear Usuario") },
            text = { Text("¿Quieres crear un nuevo usuario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showCrearUsuarioDialog = false
                        try {
                            navController.navigate(AppScreens.CrearUsuarioRapido.route)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a creación de usuario: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrearUsuarioDialog = false }) {
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
            text = { Text("¿Quieres ver el calendario del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showCalendarioDialog = false
                        try {
                            navController.navigate(AppScreens.Calendario.route)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar al calendario: ${e.message}")
                            }
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
    
    // Diálogo de confirmación para ir a Notificaciones
    if (showNotificacionesDialog) {
        AlertDialog(
            onDismissRequest = { showNotificacionesDialog = false },
            title = { Text("Notificaciones") },
            text = { Text("¿Quieres ver las notificaciones del centro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showNotificacionesDialog = false
                        try {
                            navController.navigate(AppScreens.Notificaciones.route)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar a notificaciones: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificacionesDialog = false }) {
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
                TextButton(onClick = {
                    try {
                        showPerfilDialog = false
                        // Navegar directamente sin lógica adicional
                        navController.navigate(AppScreens.Perfil.createRoute(false))
                    } catch (e: Exception) {
                        Timber.e(e, "Error al navegar al perfil: ${e.message}")
                        scope.launch {
                            snackbarHostState.showSnackbar("Error al navegar al perfil")
                        }
                    }
                }) {
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

    // Añadir el diálogo de comunicación unificada después de los otros diálogos existentes
    if (showMensajesUnificadosDialog) {
        AlertDialog(
            onDismissRequest = { showMensajesUnificadosDialog = false },
            title = { Text("Sistema de Comunicación Unificado") },
            text = { Text("¿Quieres acceder al sistema de comunicación unificado?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMensajesUnificadosDialog = false
                        try {
                            navController.navigate(AppScreens.UnifiedInbox.route)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al navegar al sistema de comunicación: ${e.message}")
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMensajesUnificadosDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    // Añadir el diálogo de error de índice
    if (showIndexErrorDialog && indexErrorException != null) {
        indexErrorException?.let { exception ->
            com.tfg.umeegunero.util.FirestoreIndexErrorHandler.showIndexErrorDialog(
                context = LocalContext.current,
                error = exception,
                onDismiss = { showIndexErrorDialog = false }
            )
        }
    }

    // Funciones de navegación locales actualizadas
    val onNavigateToListaCursos: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showCursosDialog = true
    }

    val onNavigateToListaClases: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showClasesDialog = true
    }

    val onNavigateToGestionProfesores: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showProfesoresDialog = true
    }

    val onNavigateToGestionAlumnos: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showAlumnosDialog = true
    }

    val onNavigateToVincularProfesorClase: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showVincularProfesorDialog = true
    }

    val onNavigateToVincularFamiliar: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showVincularFamiliarDialog = true
    }

    val onNavigateToCalendario: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showCalendarioDialog = true
    }

    val onNavigateToNotificaciones: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showNotificacionesDialog = true
    }

    val onNavigateToPerfil: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            Timber.e(e, "Error al realizar feedback háptico")
        }
        showPerfilDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Panel de Centro",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // BadgedBox para notificaciones pendientes
                    BadgedBox(
                        badge = {
                            if (uiState.notificacionesPendientes > 0) {
                                Badge { 
                                    Text(
                                        text = if (uiState.notificacionesPendientes > 99) "99+" else uiState.notificacionesPendientes.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al realizar feedback háptico")
                                }
                                // Navegar al inbox unificado
                                navController.navigate(AppScreens.UnifiedInbox.route)
                                // Actualizar contador de notificaciones
                                viewModel.actualizarContadorNotificaciones()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Mensajes",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Icono para probar emails
                    IconButton(
                        onClick = {
                            navController.navigate(AppScreens.PruebaEmail.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Probar Email",
                            tint = Color.White
                        )
                    }
                    
                    IconButton(onClick = onNavigateToPerfil) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                    
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = CentroColor.copy(alpha = 0.05f)
    ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CentroColor
                    )
                } else {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
                        exit = fadeOut()
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header de bienvenida
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                CategoriaCardBienvenida(
                                    data = CategoriaCardData(
                                        titulo = "¡Bienvenido/a, ${uiState.currentUser?.nombre ?: ""}!",
                                        descripcion = nombreCentro + "\n" + currentDate,
                                        icono = Icons.Filled.School,
                                        color = CentroColor,
                                        onClick = {},
                                        iconTint = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp, max = 100.dp)
                                )
                            }
                            // Separador visual
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                            
                            // --- Inicio Nueva Sección: Estructura Académica ---
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Estructura Académica",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CentroColor
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Cursos",
                                    descripcion = "Visualiza, crea y edita los cursos del centro",
                                    icono = Icons.AutoMirrored.Filled.MenuBook,
                                    color = CentroColor,
                                    iconTint = AppColors.PurplePrimary,
                                    onClick = onNavigateToListaCursos,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Clases",
                                    descripcion = "Gestiona los grupos y asigna alumnos a clases",
                                    icono = Icons.Default.Class,
                                    color = CentroColor,
                                    iconTint = AppColors.PurpleSecondary,
                                    onClick = onNavigateToListaClases,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                             item {
                                CategoriaCard(
                                    titulo = "Calendario",
                                    descripcion = "Consulta eventos y fechas importantes del centro",
                                    icono = Icons.Default.DateRange,
                                    color = CentroColor,
                                    iconTint = AppColors.PurpleTertiary,
                                    onClick = onNavigateToCalendario,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            // --- Fin Nueva Sección: Estructura Académica ---
                            
                            // Separador visual
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                            
                            // --- Inicio Nueva Sección: Comunidad Educativa ---
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Comunidad Educativa",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CentroColor
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Alumnos",
                                    descripcion = "Gestiona el listado y los datos de los estudiantes",
                                    icono = Icons.Default.Face,
                                    color = CentroColor,
                                    iconTint = AppColors.Pink80,
                                    onClick = onNavigateToGestionAlumnos,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Profesores",
                                    descripcion = "Consulta, añade y administra el personal docente",
                                    icono = Icons.Default.SupervisorAccount,
                                    color = CentroColor,
                                    iconTint = AppColors.Green500,
                                    onClick = onNavigateToGestionProfesores,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                             item {
                                CategoriaCard(
                                    titulo = "Vinculación Familiar",
                                    descripcion = "Gestiona la relación entre alumnos y familiares",
                                    icono = Icons.Default.People,
                                    color = CentroColor,
                                    iconTint = AppColors.Red500,
                                    onClick = onNavigateToVincularFamiliar,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Asignación",
                                    descripcion = "Asigna profesores a clases de forma sencilla",
                                    icono = Icons.AutoMirrored.Filled.Assignment,
                                    color = CentroColor,
                                    iconTint = AppColors.PurpleTertiary,
                                    onClick = onNavigateToVincularProfesorClase,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                           
                            // --- Fin Nueva Sección: Comunidad Educativa ---
                            
                            // Separador visual
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                            
                            // --- Inicio Nueva Sección: Herramientas Administrativas ---
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Herramientas Administrativas",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CentroColor
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Notificaciones",
                                    descripcion = "Revisa avisos y comunicaciones recientes",
                                    icono = Icons.Default.Notifications,
                                    color = CentroColor,
                                    iconTint = AppColors.GradientEnd,
                                    onClick = onNavigateToNotificaciones,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Solicitudes de Vinculación",
                                    descripcion = "Gestiona las solicitudes de los padres para vincularse con sus hijos",
                                    icono = Icons.Default.People,
                                    color = CentroColor,
                                    iconTint = AppColors.Pink80,
                                    onClick = { mostrarDialogoSolicitudes = true },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            item {
                                BadgedBox(
                                    badge = {
                                        if (uiState.notificacionesPendientes > 0) {
                                            Badge { 
                                                Text(
                                                    text = if (uiState.notificacionesPendientes > 99) "99+" else uiState.notificacionesPendientes.toString(),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    CategoriaCard(
                                        titulo = "Comunicación",
                                        descripcion = "Sistema unificado de mensajes\ny comunicados",
                                        icono = Icons.Default.Email,
                                        color = CentroColor,
                                        iconTint = AppColors.Blue500,
                                        onClick = { 
                                            try {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } catch (e: Exception) {
                                                Timber.e(e, "Error al realizar feedback háptico")
                                            }
                                            // Marcar notificaciones como leídas al abrir comunicación
                                            viewModel.actualizarContadorNotificaciones()
                                            navController.navigate(AppScreens.UnifiedInbox.route)
                                        }
                                    )
                                }
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Historial de Solicitudes",
                                    descripcion = "Ver el historial completo de solicitudes procesadas",
                                    icono = Icons.Default.History,
                                    color = CentroColor,
                                    iconTint = AppColors.PurplePrimary,
                                    onClick = { navController.navigate(AppScreens.HistorialSolicitudes.route) },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Tema",
                                    descripcion = "Cambiar el tema de la aplicación",
                                    icono = Icons.Default.Palette,
                                    color = CentroColor,
                                    iconTint = AppColors.GradientStart,
                                    onClick = { showThemeDialog = true },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                            // --- Fin Nueva Sección: Herramientas Administrativas ---

                            // Espaciador final para scroll
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            } // Cierre Box
            
            // --- Diálogo de Solicitudes de Vinculación ---
            if (mostrarDialogoSolicitudes) {
                SolicitudesPendientesDialog(
                    solicitudes = solicitudesPendientes,
                    onDismiss = { mostrarDialogoSolicitudes = false },
                    onProcesarSolicitud = { solicitudId, aprobar ->
                        viewModel.procesarSolicitud(
                            solicitudId = solicitudId,
                            aprobar = aprobar
                            // No es necesario especificar enviarEmail=true, ya que los emails
                            // se envían automáticamente a través del repositorio
                        )
                    }
                )
            }
            // --- Fin Diálogo de Solicitudes de Vinculación ---
            
        } // Cierre lambda de contenido del Scaffold
    } // Cierre de la función @Composable CentroDashboardScreen

/**
 * Vista previa de la pantalla del panel de control en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun CentroDashboardScreenPreview() {
    UmeEguneroTheme {
        CentroDashboardScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Vista previa de la pantalla del panel de control en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun CentroDashboardScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        CentroDashboardScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Componente que muestra una solicitud de vinculación con opciones para aprobarla o rechazarla.
 *
 * @param solicitud La solicitud de vinculación a mostrar
 * @param onAprobar Función a ejecutar cuando se aprueba la solicitud
 * @param onRechazar Función a ejecutar cuando se rechaza la solicitud
 */
@Composable
fun SolicitudVinculacionItem(
    solicitud: SolicitudVinculacion,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    var procesando by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Información del alumno
            Text(
                text = "Alumno: ${solicitud.alumnoNombre}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Información del familiar
            Text(
                text = "Familiar: ${solicitud.nombreFamiliar}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Tipo de relación
            Text(
                text = "Relación: ${solicitud.tipoRelacion}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Fecha de solicitud
            Text(
                text = "Fecha: ${solicitud.fechaSolicitud.toDate().let { 
                    SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(it) 
                }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botones de acción o Indicador de procesamiento
            if (procesando) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CentroColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Procesando solicitud...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { 
                            procesando = true
                            onRechazar()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Rechazar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { 
                            procesando = true
                            onAprobar()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CentroColor
                        )
                    ) {
                        Text("Aprobar")
                    }
                }
            }
        }
    }
}

/**
 * Formatea una fecha de tipo Date a un formato legible.
 *
 * @param fecha La fecha a formatear
 * @return String con la fecha formateada
 */
private fun formatearFecha(fecha: Date): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
    return formato.format(fecha)
}

@Composable
fun SolicitudesPendientesDialog(
    solicitudes: List<SolicitudVinculacion>,
    onDismiss: () -> Unit,
    onProcesarSolicitud: (solicitudId: String, aprobar: Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitudes Pendientes de Vinculación") },
        text = {
            if (solicitudes.isEmpty()) {
                Text("No hay solicitudes pendientes.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(solicitudes) { solicitud ->
                        SolicitudItem(solicitud = solicitud, onProcesarSolicitud = onProcesarSolicitud)
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun SolicitudItem(
    solicitud: SolicitudVinculacion,
    onProcesarSolicitud: (solicitudId: String, aprobar: Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Familiar: ${solicitud.nombreFamiliar}", fontWeight = FontWeight.Bold)
        Text("Alumno ID: ${solicitud.alumnoId}")
        Text("Relación: ${solicitud.tipoRelacion}", fontWeight = FontWeight.Bold)
        Text("Fecha: ${solicitud.fechaSolicitud.toDate().let { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(it) }}")
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = { onProcesarSolicitud(solicitud.id, true) }) {
                Text("Aprobar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = { onProcesarSolicitud(solicitud.id, false) }) {
                Text("Rechazar")
            }
        }
    }
} 