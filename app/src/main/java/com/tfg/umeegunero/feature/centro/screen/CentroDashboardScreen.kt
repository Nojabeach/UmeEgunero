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
    // --- FUNCIONES DE NAVEGACIÓN LOCALES ---
    fun onNavigateToListaCursos() = navController.navigate(AppScreens.ListaCursos.route)
    fun onNavigateToListaClases() = navController.navigate(AppScreens.ListaClases.route)
    fun onNavigateToGestionProfesores() = navController.navigate(AppScreens.GestionProfesores.route)
    fun onNavigateToAddAlumno() = navController.navigate(AppScreens.AlumnoList.route)
    fun onNavigateToVincularProfesorClase() = navController.navigate(AppScreens.VincularProfesorClase.route)
    fun onNavigateToVinculacionFamiliar() = navController.navigate(AppScreens.VincularAlumnoFamiliar.route)
    fun onNavigateToCrearUsuarioRapido() = navController.navigate(AppScreens.CrearUsuarioRapido.route)
    fun onNavigateToCalendario() = navController.navigate(AppScreens.Calendario.route)
    fun onNavigateToNotificaciones() = navController.navigate(AppScreens.Notificaciones.route)

    // Definir la lista de tarjetas para el grid
    val cards = listOf(
        CategoriaCardData(
            titulo = "Cursos",
            descripcion = "Visualiza, crea y edita los cursos del centro",
            icono = Icons.AutoMirrored.Filled.MenuBook,
            color = CentroColor,
            iconTint = AppColors.PurplePrimary,
            border = true,
            onClick = { onNavigateToListaCursos() }
        ),
        CategoriaCardData(
            titulo = "Clases",
            descripcion = "Gestiona los grupos y asigna alumnos a clases",
            icono = Icons.Default.Class,
            color = CentroColor,
            iconTint = AppColors.PurpleSecondary,
            border = true,
            onClick = { onNavigateToListaClases() }
        ),
        CategoriaCardData(
            titulo = "Profesores",
            descripcion = "Consulta, añade y administra el personal docente",
            icono = Icons.Default.SupervisorAccount,
            color = CentroColor,
            iconTint = AppColors.Green500,
            border = true,
            onClick = { onNavigateToGestionProfesores() }
        ),
        CategoriaCardData(
            titulo = "Alumnos",
            descripcion = "Gestiona el listado y los datos de los estudiantes",
            icono = Icons.Default.Face,
            color = CentroColor,
            iconTint = AppColors.Pink80,
            border = true,
            onClick = { onNavigateToAddAlumno() }
        ),
        CategoriaCardData(
            titulo = "Asignación",
            descripcion = "Asigna profesores a clases de forma sencilla",
            icono = Icons.AutoMirrored.Filled.Assignment,
            color = CentroColor,
            iconTint = AppColors.PurpleTertiary,
            border = true,
            onClick = { onNavigateToVincularProfesorClase() }
        ),
        CategoriaCardData(
            titulo = "Vinculación Familiar",
            descripcion = "Gestiona la relación entre alumnos y familiares",
            icono = Icons.Default.People,
            color = CentroColor,
            iconTint = AppColors.Red500,
            border = true,
            onClick = { onNavigateToVinculacionFamiliar() }
        ),
        CategoriaCardData(
            titulo = "Crear Usuario",
            descripcion = "Registra nuevos usuarios en el sistema",
            icono = Icons.Default.PersonAdd,
            color = CentroColor,
            iconTint = AppColors.PurplePrimary,
            border = true,
            onClick = { onNavigateToCrearUsuarioRapido() }
        ),
        CategoriaCardData(
            titulo = "Calendario",
            descripcion = "Consulta eventos y fechas importantes del centro",
            icono = Icons.Default.DateRange,
            color = CentroColor,
            iconTint = AppColors.PurpleTertiary,
            border = true,
            onClick = { onNavigateToCalendario() }
        ),
        CategoriaCardData(
            titulo = "Notificaciones",
            descripcion = "Revisa avisos y comunicaciones recientes",
            icono = Icons.Default.Notifications,
            color = CentroColor,
            iconTint = AppColors.GradientEnd,
            border = true,
            onClick = { onNavigateToNotificaciones() }
        )
    )
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
                        // Sección: Gestión Académica
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Gestión Académica",
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
                        // Separador visual
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                        // Sección: Gestión de Personal
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Gestión de Personal",
                                style = MaterialTheme.typography.titleLarge,
                                color = CentroColor
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
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CategoriaCard(
                                titulo = "Asignación",
                                descripcion = "Asigna profesores a clases de forma sencilla",
                                icono = Icons.AutoMirrored.Filled.Assignment,
                                color = CentroColor,
                                iconTint = AppColors.PurpleTertiary,
                                border = true,
                                onClick = { onNavigateToVincularProfesorClase() },
                                modifier = Modifier.padding(4.dp).fillMaxWidth()
                            )
                        }
                        // Separador visual
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                        // Sección: Usuarios y Comunicaciones
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Usuarios y Comunicaciones",
                                style = MaterialTheme.typography.titleLarge,
                                color = CentroColor
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
                        // Espaciador final para scroll
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenido principal del Dashboard de Centro con tarjetas informativas y opciones de gestión.
 * 
 * @param nombreCentro Nombre del centro educativo
 * @param centroId Identificador del centro
 * @param cursos Lista de cursos del centro
 * @param currentDate Fecha actual formateada
 * @param currentUser Usuario actual (administrador de centro)
 * @param onNavigateToGestionProfesores Acción para navegar a gestión de profesores
 * @param onNavigateToGestionCursosYClases Acción para navegar a gestión de cursos y clases
 * @param onNavigateToVinculacionFamiliar Acción para navegar a vinculación familiar
 * @param onNavigateToCalendario Acción para navegar al calendario
 * @param onNavigateToNotificaciones Acción para navegar a notificaciones
 * @param onNavigateToAddAlumno Acción para navegar a añadir alumno
 * @param onNavigateToListaCursos Acción para navegar a la lista de cursos
 * @param onNavigateToListaClases Acción para navegar a la lista de clases
 * @param onNavigateToVincularProfesorClase Acción para navegar a vincular profesor a clase
 * @param onNavigateToCrearUsuarioRapido Acción para navegar a crear usuario rápido
 * @param onNavigateAccionAcademica Acción para navegar a gestión académica
 * @param modifier Modificador para personalizar el aspecto
 */
@Composable
fun CentroDashboardContent(
    nombreCentro: String,
    centroId: String,
    cursos: List<Curso>,
    currentDate: String,
    currentUser: Usuario?,
    onNavigateToGestionProfesores: () -> Unit,
    onNavigateToGestionCursosYClases: () -> Unit,
    onNavigateToVinculacionFamiliar: () -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToAddAlumno: () -> Unit,
    onNavigateToListaCursos: () -> Unit,
    onNavigateToListaClases: () -> Unit,
    onNavigateToVincularProfesorClase: () -> Unit,
    onNavigateToCrearUsuarioRapido: () -> Unit,
    onNavigateAccionAcademica: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Tarjeta de bienvenida
    Card(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp, max = 100.dp),
        colors = CardDefaults.cardColors(
            containerColor = CentroColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
            ) {
                Text(
                        text = "¡Bienvenido/a, ${currentUser?.nombre ?: ""}!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                        color = CentroColor
                )
                Text(
                    text = nombreCentro,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                        color = CentroColor
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                        .size(44.dp)
                    .clip(CircleShape)
                    .background(CentroColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        // Separador visual
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        // Sección: Gestión Académica
        Text(
            text = "Gestión Académica",
            style = MaterialTheme.typography.titleLarge,
            color = CentroColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoriaCard(
                titulo = "Cursos",
                descripcion = "Visualiza, crea y edita los cursos del centro",
                icono = Icons.AutoMirrored.Filled.MenuBook,
                color = CentroColor,
                iconTint = AppColors.PurplePrimary,
                border = false,
                onClick = { onNavigateToListaCursos() },
                modifier = Modifier.weight(1f)
            )
            CategoriaCard(
                titulo = "Clases",
                descripcion = "Gestiona los grupos y asigna alumnos a clases",
                icono = Icons.Default.Class,
                color = CentroColor,
                iconTint = AppColors.PurpleSecondary,
                border = false,
                onClick = { onNavigateToListaClases() },
                modifier = Modifier.weight(1f)
            )
        }
        // Separador visual
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        // Sección: Gestión de Personal
        Text(
            text = "Gestión de Personal",
            style = MaterialTheme.typography.titleLarge,
            color = CentroColor
        )
        Row(
        modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoriaCard(
                titulo = "Profesores",
                descripcion = "Consulta, añade y administra el personal docente",
                icono = Icons.Default.SupervisorAccount,
                color = CentroColor,
                iconTint = AppColors.Green500,
                border = false,
                onClick = { onNavigateToGestionProfesores() },
                modifier = Modifier.weight(1f)
            )
            CategoriaCard(
                titulo = "Alumnos",
                descripcion = "Gestiona el listado y los datos de los estudiantes",
                    icono = Icons.Default.Face,
                color = CentroColor,
                iconTint = AppColors.Pink80,
                border = false,
                onClick = { onNavigateToAddAlumno() },
                modifier = Modifier.weight(1f)
            )
            CategoriaCard(
                titulo = "Asignación",
                descripcion = "Asigna profesores a clases de forma sencilla",
                icono = Icons.AutoMirrored.Filled.Assignment,
                color = CentroColor,
                iconTint = AppColors.PurpleTertiary,
                border = false,
                onClick = { onNavigateToVincularProfesorClase() },
                modifier = Modifier.weight(1f)
            )
        }
        // Separador visual
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        // Sección: Usuarios y Comunicaciones
        Text(
            text = "Usuarios y Comunicaciones",
            style = MaterialTheme.typography.titleLarge,
            color = CentroColor
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoriaCard(
                titulo = "Vinculación Familiar",
                descripcion = "Gestiona la relación entre alumnos y familiares",
                icono = Icons.Default.People,
                color = CentroColor,
                iconTint = AppColors.Red500,
                border = false,
                onClick = { onNavigateToVinculacionFamiliar() },
                modifier = Modifier.weight(1f)
            )
            CategoriaCard(
                titulo = "Crear Usuario",
                descripcion = "Registra nuevos usuarios en el sistema",
                icono = Icons.Default.PersonAdd,
                color = CentroColor,
                iconTint = AppColors.PurplePrimary,
                border = false,
                onClick = { onNavigateToCrearUsuarioRapido() },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoriaCard(
                titulo = "Calendario",
                descripcion = "Consulta eventos y fechas importantes del centro",
                icono = Icons.Default.DateRange,
                color = CentroColor,
                iconTint = AppColors.PurpleTertiary,
                border = false,
                onClick = { onNavigateToCalendario() },
                modifier = Modifier.weight(1f)
            )
            CategoriaCard(
                titulo = "Notificaciones",
                descripcion = "Revisa avisos y comunicaciones recientes",
                icono = Icons.Default.Notifications,
                color = CentroColor,
                iconTint = AppColors.GradientEnd,
                border = false,
                onClick = { onNavigateToNotificaciones() },
                modifier = Modifier.weight(1f)
            )
        }
        // Espaciador final para scroll
        Spacer(modifier = Modifier.height(32.dp))
    }
}

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

/**
 * Previsualización del contenido del Dashboard de Centro con datos de ejemplo.
 */
@Preview(showBackground = true)
@Composable
fun CentroDashboardContentPreview() {
    UmeEguneroTheme {
        Surface {
            CentroDashboardContent(
                nombreCentro = "IES Valle del Cidacos",
                centroId = "centro1",
                cursos = emptyList(),
                currentDate = "Lunes, 10 de abril",
                currentUser = Usuario(nombre = "Carmen"),
                onNavigateToGestionProfesores = {},
                onNavigateToGestionCursosYClases = {},
                onNavigateToVinculacionFamiliar = {},
                onNavigateToCalendario = {},
                onNavigateToNotificaciones = {},
                onNavigateToAddAlumno = {},
                onNavigateToListaCursos = {},
                onNavigateToListaClases = {},
                onNavigateToVincularProfesorClase = {},
                onNavigateToCrearUsuarioRapido = {},
                onNavigateAccionAcademica = {}
            )
        }
    }
} 