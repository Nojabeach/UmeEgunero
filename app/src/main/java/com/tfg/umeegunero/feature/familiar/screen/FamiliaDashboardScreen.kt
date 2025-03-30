package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaDashboardScreen(
    navController: NavController
) {
    // Datos de ejemplo
    val alumnos = remember {
        listOf(
            AlumnoInfo("1", "Ana", "López", "3A - Infantil", R.drawable.placeholder_child_1),
            AlumnoInfo("2", "Pablo", "López", "4B - Infantil", R.drawable.placeholder_child_2)
        )
    }
    
    val proximosEventos = remember {
        listOf(
            EventoInfo("Reunión de padres", "10:00 - 11:30", "Mañana", Color(0xFF1976D2)),
            EventoInfo("Fiesta de primavera", "16:00 - 19:00", "23/05/2023", Color(0xFF009688)),
            EventoInfo("Excursión al museo", "09:00 - 14:00", "25/05/2023", Color(0xFFFF9800))
        )
    }
    
    val notificaciones = remember {
        listOf(
            NotificacionInfo("Profesora Laura", "Ha añadido una nueva tarea para Ana", "Hace 2 horas"),
            NotificacionInfo("Centro escolar", "Recordatorio: Día sin clase el próximo lunes", "Ayer"),
            NotificacionInfo("Profesora Carmen", "Pablo ha obtenido una estrella por buen comportamiento", "Ayer")
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Familia López") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Abrir notificaciones */ }) {
                        BadgedBox(
                            badge = {
                                Badge { Text("3") }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    IconButton(onClick = { /* Abrir perfil */ }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Sección de mis alumnos
            item {
                Text(
                    text = "Mis Estudiantes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    alumnos.forEach { alumno ->
                        item {
                            AlumnoCard(
                                alumno = alumno,
                                onClick = {
                                    navController.navigate(AppScreens.DetalleAlumnoFamilia.createRoute(alumno.id))
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Sección de acciones rápidas
            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    item {
                        AccionRapidaCard(
                            title = "Calendario",
                            icon = Icons.Default.CalendarToday,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = {
                                navController.navigate(AppScreens.CalendarioFamilia.route)
                            }
                        )
                    }
                    
                    item {
                        AccionRapidaCard(
                            title = "Chat",
                            icon = Icons.Default.Chat,
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = {
                                navController.navigate(AppScreens.ChatFamilia.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    
                    item {
                        AccionRapidaCard(
                            title = "Tareas",
                            icon = Icons.Default.Assignment,
                            color = MaterialTheme.colorScheme.tertiary,
                            onClick = {
                                navController.navigate(AppScreens.TareasFamilia.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    
                    item {
                        AccionRapidaCard(
                            title = "Actividades Preescolar",
                            icon = Icons.Default.ChildCare,
                            color = Color(0xFF8E24AA), // Morado
                            onClick = {
                                navController.navigate(AppScreens.ActividadesPreescolar.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    
                    item {
                        AccionRapidaCard(
                            title = "Asistencia",
                            icon = Icons.Default.EventAvailable,
                            color = Color(0xFF00897B), // Verde azulado
                            onClick = {
                                // Navegación a asistencia
                            }
                        )
                    }
                    
                    item {
                        AccionRapidaCard(
                            title = "Notas",
                            icon = Icons.AutoMirrored.Filled.List,
                            color = Color(0xFFFFB300), // Ámbar
                            onClick = {
                                // Navegación a notas
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Sección de próximos eventos
            item {
                Text(
                    text = "Próximos Eventos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                if (proximosEventos.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay eventos próximos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    proximosEventos.forEach { evento ->
                        EventoCard(
                            evento = evento,
                            onClick = { /* Ver detalle del evento */ },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Sección de notificaciones recientes
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notificaciones Recientes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(
                        onClick = { 
                            navController.navigate(AppScreens.NotificacionesFamilia.route)
                        }
                    ) {
                        Text("Ver todas")
                    }
                }
                
                if (notificaciones.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay notificaciones recientes",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    notificaciones.forEach { notificacion ->
                        NotificacionCard(
                            notificacion = notificacion,
                            onClick = { /* Ver detalle de la notificación */ },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlumnoCard(
    alumno: AlumnoInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.first().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nombre
            Text(
                text = alumno.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Clase
            Text(
                text = alumno.clase,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AccionRapidaCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EventoCard(
    evento: EventoInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de color
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(evento.color)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del evento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = evento.horario,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Fecha
            Text(
                text = evento.fecha,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun NotificacionCard(
    notificacion: NotificacionInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notificacion.emisor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = notificacion.tiempo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notificacion.mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Modelos de datos para la pantalla
data class AlumnoInfo(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val clase: String,
    val avatarRes: Int
)

data class EventoInfo(
    val titulo: String,
    val horario: String,
    val fecha: String,
    val color: Color
)

data class NotificacionInfo(
    val emisor: String,
    val mensaje: String,
    val tiempo: String
)

// Clase temporal para recursos de imagen
object R {
    object drawable {
        const val placeholder_child_1 = 0
        const val placeholder_child_2 = 0
    }
} 