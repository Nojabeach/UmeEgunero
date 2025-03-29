package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Enumeración que define los posibles estados de asistencia de un alumno en el sistema UmeEgunero.
 * 
 * Esta enumeración proporciona valores estandarizados para representar si un alumno ha asistido
 * a clase, ha faltado, o ha llegado tarde. Estos estados se utilizan en los registros de asistencia
 * diarios que mantienen los profesores para cada grupo de alumnos.
 * 
 * Los valores de esta enumeración son utilizados como parte del modelo [RegistroAsistencia]
 * para llevar un control preciso de la presencia de cada alumno en las actividades académicas.
 * 
 * @property PRESENTE Indica que el alumno ha asistido a clase normalmente.
 * @property AUSENTE Indica que el alumno no ha asistido a clase.
 * @property RETRASADO Indica que el alumno ha llegado tarde a clase.
 * 
 * @see RegistroAsistencia Modelo que utiliza esta enumeración para el registro de asistencia
 */
enum class Asistencia {
    PRESENTE,
    AUSENTE,
    RETRASADO
}

/**
 * Modelo que representa un registro diario de asistencia para una clase en el sistema UmeEgunero.
 * 
 * Esta clase almacena información sobre la asistencia de todos los alumnos de una clase específica 
 * en una fecha determinada. El registro es creado por el profesor y facilita el seguimiento
 * de la regularidad con la que los alumnos asisten a las actividades académicas.
 * 
 * Los registros de asistencia se almacenan como documentos en la colección 'registrosAsistencia'
 * en Firestore, donde el ID del documento coincide con el campo [id] de esta clase.
 * 
 * Cada registro contiene un mapa ([estadosAsistencia]) que asocia el identificador (DNI) de cada 
 * alumno con su estado de asistencia para esa fecha, utilizando la enumeración [Asistencia].
 * 
 * Relaciones principales:
 * - Un registro pertenece a una única clase ([claseId])
 * - Un registro es creado por un profesor específico ([profesorId])
 * - Un registro contiene el estado de asistencia de múltiples alumnos ([estadosAsistencia])
 * 
 * Esta entidad es creada y gestionada por usuarios con rol PROFESOR para mantener
 * un control de la asistencia de sus alumnos, y puede ser consultada por administradores
 * y familiares para el seguimiento académico.
 * 
 * @property id Identificador único del registro, anotado con @DocumentId para Firestore
 * @property claseId Identificador de la clase a la que pertenece este registro
 * @property profesorId Identificador del profesor que ha creado el registro
 * @property fecha Fecha y hora en que se realizó el registro de asistencia
 * @property estadosAsistencia Mapa que asocia el DNI de cada alumno con su estado de asistencia
 * @property observaciones Notas adicionales sobre la asistencia del día (justificaciones, etc.)
 * 
 * @see Asistencia Enumeración que define los posibles estados de asistencia
 * @see Clase Entidad relacionada a la que pertenece este registro
 * @see Usuario Entidad relacionada con el profesor que crea el registro
 * @see Alumno Entidad relacionada con los estudiantes cuya asistencia se registra
 */
data class RegistroAsistencia(
    @DocumentId val id: String = "",
    val claseId: String = "",
    val profesorId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val estadosAsistencia: Map<String, Asistencia> = emptyMap(), // DNI del alumno -> Estado de asistencia
    val observaciones: String = ""
) 