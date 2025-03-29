package com.tfg.umeegunero.feature.admin.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tfg.umeegunero.feature.admin.viewmodel.EstadisticasViewModel

/**
 * Pantalla de estadísticas para el administrador
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    viewModel: EstadisticasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
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
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sección de resumen
                Text(
                    text = "Resumen General",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Mostrar tarjetas de resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Centros",
                        value = uiState.totalCentros.toString(),
                        icon = Icons.Default.Business,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Usuarios",
                        value = uiState.totalUsuarios.toString(),
                        icon = Icons.Default.Group,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Profesores",
                        value = uiState.totalProfesores.toString(),
                        icon = Icons.Default.School,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Alumnos",
                        value = uiState.totalAlumnos.toString(),
                        icon = Icons.Default.Person,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Sección de actividad
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
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
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButton(
                        text = "Informe Detallado",
                        icon = Icons.Default.Assessment,
                        onClick = { viewModel.generarInforme() },
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionButton(
                        text = "Exportar Datos",
                        icon = Icons.Default.Download,
                        onClick = { viewModel.exportarDatos() },
                        modifier = Modifier.weight(1f)
                    )
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
                            Text(
                                text = "Informe Generado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
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
                Spacer(modifier = Modifier.height(32.dp))
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
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
            .padding(vertical = 8.dp),
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
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
 * Gráfica circular
 */
@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.value.toDouble() }
    
    Canvas(
        modifier = modifier
    ) {
        var startAngle = 0f
        
        data.forEach { item ->
            val sweepAngle = (item.value / total.toFloat()) * 360f
            
            // Dibujar el sector
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            
            // Borde blanco para separar sectores
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = Stroke(width = 2f),
                size = Size(size.width, size.height)
            )
            
            startAngle += sweepAngle
        }
    }
}

/**
 * Gráfica de líneas
 */
@Composable
fun LineChart(
    data: List<Int>,
    labels: List<String>,
    maxValue: Float,
    modifier: Modifier = Modifier,
    lineColor: Color
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val spaceBetweenPoints = width / (data.size - 1)
                
                // Dibujar líneas horizontales de referencia
                val numLines = 5
                val lineSpacing = height / numLines
                
                for (i in 0..numLines) {
                    val y = height - (i * lineSpacing)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }
                
                // Dibujar puntos y líneas
                var lastX = 0f
                var lastY = 0f
                
                data.forEachIndexed { index, value ->
                    val x = index * spaceBetweenPoints
                    val y = height - (value / maxValue) * height
                    
                    // Dibujar punto
                    drawCircle(
                        color = lineColor,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    
                    // Dibujar línea (excepto para el primer punto)
                    if (index > 0) {
                        drawLine(
                            color = lineColor,
                            start = Offset(lastX, lastY),
                            end = Offset(x, y),
                            strokeWidth = 3f
                        )
                    }
                    
                    lastX = x
                    lastY = y
                }
            }
        }
        
        // Etiquetas del eje X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Gráfica de barras
 */
@Composable
fun BarChart(
    data: List<Int>,
    labels: List<String>,
    maxValue: Float,
    modifier: Modifier = Modifier,
    barColor: Color
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val barWidth = (width / data.size) * 0.6f
                val spacingFactor = (width / data.size) * 0.4f
                
                // Dibujar líneas horizontales de referencia
                val numLines = 5
                val lineSpacing = height / numLines
                
                for (i in 0..numLines) {
                    val y = height - (i * lineSpacing)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    
                    // Etiquetas del eje Y
                    val labelValue = ((i * maxValue) / numLines).toInt()
                }
                
                // Dibujar barras
                data.forEachIndexed { index, value ->
                    val barHeight = (value / maxValue) * height
                    val x = index * (barWidth + spacingFactor) + spacingFactor / 2
                    
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x, height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
        }
        
        // Etiquetas del eje X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Indicador de progreso circular
 */
@Composable
fun ProgressIndicator(
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp)
        ) {
            CircularProgressIndicator(
                progress = { value },
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = color,
                modifier = Modifier.fillMaxSize()
            )
            
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Datos para gráficas
 */
data class StatisticsData(
    val meses: List<String>,
    val centrosPorMes: List<Int>,
    val alumnosPorMes: List<Int>
)

/**
 * Datos para gráfica circular
 */
data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

// Añadir previews para la pantalla de estadísticas
@Preview(showBackground = true)
@Composable
fun EstadisticasPreview() {
    UmeEguneroTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Estadísticas del Sistema",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Actividad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Indicador de estadística
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Usuarios",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "356",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Indicador de estadística
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Centros",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "12",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Indicador de estadística
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Actividad",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "87%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EstadisticasPreviewDark() {
    UmeEguneroTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Estadísticas del Sistema",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Actividad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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