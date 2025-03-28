package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo para la información de siesta
 */
data class Siesta(
    val duracion: Int = 0,
    val observaciones: String = "",
    val inicio: Timestamp? = null,
    val fin: Timestamp? = null
) 