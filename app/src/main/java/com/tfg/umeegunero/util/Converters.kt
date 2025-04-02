package com.tfg.umeegunero.util

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tfg.umeegunero.data.model.EstadoComida
import java.util.Date

/**
 * Clase de utilidad que proporciona convertidores para tipos de datos complejos.
 * 
 * Estos convertidores pueden utilizarse con Room o para cualquier otra conversión
 * en la aplicación que requiera transformar entre tipos de datos nativos y formatos
 * serializables o entre distintos tipos de datos.
 */
class Converters {
    private val gson = Gson()

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
} 