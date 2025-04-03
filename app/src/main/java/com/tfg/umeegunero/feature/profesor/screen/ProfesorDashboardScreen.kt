package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
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
 * Dashboard principal para profesores del sistema UmeEgunero.
 * 
 * Esta pantalla actúa como centro de operaciones para los profesores, permitiéndoles:
 * 
 * - Visualizar y gestionar los alumnos asignados a sus clases
 * - Acceder rápidamente a la creación de registros diarios de actividad
 * - Monitorear alumnos que requieren atención prioritaria
 * - Comunicarse con los familiares mediante el sistema de chat
 * - Gestionar asistencias, tareas y calendario
 * 
 * La interfaz está organizada en secciones mediante pestañas y tarjetas informativas
 * para facilitar el acceso a la información más relevante. Incluye además un cajón
 * de navegación lateral para acceder a todas las funcionalidades disponibles.
 * 
 * Los datos se cargan dinámicamente desde el ViewModel, que gestiona la comunicación
 * con los repositorios de datos.
 *
 * @param navController Controlador de navegación para la aplicación
 * @param onLogout Callback ejecutado cuando el profesor cierra sesión
 * @param onNavigateToRegistroActividad Callback para navegar a la pantalla de registro de actividad
 * @param onNavigateToDetalleAlumno Callback para navegar a los detalles de un alumno
 * @param onNavigateToChat Callback para navegar al chat con un familiar
 * @param alumnosPendientes Lista de alumnos que requieren atención prioritaria
 * @param alumnos Lista completa de alumnos asignados al profesor
 * @param mensajesNoLeidos Mensajes pendientes de leer, con información del remitente
 * @param totalMensajesNoLeidos Contador total de mensajes no leídos
 * @param isLoading Indicador de carga de datos
 * @param error Mensaje de error, si existe
 * @param selectedTab Índice de la pestaña seleccionada
 * @param onTabSelected Callback cuando se cambia de pestaña
 * @param onCrearRegistroActividad Callback para crear un nuevo registro de actividad
 * @param onErrorDismissed Callback para descartar un error mostrado
 * @param viewModel ViewModel que gestiona los datos y la lógica de negocio
 * 
 * @see ProfesorDashboardViewModel
 * @see RegistroActividadScreen
 * @see AlumnoScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToRegistroActividad: (String) -> Unit = {},
    onNavigateToDetalleAlumno: (String) -> Unit = {},
    onNavigateToChat: (String, String) -> Unit = { _, _ -> },
    alumnosPendientes: List<Alumno> = emptyList(),
    alumnos: List<Alumno> = emptyList(),
    mensajesNoLeidos: List<Triple<String, String, Boolean>> = emptyList(),
    totalMensajesNoLeidos: Int = 0,
    isLoading: Boolean = false,
    error: String? = null,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onCrearRegistroActividad: (String) -> Unit = {},
    onErrorDismissed: () -> Unit = {},
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Mostrar error si existe
    error?.let {
        // Aquí podrías mostrar un Snackbar o un diálogo con el error
        // y llamar a onErrorDismissed cuando se cierre
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Imagen de perfil del profesor
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Laura Martínez",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Profesora • Aula 2B",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                // Elementos del menú
                val menuItems = listOf(
                    "Inicio" to Icons.Default.Home,
                    "Mis Alumnos" to Icons.Default.Person,
                    "Actividad" to Icons.AutoMirrored.Filled.List,
                    "Mensajes" to Icons.AutoMirrored.Filled.Chat,
                    "Configuración" to Icons.Default.Settings
                )

                menuItems.forEachIndexed { index, (title, icon) ->
                    NavigationDrawerItem(
                        label = { Text(text = title) },
                        selected = selectedTab == index,
                        onClick = {
                            onTabSelected(index)
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider()

                // Botón de cerrar sesión
                NavigationDrawerItem(
                    label = { Text(text = "Cerrar Sesión") },
                    selected = false,
                    onClick = onLogout,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión"
                        )
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            when (selectedTab) {
                                0 -> "Panel de Profesor"
                                1 -> "Mis Alumnos"
                                2 -> "Actividad"
                                3 -> "Mensajes"
                                4 -> "Configuración"
                                else -> "Panel de Profesor"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Mostrar notificaciones */ }) {
                            BadgedBox(badge = { 
                                if (totalMensajesNoLeidos > 0) {
                                    Badge { Text(totalMensajesNoLeidos.toString()) }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF34C759), // Verde iOS para profesor
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    val items = listOf(
                        "Inicio" to Icons.Default.Home,
                        "Alumnos" to Icons.Default.Person,
                        "Actividad" to Icons.AutoMirrored.Filled.List,
                        "Mensajes" to Icons.AutoMirrored.Filled.Chat
                    )

                    items.forEachIndexed { index, (title, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (index == 3 && totalMensajesNoLeidos > 0) {
                                    BadgedBox(badge = { Badge { Text(totalMensajesNoLeidos.toString()) } }) {
                                        Icon(icon, contentDescription = title)
                                    }
                                } else {
                                    Icon(icon, contentDescription = title)
                                }
                            },
                            label = { Text(title) },
                            selected = selectedTab == index,
                            onClick = { onTabSelected(index) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (selectedTab == 0 && alumnosPendientes.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { 
                            // Crear nuevo registro para el primer alumno pendiente
                            alumnosPendientes.firstOrNull()?.let { alumno ->
                                onCrearRegistroActividad(alumno.dni)
                            }
                        },
                        containerColor = Color(0xFF34C759)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir registro",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            ProfesorDashboardContent(
                selectedTab = selectedTab,
                paddingValues = paddingValues,
                alumnosPendientes = alumnosPendientes,
                alumnos = alumnos,
                mensajesNoLeidos = mensajesNoLeidos,
                isLoading = isLoading,
                onNavigateToDetalleAlumno = onNavigateToDetalleAlumno,
                onNavigateToChat = onNavigateToChat,
                onCrearRegistroActividad = onCrearRegistroActividad,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ProfesorDashboardContent(
    selectedTab: Int,
    paddingValues: PaddingValues,
    alumnosPendientes: List<Alumno> = emptyList(),
    alumnos: List<Alumno> = emptyList(),
    mensajesNoLeidos: List<Triple<String, String, Boolean>> = emptyList(),
    isLoading: Boolean = false,
    onNavigateToDetalleAlumno: (String) -> Unit = {},
    onNavigateToChat: (String, String) -> Unit = { _, _ -> },
    onCrearRegistroActividad: (String) -> Unit = {},
    navController: NavController,
    viewModel: ProfesorDashboardViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (selectedTab) {
            0 -> ProfesorHomeContent(
                alumnosPendientes = alumnosPendientes,
                onCrearRegistroActividad = onCrearRegistroActividad,
                navController = navController
            )
            1 -> AlumnosTab(
                alumnos = alumnos,
                onNavigateToDetalleAlumno = onNavigateToDetalleAlumno,
                onNavigateToChat = onNavigateToChat,
                onRegistroDiario = { alumno ->
                    // Obtener valores del viewModel
                    val profesorId = viewModel.uiState.value.profesor?.documentId ?: ""
                    val claseId = viewModel.uiState.value.claseActual?.id ?: ""
                    val claseNombre = viewModel.uiState.value.claseActual?.nombre ?: "Sin clase"
                    
                    viewModel.navegarARegistroDiario(
                        navController = navController,
                        alumno = alumno,
                        profesorId = profesorId,
                        claseId = claseId,
                        claseNombre = claseNombre
                    )
                },
                isLoading = isLoading
            )
            2 -> HistorialContent()
            3 -> MensajesContent(
                mensajes = mensajesNoLeidos,
                onNavigateToChat = onNavigateToChat
            )
            4 -> ConfiguracionProfesorContent()
            else -> ProfesorHomeContent(
                alumnosPendientes = alumnosPendientes,
                onCrearRegistroActividad = onCrearRegistroActividad,
                navController = navController
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProfesorHomeContent(
    alumnosPendientes: List<Alumno> = emptyList(),
    onCrearRegistroActividad: (String) -> Unit = {},
    navController: NavController
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")
    val formattedDate = remember { today.format(formatter).replaceFirstChar { it.uppercase() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado con fecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de aula
        Text(
            text = "Tu aula: 2B - Educación Infantil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "15 alumnos a tu cargo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Estado del día
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Estado del día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusItem(
                        count = 15,
                        total = 15,
                        title = "Comidas",
                        icon = Icons.Default.Fastfood
                    )

                    StatusItem(
                        count = 10,
                        total = 15,
                        title = "Siestas",
                        icon = Icons.Default.Check
                    )

                    StatusItem(
                        count = 8,
                        total = 15,
                        title = "Informes",
                        icon = Icons.AutoMirrored.Filled.List
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Acciones rápidas
        Text(
            text = "Acciones rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = rememberLazyGridState(),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(180.dp)
        ) {
            item {
                AccionRapidaItem(
                    title = "Asistencia",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF34C759),
                    onClick = {
                        navController.navigate(AppScreens.AsistenciaProfesor.route)
                    }
                )
            }
            
            item {
                AccionRapidaItem(
                    title = "Tareas",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    color = Color(0xFF007AFF),
                    onClick = {
                        navController.navigate(AppScreens.ProfesorTareas.route)
                    }
                )
            }
            
            item {
                AccionRapidaItem(
                    title = "Comunicación",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    color = Color(0xFF5856D6),
                    onClick = {
                        navController.navigate(AppScreens.ChatProfesor.route)
                    }
                )
            }
            
            item {
                AccionRapidaItem(
                    title = "Actividades Preescolar",
                    icon = Icons.Default.ChildCare,
                    color = Color(0xFF8E24AA),
                    onClick = {
                        navController.navigate(AppScreens.ActividadesPreescolarProfesor.route)
                    }
                )
            }
            
            item {
                AccionRapidaItem(
                    title = "Comedor",
                    icon = Icons.Default.Fastfood,
                    color = Color(0xFFFF9500),
                    onClick = {
                        // Navegar a comedor
                    }
                )
            }
            
            item {
                AccionRapidaItem(
                    title = "Calendario",
                    icon = Icons.Default.CalendarToday,
                    color = Color(0xFFFF2D55),
                    onClick = {
                        navController.navigate(AppScreens.ProfesorCalendario.route)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de alumnos pendientes
        if (alumnosPendientes.isNotEmpty()) {
            Text(
                text = "Alumnos pendientes de registro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                items(alumnosPendientes) { alumno ->
                    AlumnoPendienteItem(
                        nombre = "${alumno.nombre} ${alumno.apellidos}",
                        onClick = { onCrearRegistroActividad(alumno.dni) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun StatusItem(
    count: Int,
    total: Int,
    title: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$count/$total",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AlumnoPendienteItem(
    nombre: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crear registro",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AlumnosTab(
    alumnos: List<Alumno>,
    onNavigateToDetalleAlumno: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onRegistroDiario: (Alumno) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (alumnos.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes alumnos asignados",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Contacta con el administrador del centro para asignar alumnos a tu clase",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(alumnos) { alumno ->
            AlumnoItem(
                alumno = alumno,
                onNavigateToDetalleAlumno = onNavigateToDetalleAlumno,
                onNavigateToChat = onNavigateToChat,
                onRegistroDiario = onRegistroDiario
            )
        }
    }
}

@Composable
fun AlumnoItem(
    alumno: Alumno,
    onNavigateToDetalleAlumno: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onRegistroDiario: (Alumno) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetalleAlumno(alumno.dni) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil (reemplazar con la real cuando esté disponible)
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Foto de ${alumno.nombre}",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Información del alumno
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Edad: ${calcularEdad(alumno.fechaNacimiento)} años",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Acciones
            Row {
                // Botón de chat
                IconButton(onClick = { onNavigateToChat(alumno.dni, "${alumno.nombre} ${alumno.apellidos}") }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat con la familia de ${alumno.nombre}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón de registro diario
                IconButton(onClick = { onRegistroDiario(alumno) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = "Registro diario de ${alumno.nombre}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Actividades",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Selector de fecha
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hoy, ${
                        SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                            .format(Date())
                    }",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Seleccionar fecha",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Lista de actividades
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Actividades de la mañana
            item {
                Text(
                    text = "Mañana",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(3) { index ->
                ActividadItem(
                    hora = when(index) {
                        0 -> "08:30"
                        1 -> "10:15"
                        else -> "11:45"
                    },
                    descripcion = when(index) {
                        0 -> "Llegada y control de asistencia"
                        1 -> "Actividad de lectura en grupo"
                        else -> "Juegos en el patio"
                    },
                    completada = index != 2
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            // Actividades de la tarde
            item {
                Text(
                    text = "Tarde",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(2) { index ->
                ActividadItem(
                    hora = when(index) {
                        0 -> "14:30"
                        else -> "16:00"
                    },
                    descripcion = when(index) {
                        0 -> "Siesta y control de descanso"
                        else -> "Actividades psicomotrices"
                    },
                    completada = index == 0
                )
                if (index < 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ActividadItem(
    hora: String,
    descripcion: String,
    completada: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hora
        Text(
            text = hora,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(50.dp)
        )
        
        // Estado (completada o pendiente)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (completada) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else 
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f), 
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (completada) Icons.Default.Check else Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (completada) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Descripción
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyLarge,
                color = if (completada) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textDecoration = if (completada) TextDecoration.None else TextDecoration.None
            )
            
            if (completada) {
                Text(
                    text = "Completada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun MensajesContent(
    mensajes: List<Triple<String, String, Boolean>> = emptyList(),
    onNavigateToChat: (String, String) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mensajes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de chats
        if (mensajes.isEmpty()) {
            // Mostrar mensaje cuando no hay mensajes
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes mensajes nuevos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(mensajes) { (emisorId, texto, noLeido) ->
                    ChatItem(
                        nombre = emisorId, // Idealmente aquí mostrarías el nombre real del emisor
                        ultimoMensaje = texto,
                        noLeido = noLeido,
                        onClick = { onNavigateToChat(emisorId, "") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    nombre: String,
    ultimoMensaje: String,
    noLeido: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (noLeido) FontWeight.Bold else FontWeight.Medium
            )

            Text(
                text = ultimoMensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (noLeido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (noLeido) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun ConfiguracionProfesorContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Sección de perfil
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Imagen de perfil
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Información del profesor
                    Column {
                        Text(
                            text = "Laura Martínez",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Profesora • Aula 2B",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón para editar perfil
                Button(
                    onClick = { /* Navegar a editar perfil */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34C759)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Editar perfil")
                }
            }
        }
        
        // Opciones de configuración
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Preferencias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Opción 1: Notificaciones
                ConfiguracionItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Configura las alertas que recibes",
                    onClick = { /* Navegar a configuración de notificaciones */ }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Opción 2: Tema
                ConfiguracionItem(
                    icon = Icons.Default.Settings,
                    title = "Tema de la aplicación",
                    subtitle = "Personaliza la apariencia",
                    onClick = { /* Abrir selector de tema */ }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Opción 3: Idioma
                ConfiguracionItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    title = "Idioma",
                    subtitle = "Español",
                    onClick = { /* Abrir selector de idioma */ }
                )
            }
        }
        
        // Ajustes de clase
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ajustes de Clase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Opción 1: Gestión de clase
                ConfiguracionItem(
                    icon = Icons.Default.Person,
                    title = "Gestión de alumnos",
                    subtitle = "Administra los alumnos de tu clase",
                    onClick = { /* Navegar a gestión de alumnos */ }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Opción 2: Plantillas de actividad
                ConfiguracionItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "Plantillas de actividad",
                    subtitle = "Crea y gestiona plantillas para registros",
                    onClick = { /* Navegar a plantillas */ }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Opción 3: Calendario escolar
                ConfiguracionItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Calendario escolar",
                    subtitle = "Ajusta fechas importantes del curso",
                    onClick = { /* Navegar a calendario */ }
                )
            }
        }
        
        // Botón de cerrar sesión
        Button(
            onClick = { /* Cerrar sesión */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar sesión")
        }
    }
}

@Composable
fun ConfiguracionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Flecha
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionProfesorPreviewNew() {
    UmeEguneroTheme {
        ConfiguracionScreen(perfil = PerfilConfiguracion.PROFESOR)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesorDashboardPreview() {
    UmeEguneroTheme {
        ProfesorDashboardScreen(
            navController = rememberNavController(),
            onLogout = {},
            onNavigateToRegistroActividad = {},
            onNavigateToDetalleAlumno = {},
            onNavigateToChat = { _, _ -> },
            alumnosPendientes = emptyList(),
            alumnos = emptyList(),
            mensajesNoLeidos = emptyList(),
            totalMensajesNoLeidos = 0,
            isLoading = false,
            error = null,
            selectedTab = 0
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfesorDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ProfesorDashboardScreen(
            navController = rememberNavController(),
            onLogout = {},
            onNavigateToRegistroActividad = {},
            onNavigateToDetalleAlumno = {},
            onNavigateToChat = { _, _ -> },
            alumnosPendientes = emptyList(),
            alumnos = emptyList(),
            mensajesNoLeidos = emptyList(),
            totalMensajesNoLeidos = 0,
            isLoading = false,
            error = null,
            selectedTab = 0
        )
    }
}

@Composable
fun FuncionalidadCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AccionRapidaItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}