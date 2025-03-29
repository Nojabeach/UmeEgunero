package com.tfg.umeegunero.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
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