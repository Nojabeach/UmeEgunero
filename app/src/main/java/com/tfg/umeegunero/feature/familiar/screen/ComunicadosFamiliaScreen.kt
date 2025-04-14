package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.familiar.viewmodel.ComunicadosFamiliaViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Componente que muestra un mensaje cuando no hay contenido para mostrar.
 *
 * @param mensaje Mensaje a mostrar al usuario
 * @param icon Icono a mostrar (debe ser un ImageVector)
 */
@Composable
fun EmptyContentMessage(
    mensaje: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

/**
 * Pantalla principal de comunicados para perfil familiar
 *
 * Esta pantalla muestra la lista de comunicados dirigidos a usuarios con perfil de familiar.
 * Permite ver los detalles de cada comunicado, marcarlos como leídos y filtrarlos por estado.
 *
 * @param viewModel ViewModel que gestiona la lógica de negocio y estado de la UI
 * @param onNavigateUp Callback para navegar hacia atrás
 * @param navController Controlador de navegación (opcional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadosFamiliaScreen(
    viewModel: ComunicadosFamiliaViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar snackbar con mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Comunicados")
                        if (uiState.comunicadosNoLeidos > 0 || uiState.comunicadosSinConfirmar > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            BadgeNotification(uiState.comunicadosNoLeidos, uiState.comunicadosSinConfirmar)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón para activar/desactivar filtros
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mostrar indicador de filtro activo
                        if (uiState.filtroActivo) {
                            IconButton(onClick = { viewModel.resetFiltros() }) {
                                Icon(
                                    imageVector = Icons.Default.FilterAltOff,
                                    contentDescription = "Quitar filtros",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            // Mostrar filtros disponibles
                            AnimatedVisibility(
                                visible = uiState.comunicadosNoLeidos > 0,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                TextButton(
                                    onClick = { viewModel.toggleFiltroNoLeidos() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("No leídos (${uiState.comunicadosNoLeidos})")
                                }
                            }
                            
                            AnimatedVisibility(
                                visible = uiState.comunicadosSinConfirmar > 0,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                TextButton(
                                    onClick = { viewModel.toggleFiltroSinConfirmar() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("Sin confirmar (${uiState.comunicadosSinConfirmar})")
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar contenido principal o indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            } else {
                // Filtrar comunicados según los filtros activos
                val comunicadosFiltrados = when {
                    uiState.mostrarSoloNoLeidos -> uiState.comunicados.filter { 
                        !viewModel.esComunicadoLeido(it.id) 
                    }
                    uiState.mostrarSoloSinConfirmar -> uiState.comunicados.filter {
                        it.requiereConfirmacion && !viewModel.esComunicadoConfirmado(it.id)
                    }
                    else -> uiState.comunicados
                }
                
                if (comunicadosFiltrados.isEmpty()) {
                    EmptyContentMessage(
                        mensaje = when {
                            uiState.comunicados.isEmpty() -> "No hay comunicados disponibles"
                            uiState.mostrarSoloNoLeidos -> "No hay comunicados sin leer"
                            uiState.mostrarSoloSinConfirmar -> "No hay comunicados pendientes de confirmar"
                            else -> "No hay comunicados que coincidan con los filtros"
                        },
                        icon = Icons.Default.Notifications
                    )
                } else {
                    // Lista de comunicados
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    ) {
                        items(uiState.comunicadosFiltrados) { comunicado ->
                            ComunicadoItem(
                                comunicado = comunicado,
                                leido = viewModel.esComunicadoLeido(comunicado.id),
                                confirmado = viewModel.esComunicadoConfirmado(comunicado.id),
                                onClick = {
                                    // Si el comunicado requiere confirmación y no está confirmado,
                                    // mostramos el diálogo de confirmación
                                    if (comunicado.requiereConfirmacion && 
                                        !viewModel.esComunicadoConfirmado(comunicado.id)) {
                                        viewModel.mostrarConfirmacion(comunicado)
                                    } else {
                                        // Si no requiere confirmación o ya está confirmado,
                                        // solo lo marcamos como leído
                                        if (!viewModel.esComunicadoLeido(comunicado.id)) {
                                            viewModel.marcarComoLeido(comunicado.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Diálogo de confirmación de lectura
            if (uiState.mostrarDialogoConfirmacion && uiState.comunicadoSeleccionado != null) {
                ConfirmacionLecturaDialog(
                    comunicado = uiState.comunicadoSeleccionado!!,
                    onConfirmar = {
                        viewModel.confirmarLectura(uiState.comunicadoSeleccionado!!.id)
                    },
                    onDismiss = {
                        viewModel.cerrarDialogoConfirmacion()
                    }
                )
            }
        }
    }
}

/**
 * Componente que muestra un indicador visual del número de notificaciones sin leer
 *
 * @param noLeidos Número de comunicados no leídos
 * @param sinConfirmar Número de comunicados sin confirmar
 */
@Composable
fun BadgeNotification(noLeidos: Int, sinConfirmar: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (noLeidos > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .border(1.dp, MaterialTheme.colorScheme.onError, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (noLeidos > 99) "99+" else noLeidos.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
        
        if (sinConfirmar > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .border(1.dp, MaterialTheme.colorScheme.onTertiary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = "$sinConfirmar comunicados pendientes de confirmar",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Componente que muestra un comunicado individual en la lista
 *
 * @param comunicado Comunicado a mostrar
 * @param leido Indica si el comunicado ha sido leído
 * @param confirmado Indica si el comunicado ha sido confirmado
 * @param onClick Callback para cuando se hace click en el comunicado
 */
@Composable
fun ComunicadoItem(
    comunicado: Comunicado,
    leido: Boolean,
    confirmado: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        comunicado.requiereConfirmacion && !confirmado -> 
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        !leido -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!leido) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fila superior con título y estado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Título del comunicado
                Text(
                    text = comunicado.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!leido) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Indicador de estado: leído/sin leer, confirmado/sin confirmar
                when {
                    comunicado.requiereConfirmacion && confirmado -> {
                        // Confirmado
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Lectura confirmada",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    comunicado.requiereConfirmacion && !confirmado -> {
                        // Requiere confirmación pero aún no se ha confirmado
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = "Requiere confirmación",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    leido -> {
                        // Leído
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Leído",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        // No leído
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contenido del comunicado
            Text(
                text = comunicado.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Fila inferior con fecha y destinatarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fecha de creación
                Text(
                    text = formatearFecha(comunicado.fechaCreacion.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Indicador de confirmación requerida si aplica
                if (comunicado.requiereConfirmacion) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (confirmado) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = if (confirmado) 
                                "Confirmado" 
                            else if (comunicado.fechaLimiteConfirmacion != null && 
                                    Date().after(comunicado.fechaLimiteConfirmacion?.toDate())) 
                                "¡Confirma antes del ${formatearFecha(comunicado.fechaLimiteConfirmacion?.toDate())}!"
                            else 
                                "Requiere confirmación",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (confirmado) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para confirmar la lectura de un comunicado importante
 *
 * @param comunicado Comunicado que requiere confirmación
 * @param onConfirmar Callback para cuando el usuario confirma la lectura
 * @param onDismiss Callback para cuando el usuario cierra el diálogo sin confirmar
 */
@Composable
fun ConfirmacionLecturaDialog(
    comunicado: Comunicado,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirmar lectura",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Este es un comunicado importante que requiere tu confirmación de lectura:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = comunicado.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = comunicado.mensaje,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (comunicado.fechaLimiteConfirmacion != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "Fecha límite: ${formatearFecha(comunicado.fechaLimiteConfirmacion?.toDate())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (comunicado.fechaLimiteConfirmacion?.toDate()?.before(Date()) == true)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Al confirmar, estás indicando que has leído y entendido el contenido de este comunicado.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar lectura")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Leer más tarde")
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    )
}

/**
 * Formatea una fecha para mostrarla en la UI
 *
 * @param date Fecha a formatear
 * @return String con la fecha formateada
 */
private fun formatearFecha(date: Date?): String {
    if (date == null) return "Fecha desconocida"
    
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    return formatoFecha.format(date)
}

@Preview(showBackground = true)
@Composable
fun ComunicadoItemPreview() {
    UmeEguneroTheme {
        Surface {
            ComunicadoItem(
                comunicado = Comunicado(
                    id = "1",
                    titulo = "Reunión de padres",
                    mensaje = "Se convoca reunión de padres para el día 15 de mayo a las 18:00h en el salón de actos del centro.",
                    fechaCreacion = Timestamp.now(),
                    remitente = "Director",
                    tiposDestinatarios = listOf(TipoUsuario.FAMILIAR.name),
                    requiereConfirmacion = true,
                    fechaLimiteConfirmacion = Timestamp.now()
                ),
                leido = false,
                confirmado = false,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmacionLecturaDialogPreview() {
    UmeEguneroTheme {
        Surface {
            ConfirmacionLecturaDialog(
                comunicado = Comunicado(
                    id = "1",
                    titulo = "Reunión de padres",
                    mensaje = "Se convoca reunión de padres para el día 15 de mayo a las 18:00h en el salón de actos del centro.",
                    fechaCreacion = Timestamp.now(),
                    remitente = "Director",
                    tiposDestinatarios = listOf(TipoUsuario.FAMILIAR.name),
                    requiereConfirmacion = true,
                    fechaLimiteConfirmacion = Timestamp.now()
                ),
                onConfirmar = {},
                onDismiss = {}
            )
        }
    }
} 