package com.tfg.umeegunero.data.model

/**
 * Estado de la UI para la pantalla de reporte de seguimiento de educación infantil.
 *
 * Esta clase encapsula los datos y estados necesarios para la representación de la interfaz de usuario
 * en la pantalla de reportes de seguimiento para niños de 2 y 3 años. Gestiona información sobre
 * aspectos de desarrollo, asistencia, actividades y observaciones diarias.
 *
 * @property isLoading Indica si se están cargando los datos desde el repositorio
 * @property error Mensaje de error en caso de que ocurra algún problema, o null si no hay errores
 * @property periodoSeleccionado Periodo de tiempo seleccionado para el análisis (ej. "Último mes", "Trimestre actual")
 * @property centroSeleccionado Centro educativo seleccionado para el análisis, o "Todos los centros"
 * @property alumnoSeleccionado Alumno seleccionado para el seguimiento, o null si no se ha seleccionado ninguno
 * @property asistencia Porcentaje de asistencia en el periodo seleccionado
 * @property areasDesarrollo Lista de áreas de desarrollo infantil con sus datos de seguimiento
 * @property observacionesDestacadas Lista de observaciones destacadas durante el periodo
 * @property recomendaciones Lista de recomendaciones para los cuidadores o profesores
 * @property isGeneratingReport Indica si se está generando un informe PDF para enviar a los padres
 * @property reportGenerated Indica si el informe ha sido generado correctamente
 *
 * @see AreaDesarrollo
 * @see ObservacionInfantil
 * @see RecomendacionInfantil
 */
data class ReporteSeguimientoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val periodoSeleccionado: String = "Trimestre actual",
    val centroSeleccionado: String = "Todos los centros",
    val alumnoSeleccionado: String? = null,
    val asistencia: Float = 0f,
    val areasDesarrollo: List<AreaDesarrollo> = emptyList(),
    val observacionesDestacadas: List<ObservacionInfantil> = emptyList(),
    val recomendaciones: List<RecomendacionInfantil> = emptyList(),
    val isGeneratingReport: Boolean = false,
    val reportGenerated: Boolean = false
)

/**
 * Modelo que representa un área de desarrollo infantil para niños de 2-3 años.
 *
 * Esta clase se utiliza en los informes de seguimiento infantil para mostrar el progreso 
 * en diferentes aspectos del desarrollo del niño, como motricidad, lenguaje, socialización, etc.
 *
 * @property nombre Nombre del área de desarrollo (ej. "Motricidad fina", "Lenguaje", "Socialización")
 * @property progreso Nivel de progreso observado en una escala de 0 a 3 (0: no iniciado, 1: iniciado, 2: en desarrollo, 3: adquirido)
 * @property observaciones Comentarios específicos sobre el desarrollo en esta área
 * @property indicadoresLogro Lista de indicadores específicos que muestran el progreso en esta área
 * @property colorRepresentativo Color opcional para representar esta área en gráficos
 */
data class AreaDesarrollo(
    val nombre: String,
    val progreso: NivelProgreso,
    val observaciones: String = "",
    val indicadoresLogro: List<IndicadorLogro> = emptyList(),
    val colorRepresentativo: androidx.compose.ui.graphics.Color? = null
)

/**
 * Modelo que representa un indicador de logro específico en el desarrollo infantil.
 *
 * @property descripcion Descripción del indicador de logro
 * @property conseguido Si el niño ha conseguido este logro específico
 * @property fechaConsecucion Fecha en que se registró el logro, si está conseguido
 */
data class IndicadorLogro(
    val descripcion: String,
    val conseguido: Boolean = false,
    val fechaConsecucion: String? = null
)

/**
 * Modelo que representa una observación específica sobre el desarrollo o comportamiento del niño.
 *
 * @property fecha Fecha en que se registró la observación
 * @property descripcion Detalle de la observación realizada
 * @property tipo Categoría de la observación (comportamiento, aprendizaje, salud, etc.)
 * @property esPositiva Indica si la observación refleja un aspecto positivo o un área de mejora
 * @property autor Nombre del profesor o cuidador que registró la observación
 */
data class ObservacionInfantil(
    val fecha: String,
    val descripcion: String,
    val tipo: TipoObservacionInfantil,
    val esPositiva: Boolean = true,
    val autor: String
)

/**
 * Modelo que representa una recomendación para mejorar o apoyar el desarrollo del niño.
 *
 * @property titulo Título descriptivo de la recomendación
 * @property descripcion Texto detallado explicando la recomendación
 * @property paraFamilia Indica si la recomendación está dirigida a la familia o al profesorado
 * @property areaRelacionada Área de desarrollo a la que se relaciona esta recomendación
 */
data class RecomendacionInfantil(
    val titulo: String,
    val descripcion: String,
    val paraFamilia: Boolean = true,
    val areaRelacionada: String? = null
)

/**
 * Enumeración que representa el nivel de progreso en una habilidad o área de desarrollo infantil.
 */
enum class NivelProgreso {
    /** La habilidad aún no ha sido iniciada o no ha mostrado signos de desarrollo */
    NO_INICIADO,
    
    /** La habilidad está en sus primeras etapas de desarrollo */
    INICIADO,
    
    /** La habilidad está en proceso de desarrollo activo */
    EN_DESARROLLO,
    
    /** La habilidad ha sido adquirida según lo esperado para la edad */
    ADQUIRIDO
}

/**
 * Enumeración que representa el tipo de observación realizada sobre el niño.
 */
enum class TipoObservacionInfantil {
    /** Observaciones relacionadas con el comportamiento y actitud */
    COMPORTAMIENTO,
    
    /** Observaciones relacionadas con el aprendizaje y desarrollo cognitivo */
    APRENDIZAJE,
    
    /** Observaciones relacionadas con las interacciones sociales */
    SOCIALIZACIÓN,
    
    /** Observaciones relacionadas con aspectos emocionales */
    EMOCIONAL,
    
    /** Observaciones relacionadas con la salud y necesidades físicas */
    SALUD,
    
    /** Otros tipos de observaciones no categorizadas */
    OTROS
} 