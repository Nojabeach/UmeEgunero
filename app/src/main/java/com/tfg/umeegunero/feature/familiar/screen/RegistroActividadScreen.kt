package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.RegistroActividad // Asume este modelo
import com.tfg.umeegunero.feature.familiar.viewmodel.RegistroActividadViewModel // Asume este ViewModel
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.ui.components.DefaultTopAppBar
import androidx.compose.runtime.LaunchedEffect // Importar LaunchedEffect
import androidx.compose.foundation.clickable // Importar clickable
import com.google.firebase.Timestamp // Importar Timestamp
import com.tfg.umeegunero.navigation.AppScreens
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Pantalla para que los familiares vean el historial de registros de actividad de sus hijos.
 *
 * Muestra una lista cronológica de los registros diarios (comidas, siestas, etc.)
 * para el hijo seleccionado.
 *
 * @param navController Controlador de navegación.
 * @param alumnoId ID del alumno cuyo historial se mostrará.
 * @param viewModel ViewModel que gestiona la lógica y el estado de los registros.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroActividadScreen(
    navController: NavController,
    alumnoId: String, // Necesitamos saber de qué alumno es el historial
    viewModel: RegistroActividadViewModel = hiltViewModel() // Asume la existencia de este ViewModel
) {
    val uiState by viewModel.uiState.collectAsState() // Asume un UiState en el ViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    // Cargar registros para el alumno específico
    LaunchedEffect(alumnoId) {
        viewModel.cargarRegistros(alumnoId)
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = "Historial de Actividad", // Podrías añadir el nombre del alumno aquí
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                containerColor = FamiliarColor,
                contentColor = Color.White
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.registros.isEmpty() -> {
                    Column(
                         modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                         Icon(
                            Icons.Filled.Restaurant, // O un icono más genérico de historial
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                         )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay registros de actividad disponibles.")
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.registros) { registro ->
                            // Placeholder para el item de registro
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        } catch (e: Exception) {
                                            Timber.e(e, "Error al realizar feedback háptico")
                                        }
                                        
                                        try {
                                            navController.navigate(AppScreens.DetalleRegistro.createRoute(registro.id))
                                            Timber.d("Navegando a detalle de registro: ${registro.id}")
                                        } catch (e: Exception) {
                                            Timber.e(e, "Error al navegar a detalle de registro: ${registro.id}")
                                        }
                                    },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    Text("Fecha: ${dateFormat.format(Date(registro.fecha.seconds * 1000))}", style = MaterialTheme.typography.titleMedium)
                                    // Mostrar resumen (ej. Comida: Completa, Siesta: Sí)
                                    Text("Resumen: (Pendiente)", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegistroActividadScreenPreview() {
    UmeEguneroTheme {
        RegistroActividadScreen(
            navController = rememberNavController(), 
            alumnoId = "preview_alumno"
        )
    }
} 