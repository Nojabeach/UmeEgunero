package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.feature.centro.viewmodel.HistorialSolicitudesViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*

// Colores de estado (pueden ir al Theme o AppColors si se usan en más sitios)
private val colorAprobada = Color(0xFF4CAF50)
private val colorRechazada = Color(0xFFF44336)
private val colorPendiente = Color(0xFFFFC107)

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(horizontal = 16.dp) // Padding horizontal general
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator() // M3 Progress Indicator
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Filtros con Chips
                FiltrosEstadoChips(
                    estadoSeleccionado = filtroEstado,
                    onFiltroSeleccionado = { filtroEstado = it },
                    modifier = Modifier.padding(vertical = 16.dp) // Añadir padding vertical a los filtros
                )

                if (uiState.solicitudes.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                        Text("No hay solicitudes en el historial", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    val solicitudesFiltradas = remember(uiState.solicitudes, filtroEstado) {
                        if (filtroEstado != null) {
                            uiState.solicitudes.filter { it.estado == filtroEstado }
                        } else {
                            uiState.solicitudes
                        }
                    }
                    
                    if (solicitudesFiltradas.isEmpty()) {
                         Box(Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                            Text("No hay solicitudes con el filtro seleccionado", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp), // Más espacio entre items
                            contentPadding = PaddingValues(bottom = 16.dp) // Padding inferior para el último item
                        ) {
                            items(solicitudesFiltradas, key = { it.id }) { solicitud ->
                                SolicitudHistorialItem(solicitud = solicitud)
                            }
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FiltrosEstadoChips(
    estadoSeleccionado: EstadoSolicitud?,
    onFiltroSeleccionado: (EstadoSolicitud?) -> Unit,
    modifier: Modifier = Modifier
) {
    val estados = listOf(null, EstadoSolicitud.APROBADA, EstadoSolicitud.RECHAZADA, EstadoSolicitud.PENDIENTE)
    
    Column(modifier = modifier) {
         Text(
             text = "Filtrar por estado:",
             style = MaterialTheme.typography.titleSmall,
             modifier = Modifier.padding(bottom = 8.dp)
         )
        // Usar FlowRow para que los chips se ajusten si no caben en una línea
        FlowRow(
             modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.spacedBy(8.dp),
             // verticalArrangement = Arrangement.spacedBy(4.dp) // Si se necesitan varias líneas
         ) {
             estados.forEach { estado ->
                 val selected = estadoSeleccionado == estado
                 FilterChip(
                     selected = selected,
                     onClick = { onFiltroSeleccionado(estado) },
                     label = { Text(getLabelForEstado(estado)) },
                     leadingIcon = if (selected) {
                         { Icon(imageVector = Icons.Filled.Done, contentDescription = "Seleccionado") }
                     } else null,
                     colors = FilterChipDefaults.filterChipColors(
                         selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                         selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                         selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                     )
                 )
             }
         }
    }
}

// Helper para obtener texto del filtro
private fun getLabelForEstado(estado: EstadoSolicitud?): String {
    return when (estado) {
        null -> "Todos"
        EstadoSolicitud.APROBADA -> "Aprobadas"
        EstadoSolicitud.RECHAZADA -> "Rechazadas"
        EstadoSolicitud.PENDIENTE -> "Pendientes"
    }
}

// Formateador de fecha (reutilizable)
private val dateFormatSimple: SimpleDateFormat by lazy {
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
}
private val dateFormatCompleto: SimpleDateFormat by lazy {
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
}

/**
 * Componente que muestra una solicitud en el historial
 */
@Composable
fun SolicitudHistorialItem(solicitud: SolicitudVinculacion) {
    
    val fechaSolicitudFormatted = remember(solicitud.fechaSolicitud) {
        dateFormatSimple.format(solicitud.fechaSolicitud.toDate())
    }
    val fechaProcesamientoFormatted = remember(solicitud.fechaProcesamiento) {
        solicitud.fechaProcesamiento?.toDate()?.let { dateFormatCompleto.format(it) }
    }
    
    val statusColor = when (solicitud.estado) {
        EstadoSolicitud.APROBADA -> colorAprobada
        EstadoSolicitud.RECHAZADA -> colorRechazada
        EstadoSolicitud.PENDIENTE -> colorPendiente
    }
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), // Borde sutil
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface // Color base
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fila superior: Estado y Fecha Solicitud
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Estado con icono
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (solicitud.estado) {
                            EstadoSolicitud.APROBADA -> Icons.Filled.CheckCircle
                            EstadoSolicitud.RECHAZADA -> Icons.Filled.Cancel // O Error icon
                            EstadoSolicitud.PENDIENTE -> Icons.Filled.HourglassEmpty // O Schedule icon
                        },
                        contentDescription = "Estado: ${solicitud.estado.name}",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = solicitud.estado.name,
                        style = MaterialTheme.typography.labelLarge, // Label para estado
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                
                // Fecha de solicitud
                Text(
                    text = fechaSolicitudFormatted,
                    style = MaterialTheme.typography.bodySmall, // Más pequeño
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider() // Separador M3
            Spacer(Modifier.height(16.dp))

            // Detalles: Familiar y Alumno
            InfoSection(icon = Icons.Filled.Person, title = "Familiar") {
                Text("Nombre: ${solicitud.nombreFamiliar}", style = MaterialTheme.typography.bodyMedium)
                Text("Relación: ${solicitud.tipoRelacion}", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(Modifier.height(12.dp)) // Espacio entre secciones

            InfoSection(icon = Icons.Filled.Face, title = "Alumno") { // Usar Face para alumno
                Text("Nombre: ${solicitud.alumnoNombre}", style = MaterialTheme.typography.bodyMedium)
                Text("DNI: ${solicitud.alumnoDni}", style = MaterialTheme.typography.bodyMedium)
            }

            // Detalles: Procesamiento (si existe)
            if (solicitud.adminId.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                InfoSection(icon = Icons.Filled.AdminPanelSettings, title = "Procesamiento") { // Icono admin
                    Text(
                        "Admin: ${solicitud.nombreAdmin.ifEmpty { solicitud.adminId }}", 
                        style = MaterialTheme.typography.bodyMedium
                    )
                    fechaProcesamientoFormatted?.let {
                        Text(
                            "Fecha: $it", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (solicitud.observaciones.isNotEmpty()) {
                         Spacer(Modifier.height(4.dp))
                         Text(
                            "Observaciones: ${solicitud.observaciones}", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// --- Componente reutilizable para secciones de información ---
@Composable
fun InfoSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.Top) { // Alinear al top para que el icono quede bien
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, // Usar color primario para icono de sección
            modifier = Modifier.size(24.dp).padding(top = 2.dp) // Ajuste vertical icono
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall, // Título pequeño para sección
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            // Contenido específico de la sección
            content()
        }
    }
} 