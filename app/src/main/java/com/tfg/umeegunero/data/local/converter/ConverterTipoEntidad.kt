package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.tfg.umeegunero.data.model.OperacionPendiente

/**
 * Conversor para el tipo de entidad de operaciones pendientes
 */
class ConverterTipoEntidad {
    
    /**
     * Convierte de enum a String para almacenar en la base de datos
     */
    @TypeConverter
    fun fromTipoEntidad(tipo: OperacionPendiente.TipoEntidad?): String? {
        return tipo?.name
    }
    
    /**
     * Convierte de String a enum para recuperar de la base de datos
     */
    @TypeConverter
    fun toTipoEntidad(value: String?): OperacionPendiente.TipoEntidad? {
        return value?.let { OperacionPendiente.TipoEntidad.valueOf(it) }
    }
} 