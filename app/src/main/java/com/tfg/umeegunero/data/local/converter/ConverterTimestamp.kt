package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

/**
 * Converter para manejar objetos Timestamp de Firestore en Room
 */
class ConverterTimestamp {
    
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
    
    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }
} 