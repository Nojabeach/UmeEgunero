package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.tfg.umeegunero.data.model.PendingOperation

/**
 * Converter para manejar el enum PendingOperation.Type en Room
 */
class ConverterPendingOperationType {
    
    @TypeConverter
    fun fromType(type: PendingOperation.Type?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toType(value: String?): PendingOperation.Type? {
        return value?.let { PendingOperation.Type.valueOf(it) }
    }
} 