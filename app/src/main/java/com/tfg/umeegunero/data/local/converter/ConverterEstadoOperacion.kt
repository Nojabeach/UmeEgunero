package com.tfg.umeegunero.data.local.converter

import androidx.room.TypeConverter
import com.tfg.umeegunero.data.model.OperacionPendiente

/**
 * Conversor para el estado de operaciones pendientes
 */
class ConverterEstadoOperacion {
    
    /**
     * Convierte de enum a String para almacenar en la base de datos
     */
    @TypeConverter
    fun fromEstadoOperacion(estado: OperacionPendiente.Estado?): String? {
        return estado?.name
    }
    
    /**
     * Convierte de String a enum para recuperar de la base de datos
     */
    @TypeConverter
    fun toEstadoOperacion(value: String?): OperacionPendiente.Estado? {
        return value?.let { OperacionPendiente.Estado.valueOf(it) }
    }
} 