package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Estado de la UI para la pantalla de reporte de uso de la aplicación.
 *
 * Esta clase encapsula todos los datos necesarios para la representación del estado
 * de la interfaz de usuario en la pantalla de reportes de uso. Contiene información
 * sobre las características más utilizadas, datos de actividad de usuarios y estados
 * de generación de informes.
 *
 * @property isLoading Indica si se están cargando los datos desde el repositorio
 * @property error Mensaje de error en caso de que ocurra algún problema, o null si no hay errores
 * @property caracteristicasUsadas Lista de características con sus datos de frecuencia de uso
 * @property periodoSeleccionado Periodo de tiempo seleccionado para el análisis (ej. "Último mes")
 * @property usuariosActivos Número total de usuarios activos durante el periodo seleccionado
 * @property sesionesPromedio Promedio de sesiones por usuario durante el periodo seleccionado
 * @property tiempoPromedioSesion Duración media de cada sesión de usuario formateada (ej. "15 min")
 * @property isGeneratingReport Indica si se está generando un informe exportable
 * @property reportGenerated Indica si el informe ha sido generado correctamente
 * @property ultimaActualizacion Timestamp con la fecha y hora de la última actualización de datos
 * @property fechaActualizacion Fecha de última actualización formateada como String
 *
 * @see CaracteristicaUsada
 * @see com.tfg.umeegunero.feature.admin.viewmodel.ReporteUsoViewModel
 * @see com.tfg.umeegunero.feature.admin.screen.ReporteUsoScreen
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
    val reportGenerated: Boolean = false,
    val ultimaActualizacion: Timestamp? = null,
    val fechaActualizacion: String = "No disponible"
) 