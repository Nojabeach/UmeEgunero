package com.tfg.umeegunero.data.model

/**
 * Modelo que representa la información de un archivo
 */
data class InfoArchivo(
    val nombre: String,
    val tamaño: Long, // en bytes
    val tipo: String?, // MIME type
    val fechaCreacion: Long, // timestamp en milisegundos
    val fechaModificacion: Long, // timestamp en milisegundos
    val metadatos: Map<String, String> = emptyMap()
) 