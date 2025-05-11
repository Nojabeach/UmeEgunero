package com.tfg.umeegunero.util

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

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
     * Modifica el tamaño mínimo del componente para asegurar que cumpla con los requisitos
     * de accesibilidad (48dp)
     */
    fun Modifier.accessibleMinSize(): Modifier {
        return this.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
    }

    /**
     * Crea un área de toque más amplia alrededor del componente
     */
    fun Modifier.touchTargetAdjustment(extraPadding: PaddingValues = PaddingValues(8.dp)): Modifier {
        return this.padding(extraPadding)
    }

    /**
     * Hace clickable un componente con interacción personalizada
     */
    fun Modifier.accessibleClickable(
        enabled: Boolean = true,
        onClickLabel: String? = null,
        role: Role? = null,
        onClick: () -> Unit
    ): Modifier = composed {
        this.clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    }

    /**
     * Calcula el tamaño mínimo accesible basado en un valor existente
     */
    fun calculateAccessibleSize(defaultSize: Float): Float {
        return if (defaultSize < 48f) 48f else defaultSize
    }
} 