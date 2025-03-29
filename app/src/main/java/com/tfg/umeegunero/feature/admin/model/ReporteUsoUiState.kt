package com.tfg.umeegunero.feature.admin.model

import com.tfg.umeegunero.feature.admin.model.CaracteristicaUsada

/**
 * Estado de la UI para la pantalla de reporte de uso
 *
 * @property isLoading Estado de carga
 * @property error Mensaje de error si existe
 * @property caracteristicasUsadas Lista de características usadas con su frecuencia
 * @property periodoSeleccionado Periodo de tiempo seleccionado para el reporte
 * @property usuariosActivos Número de usuarios activos en el periodo
 * @property sesionesPromedio Promedio de sesiones por usuario
 * @property tiempoPromedioSesion Tiempo promedio de cada sesión
 * @property isGeneratingReport Indica si se está generando un reporte
 * @property reportGenerated Indica si el reporte fue generado correctamente
 */
data class ReporteUsoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val caracteristicasUsadas: List<CaracteristicaUsada> = emptyList(),
    val periodoSeleccionado: String = "Último mes",
    val usuariosActivos: Int = 0,
    val sesionesPromedio: Double = 0.0,
    val tiempoPromedioSesion: String = "0 min",
    val isGeneratingReport: Boolean = false,
    val reportGenerated: Boolean = false
) 