package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.admin.viewmodel.ComunicadosViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.border

/**
 * Pantalla para la gestión y envío de comunicados generales
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
            } else if (uiState.showNuevoComunicado) {
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
                        isError = uiState.titulo.isBlank(),
                        supportingText = {
                            if (uiState.titulo.isBlank()) {
                                Text("El título es obligatorio")
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
                        isError = uiState.mensaje.isBlank(),
                        supportingText = {
                            if (uiState.mensaje.isBlank()) {
                                Text("El mensaje es obligatorio")
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
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
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
                    
                    if (uiState.success != null) {
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
                                    text = uiState.success ?: "",
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
                                ComunicadoItem(
                                    comunicado = comunicado,
                                    onVerEstadisticas = { viewModel.verEstadisticas(it) },
                                    onVerFirmaDigital = { viewModel.verFirmaDigital(it) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Diálogo de estadísticas
            if (uiState.showEstadisticas) {
                EstadisticasDialog(
                    estadisticas = uiState.estadisticas ?: emptyMap(),
                    onDismiss = { viewModel.cerrarEstadisticas() }
                )
            }
            
            // Diálogo de firma digital
            if (uiState.showFirmaDigital) {
                uiState.firmaDigital?.let { firma ->
                    FirmaDigitalDialog(
                        firmaDigital = firma,
                        onDismiss = { viewModel.cerrarFirmaDigital() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadoItem(
    comunicado: Comunicado,
    onVerEstadisticas: (Comunicado) -> Unit,
    onVerFirmaDigital: (Comunicado) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comunicado.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(comunicado.fechaCreacion.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = comunicado.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onVerEstadisticas(comunicado) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Ver estadísticas"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Estadísticas")
                    }
                    
                    if (comunicado.firmaDigital != null) {
                        TextButton(
                            onClick = { onVerFirmaDigital(comunicado) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = "Ver firma digital"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Firma")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadisticasDialog(
    estadisticas: Map<String, Any>,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Estadísticas de lectura") },
        text = {
            Column {
                Text("Total de destinatarios: ${estadisticas["totalDestinatarios"]}")
                Text("Lecturas confirmadas: ${estadisticas["lecturasConfirmadas"]}")
                Text("Porcentaje de lectura: ${estadisticas["porcentajeLectura"]}%")
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Últimas lecturas:", style = MaterialTheme.typography.titleSmall)
                (estadisticas["ultimasLecturas"] as? List<Map<String, Any>>)?.forEach { lectura ->
                    Text("${lectura["usuarioNombre"]} - ${dateFormat.format((lectura["fechaLectura"] as com.google.firebase.Timestamp).toDate())}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun FirmaDigitalDialog(
    firmaDigital: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Firma Digital") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = firmaDigital,
                    contentDescription = "Firma digital",
                    modifier = Modifier
                        .size(200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Vista previa de la pantalla de comunicados
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