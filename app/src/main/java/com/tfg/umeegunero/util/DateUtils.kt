package com.tfg.umeegunero.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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

/**
 * Convierte un Timestamp de Firebase a un objeto Date
 * 
 * @param timestamp El timestamp de Firebase a convertir
 * @return Objeto Date o null si el timestamp es null
 */
fun timestampToDate(timestamp: Timestamp?): Date? {
    return timestamp?.toDate()
}

/**
 * Convierte una Date a un Timestamp de Firebase
 * 
 * @param date La fecha a convertir
 * @return Objeto Timestamp de Firebase o null si la fecha es null
 */
fun dateToTimestamp(date: Date?): Timestamp? {
    return date?.let { Timestamp(it) }
}

/**
 * Convierte un Timestamp de Firebase a un objeto LocalDateTime
 * 
 * @return LocalDateTime del timestamp actual
 */
fun Timestamp.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(this.seconds * 1000 + this.nanoseconds / 1000000),
        ZoneId.systemDefault()
    )
}

/**
 * Convierte un LocalDateTime a un Timestamp de Firebase
 * 
 * @return Timestamp generado a partir del LocalDateTime actual
 */
fun LocalDateTime.toTimestamp(): Timestamp {
    val instant = this.atZone(ZoneId.systemDefault()).toInstant()
    return Timestamp(instant.epochSecond, instant.nano)
}

/**
 * Convierte un Timestamp de Firebase a un objeto LocalDate
 * 
 * @return LocalDate del timestamp actual
 */
fun Timestamp.toLocalDate(): LocalDate {
    return this.toLocalDateTime().toLocalDate()
}

/**
 * Convierte un Timestamp de Firebase a un objeto LocalTime
 * 
 * @return LocalTime del timestamp actual
 */
fun Timestamp.toLocalTime(): LocalTime {
    return this.toLocalDateTime().toLocalTime()
}

/**
 * Convierte un LocalDate a un Timestamp de Firebase
 * Establece la hora a 00:00:00
 * 
 * @return Timestamp generado a partir del LocalDate actual
 */
fun LocalDate.toTimestamp(): Timestamp {
    val dateTime = this.atStartOfDay()
    return dateTime.toTimestamp()
} 