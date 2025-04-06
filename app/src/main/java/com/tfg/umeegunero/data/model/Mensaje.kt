package com.tfg.umeegunero.data.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable

/**
 * Modelo de datos para mensajes en formato de Firebase.
 * Usado para la comunicación directa con Firestore.
 */
data class Mensaje(
    val id: String = "",
    val emisorId: String = "",
    val receptorId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val texto: String = "",
    val leido: Boolean = false,
    val fechaLeido: Timestamp? = null,
    val conversacionId: String = "",
    val alumnoId: String? = null,
    val adjuntos: List<String>? = null,
    val tipoMensaje: String = "TEXTO",
    // Campos adicionales para la comunicación
    val remitente: String = "",
    val remitenteNombre: String = "",
    val destinatarioId: String = "",
    val destinatarioNombre: String = "",
    val asunto: String = "",
    val contenido: String = "",
    val fechaEnvio: Timestamp = Timestamp.now(),
    val leidos: List<String>? = null,
    val destacado: Boolean = false,
    val tipoDestinatario: String = "INDIVIDUAL",
    val respuestaA: String? = null
) : Serializable {
    
    /**
     * Convierte el mensaje a un mapa para Firestore
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "emisorId" to emisorId,
        "receptorId" to receptorId,
        "timestamp" to timestamp,
        "texto" to texto,
        "leido" to leido,
        "fechaLeido" to fechaLeido,
        "conversacionId" to conversacionId,
        "alumnoId" to alumnoId,
        "adjuntos" to adjuntos,
        "tipoMensaje" to tipoMensaje,
        "remitente" to remitente,
        "remitenteNombre" to remitenteNombre,
        "destinatarioId" to destinatarioId,
        "destinatarioNombre" to destinatarioNombre,
        "asunto" to asunto,
        "contenido" to contenido,
        "fechaEnvio" to fechaEnvio,
        "leidos" to leidos,
        "destacado" to destacado,
        "tipoDestinatario" to tipoDestinatario,
        "respuestaA" to respuestaA
    )
    
    companion object {
        private const val TAG = "Mensaje"
        
        /**
         * Crea un objeto Mensaje a partir de un DocumentSnapshot de Firestore
         */
        fun fromSnapshot(doc: DocumentSnapshot): Mensaje? {
            return try {
                val data = doc.data ?: return null
                
                val adjuntosData = data["adjuntos"]
                val adjuntosList = when (adjuntosData) {
                    is List<*> -> adjuntosData.filterIsInstance<String>()
                    else -> null
                }
                
                val leidosData = data["leidos"]
                val leidosList = when (leidosData) {
                    is List<*> -> leidosData.filterIsInstance<String>()
                    else -> null
                }
                
                Mensaje(
                    id = doc.id,
                    emisorId = data["emisorId"] as? String ?: "",
                    receptorId = data["receptorId"] as? String ?: "",
                    timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                    texto = data["texto"] as? String ?: "",
                    leido = data["leido"] as? Boolean ?: false,
                    fechaLeido = data["fechaLeido"] as? Timestamp,
                    conversacionId = data["conversacionId"] as? String ?: "",
                    alumnoId = data["alumnoId"] as? String,
                    adjuntos = adjuntosList,
                    tipoMensaje = data["tipoMensaje"] as? String ?: "TEXTO",
                    remitente = data["remitente"] as? String ?: "",
                    remitenteNombre = data["remitenteNombre"] as? String ?: "",
                    destinatarioId = data["destinatarioId"] as? String ?: "",
                    destinatarioNombre = data["destinatarioNombre"] as? String ?: "",
                    asunto = data["asunto"] as? String ?: "",
                    contenido = data["contenido"] as? String ?: "",
                    fechaEnvio = data["fechaEnvio"] as? Timestamp ?: Timestamp.now(),
                    leidos = leidosList,
                    destacado = data["destacado"] as? Boolean ?: false,
                    tipoDestinatario = data["tipoDestinatario"] as? String ?: "INDIVIDUAL",
                    respuestaA = data["respuestaA"] as? String
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error al convertir documento a Mensaje", e)
                null
            }
        }
    }
}

/**
 * Tipos de destinatario para un mensaje
 */
enum class TipoDestinatario {
    INDIVIDUAL,   // Mensaje a un único destinatario
    GRUPO,        // Mensaje a un grupo (ej: todos los profesores de un centro)
    CLASE,        // Mensaje a una clase (ej: todos los alumnos/familias de una clase)
    CENTRO        // Mensaje a todo el centro
} 