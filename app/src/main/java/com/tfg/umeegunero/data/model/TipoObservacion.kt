package com.tfg.umeegunero.data.model

/**
 * Enumeración que define los diferentes tipos de observaciones que se pueden registrar
 * sobre un alumno en el sistema UmeEgunero.
 * 
 * Esta enumeración proporciona una categorización estandarizada para las observaciones
 * realizadas por los educadores, facilitando su clasificación, filtrado y posterior
 * procesamiento. La tipificación permite organizar la información según su naturaleza
 * y mejorar la comunicación con las familias al estructurar los mensajes por temáticas.
 * 
 * Los tipos abarcan desde aspectos del desarrollo personal y académico hasta necesidades
 * materiales específicas, cubriendo las principales áreas de seguimiento en entornos
 * educativos infantiles.
 * 
 * Se utiliza principalmente en el modelo [Observacion] para categorizar las anotaciones
 * realizadas sobre los alumnos durante la jornada escolar.
 * 
 * @property COMPORTAMIENTO Observaciones relacionadas con conducta, socialización, emociones o actitudes
 * @property ACADEMICO Observaciones sobre rendimiento, aprendizaje, participación académica y logros educativos
 * @property TOALLITAS Notificación sobre la necesidad de reponer toallitas higiénicas
 * @property PAÑALES Notificación sobre la necesidad de reponer pañales
 * @property ROPA Notificación sobre la necesidad de ropa de cambio o aspectos relacionados con la vestimenta
 * @property OTRO Observaciones de carácter general que no encajan en las categorías anteriores
 * 
 * @see Observacion Modelo que utiliza esta enumeración para categorizar observaciones
 * @see RegistroActividad Entidad principal donde se utilizan observaciones categorizadas
 */
enum class TipoObservacion {
    /** Observaciones sobre conducta, relaciones sociales y aspectos emocionales */
    COMPORTAMIENTO, 
    
    /** Observaciones sobre progreso académico y aprendizaje */
    ACADEMICO, 
    
    /** Notificación de necesidad de reposición de toallitas */
    TOALLITAS, 
    
    /** Notificación de necesidad de reposición de pañales */
    PAÑALES, 
    
    /** Notificación de necesidad de ropa de cambio */
    ROPA, 
    
    /** Otras observaciones no clasificables en categorías anteriores */
    OTRO
} 