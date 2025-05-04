package com.tfg.umeegunero.feature.familiar.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.R
import com.tfg.umeegunero.feature.familiar.onboarding.viewmodel.PermisoNotificacionesViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla para solicitar permisos de notificaciones al usuario familiar
 * durante el proceso de registro o primer inicio de sesión.
 * 
 * Esta pantalla explica al usuario la importancia de las notificaciones
 * y solicita el permiso correspondiente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermisoNotificacionesScreen(
    viewModel: PermisoNotificacionesViewModel = hiltViewModel(),
    onContinuar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Definir el lanzador para solicitar permiso de notificaciones
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermisoRespuesta(isGranted)
        }
    )
    
    // Monitorear cambios y mostrar mensajes
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarMensaje()
            }
        }
    }
    
    // Verificar si debemos continuar a la siguiente pantalla
    LaunchedEffect(uiState.continuarSiguientePantalla) {
        if (uiState.continuarSiguientePantalla) {
            onContinuar()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permisos de Notificaciones") },
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Icono de notificaciones
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                // Título
                Text(
                    text = "Mantente informado",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Descripción principal
                Text(
                    text = "Para que puedas recibir actualizaciones sobre las solicitudes de vinculación y notificaciones importantes del centro educativo, necesitamos tu permiso.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
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
                            text = "Las notificaciones te permitirán:\n\n" +
                                   "• Saber cuando tu solicitud ha sido aprobada\n" +
                                   "• Recibir comunicados importantes del centro\n" +
                                   "• Estar al día con las actividades escolares",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Imagen ilustrativa
                Image(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp)
                )
                
                // Texto adicional
                Text(
                    text = "Puedes cambiar esta configuración más adelante en la sección de Preferencias.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón para rechazar
                    OutlinedButton(
                        onClick = { viewModel.rechazarNotificaciones() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("No, gracias")
                    }
                    
                    // Botón para aceptar
                    Button(
                        onClick = { 
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.aceptarNotificaciones()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Activar")
                    }
                }
                
                // Estado actual
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
} 