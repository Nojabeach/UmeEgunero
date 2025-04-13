package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.tfg.umeegunero.data.model.OperacionPendiente

/**
 * Conversor para el tipo de operación pendiente
 */
class ConverterTipoOperacion {
    
    /**
     * Convierte de enum a String para almacenar en la base de datos
     */
    @TypeConverter
    fun fromTipoOperacion(tipo: OperacionPendiente.Tipo?): String? {
        return tipo?.name
    }
    
    /**
     * Convierte de String a enum para recuperar de la base de datos
     */
    @TypeConverter
    fun toTipoOperacion(value: String?): OperacionPendiente.Tipo? {
        return value?.let { OperacionPendiente.Tipo.valueOf(it) }
    }
} 