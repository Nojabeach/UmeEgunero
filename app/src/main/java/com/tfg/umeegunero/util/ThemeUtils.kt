package com.tfg.umeegunero.util

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Utilidades para el manejo del tema en la aplicación.
 */
object ThemeUtils {
    /**
     * Extensión para verificar si el tema actual es claro.
     * 
     * @return true si el tema es claro, false si es oscuro
     */
    fun ColorScheme.isLight(): Boolean {
        val backgroundColor = this.background
        val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
        return luminance > 0.5
    }

    /**
     * Composable que devuelve si el tema actual es claro.
     * 
     * @return true si el tema es claro, false si es oscuro
     */
    @Composable
    fun isLightTheme(): Boolean {
        return MaterialTheme.colorScheme.isLight()
    }
} 