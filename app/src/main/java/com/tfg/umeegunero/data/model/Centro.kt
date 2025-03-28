package com.tfg.umeegunero.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Modelo para representar un centro educativo en la aplicación
 */
data class Centro(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val direccion: Direccion = Direccion(),
    val contacto: Contacto = Contacto(),
    val adminIds: List<String> = emptyList(),
    val profesorIds: List<String> = emptyList(),
    val activo: Boolean = true,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val descripcion: String = ""
) 