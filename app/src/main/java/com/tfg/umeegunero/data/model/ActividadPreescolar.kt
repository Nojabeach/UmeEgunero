package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo que representa una actividad para niños preescolares (2-3 años)
 * 
 * Este modelo está diseñado para actividades menos estructuradas y más lúdicas,
 * adaptadas a las necesidades de desarrollo de niños pequeños.
 * 
 * @property id Identificador único de la actividad
 * @property titulo Título descriptivo de la actividad
 * @property descripcion Descripción detallada de la actividad
 * @property profesorId ID del profesor que creó la actividad
 * @property profesorNombre Nombre del profesor (para mostrar sin consultas adicionales)
 * @property alumnoId ID del alumno al que está dirigida la actividad (puede ser nulo si es grupal)
 * @property claseId ID de la clase a la que está dirigida la actividad
 * @property categoria Categoría de la actividad (motricidad, lenguaje, etc.)
 * @property estado Estado actual de la actividad
 * @property fechaCreacion Fecha en que se creó la actividad
 * @property fechaProgramada Fecha programada para realizar la actividad (opcional)
 * @property revisadaPorFamiliar Indica si la familia ha revisado la actividad
 * @property fechaRevision Fecha en que la familia revisó la actividad
 * @property comentariosProfesor Observaciones adicionales del profesor
 * @property comentariosFamiliar Comentarios de la familia sobre la actividad
 * @property imagenes Lista de URLs de imágenes relacionadas (fotos de la actividad)
 */
@IgnoreExtraProperties
data class ActividadPreescolar(
    @DocumentId val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val alumnoId: String? = null,
    val claseId: String = "",
    val categoria: CategoriaActividad = CategoriaActividad.JUEGO,
    val estado: EstadoActividad = EstadoActividad.PENDIENTE,
    @ServerTimestamp val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaProgramada: Timestamp? = null,
    val revisadaPorFamiliar: Boolean = false,
    val fechaRevision: Timestamp? = null,
    val comentariosProfesor: String = "",
    val comentariosFamiliar: String = "",
    val imagenes: List<String> = emptyList()
)

/**
 * Categorías para actividades preescolares
 */
enum class CategoriaActividad {
    JUEGO,
    MOTOR,
    LENGUAJE,
    MUSICA,
    ARTE,
    EXPLORACION,
    AUTONOMIA,
    OTRA
}

/**
 * Estados posibles de una actividad preescolar
 */
enum class EstadoActividad {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA,
    CANCELADA
} 