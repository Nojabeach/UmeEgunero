package com.tfg.umeegunero.data.model

/**
 * Clase sellada que representa el resultado de una operación asíncrona.
 * Similar a Result pero adaptada para el contexto específico de este proyecto.
 */
sealed class Resultado<out T> {
    /**
     * Estado de carga. Indica que la operación está en progreso.
     */
    data class Cargando<T>(val datos: T? = null) : Resultado<T>()

    /**
     * Estado de éxito. Contiene los datos del resultado de la operación.
     * @property datos Datos resultantes de la operación
     */
    data class Exito<T>(val datos: T) : Resultado<T>()

    /**
     * Estado de error. Contiene un mensaje descriptivo del error.
     * @property mensaje Mensaje de error
     * @property excepcion Excepción opcional para mayor detalle
     */
    data class Error(
        val mensaje: String? = null, 
        val excepcion: Throwable? = null
    ) : Resultado<Nothing>()
    
    /**
     * Comprueba si el resultado es exitoso (instancia de Exito)
     */
    val esExitoso: Boolean get() = this is Exito
    
    /**
     * Comprueba si el resultado es un error (instancia de Error)
     */
    val esError: Boolean get() = this is Error
    
    /**
     * Comprueba si el resultado está en carga (instancia de Cargando)
     */
    val estaCargando: Boolean get() = this is Cargando
    
    /**
     * Obtiene los datos si es un resultado exitoso, o null en caso contrario
     */
    fun obtenerDatos(): T? = when (this) {
        is Exito -> datos
        is Cargando -> datos
        else -> null
    }
    
    /**
     * Obtiene el mensaje de error si es un resultado de error, o null en caso contrario
     */
    fun obtenerMensajeError(): String? = if (this is Error) mensaje else null
} 