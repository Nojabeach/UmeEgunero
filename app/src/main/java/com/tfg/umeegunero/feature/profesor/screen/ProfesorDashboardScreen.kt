package com.tfg.umeegunero.feature.profesor.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCard
import com.tfg.umeegunero.ui.components.CategoriaCardData
import com.tfg.umeegunero.feature.admin.screen.components.CategoriaCardBienvenida
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.foundation.lazy.grid.GridItemSpan


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
 * 
 * Proporciona acceso rápido a todas las funcionalidades importantes del sistema para
 * los profesores: gestión de alumnos, comunicados, incidencias, actividades, calendario
 * y comunicación con familias.
 *
 * @param navController Controlador de navegación para moverse entre pantallas
 * @param viewModel ViewModel que gestiona los datos y lógica del dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorDashboardScreen(
    navController: NavController,
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showContent = true
    }
    // Efecto para navegar a la pantalla de bienvenida tras logout
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.ProfesorDashboard.route) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "UmeEgunero - Profesor",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Icono de perfil
                    IconButton(onClick = { navController.navigate(AppScreens.Perfil.route) }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color.White
                        )
                    }
                    // Botón de logout
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ),
            exit = fadeOut()
        ) {
            ProfesorDashboardContent(
                alumnosPendientes = uiState.alumnosPendientes,
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Contenido principal del Dashboard de Profesor
 * 
 * Esta composable organiza todos los elementos visuales del dashboard:
 * - Tarjeta de bienvenida con información del día
 * - Accesos directos a funcionalidades principales
 * - Listado de alumnos pendientes de registro
 * - Próximas actividades y eventos
 *
 * @param alumnosPendientes Lista de alumnos sin registro para el día actual
 * @param navController Controlador de navegación
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun ProfesorDashboardContent(
    alumnosPendientes: List<Alumno>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Sección: Gestión Académica
    val gestionAcademicaCards = listOf(
        CategoriaCardData("Asistencia", "Registra y revisa la asistencia de tus alumnos", Icons.Default.CheckCircle, MaterialTheme.colorScheme.secondary, onClick = { navController.navigate(AppScreens.AsistenciaProfesor.route) }),
        CategoriaCardData("Mis Clases", "Accede a la gestión de tus clases asignadas", Icons.Default.School, MaterialTheme.colorScheme.primary, onClick = { /* TODO: Mis Clases */ }),
        CategoriaCardData("Evaluación", "Evalúa el progreso académico de tus alumnos", Icons.Default.PieChart, MaterialTheme.colorScheme.tertiary, onClick = { navController.navigate(AppScreens.Evaluacion.route) })
    )

    // Sección: Comunicación
    val comunicacionCards = listOf(
        CategoriaCardData("Comunicados", "Consulta y publica comunicados para tus clases", Icons.Default.Description, MaterialTheme.colorScheme.primary, onClick = { navController.navigate(AppScreens.ComunicadosCirculares.route) }),
        CategoriaCardData("Chat", "Comunícate con familias y otros docentes", Icons.AutoMirrored.Filled.Chat, MaterialTheme.colorScheme.primary, onClick = { navController.navigate(AppScreens.ConversacionesProfesor.route) }),
        CategoriaCardData("Incidencias", "Reporta y gestiona incidencias del aula", Icons.Default.Warning, MaterialTheme.colorScheme.error, onClick = { /* TODO: Incidencias */ })
    )

    // Sección: Actividades
    val actividadesCards = listOf(
        CategoriaCardData("Actividades", "Crea y gestiona actividades para tus clases", Icons.Default.PlayCircle, MaterialTheme.colorScheme.secondary, onClick = { navController.navigate(AppScreens.RegistroActividad.route) }),
        CategoriaCardData("Calendario", "Consulta eventos y fechas importantes", Icons.Default.CalendarMonth, MaterialTheme.colorScheme.tertiary, onClick = { navController.navigate(AppScreens.CalendarioProfesor.route) })
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header de bienvenida
        item(span = { GridItemSpan(2) }) {
            CategoriaCardBienvenida(
                data = CategoriaCardData(
                    titulo = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM")).replaceFirstChar { it.uppercase() },
                    descripcion = "2B - Educación Infantil\n15 alumnos a tu cargo",
                    icono = Icons.Default.CalendarToday,
                    color = ProfesorColor,
                    onClick = {},
                    iconTint = Color.White,
                    border = true
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp, max = 100.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ... contenido ...
                }
            }
        }

        // Sección: Gestión Académica
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Gestión Académica",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ProfesorColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            HorizontalDivider(thickness = 2.dp, color = ProfesorColor.copy(alpha = 0.2f))
        }
        items(gestionAcademicaCards.size) { index ->
            val card = gestionAcademicaCards[index]
            CategoriaCard(
                titulo = card.titulo,
                descripcion = card.descripcion,
                icono = card.icono,
                color = ProfesorColor,
                iconTint = card.iconTint,
                border = true,
                onClick = card.onClick,
                modifier = Modifier.padding(4.dp)
            )
        }

        // Sección: Comunicación
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Comunicación",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ProfesorColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            HorizontalDivider(thickness = 2.dp, color = ProfesorColor.copy(alpha = 0.2f))
        }
        items(comunicacionCards.size) { index ->
            val card = comunicacionCards[index]
            CategoriaCard(
                titulo = card.titulo,
                descripcion = card.descripcion,
                icono = card.icono,
                color = ProfesorColor,
                iconTint = card.iconTint,
                border = true,
                onClick = card.onClick,
                modifier = Modifier.padding(4.dp)
            )
        }

        // Sección: Actividades
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Actividades",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ProfesorColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            HorizontalDivider(thickness = 2.dp, color = ProfesorColor.copy(alpha = 0.2f))
        }
        items(actividadesCards.size) { index ->
            val card = actividadesCards[index]
            CategoriaCard(
                titulo = card.titulo,
                descripcion = card.descripcion,
                icono = card.icono,
                color = ProfesorColor,
                iconTint = card.iconTint,
                border = true,
                onClick = card.onClick,
                modifier = Modifier.padding(4.dp)
            )
        }

        // Divider y sección de alumnos pendientes
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Alumnos Pendientes",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ProfesorColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            HorizontalDivider(thickness = 2.dp, color = ProfesorColor.copy(alpha = 0.2f))
        }
        item(span = { GridItemSpan(2) }) {
            AlumnosPendientesResumen(alumnosPendientes)
        }

        // Espaciador final
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Elemento de estadística simple
 */
@Composable
fun EstadisticaItem(
    valor: String,
    titulo: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlumnosPendientesResumen(
    alumnosPendientes: List<Alumno>
) {
    if (alumnosPendientes.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alumnos sin registro hoy: ${alumnosPendientes.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* Navegar a la lista de pendientes */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ver")
                }
            }
        }
    }
}

@Composable
fun GuardarRegistroButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardando...")
        } else {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar registro")
        }
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