package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Pantalla para gestionar diferentes opciones relacionadas con un centro educativo.
 * Esta pantalla presenta un panel con múltiples opciones para la administración completa del centro,
 * incluyendo la gestión de cursos, clases, profesores, alumnos y familias.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCentroScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Centro") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección Gestión Académica
            item {
                Text(
                    text = "Gestión Académica",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }
            
            // Opción para gestionar cursos
            item {
                GestionCentroOptionCard(
                    title = "Gestión de Cursos",
                    description = "Ver, crear, editar y eliminar cursos académicos",
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { navController.navigate(AppScreens.ListaCursos.route) }
                )
            }
            
            // Opción para gestionar clases
            item {
                GestionCentroOptionCard(
                    title = "Gestión de Clases",
                    description = "Organizar aulas, cursos y asignaturas",
                    icon = Icons.Default.Class,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { navController.navigate(AppScreens.ListaClases.route) }
                )
            }
            
            // Sección Gestión de Personas
            item {
                Text(
                    text = "Gestión de Personas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
            }
            
            // Opción para crear usuarios
            item {
                GestionCentroOptionCard(
                    title = "Crear Usuario",
                    description = "Añadir nuevos profesores, alumnos o familias",
                    icon = Icons.Default.PersonAdd,
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = { navController.navigate(AppScreens.CrearUsuarioRapido.route) }
                )
            }
            
            // Opción para vincular profesores a clases
            item {
                GestionCentroOptionCard(
                    title = "Profesores y Clases",
                    description = "Asignar profesores a cursos y clases",
                    icon = Icons.Default.Groups,
                    color = Color(0xFF2E7D32), // Verde oscuro
                    onClick = { navController.navigate(AppScreens.ListaProfesoresClases.route) }
                )
            }
            
            // Opción para vincular familias a alumnos
            item {
                GestionCentroOptionCard(
                    title = "Vinculación Familiar",
                    description = "Conectar alumnos con sus familias",
                    icon = Icons.Default.Person,
                    color = Color(0xFF004D40), // Verde azulado oscuro
                    onClick = { navController.navigate(AppScreens.ListaAlumnosFamilias.route) }
                )
            }
            
            // Sección Ajustes del Centro
            item {
                Text(
                    text = "Ajustes del Centro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
            }
            
            // Opción para volver al panel principal
            item {
                GestionCentroOptionCard(
                    title = "Panel Principal",
                    description = "Volver al panel de administración",
                    icon = Icons.Default.Dashboard,
                    color = Color(0xFF6A1B9A), // Púrpura
                    onClick = { navController.navigate(AppScreens.AdminDashboard.route) {
                        popUpTo(AppScreens.GestionCentros.route) { inclusive = true }
                    }}
                )
            }
            
            // Espacio en blanco al final
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Tarjeta de opción para las diferentes funcionalidades de gestión de centro.
 */
@Composable
fun GestionCentroOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido de texto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Icono de flecha
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
} 