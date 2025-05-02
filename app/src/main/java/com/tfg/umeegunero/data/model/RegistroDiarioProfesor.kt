package com.tfg.umeegunero.data.model

import java.util.Date

/**
 * Modelo de datos que representa un registro diario realizado por un profesor para un alumno.
 * Contiene información sobre comidas, siestas, cambios de pañal y otras actividades diarias.
 *
 * @property id Identificador único del registro
 * @property alumnoId Identificador del alumno al que pertenece el registro
 * @property profesorId Identificador del profesor que realizó el registro
 * @property claseId Identificador de la clase del alumno
 * @property fecha Fecha del registro
 * @property desayuno Indica si el alumno desayunó en el centro
 * @property comida Indica si el alumno comió en el centro
 * @property merienda Indica si el alumno merendó en el centro
 * @property siesta Duración de la siesta en minutos (0 si no durmió)
 * @property cambiosPanal Lista de horas en las que se cambió el pañal
 * @property asistencia Indica si el alumno asistió ese día
 * @property observaciones Observaciones adicionales sobre el día
 * @property estado Estado actual del registro (borrador, completado, revisado)
 * @property ultimaModificacion Fecha de la última modificación del registro
 */
data class RegistroDiarioProfesor(
    val id: String = "",
    val alumnoId: String = "",
    val profesorId: String = "",
    val claseId: String = "",
    val fecha: Date = Date(),
    val desayuno: Boolean = false,
    val comida: Boolean = false,
    val merienda: Boolean = false,
    val siesta: Int = 0, // Duración en minutos
    val cambiosPanal: List<Date> = emptyList(),
    val asistencia: Boolean = true,
    val observaciones: String = "",
    val estado: EstadoRegistro = EstadoRegistro.BORRADOR,
    val ultimaModificacion: Date = Date()
) {
    /**
     * Posibles estados de un registro diario
     */
    enum class EstadoRegistro {
        BORRADOR,
        COMPLETADO,
        REVISADO
    }
} 