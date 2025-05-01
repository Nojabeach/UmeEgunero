package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo que representa una solicitud de vinculación entre un familiar y un alumno.
 * 
 * Este modelo se utiliza para almacenar y gestionar las solicitudes que hacen los familiares
 * para vincularse con nuevos alumnos en el sistema.
 * 
 * @property id Identificador único de la solicitud
 * @property familiarId ID del familiar que hace la solicitud
 * @property alumnoDni DNI del alumno que se quiere vincular
 * @property alumnoNombre Nombre del alumno (opcional, puede estar vacío)
 * @property centroId ID del centro educativo
 * @property fechaSolicitud Fecha en que se realizó la solicitud
 * @property estado Estado actual de la solicitud (Pendiente, Aprobada, Rechazada)
 */
data class SolicitudVinculacion(
    val id: String = "",
    val familiarId: String = "",
    val alumnoDni: String = "",
    val alumnoNombre: String? = null,
    val centroId: String = "",
    val fechaSolicitud: Timestamp = Timestamp.now(),
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE
) 