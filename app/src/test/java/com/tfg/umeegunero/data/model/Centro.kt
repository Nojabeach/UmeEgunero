package com.tfg.umeegunero.data.model

/**
 * Modelo de Centro educativo para tests
 */
data class Centro(
    val id: String = "",
    val nombre: String = "",
    val codigo: String = "",
    val direccion: Map<String, String> = emptyMap(),
    val telefono: String = "",
    val email: String = "",
    val adminId: String = "",
    val logo: String? = null,
    val activo: Boolean = true
) 