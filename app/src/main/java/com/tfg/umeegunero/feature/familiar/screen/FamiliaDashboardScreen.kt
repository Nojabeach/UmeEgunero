package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.feature.familiar.viewmodel.FamiliarDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.StatsOverviewCard
import com.tfg.umeegunero.ui.components.StatItem
import com.tfg.umeegunero.ui.components.StatsOverviewRow
import com.tfg.umeegunero.ui.components.charts.LineChart
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring

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
 * @author Equipo UmeEgunero
 * @version 3.0
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
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
            .format(Date())
            .replaceFirstChar { it.uppercase() }
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
                    Column {
                        Text(
                            text = uiState.familiar?.nombre?.let { "Hola, $it" } ?: "Familia", 
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold
                        ) 
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White
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
                                tint = Color.White
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
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Configuración
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.Configuracion.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Color.White
                        )
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
                    CircularProgressIndicator(color = FamiliarColor)
                }
            } 
            // Mostrar contenido principal con animación
            else {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow
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
                            // La pantalla de asistencia para familia aún no está implementada
                            // pero podemos navegar a la correcta cuando esté disponible
                            navController.navigate(AppScreens.TareasFamilia.route)
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
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Sección: Selección de hijos
        SelectorHijos(
            hijos = hijos,
            hijoActual = hijoActual,
            onSelectHijo = onSelectHijo
        )
        
        // Sección: Métricas destacadas - Solo si hay un hijo seleccionado
        hijoActual?.let { hijo ->
            Text(
                text = "Métricas de ${hijo.nombre}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Estadísticas clave
            MetricasCard(hijo = hijo, registrosActividad = registrosActividad)
            
            // Sección: Último registro diario
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
                        onClick = { onVerTodosRegistros(hijo) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = FamiliarColor
                        )
                    ) {
                        Text("Ver todos")
                    }
                }
                
                if (registrosActividad.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
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
        
        // Sección: Acciones rápidas
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Opciones en grid
        AccionesGrid(
            onCalendarioClick = onNavigateToCalendario,
            onChatClick = onNavigateToChat,
            onTareasClick = onNavigateToTareas,
            onActividadesClick = onNavigateToActividades
        )
    }
}

/**
 * Tarjeta de métricas con estadísticas clave del alumno.
 */
@Composable
fun MetricasCard(
    hijo: Alumno,
    registrosActividad: List<RegistroActividad>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Métricas en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

/**
 * Rejilla de acciones rápidas para el dashboard familiar.
 */
@Composable
fun AccionesGrid(
    onCalendarioClick: () -> Unit,
    onChatClick: () -> Unit,
    onTareasClick: () -> Unit,
    onActividadesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        item {
            AccionFamiliaItem(
                titulo = "Calendario",
                descripcion = "Eventos escolares",
                icono = Icons.Default.CalendarToday,
                color = MaterialTheme.colorScheme.primary,
                onClick = onCalendarioClick
            )
        }
        
        item {
            AccionFamiliaItem(
                titulo = "Chat",
                descripcion = "Comunicación",
                icono = Icons.AutoMirrored.Filled.Chat,
                color = MaterialTheme.colorScheme.secondary,
                onClick = onChatClick
            )
        }
        
        item {
            AccionFamiliaItem(
                titulo = "Tareas",
                descripcion = "Seguimiento",
                icono = Icons.AutoMirrored.Filled.Assignment,
                color = MaterialTheme.colorScheme.tertiary,
                onClick = onTareasClick
            )
        }
        
        item {
            AccionFamiliaItem(
                titulo = "Actividades",
                descripcion = "Preescolar",
                icono = Icons.Default.ChildCare,
                color = Color(0xFF8E24AA), // Morado
                onClick = onActividadesClick
            )
        }
    }
}

/**
 * Selector de hijos para el dashboard familiar.
 */
@Composable
fun SelectorHijos(
    hijos: List<Alumno>,
    hijoActual: Alumno?,
    onSelectHijo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FamiliarColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tus hijos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FamiliarColor
            )
            
            if (hijos.isEmpty()) {
                Text(
                    text = "No hay hijos asociados a tu cuenta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(hijos) { hijo ->
                        HijoChip(
                            nombre = hijo.nombre,
                            isSelected = hijo.id == hijoActual?.id,
                            onClick = { onSelectHijo(hijo.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chip para seleccionar un hijo.
 */
@Composable
fun HijoChip(
    nombre: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) FamiliarColor else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Ítem individual de una estadística.
 */
@Composable
fun EstadisticaItem(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tarjeta que muestra un registro diario resumido.
 */
@Composable
fun RegistroDiarioCard(
    registro: RegistroActividad,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FamiliarColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = registro.fecha?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.toDate())
                    } ?: "Fecha desconocida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividades: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = registro.actividades?.titulo ?: "Sin actividades",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comida: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = when {
                            registro.primerPlato == EstadoComida.COMPLETO -> "Completa"
                            registro.primerPlato == EstadoComida.PARCIAL -> "Parcial"
                            registro.primerPlato == EstadoComida.RECHAZADO -> "No ha comido"
                            else -> "Sin registro"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Ítem de acción para el dashboard familiar.
 */
@Composable
fun AccionFamiliaItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Función para generar estadísticas de un alumno basado en sus registros.
 */
private fun generarEstadisticas(
    alumno: Alumno,
    registros: List<RegistroActividad>
): List<Estadistica> {
    // Calcular asistencia (porcentaje de días asistidos)
    val asistencia = "90%"
    
    // Calcular tareas completadas
    val tareasCompletadas = "8/10"
    
    // Calcular comidas completas
    val comidasCompletadas = registros
        .count { 
            it.primerPlato == EstadoComida.COMPLETO && 
            it.segundoPlato == EstadoComida.COMPLETO 
        }
        .toString()
    
    return listOf(
        Estadistica(asistencia, "Asistencia"),
        Estadistica(tareasCompletadas, "Tareas"),
        Estadistica(comidasCompletadas, "Comidas")
    )
}

/**
 * Preview del Dashboard Familiar
 */
@Preview(showBackground = true)
@Composable
fun FamiliaDashboardPreview() {
    UmeEguneroTheme {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
} 