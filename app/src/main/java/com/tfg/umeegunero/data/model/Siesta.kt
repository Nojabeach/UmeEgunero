package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp

/**
 * Modelo que representa información sobre la siesta de un alumno en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para registrar los períodos de descanso
 * o siesta de los alumnos, principalmente en contextos de educación infantil.
 * Permite un seguimiento detallado tanto de la duración como de la calidad del descanso,
 * información valiosa para comunicar a las familias.
 * 
 * El modelo captura tanto datos cuantitativos (duración, horas de inicio y fin) como
 * cualitativos (observaciones sobre la calidad del descanso), proporcionando una
 * visión completa del patrón de sueño del alumno durante la jornada escolar.
 * 
 * Se utiliza principalmente como componente del [RegistroActividad] para documentar
 * las rutinas diarias de los alumnos más pequeños.
 * 
 * @property duracion Duración total de la siesta en minutos
 * @property observaciones Comentarios sobre la calidad del descanso, incidencias, etc.
 * @property inicio Marca temporal del momento en que el alumno comenzó a dormir
 * @property fin Marca temporal del momento en que el alumno despertó
 * 
 * @see RegistroActividad Entidad principal que utiliza este modelo
 */
data class Siesta(
    val duracion: Int = 0,
    val observaciones: String = "",
    val inicio: Timestamp? = null,
    val fin: Timestamp? = null
) 