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
 * @property alumnoId ID del alumno que se quiere vincular
 * @property alumnoDni DNI del alumno que se quiere vincular
 * @property alumnoNombre Nombre del alumno
 * @property nombreFamiliar Nombre del familiar que hace la solicitud
 * @property tipoRelacion Tipo de relación entre el familiar y el alumno
 * @property centroId ID del centro educativo
 * @property fechaSolicitud Fecha en que se realizó la solicitud
 * @property estado Estado actual de la solicitud (Pendiente, Aprobada, Rechazada)
 * @property adminId ID del administrador que procesó la solicitud
 * @property nombreAdmin Nombre del administrador que procesó la solicitud
 * @property fechaProcesamiento Fecha en que se procesó la solicitud
 * @property observaciones Observaciones adicionales sobre la solicitud
 */
data class SolicitudVinculacion(
    val id: String = "",
    val familiarId: String = "",
    val alumnoId: String = "",
    val alumnoDni: String = "",
    val alumnoNombre: String = "",
    val nombreFamiliar: String = "",
    val tipoRelacion: String = "",
    val centroId: String = "",
    val fechaSolicitud: Timestamp = Timestamp.now(),
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val adminId: String = "",
    val nombreAdmin: String = "",
    val fechaProcesamiento: Timestamp? = null,
    val observaciones: String = ""
) 