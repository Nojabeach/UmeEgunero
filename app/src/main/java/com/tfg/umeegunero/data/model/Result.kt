package com.tfg.umeegunero.data.model

/**
 * Clase sellada para representar el resultado de operaciones asíncronas.
 * Puede ser Success (con datos), Error (con excepción) o Loading (estado de carga).
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
} 