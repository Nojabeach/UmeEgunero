package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.collect

/**
 * Dashboard principal del profesor implementado con Hilt
 * 
 * Esta pantalla actúa como punto de entrada principal para los profesores
 * después de iniciar sesión. Muestra una visión general de todas sus
 * responsabilidades y acceso rápido a las principales funcionalidades.
 * 
 * El diseño está optimizado para proporcionar acceso rápido a:
 * - Gestión de alumnos
 * - Control de asistencia
 * - Comunicación con familias
 * - Gestión de eventos y calendario
 * - Creación de registros de actividad
 *
 * @param navController Controlador de navegación para manejar la navegación entre pantallas
 * @param viewModel ViewModel que gestiona los datos y la lógica de negocio de la pantalla
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiltProfesorDashboardScreen(
    navController: NavController,
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Observamos el estado de navegación
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo("profesor_graph") { inclusive = true }
            }
        }
    }
    
    // Si ocurrió un error, mostramos un Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Actualizar cada vez que la pantalla vuelve a tener el foco
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.cargarMensajesNoLeidos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // También actualizar al montar la pantalla
    LaunchedEffect(key1 = Unit) {
        viewModel.cargarMensajesNoLeidos()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Dashboard Profesor",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Botón de mensajes con badge para notificaciones
                    IconButton(
                        onClick = { 
                            navController.navigate(AppScreens.UnifiedInbox.route) 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Ver mensajes",
                            tint = Color.White
                        )
                    }
                    
                    // Menú de opciones
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi perfil") },
                            onClick = {
                                showMenu = false
                                navController.navigate("perfil")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = {
                                showMenu = false
                                navController.navigate("configuracion")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        ProfesorDashboardContent(
            nombre = uiState.profesor?.nombre ?: "Profesor",
            alumnosPendientes = uiState.alumnosPendientes.size,
            onCrearRegistroActividad = {
                navController.navigate(AppScreens.ListadoPreRegistroDiario.route)
            },
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Contenido principal de la pantalla del profesor
 * 
 * Muestra el contenido estructurado del dashboard con tarjetas de información
 * y acciones rápidas para las principales funcionalidades.
 * 
 * @param nombre Nombre del profesor para personalizar la pantalla
 * @param alumnosPendientes Número de alumnos con pendientes
 * @param onCrearRegistroActividad Acción para crear un nuevo registro de actividad
 * @param navController Controlador de navegación
 * @param modifier Modificador opcional para personalizar el layout
 */
@Composable
fun ProfesorDashboardContent(
    nombre: String,
    alumnosPendientes: Int,
    onCrearRegistroActividad: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Saludo personalizado
        Text(
            text = "¡Hola, $nombre!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Bienvenido a tu panel de control",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tarjetas de información - Primera fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Alumnos Pendientes",
                value = alumnosPendientes.toString(),
                icon = Icons.Default.Person,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.AsistenciaProfesor.route)
                    }
            )
            
            InfoCard(
                title = "Asistencia",
                value = "Pendiente",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.AsistenciaProfesor.route)
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Segunda fila de tarjetas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Eventos",
                value = "Próximamente",
                icon = Icons.Default.CalendarToday,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(AppScreens.CalendarioProfesor.route)
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Acciones rápidas
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Registrar Asistencia",
                icon = Icons.Default.CheckCircle,
                onClick = {
                    navController.navigate(AppScreens.AsistenciaProfesor.route)
                }
            )
            
            ActionButton(
                text = "Enviar Mensaje a Padres",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                    navController.navigate(AppScreens.UnifiedInbox.route)
                }
            )
            
            ActionButton(
                text = "Registro Diario",
                icon = Icons.Default.Assignment,
                onClick = onCrearRegistroActividad
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Próximos eventos (placeholder para futuras implementaciones)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Próximos Eventos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "No hay eventos programados para hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = { navController.navigate(AppScreens.CalendarioProfesor.route) }
                ) {
                    Text("Ver calendario completo")
                }
            }
        }
    }
}

/**
 * Tarjeta de información con título, valor e icono
 * 
 * Componente reutilizable para mostrar estadísticas o información
 * clave en formato visual destacado.
 * 
 * @param title Título descriptivo de la información
 * @param value Valor numérico o texto corto a destacar
 * @param icon Icono representativo
 * @param color Color temático para el icono y fondo
 * @param modifier Modificador opcional para personalizar el componente
 */
@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Botón de acción con texto e icono
 * 
 * Componente para acciones principales en el dashboard,
 * con diseño destacado para facilitar la interacción.
 * 
 * @param text Texto descriptivo de la acción
 * @param icon Icono representativo
 * @param onClick Acción a ejecutar al hacer clic
 */
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
