package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.feature.admin.viewmodel.ComunicadosViewModel
import com.tfg.umeegunero.navigation.AppScreens
import kotlinx.coroutines.launch

/**
 * Pantalla para la gestión y envío de comunicados generales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadosScreen(
    navController: NavController,
    viewModel: ComunicadosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showNewComunicadoDialog by remember { mutableStateOf(false) }
    
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
                title = { Text("Comunicados") },
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
            ExtendedFloatingActionButton(
                onClick = { showNewComunicadoDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nuevo") },
                text = { Text("Nuevo Comunicado") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
            } else if (uiState.comunicados.isEmpty()) {
                // No hay comunicados
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Announcement,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay comunicados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Crea un nuevo comunicado para informar a los usuarios",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { showNewComunicadoDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text("Crear Comunicado")
                    }
                }
            } else {
                // Lista de comunicados
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Comunicados recientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(uiState.comunicados) { comunicado ->
                        ComunicadoItem(
                            comunicado = comunicado,
                            onDelete = { viewModel.eliminarComunicado(comunicado.id) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Espacio para FAB
                    }
                }
            }
        }
    }
    
    // Diálogo para nuevo comunicado
    if (showNewComunicadoDialog) {
        NuevoComunicadoDialog(
            onDismiss = { showNewComunicadoDialog = false },
            onConfirm = { titulo, mensaje, destinatarios ->
                viewModel.crearComunicado(titulo, mensaje, destinatarios)
                showNewComunicadoDialog = false
            }
        )
    }
}

@Composable
fun ComunicadoItem(
    comunicado: com.tfg.umeegunero.data.model.Comunicado,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = comunicado.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = comunicado.mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha
                Text(
                    text = "Enviado: ${formatDate(comunicado.fechaCreacion)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Destinatarios
                val tiposDestinatarios = comunicado.tiposDestinatarios.joinToString(", ") { 
                    getTipoUsuarioLabel(it)
                }
                Text(
                    text = "Para: $tiposDestinatarios",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoComunicadoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<TipoUsuario>) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var profesoresSelected by remember { mutableStateOf(true) }
    var familiaresSelected by remember { mutableStateOf(true) }
    var adminCentroSelected by remember { mutableStateOf(false) }
    var adminAppSelected by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Comunicado") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mensaje
                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    label = { Text("Mensaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Destinatarios
                Text(
                    text = "Destinatarios:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Checkboxes para tipos de usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = profesoresSelected,
                        onCheckedChange = { profesoresSelected = it }
                    )
                    Text("Profesores")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = familiaresSelected,
                        onCheckedChange = { familiaresSelected = it }
                    )
                    Text("Familiares")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = adminCentroSelected,
                        onCheckedChange = { adminCentroSelected = it }
                    )
                    Text("Administradores de Centro")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = adminAppSelected,
                        onCheckedChange = { adminAppSelected = it }
                    )
                    Text("Administradores de App")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank() && mensaje.isNotBlank() && 
                        (profesoresSelected || familiaresSelected || adminCentroSelected || adminAppSelected)) {
                        val destinatarios = mutableListOf<TipoUsuario>()
                        if (profesoresSelected) destinatarios.add(TipoUsuario.PROFESOR)
                        if (familiaresSelected) destinatarios.add(TipoUsuario.FAMILIAR)
                        if (adminCentroSelected) destinatarios.add(TipoUsuario.ADMIN_CENTRO)
                        if (adminAppSelected) destinatarios.add(TipoUsuario.ADMIN_APP)
                        
                        onConfirm(titulo, mensaje, destinatarios)
                    }
                },
                enabled = titulo.isNotBlank() && mensaje.isNotBlank() && 
                    (profesoresSelected || familiaresSelected || adminCentroSelected || adminAppSelected)
            ) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

private fun getTipoUsuarioLabel(tipo: TipoUsuario): String {
    return when (tipo) {
        TipoUsuario.ADMIN_APP -> "Administradores App"
        TipoUsuario.ADMIN_CENTRO -> "Administradores Centro"
        TipoUsuario.PROFESOR -> "Profesores"
        TipoUsuario.FAMILIAR -> "Familiares"
        TipoUsuario.ALUMNO -> "Alumnos"
    }
} 