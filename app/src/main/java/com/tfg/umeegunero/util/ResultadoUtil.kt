package com.tfg.umeegunero.util

import com.tfg.umeegunero.data.model.Resultado

/**
 * Utilidades para convertir entre los tipos Resultado y Result
 * para facilitar la interoperabilidad entre repositorios y ViewModels.
 */
object ResultadoUtil {
    
    /**
     * Convierte un Resultado<T> a Result<T>
     */
    fun <T> convertirResultadoAResult(resultado: Resultado<T>): Result<T> {
        return when (resultado) {
            is Resultado.Exito -> Result.Success(resultado.datos)
            is Resultado.Error -> Result.Error(Exception(resultado.mensaje))
            is Resultado.Cargando -> Result.Loading(resultado.datos)
        }
    }
    
    /**
     * Convierte un Result<T> a Resultado<T>
     */
    fun <T> convertirResultAResultado(result: Result<T>): Resultado<T> {
        return when (result) {
            is Result.Success -> Resultado.Exito(result.data)
            is Result.Error -> Resultado.Error(result.exception?.message)
            is Result.Loading -> Resultado.Cargando(result.data)
        }
    }
    
    /**
     * Extrae datos seguros desde un Resultado
     */
    fun <T> obtenerDatosDeResultado(resultado: Resultado<T>): T? {
        return when (resultado) {
            is Resultado.Exito -> resultado.datos
            is Resultado.Cargando -> resultado.datos
            is Resultado.Error -> null
        }
    }
    
    /**
     * Extrae datos seguros desde un Result
     */
    fun <T> obtenerDatosDeResult(result: Result<T>): T? {
        return when (result) {
            is Result.Success -> result.data
            is Result.Loading -> result.data
            is Result.Error -> null
        }
    }
} 