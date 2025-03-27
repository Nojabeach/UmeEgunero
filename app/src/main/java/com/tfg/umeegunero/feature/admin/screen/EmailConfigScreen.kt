package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.EmailConfigViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de configuración de email para el administrador
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailConfigScreen(
    viewModel: EmailConfigViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Mostrar mensajes
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMensaje()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Email") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (uiState.cambiosPendientes) {
                        IconButton(
                            onClick = { viewModel.guardarConfiguracion() },
                            enabled = !uiState.isSaving
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Guardar configuración"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Fecha de última actualización
                uiState.ultimaActualizacion?.let { timestamp ->
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val fechaFormateada = formatter.format(date)
                    
                    Text(
                        text = "Última actualización: $fechaFormateada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Configuración de Soporte Técnico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Email de destino
                        OutlinedTextField(
                            value = uiState.emailDestino,
                            onValueChange = { viewModel.updateEmailDestino(it) },
                            label = { Text("Email de Soporte") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null
                                )
                            },
                            isError = uiState.errores["emailDestino"] != null,
                            supportingText = {
                                if (uiState.errores["emailDestino"] != null) {
                                    Text(uiState.errores["emailDestino"]!!)
                                } else {
                                    Text("Los mensajes de soporte se enviarán a este email")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Nota: La contraseña para el envío de emails se configura desde Firebase Remote Config",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
} 