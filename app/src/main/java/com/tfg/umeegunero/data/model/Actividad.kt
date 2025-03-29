package com.tfg.umeegunero.data.model

/**
 * Modelo que representa la información sobre actividades educativas realizadas
 * por los alumnos en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para registrar las actividades 
 * pedagógicas y lúdicas desarrolladas durante la jornada escolar. Permite
 * documentar tanto el contenido de la actividad como el nivel de participación
 * y desempeño del alumno, facilitando el seguimiento de su desarrollo.
 * 
 * El registro de actividades es particularmente relevante en educación infantil
 * y primaria, donde proporciona a las familias información valiosa sobre el
 * proceso de aprendizaje y socialización de sus hijos.
 * 
 * Se utiliza principalmente como componente del [RegistroActividad] para
 * comunicar el desarrollo de experiencias educativas significativas.
 * 
 * @property titulo Nombre o título descriptivo de la actividad realizada
 * @property descripcion Explicación detallada del contenido y objetivos de la actividad
 * @property participacion Nivel de implicación y desempeño del alumno en la actividad
 * @property observaciones Comentarios adicionales sobre aspectos destacables
 * 
 * @see RegistroActividad Entidad principal que utiliza este modelo
 */
data class Actividad(
    val titulo: String = "",
    val descripcion: String = "",
    val participacion: String = "",
    val observaciones: String = ""
) 