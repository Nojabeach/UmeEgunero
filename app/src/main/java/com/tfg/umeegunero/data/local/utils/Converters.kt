package com.tfg.umeegunero.data.local.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

/**
 * Clase de conversores para Room que permite almacenar tipos de datos complejos en la base de datos.
 * 
 * Esta clase proporciona métodos para convertir entre tipos de datos de Kotlin/Java 
 * y representaciones que pueden ser almacenadas en SQLite.
 *
 * @author Estudiante 2º DAM
 */
class Converters {
    
    private val gson = Gson()
    
    /**
     * Convierte una fecha [Date] a su representación en milisegundos [Long].
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Convierte milisegundos [Long] a un objeto [Date].
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    /**
     * Convierte una lista de cadenas a JSON.
     */
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }
    
    /**
     * Convierte JSON a una lista de cadenas.
     */
    @TypeConverter
    fun toStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Convierte un mapa de String a cualquier tipo a JSON.
     */
    @TypeConverter
    fun fromMapStringToAny(map: Map<String, Any>?): String {
        return gson.toJson(map ?: emptyMap<String, Any>())
    }
    
    /**
     * Convierte JSON a un mapa de String a Any.
     */
    @TypeConverter
    fun toMapStringToAny(json: String): Map<String, Any> {
        if (json.isBlank()) return emptyMap()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(json, type)
    }
} 