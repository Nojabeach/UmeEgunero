package com.tfg.umeegunero.data.model

import java.util.Date

/**
 * Modelo de datos que representa las observaciones realizadas por un profesor sobre un alumno.
 * Permite registrar eventos importantes, logros, incidencias o cualquier información relevante.
 *
 * @property id Identificador único de la observación
 * @property alumnoId Identificador del alumno observado
 * @property profesorId Identificador del profesor que realiza la observación
 * @property claseId Identificador de la clase del alumno
 * @property fecha Fecha en que se realizó la observación
 * @property titulo Título descriptivo de la observación
 * @property descripcion Contenido detallado de la observación
 * @property tipo Categoría o tipo de observación (logro, incidencia, etc.)
 * @property visibilidadFamiliar Indica si la observación es visible para los familiares
 * @property imagenes Lista de URLs de imágenes adjuntas (opcional)
 * @property estado Estado de la observación (borrador, publicada, etc.)
 * @property ultimaModificacion Fecha de la última modificación
 */
data class ObservacionesProfesor(
    val id: String = "",
    val alumnoId: String = "",
    val profesorId: String = "",
    val claseId: String = "",
    val fecha: Date = Date(),
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: TipoObservacion = TipoObservacion.GENERAL,
    val visibilidadFamiliar: Boolean = true,
    val imagenes: List<String> = emptyList(),
    val estado: EstadoObservacion = EstadoObservacion.BORRADOR,
    val ultimaModificacion: Date = Date()
) {
    /**
     * Tipos de observaciones que pueden realizarse
     */
    enum class TipoObservacion {
        GENERAL,
        LOGRO,
        INCIDENCIA,
        COMPORTAMIENTO,
        DESARROLLO,
        SALUD
    }

    /**
     * Estados posibles de una observación
     */
    enum class EstadoObservacion {
        BORRADOR,
        PUBLICADA,
        ARCHIVADA
    }
} 