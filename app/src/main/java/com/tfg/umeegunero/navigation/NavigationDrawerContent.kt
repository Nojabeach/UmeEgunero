package com.tfg.umeegunero.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario

/**
 * Contenido del navigation drawer
 * 
 * @param navController Controlador de navegación
 * @param userType Tipo de usuario actual
 * @param onItemClick Callback para cuando se hace clic en un ítem del menú
 * @param onCloseDrawer Callback para cerrar el drawer
 * @param onLogout Callback para cerrar sesión
 */
@Composable
fun NavigationDrawerContent(
    navController: NavController,
    userType: TipoUsuario,
    onItemClick: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    // Cabecera del drawer
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp)
    ) {
        // Logo y título
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UmeEgunero",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Opciones comunes
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = false,
            onClick = { onItemClick(AppScreens.Welcome.route) }
        )
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Mi Perfil") },
            selected = false,
            onClick = { onItemClick(AppScreens.Perfil.route) }
        )
        
        // Opciones específicas según tipo de usuario
        when (userType) {
            TipoUsuario.ADMIN_APP -> {
                // Opciones para administrador
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Centros") },
                    label = { Text("Gestión de Centros") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.GestionCentros.route) }
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                    label = { Text("Configuración") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.Config.route) }
                )
            }
            TipoUsuario.FAMILIAR -> {
                // Opciones para familiar
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Face, contentDescription = "Mis Hijos") },
                    label = { Text("Mis Hijos") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.ListaAlumnos.route) }
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Actividades") },
                    label = { Text("Actividades Preescolares") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.ActividadesPreescolar.route) }
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Mensajes") },
                    label = { Text("Mensajes") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.ConversacionesFamilia.route) }
                )
            }
            TipoUsuario.PROFESOR -> {
                // Opciones para profesor
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Mis Clases") },
                    label = { Text("Mis Clases") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.Clases.route) }
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Actividades") },
                    label = { Text("Actividades Preescolares") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.ActividadesPreescolarProfesor.route) }
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Mensajes") },
                    label = { Text("Mensajes") },
                    selected = false,
                    onClick = { onItemClick(AppScreens.ConversacionesProfesor.route) }
                )
            }
            else -> {
                // Otras opciones para otros tipos de usuario
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Cerrar sesión al final
        NavigationDrawerItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión") },
            label = { Text("Cerrar Sesión") },
            selected = false,
            onClick = { onLogout() }
        )
    }
} 