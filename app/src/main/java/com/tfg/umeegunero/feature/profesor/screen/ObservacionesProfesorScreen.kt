package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.feature.profesor.viewmodel.ObservacionesProfesorViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla para registrar observaciones o incidencias sobre alumnos o la clase.
 *
 * TODO: Implementar la interfaz para escribir la observación, seleccionar 
 *       tipo/alumno (si aplica), y manejar el estado de guardado/error.
 *
 * @param navController Controlador de navegación.
 * @param viewModel ViewModel para gestionar el guardado de observaciones.
 * @author Maitane Ibáñez (2º DAM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservacionesProfesorScreen(
    navController: NavController,
    viewModel: ObservacionesProfesorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // TODO: Manejar uiState.observacionGuardada (ej. mostrar Snackbar y navegar atrás)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Observación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                 colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                // TODO: Añadir TextField para escribir observación, botón de guardar,
                // y posiblemente un selector de alumno o tipo de observación.
                Text("Formulario para registrar observación aquí.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ObservacionesProfesorScreenPreview() {
    UmeEguneroTheme {
        ObservacionesProfesorScreen(navController = rememberNavController())
    }
} 