package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una conversación en la base de datos local
 */
@Entity(tableName = "conversaciones")
data class ConversacionEntity(
    @PrimaryKey
    val id: String,
    val participante1Id: String,
    val participante2Id: String,
    val nombreParticipante1: String,
    val nombreParticipante2: String,
    val ultimoMensaje: String? = null,
    val ultimoMensajeTimestamp: Long = 0,
    val noLeidosParticipante1: Int = 0,
    val noLeidosParticipante2: Int = 0,
    val alumnoId: String? = null,
    val activa: Boolean = true,
    val sincronizada: Boolean = false
) 