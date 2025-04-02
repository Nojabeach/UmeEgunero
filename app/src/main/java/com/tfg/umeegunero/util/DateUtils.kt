package com.tfg.umeegunero.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formatea un objeto Timestamp de Firestore a un string legible.
 * 
 * @param timestamp El timestamp de Firestore a formatear
 * @param pattern El patrón de formato de fecha (opcional)
 * @return La fecha formateada como string
 */
fun formatDate(timestamp: Timestamp?, pattern: String = "dd/MM/yyyy HH:mm"): String {
    if (timestamp == null) return "N/A"
    
    val formatter = SimpleDateFormat(pattern, Locale("es", "ES"))
    return formatter.format(timestamp.toDate())
}

/**
 * Formatea un objeto Date a un string legible.
 * 
 * @param date La fecha a formatear
 * @param pattern El patrón de formato de fecha (opcional)
 * @return La fecha formateada como string
 */
fun formatDate(date: Date?, pattern: String = "dd/MM/yyyy HH:mm"): String {
    if (date == null) return "N/A"
    
    val formatter = SimpleDateFormat(pattern, Locale("es", "ES"))
    return formatter.format(date)
}

/**
 * Formatea un timestamp en milisegundos a un string legible.
 * 
 * @param timestamp El timestamp en milisegundos a formatear
 * @param pattern El patrón de formato de fecha (opcional)
 * @return La fecha formateada como string
 */
fun formatDate(timestamp: Long?, pattern: String = "dd/MM/yyyy HH:mm"): String {
    if (timestamp == null) return "N/A"
    
    val formatter = SimpleDateFormat(pattern, Locale("es", "ES"))
    return formatter.format(Date(timestamp))
}

/**
 * Formatea un timestamp en milisegundos a una fecha relativa
 * (hoy, ayer, hace X días, etc.)
 * 
 * @param timestamp El timestamp en milisegundos a formatear
 * @return Fecha relativa como string
 */
fun formatRelativeDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }
    
    return when {
        // Hoy: mostrar hora
        now.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("HH:mm", Locale("es", "ES")).format(date)
        }
        // Ayer: mostrar "Ayer"
        now.get(Calendar.DAY_OF_YEAR) - messageDate.get(Calendar.DAY_OF_YEAR) == 1 &&
                now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            "Ayer"
        }
        // Esta semana: mostrar día de la semana
        now.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            SimpleDateFormat("EEEE", Locale("es", "ES")).format(date).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }
        now.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) -> {
            // Este año, mostrar solo día y mes
            formatDate(timestamp, "d 'de' MMMM")
        }
        else -> {
            // Otro año, mostrar fecha completa
            formatDate(timestamp, "d 'de' MMMM 'de' yyyy")
        }
    }
}

/**
 * Formatea un Timestamp de Firestore a una fecha relativa
 * 
 * @param timestamp El timestamp de Firestore a formatear
 * @return Fecha relativa como string
 */
fun formatRelativeDate(timestamp: Timestamp): String {
    return formatRelativeDate(timestamp.toDate().time)
} 