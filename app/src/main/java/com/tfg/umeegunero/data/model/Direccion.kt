package com.tfg.umeegunero.data.model

/**
 * Modelo que representa una dirección postal
 */
data class Direccion(
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = ""
) 