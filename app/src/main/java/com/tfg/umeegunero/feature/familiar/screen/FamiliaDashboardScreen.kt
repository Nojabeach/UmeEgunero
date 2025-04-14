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
    
    // Si ocurrió un error, mostramos un Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    LaunchedEffect(Unit) {
        // Cargar datos del familiar al inicio
        viewModel.cargarDatosFamiliar()
    }
    
    // Observamos si debemos navegar a la pantalla de welcome
    LaunchedEffect(uiState.navigateToWelcome) {
        if (uiState.navigateToWelcome) {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo(AppScreens.FamiliarDashboard.route) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Dashboard Familiar",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de mensajes con badge para notificaciones
                    BadgedBox(
                        badge = {
                            if (uiState.totalMensajesNoLeidos > 0) {
                                Badge { Text(uiState.totalMensajesNoLeidos.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = {
                            navController.navigate(AppScreens.ConversacionesFamilia.route)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Mensajes"
                            )
                        }
                    }
                    
                    // Menú de opciones
                    var showMenu by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi perfil") },
                            onClick = {
                                showMenu = false
                                navController.navigate("perfil")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            onClick = {
                                showMenu = false
                                navController.navigate("configuracion")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null
                                )
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Mostrar un indicador de carga mientras se cargan los datos
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Mostrar el contenido principal cuando los datos estén cargados
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de bienvenida
                WelcomeCard(
                    nombreAlumno = uiState.familiar?.nombre ?: "Familiar",
                    nombreHijo = uiState.hijoSeleccionado?.nombre ?: "Alumno"
                )
                
                // Selector de hijos (si hay más de uno)
                if (uiState.hijos.size > 1) {
                    HijosSelector(
                        hijos = uiState.hijos,
                        hijoSeleccionado = uiState.hijoSeleccionado,
                        onHijoSelected = { viewModel.seleccionarHijo(it) }
                    )
                }
                
                // Tarjeta de resumen de actividad diaria
                if (uiState.hijoSeleccionado != null) {
                    ResumenActividadCard(
                        alumno = uiState.hijoSeleccionado!!,
                        registrosActividad = uiState.registrosActividad,
                        onVerDetalles = { registroId ->
                            // Navegar a la pantalla de detalle del registro
                            navController.navigate("detalle_registro/$registroId")
                        }
                    )
                }
                
                // Accesos rápidos
                QuickActionsCard(
                    onVerMensajes = { navController.navigate(AppScreens.ConversacionesFamilia.route) },
                    onVerCalendario = { navController.navigate(AppScreens.CalendarioFamilia.route) },
                    onVerHistorial = { 
                        if (uiState.hijoSeleccionado != null) {
                            val alumnoId = uiState.hijoSeleccionado!!.dni
                            val alumnoNombre = uiState.hijoSeleccionado!!.nombre
                            navController.navigate(
                                AppScreens.ConsultaRegistroDiario.createRoute(
                                    alumnoId = alumnoId,
                                    alumnoNombre = alumnoNombre
                                )
                            )
                        }
                    },
                    onVerComunicados = { navController.navigate(AppScreens.ComunicadosFamilia.route) }
                )
                
                // Notificaciones y comunicados recientes
                NotificacionesCard(
                    totalNoLeidas = uiState.registrosSinLeer,
                    onVerTodas = { navController.navigate(AppScreens.NotificacionesFamilia.route) }
                )
            }
        }
    }
}

@Composable
fun WelcomeCard(nombreAlumno: String, nombreHijo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FamiliarColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hola, $nombreAlumno",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Familia de $nombreHijo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Centro Infantil UmeEgunero",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Última actualización: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HijosSelector(
    hijos: List<Alumno>,
    hijoSeleccionado: Alumno?,
    onHijoSelected: (Alumno) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Selecciona un hijo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(hijos) { hijo ->
                    val isSelected = hijo.dni == hijoSeleccionado?.dni
                    
                    Card(
                        modifier = Modifier
                            .width(120.dp)
                            .height(140.dp)
                            .clickable { onHijoSelected(hijo) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                FamiliarColor.copy(alpha = 0.3f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) 
                            BorderStroke(2.dp, FamiliarColor) 
                        else 
                            null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Avatar/Inicial del alumno
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) FamiliarColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = hijo.nombre.first().toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = hijo.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenActividadCard(
    alumno: Alumno,
    registrosActividad: List<RegistroActividad>,
    onVerDetalles: (String) -> Unit
) {
    val ultimoRegistro = registrosActividad.maxByOrNull { it.fecha }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
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
                    text = "Resumen de actividad",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { 
                    if (ultimoRegistro != null) {
                        onVerDetalles(ultimoRegistro.id)
                    }
                }) {
                    Text("Ver más")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (ultimoRegistro != null) {
                val fecha = ultimoRegistro.fecha
                val fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(fecha.seconds * 1000))
                
                Text(
                    text = "Último registro: $fechaFormateada",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mostrar información básica del registro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActividadInfoItem(
                        icon = Icons.Default.Restaurant,
                        title = "Alimentación",
                        value = when {
                            ultimoRegistro.comidas.primerPlato.nivelConsumo == NivelConsumo.valueOf("COMPLETO") -> "Completa"
                            ultimoRegistro.comidas.primerPlato.nivelConsumo == NivelConsumo.valueOf("PARCIAL") -> "Parcial"
                            else -> "No servido"
                        }
                    )
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bedtime,
                        title = "Siesta",
                        value = if (ultimoRegistro.siesta?.duracion ?: 0 > 0) 
                            "${ultimoRegistro.siesta?.duracion} min" 
                        else 
                            "No"
                    )
                    
                    ActividadInfoItem(
                        icon = Icons.Default.Bathroom,
                        title = "Necesidades",
                        value = if (ultimoRegistro.necesidadesFisiologicas.caca) "Sí" else "No"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Observaciones
                if (!ultimoRegistro.observaciones.isNullOrBlank()) {
                    Text(
                        text = "Observaciones:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = ultimoRegistro.observaciones ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No hay observaciones para este registro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                // No hay registros disponibles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay registros de actividad disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActividadInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = FamiliarColor,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuickActionsCard(
    onVerMensajes: () -> Unit,
    onVerCalendario: () -> Unit,
    onVerHistorial: () -> Unit,
    onVerComunicados: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Accesos Rápidos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    text = "Mensajes",
                    onClick = onVerMensajes,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                ActionButton(
                    icon = Icons.Default.CalendarMonth,
                    text = "Calendario",
                    onClick = onVerCalendario,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                ActionButton(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    text = "Historial",
                    onClick = onVerHistorial,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                ActionButton(
                    icon = Icons.Default.Announcement,
                    text = "Comunicados",
                    onClick = onVerComunicados,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = FamiliarColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NotificacionesCard(
    totalNoLeidas: Int,
    onVerTodas: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (totalNoLeidas > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = totalNoLeidas.toString(),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Mostrar notificaciones
            if (totalNoLeidas > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onVerTodas)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Tienes $totalNoLeidas ${if (totalNoLeidas == 1) "registro" else "registros"} sin leer",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    text = "No hay notificaciones pendientes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onVerTodas,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FamiliarColor
                )
            ) {
                Text("Ver todas")
            }
        }
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