/**
 * Módulo de comunicados del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz para la gestión y envío de
 * comunicados generales a diferentes grupos de usuarios del sistema.
 */
package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.admin.viewmodel.ComunicadosViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla principal para la gestión de comunicados del sistema.
 * 
 * Esta pantalla proporciona una interfaz completa para la creación,
 * envío y gestión de comunicados a diferentes grupos de usuarios,
 * con capacidades de segmentación y seguimiento.
 * 
 * ## Características
 * - Creación de comunicados
 * - Selección de destinatarios
 * - Historial de comunicados
 * - Estado de envío
 * 
 * ## Funcionalidades
 * - Redacción de comunicados
 * - Selección de grupos objetivo
 * - Envío programado
 * - Seguimiento de entrega
 * 
 * ## Estados
 * - Formulario de creación
 * - Lista de comunicados
 * - Carga de datos
 * - Errores y mensajes
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona la lógica de comunicados
 * 
 * @see ComunicadosViewModel
 * @see Comunicado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadosScreen(
    navController: NavController,
    viewModel: ComunicadosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.cargarComunicados()
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }
    
    LaunchedEffect(uiState.success) {
        uiState.success?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearSuccess()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sistema de Comunicados") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleNuevoComunicado() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Comunicado",
                    tint = MaterialTheme.colorScheme.onPrimary
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.mostrarFormulario) {
                // Mostrar formulario de nuevo comunicado
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Nuevo Comunicado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.titulo,
                        onValueChange = { viewModel.updateTitulo(it) },
                        label = { Text("Título") },
                        placeholder = { Text("Ingrese el título del comunicado") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.tituloError.isNotEmpty(),
                        supportingText = {
                            if (uiState.tituloError.isNotEmpty()) {
                                Text(uiState.tituloError)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.mensaje,
                        onValueChange = { viewModel.updateMensaje(it) },
                        label = { Text("Mensaje") },
                        placeholder = { Text("Ingrese el contenido del comunicado") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        isError = uiState.mensajeError.isNotEmpty(),
                        supportingText = {
                            if (uiState.mensajeError.isNotEmpty()) {
                                Text(uiState.mensajeError)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Destinatarios:", fontWeight = FontWeight.Bold)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = uiState.enviarATodos,
                                onCheckedChange = { viewModel.toggleEnviarATodos(it) }
                            )
                            Text("Todos los usuarios")
                        }
                    }
                    
                    if (!uiState.enviarATodos) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.enviarACentros,
                                    onCheckedChange = { viewModel.toggleEnviarACentros(it) }
                                )
                                Text("Centros educativos")
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.enviarAProfesores,
                                    onCheckedChange = { viewModel.toggleEnviarAProfesores(it) }
                                )
                                Text("Profesores")
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.enviarAFamiliares,
                                    onCheckedChange = { viewModel.toggleEnviarAFamiliares(it) }
                                )
                                Text("Familiares")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.toggleNuevoComunicado() }
                        ) {
                            Text("Cancelar")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { viewModel.enviarComunicado() },
                            enabled = !uiState.isEnviando
                        ) {
                            if (uiState.isEnviando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enviar")
                        }
                    }
                    
                    if (uiState.enviado) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Comunicado enviado correctamente",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Mostrar lista de comunicados
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Comunicados enviados",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (uiState.comunicados.isEmpty()) {
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
                                    text = "No hay comunicados",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Text(
                                    text = "Crea un nuevo comunicado para enviar a los usuarios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.comunicados) { comunicado ->
                                ComunicadoItem(comunicado = comunicado)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente que representa un comunicado individual.
 * 
 * Este componente implementa la visualización de un comunicado,
 * mostrando su información relevante y estado de envío.
 * 
 * ## Características
 * - Información del comunicado
 * - Estado de envío
 * - Destinatarios
 * - Fecha de envío
 * 
 * @param comunicado Datos del comunicado a mostrar
 * @param onDelete Callback para eliminar el comunicado
 * 
 * @see Comunicado
 */
@Composable
private fun ComunicadoItem(
    comunicado: Comunicado
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = comunicado.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = comunicado.fecha,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = comunicado.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Destinatarios: ${comunicado.destinatarios}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Enviado por: ${comunicado.remitente}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Vista previa de la pantalla de comunicados en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun ComunicadosScreenPreview() {
    MaterialTheme {
        ComunicadosScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Vista previa de la pantalla de comunicados en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ComunicadosScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ComunicadosScreen(
            navController = rememberNavController()
        )
    }
} 