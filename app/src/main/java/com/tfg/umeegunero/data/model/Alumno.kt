package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.*

/**
 * Modelo que representa a un alumno en el sistema UmeEgunero.
 * 
 * Este modelo almacena toda la información relevante de un estudiante incluyendo datos
 * personales, académicos, médicos y relaciones con familiares y profesores. Los alumnos
 * son el eje central del sistema educativo y están vinculados a centros, aulas y clases.
 *
 * La información del alumno incluye datos relevantes para su gestión diaria, seguimiento
 * académico y atención de necesidades especiales o médicas.
 *
 * @property id Identificador único del alumno generado por el sistema.
 * @property dni Documento Nacional de Identidad del alumno. Marcado con @DocumentId para
 *              indicar que es el ID del documento en Firestore.
 * @property nombre Nombre completo del alumno.
 * @property apellidos Apellidos del alumno.
 * @property email Correo electrónico de contacto (generalmente de los padres para menores).
 * @property telefono Teléfono de contacto.
 * @property fechaNacimiento Fecha de nacimiento en formato string (YYYY-MM-DD).
 * @property centroId Identificador del centro educativo al que pertenece.
 * @property aulaId Identificador del aula específica a la que asiste.
 * @property curso Nombre del curso académico (por ejemplo, "Primero de Infantil").
 * @property clase Nombre de la clase específica (por ejemplo, "Clase A").
 * @property profesorId Identificador del profesor principal asignado al alumno.
 * @property profesorIds Lista de identificadores de todos los profesores asignados.
 * @property familiarIds Lista de identificadores de los familiares vinculados.
 * @property activo Estado de actividad del alumno en el sistema.
 * @property necesidadesEspeciales Descripción de necesidades educativas especiales.
 * @property alergias Lista de alergias conocidas (alimentos, medicamentos, etc.).
 * @property medicacion Lista de medicamentos que el alumno debe tomar regularmente.
 * @property observacionesMedicas Observaciones relevantes sobre la salud del alumno.
 * @property numeroSS Número de Seguridad Social del alumno (dato sensible).
 * @property condicionesMedicas Información detallada sobre condiciones médicas que requieren atención (dato sensible).
 * @property observaciones Observaciones generales sobre el comportamiento o situación del alumno.
 * @property familiares Lista de objetos [Familiar] con información de contactos familiares.
 * @property presente Indica si el alumno está presente/asistió en una fecha determinada.
 * @property nombreCompleto Nombre completo del alumno (nombre + apellidos).
 * @property asistenciaHoy Indica si el alumno está presente hoy.
 * @property ultimaAsistencia Fecha de la última asistencia del alumno.
 *
 * @see Familiar
 */
data class Alumno(
    val id: String = "",
    @DocumentId val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val centroId: String = "",
    val aulaId: String = "",
    val curso: String = "",
    val clase: String = "",
    val claseId: String = "",
    val profesorId: String = "",
    val profesorIds: List<String> = emptyList(),
    val familiarIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val necesidadesEspeciales: String = "",
    val alergias: List<String> = emptyList(),
    val medicacion: List<String> = emptyList(),
    val observacionesMedicas: String = "",
    val numeroSS: String = "",
    val condicionesMedicas: String = "",
    val observaciones: String = "",
    val familiares: List<Familiar> = emptyList(),
    val presente: Boolean = false,
    val asistenciaHoy: Boolean? = null,
    val ultimaAsistencia: Date? = null
) {
    // Propiedad computada para obtener el nombre completo
    val nombreCompleto: String
        get() = "$nombre $apellidos".trim()
}

/**
 * Modelo que representa la información básica de un familiar vinculado a un alumno.
 * 
 * Esta clase contiene información resumida de un familiar para uso en listados
 * o referencias rápidas. Para información completa del familiar, se debe consultar
 * el modelo [Usuario] correspondiente usando el ID.
 *
 * @property id Identificador único del familiar (normalmente su DNI).
 * @property nombre Nombre del familiar.
 * @property apellidos Apellidos del familiar.
 * @property parentesco Relación con el alumno (padre, madre, tutor, etc.).
 *
 * @see Alumno
 * @see Usuario
 */
data class Familiar(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val parentesco: String = ""
) 