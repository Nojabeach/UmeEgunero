package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.feature.centro.viewmodel.HistorialSolicitudesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla que muestra el historial de solicitudes de vinculación
 * procesadas por los administradores del centro.
 *
 * Esta pantalla permite visualizar todas las solicitudes de vinculación
 * familiar-alumno que han sido procesadas (aprobadas o rechazadas), junto
 * con información de quién tomó la decisión y cuándo.
 *
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona los datos de historial
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSolicitudesScreen(
    navController: NavController,
    viewModel: HistorialSolicitudesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.cargarHistorialSolicitudes()
    }
    
    var filtroEstado by remember { mutableStateOf<EstadoSolicitud?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Solicitudes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cargando historial de solicitudes...")
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // Filtros para el estado
                FiltrosEstado(
                    estadoSeleccionado = filtroEstado,
                    onFiltroSeleccionado = { filtroEstado = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lista de solicitudes
                if (uiState.solicitudes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay solicitudes en el historial",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    val solicitudesFiltradas = if (filtroEstado != null) {
                        uiState.solicitudes.filter { it.estado == filtroEstado }
                    } else {
                        uiState.solicitudes
                    }
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(solicitudesFiltradas) { solicitud ->
                            SolicitudHistorialItem(solicitud = solicitud)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra los filtros de estado para el historial
 */
@Composable
fun FiltrosEstado(
    estadoSeleccionado: EstadoSolicitud?,
    onFiltroSeleccionado: (EstadoSolicitud?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filtrar por estado:",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FiltroEstadoItem(
                    estado = null,
                    seleccionado = estadoSeleccionado == null,
                    onClick = { onFiltroSeleccionado(null) },
                    label = "Todos"
                )
                
                FiltroEstadoItem(
                    estado = EstadoSolicitud.APROBADA,
                    seleccionado = estadoSeleccionado == EstadoSolicitud.APROBADA,
                    onClick = { onFiltroSeleccionado(EstadoSolicitud.APROBADA) },
                    label = "Aprobadas"
                )
                
                FiltroEstadoItem(
                    estado = EstadoSolicitud.RECHAZADA,
                    seleccionado = estadoSeleccionado == EstadoSolicitud.RECHAZADA,
                    onClick = { onFiltroSeleccionado(EstadoSolicitud.RECHAZADA) },
                    label = "Rechazadas"
                )
                
                FiltroEstadoItem(
                    estado = EstadoSolicitud.PENDIENTE,
                    seleccionado = estadoSeleccionado == EstadoSolicitud.PENDIENTE,
                    onClick = { onFiltroSeleccionado(EstadoSolicitud.PENDIENTE) },
                    label = "Pendientes"
                )
            }
        }
    }
}

/**
 * Item de filtro de estado
 */
@Composable
fun FiltroEstadoItem(
    estado: EstadoSolicitud?,
    seleccionado: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 4.dp)
    ) {
        RadioButton(
            selected = seleccionado,
            onClick = onClick
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/**
 * Componente que muestra una solicitud en el historial
 */
@Composable
fun SolicitudHistorialItem(solicitud: SolicitudVinculacion) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = when (solicitud.estado) {
                EstadoSolicitud.APROBADA -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (solicitud.estado) {
                                    EstadoSolicitud.APROBADA -> Color(0xFF4CAF50)
                                    EstadoSolicitud.RECHAZADA -> Color(0xFFF44336)
                                    EstadoSolicitud.PENDIENTE -> Color(0xFFFFC107)
                                }
                            )
                    )
                    
                    Text(
                        text = when (solicitud.estado) {
                            EstadoSolicitud.APROBADA -> "APROBADA"
                            EstadoSolicitud.RECHAZADA -> "RECHAZADA"
                            EstadoSolicitud.PENDIENTE -> "PENDIENTE"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (solicitud.estado) {
                            EstadoSolicitud.APROBADA -> Color(0xFF4CAF50)
                            EstadoSolicitud.RECHAZADA -> Color(0xFFF44336)
                            EstadoSolicitud.PENDIENTE -> Color(0xFFFFC107)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                // Fecha de solicitud
                InfoItem(
                    icon = Icons.Default.CalendarMonth,
                    text = solicitud.fechaSolicitud.toDate().let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    },
                    showIcon = false
                )
            }
            
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            
            // Información del familiar y alumno
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoItem(
                        icon = Icons.Default.Person,
                        text = solicitud.nombreFamiliar.ifEmpty { "Familiar: ${solicitud.familiarId}" }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoItem(
                        icon = Icons.Default.School,
                        text = solicitud.alumnoNombre.ifEmpty { "Alumno: ${solicitud.alumnoDni}" }
                    )
                }
                
                // Tipo de relación si existe
                if (solicitud.tipoRelacion.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = solicitud.tipoRelacion,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Mostrar quién procesó la solicitud si no está pendiente
            if (solicitud.estado != EstadoSolicitud.PENDIENTE && solicitud.adminId.isNotEmpty()) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                
                Column {
                    InfoItem(
                        icon = Icons.Default.PersonSearch,
                        text = "Procesada por: ${solicitud.nombreAdmin.ifEmpty { solicitud.adminId }}"
                    )
                    
                    // Fecha de procesamiento
                    solicitud.fechaProcesamiento?.let { timestamp ->
                        val fecha = timestamp.toDate()
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(fecha)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 32.dp)
                        )
                    }
                    
                    // Observaciones
                    if (solicitud.observaciones.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Observaciones: ${solicitud.observaciones}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que muestra un ítem de información con ícono
 */
@Composable
fun InfoItem(
    icon: ImageVector,
    text: String,
    showIcon: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIcon) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
} 