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
import com.tfg.umeegunero.feature.profesor.viewmodel.MisAlumnosProfesorViewModel
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme

/**
 * Pantalla que muestra la lista de alumnos asignados al profesor.
 *
 * TODO: Implementar la lista real de alumnos (LazyColumn), 
 *       manejar estados de carga y error, y la navegación al perfil del alumno.
 *
 * @param navController Controlador de navegación.
 * @param viewModel ViewModel para obtener la lista de alumnos.
 * @author Maitane Ibáñez (2º DAM)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisAlumnosProfesorScreen(
    navController: NavController,
    viewModel: MisAlumnosProfesorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Alumnos") },
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
                // TODO: Implementar LazyColumn con la lista de uiState.alumnos
                // Cada item debe ser clickable para navegar a AppScreens.PerfilAlumno.route
                Text("Lista de alumnos aparecerá aquí.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MisAlumnosProfesorScreenPreview() {
    UmeEguneroTheme {
        MisAlumnosProfesorScreen(navController = rememberNavController())
    }
} 