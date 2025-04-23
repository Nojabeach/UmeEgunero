package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.centro.viewmodel.CentroDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke

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
    
    // Variables para control de animaciones
    var showContent by remember { mutableStateOf(false) }
    val currentDate = remember { 
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")).replaceFirstChar { it.uppercase() } 
    }
    
    // Efecto para mostrar contenido con animación
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    // Efecto para manejar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearError()
            }
        }
    }
    
    // Efecto para manejar navegación
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.CentroDashboard.route) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Panel de Centro",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CentroColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.Configuracion.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CentroColor)
            }
        } else {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut()
            ) {
                CentroDashboardContent(
                    nombreCentro = nombreCentro,
                    centroId = uiState.centroId,
                    cursos = uiState.cursos,
                    currentDate = currentDate,
                    currentUser = uiState.currentUser,
                    onNavigateToGestionProfesores = { 
                        navController.navigate(AppScreens.GestionProfesores.route) 
                    },
                    onNavigateToGestionCursosYClases = { 
                        navController.navigate(AppScreens.GestionCursosYClases.route) 
                    },
                    onNavigateToVinculacionFamiliar = { 
                        navController.navigate(AppScreens.VincularAlumnoFamiliar.route) 
                    },
                    onNavigateToCalendario = { 
                        navController.navigate(AppScreens.Calendario.route) 
                    },
                    onNavigateToNotificaciones = { 
                        navController.navigate(AppScreens.GestionNotificacionesCentro.route) 
                    },
                    onNavigateToAddAlumno = { 
                        navController.navigate(AppScreens.AddAlumno.route) 
                    },
                    onNavigateToListaCursos = {
                        navController.navigate(AppScreens.ListaCursos.route)
                    },
                    onNavigateToListaClases = {
                        navController.navigate(AppScreens.ListaClases.route)
                    },
                    onNavigateToVincularProfesorClase = {
                        navController.navigate(AppScreens.VincularProfesorClase.route)
                    },
                    onNavigateToCrearUsuarioRapido = {
                        navController.navigate(AppScreens.CrearUsuarioRapido.route)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de bienvenida
        WelcomeCard(
            nombreCentro = nombreCentro,
            currentDate = currentDate,
            userName = currentUser?.nombre ?: ""
        )
        
        // Estadísticas rápidas
        EstadisticasRapidasCard(
            numCursos = cursos.size,
            numAlumnos = cursos.sumOf { it.numAlumnos },
            numProfesores = cursos.sumOf { it.numProfesores }
        )
        
        // Título para sección de gestión académica
        SectionHeader(
            title = "Gestión Académica",
            icon = Icons.Default.School
        )
        
        // Accesos rápidos para gestión académica
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardActionCard(
                title = "Cursos",
                icon = Icons.Default.MenuBook,
                color = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToListaCursos,
                modifier = Modifier.weight(1f)
            )
            
            DashboardActionCard(
                title = "Clases",
                icon = Icons.Default.Class,
                color = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToListaClases,
                modifier = Modifier.weight(1f)
            )
            
            DashboardActionCard(
                title = "Centro",
                icon = Icons.Default.School,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onNavigateToGestionCursosYClases,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Título para sección de gestión de personal
        SectionHeader(
            title = "Gestión de Personal",
            icon = Icons.Default.SupervisorAccount
        )
        
        // Accesos rápidos para gestión de personal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardActionCard(
                title = "Profesores",
                icon = Icons.Default.SupervisorAccount,
                color = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToGestionProfesores,
                modifier = Modifier.weight(1f)
            )
            
            DashboardActionCard(
                title = "Alumnos",
                icon = Icons.Default.Face,
                color = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToAddAlumno,
                modifier = Modifier.weight(1f)
            )
            
            DashboardActionCard(
                title = "Asignación",
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onNavigateToVincularProfesorClase,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Título para sección de gestión de usuarios
        SectionHeader(
            title = "Usuarios y Comunicaciones",
            icon = Icons.Default.Group
        )
        
        // Rejilla de opciones principales
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false,
            modifier = Modifier.height(240.dp) // Altura fija para acomodar 2 filas
        ) {
            // Vinculación Familiar
            item {
                OpcionDashboardItem(
                    opcion = OpcionDashboard(
                        id = "familias",
                        titulo = "Vinculación Familiar",
                        descripcion = "Relacionar alumnos con familiares",
                        icono = Icons.Default.People,
                        color = MaterialTheme.colorScheme.error,
                        onClick = onNavigateToVinculacionFamiliar
                    )
                )
            }
            
            // Crear Usuario Rápido
            item {
                OpcionDashboardItem(
                    opcion = OpcionDashboard(
                        id = "crear_usuario",
                        titulo = "Crear Usuario",
                        descripcion = "Registro rápido de usuarios",
                        icono = Icons.Default.PersonAdd,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onNavigateToCrearUsuarioRapido
                    )
                )
            }
            
            // Calendario
            item {
                OpcionDashboardItem(
                    opcion = OpcionDashboard(
                        id = "calendario",
                        titulo = "Calendario",
                        descripcion = "Eventos y plazos",
                        icono = Icons.Default.DateRange,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = onNavigateToCalendario
                    )
                )
            }
            
            // Notificaciones
            item {
                OpcionDashboardItem(
                    opcion = OpcionDashboard(
                        id = "notificaciones",
                        titulo = "Notificaciones",
                        descripcion = "Comunicaciones masivas",
                        icono = Icons.Default.Notifications,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = onNavigateToNotificaciones
                    )
                )
            }
        }
        
        // Espaciador final para scroll
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Tarjeta de bienvenida con el nombre del centro y fecha actual.
 * 
 * @param nombreCentro Nombre del centro educativo
 * @param currentDate Fecha actual formateada
 * @param userName Nombre del usuario administrador
 */
@Composable
fun WelcomeCard(
    nombreCentro: String,
    currentDate: String,
    userName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CentroColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
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
                    text = "¡Bienvenido/a, $userName!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = nombreCentro,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CentroColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Encabezado de sección con icono y título
 */
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
            tint = CentroColor,
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
 * Tarjeta de acción para el dashboard
 */
@Composable
fun DashboardActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Tarjeta con estadísticas rápidas del centro educativo.
 * 
 * @param numCursos Número de cursos del centro
 * @param numAlumnos Número total de alumnos
 * @param numProfesores Número total de profesores
 */
@Composable
fun EstadisticasRapidasCard(
    numCursos: Int,
    numAlumnos: Int,
    numProfesores: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Resumen del Centro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    valor = numCursos.toString(),
                    etiqueta = "Cursos",
                    icono = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                EstadisticaItem(
                    valor = numAlumnos.toString(),
                    etiqueta = "Alumnos",
                    icono = Icons.Default.Face,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                EstadisticaItem(
                    valor = numProfesores.toString(),
                    etiqueta = "Profesores",
                    icono = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * Elemento individual de estadística con ícono, valor y etiqueta.
 * 
 * @param valor Valor numérico a mostrar
 * @param etiqueta Etiqueta descriptiva
 * @param icono Icono representativo
 * @param color Color de acento
 */
@Composable
fun EstadisticaItem(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Modelo de datos para cada opción del dashboard.
 */
data class OpcionDashboard(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

/**
 * Elemento individual para cada opción del dashboard.
 * 
 * @param opcion Datos de la opción a mostrar
 */
@Composable
fun OpcionDashboardItem(opcion: OpcionDashboard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = opcion.onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(opcion.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = opcion.icono,
                    contentDescription = opcion.titulo,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Textos
            Column {
                Text(
                    text = opcion.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = opcion.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
                onNavigateToCrearUsuarioRapido = {}
            )
        }
    }
} 