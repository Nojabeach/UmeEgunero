package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo para representar una clase dentro de un curso académico
 * Contiene la información necesaria para la gestión de clases en centros educativos
 */
data class Clase(
    @DocumentId val id: String = "",
    val cursoId: String = "",
    val centroId: String = "",
    val nombre: String = "", // Por ejemplo: "A", "B", "Mañana", "Tarde"
    val profesorTitularId: String = "",
    val profesoresAuxiliaresIds: List<String> = emptyList(),
    val alumnosIds: List<String> = emptyList(),
    val capacidadMaxima: Int = 25,
    val activo: Boolean = true,
    val horario: String = "", // Descripción del horario o referencia a un objeto Horario
    val aula: String = "" // Ubicación física dentro del centro
) 