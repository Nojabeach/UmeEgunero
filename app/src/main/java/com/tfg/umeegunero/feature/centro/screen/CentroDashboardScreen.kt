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
import androidx.compose.foundation.lazy.grid.GridItemSpan

/**
 * Pantalla principal del dashboard para administradores de centro educativo.
 * 
 * Esta pantalla presenta un panel de control completo con estadísticas, accesos rápidos
 * y gestión de diferentes áreas del centro educativo. Implementa un diseño moderno
 * siguiendo las directrices de Material 3.
 * 
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 * @param viewModel ViewModel que contiene la lógica de negocio del dashboard de centro
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 3.0
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
                
            } // Cierre Box
        } // Cierre lambda de contenido del Scaffold
    } // Cierre de la función @Composable CentroDashboardScreen

/**
 * Previsualización del Dashboard de Centro.
 */
@Preview(showBackground = true)
@Composable
fun CentroDashboardScreenPreview() {
    UmeEguneroTheme {
        CentroDashboardScreen(navController = rememberNavController())
    }
} 