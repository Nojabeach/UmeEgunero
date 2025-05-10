package com.tfg.umeegunero.data.model

import java.util.Date

/**
 * Modelo de estado para la pantalla de estadísticas
 */
data class EstadisticasUiState(
    val isLoading: Boolean = true,
    val error: String = "",
    val totalCentros: Int = 0,
    val totalUsuarios: Int = 0,
    val totalProfesores: Int = 0,
    val totalAlumnos: Int = 0,
    val totalFamiliares: Int = 0,
    val nuevosCentros: Int = 0,
    val nuevosProfesores: Int = 0,
    val nuevosAlumnos: Int = 0,
    val nuevosFamiliares: Int = 0,
    val nuevosRegistros: Int = 0,
    val informeGenerado: Boolean = false,
    val informeDescargado: Boolean = false,
    val informeContenido: String = "",
    val fechaActualizacion: String = "No disponible",
    val actividadesRecientes: List<ActividadReciente> = emptyList()
)

/**
 * Modelo para representar una actividad reciente en el sistema
 */
data class ActividadReciente(
    val id: String,
    val tipo: String,
    val descripcion: String,
    val fecha: Date,
    val usuarioId: String,
    val detalles: String
) 