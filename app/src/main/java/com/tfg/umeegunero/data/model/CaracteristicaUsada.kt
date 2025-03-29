package com.tfg.umeegunero.data.model

/**
 * Modelo de datos para las características usadas en el reporte de uso de la aplicación.
 * 
 * Este modelo se utiliza para representar estadísticas de uso de las diferentes funcionalidades
 * de la aplicación UmeEgunero. Es especialmente útil para seguimiento de características
 * más utilizadas en el módulo de reportes de uso por parte de los administradores.
 * 
 * @property nombre Nombre identificativo de la característica o funcionalidad
 * @property frecuencia Número de veces que ha sido utilizada durante el período analizado
 * @property porcentaje Porcentaje de uso respecto al total de interacciones (valor entre 0 y 100)
 * 
 * @see com.tfg.umeegunero.data.model.ReporteUsoUiState Estado UI que contiene colecciones de este modelo
 * @see com.tfg.umeegunero.feature.admin.viewmodel.ReporteUsoViewModel ViewModel que gestiona estas entidades
 */
data class CaracteristicaUsada(
    val nombre: String,
    val frecuencia: Int,
    val porcentaje: Float
) 