package com.tfg.umeegunero.feature.common.academico.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.academico.viewmodel.DetalleClaseViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.DetalleClaseUiState
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.AcademicoColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.tfg.umeegunero.notification.NotificationHelper
import kotlinx.coroutines.launch
import com.tfg.umeegunero.navigation.AppScreens
import timber.log.Timber

/**
 * Pantalla de detalle de una clase que muestra información completa sobre:
 * - Datos generales de la clase
 * - Profesor titular
 * - Profesores auxiliares
 * - Lista de alumnos
 * 
 * @param navController Controlador de navegación
 * @param claseId ID de la clase a mostrar
 * @param viewModel ViewModel de detalle de clase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleClaseScreen(
    navController: NavController,
    claseId: String,
    viewModel: DetalleClaseViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.clase?.nombre ?: "Detalle de Clase", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        // En lugar de simplemente hacer popBackStack, guardamos la información
                        // del curso y la clase antes de volver atrás
                        val clase = uiState.clase
                        if (clase != null) {
                            val centroId = clase.centroId
                            val cursoId = clase.cursoId
                            Timber.d("🔙 Navegando de vuelta de DetalleClaseScreen con centroId=$centroId, cursoId=$cursoId")
                            
                            // Guardar los IDs en las preferencias compartidas para que GestorAcademicoScreen
                            // pueda recuperarlos al volver
                            val prefs = context.getSharedPreferences("gestor_academico_prefs", 0)
                            prefs.edit()
                                .putString("last_centro_id", centroId)
                                .putString("last_curso_id", cursoId)
                                .apply()
                            
                            // Simplemente hacemos popBackStack() para volver a la pantalla anterior
                            navController.popBackStack()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    fullScreen = true
                )
            } else if (uiState.error != null) {
                // Mostrar error
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .align(Alignment.Center),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error al cargar los datos de la clase",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Volver")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Información general de la clase
                    item {
                        InfoGeneralCard(uiState)
                    }
                    // Profesor titular
                    item {
                        uiState.profesorTitular?.let { profesor ->
                            ProfesorCard(
                                nombre = "${profesor.nombre} ${profesor.apellidos}",
                                email = profesor.email,
                                telefono = profesor.telefono ?: "",
                                esTitular = true,
                                navController = navController
                            )
                        }
                    }
                    // Profesores auxiliares
                    if (uiState.profesoresAuxiliares.isNotEmpty()) {
                        item {
                            Text(
                                text = "Profesores Auxiliares",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(uiState.profesoresAuxiliares) { profesor ->
                            ProfesorCard(
                                nombre = "${profesor.nombre} ${profesor.apellidos}",
                                email = profesor.email,
                                telefono = profesor.telefono ?: "",
                                esTitular = false,
                                navController = navController
                            )
                        }
                    }
                    // Lista de alumnos
                    if (uiState.alumnos.isNotEmpty()) {
                        item {
                            Text(
                                text = "Alumnos (${uiState.alumnos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(uiState.alumnos) { alumno ->
                            AlumnoItem(
                                nombre = "${alumno.nombre} ${alumno.apellidos}",
                                dni = alumno.dni
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay alumnos asignados a esta clase",
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    // Espacio adicional al final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta con información general de la clase
 */
@Composable
private fun InfoGeneralCard(uiState: DetalleClaseUiState) {
    uiState.clase?.let { clase ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = clase.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = "Aula: ${clase.aula}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = "Horario: ${clase.horario}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = "Capacidad: ${clase.capacidadMaxima} alumnos",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Text(
                        text = "Ocupación: ${uiState.alumnos.size}/${clase.capacidadMaxima ?: 0}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                if (uiState.alumnos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val capacidadMaxima = clase.capacidadMaxima ?: 1 // Evitar división por cero
                    LinearProgressIndicator(
                        progress = { uiState.alumnos.size.toFloat() / capacidadMaxima.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            uiState.alumnos.size >= capacidadMaxima -> MaterialTheme.colorScheme.error
                            uiState.alumnos.size >= (capacidadMaxima * 0.8f).toInt() -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de profesor (titular o auxiliar)
 */
@Composable
private fun ProfesorCard(
    nombre: String,
    email: String,
    telefono: String,
    esTitular: Boolean,
    navController: NavController
) {
    val context = LocalContext.current
    var showEnviarNotificacionDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esTitular) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o iniciales
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (esTitular) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.tertiary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.firstOrNull()?.toString() ?: "P",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = if (esTitular) "Profesor/a Titular" else "Profesor/a Auxiliar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (esTitular) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (esTitular) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (telefono.isNotEmpty()) {
                    Text(
                        text = telefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (esTitular) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = { showEnviarNotificacionDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Enviar mensaje",
                    tint = if (esTitular) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
    
    // Diálogo para enviar mensaje al profesor
    if (showEnviarNotificacionDialog) {
        AlertDialog(
            onDismissRequest = { showEnviarNotificacionDialog = false },
            title = { 
                Text(
                    "Enviar mensaje al profesor", 
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Deseas enviar un mensaje a $nombre?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Se utilizará el sistema de mensajería unificado de UmeEgunero para comunicarte con el profesor de manera eficiente.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            // Navegar directamente a la pantalla de nuevo mensaje con el destinatario preseleccionado
                            navController.navigate(AppScreens.NewMessage.createRoute(
                                receiverId = email,
                                messageType = "CHAT",
                                receiverName = nombre
                            ))
                            showEnviarNotificacionDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error al abrir sistema de mensajes: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar mensaje")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEnviarNotificacionDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Item de alumno en la lista
 */
@Composable
private fun AlumnoItem(
    nombre: String,
    dni: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iniciales o avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.firstOrNull()?.toString() ?: "A",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "DNI: $dni",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
} 