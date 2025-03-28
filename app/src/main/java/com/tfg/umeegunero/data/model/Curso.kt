package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo para representar un curso académico en el sistema
 * Contiene la información necesaria para la gestión de cursos en centros educativos
 */
data class Curso(
    @DocumentId val id: String = "",
    val centroId: String = "",
    val nombre: String = "", // Por ejemplo: "Infantil 3 años", "Primaria 1º"
    val descripcion: String = "",
    val edadMinima: Int = 0,
    val edadMaxima: Int = 0,
    val aniosNacimiento: List<Int> = emptyList(), // Lista de años de nacimiento de los alumnos que pertenecen a este curso
    val activo: Boolean = true,
    val anioAcademico: String = "" // Por ejemplo: "2023-2024"
) 