package com.tfg.umeegunero.ui.theme

import androidx.compose.ui.graphics.Color

// Colores personalizados para los botones de acceso
val CentroColor = Color(0xFF1976D2)  // Azul más profesional
val ProfesorColor = Color(0xFF388E3C) // Verde más suave
val FamiliarColor = Color(0xFF7B1FA2) // Morado más suave
val AdminColor = Color(0xFF455A64)    // Gris azulado para admin

// Colores estándar (redefinidos si se eliminaron de Color.kt)
val Green500 = Color(0xFF4CAF50)
val Red500 = Color(0xFFF44336) // Similar a AppColors.Error
val Red100 = Color(0xFFFFCDD2) // Rojo muy claro

// Colores específicos de la aplicación
val AcademicoColor = Color(0xFF42A5F5) // Azul claro ejemplo
val AcademicoColorDark = Color(0xFF1E88E5) // Azul oscuro ejemplo
val AlumnoColor = Color(0xFFFF9800) // Naranja ejemplo
val GradientStart = Color(0xFF1E88E5) // Azul para gradiente
val GradientEnd = Color(0xFF6A1B9A) // Púrpura para gradiente

// Función placeholder para color de categoría (revisar lógica)
fun colorCategoriaActividad(categoria: String): Color {
    // Lógica placeholder: devuelve un color basado en el hash de la categoría
    // TODO: Implementar la lógica real para asignar colores a categorías
    val hash = categoria.hashCode()
    return Color(
        red = (hash and 0xFF0000 shr 16) / 255f,
        green = (hash and 0x00FF00 shr 8) / 255f,
        blue = (hash and 0x0000FF) / 255f
    )
} 