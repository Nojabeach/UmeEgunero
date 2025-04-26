package com.tfg.umeegunero.data.model

/**
 * Modelo que representa un elemento del carrusel de onboarding.
 *
 * @property icon Icono que se mostrará en el carrusel (puede ser ImageVector o Painter)
 * @property title Título del elemento del carrusel
 * @property description Descripción principal del elemento
 * @property infoDetail Información adicional opcional
 */
data class CarouselItem(
    val icon: Any,
    val title: String,
    val description: String,
    val infoDetail: String = ""
) 