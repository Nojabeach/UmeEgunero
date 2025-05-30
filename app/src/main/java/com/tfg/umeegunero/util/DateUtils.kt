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

/**
 * Formatea una fecha en formato corto (dd/MM/yyyy)
 */
fun Date.formatDateShort(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}

/**
 * Formatea una fecha y hora completa (dd/MM/yyyy HH:mm)
 */
fun Date.formatDateTime(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(this)
}

/**
 * Formatea una fecha mostrando solo la hora (HH:mm)
 */
fun Date.formatTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(this)
}

/**
 * Formatea una fecha en formato largo (dd de MMMM de yyyy)
 */
fun Date.formatDateLong(): String {
    val formatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es"))
    return formatter.format(this)
}

/**
 * Determina si la fecha es hoy
 */
fun Date.isToday(): Boolean {
    val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    val dateCompare = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(this)
    return today == dateCompare
}

/**
 * Determina si la fecha fue ayer
 */
fun Date.isYesterday(): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val yesterday = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
    val dateCompare = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(this)
    return yesterday == dateCompare
}

/**
 * Formatea una fecha relativa (Hoy, Ayer o dd/MM/yyyy)
 */
fun Date.formatRelative(): String {
    return when {
        isToday() -> "Hoy ${formatTime()}"
        isYesterday() -> "Ayer ${formatTime()}"
        else -> formatDateShort()
    }
}

/**
 * Clase de utilidades para manejo de fechas
 */
object DateUtils {
    
    /**
     * Calcula la edad en años a partir de un Timestamp de Firebase
     * 
     * @param fechaNacimiento Timestamp de la fecha de nacimiento
     * @return Edad en años
     */
    fun calcularEdad(fechaNacimiento: Timestamp?): Int {
        if (fechaNacimiento == null) return 0
        
        val nacimiento = fechaNacimiento.toDate()
        val hoy = Calendar.getInstance().time
        
        val calNacimiento = Calendar.getInstance()
        calNacimiento.time = nacimiento
        
        val calHoy = Calendar.getInstance()
        calHoy.time = hoy
        
        var edad = calHoy.get(Calendar.YEAR) - calNacimiento.get(Calendar.YEAR)
        if (calHoy.get(Calendar.DAY_OF_YEAR) < calNacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad
    }
    
    /**
     * Calcula la edad en años a partir de un String de fecha
     * 
     * @param fechaNacimiento String con la fecha de nacimiento (formato esperado: YYYY-MM-DD)
     * @return Edad en años o 0 si la fecha no es válida
     */
    fun calcularEdad(fechaNacimiento: String): Int {
        if (fechaNacimiento.isBlank()) return 0
        
        try {
            val partes = fechaNacimiento.split("-")
            if (partes.size != 3) return 0
            
            val año = partes[0].toIntOrNull() ?: return 0
            val mes = partes[1].toIntOrNull()?.minus(1) ?: return 0 // Restar 1 porque Calendar usa 0-11 para meses
            val dia = partes[2].toIntOrNull() ?: return 0
            
            val fechaNacimientoCalendar = Calendar.getInstance()
            fechaNacimientoCalendar.set(año, mes, dia)
            
            val hoyCalendar = Calendar.getInstance()
            
            var años = hoyCalendar.get(Calendar.YEAR) - fechaNacimientoCalendar.get(Calendar.YEAR)
            
            // Verificar si todavía no ha cumplido años en este año
            if (hoyCalendar.get(Calendar.MONTH) < fechaNacimientoCalendar.get(Calendar.MONTH) ||
                (hoyCalendar.get(Calendar.MONTH) == fechaNacimientoCalendar.get(Calendar.MONTH) && 
                 hoyCalendar.get(Calendar.DAY_OF_MONTH) < fechaNacimientoCalendar.get(Calendar.DAY_OF_MONTH))) {
                años--
            }
            
            return if (años < 0) 0 else años
        } catch (e: Exception) {
            return 0
        }
    }

    /**
     * Convierte una fecha en formato string (dd/MM/yyyy) a un string en formato estándar
     * 
     * @param dateString String con la fecha en formato dd/MM/yyyy
     * @return La fecha en formato string normalizado o fecha vacía si hay error
     */
    fun parseDateString(dateString: String): String {
        return try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = formatter.parse(dateString)
            if (date != null) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Parsea un string de fecha en varios formatos comunes (yyyy-MM-dd, dd/MM/yyyy) a un objeto Date.
     *
     * @param dateString El string de la fecha.
     * @return El objeto Date, o null si el parseo falla para todos los formatos conocidos.
     */
    fun parseFlexibleDateString(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            // Añadir más formatos si es necesario
        )

        for (format in formats) {
            try {
                return format.parse(dateString)
            } catch (e: java.text.ParseException) {
                // Intentar el siguiente formato
            }
        }
        return null // No se pudo parsear con ningún formato conocido
    }

    /**
     * Parsea un string de fecha (intentando formatos yyyy-MM-dd o dd/MM/yyyy) y lo formatea a dd/MM/yyyy.
     *
     * @param dateString El string de la fecha original.
     * @return La fecha formateada como dd/MM/yyyy, o el string original si no se puede parsear/formatear, o "N/A" si es nulo/vacío.
     */
    fun formatStringDateToDdMmYyyy(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "N/A"
        val parsedDate = parseFlexibleDateString(dateString)
        return parsedDate?.formatDateShort() ?: dateString // Devuelve el original si no se pudo parsear
    }
} 