package com.tfg.umeegunero.feature.profesor.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * Modelo de datos para representar una rúbrica de evaluación.
 * Una rúbrica contiene múltiples criterios con sus respectivos pesos para evaluar
 * el desempeño de los alumnos.
 */
@Parcelize
data class Rubrica(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val descripcion: String = "",
    val asignatura: String = "",
    val criterios: List<Criterio> = emptyList(),
    val fechaCreacion: Date = Date()
) : Parcelable {
    
    /**
     * Calcula la calificación total de la rúbrica basada en los valores asignados a cada criterio
     * considerando su ponderación.
     *
     * @param valoresCriterios Mapa que asocia cada ID de criterio con su valor asignado
     * @return Calificación total calculada sobre 10
     */
    fun calcularCalificacion(valoresCriterios: Map<String, Float>): Float {
        if (criterios.isEmpty()) return 0f
        
        var sumaPonderada = 0f
        var sumaPesos = 0f
        
        criterios.forEach { criterio ->
            val valor = valoresCriterios[criterio.id] ?: 0f
            sumaPonderada += valor * criterio.peso
            sumaPesos += criterio.peso
        }
        
        return if (sumaPesos > 0) (sumaPonderada / sumaPesos) * 10 else 0f
    }
    
    /**
     * Verifica si la rúbrica está completa y válida para ser utilizada.
     *
     * @return true si la rúbrica tiene nombre, asignatura y al menos un criterio
     */
    fun esValida(): Boolean {
        return nombre.isNotBlank() && asignatura.isNotBlank() && criterios.isNotEmpty()
    }
}

/**
 * Modelo que representa un criterio de evaluación dentro de una rúbrica.
 *
 * @property id Identificador único del criterio
 * @property nombre Nombre descriptivo del criterio
 * @property descripcion Descripción detallada de lo que evalúa el criterio
 * @property tipo Tipo de criterio (numérico, textual, selección)
 * @property peso Peso del criterio en el cálculo de la calificación total (1.0 por defecto)
 * @property valorMaximo Valor máximo que puede alcanzar el criterio (solo para tipo numérico)
 * @property opciones Lista de opciones para criterios de tipo selección
 */
@Parcelize
data class Criterio(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: TipoCriterio = TipoCriterio.NUMERICO,
    val peso: Float = 1.0f,
    val valorMaximo: Float = 10.0f,
    val opciones: List<OpcionCriterio> = emptyList()
) : Parcelable

/**
 * Modelo que representa una opción dentro de un criterio de tipo selección.
 *
 * @property id Identificador único de la opción
 * @property texto Texto descriptivo de la opción
 * @property valor Valor numérico asociado a la opción para el cálculo de calificaciones
 */
@Parcelize
data class OpcionCriterio(
    val id: String = UUID.randomUUID().toString(),
    val texto: String = "",
    val valor: Float = 0f
) : Parcelable

/**
 * Enumeración que define los tipos de criterios disponibles para una rúbrica.
 */
enum class TipoCriterio {
    /**
     * Criterio evaluado con un valor numérico dentro de un rango
     */
    NUMERICO,
    
    /**
     * Criterio evaluado con un texto libre
     */
    TEXTUAL,
    
    /**
     * Criterio evaluado mediante selección de una opción entre varias predefinidas
     */
    SELECCION
}

/**
 * Modelo que representa la evaluación de un alumno según una rúbrica específica.
 *
 * @property id Identificador único de la evaluación
 * @property rubricaId ID de la rúbrica utilizada
 * @property alumnoId ID del alumno evaluado
 * @property trimestreId ID del trimestre al que corresponde la evaluación
 * @property valoresCriterios Mapa que asocia cada ID de criterio con su valor asignado
 * @property comentarios Comentarios adicionales sobre la evaluación
 * @property calificacionFinal Calificación final calculada
 * @property fecha Fecha en que se realizó la evaluación
 */
@Parcelize
data class EvaluacionRubrica(
    val id: String = UUID.randomUUID().toString(),
    val rubricaId: String = "",
    val alumnoId: String = "",
    val trimestreId: Int = 0,
    val valoresCriterios: Map<String, Float> = emptyMap(),
    val comentariosCriterios: Map<String, String> = emptyMap(),
    val comentariosGenerales: String = "",
    val calificacionFinal: Float = 0f,
    val fecha: Date = Date()
) : Parcelable 