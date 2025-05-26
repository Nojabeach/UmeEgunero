package com.tfg.umeegunero.util

/**
 * Utilidades generales para la aplicación UmeEgunero.
 * 
 * Esta clase objeto proporciona funciones de utilidad comunes que ayudan a:
 * - Corregir warnings de compilación de Kotlin
 * - Proporcionar alternativas a métodos obsoletos
 * - Ofrecer operaciones seguras para acceso a datos
 * - Facilitar el manejo de valores nullable de forma consistente
 * 
 * Las funciones están diseñadas para mejorar la legibilidad del código
 * y reducir la duplicación de lógica común en toda la aplicación.
 * 
 * ## Categorías de utilidades:
 * - **Manipulación de strings**: Capitalización y formateo
 * - **Acceso seguro**: Operaciones null-safe
 * - **Valores por defecto**: Manejo de valores nullable
 * - **Acceso a colecciones**: Operaciones seguras en listas
 * 
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
object AppUtils {
    /**
     * Reemplaza el método capitalize() obsoleto de Kotlin.
     * 
     * Proporciona una alternativa moderna al método deprecated `capitalize()`
     * utilizando las nuevas APIs de Kotlin para manipulación de strings.
     * 
     * @param str String a capitalizar (primera letra en mayúscula)
     * @return String con la primera letra en mayúscula y el resto sin cambios
     * 
     * @sample
     * ```kotlin
     * val resultado = AppUtils.capitalizeFirst("hola mundo")
     * // resultado = "Hola mundo"
     * ```
     */
    fun capitalizeFirst(str: String): String {
        return str.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
    
    /**
     * Accede a una propiedad no nula de manera segura sin operador Elvis redundante.
     * 
     * Esta función ayuda a evitar warnings del compilador cuando se accede
     * a propiedades que ya se sabe que no son nulas, pero el compilador
     * no puede inferirlo automáticamente.
     * 
     * @param T Tipo del valor a devolver
     * @param value Valor no nulo a devolver
     * @return El mismo valor sin modificaciones
     * 
     * @sample
     * ```kotlin
     * val usuario = obtenerUsuario() // Usuario?
     * if (usuario != null) {
     *     val nombre = AppUtils.safeAccess(usuario.nombre)
     * }
     * ```
     */
    fun <T> safeAccess(value: T): T {
        return value
    }

    /**
     * Convierte un objeto nullable a un objeto no nullable con valor por defecto.
     * 
     * Proporciona una forma explícita y legible de manejar valores nullable,
     * especialmente útil cuando se quiere evitar el operador Elvis (?:) 
     * para mejorar la legibilidad del código.
     * 
     * @param T Tipo del valor
     * @param value Valor que podría ser nulo
     * @param default Valor por defecto a usar si value es null
     * @return El valor original si no es null, o el valor por defecto
     * 
     * @sample
     * ```kotlin
     * val nombre = AppUtils.valueOrDefault(usuario.nombre, "Sin nombre")
     * val edad = AppUtils.valueOrDefault(usuario.edad, 0)
     * ```
     */
    fun <T> valueOrDefault(value: T?, default: T): T {
        return value ?: default
    }
    
    /**
     * Accede a un elemento de una lista de manera segura sin generar warnings.
     * 
     * Proporciona acceso seguro a elementos de lista verificando que el índice
     * esté dentro del rango válido antes de acceder al elemento. Evita
     * IndexOutOfBoundsException y warnings del compilador.
     * 
     * @param T Tipo de elementos en la lista
     * @param list Lista a la que acceder
     * @param index Índice del elemento deseado
     * @return Elemento en el índice especificado o null si está fuera de rango
     * 
     * @sample
     * ```kotlin
     * val lista = listOf("a", "b", "c")
     * val elemento = AppUtils.safeGet(lista, 1) // "b"
     * val fueraDeRango = AppUtils.safeGet(lista, 5) // null
     * ```
     */
    fun <T> safeGet(list: List<T>, index: Int): T? {
        return if (index in list.indices) list[index] else null
    }
} 