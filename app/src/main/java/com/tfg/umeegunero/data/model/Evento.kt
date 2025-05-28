package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.Date
import timber.log.Timber

/**
 * Modelo de datos para eventos del calendario
 * @param id Identificador único del evento
 * @param titulo Título descriptivo del evento
 * @param descripcion Descripción detallada del evento
 * @param fecha Fecha y hora del evento
 * @param tipo Tipo de evento
 * @param creadorId ID del usuario que creó el evento
 * @param centroId ID del centro educativo al que pertenece el evento
 * @param recordatorio Indica si el evento tiene un recordatorio configurado
 * @param tiempoRecordatorioMinutos Minutos antes del evento para mostrar el recordatorio
 * @param publico Indica si el evento es visible para todos los usuarios del centro
 * @param destinatarios Lista de IDs de usuarios destinatarios del evento (si no es público)
 */
data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val tipo: Any = TipoEvento.OTRO, // Puede ser TipoEvento o String
    val creadorId: String = "",
    val centroId: String = "",
    val recordatorio: Boolean = false,
    val tiempoRecordatorioMinutos: Int = 30,
    val publico: Boolean = true,
    val destinatarios: List<String> = emptyList(),
    val ubicacion: String = ""
) : Serializable {
    
    /**
     * Convierte el objeto a un Map para guardarlo en Firestore
     */
    fun toMap(): Map<String, Any> {
        val tipoString = when (tipo) {
            is TipoEvento -> tipo.name
            is String -> tipo
            else -> TipoEvento.OTRO.name
        }
        
        val map = mutableMapOf<String, Any>(
            "titulo" to titulo,
            "tipo" to tipoString,
            "descripcion" to descripcion,
            "fecha" to fecha,
            "creadorId" to creadorId,
            "centroId" to centroId,
            "recordatorio" to recordatorio,
            "tiempoRecordatorioMinutos" to tiempoRecordatorioMinutos,
            "publico" to publico
        )
        
        if (ubicacion.isNotEmpty()) {
            map["ubicacion"] = ubicacion
        }
        
        // Agregar log para verificar los destinatarios antes de incluirlos en el mapa
        Timber.d("DEBUG-EVENTO-MODEL: Destinatarios en objeto Evento: $destinatarios (tamaño: ${destinatarios.size})")
        
        if (destinatarios.isNotEmpty()) {
            map["destinatarios"] = ArrayList(destinatarios)
            Timber.d("DEBUG-EVENTO-MODEL: Destinatarios añadidos al mapa: ${map["destinatarios"]}")
        } else {
            Timber.d("DEBUG-EVENTO-MODEL: No hay destinatarios para añadir al mapa")
        }
        
        return map
    }
    
    /**
     * Obtiene el tipo de evento como TipoEvento
     */
    fun getTipoEvento(): TipoEvento {
        return when (tipo) {
            is TipoEvento -> tipo
            is String -> {
                try {
                    TipoEvento.valueOf(tipo)
                } catch (e: Exception) {
                    TipoEvento.OTRO
                }
            }
            else -> TipoEvento.OTRO
        }
    }

    companion object {
        /**
         * Crea un objeto Evento a partir de un Map de Firestore
         */
        fun fromMap(map: Map<String, Any>, id: String): Evento? {
            return try {
                val destinatariosData = map["destinatarios"]
                val destinatariosList = when (destinatariosData) {
                    is List<*> -> {
                        destinatariosData.filterIsInstance<String>()
                    }
                    is ArrayList<*> -> {
                        destinatariosData.filterIsInstance<String>()
                    }
                    else -> emptyList()
                }
                
                // Tratar de obtener el tipo de evento
                val tipoString = map["tipo"] as? String ?: TipoEvento.OTRO.name
                val tipo = try {
                    TipoEvento.valueOf(tipoString)
                } catch (e: Exception) {
                    tipoString // Si no se puede convertir, guardamos la cadena
                }
                
                Evento(
                    id = id,
                    titulo = map["titulo"] as? String ?: "",
                    descripcion = map["descripcion"] as? String ?: "",
                    fecha = map["fecha"] as? Timestamp ?: Timestamp.now(),
                    tipo = tipo,
                    creadorId = map["creadorId"] as? String ?: "",
                    centroId = map["centroId"] as? String ?: "",
                    recordatorio = map["recordatorio"] as? Boolean ?: false,
                    tiempoRecordatorioMinutos = (map["tiempoRecordatorioMinutos"] as? Number)?.toInt() ?: 30,
                    publico = map["publico"] as? Boolean ?: true,
                    destinatarios = destinatariosList,
                    ubicacion = map["ubicacion"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
} 