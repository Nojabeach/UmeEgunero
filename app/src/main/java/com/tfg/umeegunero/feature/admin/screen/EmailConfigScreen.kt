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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.admin.viewmodel.EmailConfigViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner

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
    
    // Diálogo de confirmación para descartar cambios
    var mostrarDialogoDescartar by remember { mutableStateOf(false) }
    
    if (mostrarDialogoDescartar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoDescartar = false },
            title = { Text("Descartar cambios") },
            text = { Text("¿Estás seguro de que deseas descartar los cambios?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.descartarCambios()
                        mostrarDialogoDescartar = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoDescartar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Prevenir navegación si hay cambios sin guardar
    BackHandler(enabled = uiState.cambiosPendientes) {
        mostrarDialogoDescartar = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Email") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.cambiosPendientes) {
                                mostrarDialogoDescartar = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (uiState.cambiosPendientes) {
                        IconButton(
                            onClick = { mostrarDialogoDescartar = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Descartar cambios"
                            )
                        }
                        
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
                            label = { Text("Email de Soporte (Destino)") },
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
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
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
                            text = "Configuración SMTP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Email del remitente
                        OutlinedTextField(
                            value = uiState.emailRemitente,
                            onValueChange = { viewModel.updateEmailRemitente(it) },
                            label = { Text("Email Remitente (SMTP)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null
                                )
                            },
                            isError = uiState.errores["emailRemitente"] != null,
                            supportingText = {
                                if (uiState.errores["emailRemitente"] != null) {
                                    Text(uiState.errores["emailRemitente"]!!)
                                } else {
                                    Text("Email para enviar mensajes (p.ej: gmail)")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        
                        // Contraseña
                        OutlinedTextField(
                            value = uiState.passwordTemporal,
                            onValueChange = { viewModel.updatePasswordRemitente(it) },
                            label = { Text("Contraseña SMTP") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.mostrarPassword) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (uiState.mostrarPassword) {
                                            "Ocultar contraseña"
                                        } else {
                                            "Mostrar contraseña"
                                        }
                                    )
                                }
                            },
                            visualTransformation = if (uiState.mostrarPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            isError = uiState.errores["passwordRemitente"] != null,
                            supportingText = {
                                if (uiState.errores["passwordRemitente"] != null) {
                                    Text(uiState.errores["passwordRemitente"]!!)
                                } else {
                                    Text("Para cambiar la contraseña definitiva, actualizar en la consola de Firebase")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        
                        // Información sobre Remote Config
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Seguridad de la contraseña",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "La contraseña aquí mostrada es solo temporal para pruebas. La contraseña real debe configurarse desde Firebase Remote Config para mayor seguridad.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Nombre del remitente
                        OutlinedTextField(
                            value = uiState.nombreRemitente,
                            onValueChange = { viewModel.updateNombreRemitente(it) },
                            label = { Text("Nombre del Remitente") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            supportingText = {
                                Text("Nombre que aparecerá como remitente")
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Opciones Adicionales",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Switch(
                                checked = uiState.usarEmailUsuarioComoRemitente,
                                onCheckedChange = { viewModel.updateUsarEmailUsuarioComoRemitente(it) }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Usar email del usuario como remitente",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Text(
                            text = "Si está activado, el email del usuario se mostrará como remitente cuando envíe un mensaje",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón de guardar en pantalla grande
                Button(
                    onClick = { viewModel.guardarConfiguracion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.cambiosPendientes && !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar configuración")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Información adicional
                Text(
                    text = "Nota: Para usar Gmail como servidor SMTP, es necesario habilitar la verificación en dos pasos y crear una 'Contraseña de aplicación' en la configuración de seguridad de Google.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)
    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    
    // Actualizar el callback si se cambia enabled
    SideEffect {
        backCallback.isEnabled = enabled
    }
    
    DisposableEffect(backDispatcher) {
        backDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }
} 