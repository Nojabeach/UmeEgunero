package com.tfg.umeegunero.util

import androidx.compose.ui.graphics.Color
import com.tfg.umeegunero.data.model.TipoUsuario

fun getUserColor(tipo: TipoUsuario?): Color = when (tipo) {
    TipoUsuario.ADMIN_APP      -> Color(0xFF0D47A1) // Azul oscuro
    TipoUsuario.ADMIN_CENTRO   -> Color(0xFF42A5F5) // Azul claro
    TipoUsuario.PROFESOR       -> Color(0xFF388E3C) // Verde
    TipoUsuario.FAMILIAR       -> Color(0xFF8E24AA) // Morado
    TipoUsuario.ALUMNO         -> Color(0xFFFF9800) // Naranja
    else                       -> Color.Gray        // Color por defecto
}
