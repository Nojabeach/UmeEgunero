package com.tfg.umeegunero.data.model

/**
 * Clase para el formulario de creación de notificaciones
 */
data class NotificacionForm(
    val titulo: String = "",
    val mensaje: String = "",
    val tipoNotificacion: TipoNotificacion = TipoNotificacion.GENERAL,
    val tipoDestino: TipoDestino = TipoDestino.TODOS,
    val destinoId: String = "",
    val emisorId: String = "",
    val centroId: String = ""
) 