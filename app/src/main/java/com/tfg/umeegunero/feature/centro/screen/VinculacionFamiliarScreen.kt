package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.tfg.umeegunero.feature.common.screen.DummyScreenWithDashboard
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Pantalla para gestionar las vinculaciones entre alumnos y familiares
 * Permite al administrador del centro asociar familiares a alumnos
 */
@Composable
fun VinculacionFamiliarScreen(
    navController: NavController
) {
    DummyScreenWithDashboard(
        title = "Gestión de Vínculos Familiares",
        description = "Estamos trabajando en esta funcionalidad para permitir la gestión de las relaciones entre alumnos y sus familiares. Estará disponible próximamente.",
        onBackClick = { navController.popBackStack() },
        onDashboardClick = { 
            navController.navigate(AppScreens.CentroDashboard.route) {
                popUpTo(AppScreens.CentroDashboard.route) {
                    inclusive = true
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VinculacionFamiliarScreenPreview() {
    VinculacionFamiliarScreen(
        navController = rememberNavController()
    )
} 