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
    var showLogoutDialog by remember { mutableStateOf(false) }
    var mostrarDialogoSolicitudes by remember { mutableStateOf(false) }
    val solicitudesPendientes by viewModel.solicitudesPendientes.collectAsState()
    val emailStatus by viewModel.emailStatus.collectAsState()
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
    // Obtener el ID del centro del usuario actual
    val centroId = uiState.currentUser?.perfiles?.firstOrNull { perfil -> 
        perfil.tipo == TipoUsuario.ADMIN_CENTRO 
    }?.centroId ?: ""

    // Funciones de navegación locales actualizadas
    fun onNavigateToListaCursos() = try {
        navController.navigate("gestor_academico/CURSOS?centroId=$centroId&selectorCentroBloqueado=true&perfilUsuario=ADMIN_CENTRO")
    } catch (e: Exception) {
        scope.launch {
            snackbarHostState.showSnackbar("Error al navegar a cursos: ${e.message}")
        }
    }

    fun onNavigateToListaClases() = try {
        navController.navigate("gestor_academico/CLASES?centroId=$centroId&selectorCentroBloqueado=true&perfilUsuario=ADMIN_CENTRO")
    } catch (e: Exception) {
        scope.launch {
            snackbarHostState.showSnackbar("Error al navegar a clases: ${e.message}")
        }
    }

    fun onNavigateToGestionProfesores() = navController.navigate(AppScreens.GestionProfesores.route)
    fun onNavigateToAddAlumno() = try {
        if (centroId.isNotBlank()) {
            navController.navigate(AppScreens.AlumnoList.createRoute(centroId))
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("No se pudo obtener el ID del centro.")
            }
        }
    } catch (e: Exception) {
        scope.launch {
            snackbarHostState.showSnackbar("Error al navegar a lista de alumnos: ${e.message}")
        }
    }
    fun onNavigateToVincularProfesorClase() = navController.navigate(AppScreens.VincularProfesorClase.route)
    fun onNavigateToVinculacionFamiliar() = navController.navigate(AppScreens.VincularAlumnoFamiliar.route)
    fun onNavigateToCrearUsuarioRapido() = navController.navigate(AppScreens.CrearUsuarioRapido.route)
    fun onNavigateToCalendario() = navController.navigate(AppScreens.Calendario.route)
    fun onNavigateToNotificaciones() = navController.navigate(AppScreens.Notificaciones.route)

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
                    IconButton(onClick = { navController.navigate(AppScreens.Perfil.route) }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                    
                    // Botón de prueba de email (solo para desarrollo)
                    IconButton(onClick = { navController.navigate(AppScreens.PruebaEmail.route) }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Prueba Email",
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
                                        iconTint = Color.White,
                                        border = true
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
                                    border = true,
                                    onClick = { onNavigateToListaCursos() },
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
                                    border = true,
                                    onClick = { onNavigateToListaClases() },
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
                                    border = true,
                                    onClick = { onNavigateToCalendario() },
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
                                    border = true,
                                    onClick = { onNavigateToAddAlumno() },
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
                                    border = true,
                                    onClick = { onNavigateToGestionProfesores() },
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
                                    border = true,
                                    onClick = { onNavigateToVinculacionFamiliar() },
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
                                    border = true,
                                    onClick = { onNavigateToVincularProfesorClase() },
                                    modifier = Modifier.padding(4.dp) // Quitado fillMaxWidth para que quepa en la grid
                                )
                            }
                            item {
                                CategoriaCard(
                                    titulo = "Crear Usuario",
                                    descripcion = "Registra nuevos usuarios en el sistema",
                                    icono = Icons.Default.PersonAdd,
                                    color = CentroColor,
                                    iconTint = AppColors.PurplePrimary,
                                    border = true,
                                    onClick = { onNavigateToCrearUsuarioRapido() },
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
                                    border = true,
                                    onClick = { onNavigateToNotificaciones() },
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
                                    border = true,
                                    onClick = { mostrarDialogoSolicitudes = true },
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
                
                // --- Mover AlertDialog aquí dentro del Box --- 
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Cerrar sesión") },
                        text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                viewModel.logout()
                            }) { Text("Sí") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) { Text("No") }
                        }
                    )
                }
                // --- Fin AlertDialog movido ---
                
                // --- Diálogo de Solicitudes de Vinculación ---
                if (mostrarDialogoSolicitudes) {
                    AlertDialog(
                        onDismissRequest = { 
                            mostrarDialogoSolicitudes = false 
                            viewModel.limpiarEstadoEmail()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 600.dp),
                        title = { 
                            Text(
                                "Solicitudes de Vinculación Pendientes",
                                style = MaterialTheme.typography.titleLarge,
                                color = CentroColor
                            ) 
                        },
                        text = {
                            Column {
                                // Mostrar mensaje cuando no hay solicitudes
                                if (solicitudesPendientes.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No hay solicitudes pendientes",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                } else {
                                    // Lista de solicitudes pendientes
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 400.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(solicitudesPendientes) { solicitud ->
                                            SolicitudVinculacionItem(
                                                solicitud = solicitud,
                                                onAprobar = { 
                                                    viewModel.procesarSolicitud(
                                                        solicitudId = solicitud.id, 
                                                        aprobar = true,
                                                        enviarEmail = true,
                                                        emailFamiliar = null // Aquí deberíamos obtener el email del familiar
                                                    )
                                                },
                                                onRechazar = { 
                                                    viewModel.procesarSolicitud(
                                                        solicitudId = solicitud.id, 
                                                        aprobar = false,
                                                        enviarEmail = true,
                                                        emailFamiliar = null // Aquí deberíamos obtener el email del familiar
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                // Mostrar estado del envío de emails
                                emailStatus?.let { status ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { 
                                // Recargar solicitudes por si hay cambios
                                viewModel.cargarSolicitudesPendientes()
                            }) { 
                                Text("Actualizar") 
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { 
                                mostrarDialogoSolicitudes = false 
                                viewModel.limpiarEstadoEmail()
                            }) { 
                                Text("Cerrar") 
                            }
                        }
                    )
                }
                // --- Fin Diálogo de Solicitudes de Vinculación ---
                
            } // Cierre Box
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
                text = "Alumno: ${solicitud.alumnoNombre ?: "No especificado"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "DNI: ${solicitud.alumnoDni}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Fecha de solicitud formateada
            Text(
                text = "Fecha: ${formatearFecha(solicitud.fechaSolicitud.toDate())}",
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