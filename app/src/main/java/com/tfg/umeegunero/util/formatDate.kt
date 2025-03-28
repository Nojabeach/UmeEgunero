package com.tfg.umeegunero.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formatea un timestamp en milisegundos a una fecha legible
 * @param timestamp Timestamp en milisegundos
 * @param pattern Patrón de formato (por defecto: dd/MM/yyyy)
 * @return Fecha formateada como cadena de texto
 */
fun formatDate(timestamp: Long, pattern: String = "dd/MM/yyyy"): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
} 