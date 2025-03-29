package com.tfg.umeegunero.data.model

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
    val informeGenerado: Boolean = false
) 