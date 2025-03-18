package com.tfg.umeegunero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tfg.umeegunero.data.model.TemaPref
import com.tfg.umeegunero.data.repository.PreferenciasRepository

/**
 * Determina si el modo oscuro debe estar activado según la preferencia del usuario
 */
@Composable
fun rememberDarkThemeState(
    preferenciasRepository: PreferenciasRepository
): Boolean {
    val themePreference by preferenciasRepository.temaPreferencia.collectAsState(initial = TemaPref.SYSTEM)
    
    return when (themePreference) {
        TemaPref.DARK -> true
        TemaPref.LIGHT -> false
        TemaPref.SYSTEM -> isSystemInDarkTheme()
    }
}

/**
 * Obtiene el nombre de la preferencia de tema para mostrar en la UI
 */
@Composable
fun getNombreTema(tema: TemaPref): String {
    return when (tema) {
        TemaPref.DARK -> "Oscuro"
        TemaPref.LIGHT -> "Claro"
        TemaPref.SYSTEM -> "Sistema"
    }
} 