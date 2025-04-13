package com.tfg.umeegunero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Define los items de navegación de la aplicación.
 * Cada objeto implementa esta interfaz con información de la ruta, título e icono.
 * 
 * Para rutas con parámetros, se usa el formato: "ruta_base/{parametro}"
 * Ejemplo: "detalle_clase/{claseId}"
 */
interface NavItem {
    /**
     * Ruta de navegación que incluye los parámetros necesarios.
     * Puede contener parámetros de ruta con formato {parametro} o
     * parámetros de consulta con formato ?parametro={parametro}
     */
    val route: String
    
    /**
     * Conjunto de argumentos que requiere esta ruta (opcional).
     * Cada implementación puede proporcionar sus propios argumentos específicos.
     */
    val arguments: List<String>
        get() = emptyList()
    
    val title: String
    val icon: ImageVector
}

/**
 * Destino de navegación para la pantalla de detalle de un día con eventos.
 * 
 * @property Fecha Constante que define el nombre del parámetro de fecha.
 */
object DetalleDiaEvento : NavItem {
    // Constante para el nombre del parámetro
    const val FECHA = "fecha"
    
    // Definición de la ruta con parámetro de ruta
    override val route: String = "detalle_dia_evento/{$FECHA}"
    
    // Lista de argumentos requeridos
    override val arguments: List<String> = listOf(FECHA)
    
    /**
     * Crea la ruta de navegación con la fecha especificada.
     * 
     * @param fecha Fecha en formato ISO (yyyy-MM-dd) para la que se quieren ver los eventos
     * @return Ruta completa para la navegación
     */
    fun createRoute(fecha: String): String = "detalle_dia_evento/$fecha"
    
    override val title: String = "Detalle del Día"
    override val icon: ImageVector = Icons.Default.DateRange
}

/**
 * Destino de navegación para la pantalla de bandeja de entrada de mensajes.
 */
object BandejaEntrada : NavItem {
    override val route: String = "bandeja_entrada"
    override val title: String = "Bandeja de Entrada"
    override val icon: ImageVector = Icons.Default.Inbox
}

/**
 * Destino de navegación para la pantalla de composición de mensajes.
 * Permite crear un nuevo mensaje o responder a uno existente.
 * 
 * @property MENSAJE_ID Constante que define el nombre del parámetro de ID del mensaje.
 */
object ComponerMensaje : NavItem {
    // Constante para el nombre del parámetro
    const val MENSAJE_ID = "mensajeId"
    
    // Definición de la ruta con parámetro de consulta opcional
    override val route: String = "componer_mensaje?$MENSAJE_ID={$MENSAJE_ID}"
    
    /**
     * Crea la ruta de navegación para componer un mensaje.
     * 
     * @param mensajeId ID del mensaje al que se responde (opcional)
     * @return Ruta completa para la navegación
     */
    fun createRoute(mensajeId: String? = null): String {
        return if (mensajeId != null) {
            "componer_mensaje?$MENSAJE_ID=$mensajeId"
        } else {
            "componer_mensaje"
        }
    }
    
    override val title: String = "Componer Mensaje"
    override val icon: ImageVector = Icons.Default.Email
}

/**
 * Navegación a la pantalla que muestra el detalle de una clase
 * @param claseId Identificador único de la clase
 */
object DetalleClase : NavItem {
    const val ClaseId = "claseId"
    override val route = "detalle_clase/{$ClaseId}"
    override val title = "Detalle de Clase"
    override val icon: ImageVector = Icons.Default.Description

    /**
     * Crea la ruta de navegación con el ID de la clase
     * @param claseId Identificador único de la clase
     * @return Ruta completa para la navegación
     */
    fun createRoute(claseId: String): String = "detalle_clase/$claseId"
} 