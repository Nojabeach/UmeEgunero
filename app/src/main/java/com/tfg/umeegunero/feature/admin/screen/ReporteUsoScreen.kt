package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.admin.viewmodel.ReporteUsoViewModel
import com.tfg.umeegunero.ui.components.charts.BarChart
import com.tfg.umeegunero.ui.components.charts.PieChart
import com.tfg.umeegunero.ui.components.charts.LineChart
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla para visualizar reportes de uso de la plataforma
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteUsoScreen(
    navController: NavController,
    viewModel: ReporteUsoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.cargarDatosUso()
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uso de Plataforma") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Resumen general
                    ResumenGeneral(uiState = uiState)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Gráfico de usuarios activos por mes
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Usuarios Activos por Mes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            LineChart(
                                data = uiState.usuariosActivosPorMes.map { it.toFloat() },
                                labels = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gráfico de distribución de usuarios por tipo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Distribución por Tipo de Usuario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Gráfico de tarta
                                PieChart(
                                    data = listOf(
                                        uiState.totalAdministradores.toFloat(),
                                        uiState.totalProfesores.toFloat(),
                                        uiState.totalAlumnos.toFloat(),
                                        uiState.totalFamiliares.toFloat()
                                    ),
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                        MaterialTheme.colorScheme.secondary,
                                        MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier
                                        .size(180.dp)
                                        .padding(8.dp)
                                )
                                
                                // Leyenda
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    LeyendaItem(
                                        color = MaterialTheme.colorScheme.primary,
                                        label = "Administradores",
                                        valor = uiState.totalAdministradores
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    LeyendaItem(
                                        color = MaterialTheme.colorScheme.tertiary,
                                        label = "Profesores",
                                        valor = uiState.totalProfesores
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    LeyendaItem(
                                        color = MaterialTheme.colorScheme.secondary,
                                        label = "Alumnos",
                                        valor = uiState.totalAlumnos
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    LeyendaItem(
                                        color = MaterialTheme.colorScheme.error,
                                        label = "Familiares",
                                        valor = uiState.totalFamiliares
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gráfico de accesos por hora del día
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Accesos por Hora del Día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            BarChart(
                                data = uiState.accesosPorHora.map { it.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "12h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "23h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Métricas de uso por centro
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Uso por Centro Educativo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            uiState.usoPorCentro.forEach { (centro, valor) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = centro,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Text(
                                        text = "${formatNumber(valor)} usuarios",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                LinearProgressIndicator(
                                    progress = valor.toFloat() / uiState.totalUsuarios,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ResumenGeneral(uiState: com.tfg.umeegunero.feature.admin.viewmodel.ReporteUsoUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total de usuarios
                MetricaItem(
                    icon = Icons.Default.Group,
                    valor = formatNumber(uiState.totalUsuarios),
                    label = "Usuarios",
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Total de accesos
                MetricaItem(
                    icon = Icons.Default.Login,
                    valor = formatNumber(uiState.totalAccesos),
                    label = "Accesos",
                    color = MaterialTheme.colorScheme.secondary
                )
                
                // Promedio de uso diario
                MetricaItem(
                    icon = Icons.Default.Schedule,
                    valor = formatNumber(uiState.promedioUsoDiario),
                    label = "Min/día",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tasas de crecimiento y engagement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tasa de crecimiento
                MetricaPercentItem(
                    icon = Icons.Default.TrendingUp,
                    valor = uiState.tasaCrecimiento,
                    label = "Crecimiento",
                    esPositivo = uiState.tasaCrecimiento >= 0
                )
                
                // Tasa de engagement
                MetricaPercentItem(
                    icon = Icons.Default.ThumbUp,
                    valor = uiState.tasaEngagement,
                    label = "Engagement",
                    esPositivo = uiState.tasaEngagement >= 0
                )
                
                // Retención de usuarios
                MetricaPercentItem(
                    icon = Icons.Default.Loop,
                    valor = uiState.tasaRetencion,
                    label = "Retención",
                    esPositivo = uiState.tasaRetencion >= 70
                )
            }
        }
    }
}

@Composable
private fun MetricaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(30.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricaPercentItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: Float,
    label: String,
    esPositivo: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (esPositivo) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error,
            modifier = Modifier.size(30.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${if (valor > 0) "+" else ""}${String.format("%.1f", valor)}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (esPositivo) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LeyendaItem(
    color: Color,
    label: String,
    valor: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = formatNumber(valor),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatNumber(number: Number): String {
    return when {
        number.toLong() >= 1_000_000 -> String.format("%.1fM", number.toFloat() / 1_000_000)
        number.toLong() >= 1_000 -> String.format("%.1fK", number.toFloat() / 1_000)
        else -> NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
    }
} 