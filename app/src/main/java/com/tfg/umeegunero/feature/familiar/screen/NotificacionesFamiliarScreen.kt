package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.familiar.viewmodel.NotificacionesFamiliarViewModel
import kotlinx.coroutines.launch

// Clase de modelo simplificada para notificaciones
data class Notificacion(
    val id: String,
    val titulo: String,
    val contenido: String,
    val fecha: String
)

/**
 * Pantalla principal de notificaciones para familiares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesFamiliarScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificacionesFamiliarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Si hay un error, mostrar snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarNotificaciones() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recargar notificaciones"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.notificaciones.isEmpty()) {
                Text(
                    text = "No hay notificaciones",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = uiState.notificaciones,
                        key = { it.id }
                    ) { notificacion ->
                        var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
                        
                        NotificacionItem(
                            notificacion = notificacion,
                            onClick = {
                                viewModel.marcarComoLeida(notificacion.id)
                            },
                            onLongClick = {
                                showDeleteConfirmation = true
                            }
                        )
                        
                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = false },
                                title = { Text("Eliminar notificación") },
                                text = { Text("¿Estás seguro de que quieres eliminar esta notificación?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.eliminarNotificacion(notificacion.id)
                                            showDeleteConfirmation = false
                                        }
                                    ) {
                                        Text("Eliminar")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirmation = false }) {
                                        Text("Cancelar")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notificacion.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(
                    onClick = onLongClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar notificación",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notificacion.contenido,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notificacion.fecha,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
} 