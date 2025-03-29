package com.tfg.umeegunero.util

import android.util.Log

/**
 * Utilidades para la gestión de administración de centros educativos.
 *
 * Esta clase proporciona funciones de utilidad relacionadas con la administración
 * de centros educativos en la aplicación UmeEgunero, principalmente enfocadas
 * a obtener información del centro actual y gestionar operaciones comunes.
 */
object CentroAdminUtils {
    
    /**
     * Obtiene el identificador del centro educativo actual.
     * 
     * En una implementación real, este identificador vendría determinado por el usuario
     * que ha iniciado sesión y sus permisos. Por ahora, devuelve un valor predeterminado
     * para propósitos de prueba y desarrollo.
     *
     * @return String con el identificador único del centro
     */
    fun getCentroIdActual(): String {
        Log.d("CentroAdminUtils", "Obteniendo ID de centro actual (temporal)")
        // Implementación temporal - en una aplicación real, esto vendría del usuario logueado
        return "centro_test"
    }
} 