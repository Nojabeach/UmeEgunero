package com.tfg.umeegunero.data.model

/**
 * Modelo que representa las preferencias de un usuario en el sistema
 */
data class Preferencias(
    val idiomaApp: String = "es",
    val notificaciones: Notificaciones = Notificaciones(),
    val tema: TemaPref = TemaPref.SYSTEM
)

/**
 * Modelo que representa las preferencias de notificaciones de un usuario
 */
data class Notificaciones(
    val push: Boolean = true,
    val email: Boolean = true
) 