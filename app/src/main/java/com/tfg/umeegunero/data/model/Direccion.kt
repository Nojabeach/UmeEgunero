package com.tfg.umeegunero.data.model

/**
 * Modelo de datos para una dirección postal
 * 
 * Incluye toda la información necesaria para localizar una dirección física
 * y sus coordenadas geográficas (latitud y longitud).
 *
 * @param calle Nombre de la calle o vía
 * @param numero Número del portal o edificio
 * @param piso Piso y puerta (opcional)
 * @param codigoPostal Código postal
 * @param ciudad Ciudad o localidad
 * @param provincia Provincia o estado
 * @param pais País (por defecto "España")
 * @param latitud Coordenada de latitud para geolocalización
 * @param longitud Coordenada de longitud para geolocalización
 * 
 * @author Maitane (Estudiante 2º DAM)
 */
data class Direccion(
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val codigoPostal: String = "",
    val ciudad: String = "",
    val provincia: String = "",
    val pais: String = "España",
    val latitud: String = "",
    val longitud: String = ""
) {
    /**
     * Devuelve la dirección formateada como texto
     */
    override fun toString(): String {
        val calleNumero = if (numero.isNotEmpty()) "$calle, $numero" else calle
        val pisoText = if (piso.isNotEmpty()) ", $piso" else ""
        val cpCiudad = if (codigoPostal.isNotEmpty() && ciudad.isNotEmpty()) "$codigoPostal $ciudad" 
                       else codigoPostal + ciudad
        
        return "$calleNumero$pisoText\n$cpCiudad\n$provincia, $pais"
    }
    
    /**
     * Comprueba si la dirección está completa con los campos mínimos necesarios
     */
    fun estaCompleta(): Boolean {
        return calle.isNotEmpty() && 
               codigoPostal.isNotEmpty() && 
               ciudad.isNotEmpty() && 
               provincia.isNotEmpty()
    }
    
    /**
     * Comprueba si la geolocalización está disponible con datos válidos
     */
    fun tieneGeolocalizacion(): Boolean {
        return latitud.isNotEmpty() && longitud.isNotEmpty()
    }
} 