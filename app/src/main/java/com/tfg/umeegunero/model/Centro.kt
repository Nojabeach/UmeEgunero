package com.tfg.umeegunero.model

/**
 * Modelo que representa un centro educativo.
 * 
 * @property id Identificador único del centro
 * @property nombre Nombre del centro
 * @property direccion Dirección completa del centro
 * @property telefono Teléfono de contacto del centro (opcional)
 * @property latitud Coordenada latitud para ubicar el centro en el mapa (opcional)
 * @property longitud Coordenada longitud para ubicar el centro en el mapa (opcional)
 */
data class Centro(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
) 