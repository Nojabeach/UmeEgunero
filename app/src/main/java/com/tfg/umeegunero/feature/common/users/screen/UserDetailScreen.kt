package com.tfg.umeegunero.feature.common.users.screen

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
import com.tfg.umeegunero.feature.common.users.viewmodel.UserDetailViewModel
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
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.usuario?.dni?.let { dni ->
                            viewModel.deleteUsuario(dni)
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteConfirmation() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Encabezado con avatar y nombre del usuario
 */
@Composable
private fun UserHeader(usuario: Usuario) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "${usuario.nombre} ${usuario.apellidos}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = usuario.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Fila de información con icono, etiqueta y valor
 */
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Sección de información específica para profesores
 */
@Composable
private fun ProfesorInfoSection(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información del Profesor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Aquí puedes agregar información específica del profesor
            // Por ejemplo: especialidad, cursos asignados, etc.
        }
    }
}

/**
 * Sección de información específica para alumnos
 */
@Composable
private fun AlumnoInfoSection(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información del Alumno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Aquí puedes agregar información específica del alumno
            // Por ejemplo: curso, clase, notas, etc.
        }
    }
}

/**
 * Sección de información específica para familiares
 */
@Composable
private fun FamiliarInfoSection(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información del Familiar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Aquí puedes agregar información específica del familiar
            // Por ejemplo: alumnos asociados, permisos, etc.
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun UserDetailScreenPreview() {
    UmeEguneroTheme {
        val navController = rememberNavController()
        UserDetailScreen(
            navController = navController,
            dni = "12345678A"
        )
    }
} 