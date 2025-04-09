package com.tfg.umeegunero.feature.common.stats.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.NumberFormat
import java.time.LocalDate as JavaLocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Modelo para los datos de gráficos de barras
 */
data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * Modelo para los datos de gráficos circulares
 */
data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color,
    val legendText: String = ""
)

/**
 * Modelo para los datos estadísticos generales
 */
data class StatisticsData(
    val meses: List<String>,
    val centrosPorMes: List<Int>,
    val alumnosPorMes: List<Int>
)

/**
 * Pantalla que muestra las estadísticas generales del sistema
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    
    // Datos simulados para las estadísticas (en una implementación real vendrían del ViewModel)
    val centrosCount = 15
    val profesoresCount = 124
    val alumnosCount = 937
    val familiaresCount = 1085
    
    // Datos para gráficos
    val datosMensuales = remember { 
        val meses = (1..6).map { 
            JavaLocalDate.now().minusMonths(it.toLong()).month.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).capitalize() 
        }.reversed()
        
        // Centros por mes (simulado)
        val centrosPorMes = listOf(8, 9, 10, 12, 14, 15)
        
        // Alumnos por mes (simulado)
        val alumnosPorMes = listOf(450, 520, 620, 720, 850, 937)
        
        StatisticsData(
            meses = meses,
            centrosPorMes = centrosPorMes,
            alumnosPorMes = alumnosPorMes
        )
    }
    
    // Distribución de tipos de usuarios (simulado)
    val distribucionUsuarios = remember {
        listOf(
            PieChartData("Alumnos", alumnosCount.toFloat(), Color(0xFF2196F3), "$alumnosCount usuarios"),
            PieChartData("Familiares", familiaresCount.toFloat(), Color(0xFF4CAF50), "$familiaresCount usuarios"),
            PieChartData("Profesores", profesoresCount.toFloat(), Color(0xFFFF9800), "$profesoresCount usuarios"),
            PieChartData("Administradores", 24f, Color(0xFFF44336), "24 usuarios")
        )
    }
    
    // Datos de rendimiento (simulados)
    val datosRendimiento = remember {
        listOf(
            BarChartData("Tiempos de respuesta", 86f, Color(0xFF03A9F4)),
            BarChartData("Disponibilidad", 99.5f, Color(0xFF4CAF50)),
            BarChartData("Usuarios activos", 78f, Color(0xFFFF9800))
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
            // Título y descripción
            Text(
                text = "Resumen del Sistema",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Estadísticas y métricas de rendimiento del sistema",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Tarjetas con contadores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    icon = Icons.Default.Business,
                    value = formatNumber(centrosCount),
                    label = "Centros",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                StatisticCard(
                    icon = Icons.Default.Group,
                    value = formatNumber(profesoresCount),
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
                    icon = Icons.Default.Face,
                    value = formatNumber(alumnosCount),
                    label = "Alumnos",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                
                StatisticCard(
                    icon = Icons.Default.People,
                    value = formatNumber(familiaresCount),
                    label = "Familiares",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Gráfico de usuarios por tipo
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                            .height(200.dp)
                    ) {
                        PieChart(
                            data = distribucionUsuarios,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Leyenda del gráfico
                    Column {
                        distribucionUsuarios.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(item.color, CircleShape)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Text(
                                    text = item.legendText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Evolución de centros y alumnos
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Evolución Mensual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Crecimiento de centros y alumnos en los últimos 6 meses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gráfico de líneas para centros
                    Column {
                        Text(
                            text = "Centros educativos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            LineChart(
                                data = datosMensuales.centrosPorMes.map { it.toFloat() },
                                labels = datosMensuales.meses,
                                maxValue = datosMensuales.centrosPorMes.maxOrNull()?.toFloat()?.times(1.2f) ?: 20f,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Gráfico de líneas para alumnos
                    Column {
                        Text(
                            text = "Alumnos registrados",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            LineChart(
                                data = datosMensuales.alumnosPorMes.map { it.toFloat() },
                                labels = datosMensuales.meses,
                                maxValue = datosMensuales.alumnosPorMes.maxOrNull()?.toFloat()?.times(1.2f) ?: 1000f,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Métricas de rendimiento
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Métricas de Rendimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    datosRendimiento.forEach { metrica ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = metrica.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Text(
                                    text = "${metrica.value.roundToInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LinearProgressIndicator(
                                progress = { metrica.value / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = metrica.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Últimos 30 días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Uso por plataforma
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Uso por Plataforma",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Datos simulados de plataformas
                    val plataformas = listOf(
                        BarChartData("Android", 72f, Color(0xFF4CAF50)),
                        BarChartData("iOS", 24f, Color(0xFF2196F3)),
                        BarChartData("Web", 4f, Color(0xFFFF9800))
                    )
                    
                    plataformas.forEach { plataforma ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = plataforma.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                Text(
                                    text = "${plataforma.value.roundToInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            LinearProgressIndicator(
                                progress = { plataforma.value / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = plataforma.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información Adicional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val infoItems = listOf(
                        Triple(Icons.Default.Schedule, "Tiempo promedio por sesión", "8.2 minutos"),
                        Triple(Icons.Default.DateRange, "Días activos por mes", "18.4 días"),
                        Triple(Icons.Default.Refresh, "Tasa de retorno", "76%"),
                        Triple(Icons.Default.Storage, "Almacenamiento utilizado", "427 GB")
                    )
                    
                    infoItems.forEach { (icon, label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (infoItems.last() != Triple(icon, label, value)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Tarjeta para mostrar una estadística individual
 */
@Composable
private fun StatisticCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.value.toDouble() }
    var startAngle = 0f
    
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        
        data.forEach { slice ->
            val sweepAngle = 360 * (slice.value / total.toFloat())
            
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(size.width / 2 - radius, size.height / 2 - radius)
            )
            
            startAngle += sweepAngle
        }
        
        // Círculo central (opcional, para efecto de donut)
        // drawCircle(
        //     color = MaterialTheme.colorScheme.surface,
        //     radius = radius * 0.5f,
        //     center = Offset(size.width / 2, size.height / 2)
        // )
    }
}

@Composable
fun LineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    maxValue: Float
) {
    if (data.isEmpty() || data.size != labels.size) return
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val xStep = width / (data.size - 1)
        
        // Dibujar líneas horizontales (grid)
        val gridLineCount = 4
        val yStep = height / gridLineCount
        
        for (i in 0..gridLineCount) {
            val y = height - (i * yStep)
            
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        
        // Dibujar puntos y líneas de conexión
        for (i in 0 until data.size - 1) {
            val startX = i * xStep
            val startY = height - (height * (data[i] / maxValue))
            val endX = (i + 1) * xStep
            val endY = height - (height * (data[i + 1] / maxValue))
            
            // Línea entre puntos
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
            
            // Punto inicial
            if (i == 0) {
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = Offset(startX, startY)
                )
            }
            
            // Punto final
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(endX, endY)
            )
        }
        
        // Dibujar etiquetas de los meses en el eje X (opcional)
        // Esto requeriría usar Canvas con drawIntoCanvas y CustomDrawScope
    }
    
    // Etiquetas del eje X
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { month ->
            Text(
                text = month,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale("es", "ES")).format(number)
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

@Preview(showBackground = true)
@Composable
fun EstadisticasScreenPreview() {
    UmeEguneroTheme {
        EstadisticasScreen(rememberNavController())
    }
} 