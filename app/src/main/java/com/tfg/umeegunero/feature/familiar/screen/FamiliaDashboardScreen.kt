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
import androidx.compose.foundation.BorderStroke
import android.content.res.Configuration

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
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de bienvenida
            WelcomeCard(nombreAlumno = uiState.familiar?.nombre ?: "Familiar")
            
            // Tarjeta de estadísticas
            EstadisticasCard(alumno = uiState.hijoSeleccionado)
            
            // Últimas notificaciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Últimas notificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay notificaciones recientes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // Accesos rápidos
            QuickActionsCard(
                onVerMensajes = { navController.navigate(AppScreens.ConversacionesFamilia.route) },
                onVerCalendario = { navController.navigate(AppScreens.CalendarioFamilia.route) },
                onVerHistorial = { navController.navigate(AppScreens.Notificaciones.route) }
            )
        }
    }
}

@Composable
fun WelcomeCard(nombreAlumno: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hola, Familia de",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = nombreAlumno,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Centro Infantil UmeEgunero",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EstadisticasCard(alumno: Alumno?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Estadísticas de hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas en formato grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Siesta
                EstadisticaCircular(
                    valor = 0.5f, // Valor por defecto hasta que se implemente la lógica real
                    etiqueta = "Siesta",
                    color = Color(0xFF4CAF50)
                )
                
                // Comida
                EstadisticaCircular(
                    valor = calcularNivelComida(alumno),
                    etiqueta = "Comida",
                    color = Color(0xFFFF9800)
                )
                
                // Actividades
                EstadisticaCircular(
                    valor = 0.85f,
                    etiqueta = "Actividades",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun EstadisticaCircular(
    valor: Float,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        ) {
            CircularProgressIndicator(
                progress = { valor },
                modifier = Modifier.fillMaxSize(),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeWidth = 8.dp
            )
            
            Text(
                text = "${(valor * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun QuickActionsCard(
    onVerMensajes: () -> Unit,
    onVerCalendario: () -> Unit,
    onVerHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Accesos rápidos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.Message,
                    texto = "Mensajes",
                    color = Color(0xFF4CAF50),
                    onClick = onVerMensajes
                )
                
                ActionButton(
                    icon = Icons.Default.DateRange,
                    texto = "Calendario",
                    color = Color(0xFFFF9800),
                    onClick = onVerCalendario
                )
                
                ActionButton(
                    icon = Icons.Default.History,
                    texto = "Historial",
                    color = Color(0xFF2196F3),
                    onClick = onVerHistorial
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .padding(4.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = color.copy(alpha = 0.2f),
                contentColor = color
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = texto,
                tint = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// Función auxiliar para calcular el nivel de comida en base a la enum EstadoComida
private fun calcularNivelComida(alumno: Alumno?): Float {
    return when {
        alumno == null -> 0f
        else -> 0.5f // Valor por defecto hasta que se implemente la lógica real
    }
}

@Composable
@Preview(showBackground = true)
fun FamiliaDashboardScreenPreview() {
    UmeEguneroTheme {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Composable
@Preview(showBackground = true)
fun FamiliaDashboardScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        FamiliaDashboardScreen(
            navController = rememberNavController()
        )
    }
} 