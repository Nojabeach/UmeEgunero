package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo que representa una observación puntual sobre un alumno en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para registrar comentarios, incidencias
 * o anotaciones específicas sobre un alumno durante la jornada escolar. A diferencia
 * de otros modelos más estructurados, las observaciones permiten capturar información
 * libre y flexible sobre cualquier aspecto relevante.
 * 
 * Las observaciones pueden ser de diferentes tipos según su naturaleza (comportamiento,
 * académico, necesidades materiales, etc.), lo que facilita su categorización y
 * procesamiento posterior. Cada observación incluye una marca temporal para situar
 * cronológicamente el evento o comentario.
 * 
 * Este modelo es especialmente útil para:
 * - Documentar incidencias puntuales
 * - Registrar logros o dificultades específicas
 * - Comunicar necesidades de materiales a las familias
 * - Dejar constancia de situaciones destacables
 * 
 * Se utiliza principalmente en el contexto del [RegistroActividad] como parte
 * del registro diario del alumno.
 * 
 * @property mensaje Texto descriptivo que contiene el contenido de la observación
 * @property tipo Categoría de la observación según su naturaleza, definida por [TipoObservacion]
 * @property timestamp Marca temporal que indica cuándo se realizó la observación
 * 
 * @see TipoObservacion Enumeración que define los tipos de observación disponibles
 * @see RegistroActividad Entidad principal donde se utilizan las observaciones
 */
data class Observacion(
    val mensaje: String = "",
    val tipo: TipoObservacion = TipoObservacion.OTRO,
    val timestamp: Timestamp = Timestamp.now()
)