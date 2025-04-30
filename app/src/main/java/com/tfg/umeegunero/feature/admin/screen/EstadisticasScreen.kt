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
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule

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
    
    // Diálogo de selección de período
    if (mostrarDialogoPeriodo.value) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPeriodo.value = false },
            title = { Text("Seleccionar período") },
            text = {
                Column {
                    Text(
                        "Elige el período para el que quieres ver las estadísticas",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    periodos.forEach { periodo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Llamar al método correspondiente del viewModel
                                    when (periodo) {
                                        "Última semana" -> viewModel.cargarEstadisticasPorPeriodo(7)
                                        "Último mes" -> viewModel.cargarEstadisticasPorPeriodo(30)
                                        "Último trimestre" -> viewModel.cargarEstadisticasPorPeriodo(90)
                                        "Último año" -> viewModel.cargarEstadisticasPorPeriodo(365)
                                        else -> viewModel.cargarEstadisticas()
                                    }
                                    mostrarDialogoPeriodo.value = false
                                    Toast.makeText(context, "Cargando estadísticas para: $periodo", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = periodo,
                                style = MaterialTheme.typography.bodyLarge
                            )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoPeriodo.value = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleccionar Período",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando estadísticas...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de resumen
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Resumen de Estadísticas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FechaActualizacionBadge(fechaActualizacion = uiState.fechaActualizacion)
                            
                            Button(
                                onClick = { 
                                    viewModel.recargarEstadisticas() 
                                    Toast.makeText(context, "Actualizando estadísticas...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar estadísticas",
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = "Actualizar",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    
                    // Gráfico de distribución de usuarios
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Distribución de Usuarios",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Calcular porcentajes
                            val totalUsuarios = uiState.totalUsuarios.coerceAtLeast(1)
                            val porcentajeProfesores = (uiState.totalProfesores.toFloat() / totalUsuarios) * 100
                            val porcentajeAlumnos = (uiState.totalAlumnos.toFloat() / totalUsuarios) * 100
                            val porcentajeFamiliares = (uiState.totalFamiliares.toFloat() / totalUsuarios) * 100
                            
                            // Barras de distribución
                            UserTypeBar(
                                label = "Profesores",
                                count = uiState.totalProfesores,
                                percentage = porcentajeProfesores,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            UserTypeBar(
                                label = "Alumnos",
                                count = uiState.totalAlumnos,
                                percentage = porcentajeAlumnos,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            UserTypeBar(
                                label = "Familiares",
                                count = uiState.totalFamiliares,
                                percentage = porcentajeFamiliares,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    
                    // Mostrar tarjetas de resumen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Centros",
                            value = uiState.totalCentros.toString(),
                            icon = Icons.Default.Business,
                            color = MaterialTheme.colorScheme.primary,
                            trend = if (uiState.nuevosCentros > 0) "+" else "",
                            trendValue = if (uiState.nuevosCentros > 0) uiState.nuevosCentros.toString() else "",
                            modifier = Modifier.weight(1f)
                        )
                        
                        StatCard(
                            title = "Usuarios",
                            value = uiState.totalUsuarios.toString(),
                            icon = Icons.Default.Group,
                            color = MaterialTheme.colorScheme.secondary,
                            trend = if (uiState.nuevosProfesores + uiState.nuevosAlumnos + uiState.nuevosFamiliares > 0) "+" else "",
                            trendValue = if (uiState.nuevosProfesores + uiState.nuevosAlumnos + uiState.nuevosFamiliares > 0) 
                                (uiState.nuevosProfesores + uiState.nuevosAlumnos + uiState.nuevosFamiliares).toString() else "",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Profesores",
                            value = uiState.totalProfesores.toString(),
                            icon = Icons.Default.School,
                            color = MaterialTheme.colorScheme.tertiary,
                            trend = if (uiState.nuevosProfesores > 0) "+" else "",
                            trendValue = if (uiState.nuevosProfesores > 0) uiState.nuevosProfesores.toString() else "",
                            modifier = Modifier.weight(1f)
                        )
                        
                        StatCard(
                            title = "Alumnos",
                            value = uiState.totalAlumnos.toString(),
                            icon = Icons.Default.Person,
                            color = MaterialTheme.colorScheme.error,
                            trend = if (uiState.nuevosAlumnos > 0) "+" else "",
                            trendValue = if (uiState.nuevosAlumnos > 0) uiState.nuevosAlumnos.toString() else "",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Sección de actividad
                    Text(
                        text = "Actividad Reciente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nuevos registros (últimos 7 días)",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                
                                IconButton(
                                    onClick = { 
                                        viewModel.recargarEstadisticas() 
                                        Toast.makeText(context, "Actualizando datos recientes...", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Actualizar estadísticas",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            
                            ActivityItem(
                                title = "Nuevos centros",
                                value = uiState.nuevosCentros,
                                trend = if (uiState.nuevosCentros > 0) "↑" else "→",
                                trendColor = if (uiState.nuevosCentros > 0) Color.Green else Color.Gray
                            )
                            
                            ActivityItem(
                                title = "Nuevos profesores",
                                value = uiState.nuevosProfesores,
                                trend = if (uiState.nuevosProfesores > 0) "↑" else "→",
                                trendColor = if (uiState.nuevosProfesores > 0) Color.Green else Color.Gray
                            )
                            
                            ActivityItem(
                                title = "Nuevos alumnos",
                                value = uiState.nuevosAlumnos,
                                trend = if (uiState.nuevosAlumnos > 0) "↑" else "→",
                                trendColor = if (uiState.nuevosAlumnos > 0) Color.Green else Color.Gray
                            )
                            
                            ActivityItem(
                                title = "Nuevos familiares",
                                value = uiState.nuevosFamiliares,
                                trend = if (uiState.nuevosFamiliares > 0) "↑" else "→",
                                trendColor = if (uiState.nuevosFamiliares > 0) Color.Green else Color.Gray
                            )
                        }
                    }
                    
                    // Sección de acciones
                    Text(
                        text = "Acciones Rápidas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Text(
                        text = "Estas acciones te permiten generar informes detallados y exportar los datos del sistema para su análisis externo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ActionButton(
                                    text = "Informe Detallado",
                                    icon = Icons.Default.Assessment,
                                    description = "Genera un informe completo con todos los datos del sistema",
                                    onClick = { viewModel.generarInforme() },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                ActionButton(
                                    text = "Exportar Datos",
                                    icon = Icons.Default.Download,
                                    description = "Exporta los datos en formato CSV para análisis",
                                    onClick = { viewModel.exportarDatos() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    if (uiState.informeGenerado) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Informe Generado",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "El informe ha sido generado correctamente y está disponible para su descarga.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { viewModel.descargarInforme() },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Descargar")
                                }
                            }
                        }
                    }
                    
                    // Espacio adicional al final
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Mensaje de actualización
            AnimatedVisibility(
                visible = showUpdateMessage,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estadísticas actualizadas correctamente",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
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
                                imageVector = Icons.Default.TrendingUp,
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