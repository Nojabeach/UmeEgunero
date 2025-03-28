package com.tfg.umeegunero.data.model

/**
 * Enum que define los posibles estados para las comidas de los alumnos.
 * 
 * Este enum es utilizado tanto en los registros de actividades como en otras
 * partes de la aplicación para representar el nivel de consumo de alimentos.
 * 
 * @author Estudiante 2º DAM
 */
enum class EstadoComida {
    /**
     * Indica que no se ha servido la comida
     */
    NO_SERVIDO,
    
    /**
     * Indica que el alumno ha consumido la comida completamente
     */
    COMPLETO,
    
    /**
     * Indica que el alumno ha consumido parte de la comida
     */
    PARCIAL,
    
    /**
     * Indica que el alumno ha rechazado la comida
     */
    RECHAZADO,
    
    /**
     * Indica que esta comida no aplica para el alumno
     */
    NO_APLICABLE
} 