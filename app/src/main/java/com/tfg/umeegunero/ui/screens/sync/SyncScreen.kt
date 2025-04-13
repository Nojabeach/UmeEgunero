package com.tfg.umeegunero.ui.screens.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.OperacionPendiente
import com.tfg.umeegunero.ui.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val estadoSincronizacion by viewModel.estadoSincronizacion.collectAsState()
    val operacionesPendientes by viewModel.operacionesPendientes.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sync_screen_title)) },
                actions = {
                    when (estadoSincronizacion) {
                        SyncViewModel.EstadoSincronizacion.NoSincronizando,
                        SyncViewModel.EstadoSincronizacion.SincronizacionCompletada -> {
                            IconButton(onClick = { viewModel.iniciarSincronizacion() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Iniciar sincronización")
                            }
                        }
                        SyncViewModel.EstadoSincronizacion.Sincronizando -> {
                            IconButton(onClick = { viewModel.detenerSincronizacion() }) {
                                Icon(Icons.Default.Stop, contentDescription = "Detener sincronización")
                            }
                        }
                        is SyncViewModel.EstadoSincronizacion.ErrorSincronizacion -> {
                            IconButton(onClick = { viewModel.iniciarSincronizacion() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reintentar sincronización")
                            }
                        }
                        is SyncViewModel.EstadoSincronizacion.SincronizacionPendiente -> {
                            IconButton(onClick = { viewModel.iniciarSincronizacion() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Iniciar sincronización")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            EstadoSincronizacionCard(estadoSincronizacion)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.pending_operations_title),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (operacionesPendientes.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_pending_operations),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn {
                    items(operacionesPendientes) { operacion ->
                        OperacionPendienteItem(operacion)
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoSincronizacionCard(estado: SyncViewModel.EstadoSincronizacion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (estado) {
                SyncViewModel.EstadoSincronizacion.NoSincronizando -> MaterialTheme.colorScheme.surface
                SyncViewModel.EstadoSincronizacion.Sincronizando -> MaterialTheme.colorScheme.primaryContainer
                SyncViewModel.EstadoSincronizacion.SincronizacionCompletada -> MaterialTheme.colorScheme.secondaryContainer
                is SyncViewModel.EstadoSincronizacion.ErrorSincronizacion -> MaterialTheme.colorScheme.errorContainer
                is SyncViewModel.EstadoSincronizacion.SincronizacionPendiente -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (estado) {
                    SyncViewModel.EstadoSincronizacion.NoSincronizando -> stringResource(R.string.sync_status_initial)
                    SyncViewModel.EstadoSincronizacion.Sincronizando -> stringResource(R.string.sync_status_syncing)
                    SyncViewModel.EstadoSincronizacion.SincronizacionCompletada -> stringResource(R.string.sync_status_completed)
                    is SyncViewModel.EstadoSincronizacion.ErrorSincronizacion -> stringResource(R.string.sync_status_error)
                    is SyncViewModel.EstadoSincronizacion.SincronizacionPendiente -> stringResource(R.string.sync_status_pending)
                },
                style = MaterialTheme.typography.titleMedium
            )
            
            if (estado is SyncViewModel.EstadoSincronizacion.ErrorSincronizacion) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = estado.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            if (estado is SyncViewModel.EstadoSincronizacion.SincronizacionPendiente) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sync_pending_operations_count, estado.cantidadOperaciones),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun OperacionPendienteItem(operacion: OperacionPendiente) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = when (operacion.tipo) {
                    OperacionPendiente.Tipo.FIRMA_DIGITAL -> stringResource(R.string.operation_type_signature)
                    OperacionPendiente.Tipo.COMUNICADO -> stringResource(R.string.operation_type_communication)
                    OperacionPendiente.Tipo.ARCHIVO -> stringResource(R.string.operation_type_file)
                    OperacionPendiente.Tipo.CREAR -> stringResource(R.string.operation_type_create)
                    OperacionPendiente.Tipo.ACTUALIZAR -> stringResource(R.string.operation_type_update)
                    OperacionPendiente.Tipo.ELIMINAR -> stringResource(R.string.operation_type_delete)
                    OperacionPendiente.Tipo.CONFIRMACION_LECTURA -> stringResource(R.string.operation_type_read_confirmation)
                },
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = stringResource(
                    R.string.operation_status,
                    when (operacion.estado) {
                        OperacionPendiente.Estado.PENDIENTE -> stringResource(R.string.status_pending)
                        OperacionPendiente.Estado.EN_PROCESO -> stringResource(R.string.status_processing)
                        OperacionPendiente.Estado.COMPLETADA -> stringResource(R.string.status_completed)
                        OperacionPendiente.Estado.ERROR -> stringResource(R.string.status_error)
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = stringResource(
                    R.string.operation_timestamp,
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        .format(Date(operacion.timestamp))
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 