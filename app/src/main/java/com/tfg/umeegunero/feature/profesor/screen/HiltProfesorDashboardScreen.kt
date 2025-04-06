package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Dashboard del profesor con Hilt
 * Se encarga de mostrar las opciones principales para el profesor
 * y navegar a las diferentes pantallas de la aplicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiltProfesorDashboardScreen(
    navController: NavController,
    viewModel: ProfesorDashboardViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profesor Dashboard", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate(AppScreens.TareasProfesor.route)
        }) {
            Text("Gestionar Tareas")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(AppScreens.ConversacionesProfesor.route)
        }) {
            Text("Mensajes")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(AppScreens.Welcome.route) {
                popUpTo("profesor_graph") { inclusive = true }
            }
        }) {
            Text("Cerrar Sesión")
        }
    }
}

/**
 * Contenido principal de la pantalla del profesor
 */
@Composable
fun ProfesorHomeContent(
    alumnosPendientes: Int,
    onCrearRegistroActividad: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tarjetas de información
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Alumnos Pendientes",
                value = alumnosPendientes.toString(),
                icon = Icons.Default.Person,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.AsistenciaProfesor.route)
                    }
            )
            
            InfoCard(
                title = "Asistencia",
                value = "Pendiente",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.AsistenciaProfesor.route)
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Segunda fila de tarjetas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Tareas",
                value = "2 Pendientes",
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.TareasProfesor.route)
                    }
            )
            
            InfoCard(
                title = "Eventos",
                value = "Próximamente",
                icon = Icons.Default.CalendarToday,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(AppScreens.CalendarioProfesor.route)
                    }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Acciones rápidas
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Registrar Asistencia",
                icon = Icons.Default.CheckCircle,
                onClick = {
                    navController.navigate(AppScreens.AsistenciaProfesor.route)
                }
            )
            
            ActionButton(
                text = "Enviar Mensaje a Padres",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                    navController.navigate(AppScreens.ConversacionesProfesor.route)
                }
            )
            
            ActionButton(
                text = "Crear Actividad",
                icon = Icons.Default.Add,
                onClick = onCrearRegistroActividad
            )
        }
    }
}

/**
 * Tarjeta de información con título, valor e icono
 */
@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
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
 * Botón de acción con texto e icono
 */
@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
