package com.tfg.umeegunero.feature.common.notifications.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.PrioridadNotificacion
import com.tfg.umeegunero.data.model.TipoNotificacion
import com.tfg.umeegunero.feature.common.notifications.viewmodel.NotificacionesViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

/**
 * Pantalla de notificaciones del sistema
 * Permite visualizar y gestionar las notificaciones recibidas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    navController: NavController,
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var expandedNotificationId by remember { mutableStateOf<String?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Efecto para cargar notificaciones
    LaunchedEffect(Unit) {
        viewModel.cargarNotificaciones()
    }
    
    // Manejo de errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(mensaje)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // Marcar todas como leídas
                    if (uiState.notificaciones.any { !it.leida }) {
                        IconButton(
                            onClick = { viewModel.marcarTodasComoLeidas() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Marcar todas como leídas",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Filtro de notificaciones
                    IconButton(
                        onClick = { showFilterDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Estado de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            // Lista vacía
            if (!uiState.isLoading && uiState.notificaciones.isEmpty()) {
                EmptyNotificationsState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Lista de notificaciones
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Mostrar notificaciones agrupadas por fecha
                val agrupadasPorFecha = uiState.notificaciones.groupBy { notificacion ->
                    val calendar = Calendar.getInstance()
                    calendar.time = notificacion.fecha.toDate()
                    
                    val hoy = Calendar.getInstance()
                    val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    
                    when {
                        calendar.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == hoy.get(Calendar.DAY_OF_YEAR) -> "Hoy"
                        
                        calendar.get(Calendar.YEAR) == ayer.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == ayer.get(Calendar.DAY_OF_YEAR) -> "Ayer"
                        
                        calendar.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) -> {
                            SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")).format(calendar.time)
                        }
                        
                        else -> SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "ES")).format(calendar.time)
                    }
                }
                
                agrupadasPorFecha.forEach { (fecha, notificaciones) ->
                    // Encabezado de fecha
                    item {
                        Text(
                            text = fecha,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .fillMaxWidth()
                        )
                    }
                    
                    // Notificaciones de ese día
                    items(
                        items = notificaciones,
                        key = { it.id }
                    ) { notificacion ->
                        NotificacionItem(
                            notificacion = notificacion,
                            isExpanded = expandedNotificationId == notificacion.id,
                            onExpandClick = {
                                expandedNotificationId = if (expandedNotificationId == notificacion.id) null else notificacion.id
                                if (!notificacion.leida) {
                                    viewModel.marcarComoLeida(notificacion.id)
                                }
                            },
                            onDeleteClick = {
                                viewModel.eliminarNotificacion(notificacion.id)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Diálogo de filtrado
            if (showFilterDialog) {
                FilterDialog(
                    currentFilter = uiState.filtroTipo,
                    onDismiss = { showFilterDialog = false },
                    onFilterSelected = { tipo ->
                        viewModel.aplicarFiltro(tipo)
                        showFilterDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificacionItem(
    notificacion: Notificacion,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val elevation by animateFloatAsState(targetValue = if (isExpanded) 4f else 1f, label = "elevation")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !notificacion.leida -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                isExpanded -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono según tipo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = when(notificacion.prioridad) {
                                PrioridadNotificacion.BAJA -> MaterialTheme.colorScheme.tertiary
                                PrioridadNotificacion.NORMAL -> MaterialTheme.colorScheme.primary
                                PrioridadNotificacion.ALTA -> MaterialTheme.colorScheme.secondary
                                PrioridadNotificacion.URGENTE -> MaterialTheme.colorScheme.error
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(notificacion.tipo) {
                            TipoNotificacion.SISTEMA -> Icons.Default.Info
                            TipoNotificacion.EVENTO -> Icons.Default.Event
                            TipoNotificacion.MENSAJE -> Icons.Default.Email
                            TipoNotificacion.ACADEMICO -> Icons.Default.School
                            TipoNotificacion.ALERTA -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Título y fecha
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notificacion.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (!notificacion.leida) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Fecha formateada
                    val dateFormat = SimpleDateFormat("HH:mm", Locale("es", "ES"))
                    Text(
                        text = dateFormat.format(notificacion.fecha.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                // Indicador de no leída
                if (!notificacion.leida) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                
                // Icono de desplegar
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                    )
                }
            }
            
            // Contenido expandido
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mensaje completo
                    Text(
                        text = notificacion.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (notificacion.remitente.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "De: ${notificacion.remitente}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Acciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDeleteClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No hay notificaciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Las notificaciones aparecerán aquí",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun FilterDialog(
    currentFilter: TipoNotificacion?,
    onDismiss: () -> Unit,
    onFilterSelected: (TipoNotificacion?) -> Unit
) {
    val opciones = listOf(
        null to "Todas las notificaciones",
        TipoNotificacion.SISTEMA to "Sistema",
        TipoNotificacion.EVENTO to "Eventos",
        TipoNotificacion.MENSAJE to "Mensajes",
        TipoNotificacion.ACADEMICO to "Académicas",
        TipoNotificacion.ALERTA to "Alertas"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Filtrar notificaciones",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                opciones.forEach { (tipo, nombre) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(tipo) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFilter == tipo,
                            onClick = { onFilterSelected(tipo) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NotificacionesScreenPreview() {
    UmeEguneroTheme {
        NotificacionesScreen(
            navController = rememberNavController()
        )
    }
} 