package com.tfg.umeegunero.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Clase sellada para representar el resultado de operaciones asíncronas en la aplicación.
 * Implementa el patrón Result para manejar éxitos, errores y estados de carga.
 */
sealed class Result<out T> {
    /**
     * Representa un resultado exitoso con datos
     */
    data class Success<out T>(val data: T) : Result<T>()
    
    /**
     * Representa un error con una excepción opcional
     */
    data class Error(val exception: Throwable? = null) : Result<Nothing>()
    
    /**
     * Representa un estado de carga, opcionalmente con datos previos
     */
    data class Loading<out T>(val data: T? = null) : Result<T>()
}

/**
 * Transforma un Flow normal en un Flow de Result, gestionando los estados
 * Loading, Success y Error automáticamente.
 *
 * @param T Tipo de datos del Flow
 * @return Flow de Result que maneja los estados de la operación
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading()) }
        .catch { emit(Result.Error(it)) }
}

/**
 * Extrae los datos de un Result.Success o devuelve un valor por defecto
 *
 * @param defaultValue Valor que se devuelve si el resultado no es Success
 * @return Datos contenidos en Success o defaultValue
 */
fun <T> Result<T>.getOrDefault(defaultValue: T): T {
    return if (this is Result.Success) data else defaultValue
}

/**
 * Extrae los datos de un Result.Success o lanza la excepción contenida en Result.Error
 *
 * @throws Throwable La excepción contenida en Result.Error o IllegalStateException si es Loading
 * @return Datos contenidos en Success
 */
fun <T> Result<T>.getOrThrow(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception ?: IllegalStateException("Error desconocido")
        is Result.Loading -> throw IllegalStateException("El resultado aún está cargando")
    }
}

/**
 * Aplica una transformación a los datos contenidos en Result.Success.
 * Preserva el estado (Loading o Error) si no es Success.
 *
 * @param transform Función de transformación para los datos
 * @return Un nuevo Result con los datos transformados o en el mismo estado anterior
 */
fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(this.exception)
        is Result.Loading -> Result.Loading(this.data?.let { transform(it) })
    }
} 