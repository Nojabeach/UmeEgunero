package com.tfg.umeegunero.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Formatea un timestamp en milisegundos a una fecha legible
 * @param timestampMs Timestamp en milisegundos
 * @param format Formato de fecha (opcional)
 * @return Fecha formateada como string
 */
fun formatDate(
    timestampMs: Long,
    format: String = "dd/MM/yyyy"
): String {
    val date = Date(timestampMs)
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    return dateFormat.format(date)
}

/**
 * Formatea un timestamp en milisegundos a una fecha con hora legible
 * @param timestampMs Timestamp en milisegundos
 * @return Fecha con hora formateada como string
 */
fun formatDateTime(
    timestampMs: Long
): String {
    return formatDate(timestampMs, "dd/MM/yyyy HH:mm")
}

/**
 * Formatea un timestamp en milisegundos a una fecha relativa
 * (hoy, ayer, hace X días, etc.)
 * @param timestampMs Timestamp en milisegundos
 * @return Fecha relativa como string
 */
fun formatRelativeDate(timestampMs: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestampMs }
    
    // Diferencia en días
    val diffDays = (now.timeInMillis - timestampMs) / (24 * 60 * 60 * 1000)
    
    return when {
        diffDays == 0L -> {
            // Hoy
            "Hoy"
        }
        diffDays == 1L -> {
            // Ayer
            "Ayer"
        }
        diffDays < 7L -> {
            // Hace X días
            "Hace $diffDays días"
        }
        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
            // Este año, mostrar solo día y mes
            formatDate(timestampMs, "d 'de' MMMM")
        }
        else -> {
            // Otro año, mostrar fecha completa
            formatDate(timestampMs, "d 'de' MMMM 'de' yyyy")
        }
    }
} 