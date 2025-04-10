package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.HorizontalDivider
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.ui.components.TemaSelector
import com.tfg.umeegunero.ui.components.TemaActual
import com.tfg.umeegunero.feature.common.config.viewmodel.ConfiguracionViewModel
import com.tfg.umeegunero.feature.common.config.screen.ConfiguracionScreen
import com.tfg.umeegunero.feature.common.config.screen.PerfilConfiguracion
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import androidx.navigation.compose.rememberNavController
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp

/**
 * Calcula la edad en años a partir de una fecha de nacimiento en formato dd/MM/yyyy
 * 
 * @param fechaNacimiento Fecha de nacimiento en formato dd/MM/yyyy
 * @return Edad en años, o 0 si la fecha no es válida
 */
fun calcularEdad(fechaNacimiento: String?): Int {
    if (fechaNacimiento.isNullOrEmpty()) return 0
    
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    try {
        val fechaNac = formatoFecha.parse(fechaNacimiento) ?: return 0
        val hoy = Calendar.getInstance()
        val fechaNacCal = Calendar.getInstance().apply { time = fechaNac }
        
        var edad = hoy.get(Calendar.YEAR) - fechaNacCal.get(Calendar.YEAR)
        
        // Ajustar si todavía no ha cumplido años este año
        if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNacCal.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        
        return edad
    } catch (e: Exception) {
        return 0
    }
}

/**
 * Pantalla principal del dashboard para profesores.
 */
@Composable
fun ProfesorDashboardScreen(
    navController: NavController,
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold { paddingValues ->
        ProfesorDashboardContent(
            alumnosPendientes = uiState.alumnosPendientes,
            onCrearRegistroActividad = { dni ->
                navController.navigate("${AppScreens.RegistroActividad.route}/$dni")
            },
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Contenido principal del Dashboard de Profesor
 */
@Composable
fun ProfesorDashboardContent(
    alumnosPendientes: List<Alumno>,
    onCrearRegistroActividad: (String) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bienvenida y fecha
        WelcomeCard()
        
        // Accesos rápidos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Nuevo Registro
                    SimpleActionButton(
                        text = "Nuevo Registro",
                        color = Color(0xFF34C759),
                        onClick = { 
                            try {
                                navController.navigate(AppScreens.RegistroDiario.createRoute(
                                    alumnoId = "dummy",
                                    claseId = "dummy",
                                    profesorId = "dummy",
                                    alumnoNombre = "Alumno de prueba",
                                    claseNombre = "Clase de prueba"
                                ))
                            } catch (e: Exception) {
                                // Si falla, navegar al DummyScreen
                                navController.navigate(AppScreens.AsistenciaProfesor.route)
                            }
                        }
                    )
                    
                    // Clonar Registro
                    SimpleActionButton(
                        text = "Clonar Registro",
                        color = Color(0xFF2196F3),
                        onClick = { 
                            // Navegamos a la pantalla de asistencia del profesor
                            navController.navigate(AppScreens.AsistenciaProfesor.route)
                        }
                    )
                }
            }
        }
        
        // Alumnos que requieren atención
        if (alumnosPendientes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Alumnos pendientes de registro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${alumnosPendientes.size} alumnos sin registrar hoy",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mostrar algunos alumnos
                    Text(
                        text = "Pulse el botón para crear un nuevo registro",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { onCrearRegistroActividad("") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Crear registro")
                    }
                }
            }
        }
        
        // Tareas pendientes y próximos eventos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Próximas actividades",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "No hay actividades programadas para hoy",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Tarjeta de bienvenida para el profesor con información del día
 */
@Composable
fun WelcomeCard(modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")
    val formattedDate = remember { today.format(formatter).replaceFirstChar { it.uppercase() } }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF34C759).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF34C759)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de aula
            Text(
                text = "Tu aula: 2B - Educación Infantil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "15 alumnos a tu cargo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Botón de acción simplificado
 */
@Composable
fun SimpleActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = text,
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionProfesorPreviewNew() {
    UmeEguneroTheme {
        ConfiguracionScreen(perfil = PerfilConfiguracion.PROFESOR)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfesorDashboardPreview() {
    UmeEguneroTheme {
        ProfesorDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfesorDashboardDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ProfesorDashboardScreen(
            navController = rememberNavController()
        )
    }
}

@Composable
fun FuncionalidadCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    .size(60.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AccionRapidaItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    .size(60.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GraficoRendimiento() {
    val diasSemana = listOf("L", "M", "X", "J", "V")
    val valorActividades = listOf(85, 90, 75, 88, 92)
    val valoresAsistencia = listOf(90, 95, 85, 92, 97)
    
    val barWidth = 15.dp
    val chartHeight = 140.dp
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Leyenda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF5856D6), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Actividades",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF34C759), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Asistencia",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(vertical = 8.dp)
        ) {
            // Líneas horizontales (guías)
            for (i in 0..4) {
                val yPosition = (i * 20f + 20) / 100f
                
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = (chartHeight * (1 - yPosition)))
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                // Valores de porcentaje
                Text(
                    text = "${(i * 20) + 20}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .align(Alignment.CenterStart)
                        .offset(y = (chartHeight * (1 - yPosition) - 8.dp))
                )
            }
            
            // Barras del gráfico
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(5) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barra de actividades
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height((chartHeight * valorActividades[index] / 100))
                                .background(
                                    color = Color(0xFF5856D6),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Día de la semana
                        Text(
                            text = diasSemana[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Barra de asistencia
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height((chartHeight * valoresAsistencia[index] / 100))
                                .background(
                                    color = Color(0xFF34C759),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Espacio vacío para alinear con la otra columna
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (index < 4) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticaItem(
    valor: String,
    titulo: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}