package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Representa una actividad preescolar en el sistema educativo de UmeEgunero.
 *
 * Esta clase define la estructura de una actividad realizada por un niño en edad preescolar,
 * permitiendo un seguimiento detallado de su desarrollo, aprendizaje y comportamiento.
 *
 * Las actividades pueden incluir aspectos como juegos, aprendizaje, alimentación, 
 * descanso y otras interacciones educativas importantes en la etapa infantil.
 *
 * @property id Identificador único de la actividad
 * @property alumnoId Identificador del alumno que realiza la actividad
 * @property profesorId Identificador del profesor que registra la actividad
 * @property fechaRegistro Fecha y hora de registro de la actividad
 * @property tipoActividad Tipo de actividad realizada (juego, aprendizaje, alimentación, etc.)
 * @property descripcion Descripción detallada de la actividad y su desarrollo
 * @property etiquetas Lista de etiquetas que caracterizan la actividad
 * @property imagenes Lista de URLs de imágenes asociadas a la actividad
 * @property duracion Duración de la actividad en minutos
 * @property observaciones Notas adicionales del profesor sobre la actividad
 * @property centroId Identificador del centro educativo
 * @property claseId Identificador de la clase a la que pertenece el alumno
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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
    REALIZADA,
    CANCELADA
} 