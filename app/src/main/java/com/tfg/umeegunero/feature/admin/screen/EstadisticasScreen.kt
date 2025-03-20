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

/**
 * Pantalla que muestra estadísticas del sistema
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    
    // Datos simulados para las estadísticas (en una implementación real vendrían del ViewModel)
    val centrosCount = 12
    val profesoresCount = 86
    val alumnosCount = 548
    val familiaresCount = 623
    
    // Datos para gráficos
    val datosMensuales = remember { 
        val meses = (1..6).map { 
            LocalDate.now().minusMonths(it.toLong()).month.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).capitalize() 
        }.reversed()
        
        // Centros por mes (simulado)
        val centrosPorMes = (1..6).map { Random.nextInt(8, 15) }.reversed()
        
        // Alumnos por mes (simulado)
        val alumnosPorMes = (1..6).map { Random.nextInt(400, 600) }.reversed()
        
        StatisticsData(
            meses = meses,
            centrosPorMes = centrosPorMes,
            alumnosPorMes = alumnosPorMes
        )
    }
    
    // Distribución de tipos de usuarios (simulado)
    val distribucionUsuarios = remember {
        listOf(
            PieChartData("Alumnos", alumnosCount.toFloat(), Color(0xFF2196F3)), // Azul
            PieChartData("Familiares", familiaresCount.toFloat(), Color(0xFF4CAF50)), // Verde
            PieChartData("Profesores", profesoresCount.toFloat(), Color(0xFFFF9800)), // Naranja
            PieChartData("Administradores", 24f, Color(0xFFF44336))  // Rojo
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Tarjetas con contadores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    icon = Icons.Default.Business,
                    value = centrosCount.toString(),
                    label = "Centros",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                StatisticCard(
                    icon = Icons.Default.Group,
                    value = profesoresCount.toString(),
                    label = "Profesores",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    icon = Icons.Default.School,
                    value = alumnosCount.toString(),
                    label = "Alumnos",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                
                StatisticCard(
                    icon = Icons.Default.People,
                    value = familiaresCount.toString(),
                    label = "Familiares",
                    color = Color(0xFF009688), // Teal
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Gráfica de distribución de usuarios
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Distribución de Usuarios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PieChart(
                            data = distribucionUsuarios,
                            modifier = Modifier
                                .size(180.dp)
                                .align(Alignment.Center)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Leyenda
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        distribucionUsuarios.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(item.color, RoundedCornerShape(4.dp))
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Text(
                                    text = item.value.toInt().toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Porcentaje
                                val total = distribucionUsuarios.sumOf { it.value.toDouble() }
                                val percentage = (item.value / total * 100).toInt()
                                
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Gráfica de tendencia de centros
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tendencia de Centros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gráfica de línea para centros
                    LineChart(
                        data = datosMensuales.centrosPorMes,
                        labels = datosMensuales.meses,
                        maxValue = datosMensuales.centrosPorMes.maxOrNull()?.toFloat() ?: 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Gráfica de tendencia de alumnos
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tendencia de Alumnos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gráfica de barras para alumnos
                    BarChart(
                        data = datosMensuales.alumnosPorMes,
                        labels = datosMensuales.meses,
                        maxValue = datosMensuales.alumnosPorMes.maxOrNull()?.toFloat() ?: 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        barColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tarjeta de indicadores adicionales
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Indicadores adicionales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProgressIndicator(
                            label = "Uso del sistema",
                            value = 0.78f,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        ProgressIndicator(
                            label = "Actividad diaria",
                            value = 0.65f,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProgressIndicator(
                            label = "Registros completados",
                            value = 0.92f,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        ProgressIndicator(
                            label = "Satisfacción",
                            value = 0.89f,
                            color = Color(0xFF4CAF50), // Verde
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
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