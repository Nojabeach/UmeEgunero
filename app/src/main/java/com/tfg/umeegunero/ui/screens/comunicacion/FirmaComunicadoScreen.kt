package com.tfg.umeegunero.ui.screens.comunicacion

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.ui.components.firma.SignatureCanvasWithControls
import com.tfg.umeegunero.util.FirmaDigitalUtil
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.launch

/**
 * Pantalla para firmar un comunicado
 * 
 * @param navController Controlador de navegación
 * @param comunicadoId ID del comunicado a firmar
 * @param viewModel ViewModel para la pantalla de firma
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmaComunicadoScreen(
    navController: NavController,
    comunicadoId: String,
    viewModel: FirmaComunicadoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var firmaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var puntosFirma by remember { mutableStateOf<List<FirmaDigitalUtil.PuntoFirma>>(emptyList()) }
    var estadoFirma by remember { mutableStateOf(FirmaComunicadoViewModel.EstadoFirma.Inicial) }
    
    // Cargar datos del comunicado
    LaunchedEffect(comunicadoId) {
        viewModel.cargarComunicado(comunicadoId)
    }
    
    // Observar cambios en el estado de la firma
    LaunchedEffect(viewModel.estadoFirma) {
        viewModel.estadoFirma.value.let { nuevoEstado ->
            estadoFirma = nuevoEstado
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firmar Comunicado") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Información del comunicado
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Comunicado: ${viewModel.comunicado.value?.titulo ?: "Cargando..."}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Por favor, firma este comunicado para confirmar que has leído y aceptas su contenido.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Canvas para la firma
            SignatureCanvasWithControls(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                onFirmaCapturada = { puntos ->
                    puntosFirma = puntos
                    if (puntos.isNotEmpty()) {
                        firmaBitmap = FirmaDigitalUtil.crearBitmapDeFirma(puntos)
                    } else {
                        firmaBitmap = null
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botones de acción
            when (estadoFirma) {
                FirmaComunicadoViewModel.EstadoFirma.Inicial -> {
                    Button(
                        onClick = {
                            if (firmaBitmap != null) {
                                viewModel.firmarComunicado(comunicadoId, firmaBitmap!!)
                            }
                        },
                        enabled = firmaBitmap != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Firmar")
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text("Firmar Comunicado")
                    }
                }
                FirmaComunicadoViewModel.EstadoFirma.Firmando -> {
                    Text(
                        text = "Procesando firma...",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                FirmaComunicadoViewModel.EstadoFirma.Exito -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¡Firma completada con éxito!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver al Comunicado")
                        }
                    }
                }
                FirmaComunicadoViewModel.EstadoFirma.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Error al firmar el comunicado: ${viewModel.mensajeError.value}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.resetEstado() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Reintentar")
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
} 