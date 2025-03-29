package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa una clase o grupo dentro de un curso académico en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para las clases o grupos educativos, que son
 * subdivisiones de un curso académico. Una clase agrupa a un conjunto de alumnos bajo
 * la tutela de uno o varios profesores, con un espacio físico (aula) y horario asignados.
 * 
 * Las clases se almacenan como documentos en la colección 'clases' en Firestore,
 * donde el ID del documento coincide con el campo [id] de esta clase. Cada clase
 * está vinculada a un curso específico ([cursoId]) y a un centro educativo ([centroId]).
 * 
 * Relaciones principales:
 * - Una clase pertenece a un único curso ([cursoId])
 * - Una clase está asociada a un único centro educativo ([centroId])
 * - Una clase tiene un profesor titular ([profesorTitularId]) y puede tener varios auxiliares
 * - Una clase agrupa a múltiples alumnos ([alumnosIds])
 * - Una clase se imparte en un espacio físico específico ([aula])
 * 
 * Esta entidad es gestionada principalmente por usuarios con roles ADMIN_CENTRO 
 * y es utilizada por profesores para organizar a sus alumnos y actividades diarias.
 * 
 * @property id Identificador único de la clase, anotado con @DocumentId para Firestore
 * @property cursoId Identificador del curso académico al que pertenece esta clase
 * @property centroId Identificador del centro educativo al que pertenece
 * @property nombre Nombre distintivo de la clase (ej. "A", "B", "Mañana", "Tarde")
 * @property profesorTitularId Identificador del profesor principal responsable de la clase
 * @property profesoresAuxiliaresIds Lista de identificadores de profesores de apoyo asignados
 * @property alumnosIds Lista de identificadores de alumnos matriculados en esta clase
 * @property capacidadMaxima Número máximo de alumnos permitidos en la clase
 * @property activo Indica si la clase está actualmente operativa en el sistema
 * @property horario Descripción del horario o referencia a un objeto Horario
 * @property aula Ubicación física dentro del centro donde se imparte la clase
 * 
 * @see Curso Entidad relacionada a la que pertenece esta clase
 * @see Centro Entidad relacionada que contiene esta clase
 * @see Usuario Entidad relacionada con los profesores asignados
 * @see Alumno Entidad relacionada de estudiantes pertenecientes a esta clase
 */
data class Clase(
    @DocumentId val id: String = "",
    val cursoId: String = "",
    val centroId: String = "",
    val nombre: String = "", // Por ejemplo: "A", "B", "Mañana", "Tarde"
    val profesorTitularId: String = "",
    val profesoresAuxiliaresIds: List<String> = emptyList(),
    val alumnosIds: List<String> = emptyList(),
    val capacidadMaxima: Int = 25,
    val activo: Boolean = true,
    val horario: String = "", // Descripción del horario o referencia a un objeto Horario
    val aula: String = "" // Ubicación física dentro del centro
) 