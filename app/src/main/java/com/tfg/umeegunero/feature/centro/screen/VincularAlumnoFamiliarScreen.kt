package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.common.screen.DummyScreen

/**
 * Pantalla para vincular alumnos a familiares
 * 
 * Esta pantalla permite al administrador del centro vincular alumnos con sus familiares,
 * facilitando la gestión de relaciones familiares en el sistema.
 *
 * @param navController Controlador de navegación para gestionar la navegación entre pantallas
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@Composable
fun VincularAlumnoFamiliarScreen(
    navController: NavController,
    viewModel: com.tfg.umeegunero.feature.centro.viewmodel.VincularAlumnoFamiliarViewModel
) {
    DummyScreen(
        title = "Vincular Alumnos y Familiares",
        description = "Esta funcionalidad permitirá vincular alumnos con sus familiares para facilitar la comunicación y el seguimiento educativo. Estará disponible próximamente.",
        onNavigateBack = { navController.popBackStack() }
    )
} 