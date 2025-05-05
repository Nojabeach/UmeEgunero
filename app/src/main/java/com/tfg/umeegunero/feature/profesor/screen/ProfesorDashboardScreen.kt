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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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

    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
        // Podrías llamar a una función explícita del ViewModel para cargar/refrescar si es necesario
        // viewModel.cargarDatosDashboard()
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
    
    // Diálogo de confirmación para ir a Registro Diario
    if (showRegistroDiarioDialog) {
        AlertDialog(
            onDismissRequest = { showRegistroDiarioDialog = false },
            title = { Text("Registro Diario") },
            text = { Text("¿Quieres realizar el registro diario de tus alumnos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showRegistroDiarioDialog = false
                        try {
                            navController.navigate(AppScreens.ListadoPreRegistroDiario.createRoute())
                            Timber.d("Navegando a Listado Pre-Registro Diario")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Listado Pre-Registro Diario")
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
            text = { Text("¿Quieres ver tus conversaciones con las familias?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        showChatFamiliasDialog = false
                        try {
                            navController.navigate(AppScreens.UnifiedInbox.route)
                            Timber.d("Navegando a Chat con Familias")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al navegar a Chat con Familias")
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChatFamiliasDialog = false }) {
                    Text("No")
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
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
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
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
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
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
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
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
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

    // --- UI Principal (Scaffold) ---
    Scaffold(
        topBar = {
            ProfesorDashboardTopBar(
                onProfileClick = { showPerfilDialog = true },
                onLogoutClick = { showLogoutDialog = true }
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
    onLogoutClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Panel de Profesor", // Título más conciso
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = ProfesorColor, // Color corporativo del profesor
            titleContentColor = Color.White, // Color del título
            actionIconContentColor = Color.White // Color de los iconos de acción
        ),
        actions = {
            // Icono de perfil
            IconButton(onClick = { 
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                onProfileClick() 
            }) {
                Icon(
                    imageVector = Icons.Default.Face, // Icono más representativo para perfil
                    contentDescription = "Ver Perfil",
                    tint = Color.White // Asegurar tinte blanco
                )
            }
            
            // Botón de cerrar sesión
            IconButton(onClick = { 
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                onLogoutClick() 
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = Color.White // Asegurar tinte blanco
                )
            }
        }
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
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
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
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                navController.navigate(AppScreens.UnifiedInbox.route)
            }
        ),
        CategoriaCardData(
            titulo = "Nuevo Mensaje",
            descripcion = "Crear comunicados o mensajes personalizados",
            icono = Icons.AutoMirrored.Filled.SpeakerNotes,
            color = ProfesorColor,
            onClick = { 
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                navController.navigate(AppScreens.NewMessage.createRoute())
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
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
                onMisAlumnosClick()
            }
        ),
        CategoriaCardData(
            titulo = "Calendario",
            descripcion = "Eventos escolares y festivos",
            icono = Icons.Default.CalendarMonth,
            color = ProfesorColor,
            onClick = { 
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    Timber.e(e, "Error al realizar feedback háptico")
                }
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

        // --- Acciones Diarias ---
        item { SeccionTitulo("Acciones Diarias") }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mostrar una única card para registro diario con altura aumentada
                CategoriaCard(
                    titulo = accionesDiariasCards[0].titulo,
                    descripcion = accionesDiariasCards[0].descripcion,
                    icono = accionesDiariasCards[0].icono,
                    color = accionesDiariasCards[0].color,
                    iconTint = accionesDiariasCards[0].iconTint,
                    border = true,
                    enabled = !hoyEsFestivo,
                    onClick = accionesDiariasCards[0].onClick,
                    modifier = Modifier.heightIn(min = 100.dp)
                )
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
                                onClick = card.onClick
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
                    // Primera card - 50% del ancho
                    Box(modifier = Modifier.weight(1f)) {
                        CategoriaCard(
                            titulo = gestionPlanificacionCards[0].titulo,
                            descripcion = gestionPlanificacionCards[0].descripcion,
                            icono = gestionPlanificacionCards[0].icono,
                            color = gestionPlanificacionCards[0].color,
                            iconTint = gestionPlanificacionCards[0].iconTint,
                            border = true,
                            onClick = gestionPlanificacionCards[0].onClick
                        )
                    }
                    // Segunda card - 50% del ancho
                    Box(modifier = Modifier.weight(1f)) {
                        CategoriaCard(
                            titulo = gestionPlanificacionCards[1].titulo,
                            descripcion = gestionPlanificacionCards[1].descripcion,
                            icono = gestionPlanificacionCards[1].icono,
                            color = gestionPlanificacionCards[1].color,
                            iconTint = gestionPlanificacionCards[1].iconTint,
                            border = true,
                            onClick = gestionPlanificacionCards[1].onClick
                        )
                    }
                }
            }
        }

        // --- Alumnos Pendientes ---
        if (uiState.alumnosPendientes.isNotEmpty() && !hoyEsFestivo) {
             item { SeccionTitulo("Alumnos con Registro Pendiente Hoy") }
             uiState.alumnosPendientes.forEach { alumno ->
                 item {
                     AlumnoPendienteCard(
                         alumno = alumno,
                         onClick = {
                             try {
                                 navController.navigate(AppScreens.DetalleAlumnoProfesor.createRoute(alumno.id))
                                 Timber.d("Navegando a Detalle de Alumno: ${alumno.nombre}")
                             } catch (e: Exception) {
                                 Timber.e(e, "Error al navegar a Detalle de Alumno: ${alumno.nombre}")
                             }
                         }
                     )
                     Spacer(modifier = Modifier.height(8.dp))
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
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al realizar feedback háptico")
                    }
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
            alumnosPendientes = List(3) { Alumno(id = "$it", nombre = "Alumno $it") },
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
                haptic = LocalHapticFeedback.current
            )
        }
    }
}