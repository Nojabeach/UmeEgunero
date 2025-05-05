package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Representa un curso académico en la aplicación UmeEgunero.
 *
 * Un curso es un período de tiempo definido en el contexto educativo que agrupa
 * un conjunto de clases, actividades y estudiantes. Proporciona una estructura
 * para organizar el año o semestre académico.
 *
 * @property id Identificador único del curso
 * @property nombre Nombre descriptivo del curso (ej. "Curso 2023-2024")
 * @property centroId Identificador del centro educativo al que pertenece el curso
 * @property fechaInicio Fecha de inicio del curso
 * @property fechaFin Fecha de finalización del curso
 * @property activo Indica si el curso está actualmente en vigor
 * @property clases Lista de identificadores de clases asociadas al curso
 * @property profesores Lista de identificadores de profesores asignados al curso
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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