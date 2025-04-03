package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa una conversación entre dos usuarios.
 * Almacena metadata sobre el chat, como los participantes y el último mensaje.
 */
@Entity(
    tableName = "conversaciones",
    indices = [
        Index("participante1Id"),
        Index("participante2Id"),
        Index(value = ["participante1Id", "participante2Id"], unique = true)
    ]
)
data class ConversacionEntity(
    @PrimaryKey
    val id: String,
    val participante1Id: String,
    val participante2Id: String,
    val nombreParticipante1: String,
    val nombreParticipante2: String,
    val ultimoMensajeId: String? = null,
    val ultimoMensajeTexto: String? = null,
    val ultimoMensajeTimestamp: Long? = null,
    val ultimoMensajeEmisorId: String? = null,
    val tieneNoLeidos: Boolean = false,
    val cantidadNoLeidos: Int = 0,
    val alumnoId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false
) 