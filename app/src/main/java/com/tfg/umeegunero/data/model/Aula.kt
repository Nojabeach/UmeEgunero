package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un aula o espacio físico en un centro educativo del sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para las aulas o espacios físicos donde se imparten 
 * las clases. Un aula es un recurso del centro educativo que puede asignarse a diferentes cursos
 * o grupos según las necesidades organizativas del centro.
 * 
 * Las aulas se almacenan como documentos en la colección 'aulas' en Firestore,
 * donde el ID del documento coincide con el campo [id] de esta clase. Cada aula
 * está vinculada a un centro educativo específico a través del campo [centroId].
 * 
 * Relaciones principales:
 * - Un aula pertenece a un único centro educativo ([centroId])
 * - Un aula puede estar asociada a un curso específico ([curso])
 * - Un aula puede tener asignados múltiples profesores ([profesorIds])
 * - Un aula puede acoger a múltiples alumnos ([alumnoIds])
 * 
 * Esta entidad es principalmente gestionada por usuarios con roles ADMIN_CENTRO
 * para la organización física de espacios educativos.
 * 
 * @property id Identificador único del aula, anotado con @DocumentId para Firestore
 * @property centroId Identificador del centro educativo al que pertenece esta aula
 * @property nombre Nombre descriptivo del aula (ej. "Aula 101", "Laboratorio de Ciencias")
 * @property curso Curso al que está asignada prioritariamente (puede ser un identificador o nombre)
 * @property profesorIds Lista de identificadores de profesores que utilizan esta aula
 * @property alumnoIds Lista de identificadores de alumnos que asisten a esta aula
 * 
 * @see Centro Entidad relacionada que contiene esta aula
 * @see Clase Entidad relacionada que puede estar vinculada a esta aula
 * @see Usuario Entidad relacionada con los profesores asignados
 * @see Alumno Entidad relacionada de estudiantes asignados
 */
data class Aula(
    @DocumentId val id: String = "",
    val centroId: String = "",
    val nombre: String = "",
    val curso: String = "",
    val profesorIds: List<String> = emptyList(),
    val alumnoIds: List<String> = emptyList()
) 