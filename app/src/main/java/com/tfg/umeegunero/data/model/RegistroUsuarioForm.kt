package com.tfg.umeegunero.data.model

/**
 * Modelo para formulario de registro de nuevo usuario
 */
data class RegistroUsuarioForm(
    val dni: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val subtipo: SubtipoFamiliar = SubtipoFamiliar.PADRE,
    val direccion: Direccion = Direccion(),
    val alumnosDni: List<String> = emptyList(),
    // Datos del centro a seleccionar
    val centroId: String = ""
) 