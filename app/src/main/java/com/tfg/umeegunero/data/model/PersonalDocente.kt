package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa a un miembro del personal docente (profesor) en el sistema.
 * 
 * Esta clase contiene la información específica de los profesores, extendiendo la información
 * básica que ya proporciona el modelo Usuario.
 *
 * @property id Identificador único del profesor, generalmente su DNI
 * @property especialidades Lista de especialidades o materias que imparte
 * @property clasesAsignadas Lista de identificadores de clases asignadas al profesor
 * @property tutorDe Lista de identificadores de clases donde el profesor es tutor
 * @property centroId Identificador del centro educativo al que pertenece
 * @property activo Estado de actividad del profesor en el sistema
 * @property horasSemanales Número de horas semanales de trabajo
 * @property fechaIncorporacion Fecha de incorporación al centro
 * @property experiencia Años de experiencia docente
 * @property observaciones Notas adicionales sobre el profesor
 */
data class PersonalDocente(
    @DocumentId val id: String = "",
    val especialidades: List<String> = emptyList(),
    val clasesAsignadas: List<String> = emptyList(),
    val tutorDe: List<String> = emptyList(),
    val centroId: String = "",
    val activo: Boolean = true,
    val horasSemanales: Int = 40,
    val fechaIncorporacion: Timestamp = Timestamp.now(),
    val experiencia: Int = 0,
    val observaciones: String = ""
) 