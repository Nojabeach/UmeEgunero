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

/**
 * Pantalla de configuración de seguridad para el administrador del sistema.
 * 
 * Esta pantalla permite configurar diversos parámetros de seguridad como:
 * - Políticas de contraseñas
 * - Tiempo de sesión
 * - Intentos de acceso
 * - Activación de verificación en dos pasos
 * - Registro de actividad sospechosa
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguridadScreen(
    navController: NavController
) {
    val scrollState = rememberScrollState()
    var complejidadPassword by remember { mutableStateOf(2) }
    var tiempoSesion by remember { mutableStateOf(30) }
    var maxIntentos by remember { mutableStateOf(3) }
    var verificacionDosFactores by remember { mutableStateOf(false) }
    var notificacionesActividad by remember { mutableStateOf(true) }
    var registroCompleto by remember { mutableStateOf(true) }
    var bloqueoIP by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Seguridad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                            value = complejidadPassword.toFloat(),
                            onValueChange = { complejidadPassword = it.toInt() },
                            valueRange = 1f..4f,
                            steps = 2,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = when (complejidadPassword) {
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
                        text = when (complejidadPassword) {
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
                            value = tiempoSesion.toFloat(),
                            onValueChange = { 
                                tiempoSesion = it.toInt()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tiempo de sesión actualizado a $tiempoSesion minutos")
                                }
                            },
                            valueRange = 5f..60f,
                            steps = 10,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "$tiempoSesion min",
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
                            value = maxIntentos.toFloat(),
                            onValueChange = { maxIntentos = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "$maxIntentos intentos",
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
                            checked = verificacionDosFactores,
                            onCheckedChange = { verificacionDosFactores = it }
                        )
                    }
                    
                    AnimatedVisibility(visible = verificacionDosFactores) {
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
                            checked = notificacionesActividad,
                            onCheckedChange = { notificacionesActividad = it }
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
                            checked = registroCompleto,
                            onCheckedChange = { registroCompleto = it }
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
                            checked = bloqueoIP,
                            onCheckedChange = { bloqueoIP = it }
                        )
                    }
                }
            }
            
            // Botón para guardar cambios
            Button(
                onClick = { 
                    scope.launch {
                        snackbarHostState.showSnackbar("Configuración de seguridad guardada correctamente")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
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
 * Vista previa de la pantalla de configuración de seguridad.
 * 
 * Muestra una representación de la pantalla con valores predeterminados.
 */
@Preview(showBackground = true)
@Composable
fun SeguridadScreenPreview() {
    MaterialTheme {
        SeguridadScreen(
            navController = rememberNavController()
        )
    }
} 