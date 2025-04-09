package com.tfg.umeegunero.feature.common.reuniones.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Reunion
import com.tfg.umeegunero.data.model.EstadoReunion
import com.tfg.umeegunero.data.model.TipoReunion
import com.tfg.umeegunero.feature.common.reuniones.viewmodel.ReunionesViewModel
import com.tfg.umeegunero.feature.common.reuniones.viewmodel.ReunionesUiState
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate

/**
 * Pantalla principal de reuniones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReunionesScreen(
    viewModel: ReunionesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reuniones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleNuevaReunion() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva reunión")
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reuniones) { reunion ->
                        ReunionItem(
                            reunion = reunion,
                            onConfirmarAsistencia = { viewModel.confirmarAsistencia(reunion.id, "usuarioId") },
                            onEliminar = { viewModel.showDeleteDialog(reunion) }
                        )
                    }
                }
            }

            if (uiState.showNuevaReunion) {
                NuevaReunionDialog(
                    uiState = uiState,
                    onDismiss = { viewModel.toggleNuevaReunion() },
                    onTituloChange = { viewModel.updateTitulo(it) },
                    onDescripcionChange = { viewModel.updateDescripcion(it) },
                    onFechaInicioChange = { viewModel.updateFechaInicio(it) },
                    onFechaFinChange = { viewModel.updateFechaFin(it) },
                    onTipoChange = { viewModel.updateTipo(it) },
                    onUbicacionChange = { viewModel.updateUbicacion(it) },
                    onEnlaceVirtualChange = { viewModel.updateEnlaceVirtual(it) },
                    onNotasChange = { viewModel.updateNotas(it) },
                    onGuardar = { viewModel.crearReunion() }
                )
            }

            if (uiState.showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideDeleteDialog() },
                    title = { Text("Eliminar reunión") },
                    text = { Text("¿Estás seguro de que quieres eliminar esta reunión?") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.deleteReunion() }
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Cerrar")
                    }
                }
            }

            uiState.success?.let { success ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(success)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.clearSuccess() }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

/**
 * Item de reunión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReunionItem(
    reunion: Reunion,
    onConfirmarAsistencia: () -> Unit,
    onEliminar: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* TODO: Mostrar detalles */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reunion.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = reunion.estado.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reunion.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Inicio: ${dateFormat.format(reunion.fechaInicio.toDate())}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Fin: ${dateFormat.format(reunion.fechaFin.toDate())}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row {
                    IconButton(onClick = onConfirmarAsistencia) {
                        Icon(Icons.Default.Check, contentDescription = "Confirmar asistencia")
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para crear una nueva reunión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaReunionDialog(
    uiState: ReunionesUiState,
    onDismiss: () -> Unit,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaInicioChange: (LocalDate) -> Unit,
    onFechaFinChange: (LocalDate) -> Unit,
    onTipoChange: (TipoReunion) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onEnlaceVirtualChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva reunión") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.titulo,
                    onValueChange = onTituloChange,
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // TODO: Implementar DatePicker para fechaInicio y fechaFin

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = uiState.tipo.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = uiState.ubicacion,
                    onValueChange = onUbicacionChange,
                    label = { Text("Ubicación") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.enlaceVirtual,
                    onValueChange = onEnlaceVirtualChange,
                    label = { Text("Enlace virtual") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.notas,
                    onValueChange = onNotasChange,
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onGuardar) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        content()
    }
} 