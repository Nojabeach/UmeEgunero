/**
 * Módulo de gestión de clases del sistema UmeEgunero.
 * 
 * Este módulo implementa la interfaz para visualizar y gestionar
 * las clases de un centro educativo, permitiendo ver detalles
 * y realizar acciones sobre cada clase.
 * 
 * ## Características
 * - Lista completa de clases
 * - Filtrado y búsqueda
 * - Detalles de cada clase
 * - Gestión de asignaciones
 * 
 * ## Funcionalidades
 * - Visualización de clases
 * - Acceso a detalles
 * - Navegación a edición
 * - Integración con profesores
 * 
 * ## Estados
 * - Carga de datos
 * - Lista vacía
 * - Error de carga
 * - Detalles expandidos
 * 
 * @see ListaClasesViewModel
 * @see Clase
 */
package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.tfg.umeegunero.data.model.Clase

/**
 * Pantalla principal de lista de clases.
 * 
 * Esta pantalla muestra todas las clases del centro en una lista
 * desplazable, permitiendo acceder a los detalles de cada una
 * y realizar acciones sobre ellas.
 * 
 * ## Características
 * - Lista con scroll infinito
 * - Tarjetas de información
 * - Acciones contextuales
 * - Navegación integrada
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona la lógica de clases
 * 
 * @see ListaClasesViewModel
 * @see ClaseCard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClasesScreen(
    navController: NavController,
    viewModel: ListaClasesViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clases") },
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
        // Mensaje de redirección a la pantalla de cursos
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
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "¡Gestión de Clases actualizada!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "La funcionalidad de gestión de clases se ha mejorado y ahora está disponible a través de la pantalla de Gestión de Cursos.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Text(
                            text = "Para gestionar las clases, primero seleccione un curso y luego utilice el botón 'Ver Clases'.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { navController.navigate(AppScreens.ListaCursos.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Ir a Gestión de Cursos",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ir a Gestión de Cursos")
                }
            }
        }
    }
}

/**
 * ViewModel para la pantalla de listado de clases
 * Implementación mínima, ya que la funcionalidad real está en GestionClasesViewModel
 */
@HiltViewModel
class ListaClasesViewModel @Inject constructor() : ViewModel()

/**
 * Tarjeta que muestra la información de una clase.
 * 
 * Este componente representa una clase individual en la lista,
 * mostrando su información básica y permitiendo acciones.
 * 
 * ## Características
 * - Diseño Material Design 3
 * - Información relevante
 * - Acciones contextuales
 * - Estados visuales
 * 
 * @param clase Datos de la clase a mostrar
 * @param onClick Callback para gestionar el click
 * 
 * @see Clase
 */
@Composable
fun ClaseCard(
    clase: Clase,
    onClick: () -> Unit
) {
    // ... existing code ...
}

/**
 * Vista previa de la pantalla de lista de clases en modo claro.
 */
@Preview(showBackground = true)
@Composable
fun ListaClasesScreenPreview() {
    UmeEguneroTheme {
        ListaClasesScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Vista previa de la pantalla de lista de clases en modo oscuro.
 */
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ListaClasesScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        ListaClasesScreen(
            navController = rememberNavController()
        )
    }
} 