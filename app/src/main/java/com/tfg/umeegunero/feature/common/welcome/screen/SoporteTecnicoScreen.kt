package com.tfg.umeegunero.feature.common.welcome.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.common.welcome.viewmodel.SoporteTecnicoViewModel
import com.tfg.umeegunero.ui.theme.GradientEnd
import com.tfg.umeegunero.ui.theme.GradientStart

/**
 * Pantalla de soporte técnico que permite enviar un correo electrónico
 * al administrador de la aplicación directamente utilizando SMTP.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoporteTecnicoScreen(
    viewModel: SoporteTecnicoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.enviado) {
        if (uiState.enviado) {
            snackbarHostState.showSnackbar("Mensaje enviado correctamente")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soporte Técnico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Envíanos tu consulta",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Campo de nombre
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = { viewModel.updateNombre(it) },
                            label = { Text("Nombre") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            isError = uiState.errores["nombre"] != null,
                            supportingText = {
                                if (uiState.errores["nombre"] != null) {
                                    Text(uiState.errores["nombre"]!!)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        // Campo de email
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null
                                )
                            },
                            isError = uiState.errores["email"] != null,
                            supportingText = {
                                if (uiState.errores["email"] != null) {
                                    Text(uiState.errores["email"]!!)
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

                        // Campo de asunto
                        OutlinedTextField(
                            value = uiState.asunto,
                            onValueChange = { viewModel.updateAsunto(it) },
                            label = { Text("Asunto") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Subject,
                                    contentDescription = null
                                )
                            },
                            isError = uiState.errores["asunto"] != null,
                            supportingText = {
                                if (uiState.errores["asunto"] != null) {
                                    Text(uiState.errores["asunto"]!!)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        // Campo de mensaje
                        OutlinedTextField(
                            value = uiState.mensaje,
                            onValueChange = { viewModel.updateMensaje(it) },
                            label = { Text("Mensaje") },
                            isError = uiState.errores["mensaje"] != null,
                            supportingText = {
                                if (uiState.errores["mensaje"] != null) {
                                    Text(uiState.errores["mensaje"]!!)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(bottom = 24.dp),
                            minLines = 5
                        )

                        // Botón de enviar
                        Button(
                            onClick = { viewModel.enviarEmail() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    "Enviar Mensaje",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 