package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo de datos que representa un curso académico en un centro educativo.
 * 
 * Esta clase define la estructura de datos para los cursos educativos, que son
 * elementos organizativos fundamentales dentro de un centro educativo. Un curso
 * representa un nivel académico específico (por ejemplo, "Infantil 3 años" o 
 * "Primaria 1º") y agrupa a los alumnos según su edad y etapa educativa.
 * 
 * Los cursos se almacenan como documentos en la colección 'cursos' en Firestore,
 * donde el ID del documento coincide con el campo [id] de esta clase. Cada curso
 * está vinculado a un centro educativo específico a través del campo [centroId].
 * 
 * Relaciones principales:
 * - Un curso pertenece a un único centro educativo ([centroId])
 * - Un curso puede contener múltiples clases o grupos (referenciados desde la colección 'clases')
 * - Un curso tiene criterios de edad para sus alumnos ([edadMinima], [edadMaxima], [aniosNacimiento])
 * - Un curso está asociado a un año académico específico ([anioAcademico])
 * 
 * Esta entidad es gestionada principalmente por usuarios con roles ADMIN_CENTRO 
 * y es consultada por profesores para la organización de sus clases y alumnos.
 * 
 * @property id Identificador único del curso, anotado con @DocumentId para Firestore
 * @property centroId Identificador del centro educativo al que pertenece este curso
 * @property nombre Nombre descriptivo del curso (ej. "Infantil 3 años", "Primaria 1º")
 * @property descripcion Información adicional sobre el curso, contenidos, enfoque, etc.
 * @property edadMinima Edad mínima recomendada/requerida para los alumnos (en años)
 * @property edadMaxima Edad máxima recomendada/requerida para los alumnos (en años)
 * @property aniosNacimiento Lista de años de nacimiento de los alumnos que pertenecen a este curso
 * @property activo Indica si el curso está actualmente operativo en el sistema
 * @property anioAcademico Período académico al que corresponde el curso (ej. "2023-2024")
 * @property fechaCreacion Fecha de creación del curso
 * @property clases Lista de IDs de clases asociadas al curso
 * @property numAlumnos Número de alumnos matriculados en el curso
 * @property numProfesores Número de profesores asignados al curso
 * 
 * @see Centro Entidad relacionada que contiene el curso
 * @see Clase Entidad relacionada que representa los grupos dentro del curso
 * @see Alumno Entidad relacionada de estudiantes asignados al curso
 */
data class Curso(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val anioAcademico: String = "",
    val descripcion: String = "",
    val edadMinima: Int = 0,
    val edadMaxima: Int = 0,
    val centroId: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val activo: Boolean = true,
    val clases: List<String> = emptyList(),
    val numAlumnos: Int = 0,
    val numProfesores: Int = 0
) 