package com.tfg.umeegunero.data.model

/**
 * Modelo para la información de comidas
 */
data class Comida(
    val consumoPrimero: String? = null,
    val descripcionPrimero: String? = null,
    val consumoSegundo: String? = null,
    val descripcionSegundo: String? = null,
    val consumoPostre: String? = null,
    val descripcionPostre: String? = null,
    val observaciones: String? = null
)

/**
 * Modelo para representar un plato de comida y su nivel de consumo
 */
data class Plato(
    val descripcion: String = "",
    val nivelConsumo: NivelConsumo = NivelConsumo.BIEN,
    val consumo: NivelConsumo = NivelConsumo.BIEN
)

/**
 * Modelo para representar las comidas del día
 */
data class Comidas(
    val primerPlato: Plato = Plato(),
    val segundoPlato: Plato = Plato(),
    val postre: Plato = Plato()
) 