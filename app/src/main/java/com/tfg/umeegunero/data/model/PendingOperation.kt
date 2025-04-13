package com.tfg.umeegunero.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

/**
 * Modelo que representa una operación pendiente de sincronización.
 */
@Entity(tableName = "pending_operations")
@TypeConverters(PendingOperationConverters::class)
data class PendingOperation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: Type,
    val entityType: EntityType,
    val entityId: String,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null
) {
    enum class Type {
        CREATE,
        UPDATE,
        DELETE
    }

    enum class EntityType {
        COMUNICADO,
        FIRMA_DIGITAL,
        CONFIRMACION_LECTURA,
        USUARIO
    }
}

/**
 * Converters para los tipos complejos de PendingOperation
 */
class PendingOperationConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromTypeToString(type: PendingOperation.Type): String {
        return type.name
    }

    @TypeConverter
    fun fromStringToType(value: String): PendingOperation.Type {
        return PendingOperation.Type.valueOf(value)
    }

    @TypeConverter
    fun fromEntityTypeToString(entityType: PendingOperation.EntityType): String {
        return entityType.name
    }

    @TypeConverter
    fun fromStringToEntityType(value: String): PendingOperation.EntityType {
        return PendingOperation.EntityType.valueOf(value)
    }

    @TypeConverter
    fun fromMapToString(map: Map<String, Any>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun fromStringToMap(value: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, mapType)
    }
} 