package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de Alumno para tests
 */
data class Alumno(
    val id: String = "",
    val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val fechaNacimiento: Timestamp = Timestamp.now(),
    val foto: String? = null,
    val claseId: String = "",
    val centroId: String = "",
    val familiaresIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val curso: String = "",
    val clase: String = ""
) 