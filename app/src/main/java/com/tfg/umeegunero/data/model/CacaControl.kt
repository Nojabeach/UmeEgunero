package com.tfg.umeegunero.data.model

/**
 * Modelo que representa la información sobre necesidades fisiológicas de un alumno
 * en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para registrar el control de esfínteres
 * y necesidades fisiológicas de los alumnos, particularmente importante en educación
 * infantil donde este seguimiento forma parte del cuidado diario y desarrollo evolutivo.
 * 
 * El modelo permite registrar diferentes tipos y características de las deposiciones,
 * así como otras necesidades fisiológicas como la micción. Esta información es relevante
 * tanto para el seguimiento del desarrollo del control de esfínteres como para la 
 * detección temprana de posibles problemas de salud.
 * 
 * Se utiliza principalmente como componente del [RegistroActividad] para la comunicación
 * con las familias sobre los hábitos y patrones fisiológicos del alumno durante la
 * jornada escolar.
 * 
 * @property tipo1 Indica si la deposición fue de consistencia dura (formato legado)
 * @property tipo2 Indica si la deposición fue de consistencia normal (formato legado)
 * @property tipo3 Indica si la deposición fue de consistencia blanda o líquida (formato legado)
 * @property hora Hora aproximada en que ocurrió la deposición (formato legado)
 * @property cantidad Cantidad estimada de la deposición (formato legado)
 * @property tipo Tipo o consistencia de la deposición (formato legado)
 * @property descripcion Descripción adicional sobre las características (formato legado)
 * @property caca Indica si el alumno ha realizado deposiciones (formato actual)
 * @property pipi Indica si el alumno ha realizado micciones (formato actual)
 * @property observaciones Comentarios adicionales sobre las necesidades fisiológicas
 * 
 * @see RegistroActividad Entidad principal que utiliza este modelo
 */
data class CacaControl(
    val tipo1: Boolean? = null,
    val tipo2: Boolean? = null,
    val tipo3: Boolean? = null,
    val hora: String? = null,
    val cantidad: String? = null,
    val tipo: String? = null,
    val descripcion: String? = null,
    val caca: Boolean = false,
    val pipi: Boolean = false,
    val observaciones: String = ""
)

/**
 * Alias tipificado para el modelo [CacaControl] que proporciona un nombre semánticamente
 * más descriptivo para su uso en el código.
 * 
 * Este alias existe por razones de compatibilidad con código existente y para ofrecer
 * una denominación más apropiada y profesional en el contexto educativo. Permite
 * referenciar el modelo con una terminología más formal en nuevas implementaciones,
 * manteniendo la compatibilidad con el código legacy.
 * 
 * @see CacaControl Modelo base al que refiere este alias
 * @see RegistroActividad Entidad que utiliza este modelo a través del alias
 */
typealias NecesidadesFisiologicas = CacaControl 