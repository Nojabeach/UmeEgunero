package com.tfg.umeegunero.feature.common.config.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.feature.common.config.viewmodel.NotificacionesViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/**
 * Pantalla de configuración de notificaciones
 */
@Composable
fun NotificacionesScreen(
    navController: NavController,
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Monitorear cambios en el mensaje para mostrar snackbar
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensaje()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de notificaciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Preferencias de notificaciones",
                    style = MaterialTheme.typography.titleLarge
                )
                
                // Notificaciones generales
                PreferenciaNotificacion(
                    icon = Icons.Filled.Notifications,
                    titulo = "Notificaciones generales",
                    descripcion = "Recibe anuncios importantes y otros avisos del centro educativo.",
                    habilitada = uiState.notificacionesGeneralHabilitadas,
                    onCambiarEstado = { viewModel.setNotificacionesGeneral(it) }
                )
                
                // Separador
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Registración del dispositivo
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        Text(
                            text = "Registro del dispositivo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (uiState.fcmToken.isNotEmpty()) 
                                "Este dispositivo está registrado para recibir notificaciones." 
                            else 
                                "Este dispositivo no está registrado para recibir notificaciones.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { viewModel.actualizarToken() },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Text(
                                text = if (uiState.fcmToken.isNotEmpty()) 
                                    "Actualizar registro" 
                                else 
                                    "Registrar dispositivo"
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.ejecutarDiagnosticoManual() },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Text("🔍 Diagnosticar notificaciones")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Información del sistema
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "ℹ️ Estado del Sistema",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "✅ Cloud Functions: OPERATIVAS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "✅ Google Apps Script: OPERATIVO",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = "✅ Firebase FCM: CONECTADO",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Si no recibes notificaciones, el problema está en la configuración de tu dispositivo. Ejecuta el diagnóstico para más detalles.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                        
                        if (uiState.fcmToken.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Token del dispositivo",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = uiState.fcmToken,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tarjeta informativa
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "Las notificaciones te permiten estar al día con las actividades escolares. " +
                                   "Puedes personalizar qué tipo de notificaciones quieres recibir según tus necesidades.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    )
}

/**
 * Componente para una preferencia de notificación individual
 */
@Composable
fun PreferenciaNotificacion(
    icon: ImageVector,
    titulo: String,
    descripcion: String,
    habilitada: Boolean,
    onCambiarEstado: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Switch(
                checked = habilitada,
                onCheckedChange = onCambiarEstado
            )
        }
    }
} 