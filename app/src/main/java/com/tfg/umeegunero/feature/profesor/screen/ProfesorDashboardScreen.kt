package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.ui.components.TemaSelector
import com.tfg.umeegunero.ui.components.TemaActual
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import androidx.navigation.compose.rememberNavController
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.tfg.umeegunero.ui.theme.ProfesorColor
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ExitToApp

/**
 * Calcula la edad en años a partir de una fecha de nacimiento en formato dd/MM/yyyy
 * 
 * @param fechaNacimiento Fecha de nacimiento en formato dd/MM/yyyy
 * @return Edad en años, o 0 si la fecha no es válida
 */
fun calcularEdad(fechaNacimiento: String?): Int {
    if (fechaNacimiento.isNullOrEmpty()) return 0
    
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    try {
        val fechaNac = formatoFecha.parse(fechaNacimiento) ?: return 0
        val hoy = Calendar.getInstance()
        val fechaNacCal = Calendar.getInstance().apply { time = fechaNac }
        
        var edad = hoy.get(Calendar.YEAR) - fechaNacCal.get(Calendar.YEAR)
        
        // Ajustar si todavía no ha cumplido años este año
        if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacCal.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        
        return edad
    } catch (e: Exception) {
        return 0
    }
}

/**
 * Pantalla principal del dashboard para profesores.
 * 
 * Proporciona acceso rápido a todas las funcionalidades importantes del sistema para
 * los profesores: gestión de alumnos, comunicados, incidencias, actividades, calendario
 * y comunicación con familias.
 *
 * @param navController Controlador de navegación para moverse entre pantallas
 * @param viewModel ViewModel que gestiona los datos y lógica del dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDashboardScreen(
    navController: NavController,
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showContent = true
    }
    // Efecto para navegar a la pantalla de bienvenida tras logout
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "UmeEgunero - Profesor",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White
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
                    // Botón de logout
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ),
            exit = fadeOut()
        ) {
            ProfesorDashboardContent(
                alumnosPendientes = uiState.alumnosPendientes,
                onCrearRegistroActividad = { dni ->
                    navController.navigate("${AppScreens.RegistroActividad.route}/$dni")
                },
                navController = navController,
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Contenido principal del Dashboard de Profesor
 * 
 * Esta composable organiza todos los elementos visuales del dashboard:
 * - Tarjeta de bienvenida con información del día
 * - Accesos directos a funcionalidades principales
 * - Listado de alumnos pendientes de registro
 * - Próximas actividades y eventos
 *
 * @param alumnosPendientes Lista de alumnos sin registro para el día actual
 * @param onCrearRegistroActividad Callback para crear un nuevo registro de actividad
 * @param navController Controlador de navegación
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun ProfesorDashboardContent(
    alumnosPendientes: List<Alumno>,
    onCrearRegistroActividad: (String) -> Unit,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bienvenida y fecha
        WelcomeCard(
            navController = navController,
            onCrearRegistroActividad = onCrearRegistroActividad
        )
        
        // Grid de accesos rápidos principales
        Text(
            text = "Accesos Rápidos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(240.dp),
            userScrollEnabled = false
        ) {
            // Comunicados
            item {
                DashboardMenuItem(
                    title = "Comunicados",
                    icon = Icons.Default.Description,
                    color = Color(0xFF4CAF50),
                    onClick = {
                        // Navegar a la pantalla de comunicados (admin o común)
                        navController.navigate(AppScreens.ComunicadosCirculares.route)
                    }
                )
            }
            
            // Incidencias
            item {
                DashboardMenuItem(
                    title = "Incidencias",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFFF9800),
                    onClick = {
                        // TODO: Implementar navegación a pantalla de incidencias cuando esté disponible
                    }
                )
            }
            
            // Asistencia
            item {
                DashboardMenuItem(
                    title = "Asistencia",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF2196F3),
                    onClick = {
                        navController.navigate(AppScreens.AsistenciaProfesor.route)
                    }
                )
            }
            
            // Chat con familias
            item {
                DashboardMenuItem(
                    title = "Chat",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    color = Color(0xFF9C27B0),
                    onClick = {
                        navController.navigate(AppScreens.ConversacionesProfesor.route)
                    }
                )
            }
            
            // Calendario
            item {
                DashboardMenuItem(
                    title = "Calendario",
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFF3F51B5),
                    onClick = {
                        navController.navigate(AppScreens.Calendario.route)
                    }
                )
            }
            
            // Actividades
            item {
                DashboardMenuItem(
                    title = "Actividades",
                    icon = Icons.Default.PlayCircle,
                    color = Color(0xFFE91E63),
                    onClick = {
                        navController.navigate(AppScreens.RegistroActividad.route)
                    }
                )
            }
            
            // Actividades Preescolares
            item {
                DashboardMenuItem(
                    title = "Actividades Preescolares",
                    icon = Icons.Default.DirectionsRun,
                    color = Color(0xFF009688),
                    onClick = {
                        // Navegar a la pantalla de actividades preescolares
                        navController.navigate("actividades_preescolar_profesor/{profesorId}/{profesorNombre}")
                    }
                )
            }
        }
        
        // Segunda fila de funcionalidades
        Text(
            text = "Gestión Académica",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gestión de Clases
            GestionCard(
                title = "Mis Clases",
                icon = Icons.Default.School,
                color = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f),
                onClick = {
                    // Reemplazar o eliminar la navegación a DetalleClase
                }
            )
            
            // Evaluación
            GestionCard(
                title = "Evaluación",
                icon = Icons.Default.PieChart,
                color = Color(0xFF673AB7),
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate(AppScreens.Evaluacion.route)
                }
            )
        }
        
        // Alumnos que requieren atención
        AlumnosPendientesResumen(alumnosPendientes)
        
        // Espaciado adicional al final
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Tarjeta de bienvenida para el profesor con información del día
 */
@Composable
fun WelcomeCard(
    modifier: Modifier = Modifier,
    navController: NavController,
    onCrearRegistroActividad: (String) -> Unit
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")
    val formattedDate = remember { today.format(formatter).replaceFirstChar { it.uppercase() } }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ProfesorColor.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = ProfesorColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de aula
            Text(
                text = "2B - Educación Infantil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "15 alumnos a tu cargo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Estadísticas rápidas
                EstadisticaItem(
                    valor = "93%",
                    titulo = "Asistencia",
                    color = Color(0xFF4CAF50),
                    onClick = { navController.navigate(AppScreens.AsistenciaProfesor.route) }
                )
                
                // Separador vertical
                HorizontalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                
                EstadisticaItem(
                    valor = "87%",
                    titulo = "Actividades",
                    color = Color(0xFF2196F3),
                    onClick = { navController.navigate(AppScreens.RegistroActividad.route) }
                )
                
                HorizontalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                
                EstadisticaItem(
                    valor = "5",
                    titulo = "Pendientes",
                    color = Color(0xFFFF9800),
                    onClick = { onCrearRegistroActividad("") }
                )
            }
        }
    }
}

/**
 * Elemento de menú para el dashboard
 */
@Composable
fun DashboardMenuItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(color.copy(alpha = 0.1f))
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Tarjeta para secciones de gestión principal
 */
@Composable
fun GestionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Elemento de estadística simple
 */
@Composable
fun EstadisticaItem(
    valor: String,
    titulo: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlumnosPendientesResumen(
    alumnosPendientes: List<Alumno>
) {
    if (alumnosPendientes.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alumnos sin registro hoy: ${alumnosPendientes.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* Navegar a la lista de pendientes */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ver")
                }
            }
        }
    }
}

@Composable
fun GuardarRegistroButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardando...")
        } else {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar registro")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesorDashboardPreview() {
    UmeEguneroTheme {
        ProfesorDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfesorDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ProfesorDashboardScreen(
            navController = rememberNavController()
        )
    }
}