package com.tfg.umeegunero.data.model

data class Ciudad(
    val nombre: String,
    val codigoPostal: String,
    val provincia: String = "",
    val codigoProvincia: String = ""
)