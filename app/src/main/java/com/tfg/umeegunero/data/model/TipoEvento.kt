package com.tfg.umeegunero.data.model

import androidx.compose.ui.graphics.Color

enum class TipoEvento(
    val nombre: String,
    val color: Color
) {
    FESTIVO(
        nombre = "Festivo",
        color = Color(0xFFE57373) // Rojo claro
    ),
    ESCOLAR(
        nombre = "Escolar",
        color = Color(0xFF81C784) // Verde claro
    ),
    EXCURSION(
        nombre = "Excursión",
        color = Color(0xFF64B5F6) // Azul claro
    ),
    REUNION(
        nombre = "Reunión",
        color = Color(0xFFFFB74D) // Naranja claro
    ),
    CLASE(
        nombre = "Clase",
        color = Color(0xFF4CAF50) // Verde
    ),
    EXAMEN(
        nombre = "Examen",
        color = Color(0xFFE91E63) // Rosa
    ),
    OTRO(
        nombre = "Otro",
        color = Color(0xFF9C27B0) // Púrpura
    )
} 