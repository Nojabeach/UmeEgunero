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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaDashboardScreen(
    navController: NavController,
    viewModel: FamiliarDashboardViewModel = hiltViewModel()
) {
    // Recoger el estado desde el ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado para controlar las animaciones
    var isDataLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.hijos) {
        // Marcamos como cargado si tenemos hijos
        if (uiState.hijos.isNotEmpty()) {
            isDataLoaded = true
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
                                tint = MaterialTheme.colorScheme.onPrimary
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
                                imageVector = Icons.Default.Email,
                                contentDescription = "Mensajes",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Perfil
                    IconButton(onClick = { 
                        navController.navigate(AppScreens.Perfil.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar mensaje de carga mientras se obtienen los datos
            if (uiState.isLoading && !isDataLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Mostrar mensaje de error si hay algún problema
            else if (uiState.error != null && uiState.hijos.isEmpty() && !isDataLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarDatosFamiliar() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            // Mostrar contenido principal cuando esté disponible
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección: Selección de hijos
                    item {
                        SelectorHijos(
                            hijos = uiState.hijos,
                            hijoActual = uiState.hijoSeleccionado,
                            onSelectHijo = { viewModel.seleccionarHijo(it) }
                        )
                    }
                    
                    // Sección: Métricas destacadas - Solo si hay un hijo seleccionado
                    uiState.hijoSeleccionado?.let { hijo ->
                        item {
                            Text(
                                text = "Métricas de ${hijo.nombre}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Estadísticas clave
                            val stats = generarEstadisticas(hijo, uiState.registrosActividad)
                            StatsOverviewRow(
                                stats = stats
                            )
                        }
                        
                        // Sección: Último registro diario
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                                            onClick = { 
                                                viewModel.navegarAConsultaRegistroDiario(
                                                    navController, 
                                                    hijo
                                                ) 
                                            }
                                        ) {
                                            Text("Ver todos")
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    if (uiState.registrosActividad.isEmpty()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
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
                                        // Mostrar los últimos 3 registros o menos
                                        uiState.registrosActividad.take(3).forEach { registro ->
                                            RegistroDiarioResumen(
                                                registro = registro,
                                                onClick = {
                                                    viewModel.navegarAConsultaRegistroDiario(
                                                        navController,
                                                        hijo
                                                    )
                                                },
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
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
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            // Calendario
                            item {
                                AccionRapidaCard(
                                    title = "Calendario",
                                    icon = Icons.Default.CalendarToday,
                                    color = MaterialTheme.colorScheme.primary,
                                    onClick = {
                                        navController.navigate(AppScreens.CalendarioFamilia.route)
                                    }
                                )
                            }
                            
                            // Chat
                            item {
                                AccionRapidaCard(
                                    title = "Chat",
                                    icon = Icons.AutoMirrored.Filled.Chat,
                                    color = MaterialTheme.colorScheme.secondary,
                                    onClick = {
                                        navController.navigate(AppScreens.ConversacionesFamilia.route)
                                    }
                                )
                            }
                            
                            // Tareas
                            item {
                                AccionRapidaCard(
                                    title = "Tareas",
                                    icon = Icons.AutoMirrored.Filled.Assignment,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    onClick = {
                                        navController.navigate(AppScreens.TareasFamilia.route)
                                    }
                                )
                            }
                            
                            // Actividades Preescolar
                            item {
                                AccionRapidaCard(
                                    title = "Actividades",
                                    icon = Icons.Default.ChildCare,
                                    color = Color(0xFF8E24AA), // Morado
                                    onClick = {
                                        navController.navigate(AppScreens.ActividadesPreescolar.route)
                                    }
                                )
                            }
                            
                            // Asistencia
                            item {
                                AccionRapidaCard(
                                    title = "Asistencia",
                                    icon = Icons.Default.EventAvailable,
                                    color = Color(0xFF00897B), // Verde azulado
                                    onClick = {
                                        // Pendiente de implementar
                                        navController.navigate(AppScreens.Dummy.createRoute("Asistencia"))
                                    }
                                )
                            }
                            
                            // Configuración
                            item {
                                AccionRapidaCard(
                                    title = "Ajustes",
                                    icon = Icons.Default.Settings,
                                    color = Color(0xFF546E7A), // Gris azulado
                                    onClick = {
                                        navController.navigate(AppScreens.Configuracion.route)
                                    }
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
            
            // Mostrar mensaje de error si surge durante la operación
            if (uiState.error != null && isDataLoaded) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(uiState.error!!)
                }
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
 * Componente que muestra un resumen del registro diario
 */
@Composable
fun RegistroDiarioResumen(
    registro: RegistroActividad,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!registro.visualizadoPorFamiliar) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de registro
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Feed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Fecha del registro
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(registro.fecha.toDate()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Resumen de comidas
                val resumenComida = obtenerResumenComida(registro)
                if (resumenComida.isNotEmpty()) {
                    Text(
                        text = resumenComida,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Si hay observaciones generales, mostrarlas
                if (registro.observacionesGenerales.isNotEmpty()) {
                    Text(
                        text = registro.observacionesGenerales,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Indicador de nuevo
            if (!registro.visualizadoPorFamiliar) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

/**
 * Componente para tarjetas de acciones rápidas
 */
@Composable
fun AccionRapidaCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Función que genera las estadísticas a partir de los registros
 */
@Composable
fun generarEstadisticas(
    alumno: Alumno,
    registros: List<RegistroActividad>
): List<StatItem> {
    val totalRegistros = registros.size
    
    // Registros de hoy
    val formatoFecha = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val hoy = formatoFecha.format(Date())
    val registrosHoy = registros.count { 
        formatoFecha.format(it.fecha.toDate()) == hoy 
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
        StatItem(
            title = "Registros",
            value = "$totalRegistros",
            icon = Icons.Default.AssignmentTurnedIn,
            color = MaterialTheme.colorScheme.primary
        ),
        StatItem(
            title = "Hoy",
            value = "$registrosHoy",
            icon = Icons.Default.Today,
            color = MaterialTheme.colorScheme.secondary
        ),
        StatItem(
            title = "Comidas",
            value = porcentajeComidas,
            icon = Icons.Default.Restaurant,
            color = MaterialTheme.colorScheme.tertiary
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