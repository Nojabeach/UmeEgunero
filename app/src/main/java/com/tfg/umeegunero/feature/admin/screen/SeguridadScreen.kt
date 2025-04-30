/**
 * Módulo de configuración de seguridad del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz de configuración de seguridad
 * para los administradores del sistema, permitiendo establecer
 * políticas y parámetros de seguridad.
 */
package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.launch
import android.content.res.Configuration
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.SeguridadViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla de configuración de seguridad para administradores.
 * 
 * Esta pantalla proporciona una interfaz completa para la configuración
 * de todos los aspectos de seguridad del sistema, incluyendo políticas
 * de contraseñas, gestión de sesiones y control de acceso.
 * 
 * ## Secciones principales
 * - Políticas de contraseñas
 * - Gestión de sesiones
 * - Control de acceso
 * - Auditoría y registro
 * 
 * ## Características
 * - Configuración de complejidad de contraseñas
 * - Control de tiempo de sesión
 * - Gestión de intentos de acceso
 * - Verificación en dos pasos
 * - Registro de actividad
 * 
 * ## Funcionalidades
 * - Ajuste de parámetros de seguridad
 * - Activación/desactivación de características
 * - Feedback visual de configuraciones
 * - Validación en tiempo real
 * 
 * @param navController Controlador de navegación
 * 
 * @see SnackbarHostState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguridadScreen(
    viewModel: SeguridadViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Mostrar mensajes de error o éxito
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.success) {
        uiState.success?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Seguridad") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección de políticas de contraseñas
            Text(
                text = "Políticas de Contraseñas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Complejidad mínima requerida",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = uiState.complejidadPassword.toFloat(),
                            onValueChange = { viewModel.updateComplejidadPassword(it.toInt()) },
                            valueRange = 1f..4f,
                            steps = 2,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = when (uiState.complejidadPassword) {
                                1 -> "Baja"
                                2 -> "Media"
                                3 -> "Alta"
                                4 -> "Muy alta"
                                else -> "Media"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (uiState.complejidadPassword) {
                            1 -> "Mínimo 6 caracteres"
                            2 -> "Mínimo 8 caracteres, mayúsculas y números"
                            3 -> "Mínimo 10 caracteres, mayúsculas, números y símbolos"
                            4 -> "Mínimo 12 caracteres, mayúsculas, minúsculas, números y símbolos"
                            else -> "Mínimo 8 caracteres, mayúsculas y números"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Caducidad de contraseñas",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Forzar cambio cada 90 días",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = true,
                            onCheckedChange = { 
                                scope.launch {
                                    snackbarHostState.showSnackbar("Esta función no está disponible actualmente")
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Historial de contraseñas",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "No permitir reutilizar las últimas 5 contraseñas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = true,
                            onCheckedChange = { 
                                scope.launch {
                                    snackbarHostState.showSnackbar("Esta función no está disponible actualmente")
                                }
                            }
                        )
                    }
                }
            }
            
            // Sección de sesiones
            Text(
                text = "Configuración de Sesiones",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tiempo máximo de inactividad (minutos)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = uiState.tiempoSesion.toFloat(),
                            onValueChange = { viewModel.updateTiempoSesion(it.toInt()) },
                            valueRange = 5f..60f,
                            steps = 10,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "${uiState.tiempoSesion} min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cerrar sesión en caso de inactividad",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Cerrar automáticamente la sesión tras el tiempo indicado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = true,
                            onCheckedChange = { 
                                scope.launch {
                                    snackbarHostState.showSnackbar("Esta función no está disponible actualmente")
                                }
                            }
                        )
                    }
                }
            }
            
            // Sección de acceso
            Text(
                text = "Control de Acceso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Intentos máximos de acceso",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = uiState.maxIntentos.toFloat(),
                            onValueChange = { viewModel.updateMaxIntentos(it.toInt()) },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "${uiState.maxIntentos} intentos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verificación en dos pasos",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Requerir código adicional enviado por email o SMS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = uiState.verificacionDosFactores,
                            onCheckedChange = { viewModel.updateVerificacionDosFactores(it) }
                        )
                    }
                    
                    AnimatedVisibility(visible = uiState.verificacionDosFactores) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Método de verificación",
                                style = MaterialTheme.typography.titleSmall
                            )
                            
                            Row(
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                RadioButton(
                                    selected = true,
                                    onClick = { }
                                )
                                Text(
                                    text = "Email",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                RadioButton(
                                    selected = false,
                                    onClick = { }
                                )
                                Text(
                                    text = "SMS",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Sección de monitoreo
            Text(
                text = "Monitoreo y Alertas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificaciones de actividad sospechosa",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Enviar alertas ante accesos inusuales",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = uiState.notificacionesActividad,
                            onCheckedChange = { viewModel.updateNotificacionesActividad(it) }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Registro de actividad completo",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Mantener un historial detallado de todas las acciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = uiState.registroCompleto,
                            onCheckedChange = { viewModel.updateRegistroCompleto(it) }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bloqueo automático de IPs sospechosas",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Bloquear direcciones IP con múltiples intentos fallidos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Switch(
                            checked = uiState.bloqueoIP,
                            onCheckedChange = { viewModel.updateBloqueoIP(it) }
                        )
                    }
                }
            }
            
            // Botón para guardar cambios
            Button(
                onClick = { viewModel.guardarConfiguracion() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar configuración")
            }
            
            // Espacio adicional al final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Vista previa de la pantalla de seguridad en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun SeguridadScreenPreview() {
    UmeEguneroTheme {
        SeguridadScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {}
        )
    }
}

/**
 * Vista previa de la pantalla de seguridad en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SeguridadScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        SeguridadScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = {}
        )
    }
} 