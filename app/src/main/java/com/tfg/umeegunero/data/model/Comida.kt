package com.tfg.umeegunero.data.model

/**
 * Modelo que representa la información detallada sobre las comidas de un alumno 
 * en el sistema UmeEgunero.
 * 
 * Esta clase define la estructura de datos para registrar el consumo y detalles
 * de los distintos platos servidos durante las comidas en centros educativos.
 * Es especialmente útil en educación infantil, donde el seguimiento de la
 * alimentación es parte importante de la comunicación con las familias.
 * 
 * El modelo proporciona campos para cada tipo de plato (primero, segundo y postre),
 * permitiendo registrar tanto el nivel de consumo como descripciones adicionales.
 * Se utiliza principalmente como componente del [RegistroActividad].
 * 
 * @property consumoPrimero Nivel de consumo del primer plato (ej: "COMPLETO", "PARCIAL")
 * @property descripcionPrimero Descripción del primer plato servido
 * @property consumoSegundo Nivel de consumo del segundo plato
 * @property descripcionSegundo Descripción del segundo plato servido
 * @property consumoPostre Nivel de consumo del postre
 * @property descripcionPostre Descripción del postre servido
 * @property observaciones Comentarios adicionales sobre la comida, alergias observadas, etc.
 * 
 * @see RegistroActividad Entidad principal que utiliza este modelo
 * @see EstadoComida Enumeración relacionada para estados estándar de consumo
 */
data class Comida(
    val consumoPrimero: String? = null,
    val descripcionPrimero: String? = null,
    val consumoSegundo: String? = null,
    val descripcionSegundo: String? = null,
    val consumoPostre: String? = null,
    val descripcionPostre: String? = null,
    val observaciones: String? = null
)

/**
 * Modelo que representa un plato individual y su nivel de consumo por parte del alumno.
 * 
 * Esta clase encapsula la información tanto del alimento servido como del 
 * comportamiento alimenticio del alumno respecto a ese plato específico.
 * Permite un registro más granular que facilita el seguimiento de preferencias
 * y hábitos alimenticios.
 * 
 * Se utiliza como componente básico del modelo [Comidas] y ofrece una
 * alternativa más estructurada al uso de campos individuales para cada plato.
 * 
 * @property descripcion Descripción textual del plato servido (ej: "Puré de verduras")
 * @property estadoComida Nivel de aceptación y consumo del plato
 * 
 * @see Comidas Modelo contenedor que agrupa múltiples platos
 * @see EstadoComida Enumeración que define los niveles estándar de consumo
 */
data class Plato(
    val descripcion: String = "",
    val estadoComida: EstadoComida = EstadoComida.NO_SERVIDO
)

/**
 * Modelo que representa el conjunto completo de comidas servidas durante una jornada.
 * 
 * Esta clase agrupa los diferentes platos servidos al alumno durante el día,
 * proporcionando una visión estructurada y completa de su alimentación.
 * Facilita la generación de informes alimenticios y el seguimiento
 * de patrones nutricionales a lo largo del tiempo.
 * 
 * El modelo utiliza instancias de [Plato] para representar cada uno de los
 * elementos de la comida, aportando consistencia y facilitando el procesamiento
 * de la información alimenticia.
 * 
 * Se utiliza principalmente como componente del [RegistroActividad], ofreciendo
 * una alternativa más estructurada a los campos individuales de comida.
 * 
 * @property primerPlato Información sobre el primer plato servido y su consumo
 * @property segundoPlato Información sobre el segundo plato servido y su consumo
 * @property postre Información sobre el postre servido y su consumo
 * 
 * @see Plato Modelo componente que representa cada plato individual
 * @see RegistroActividad Entidad principal que utiliza este modelo
 */
data class Comidas(
    val primerPlato: Plato = Plato(),
    val segundoPlato: Plato = Plato(),
    val postre: Plato = Plato()
) 