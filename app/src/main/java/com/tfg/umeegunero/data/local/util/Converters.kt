package com.tfg.umeegunero.data.local.util

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tfg.umeegunero.data.local.entity.PendingSyncEntity
import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.InteractionStatus
import java.util.Date

/**
 * Clase que proporciona conversores para tipos de datos complejos utilizados con Room.
 * 
 * Room solo puede almacenar tipos primitivos y Strings, por lo que necesitamos convertir otros tipos
 * como listas, mapas, enums y objetos personalizados a formatos que Room pueda gestionar.
 */
class Converters {
    private val gson = Gson()
    
    /**
     * Convierte un Timestamp de Firestore a Date
     */
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Date? {
        return value?.toDate()
    }
    
    /**
     * Convierte un Date a Timestamp de Firestore
     */
    @TypeConverter
    fun toTimestamp(date: Date?): Timestamp? {
        return date?.let { Timestamp(it) }
    }
    
    /**
     * Convierte una fecha (Date) a un timestamp (Long).
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Convierte un timestamp (Long) a un objeto Date.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte un OperationType a String
     */
    @TypeConverter
    fun fromOperationType(value: PendingSyncEntity.OperationType?): String? {
        return value?.name
    }
    
    /**
     * Convierte un String a OperationType
     */
    @TypeConverter
    fun toOperationType(value: String?): PendingSyncEntity.OperationType? {
        return value?.let { PendingSyncEntity.OperationType.valueOf(it) }
    }

    /**
     * Convierte una lista de strings a formato JSON.
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) "" else gson.toJson(value)
    }

    /**
     * Convierte un string JSON a una lista de strings.
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    /**
     * Convierte un mapa de String a String a formato JSON.
     */
    @TypeConverter
    fun fromStringMap(map: Map<String, String>?): String {
        return if (map == null) "" else gson.toJson(map)
    }

    /**
     * Convierte un string JSON a un mapa de String a String.
     */
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        if (value.isEmpty()) return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    /**
     * Convierte un mapa de String a Any a formato JSON.
     */
    @TypeConverter
    fun fromAnyMap(map: Map<String, Any>?): String {
        return if (map == null) "" else gson.toJson(map)
    }

    /**
     * Convierte un string JSON a un mapa de String a Any.
     */
    @TypeConverter
    fun toAnyMap(value: String): Map<String, Any> {
        if (value.isEmpty()) return emptyMap()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(value, type)
    }

    /**
     * Convierte un Long a Timestamp de Firebase.
     * 
     * @param value Timestamp en milisegundos
     * @return Objeto Timestamp de Firebase o null si el valor es null
     */
    @TypeConverter
    fun fromLongToFirebaseTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }
    
    /**
     * Convierte un Timestamp de Firebase a Long.
     * 
     * @param timestamp Objeto Timestamp de Firebase
     * @return Timestamp en milisegundos o null si el objeto es null
     */
    @TypeConverter
    fun firebaseTimestampToLong(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }
    
    /**
     * Convierte un String a EstadoComida.
     * 
     * @param value Nombre del estado como String
     * @return Enum EstadoComida o NO_SERVIDO si el valor es null o inválido
     */
    @TypeConverter
    fun toEstadoComida(value: String?): EstadoComida {
        return try {
            value?.let { EstadoComida.valueOf(it) } ?: EstadoComida.NO_SERVIDO
        } catch (e: IllegalArgumentException) {
            EstadoComida.NO_SERVIDO
        }
    }
    
    /**
     * Convierte un EstadoComida a String.
     * 
     * @param estado Enum EstadoComida
     * @return Nombre del estado como String o null si el objeto es null
     */
    @TypeConverter
    fun fromEstadoComida(estado: EstadoComida?): String? {
        return estado?.name
    }
    
    /**
     * Convierte un String a InteractionStatus.
     */
    @TypeConverter
    fun toInteractionStatus(value: String?): InteractionStatus {
        return try {
            value?.let { InteractionStatus.valueOf(it) } ?: InteractionStatus.NONE
        } catch (e: IllegalArgumentException) {
            InteractionStatus.NONE
        }
    }
    
    /**
     * Convierte un InteractionStatus a String.
     */
    @TypeConverter
    fun fromInteractionStatus(estado: InteractionStatus?): String {
        return estado?.name ?: InteractionStatus.NONE.name
    }
    
    /**
     * Convierte un String a AttachmentType.
     */
    @TypeConverter
    fun toAttachmentType(value: String?): AttachmentType? {
        return try {
            value?.let { AttachmentType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
    
    /**
     * Convierte un AttachmentType a String.
     */
    @TypeConverter
    fun fromAttachmentType(tipo: AttachmentType?): String? {
        return tipo?.name
    }
} 