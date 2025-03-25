package com.tfg.umeegunero.domain.model

/**
 * Modelo de datos que representa un curso en el sistema
 * @property id Identificador único del curso
 * @property nombre Nombre del curso
 * @property descripcion Descripción detallada del curso
 * @property centroId Identificador del centro al que pertenece
 * @property fechaCreacion Fecha en la que se creó el curso
 * @property activo Indica si el curso está activo o no
 */
data class Curso(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val centroId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true
) 