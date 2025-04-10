package com.tfg.umeegunero.data.model

import java.util.Date

/**
 * Modelo de datos para eventos del calendario
 */
data class EventoCalendario(
    val titulo: String,
    val descripcion: String,
    val fechaInicio: Date,
    val fechaFin: Date,
    val ubicacion: String = "",
    val recordatorio: Long = 30 // minutos antes
) 