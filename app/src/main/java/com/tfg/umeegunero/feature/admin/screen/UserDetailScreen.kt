package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.feature.admin.viewmodel.UserDetailViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import java.util.Date
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla que muestra los detalles de un usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    dni: String,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Efecto para cargar los datos del usuario al inicio
    LaunchedEffect(dni) {
        viewModel.loadUsuario(dni)
    }
    
    // Mostrar error en Snackbar si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Usuario") },
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
                ),
                actions = {
                    // Botón de editar
                    IconButton(
                        onClick = {
                            when {
                                uiState.usuario?.perfiles?.any { it.tipo == TipoUsuario.PROFESOR } == true -> 
                                    navController.navigate(AppScreens.Dummy.createRoute("Editar Profesor"))
                                uiState.usuario?.perfiles?.any { it.tipo == TipoUsuario.ALUMNO } == true -> 
                                    navController.navigate(AppScreens.Dummy.createRoute("Editar Alumno"))
                                uiState.usuario?.perfiles?.any { it.tipo == TipoUsuario.FAMILIAR } == true -> 
                                    navController.navigate(AppScreens.Dummy.createRoute("Editar Familiar"))
                                else -> navController.navigate(AppScreens.Dummy.createRoute("Editar Usuario"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(message = "Cargando información del usuario...")
            }
        } else if (uiState.usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Usuario no encontrado",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Volver")
                    }
                }
            }
        } else {
            // Contenido principal con los detalles del usuario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Encabezado con avatar y nombre
                UserHeader(usuario = uiState.usuario!!)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Información personal
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InfoRow(
                            icon = Icons.Default.Badge,
                            label = "DNI",
                            value = uiState.usuario!!.dni
                        )
                        
                        InfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = uiState.usuario!!.email
                        )
                        
                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = "Teléfono",
                            value = uiState.usuario!!.telefono ?: "No disponible"
                        )
                        
                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "Tipo de Usuario",
                            value = when {
                                uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP } -> "Administrador de la Aplicación"
                                uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.ADMIN_CENTRO } -> "Administrador de Centro"
                                uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.PROFESOR } -> "Profesor"
                                uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.ALUMNO } -> "Alumno"
                                uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.FAMILIAR } -> "Familiar"
                                else -> "No especificado"
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Información relacionada según el tipo de usuario
                when {
                    uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.PROFESOR } -> ProfesorInfoSection(usuario = uiState.usuario!!)
                    uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.ALUMNO } -> AlumnoInfoSection(usuario = uiState.usuario!!)
                    uiState.usuario!!.perfiles.any { it.tipo == TipoUsuario.FAMILIAR } -> FamiliarInfoSection(usuario = uiState.usuario!!)
                    else -> { /* No mostrar sección adicional */ }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón para eliminar usuario
                OutlinedButton(
                    onClick = { 
                        viewModel.showDeleteConfirmation()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Usuario")
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.hideDeleteConfirmation()
            },
            title = { Text("Confirmar eliminación") },
            text = { 
                val nombre = uiState.usuario?.nombre ?: ""
                val apellidos = uiState.usuario?.apellidos ?: ""
                Text("¿Estás seguro de que deseas eliminar al usuario $nombre $apellidos? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.usuario?.let {
                            viewModel.deleteUsuario(it.dni) 
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserHeader(usuario: Usuario) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar (representado como un círculo con la inicial)
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = usuario.nombre.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Nombre completo
        Text(
            text = "${usuario.nombre} ${usuario.apellidos}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Tipo de usuario
        val tipoText = when {
            usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP } -> "Administrador de la Aplicación"
            usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_CENTRO } -> "Administrador de Centro"
            usuario.perfiles.any { it.tipo == TipoUsuario.PROFESOR } -> "Profesor"
            usuario.perfiles.any { it.tipo == TipoUsuario.ALUMNO } -> "Alumno"
            usuario.perfiles.any { it.tipo == TipoUsuario.FAMILIAR } -> "Familiar"
            else -> "Usuario"
        }
        
        Text(
            text = tipoText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ProfesorInfoSection(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información de Profesor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Como es una pantalla de ejemplo, mostramos datos ficticios
            InfoRow(
                icon = Icons.Default.School,
                label = "Centro Educativo",
                value = usuario.perfiles.firstOrNull { it.tipo == TipoUsuario.PROFESOR }?.centroId ?: "No asignado"
            )
            
            InfoRow(
                icon = Icons.Default.Class,
                label = "Clases Asignadas",
                value = "3 clases"
            )
            
            InfoRow(
                icon = Icons.Default.CalendarMonth,
                label = "Horario",
                value = "Lunes a Viernes, 8:00 - 14:00"
            )
        }
    }
}

@Composable
fun AlumnoInfoSection(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información de Alumno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Como es una pantalla de ejemplo, mostramos datos ficticios
            InfoRow(
                icon = Icons.Default.School,
                label = "Centro Educativo",
                value = usuario.perfiles.firstOrNull { it.tipo == TipoUsuario.ALUMNO }?.centroId ?: "No asignado"
            )
            
            InfoRow(
                icon = Icons.Default.Class,
                label = "Clase",
                value = "Aula de 3 años A"
            )
            
            InfoRow(
                icon = Icons.Default.Groups,
                label = "Familiares Vinculados",
                value = "2 familiares"
            )
        }
    }
}

@Composable
fun FamiliarInfoSection(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información de Familiar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Como es una pantalla de ejemplo, mostramos datos ficticios
            InfoRow(
                icon = Icons.Default.ChildCare,
                label = "Alumnos Vinculados",
                value = "${usuario.perfiles.firstOrNull { it.tipo == TipoUsuario.FAMILIAR }?.alumnos?.size ?: 0} alumno(s)"
            )
            
            InfoRow(
                icon = Icons.Default.School,
                label = "Centro Educativo",
                value = usuario.perfiles.firstOrNull { it.tipo == TipoUsuario.FAMILIAR }?.centroId ?: "No asignado"
            )
            
            InfoRow(
                icon = Icons.Default.Notifications,
                label = "Notificaciones",
                value = "Activadas"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserDetailScreenPreview() {
    UmeEguneroTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con avatar y nombre
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar (representado como un círculo con la inicial)
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "L",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nombre completo
                Text(
                    text = "Laura Martínez García",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Tipo de usuario
                Text(
                    text = "Profesor",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información personal
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // DNI
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "DNI",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "12345678A",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserDetailScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con avatar y nombre
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar (representado como un círculo con la inicial)
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "M",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nombre completo
                Text(
                    text = "María López Sánchez",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Tipo de usuario
                Text(
                    text = "Familiar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 