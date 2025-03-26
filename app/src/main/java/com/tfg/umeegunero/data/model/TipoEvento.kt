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
    )
} 