package com.tfg.umeegunero.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role

/**
 * Utilidades para mejorar la accesibilidad en la aplicación
 * Proporciona extensiones y funciones para facilitar la implementación de accesibilidad en Compose
 */
object AccessibilityUtils {

    /**
     * Añade una descripción semántica accesible a un Modifier
     * @param description Descripción del contenido
     * @param stateDesc Descripción opcional del estado actual
     * @param role Rol de accesibilidad (por defecto es None)
     */
    fun Modifier.accessibilityDescription(
        description: String,
        stateDesc: String? = null,
        role: Role? = null
    ): Modifier = semantics {
        contentDescription = description
        if (stateDesc != null) {
            stateDescription = stateDesc
        }
        if (role != null) {
            this.role = role
        }
    }
    
    /**
     * Extensión para hacer un elemento clickable con soporte de accesibilidad
     */
    fun Modifier.accessibleClickable(
        description: String,
        stateDesc: String? = null,
        role: Role = Role.Button,
        onClick: () -> Unit
    ): Modifier = composed {
        val interactionSource = remember { MutableInteractionSource() }
        
        this
            .semantics {
                contentDescription = description
                if (stateDesc != null) {
                    stateDescription = stateDesc
                }
                this.role = role
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    }
    
    /**
     * Modifica la densidad de toque para elementos interactivos según las preferencias de accesibilidad
     * @param defaultSize Tamaño por defecto en dp
     * @return Tamaño ajustado según preferencias de accesibilidad
     */
    fun adjustTouchTargetSize(defaultSize: Float): Float {
        // Esta función podría leer las preferencias del usuario para ajustar
        // el tamaño de los elementos interactivos según necesidades de accesibilidad
        // Por ahora, simplemente devolvemos un mínimo de 48dp como recomienda Material Design
        return if (defaultSize < 48f) 48f else defaultSize
    }
} 