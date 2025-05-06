package com.tfg.umeegunero.data.model

/**
 * Modelo de Clase para tests
 */
data class Clase(
    val id: String = "",
    val nombre: String = "",
    val cursoId: String = "",
    val tutorId: String = "",
    val profesoresIds: List<String> = emptyList(),
    val capacidad: Int = 25,
    val activa: Boolean = true,
    val centroId: String = "",
    val curso: String = ""
) 