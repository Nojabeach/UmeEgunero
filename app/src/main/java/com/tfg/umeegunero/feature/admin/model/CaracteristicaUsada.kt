package com.tfg.umeegunero.feature.admin.model

/**
 * Modelo de datos para las características usadas en el reporte de uso
 * 
 * @property nombre Nombre de la característica o funcionalidad
 * @property frecuencia Número de veces que ha sido utilizada
 * @property porcentaje Porcentaje de uso respecto al total
 */
data class CaracteristicaUsada(
    val nombre: String,
    val frecuencia: Int,
    val porcentaje: Float
) 