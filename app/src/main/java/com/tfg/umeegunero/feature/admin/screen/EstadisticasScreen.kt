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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
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
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showUpdateMessage by remember { mutableStateOf(false) }
    
    // Solicitud de permisos de almacenamiento para Android 9 y anteriores
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, descargar informe
            viewModel.descargarInforme(context)
        } else {
            // Permiso denegado, mostrar mensaje
            Toast.makeText(
                context,
                "Permiso de almacenamiento necesario para descargar informes",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Función para comprobar y solicitar permisos antes de descargar
    val checkAndRequestPermissions = {
        // Para Android 10 y superior no necesitamos permisos para descargar
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Ya tenemos el permiso, descargar directamente
                    viewModel.descargarInforme(context)
                }
                else -> {
                    // Solicitar el permiso
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        } else {
            // Android 10+ no necesita permisos para la carpeta de descargas
            viewModel.descargarInforme(context)
        }
    }
    
    // Estado para mostrar/ocultar el diálogo de selección de período
    val mostrarDialogoPeriodo = remember { mutableStateOf(false) }
    
    // Lista de períodos disponibles
    val periodos = listOf("Última semana", "Último mes", "Último trimestre", "Último año", "Todo")
    
    // Efecto para mostrar mensaje cuando se actualicen los datos
    LaunchedEffect(uiState.fechaActualizacion) {
        if (!uiState.isLoading && uiState.fechaActualizacion != "No disponible") {
            showUpdateMessage = true
            delay(3000)
            showUpdateMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas del Sistema") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogoPeriodo.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Seleccionar período"
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Resumen General
                ResumenGeneralCard(
                    totalCentros = uiState.totalCentros,
                    totalUsuarios = uiState.totalUsuarios,
                    nuevosCentros = uiState.nuevosCentros,
                    nuevosUsuarios = uiState.nuevosProfesores + uiState.nuevosAlumnos + uiState.nuevosFamiliares,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Distribución de Usuarios
                DistribucionUsuariosCard(
                    profesores = uiState.totalProfesores,
                    alumnos = uiState.totalAlumnos,
                    familiares = uiState.totalFamiliares,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actividad Reciente
                ActividadRecienteCard(
                    actividades = uiState.actividadesRecientes,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.generarInforme() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar Informe")
                    }
                    
                    Button(
                        onClick = { checkAndRequestPermissions() },
                        enabled = uiState.informeGenerado && !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Descargar")
                    }
                }
            }
            
            // Indicador de carga
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Mensaje de actualización
            AnimatedVisibility(
                visible = showUpdateMessage,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Datos actualizados: ${uiState.fechaActualizacion}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
    
    // Diálogo de selección de período
    if (mostrarDialogoPeriodo.value) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPeriodo.value = false },
            title = { Text("Seleccionar Período") },
            text = {
                Column {
                    periodos.forEach { periodo ->
                        TextButton(
                            onClick = {
                                when (periodo) {
                                    "Última semana" -> viewModel.cargarEstadisticasPorPeriodo(7)
                                    "Último mes" -> viewModel.cargarEstadisticasPorPeriodo(30)
                                    "Último trimestre" -> viewModel.cargarEstadisticasPorPeriodo(90)
                                    "Último año" -> viewModel.cargarEstadisticasPorPeriodo(365)
                                    "Todo" -> viewModel.cargarEstadisticas()
                                }
                                mostrarDialogoPeriodo.value = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(periodo)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoPeriodo.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Mostrar mensajes de éxito o error
    LaunchedEffect(uiState.informeGenerado, uiState.informeDescargado, uiState.error) {
        if (uiState.informeGenerado) {
            Toast.makeText(context, "Informe generado correctamente", Toast.LENGTH_SHORT).show()
        }
        
        if (uiState.informeDescargado) {
            Toast.makeText(context, "Informe descargado a la carpeta Descargas", Toast.LENGTH_SHORT).show()
        }
        
        if (uiState.error.isNotEmpty()) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
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
                            Icon(
                                imageVector = when (actividad.tipo) {
                                    "LOGIN" -> Icons.AutoMirrored.Filled.Login
                                    "REGISTRO" -> Icons.Default.PersonAdd
                                    "ASISTENCIA" -> Icons.Default.CheckCircle
                                    "MENSAJE" -> Icons.AutoMirrored.Filled.Message
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = actividad.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(actividad.fecha),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
 * Tarjeta que muestra el resumen general de estadísticas
 */
@Composable
fun ResumenGeneralCard(
    totalCentros: Int,
    totalUsuarios: Int,
    nuevosCentros: Int,
    nuevosUsuarios: Int,
    modifier: Modifier = Modifier
) {
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
                    text = "Resumen General",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    
                    Text(
                        text = totalCentros.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Centros",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (nuevosCentros > 0) {
                        Text(
                            text = "+$nuevosCentros",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    
                    Text(
                        text = totalUsuarios.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Usuarios",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (nuevosUsuarios > 0) {
                        Text(
                            text = "+$nuevosUsuarios",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
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
    profesores: Int,
    alumnos: Int,
    familiares: Int,
    modifier: Modifier = Modifier
) {
    val totalUsuarios = profesores + alumnos + familiares
    
    // Calcular porcentajes
    val porcentajeProfesores = if (totalUsuarios > 0) (profesores.toFloat() / totalUsuarios) * 100 else 0f
    val porcentajeAlumnos = if (totalUsuarios > 0) (alumnos.toFloat() / totalUsuarios) * 100 else 0f
    val porcentajeFamiliares = if (totalUsuarios > 0) (familiares.toFloat() / totalUsuarios) * 100 else 0f
    
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
                valor = profesores,
                porcentaje = porcentajeProfesores,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de alumnos
            BarraProgreso(
                label = "Alumnos",
                valor = alumnos,
                porcentaje = porcentajeAlumnos,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de familiares
            BarraProgreso(
                label = "Familiares",
                valor = familiares,
                porcentaje = porcentajeFamiliares,
                color = MaterialTheme.colorScheme.secondary
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