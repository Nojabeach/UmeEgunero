package com.tfg.umeegunero.data.model

/**
 * Modelo para representar ciudades en el sistema
 */
data class Ciudad(
    val nombre: String,
    val codigoPostal: String,
    val provincia: String = "",
    val codigoProvincia: String = ""
)

/**
 * Modelo para representar los datos de códigos postales obtenidos del dataset local
 */
data class CodigoPostalData(
    val codigoPostal: String,
    val municipio: String,
    val provincia: String,
    val codigoProvincia: String
)

/**
 * Función de extensión para convertir CodigoPostalData a Ciudad
 */
fun CodigoPostalData.toCiudad(): Ciudad {
    return Ciudad(
        nombre = this.municipio,
        codigoPostal = this.codigoPostal,
        provincia = this.provincia,
        codigoProvincia = this.codigoProvincia
    )
} 