package com.tfg.umeegunero.feature.common.comunicacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.BandejaEntradaViewModel
import com.tfg.umeegunero.feature.common.comunicacion.viewmodel.TipoBandeja
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.Send

/**
 * Pantalla principal de bandeja de entrada y mensajes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaEntradaScreen(
    navController: NavController,
    viewModel: BandejaEntradaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajería") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            navController.navigate(AppScreens.ComponerMensaje.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Nuevo mensaje"
                        )
                    }
                    
                    IconButton(onClick = { viewModel.cargarMensajes() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = uiState.bandejaActiva == TipoBandeja.RECIBIDOS,
                    onClick = { viewModel.cambiarBandeja(TipoBandeja.RECIBIDOS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Recibidos"
                        )
                    },
                    label = { Text("Recibidos") }
                )
                
                NavigationBarItem(
                    selected = uiState.bandejaActiva == TipoBandeja.ENVIADOS,
                    onClick = { viewModel.cambiarBandeja(TipoBandeja.ENVIADOS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviados"
                        )
                    },
                    label = { Text("Enviados") }
                )
                
                NavigationBarItem(
                    selected = uiState.bandejaActiva == TipoBandeja.DESTACADOS,
                    onClick = { viewModel.cambiarBandeja(TipoBandeja.DESTACADOS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Destacados"
                        )
                    },
                    label = { Text("Destacados") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    navController.navigate(AppScreens.ComponerMensaje.route)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Nuevo mensaje"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.cargando) {
                LoadingIndicator(
                    isLoading = true,
                    message = "Cargando mensajes..."
                )
            } else if (uiState.mensajesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay mensajes en esta bandeja",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Barra de búsqueda
                    OutlinedTextField(
                        value = uiState.busqueda,
                        onValueChange = { viewModel.actualizarBusqueda(it) },
                        placeholder = { Text("Buscar mensajes") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        trailingIcon = {
                            if (uiState.busqueda.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.actualizarBusqueda("") }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar"
                                    )
                                }
                            }
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de mensajes
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.mensajesFiltrados) { mensaje ->
                            MensajeItem(
                                mensaje = mensaje,
                                bandejaActiva = uiState.bandejaActiva,
                                onClick = { viewModel.seleccionarMensaje(mensaje) },
                                onToggleDestacado = { viewModel.toggleDestacado(mensaje) },
                                onEliminar = { viewModel.confirmarEliminarMensaje(mensaje) }
                            )
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Diálogo de detalle de mensaje
            if (uiState.mostrarDetalle && uiState.mensajeSeleccionado != null) {
                DetalleMensajeDialog(
                    mensaje = uiState.mensajeSeleccionado!!,
                    onDismiss = { viewModel.cerrarDetalle() },
                    onResponder = {
                        viewModel.cerrarDetalle()
                        navController.navigate(AppScreens.ComponerMensaje.createRoute(uiState.mensajeSeleccionado!!.id))
                    }
                )
            }
            
            // Diálogo de confirmación para eliminar mensaje
            if (uiState.mostrarAlertaEliminar) {
                AlertDialog(
                    onDismissRequest = { viewModel.cancelarEliminarMensaje() },
                    title = { Text("Eliminar mensaje") },
                    text = { 
                        Text("¿Estás seguro de que quieres eliminar este mensaje? Esta acción no se puede deshacer.")
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.eliminarMensaje() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.cancelarEliminarMensaje() }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Componente que muestra un elemento de mensaje en la lista
 */
@Composable
fun MensajeItem(
    mensaje: Mensaje,
    bandejaActiva: TipoBandeja,
    onClick: () -> Unit,
    onToggleDestacado: () -> Unit,
    onEliminar: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaFormateada = sdf.format(mensaje.fechaEnvio.toDate())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de leído/no leído (solo para recibidos)
        if (bandejaActiva == TipoBandeja.RECIBIDOS) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (!mensaje.leido) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Contenido principal
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (bandejaActiva) {
                        TipoBandeja.RECIBIDOS -> mensaje.remitenteNombre
                        TipoBandeja.ENVIADOS -> mensaje.destinatarioNombre
                        TipoBandeja.DESTACADOS -> mensaje.remitenteNombre
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (!mensaje.leido && bandejaActiva == TipoBandeja.RECIBIDOS) 
                        FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Fecha
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Asunto
            Text(
                text = mensaje.asunto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!mensaje.leido && bandejaActiva == TipoBandeja.RECIBIDOS) 
                    FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Vista previa del contenido
            Text(
                text = mensaje.contenido,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(0.7f)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Iconos de acciones
        Row {
            IconButton(onClick = onToggleDestacado) {
                Icon(
                    imageVector = if (mensaje.destacado) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (mensaje.destacado) "Quitar de destacados" else "Añadir a destacados",
                    tint = if (mensaje.destacado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Diálogo de detalle de mensaje
 */
@Composable
fun DetalleMensajeDialog(
    mensaje: Mensaje,
    onDismiss: () -> Unit,
    onResponder: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaFormateada = sdf.format(mensaje.fechaEnvio.toDate())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(mensaje.asunto, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Información del mensaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "De: ${mensaje.remitenteNombre}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = fechaFormateada,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Para: ${mensaje.destinatarioNombre}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Contenido del mensaje
                Text(
                    text = mensaje.contenido,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Adjuntos (si hay)
                if (mensaje.adjuntos != null && mensaje.adjuntos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Adjuntos:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    mensaje.adjuntos.forEach { adjunto ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* TODO: abrir adjunto */ }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = adjunto.substringAfterLast('/'),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cerrar")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onResponder
                ) {
                    Text("Responder")
                }
            }
        },
        dismissButton = null
    )
} 