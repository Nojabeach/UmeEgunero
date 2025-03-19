package com.tfg.umeegunero.domain.model

data class Usuario(
    val id: String,
    val dni: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val rol: Rol
) 