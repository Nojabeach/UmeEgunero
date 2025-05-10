package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un mensaje de chat en la base de datos local
 */
@Entity(tableName = "chat_mensajes")
data class ChatMensajeEntity(
    @PrimaryKey
    val id: String,
    val emisorId: String,
    val receptorId: String,
    val timestamp: Long,
    val texto: String,
    val leido: Boolean = false,
    val fechaLeido: Long? = null,
    val conversacionId: String,
    val alumnoId: String? = null,
    val tipoAdjunto: String? = null,
    val urlAdjunto: String? = null,
    val interaccionEstado: String = "NONE",
    val estaTraducido: Boolean = false,
    val textoOriginal: String? = null,
    val sincronizado: Boolean = false,
    val destacado: Boolean = false
) 