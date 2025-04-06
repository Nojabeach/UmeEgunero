package com.tfg.umeegunero.navigation

/**
 * Interfaz para los elementos de navegación
 */
interface NavItem {
    val route: String
}

/**
 * Detalle de día con eventos
 * @param fecha Fecha en formato ISO (yyyy-MM-dd)
 */
object DetalleDiaEvento : NavItem {
    override val route: String = "detalle_dia_evento/{fecha}"
    
    fun createRoute(fecha: String): String = "detalle_dia_evento/$fecha"
    
    val Fecha = "fecha"
}

/**
 * Pantalla de bandeja de entrada de mensajes
 */
object BandejaEntrada : NavItem {
    override val route: String = "bandeja_entrada"
}

/**
 * Pantalla para componer un nuevo mensaje
 * @param mensajeIdRespuesta ID del mensaje al que se responde (opcional)
 */
object ComponerMensaje : NavItem {
    override val route: String = "componer_mensaje?mensajeId={mensajeId}"
    
    fun createRoute(mensajeId: String? = null): String {
        return if (mensajeId != null) {
            "componer_mensaje?mensajeId=$mensajeId"
        } else {
            "componer_mensaje"
        }
    }
    
    val MensajeId = "mensajeId"
} 