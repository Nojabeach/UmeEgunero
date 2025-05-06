package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo de Usuario para tests
 */
data class Usuario(
    val uid: String = "",
    val email: String = "",
    val dni: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val fotoPerfil: String? = null,
    val tipoUsuario: TipoUsuario = TipoUsuario.PROFESOR,
    val centroId: String? = null,
    val fechaRegistro: Timestamp = Timestamp.now(),
    val activo: Boolean = true,
    val perfiles: List<Perfil> = emptyList()
) 