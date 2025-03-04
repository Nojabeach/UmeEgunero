package com.tfg.umeegunero.data.model

import com.google.gson.annotations.SerializedName

data class CorreosResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<PostalCodeData>
)

data class PostalCodeData(
    @SerializedName("postalCode")
    val codigoPostal: String,
    @SerializedName("locality")
    val localidad: String,
    @SerializedName("province")
    val provincia: String,
    @SerializedName("provinceCode")
    val codigoProvincia: String
)

// Función de extensión para convertir PostalCodeData a Ciudad
fun PostalCodeData.toCiudad(): Ciudad {
    return Ciudad(
        nombre = this.localidad,
        codigoPostal = this.codigoPostal,
        provincia = this.provincia,
        codigoProvincia = this.codigoProvincia
    )
} 