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
 * @version 2.0
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
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")) 
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
            TopAppBar(
                title = {
                    Text(
                        text = "Panel de Centro",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                        navController.navigate(AppScreens.VinculacionFamiliar.route) 
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
        
        // Título para sección de gestión
        Text(
            text = "Gestión del Centro",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        
        // Rejilla de opciones principales
        OpcionesGrid(
            onNavigateToGestionProfesores = onNavigateToGestionProfesores,
            onNavigateToGestionCursosYClases = onNavigateToGestionCursosYClases,
            onNavigateToVinculacionFamiliar = onNavigateToVinculacionFamiliar,
            onNavigateToCalendario = onNavigateToCalendario,
            onNavigateToNotificaciones = onNavigateToNotificaciones,
            onNavigateToAddAlumno = onNavigateToAddAlumno
        )
        
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = nombreCentro,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
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
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Resumen del Centro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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
                
                EstadisticaItem(
                    valor = numAlumnos.toString(),
                    etiqueta = "Alumnos",
                    icono = Icons.Default.Face,
                    color = MaterialTheme.colorScheme.secondary
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
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Rejilla de opciones principales del dashboard de centro.
 * 
 * @param onNavigateToGestionProfesores Acción para navegar a gestión de profesores
 * @param onNavigateToGestionCursosYClases Acción para navegar a gestión de cursos y clases
 * @param onNavigateToVinculacionFamiliar Acción para navegar a vinculación familiar
 * @param onNavigateToCalendario Acción para navegar al calendario
 * @param onNavigateToNotificaciones Acción para navegar a notificaciones
 * @param onNavigateToAddAlumno Acción para navegar a añadir alumno
 */
@Composable
fun OpcionesGrid(
    onNavigateToGestionProfesores: () -> Unit,
    onNavigateToGestionCursosYClases: () -> Unit,
    onNavigateToVinculacionFamiliar: () -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToAddAlumno: () -> Unit
) {
    val opciones = listOf(
        OpcionDashboard(
            id = "cursos",
            titulo = "Cursos y Clases",
            descripcion = "Gestión académica",
            icono = Icons.Default.School,
            color = MaterialTheme.colorScheme.primary,
            onClick = onNavigateToGestionCursosYClases
        ),
        OpcionDashboard(
            id = "profesores",
            titulo = "Profesorado",
            descripcion = "Gestión docente",
            icono = Icons.Default.SupervisorAccount,
            color = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToGestionProfesores
        ),
        OpcionDashboard(
            id = "alumnos",
            titulo = "Alumnado",
            descripcion = "Nuevo alumno",
            icono = Icons.Default.PersonAdd,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToAddAlumno
        ),
        OpcionDashboard(
            id = "familias",
            titulo = "Familias",
            descripcion = "Vinculación",
            icono = Icons.Default.Group,
            color = MaterialTheme.colorScheme.error,
            onClick = onNavigateToVinculacionFamiliar
        ),
        OpcionDashboard(
            id = "calendario",
            titulo = "Calendario",
            descripcion = "Eventos y plazos",
            icono = Icons.Default.DateRange,
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onNavigateToCalendario
        ),
        OpcionDashboard(
            id = "notificaciones",
            titulo = "Notificaciones",
            descripcion = "Comunicaciones",
            icono = Icons.Default.Notifications,
            color = MaterialTheme.colorScheme.secondary,
            onClick = onNavigateToNotificaciones
        )
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false,
        modifier = Modifier.height(350.dp) // Altura fija para acomodar 3 filas de 2 elementos
    ) {
        items(opciones) { opcion ->
            OpcionDashboardItem(opcion = opcion)
        }
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
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
                    .background(opcion.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = opcion.icono,
                    contentDescription = opcion.titulo,
                    tint = opcion.color,
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
                onNavigateToAddAlumno = {}
            )
        }
    }
} 