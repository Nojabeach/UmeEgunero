package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.EstadoAsistencia
import com.tfg.umeegunero.feature.profesor.viewmodel.AsistenciaViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.HorizontalDivider

/**
 * Pantalla de registro de asistencia
 * Permite al profesor marcar la asistencia diaria de sus alumnos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    navController: NavController,
    viewModel: AsistenciaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Fecha actual formateada
    val fechaActual = remember {
        val formatter = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        formatter.format(Date()).replaceFirstChar { it.uppercase() }
    }

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Registro de Asistencia") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón para guardar la asistencia
                    IconButton(
                        onClick = { viewModel.guardarAsistencia() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar asistencia"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Encabezado con fecha
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Asistencia del día",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = fechaActual,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Clase: ${uiState.nombreClase}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = "${uiState.alumnos.size} alumnos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Leyenda
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LeyendaItem(
                                color = MaterialTheme.colorScheme.primary,
                                texto = "Presente"
                            )
                            
                            LeyendaItem(
                                color = Color.Red.copy(alpha = 0.7f),
                                texto = "Ausente"
                            )
                            
                            LeyendaItem(
                                color = Color.Yellow.copy(alpha = 0.7f),
                                texto = "Retraso"
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedVisibility(
                    visible = !uiState.isLoading && uiState.alumnos.isEmpty(),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay alumnos asignados a esta clase",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = !uiState.isLoading && uiState.alumnos.isNotEmpty(),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    // Lista de alumnos
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.alumnos) { alumno ->
                            val estadoAsistencia = uiState.estadosAsistencia[alumno.id]
                            
                            AlumnoAsistenciaItem(
                                alumno = alumno,
                                estadoAsistencia = estadoAsistencia ?: EstadoAsistencia.PRESENTE,
                                onEstadoAsistenciaChange = { nuevoEstado ->
                                    viewModel.actualizarEstadoAsistencia(alumno.id, nuevoEstado)
                                }
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Botón flotante para guardar
            FloatingActionButton(
                onClick = { viewModel.guardarAsistencia() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = ProfesorColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Guardar asistencia"
                )
            }
        }
    }
}

@Composable
fun LeyendaItem(
    color: Color,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun AlumnoAsistenciaItem(
    alumno: Alumno,
    estadoAsistencia: EstadoAsistencia,
    onEstadoAsistenciaChange: (EstadoAsistencia) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Foto o inicial del alumno
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = alumno.nombre.firstOrNull()?.toString() ?: "A",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Datos del alumno
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${alumno.nombre} ${alumno.apellidos}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Botones de estado de asistencia
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EstadoAsistenciaButton(
                estado = EstadoAsistencia.PRESENTE,
                estadoActual = estadoAsistencia,
                onEstadoChange = onEstadoAsistenciaChange,
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.CheckCircle
            )
            
            EstadoAsistenciaButton(
                estado = EstadoAsistencia.AUSENTE,
                estadoActual = estadoAsistencia,
                onEstadoChange = onEstadoAsistenciaChange,
                color = Color.Red.copy(alpha = 0.7f),
                icon = Icons.Default.Cancel
            )
            
            EstadoAsistenciaButton(
                estado = EstadoAsistencia.RETRASADO,
                estadoActual = estadoAsistencia,
                onEstadoChange = onEstadoAsistenciaChange,
                color = Color.Yellow.copy(alpha = 0.7f),
                icon = Icons.Default.Schedule
            )
        }
    }
}

@Composable
fun EstadoAsistenciaButton(
    estado: EstadoAsistencia,
    estadoActual: EstadoAsistencia,
    onEstadoChange: (EstadoAsistencia) -> Unit,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val isSelected = estado == estadoActual
    
    IconButton(
        onClick = { onEstadoChange(estado) },
        modifier = Modifier
            .size(40.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .border(2.dp, color, CircleShape)
                        .background(color.copy(alpha = 0.1f), CircleShape)
                } else {
                    Modifier
                }
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Marcar como ${estado.name.lowercase()}",
            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AsistenciaScreenPreview() {
    UmeEguneroTheme {
        AsistenciaScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AsistenciaScreenDarkPreview() {
    UmeEguneroTheme {
        AsistenciaScreen(navController = rememberNavController())
    }
} 