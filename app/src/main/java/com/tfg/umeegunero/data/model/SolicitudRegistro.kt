package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa una solicitud de registro en el sistema
 */
data class SolicitudRegistro(
    @DocumentId val id: String = "",
    val usuarioId: String = "",
    val centroId: String = "",
    val tipoSolicitud: TipoUsuario = TipoUsuario.FAMILIAR,
    val alumnoIds: List<String> = emptyList(),
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val fechaSolicitud: Timestamp = Timestamp.now(),
    val fechaResolucion: Timestamp? = null,
    val resolutorId: String = "",
    val motivoRechazo: String = ""
) 