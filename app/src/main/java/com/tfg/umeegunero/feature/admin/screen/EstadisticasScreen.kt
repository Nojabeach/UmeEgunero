/**
 * Módulo de estadísticas del sistema UmeEgunero.
 * 
 * Este módulo implementa la visualización y análisis de estadísticas
 * del sistema para los administradores.
 */
package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.math.min
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import android.widget.Toast
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tfg.umeegunero.feature.admin.viewmodel.EstadisticasViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.AccesoPorCentro
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import java.text.SimpleDateFormat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.os.Build
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Insights
import com.tfg.umeegunero.data.model.ActividadReciente
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Login

/**
 * Pantalla de estadísticas para el administrador del sistema.
 * 
 * Esta pantalla proporciona una interfaz completa para la visualización y análisis
 * de estadísticas del sistema, incluyendo gráficos interactivos y filtros temporales.
 * 
 * ## Características
 * - Visualización de estadísticas en tiempo real
 * - Filtros por períodos temporales
 * - Gráficos interactivos
 * - Indicadores de rendimiento
 * - Exportación de datos
 * 
 * ## Componentes principales
 * - Gráficos de uso del sistema
 * - Estadísticas de usuarios activos
 * - Métricas de rendimiento
 * - Análisis de tendencias
 * 
 * ## Funcionalidades
 * - Selección de períodos de tiempo
 * - Actualización en tiempo real
 * - Filtrado de datos
 * - Exportación de informes
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona la lógica de estadísticas
 * 
 * @see EstadisticasViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accesosPorCentro by viewModel.accesosPorCentro.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showUpdateMessage by remember { mutableStateOf(false) }
    
    // Solicitud de permisos de almacenamiento
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.descargarInforme(context)
        } else {
            // Mostrar mensaje de que se necesitan permisos
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.cargarEstadisticas()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas del Sistema") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarEstadisticas() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar estadísticas"
                        )
                    }
                    IconButton(onClick = { viewModel.generarInforme() }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Generar informe"
                        )
                    }
                    IconButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                viewModel.descargarInforme(context)
                            } else {
                                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Descargar informe"
                        )
                    }
                }
            )
        }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                // Encabezado con fecha de actualización
                Text(
                    text = "Última actualización: ${uiState.fechaActualizacion}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Tarjetas de resumen
                ResumenEstadisticasCard(
                    totalCentros = uiState.totalCentros,
                    totalUsuarios = uiState.totalUsuarios,
                    nuevosCentros = uiState.nuevosCentros,
                    nuevosUsuarios = uiState.nuevosRegistros,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Tarjeta de distribución de usuarios
                DistribucionUsuariosCard(
                    totalProfesores = uiState.totalProfesores,
                    totalAlumnos = uiState.totalAlumnos,
                    totalFamiliares = uiState.totalFamiliares,
                    totalAdministradores = uiState.totalAdministradores,
                    totalAdministradoresApp = uiState.totalAdministradoresApp,
                    totalAdministradoresCentro = uiState.totalAdministradoresCentro,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Tarjeta de accesos por centro
                AccesosPorCentroCard(
                    accesosPorCentro = accesosPorCentro,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Actividad reciente
                ActividadRecienteCard(
                    actividades = uiState.actividadesRecientes,
                    modifier = Modifier.padding(16.dp)
                )
                
                // Mensajes de estado
                if (uiState.informeGenerado) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Informe generado correctamente",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Descarga el informe usando el botón de descarga en la barra superior.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                if (uiState.error.isNotEmpty()) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // Mensaje de éxito al descargar
                if (uiState.informeDescargado && showUpdateMessage) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Informe descargado correctamente",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    // Ocultar mensaje después de 3 segundos
                    LaunchedEffect(uiState.informeDescargado) {
                        showUpdateMessage = true
                        delay(3000)
                        showUpdateMessage = false
                    }
                }
            }
        }
    }
}

/**
 * Componente de leyenda para gráficos
 */
@Composable
fun LeyendaItem(
    color: Color,
    texto: String,
    valor: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Column {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Tarjeta de estadística con título, valor e icono
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: String = "",
    trendValue: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                
                if (trendValue.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color.Green,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = trend + trendValue,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Green
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
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
 * Item de actividad reciente
 */
@Composable
fun ActivityItem(
    title: String,
    value: Int,
    trend: String,
    trendColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = trend,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = trendColor
            )
        }
    }
}

/**
 * Botón de acción con texto e icono
 */
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Tarjeta que muestra un indicador estadístico
 */
@Composable
fun StatisticCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Barra horizontal que representa la proporción de un tipo de usuario
 */
@Composable
fun UserTypeBar(
    label: String,
    count: Int,
    percentage: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$count (${percentage.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Componente para mostrar la fecha de última actualización en formato Badge
 */
@Composable
fun FechaActualizacionBadge(fechaActualizacion: String) {
    Surface(
        modifier = Modifier
            .padding(end = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (fechaActualizacion == "No disponible") 
                      "Pendiente de actualización" 
                      else "Actualizado: $fechaActualizacion",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Vista previa de la pantalla de estadísticas en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun EstadisticasScreenPreview() {
    UmeEguneroTheme {
        EstadisticasScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Vista previa de la pantalla de estadísticas en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EstadisticasScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        EstadisticasScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IndicadorProgressPreview() {
    UmeEguneroTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            CircularProgressIndicator(
                progress = { 0.75f },
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
            
            Text(
                text = "75%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActividadRecienteCard(
    actividades: List<ActividadReciente>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (actividades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividad reciente",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column {
                    actividades.forEach { actividad ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono según tipo de actividad
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (actividad.tipo) {
                                            "LOGIN" -> Color(0xFF4CAF50) // Verde
                                            "REGISTRO" -> Color(0xFF2196F3) // Azul
                                            "ASISTENCIA" -> Color(0xFFFF9800) // Naranja
                                            "MENSAJE" -> Color(0xFF9C27B0) // Púrpura
                                            else -> Color(0xFF607D8B) // Gris
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (actividad.tipo) {
                                        "LOGIN" -> Icons.AutoMirrored.Filled.Login
                                        "REGISTRO" -> Icons.Default.PersonAdd
                                        "ASISTENCIA" -> Icons.Default.CheckCircle
                                        "MENSAJE" -> Icons.AutoMirrored.Filled.Message
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = actividad.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(actividad.fecha),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    if (actividad.detalles.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = actividad.detalles,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (actividad != actividades.lastOrNull()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de resumen de estadísticas
 */
@Composable
fun ResumenEstadisticasCard(
    totalCentros: Int,
    totalUsuarios: Int,
    nuevosCentros: Int,
    nuevosUsuarios: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen General",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Centros
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Centros",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$totalCentros",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+$nuevosCentros nuevos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Usuarios
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Usuarios",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$totalUsuarios",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+$nuevosUsuarios nuevos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta que muestra la distribución de usuarios
 */
@Composable
fun DistribucionUsuariosCard(
    totalProfesores: Int,
    totalAlumnos: Int,
    totalFamiliares: Int,
    totalAdministradores: Int,
    totalAdministradoresApp: Int,
    totalAdministradoresCentro: Int,
    modifier: Modifier = Modifier
) {
    val totalUsuarios = totalProfesores + totalAlumnos + totalFamiliares + totalAdministradores
    
    // Calcular porcentajes
    val porcentajeProfesores = if (totalUsuarios > 0) (totalProfesores.toFloat() / totalUsuarios) * 100 else 0f
    val porcentajeAlumnos = if (totalUsuarios > 0) (totalAlumnos.toFloat() / totalUsuarios) * 100 else 0f
    val porcentajeFamiliares = if (totalUsuarios > 0) (totalFamiliares.toFloat() / totalUsuarios) * 100 else 0f
    val porcentajeAdministradoresApp = if (totalUsuarios > 0) (totalAdministradoresApp.toFloat() / totalUsuarios) * 100 else 0f
    val porcentajeAdministradoresCentro = if (totalUsuarios > 0) (totalAdministradoresCentro.toFloat() / totalUsuarios) * 100 else 0f
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distribución de Usuarios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de profesores
            BarraProgreso(
                label = "Profesores",
                valor = totalProfesores,
                porcentaje = porcentajeProfesores,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de alumnos
            BarraProgreso(
                label = "Alumnos",
                valor = totalAlumnos,
                porcentaje = porcentajeAlumnos,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de familiares
            BarraProgreso(
                label = "Familiares",
                valor = totalFamiliares,
                porcentaje = porcentajeFamiliares,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de administradores de aplicación
            BarraProgreso(
                label = "Admin. Aplicación",
                valor = totalAdministradoresApp,
                porcentaje = porcentajeAdministradoresApp,
                color = Color(0xFF9C27B0) // Púrpura para administradores de app
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de administradores de centro
            BarraProgreso(
                label = "Admin. Centro",
                valor = totalAdministradoresCentro,
                porcentaje = porcentajeAdministradoresCentro,
                color = Color(0xFF673AB7) // Púrpura más oscuro para administradores de centro
            )
        }
    }
}

/**
 * Barra de progreso para representar un valor y su porcentaje
 */
@Composable
fun BarraProgreso(
    label: String,
    valor: Int,
    porcentaje: Float,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$valor (${porcentaje.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { porcentaje / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Componente para mostrar un gráfico de barras
 */
@Composable
fun BarChart(
    data: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    showLabels: Boolean = true,
    maxValue: Float? = null
) {
    val maxValueCalculated = maxValue ?: (data.maxOrNull() ?: 0f) * 1.2f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = axisColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(
                start = if (showLabels) 32.dp else 8.dp,
                end = 8.dp,
                top = 16.dp,
                bottom = if (showLabels) 32.dp else 8.dp
            )
    ) {
        // Dibujar ejes
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Eje Y
            drawLine(
                color = axisColor,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 1.5f
            )
            
            // Eje X
            drawLine(
                color = axisColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.5f
            )
        }
        
        // Dibujar barras
        if (data.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, value ->
                    val heightPercentage = if (maxValueCalculated > 0) value / maxValueCalculated else 0f
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp)
                                .padding(bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(heightPercentage)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(color)
                            )
                        }
                        
                        if (showLabels && index < labels.size) {
                            Text(
                                text = labels[index],
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente para mostrar un gráfico de líneas
 */
@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    showLabels: Boolean = true,
    maxValue: Float? = null
) {
    val maxValueCalculated = maxValue ?: (data.maxOrNull() ?: 0f) * 1.2f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = axisColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(
                start = if (showLabels) 32.dp else 8.dp,
                end = 8.dp,
                top = 16.dp,
                bottom = if (showLabels) 32.dp else 8.dp
            )
    ) {
        if (data.isNotEmpty()) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokePath = androidx.compose.ui.graphics.Path()
                val fillPath = androidx.compose.ui.graphics.Path()
                
                val horizontalStep = size.width / (data.size - 1)
                
                // Iniciar los paths
                data.forEachIndexed { index, value ->
                    val heightRatio = if (maxValueCalculated > 0) {
                        value / maxValueCalculated
                    } else 0f
                    
                    val x = index * horizontalStep
                    val y = size.height - (heightRatio * size.height)
                    
                    if (index == 0) {
                        strokePath.moveTo(x, y)
                        fillPath.moveTo(x, y)
                    } else {
                        strokePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                
                // Completar el path de relleno
                fillPath.lineTo(size.width, size.height)
                fillPath.lineTo(0f, size.height)
                fillPath.close()
                
                // Dibujar el relleno
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            fillColor.copy(alpha = 0.5f),
                            fillColor.copy(alpha = 0.0f)
                        )
                    )
                )
                
                // Dibujar la línea
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                // Dibujar puntos
                data.forEachIndexed { index, value ->
                    val heightRatio = if (maxValueCalculated > 0) {
                        value / maxValueCalculated
                    } else 0f
                    
                    val x = index * horizontalStep
                    val y = size.height - (heightRatio * size.height)
                    
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
                
                // Dibujar ejes
                drawLine(
                    color = axisColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 1.5f
                )
                
                drawLine(
                    color = axisColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.5f
                )
            }
        }
        
        // Etiquetas del eje X
        if (showLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Componente para mostrar un gráfico circular
 */
@Composable
fun PieChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = values.sum()
    val proportions = values.map { 
        if (total > 0) it / total else 0f 
    }
    val sweepAngles = proportions.map { prop -> prop * 360f }
    
    Canvas(
        modifier = modifier
    ) {
        var startAngle = 0f
        
        sweepAngles.forEachIndexed { index, sweepAngle ->
            val color = colors.getOrElse(index) { Color.Gray }
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            startAngle += sweepAngle
        }
    }
}

/**
 * Tarjeta que muestra la distribución de accesos por centro
 */
@Composable
fun AccesosPorCentroCard(
    accesosPorCentro: List<AccesoPorCentro>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Porcentaje de Uso por Centro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (accesosPorCentro.isEmpty()) {
                Text(
                    text = "No hay datos de acceso disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                // Mostrar porcentaje de uso por centro como barras horizontales
                accesosPorCentro.forEach { centro ->
                    AccesoCentroItem(centro)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Item individual para mostrar el acceso de un centro
 */
@Composable
fun AccesoCentroItem(centro: AccesoPorCentro) {
    // Determinar el nombre a mostrar
    val nombreMostrado = when {
        centro.nombreCentro.isBlank() -> "UmeEgunero Admin"
        centro.nombreCentro.equals("Centro", ignoreCase = true) -> "UmeEgunero Admin"
        else -> centro.nombreCentro
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nombreMostrado,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${centro.porcentajeUso.toInt()}% (${centro.numeroAccesos})",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Barra de progreso horizontal
        LinearProgressIndicator(
            progress = { (centro.porcentajeUso / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
} 