package com.tfg.umeegunero.data.model

/**
 * Enumeración que define los diferentes niveles de consumo de alimentos en el sistema UmeEgunero.
 * 
 * Esta enumeración proporciona una escala estandarizada para medir y registrar el 
 * grado de aceptación y consumo de los diferentes platos servidos a los alumnos durante
 * las comidas escolares. Permite un registro cuantitativo fácil de entender para las familias.
 * 
 * La escala graduada permite:
 * - Comunicar de forma clara y concisa el comportamiento alimenticio
 * - Realizar seguimientos de evolución de hábitos alimenticios
 * - Identificar patrones de aceptación de diferentes tipos de alimentos
 * - Facilitar la generación de informes alimenticios para familias
 * 
 * Se utiliza principalmente en los modelos [Plato] y [Comidas] para representar
 * el nivel de consumo de cada elemento servido durante las comidas.
 * 
 * @property NADA Indica que el alumno no ha consumido nada del plato o alimento.
 * @property POCO Indica que el alumno ha probado o consumido una cantidad mínima.
 * @property BIEN Indica un consumo moderado o satisfactorio del plato.
 * @property TODO Indica que el alumno ha consumido la totalidad del plato servido.
 * 
 * @see Plato Modelo que utiliza esta enumeración para registrar consumo
 * @see Comidas Modelo que agrupa elementos con niveles de consumo
 * @see EstadoComida Enumeración alternativa para estados de consumo
 */
enum class NivelConsumo {
    NADA,  // No ha comido nada
    POCO,  // Ha probado o comido muy poco
    BIEN,  // Ha comido una cantidad satisfactoria
    TODO   // Ha terminado todo el plato
} 