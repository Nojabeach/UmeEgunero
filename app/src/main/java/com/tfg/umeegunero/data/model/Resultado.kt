package com.tfg.umeegunero.data.model

/**
 * Clase sellada para representar el resultado de operaciones asíncronas.
 * Puede ser Exito (con datos), Error (con mensaje) o Cargando (estado de carga).
 */
sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
    data class Cargando<out T>(val datosAnterior: T? = null) : Resultado<T>()
    
    /**
     * Verifica si el resultado es exitoso
     */
    val esExitoso: Boolean get() = this is Exito
    
    /**
     * Verifica si el resultado está en estado de carga
     */
    val estaCargando: Boolean get() = this is Cargando
    
    /**
     * Verifica si el resultado contiene un error
     */
    val esError: Boolean get() = this is Error
    
    /**
     * Obtiene los datos si el resultado es exitoso o null en caso contrario
     */
    fun obtenerONull(): T? = when (this) {
        is Exito -> datos
        else -> null
    }
    
    /**
     * Ejecuta una función si el resultado es exitoso
     */
    inline fun siExitoso(accion: (T) -> Unit): Resultado<T> {
        if (this is Exito) accion(datos)
        return this
    }
    
    /**
     * Ejecuta una función si el resultado contiene un error
     */
    inline fun siError(accion: (String) -> Unit): Resultado<T> {
        if (this is Error) accion(mensaje)
        return this
    }
    
    /**
     * Ejecuta una función si el resultado está en estado de carga
     */
    inline fun siCargando(accion: () -> Unit): Resultado<T> {
        if (this is Cargando) accion()
        return this
    }
} 