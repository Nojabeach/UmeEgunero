package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Pantalla para crear rápidamente usuarios (profesores, alumnos o familiares)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearUsuarioRapidoScreen(
    navController: NavController,
    viewModel: CrearUsuarioRapidoViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // Contenido en desarrollo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Creación Rápida de Usuarios",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Esta pantalla está en desarrollo. Pronto podrás crear usuarios rápidamente desde aquí.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones provisionales para acceder a pantallas existentes
                ElevatedButton(
                    onClick = { navController.navigate(AppScreens.AddUser.createRoute(false, "profesor")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Profesor")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ElevatedButton(
                    onClick = { navController.navigate(AppScreens.AddUser.createRoute(false, "alumno")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Alumno")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ElevatedButton(
                    onClick = { navController.navigate(AppScreens.AddUser.createRoute(false, "familiar")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear Familiar")
                }
            }
        }
    }
}

/**
 * ViewModel para la pantalla de creación rápida de usuarios
 * Implementación pendiente
 */
@HiltViewModel
class CrearUsuarioRapidoViewModel @Inject constructor() : ViewModel() 