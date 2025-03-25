package com.tfg.umeegunero.domain.model

/**
 * Modelo de datos que representa una clase en el sistema
 * @property id Identificador único de la clase
 * @property nombre Nombre de la clase
 * @property descripcion Descripción detallada de la clase
 * @property cursoId Identificador del curso al que pertenece
 * @property profesorId Identificador del profesor asignado
 * @property fechaCreacion Fecha en la que se creó la clase
 * @property activo Indica si la clase está activa o no
 */
data class Clase(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cursoId: String = "",
    val profesorId: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val activo: Boolean = true
) 