package com.tfg.umeegunero.data.model

import java.time.LocalDate

/**
 * Estado de la UI para la pantalla de seguimiento diario de alumnos de educación infantil.
 *
 * Esta clase encapsula los datos y estados necesarios para la representación de la interfaz de usuario
 * en la pantalla de seguimiento diario para niños de 2 y 3 años. Gestiona información sobre
 * alimentación, siesta, higiene, actividades y observaciones del día.
 *
 * @property isLoading Indica si se están cargando los datos desde el repositorio
 * @property error Mensaje de error en caso de que ocurra algún problema, o null si no hay errores
 * @property fechaSeleccionada Fecha seleccionada para el registro de seguimiento diario
 * @property alumnoSeleccionado ID del alumno seleccionado, o null si no se ha seleccionado ninguno
 * @property nombreAlumno Nombre del alumno seleccionado
 * @property asistenciaRegistrada Indica si se ha registrado la asistencia del alumno para la fecha
 * @property haAsistido Indica si el alumno ha asistido ese día al centro
 * @property motivoAusencia Motivo de la ausencia, si el alumno no ha asistido
 * @property registrosAlimentacion Lista de registros de alimentación del día
 * @property registrosSiesta Lista de registros de siesta del día
 * @property registrosHigiene Lista de registros de control de esfínteres/pañal del día
 * @property registrosActividad Lista de actividades realizadas durante el día
 * @property observacionesGenerales Comentarios generales sobre el día
 * @property enviadoAFamilia Indica si el informe diario ya ha sido enviado a la familia
 */
data class SeguimientoDiarioUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val alumnoSeleccionado: String? = null,
    val nombreAlumno: String = "",
    val asistenciaRegistrada: Boolean = false,
    val haAsistido: Boolean = true,
    val motivoAusencia: String = "",
    val registrosAlimentacion: List<RegistroAlimentacion> = emptyList(),
    val registrosSiesta: List<RegistroSiesta> = emptyList(),
    val registrosHigiene: List<RegistroHigiene> = emptyList(),
    val registrosActividades: List<RegistroActividad> = emptyList(),
    val observacionesGenerales: String = "",
    val enviadoAFamilia: Boolean = false
)

/**
 * Modelo que representa un registro de alimentación para un alumno de educación infantil.
 *
 * @property id Identificador único del registro
 * @property hora Hora en que se realizó la comida/merienda
 * @property tipoComida Tipo de comida (desayuno, almuerzo, comida, merienda)
 * @property descripcion Descripción de los alimentos consumidos
 * @property cantidadConsumida Cantidad aproximada consumida por el niño
 * @property observaciones Observaciones adicionales sobre la alimentación
 */
data class RegistroAlimentacion(
    val id: String = "",
    val hora: String,
    val tipoComida: TipoComida,
    val descripcion: String,
    val cantidadConsumida: NivelConsumo,
    val observaciones: String = ""
)

/**
 * Modelo que representa un registro de siesta para un alumno de educación infantil.
 *
 * @property id Identificador único del registro
 * @property horaInicio Hora en que el niño se durmió
 * @property horaFin Hora en que el niño se despertó
 * @property duracion Duración total de la siesta en minutos
 * @property calidadSueno Indicador de la calidad del sueño
 * @property observaciones Observaciones adicionales sobre la siesta
 */
data class RegistroSiesta(
    val id: String = "",
    val horaInicio: String,
    val horaFin: String,
    val duracion: Int, // en minutos
    val calidadSueno: CalidadSueno,
    val observaciones: String = ""
)

/**
 * Modelo que representa un registro de control de esfínteres/pañal para un alumno de educación infantil.
 *
 * @property id Identificador único del registro
 * @property hora Hora en que se realizó el control
 * @property tipoCambio Tipo de cambio (pañal limpio, deposición, etc.)
 * @property usoPotty Indica si el niño usó el orinal/inodoro
 * @property exitoControl Indica si el control fue exitoso (sin accidentes)
 * @property observaciones Observaciones adicionales sobre el control
 */
data class RegistroHigiene(
    val id: String = "",
    val hora: String,
    val tipoCambio: TipoCambio,
    val usoPotty: Boolean = false,
    val exitoControl: Boolean = true,
    val observaciones: String = ""
)

/**
 * Enumeración que representa el tipo de comida en un registro de alimentación.
 */
enum class TipoComida {
    /** Desayuno de la mañana */
    DESAYUNO,
    
    /** Almuerzo o tentempié a media mañana */
    ALMUERZO,
    
    /** Comida del mediodía */
    COMIDA,
    
    /** Merienda de la tarde */
    MERIENDA
}

/**
 * Enumeración que representa la calidad del sueño durante la siesta.
 */
enum class CalidadSueno {
    /** Sueño inquieto, con despertares frecuentes */
    INQUIETO,
    
    /** Sueño normal, con algunos movimientos */
    NORMAL,
    
    /** Sueño profundo y reparador */
    PROFUNDO
}

/**
 * Enumeración que representa el tipo de cambio de pañal o control de esfínteres.
 */
enum class TipoCambio {
    /** Cambio de pañal húmedo (solo orina) */
    PANAL_HUMEDO,
    
    /** Cambio de pañal con deposición */
    PANAL_DEPOSICION,
    
    /** Control de esfínteres para orina */
    CONTROL_ORINA,
    
    /** Control de esfínteres para deposición */
    CONTROL_DEPOSICION
} 