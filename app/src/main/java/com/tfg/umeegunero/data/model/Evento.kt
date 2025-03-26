package com.tfg.umeegunero.data.model

import java.time.LocalDateTime
import java.time.ZoneId

data class Evento(
    val id: String = "",
    val tipo: TipoEvento,
    val descripcion: String,
    val fecha: LocalDateTime,
    val creadoPor: String = "",
    val centroId: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "tipo" to tipo.name,
            "descripcion" to descripcion,
            "fecha" to fecha.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            "creadoPor" to creadoPor,
            "centroId" to centroId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, id: String): Evento? {
            return try {
                Evento(
                    id = id,
                    tipo = TipoEvento.valueOf(map["tipo"] as String),
                    descripcion = map["descripcion"] as String,
                    fecha = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(map["fecha"] as Long),
                        ZoneId.systemDefault()
                    ),
                    creadoPor = map["creadoPor"] as String,
                    centroId = map["centroId"] as String
                )
            } catch (e: Exception) {
                null
            }
        }
    }
} 