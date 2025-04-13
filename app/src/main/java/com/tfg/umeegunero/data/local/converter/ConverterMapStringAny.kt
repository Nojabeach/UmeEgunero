package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Conversor para mapas de datos usando Gson
 */
class ConverterMapStringAny {
    private val gson = Gson()
    
    /**
     * Convierte un mapa a una cadena JSON para almacenar en la base de datos
     */
    @TypeConverter
    fun fromMap(map: Map<String, Any?>?): String? {
        if (map == null) return null
        return gson.toJson(map)
    }
    
    /**
     * Convierte una cadena JSON a un mapa para recuperar de la base de datos
     */
    @TypeConverter
    fun toMap(json: String?): Map<String, Any?>? {
        if (json == null) return null
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        return gson.fromJson(json, type)
    }
} 