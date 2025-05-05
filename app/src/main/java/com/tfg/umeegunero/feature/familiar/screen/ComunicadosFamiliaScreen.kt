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
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.familiar.viewmodel.ComunicadosFamiliaViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Campaign

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
 * Pantalla para que los familiares vean los comunicados del centro.
 *
 * Muestra una lista de comunicados generales o específicos para los
 * hijos del familiar.
 *
 * @param navController Controlador de navegación.
 * @param viewModel ViewModel que gestiona la lógica y el estado de los comunicados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadosFamiliaScreen(
    navController: NavController,
    viewModel: ComunicadosFamiliaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Comunicados",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                containerColor = FamiliarColor,
                contentColor = Color.White
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.comunicados.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                         Icon(
                            Icons.Filled.Campaign, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                         )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay comunicados disponibles.")
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.comunicados) { comunicado ->
                            // Placeholder para el item de comunicado
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(comunicado.titulo, style = MaterialTheme.typography.titleMedium)
                                    Text(comunicado.mensaje ?: "Sin contenido", style = MaterialTheme.typography.bodyMedium)
                                    // Añadir fecha, etc.
                                }
                            }
                        }
                    }
                }
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
 * Muestra los recursos adjuntos de un comunicado de forma atractiva
 */
@Composable
fun RecursosAdjuntos(
    recursos: List<String>,
    modifier: Modifier = Modifier
) {
    if (recursos.isEmpty()) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Título de la sección
        Text(
            text = "Adjuntos (${recursos.size})",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Lista de recursos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recursos.take(2).forEach { recurso ->
                RecursoChip(recurso)
            }
            
            // Si hay más de 2 recursos, mostrar contador
            if (recursos.size > 2) {
                RecursoChip("+${recursos.size - 2} más")
            }
        }
    }
}

/**
 * Chip para mostrar un recurso adjunto
 */
@Composable
private fun RecursoChip(texto: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = texto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Elemento que muestra un comunicado en la lista
 */
@Composable
fun ComunicadoItem(
    comunicado: Comunicado,
    leido: Boolean,
    confirmado: Boolean,
    onComunicadoClick: (Comunicado) -> Unit,
    onConfirmarClick: (Comunicado) -> Unit
) {
    val colorFondo = when {
        !leido -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        comunicado.requiereConfirmacion && !confirmado -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borde = when {
        !leido -> MaterialTheme.colorScheme.primary
        comunicado.requiereConfirmacion && !confirmado -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    
    val iconoLectura = when {
        !leido -> Icons.Default.MarkEmailUnread
        confirmado -> Icons.Default.MarkEmailRead
        else -> Icons.Default.Email
    }
    
    val colorIconoLectura = when {
        !leido -> MaterialTheme.colorScheme.primary
        confirmado -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borde
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!leido) 4.dp else 1.dp
        ),
        onClick = { onComunicadoClick(comunicado) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de estado de lectura
            Icon(
                imageVector = iconoLectura,
                contentDescription = if (leido) "Leído" else "No leído",
                tint = colorIconoLectura,
                modifier = Modifier
                    .padding(top = 2.dp, end = 12.dp)
                    .size(24.dp)
            )
            
            // Contenido principal
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = comunicado.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!leido) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = comunicado.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Recursos adjuntos
                if (comunicado.recursos.isNotEmpty()) {
                    RecursosAdjuntos(recursos = comunicado.recursos)
                }
                
                // Metadatos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fecha
                    Text(
                        text = formatearFecha(comunicado.fechaCreacion.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Remitente
                    Text(
                        text = comunicado.remitente,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Botón de confirmar si es necesario
            if (comunicado.requiereConfirmacion && !confirmado && leido) {
                IconButton(
                    onClick = { onConfirmarClick(comunicado) },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Confirmar lectura",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
 * Diálogo para mostrar el detalle completo de un comunicado
 *
 * @param comunicado Comunicado a mostrar en detalle
 * @param onDismiss Callback para cuando el usuario cierra el diálogo
 */
@Composable
fun ComunicadoDetalleDialog(
    comunicado: Comunicado,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Cabecera con título e icono
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = comunicado.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Metadatos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Fecha
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = formatearFecha(comunicado.fechaCreacion.toDate()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Remitente
                    Text(
                        text = "De: ${comunicado.remitente}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Confirmación requerida (si aplica)
                if (comunicado.requiereConfirmacion) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = "Este comunicado requiere confirmación de lectura",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            if (comunicado.fechaLimiteConfirmacion != null) {
                                Text(
                                    text = "Fecha límite: ${formatearFecha(comunicado.fechaLimiteConfirmacion?.toDate())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contenido principal
                Text(
                    text = comunicado.mensaje,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Recursos adjuntos
                if (comunicado.recursos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Recursos adjuntos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        comunicado.recursos.forEach { recurso ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriorityHigh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = recurso,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (recurso != comunicado.recursos.last()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón para cerrar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
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
private fun ComunicadosFamiliaScreenPreview() {
    UmeEguneroTheme {
        ComunicadosFamiliaScreen(navController = rememberNavController())
    }
} 