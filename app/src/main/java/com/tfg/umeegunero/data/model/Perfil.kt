package com.tfg.umeegunero.data.model

/**
 * Modelo que representa un perfil de usuario en el sistema
 */
data class Perfil(
    val tipo: TipoUsuario = TipoUsuario.FAMILIAR,
    val subtipo: SubtipoFamiliar? = null,
    val centroId: String = "",
    val verificado: Boolean = false,
    val alumnos: List<String> = emptyList() // Lista de DNIs de alumnos
) 