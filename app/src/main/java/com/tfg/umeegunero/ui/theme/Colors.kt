package com.tfg.umeegunero.ui.theme

import androidx.compose.ui.graphics.Color
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoTarea

/**
 * Devuelve un color representativo para cada categoría de actividad preescolar
 */
fun colorCategoriaActividad(categoria: CategoriaActividad): Color {
    return when (categoria) {
        CategoriaActividad.JUEGO -> Color(0xFF8E24AA)         // Morado
        CategoriaActividad.MOTOR -> Color(0xFFE53935)         // Rojo
        CategoriaActividad.LENGUAJE -> Color(0xFF1E88E5)      // Azul
        CategoriaActividad.MUSICA -> Color(0xFF26A69A)        // Verde azulado
        CategoriaActividad.ARTE -> Color(0xFFFFB300)          // Ámbar
        CategoriaActividad.EXPLORACION -> Color(0xFF7CB342)   // Verde claro
        CategoriaActividad.AUTONOMIA -> Color(0xFFEF6C00)     // Naranja
        CategoriaActividad.OTRA -> Color(0xFF757575)          // Gris
    }
} 