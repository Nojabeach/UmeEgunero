package com.tfg.umeegunero.data.model

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Modelo de datos para eventos del calendario
 * @param id Identificador único del evento
 * @param titulo Título descriptivo del evento
 * @param descripcion Descripción detallada del evento
 * @param fecha Fecha y hora del evento
 * @param tipo Tipo de evento
 * @param creadorId ID del usuario que creó el evento
 * @param centroId ID del centro educativo al que pertenece el evento
 */
data class Evento(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: LocalDateTime,
    val tipo: TipoEvento,
    val creadorId: String,
    val centroId: String
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "tipo" to tipo.name,
            "descripcion" to descripcion,
            "fecha" to fecha.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            "creadoPor" to creadorId,
            "centroId" to centroId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String): Evento? {
            return try {
                Evento(
                    id = id,
                    titulo = map["titulo"] as String,
                    descripcion = map["descripcion"] as String,
                    fecha = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(map["fecha"] as Long),
                        ZoneId.systemDefault()
                    ),
                    tipo = TipoEvento.valueOf(map["tipo"] as String),
                    creadorId = map["creadoPor"] as String,
                    centroId = map["centroId"] as String
                )
            } catch (e: Exception) {
                null
            }
        }
    }
} 