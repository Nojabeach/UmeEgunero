package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.runtime.Composable
import com.tfg.umeegunero.feature.common.screen.DummyScreen

/**
 * Pantalla para vincular alumnos a familiares
 * 
 * Esta pantalla permite al administrador del centro vincular alumnos con sus familiares,
 * facilitando la gestión de relaciones familiares en el sistema.
 *
 * @param onBack Función para volver a la pantalla anterior
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
@Composable
fun VincularAlumnoFamiliarScreen(
    onBack: () -> Unit
) {
    DummyScreen(
        title = "Vincular Alumnos y Familiares",
        description = "Esta funcionalidad permitirá vincular alumnos con sus familiares para facilitar la comunicación y el seguimiento educativo. Estará disponible próximamente.",
        onNavigateBack = onBack
    )
} 