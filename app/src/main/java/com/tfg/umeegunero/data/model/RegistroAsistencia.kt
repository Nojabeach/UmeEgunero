package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un registro de asistencia para una clase en una fecha específica.
 * 
 * @property id Identificador único del registro
 * @property claseId ID de la clase a la que pertenece el registro
 * @property profesorId ID del profesor que registró la asistencia
 * @property fecha Fecha y hora del registro
 * @property estadosAsistencia Mapa que relaciona el ID de cada alumno con su estado de asistencia
 * @property observaciones Observaciones adicionales sobre la asistencia
 */
data class RegistroAsistencia(
    @DocumentId val id: String = "",
    val claseId: String = "",
    val profesorId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val estadosAsistencia: Map<String, EstadoAsistencia> = emptyMap(),
    val observaciones: String = ""
) 