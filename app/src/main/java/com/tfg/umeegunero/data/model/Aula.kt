package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un aula en un centro educativo
 */
data class Aula(
    @DocumentId val id: String = "",
    val centroId: String = "",
    val nombre: String = "",
    val curso: String = "",
    val profesorIds: List<String> = emptyList(),
    val alumnoIds: List<String> = emptyList()
) 