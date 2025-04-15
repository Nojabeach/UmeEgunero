package com.tfg.umeegunero.ui.theme

import androidx.compose.ui.graphics.Color
import com.tfg.umeegunero.data.model.CategoriaActividad
import com.tfg.umeegunero.data.model.EstadoTarea

// Paleta de colores principal inspirada en CDK Admin
val Primary = Color(0xFF5E35B1)      // Púrpura intenso (primario)
val PrimaryLight = Color(0xFF9162E4)  // Púrpura claro
val PrimaryDark = Color(0xFF280680)   // Púrpura oscuro

// Colores secundarios
val Secondary = Color(0xFF26A69A)     // Verde azulado
val SecondaryLight = Color(0xFF64D8CB) // Verde azulado claro
val SecondaryDark = Color(0xFF00766C)  // Verde azulado oscuro

// Colores de acento para diferentes tipos de usuarios
val AdminColor = Color(0xFF6200EA)    // Púrpura intenso para administradores
val CentroColor = Color(0xFF2962FF)   // Azul intenso para centros
val ProfesorColor = Color(0xFF00B8D4) // Cian intenso para profesores
val FamiliarColor = Color(0xFF00C853) // Verde intenso para familiares

// Colores para módulos académicos
val AcademicoColor = Color(0xFF7C4DFF) // Violeta para sección académica
val AcademicoColorDark = Color(0xFF4527A0) // Violeta oscuro para sección académica

// Colores para UI
val Background = Color(0xFFF5F5F5)    // Fondo claro (para modo claro)
val BackgroundDark = Color(0xFF121212) // Fondo oscuro (para modo oscuro)
val Surface = Color(0xFFFFFFFF)        // Superficie clara
val SurfaceDark = Color(0xFF1E1E1E)    // Superficie oscura

val OnPrimary = Color(0xFFFFFFFF)      // Color para texto sobre primario
val OnSecondary = Color(0xFFFFFFFF)    // Color para texto sobre secundario
val OnBackground = Color(0xFF212121)   // Color para texto sobre fondo
val OnSurface = Color(0xFF212121)      // Color para texto sobre superficie

// Colores para estados
val Success = Color(0xFF4CAF50)        // Verde para éxito
val Warning = Color(0xFFFFC107)        // Amarillo para advertencia
val Error = Color(0xFFF44336)          // Rojo para error
val Info = Color(0xFF2196F3)           // Azul para información

// Colores para gradientes
val GradientStart = Color(0xFF673AB7)  // Inicio de gradiente
val GradientEnd = Color(0xFF3F51B5)    // Fin de gradiente

// Colores para modo oscuro
val PurpleDark = Color(0xFF4527A0)
val BlueDark = Color(0xFF283593)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Colores principales de la aplicación
val PrimaryVariant = Color(0xFF0D4B7F)
val SecondaryVariant = Color(0xFFF29F05)
val SurfaceVariant = Color(0xFFE8F4F8)

// Colores específicos para temas
val LightBackground = Color(0xFFEAF1F8)
val DarkBackground = Color(0xFF101C2B)

// Colores para gradientes
val GradientMiddle = Color(0xFF3D7EB6)

// Colores para tarjetas y elementos visuales
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1A2C42)

// Colores para texto
val TextPrimary = Color(0xFF333333)
val TextSecondary = Color(0xFF757575)
val TextDisabled = Color(0xFFBDBDBD)

// Paleta de rojos
val Red100 = Color(0xFFFFCDD2)
val Red200 = Color(0xFFEF9A9A)
val Red300 = Color(0xFFE57373)
val Red500 = Color(0xFFF44336)
val Red700 = Color(0xFFD32F2F)
val Red900 = Color(0xFFB71C1C)

// Paleta de verdes
val Green100 = Color(0xFFC8E6C9)
val Green200 = Color(0xFFA5D6A7)
val Green300 = Color(0xFF81C784)
val Green500 = Color(0xFF4CAF50)
val Green700 = Color(0xFF388E3C)
val Green900 = Color(0xFF1B5E20)

// Paleta de azules
val Blue100 = Color(0xFFBBDEFB)
val Blue200 = Color(0xFF90CAF9)
val Blue300 = Color(0xFF64B5F6)
val Blue500 = Color(0xFF2196F3)
val Blue700 = Color(0xFF1976D2)
val Blue900 = Color(0xFF0D47A1)

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

/**
 * Devuelve un color representativo para cada estado de tarea
 */
fun colorEstadoTarea(estado: EstadoTarea): Color {
    return when (estado) {
        EstadoTarea.PENDIENTE -> Warning
        EstadoTarea.EN_PROGRESO -> Info
        EstadoTarea.COMPLETADA -> Success
        EstadoTarea.CANCELADA -> Error
        EstadoTarea.VENCIDA -> Error.copy(alpha = 0.7f)
    }
}