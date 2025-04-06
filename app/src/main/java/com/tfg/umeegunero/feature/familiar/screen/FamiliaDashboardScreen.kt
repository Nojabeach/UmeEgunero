package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Feed
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
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.StatsOverviewCard
import com.tfg.umeegunero.ui.components.StatItem
import com.tfg.umeegunero.ui.components.StatsOverviewRow
import com.tfg.umeegunero.ui.components.charts.LineChart
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

/**
 * Clase que representa una estadística para mostrar en el dashboard
 */
data class Estadistica(
    val value: String,
    val etiqueta: String
)

/**
 * Pantalla principal del dashboard para familiares
 * 
 * Esta pantalla muestra toda la información relevante para los familiares de los alumnos:
 * - Lista de hijos asociados a la cuenta
 * - Resumen de registros diarios recientes
 * - Métricas de actividad y progreso
 * - Acceso rápido a funcionalidades principales
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona los datos del dashboard
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaDashboardScreen(
    navController: NavController,
    viewModel: FamiliarDashboardViewModel = hiltViewModel()
) {
    // Recoger el estado desde el ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Variables para control de animaciones
    var showContent by remember { mutableStateOf(false) }
    val currentDate = remember { 
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date()) 
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.familiar?.nombre?.let { "Hola, $it" } ?: "Familia", 
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onTertiary
                ),
                actions = {
                    // Notificaciones con badge para registros sin leer
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.NotificacionesFamilia.route)
                    }) {
                        BadgedBox(
                            badge = {
                                if (uiState.registrosSinLeer > 0) {
                                    Badge { 
                                        Text(
                                            text = if (uiState.registrosSinLeer > 9) "9+" else "${uiState.registrosSinLeer}"
                                        ) 
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                    
                    // Mensajes con badge
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.ConversacionesFamilia.route)
                    }) {
                        BadgedBox(
                            badge = {
                                if (uiState.totalMensajesNoLeidos > 0) {
                                    Badge { 
                                        Text(
                                            text = if (uiState.totalMensajesNoLeidos > 9) "9+" else "${uiState.totalMensajesNoLeidos}"
                                        ) 
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Mensajes",
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar mensaje de carga mientras se obtienen los datos
            if (uiState.isLoading && uiState.hijos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } 
            // Mostrar contenido principal con animación
            else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        )
                    ),
                    exit = fadeOut()
                ) {
                    FamiliaDashboardContent(
                        hijos = uiState.hijos,
                        hijoActual = uiState.hijoSeleccionado,
                        registrosActividad = uiState.registrosActividad,
                        currentDate = currentDate,
                        onSelectHijo = { viewModel.seleccionarHijo(it) },
                        onVerTodosRegistros = { hijo ->
                            viewModel.navegarAConsultaRegistroDiario(navController, hijo)
                        },
                        onNavigateToRegistro = { hijo ->
                            viewModel.navegarAConsultaRegistroDiario(navController, hijo)
                        },
                        onNavigateToCalendario = {
                            navController.navigate(AppScreens.CalendarioFamilia.route)
                        },
                        onNavigateToChat = {
                            navController.navigate(AppScreens.ConversacionesFamilia.route)
                        },
                        onNavigateToTareas = {
                            navController.navigate(AppScreens.TareasFamilia.route)
                        },
                        onNavigateToActividades = {
                            navController.navigate(AppScreens.ActividadesPreescolar.route)
                        },
                        onNavigateToAsistencia = {
                            navController.navigate(AppScreens.Dummy.createRoute("Asistencia"))
                        },
                        onNavigateToConfiguracion = {
                            navController.navigate(AppScreens.Configuracion.route)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Contenido principal del Dashboard Familiar con tarjetas informativas y opciones de gestión.
 */
@Composable
private fun FamiliaDashboardContent(
    hijos: List<Alumno>,
    hijoActual: Alumno?,
    registrosActividad: List<RegistroActividad>,
    currentDate: String,
    onSelectHijo: (String) -> Unit,
    onVerTodosRegistros: (Alumno) -> Unit,
    onNavigateToRegistro: (Alumno) -> Unit,
    onNavigateToCalendario: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToTareas: () -> Unit,
    onNavigateToActividades: () -> Unit,
    onNavigateToAsistencia: () -> Unit,
    onNavigateToConfiguracion: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de fecha y bienvenida
        item {
            WelcomeCard(
                nombreHijo = hijoActual?.nombre ?: "Tu familia",
                currentDate = currentDate
            )
        }
        
        // Sección: Selección de hijos
        item {
            SelectorHijos(
                hijos = hijos,
                hijoActual = hijoActual,
                onSelectHijo = onSelectHijo
            )
        }
        
        // Sección: Métricas destacadas - Solo si hay un hijo seleccionado
        hijoActual?.let { hijo ->
            item {
                Text(
                    text = "Métricas de ${hijo.nombre}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Estadísticas clave
                val stats = generarEstadisticas(hijo, registrosActividad)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Mostramos cada estadística individualmente
                            val estadisticas = generarEstadisticas(hijo, registrosActividad)
                            estadisticas.forEach { stat ->
                                EstadisticaItem(
                                    valor = stat.value,
                                    etiqueta = stat.etiqueta,
                                    icono = when(stat.etiqueta) {
                                        "Asistencia" -> Icons.Default.EventAvailable
                                        "Tareas" -> Icons.AutoMirrored.Filled.Assignment
                                        "Comidas" -> Icons.Default.Restaurant
                                        else -> Icons.Default.Star
                                    },
                                    color = when(stat.etiqueta) {
                                        "Asistencia" -> MaterialTheme.colorScheme.primary
                                        "Tareas" -> MaterialTheme.colorScheme.secondary
                                        "Comidas" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Sección: Último registro diario
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Últimos registros",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = { onVerTodosRegistros(hijo) }
                        ) {
                            Text("Ver todos")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (registrosActividad.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay registros disponibles todavía",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Mostrar los últimos 3 registros o menos con estilo renovado
                        registrosActividad.take(3).forEach { registro ->
                            RegistroDiarioCard(
                                registro = registro,
                                onClick = { onNavigateToRegistro(hijo) },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Sección: Acciones rápidas
        item {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Opciones en grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                item {
                    AccionFamiliaItem(
                        titulo = "Calendario",
                        descripcion = "Eventos escolares",
                        icono = Icons.Default.CalendarToday,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToCalendario
                    )
                }
                
                item {
                    AccionFamiliaItem(
                        titulo = "Chat",
                        descripcion = "Comunicación",
                        icono = Icons.AutoMirrored.Filled.Chat,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = onNavigateToChat
                    )
                }
                
                item {
                    AccionFamiliaItem(
                        titulo = "Tareas",
                        descripcion = "Seguimiento",
                        icono = Icons.AutoMirrored.Filled.Assignment,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = onNavigateToTareas
                    )
                }
                
                item {
                    AccionFamiliaItem(
                        titulo = "Actividades",
                        descripcion = "Preescolar",
                        icono = Icons.Default.ChildCare,
                        color = Color(0xFF8E24AA), // Morado
                        onClick = onNavigateToActividades
                    )
                }
            }
        }
        
        // Espacio adicional al final
        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

/**
 * Tarjeta de bienvenida con el nombre del hijo y fecha actual.
 */
@Composable
fun WelcomeCard(
    nombreHijo: String,
    currentDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                    text = nombreHijo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            
            // Icono decorativo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Componente para mostrar una estadística individual
 */
@Composable
fun EstadisticaItem(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
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
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = etiqueta,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Tarjeta para mostrar un registro diario.
 */
@Composable
fun RegistroDiarioCard(
    registro: RegistroActividad,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Formatear la fecha para visualización
    val fecha = remember(registro.fecha) {
        val timestamp = registro.fecha
        val date = timestamp.toDate()
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono de la actividad
            Icon(
                imageVector = Icons.Default.Feed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            // Información del registro
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Profesor: ${registro.profesorNombre ?: "No especificado"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Verificamos si hay observaciones disponibles
                if (registro.observaciones != null && registro.observaciones.isNotBlank()) {
                    Text(
                        text = registro.observaciones,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else if (registro.observacionesGenerales.isNotBlank()) {
                    Text(
                        text = registro.observacionesGenerales,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Indicador de no leído
            if (!registro.vistoPorFamiliar && !registro.visualizadoPorFamiliar) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}

/**
 * Elemento para acción rápida en el dashboard de familia.
 */
@Composable
fun AccionFamiliaItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
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
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Textos
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = descripcion,
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
 * Componente para seleccionar entre los hijos del familiar
 */
@Composable
fun SelectorHijos(
    hijos: List<Alumno>,
    hijoActual: Alumno?,
    onSelectHijo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (hijos.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay alumnos asociados a tu cuenta",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Mis Estudiantes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hijos) { hijo ->
                AlumnoPerfilItem(
                    alumno = hijo,
                    isSelected = hijo.dni == hijoActual?.dni,
                    onClick = { onSelectHijo(hijo.dni) }
                )
            }
        }
    }
}

/**
 * Componente que muestra la foto de perfil y nombre del alumno
 */
@Composable
fun AlumnoPerfilItem(
    alumno: Alumno,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(90.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Avatar con borde si está seleccionado
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.first().toString().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Indicador de notificaciones - Usar un Box regular en lugar de AnimatedVisibility
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(4.dp)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Nombre del alumno
        Text(
            text = alumno.nombre,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Función que genera las estadísticas a partir de los registros
 */
@Composable
fun generarEstadisticas(
    alumno: Alumno,
    registros: List<RegistroActividad>
): List<Estadistica> {
    val totalRegistros = registros.size
    
    // Registros de hoy
    val formatoFecha = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val hoy = formatoFecha.format(Date())
    val registrosHoy = registros.count { 
        val fecha = it.fecha.toDate()
        formatoFecha.format(fecha) == hoy 
    }
    
    // Porcentaje de comidas completas
    val comidasTotales = registros.count { 
        it.primerPlato != EstadoComida.NO_SERVIDO || 
        it.segundoPlato != EstadoComida.NO_SERVIDO || 
        it.postre != EstadoComida.NO_SERVIDO 
    }
    val comidasCompletas = registros.count { 
        it.primerPlato == EstadoComida.COMPLETO || 
        it.segundoPlato == EstadoComida.COMPLETO || 
        it.postre == EstadoComida.COMPLETO 
    }
    val porcentajeComidas = if (comidasTotales > 0) {
        (comidasCompletas * 100 / comidasTotales).toString() + "%"
    } else {
        "N/A"
    }
    
    return listOf(
        Estadistica(
            value = totalRegistros.toString(),
            etiqueta = "Asistencia"
        ),
        Estadistica(
            value = registrosHoy.toString(),
            etiqueta = "Tareas"
        ),
        Estadistica(
            value = porcentajeComidas,
            etiqueta = "Comidas"
        )
    )
}

/**
 * Función para obtener un resumen textual de las comidas
 */
private fun obtenerResumenComida(registro: RegistroActividad): String {
    val elementos = mutableListOf<String>()
    
    if (registro.primerPlato == EstadoComida.COMPLETO) elementos.add("1er plato ✓")
    else if (registro.primerPlato == EstadoComida.PARCIAL) elementos.add("1er plato ±")
    
    if (registro.segundoPlato == EstadoComida.COMPLETO) elementos.add("2º plato ✓")
    else if (registro.segundoPlato == EstadoComida.PARCIAL) elementos.add("2º plato ±")
    
    if (registro.postre == EstadoComida.COMPLETO) elementos.add("Postre ✓")
    else if (registro.postre == EstadoComida.PARCIAL) elementos.add("Postre ±")
    
    return elementos.joinToString(" · ")
}

@Preview(showBackground = true)
@Composable
fun FamiliaDashboardScreenPreview() {
    UmeEguneroTheme {
        // Creamos un NavController para la preview
        val navController = rememberNavController()
        
        // Renderizamos la pantalla con datos de prueba
        FamiliaDashboardScreen(
            navController = navController
        )
    }
} 