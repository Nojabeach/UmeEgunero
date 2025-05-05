package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Representa una tarea en el sistema educativo de UmeEgunero.
 *
 * Esta clase define la estructura de una tarea académica, que puede ser asignada
 * por profesores a alumnos, con capacidad de seguimiento y evaluación.
 *
 * Las tareas pueden ser de diversos tipos, como trabajos escritos, proyectos,
 * ejercicios o actividades prácticas, con diferentes estados de progreso.
 *
 * @property id Identificador único de la tarea
 * @property titulo Título descriptivo de la tarea
 * @property descripcion Descripción detallada de los requisitos y objetivos de la tarea
 * @property profesorId Identificador del profesor que asigna la tarea
 * @property cursoId Identificador del curso al que pertenece la tarea
 * @property claseId Identificador de la clase a la que se asigna la tarea
 * @property fechaCreacion Fecha de creación de la tarea
 * @property fechaEntrega Fecha límite de entrega de la tarea
 * @property tipoTarea Tipo de tarea (trabajo, ejercicio, proyecto, etc.)
 * @property estado Estado actual de la tarea (pendiente, en progreso, entregada, calificada)
 * @property alumnosIds Lista de identificadores de alumnos a los que se asigna la tarea
 * @property archivosAdjuntos Lista de archivos adjuntos relacionados con la tarea
 * @property puntuacionMaxima Puntuación máxima posible para la tarea
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
data class Tarea(
    @DocumentId val id: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val claseId: String = "",
    val nombreClase: String = "",
    val alumnoId: String = "", // ID del alumno si es tarea individual
    val titulo: String = "",
    val descripcion: String = "",
    val asignatura: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaEntrega: Timestamp? = null,
    val adjuntos: List<String> = emptyList(), // URLs de archivos adjuntos
    val estado: EstadoTarea = EstadoTarea.PENDIENTE,
    val prioridad: PrioridadTarea = PrioridadTarea.MEDIA,
    val revisadaPorFamiliar: Boolean = false,
    val fechaRevision: Timestamp? = null,
    val comentariosFamiliar: String = "",
    val calificacion: Double? = null,
    val feedbackProfesor: String = ""
)

/**
 * Enumeración para representar los diferentes estados de una tarea
 */
enum class EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADA,
    CANCELADA,
    VENCIDA
}

/**
 * Enumeración para representar la prioridad de una tarea
 */
enum class PrioridadTarea {
    BAJA,
    MEDIA,
    ALTA,
    URGENTE
} 