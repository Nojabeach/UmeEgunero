package com.tfg.umeegunero.data.model

/**
 * Información resumida de una conversación
 */
data class ConversacionInfo(
    val id: String = "",
    val participanteId: String = "",
    val alumnoId: String? = null,
    val mensajesNoLeidos: Int = 0
) 