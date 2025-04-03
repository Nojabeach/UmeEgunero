package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tfg.umeegunero.data.model.InteractionStatus
import com.tfg.umeegunero.util.Converters

/**
 * Entidad que representa un mensaje en el sistema de chat.
 * Almacena toda la información relacionada con los mensajes entre usuarios.
 */
@Entity(
    tableName = "chat_mensajes",
    indices = [
        Index("emisorId"),
        Index("receptorId"),
        Index("conversacionId")
    ]
)
@TypeConverters(Converters::class)
data class ChatMensajeEntity(
    @PrimaryKey
    val id: String,
    val emisorId: String,
    val receptorId: String,
    val timestamp: Long,
    val texto: String,
    val leido: Boolean,
    val fechaLeido: Long? = null,
    val alumnoId: String? = null,
    val conversacionId: String,
    val tipoAdjunto: String? = null,
    val urlAdjunto: String? = null,
    val interaccionEstado: String = InteractionStatus.NONE.name,
    val estaTraducido: Boolean = false,
    val textoOriginal: String? = null,
    // Campos adicionales para la sincronización
    val sincronizado: Boolean = false,
    val ultimaActualizacion: Long = System.currentTimeMillis()
) 