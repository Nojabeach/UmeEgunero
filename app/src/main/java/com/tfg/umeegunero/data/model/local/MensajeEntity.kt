package com.tfg.umeegunero.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un mensaje en el sistema de chat
 */
@Entity(tableName = "mensajes")
data class MensajeEntity(
    @PrimaryKey
    val id: String,
    val emisorId: String,
    val receptorId: String,
    val timestamp: Long,
    val texto: String,
    val leido: Boolean,
    val fechaLeido: Long? = null,
    val conversacionId: String,
    val alumnoId: String? = null,
    val tipoAdjunto: String? = null,
    val adjuntos: List<String> = emptyList()
) 