package com.tfg.umeegunero.data.model

/**
 * Clase que representa una característica o funcionalidad de la aplicación
 * y sus estadísticas de uso
 */
data class CaracteristicaUsada(
    val nombre: String,
    val usos: Int,
    val porcentaje: Float
) 