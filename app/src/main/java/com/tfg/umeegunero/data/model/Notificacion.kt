package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.tfg.umeegunero.data.model.TipoUsuario

/**
 * Modelo para las notificaciones del sistema
 */
data class Notificacion(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val leida: Boolean = false,
    val tipo: TipoNotificacion = TipoNotificacion.SISTEMA,
    val usuarioDestinatarioId: String = "", // ID del usuario destinatario (si es específica)
    val centroId: String = "", // ID del centro al que pertenece (si aplica)
    val remitente: String = "", // Nombre o identificador del remitente
    val remitenteId: String = "", // ID del remitente
    val prioridad: PrioridadNotificacion = PrioridadNotificacion.NORMAL,
    val accion: String = "", // Acción asociada (ruta de navegación, etc.)
    val tipoDestinatarios: List<TipoUsuario> = emptyList(), // Tipos de usuario destinatarios
    val gruposDestinatarios: List<String>? = null, // Grupos específicos destinatarios (cursos, etc.)
    val metadata: Map<String, String> = emptyMap(), // Datos adicionales para casos específicos
    val icono: String = "",
    val color: String = "",
    val destinatarioId: String = "",  // ID del destinatario específico
    val destinatarioTipo: String = "" // Tipo del destinatario (ALUMNO, PROFESOR, etc.)
)

/**
 * Nivel de prioridad de la notificación
 */
enum class PrioridadNotificacion {
    BAJA,
    NORMAL,
    ALTA,
    URGENTE
} 