package com.tfg.umeegunero.data.model

/**
 * Clase sellada que representa el resultado de una operación asíncrona.
 * Puede estar en estado de carga, éxito o error.
 */
sealed class Result<out T> {
    /**
     * Estado de carga. Indica que la operación está en progreso.
     */
    data object Loading : Result<Nothing>()

    /**
     * Estado de éxito. Contiene los datos del resultado de la operación.
     * @property data Datos resultantes de la operación
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Estado de error. Contiene un mensaje descriptivo del error.
     * @property message Mensaje de error
     */
    data class Error(val message: String? = null) : Result<Nothing>()
} 