package com.tfg.umeegunero.feature.common.academico.model

/**
 * Modelo que representa una clase en un curso académico
 */
data class Clase(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cursoId: String = "",
    val profesorId: String = ""
) 