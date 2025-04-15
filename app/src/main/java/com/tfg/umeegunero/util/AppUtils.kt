package com.tfg.umeegunero.util

/**
 * Utilidades para código común y funciones que ayudan a corregir warnings
 */
object AppUtils {
    /**
     * Reemplaza el método capitalize() obsoleto
     * @param str String a capitalizar
     * @return String con la primera letra en mayúscula
     */
    fun capitalizeFirst(str: String): String {
        return str.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
    
    /**
     * Accede a una propiedad no nula de manera segura sin operador Elvis redundante
     * @param value Valor a devolver
     * @return El mismo valor
     */
    fun <T> safeAccess(value: T): T {
        return value
    }

    /**
     * Convierte un objeto nullable a un objeto no nullable con valor por defecto
     * @param value Valor que podría ser nulo
     * @param default Valor por defecto
     * @return El valor o el valor por defecto si es nulo
     */
    fun <T> valueOrDefault(value: T?, default: T): T {
        return value ?: default
    }
    
    /**
     * Accede a una propiedad de una lista de manera segura sin generar warnings
     * @param list Lista a acceder
     * @param index Índice de la lista
     * @return Elemento en el índice o null si está fuera de rango
     */
    fun <T> safeGet(list: List<T>, index: Int): T? {
        return if (index in list.indices) list[index] else null
    }
} 