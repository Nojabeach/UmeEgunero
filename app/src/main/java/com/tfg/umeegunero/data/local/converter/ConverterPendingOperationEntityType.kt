package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.tfg.umeegunero.data.model.PendingOperation

/**
 * Converter para manejar el enum PendingOperation.EntityType en Room
 */
class ConverterPendingOperationEntityType {
    
    @TypeConverter
    fun fromEntityType(entityType: PendingOperation.EntityType?): String? {
        return entityType?.name
    }
    
    @TypeConverter
    fun toEntityType(value: String?): PendingOperation.EntityType? {
        return value?.let { PendingOperation.EntityType.valueOf(it) }
    }
} 