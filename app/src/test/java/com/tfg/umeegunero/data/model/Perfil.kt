package com.tfg.umeegunero.data.model

/**
 * Modelo de Perfil para tests
 */
data class Perfil(
    val id: String = "",
    val tipo: TipoUsuario = TipoUsuario.PROFESOR,
    val centroId: String = "",
    val activo: Boolean = true
) 