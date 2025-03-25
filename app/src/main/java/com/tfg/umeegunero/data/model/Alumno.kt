package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un alumno en el sistema
 * Este modelo contiene toda la información necesaria para la gestión de alumnos
 */
data class Alumno(
    val id: String = "",
    @DocumentId val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = "",
    val centroId: String = "",
    val aulaId: String = "",
    val curso: String = "",
    val clase: String = "",
    val profesorIds: List<String> = emptyList(),
    val familiarIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val necesidadesEspeciales: String = "",
    val alergias: List<String> = emptyList(),
    val medicacion: List<String> = emptyList(),
    val observacionesMedicas: String = "",
    val observaciones: String = "",
    val familiares: List<Familiar> = emptyList()
)

/**
 * Modelo que representa la información básica de un familiar vinculado a un alumno
 */
data class Familiar(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val parentesco: String = ""
) 