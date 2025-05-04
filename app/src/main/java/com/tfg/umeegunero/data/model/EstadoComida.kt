package com.tfg.umeegunero.data.model

/**
 * Enumeración que define los posibles estados para el consumo de alimentos de los alumnos
 * en el sistema UmeEgunero.
 * 
 * Esta enumeración proporciona un conjunto estandarizado de valores para representar
 * el nivel de aceptación y consumo de los distintos platos o alimentos servidos
 * durante las comidas en los centros educativos, especialmente en educación infantil.
 * 
 * Los valores de esta enumeración se utilizan en los registros diarios de actividad
 * para informar a las familias sobre los hábitos alimenticios de los alumnos, permitiendo:
 * - Seguimiento de la aceptación de diferentes alimentos
 * - Identificación de posibles problemas alimentarios
 * - Comunicación precisa con las familias sobre la alimentación
 * - Establecimiento de pautas nutricionales personalizadas
 * 
 * El enum es compatible con funcionalidades de filtrado y estadísticas sobre
 * patrones de alimentación a lo largo del tiempo.
 * 
 * @see RegistroActividad Entidad principal que utiliza esta enumeración
 * @see Comida Modelo relacionado para información detallada de alimentación
 */
enum class EstadoComida {
    /**
     * Indica que no se ha servido la comida al alumno.
     * Utilizado cuando un plato no forma parte del menú del día o
     * cuando el alumno no está presente durante la hora de la comida.
     */
    NO_SERVIDO,
    
    /**
     * Indica que el alumno ha consumido la comida en su totalidad.
     * Representa un comportamiento alimenticio positivo y satisfactorio.
     */
    COMPLETO,
    
    /**
     * Indica que el alumno ha consumido solo una parte de la comida.
     * Puede indicar apetito reducido o preferencia parcial por el plato.
     */
    PARCIAL,
    
    /**
     * Indica que el alumno ha rechazado completamente la comida.
     * Puede señalar aversión a ciertos alimentos o falta de apetito.
     */
    RECHAZADO,
    
    /**
     * Indica que esta comida no aplica para el alumno por alguna razón específica.
     * Utilizado en casos de alergias, dietas especiales, o cuando el alumno
     * trae su propia comida de casa.
     */
    NO_APLICABLE,
    
    /**
     * Indica que no se dispone de información sobre este plato.
     * Utilizado como valor por defecto cuando aún no se ha registrado la información
     * o cuando no se han enviado datos sobre este plato en particular.
     */
    SIN_DATOS
} 