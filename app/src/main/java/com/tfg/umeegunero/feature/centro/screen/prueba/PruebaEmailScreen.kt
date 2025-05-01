package com.tfg.umeegunero.feature.centro.screen.prueba

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.centro.viewmodel.prueba.PruebaEmailViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla para probar el envío de correos electrónicos.
 * Esta pantalla solo se utiliza para fines de desarrollo y pruebas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebaEmailScreen(
    viewModel: PruebaEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var destinatario by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { mensaje ->
            scope.launch {
                snackbarHostState.showSnackbar(mensaje)
                viewModel.limpiarMensaje()
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Prueba de Envío de Email") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Herramienta de prueba para el envío de emails",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = destinatario,
                onValueChange = { destinatario = it },
                label = { Text("Dirección de correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Opciones de prueba
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.enviarEmailAprobado(destinatario) },
                    enabled = destinatario.isNotEmpty() && !uiState.enviando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enviar Aprobado")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = { viewModel.enviarEmailRechazado(destinatario) },
                    enabled = destinatario.isNotEmpty() && !uiState.enviando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enviar Rechazado")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.enviando) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enviando email... Por favor, espere.")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instrucciones
            Text(
                text = "Instrucciones:",
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("1. Introduce un correo electrónico válido")
                    Text("2. Selecciona el tipo de notificación a enviar")
                    Text("3. Verifica la bandeja de entrada del destinatario")
                    Text("4. Recuerda configurar la API KEY de SendGrid en EmailService.kt")
                }
            }
        }
    }
} 