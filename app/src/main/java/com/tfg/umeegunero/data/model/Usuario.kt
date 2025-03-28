package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo principal que representa a un usuario en el sistema
 */
data class Usuario(
    val dni: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val fechaRegistro: Timestamp = Timestamp.now(),
    val ultimoAcceso: Timestamp? = null,
    val activo: Boolean = true,
    val perfiles: List<Perfil> = emptyList(),
    val direccion: Direccion? = null,
    val preferencias: Preferencias = Preferencias()
) {
    @field:DocumentId
    var documentId: String = dni
} 