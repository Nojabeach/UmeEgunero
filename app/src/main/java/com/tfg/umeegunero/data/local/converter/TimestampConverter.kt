package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class TimestampConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Date? {
        return timestamp?.toDate()
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Timestamp? {
        return date?.let { Timestamp(it) }
    }
} 