package com.tfg.umeegunero.data.model

/**
 * Modelo para la información de necesidades fisiológicas
 */
data class CacaControl(
    val tipo1: Boolean? = null,
    val tipo2: Boolean? = null,
    val tipo3: Boolean? = null,
    val hora: String? = null,
    val cantidad: String? = null,
    val tipo: String? = null,
    val descripcion: String? = null,
    val caca: Boolean = false,
    val pipi: Boolean = false,
    val observaciones: String = ""
)

/**
 * Alias para compatibilidad con código existente
 */
typealias NecesidadesFisiologicas = CacaControl 