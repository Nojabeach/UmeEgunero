package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.feature.familiar.viewmodel.MensajesFamiliaViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.theme.Red100
import com.tfg.umeegunero.ui.theme.Typography
import com.tfg.umeegunero.util.formatDateShort
import com.tfg.umeegunero.util.formatDateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

/**
 * Pantalla principal de mensajes para familia
 * 
 * Permite a los usuarios con perfil familiar ver sus mensajes recibidos,
 * marcarlos como leídos, destacarlos y eliminarlos.
 *
 * @param viewModel ViewModel que gestiona la lógica de negocio y estado de la UI
 * @param onNavigateUp Callback para navegar hacia atrás
 * @param navController Controlador de navegación (opcional)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MensajesFamiliaScreen(
    viewModel: MensajesFamiliaViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.cargarMensajes()
    }

    LaunchedEffect(key1 = uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Mensajes",
                        style = Typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                },
                actions = {
                    if (uiState.mensajes.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                if (uiState.filtrosActivos) {
                                    Badge { 
                                        Text("!") 
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                // Mostrar diálogo de filtros
                            }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtrar mensajes"
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController?.navigate(AppScreens.ComponerMensaje.createRoute())
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo mensaje"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.cargarMensajes() }) {
                        Text("Reintentar")
                    }
                }
            } else if (uiState.mensajesFiltrados.isEmpty() && uiState.filtrosActivos) {
                NoFilterMatchView(
                    onResetFilters = {
                        viewModel.actualizarFiltroNoLeidos(false)
                        viewModel.actualizarFiltroDestacados(false)
                    }
                )
            } else if (uiState.mensajes.isEmpty()) {
                EmptyMessagesView()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.mostrarSoloNoLeidos,
                                onClick = { 
                                    viewModel.actualizarFiltroNoLeidos(!uiState.mostrarSoloNoLeidos) 
                                },
                                label = { Text("No leídos") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.mostrarSoloNoLeidos) 
                                            Icons.Default.MarkEmailUnread 
                                        else 
                                            Icons.Default.MarkEmailRead,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            
                            FilterChip(
                                selected = uiState.mostrarSoloDestacados,
                                onClick = { 
                                    viewModel.actualizarFiltroDestacados(!uiState.mostrarSoloDestacados) 
                                },
                                label = { Text("Destacados") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.mostrarSoloDestacados) 
                                            Icons.Default.Star 
                                        else 
                                            Icons.Default.StarBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    items(uiState.mensajesFiltrados) { mensaje ->
                        MensajeItem(
                            mensaje = mensaje,
                            onMensajeClick = { viewModel.seleccionarMensaje(it) },
                            onToggleDestacado = { viewModel.toggleDestacado(it) },
                            onDeleteClick = { viewModel.confirmarEliminarMensaje(it) }
                        )
                    }
                }
            }
            
            if (uiState.mostrarDetalle && uiState.mensajeSeleccionado != null) {
                DetalleMensajeDialog(
                    mensaje = uiState.mensajeSeleccionado!!,
                    onDismiss = { viewModel.cerrarDetalle() },
                    onToggleDestacado = { viewModel.toggleDestacado(it) }
                )
            }
            
            if (uiState.mensajeParaEliminar != null) {
                ConfirmEliminarMensajeDialog(
                    onConfirm = { viewModel.eliminarMensaje() },
                    onDismiss = { viewModel.cancelarEliminarMensaje() }
                )
            }
        }
    }
}

/**
 * Componente que muestra un elemento individual de mensaje en la lista
 */
@Composable
fun MensajeItem(
    mensaje: Mensaje,
    onMensajeClick: (Mensaje) -> Unit,
    onToggleDestacado: (Mensaje) -> Unit,
    onDeleteClick: (Mensaje) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMensajeClick(mensaje) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!mensaje.leido) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mensaje.remitenteNombre.takeIf { it.isNotBlank() } ?: mensaje.remitente,
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = mensaje.fechaEnvio.toDate().formatDateShort(),
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = mensaje.asunto,
                style = Typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = mensaje.contenido,
                style = Typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onToggleDestacado(mensaje) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (mensaje.destacado) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (mensaje.destacado) "Quitar de destacados" else "Destacar mensaje",
                        tint = if (mensaje.destacado) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { onDeleteClick(mensaje) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar mensaje",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para mostrar el detalle de un mensaje
 */
@Composable
fun DetalleMensajeDialog(
    mensaje: Mensaje,
    onDismiss: () -> Unit,
    onToggleDestacado: (Mensaje) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mensaje.asunto,
                        style = Typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(onClick = { onToggleDestacado(mensaje) }) {
                        Icon(
                            imageVector = if (mensaje.destacado) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (mensaje.destacado) "Quitar de destacados" else "Destacar mensaje",
                            tint = if (mensaje.destacado) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "De: ${mensaje.remitenteNombre.takeIf { it.isNotBlank() } ?: mensaje.remitente}",
                        style = Typography.bodyMedium
                    )
                }
                
                Text(
                    text = "Enviado: ${mensaje.fechaEnvio.toDate().formatDateTime()}",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(modifier = Modifier.padding(top = 8.dp))
            }
        },
        text = {
            Text(
                text = mensaje.contenido,
                style = Typography.bodyLarge
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = null
    )
}

/**
 * Diálogo de confirmación para eliminar un mensaje
 */
@Composable
fun ConfirmEliminarMensajeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar mensaje") },
        text = { Text("¿Estás seguro de que deseas eliminar este mensaje? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Vista cuando no hay mensajes
 */
@Composable
fun EmptyMessagesView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No tienes mensajes",
            style = Typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Cuando recibas mensajes del centro o profesores, aparecerán aquí.",
            style = Typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Vista cuando no hay mensajes que coincidan con los filtros aplicados
 */
@Composable
fun NoFilterMatchView(onResetFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No hay resultados para los filtros aplicados",
            style = Typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onResetFilters) {
            Text("Quitar filtros")
        }
    }
}

/**
 * Formatea una fecha para mostrarla en formato legible
 */
private fun formatearFecha(fecha: Date): String {
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatoFecha.format(fecha)
} 