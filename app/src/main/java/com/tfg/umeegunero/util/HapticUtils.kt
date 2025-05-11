package com.tfg.umeegunero.util

import android.util.Log
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Utilidades para la retroalimentación háptica en Jetpack Compose.
 * 
 * Estas funciones proporcionan formas seguras de ejecutar la retroalimentación 
 * háptica para mejorar la experiencia táctil en la aplicación.
 */

/**
 * Ejecuta la retroalimentación háptica de forma segura, capturando cualquier excepción 
 * que pueda ocurrir durante el proceso.
 * 
 * @return Boolean indicando si la retroalimentación se ejecutó correctamente.
 */
fun HapticFeedback.performHapticFeedbackSafely() : Boolean {
    return try {
        this.performHapticFeedback(HapticFeedbackType.LongPress)
        true
    } catch (e: Exception) {
        Log.e("HapticFeedback", "Error al ejecutar retroalimentación háptica", e)
        false
    }
} 