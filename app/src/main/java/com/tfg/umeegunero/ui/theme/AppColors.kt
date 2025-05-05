package com.tfg.umeegunero.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colores personalizados para los diferentes tipos de usuarios en la aplicación.
 * 
 * Estos colores siguen la guía de diseño de Material 3 y están optimizados para
 * proporcionar una experiencia visual coherente y accesible.
 */
object AppColors {
    // Colores de usuario (predominan sobre los demás)
    val AdminApp = Color(0xFF0D47A1)      // ADMIN_APP (Azul oscuro)
    val AdminCentro = Color(0xFF42A5F5)   // ADMIN_CENTRO (Azul claro)
    val Profesor = Color(0xFF388E3C)      // PROFESOR (Verde)
    val Familiar = Color(0xFF8E24AA)      // FAMILIAR (Morado)
    val Alumno = Color(0xFFFF9800)        // ALUMNO (Naranja)
    val UserDefault = Color.Gray          // Color por defecto

    // Colores personalizados para botones y otros elementos
    val CentroColor = Color(0xFF1976D2)   // Azul profesional
    val ProfesorColor = Profesor         // Verde (igual que PROFESOR)
    val FamiliarColor = Color(0xFF7B1FA2) // Morado más suave
    val AdminColor = Color(0xFF0D47A1)    // Azul oscuro

    // Colores estándar
    val Green500 = Color(0xFF4CAF50)
    val Blue500 = Color(0xFF2196F3)    // Añadido para usar en más sitios
    val Red500 = Color(0xFFF44336) // Similar a Error
    val Red100 = Color(0xFFFFCDD2)

    // Colores específicos de la aplicación
    val AcademicoColor = AdminCentro      // Azul claro ejemplo (igual que ADMIN_CENTRO)
    val AcademicoColorDark = Color(0xFF1E88E5) // Azul oscuro ejemplo
    val GradientStart = Color(0xFF1E88E5) // Azul para gradiente
    val GradientEnd = Color(0xFF6A1B9A)   // Púrpura para gradiente

    // Colores de error y primarios
    val Error = Color(0xFFB00020)
    val PrimaryLight = Color(0xFF6200EE)
    val PrimaryDark = Color(0xFFBB86FC)

    // Colores base migrados de Theme.kt
    val PurplePrimary = Color(0xFF6750A4)
    val PurpleSecondary = Color(0xFF625B71)
    val PurpleTertiary = Color(0xFF7D5260)
    val PurpleError = Color(0xFFB3261E)

    val PurpleGrey80 = Color(0xFFCCC2DC)
    val Pink80 = Color(0xFFEFB8C8)
    val PurpleGrey40 = Color(0xFF625b71)
    val Pink40 = Color(0xFF7D5260)
    
    // Color faltante
    val Purple200 = Color(0xFFBB86FC)

    val White = Color.White
    val Black = Color.Black
    val LightGrey = Color(0xFFF5F5F5)
    val DarkGrey = Color(0xFF1C1B1F)
    val NearBlack = Color(0xFF202124) // Un gris muy oscuro para fondos oscuros

    // Función para obtener color de usuario
    fun getUserColor(tipo: com.tfg.umeegunero.data.model.TipoUsuario?): Color = when (tipo) {
        com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_APP      -> AdminApp
        com.tfg.umeegunero.data.model.TipoUsuario.ADMIN_CENTRO   -> AdminCentro
        com.tfg.umeegunero.data.model.TipoUsuario.PROFESOR       -> Profesor
        com.tfg.umeegunero.data.model.TipoUsuario.FAMILIAR       -> Familiar
        com.tfg.umeegunero.data.model.TipoUsuario.ALUMNO         -> Alumno
        else                                                     -> UserDefault
    }

    // Función para color de categoría (placeholder)
    fun colorCategoriaActividad(categoria: String): Color {
        val hash = categoria.hashCode()
        return Color(
            red = (hash and 0xFF0000 shr 16) / 255f,
            green = (hash and 0x00FF00 shr 8) / 255f,
            blue = (hash and 0x0000FF) / 255f
        )
    }
} 