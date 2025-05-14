package com.tfg.umeegunero.util

import android.util.Log
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import timber.log.Timber

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
fun HapticFeedback.performHapticFeedbackSafely(
    feedbackType: HapticFeedbackType = HapticFeedbackType.LongPress
): Boolean {
    return try {
        this.performHapticFeedback(feedbackType)
        true
    } catch (e: Exception) {
        Timber.e(e, "Error al realizar feedback háptico: ${e.message}")
        false
    }
} 