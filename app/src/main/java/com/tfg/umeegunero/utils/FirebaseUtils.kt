package com.tfg.umeegunero.utils

import com.google.firebase.Timestamp
import java.util.Date

/**
 * Convierte un Timestamp de Firebase a un objeto Date
 */
fun timestampToDate(timestamp: Timestamp?): Date? {
    return timestamp?.toDate()
}

/**
 * Convierte una Date a un Timestamp de Firebase
 */
fun dateToTimestamp(date: Date?): Timestamp? {
    return date?.let { Timestamp(it) }
} 