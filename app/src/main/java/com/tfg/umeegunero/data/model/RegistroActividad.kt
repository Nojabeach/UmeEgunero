package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un registro de actividad diaria de un alumno.
 * 
 * Esta clase centraliza toda la información relacionada con las actividades 
 * diarias de un alumno, incluyendo alimentación, siesta, necesidades fisiológicas,
 * y observaciones generales. Está diseñada para centros de educación infantil
 * donde se requiere un seguimiento detallado de las actividades del día.
 * 
 * El registro de actividad es creado por los profesores y puede ser consultado
 * por los familiares, permitiendo una comunicación efectiva entre ambas partes.
 * 
 * Este modelo unifica las funcionalidades de los antiguos modelos
 * RegistroDiario y RegistroActividad para evitar duplicidad y
 * simplificar la estructura de datos.
 * 
 * @property id Identificador único del registro generado automáticamente.
 * @property alumnoId Identificador del alumno al que pertenece el registro.
 * @property alumnoNombre Nombre completo del alumno para visualización rápida.
 * @property claseId Identificador de la clase donde se realizó el registro.
 * @property fecha Fecha y hora en que se creó el registro.
 * @property profesorId Identificador del profesor que creó el registro.
 * @property profesorNombre Nombre del profesor para visualización rápida.
 * @property comida Modelo completo de comida (alternativa a los campos individuales).
 * @property comidas Agrupación de todos los registros de comida.
 * @property primerPlato Estado de consumo del primer plato.
 * @property segundoPlato Estado de consumo del segundo plato.
 * @property postre Estado de consumo del postre.
 * @property merienda Estado de consumo de la merienda.
 * @property observacionesComida Comentarios específicos sobre la alimentación.
 * @property siesta Modelo completo de siesta (alternativa a los campos individuales).
 * @property haSiestaSiNo Indica si el alumno ha dormido siesta.
 * @property horaInicioSiesta Hora en que comenzó la siesta.
 * @property horaFinSiesta Hora en que terminó la siesta.
 * @property observacionesSiesta Comentarios sobre cómo ha dormido.
 * @property cacaControl Modelo completo para necesidades fisiológicas.
 * @property necesidadesFisiologicas Agrupación de todos los registros de necesidades.
 * @property haHechoCaca Indica si el alumno ha hecho deposiciones.
 * @property numeroCacas Cantidad de deposiciones realizadas.
 * @property observacionesCaca Comentarios sobre las deposiciones (consistencia, etc).
 * @property necesitaPanales Indica si el alumno necesita pañales nuevos.
 * @property necesitaToallitas Indica si el alumno necesita toallitas.
 * @property necesitaRopaCambio Indica si el alumno necesita ropa de cambio.
 * @property otroMaterialNecesario Otros materiales que se necesiten reponer.
 * @property actividades Actividades específicas realizadas durante el día.
 * @property observaciones Observaciones generales (nuevo formato).
 * @property observacionesGenerales Observaciones generales (formato anterior).
 * @property etiquetas Lista de IDs de etiquetas asignadas al registro.
 * @property vistoPorFamiliar Indica si el familiar ha visto el registro (nuevo formato).
 * @property fechaVisto Fecha en que el familiar visualizó el registro (nuevo formato).
 * @property visualizadoPorFamiliar Indica si el familiar ha visto el registro (formato anterior).
 * @property fechaVisualizacion Fecha en que el familiar visualizó el registro (formato anterior).
 * @property ultimaModificacion Fecha de la última modificación del registro.
 * @property creadoPor Identificador del usuario que creó el registro.
 * @property modificadoPor Identificador del último usuario que modificó el registro.
 * @property plantillaId Identificador de la plantilla utilizada (si se usó una).
 *
 * @see Comida
 * @see Siesta
 * @see CacaControl
 * @see Actividad
 * @see EtiquetaActividad
 */
data class RegistroActividad(
    // Datos básicos
    @DocumentId val id: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val claseId: String = "",
    val fecha: Timestamp = Timestamp.now(),
    
    // Datos del profesor
    val profesorId: String? = null,
    val profesorNombre: String? = null,
    
    // Datos de comidas
    // Se pueden usar tanto el modelo de Comida como los estados detallados
    val comida: Comida? = null,
    val comidas: Comidas = Comidas(),
    val primerPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val segundoPlato: EstadoComida = EstadoComida.NO_SERVIDO,
    val postre: EstadoComida = EstadoComida.NO_SERVIDO,
    val merienda: EstadoComida = EstadoComida.NO_SERVIDO,
    val observacionesComida: String = "",
    
    // Datos de siesta
    val siesta: Siesta? = null,
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiesta: Timestamp? = null,
    val horaFinSiesta: Timestamp? = null,
    val observacionesSiesta: String = "",
    
    // Datos de necesidades fisiológicas
    val cacaControl: CacaControl? = null,
    val necesidadesFisiologicas: NecesidadesFisiologicas = NecesidadesFisiologicas(),
    val haHechoCaca: Boolean = false,
    val numeroCacas: Int = 0,
    val observacionesCaca: String = "",
    
    // Material a reponer
    val necesitaPanales: Boolean = false,
    val necesitaToallitas: Boolean = false,
    val necesitaRopaCambio: Boolean = false,
    val otroMaterialNecesario: String = "",
    
    // Actividades
    val actividades: Actividad? = null,
    
    // Observaciones generales
    val observaciones: String? = null,
    val observacionesGenerales: String = "",
    
    // Etiquetas - Nueva funcionalidad
    val etiquetas: List<String> = emptyList(), // Lista de IDs de etiquetas
    
    // Control de visualización
    val vistoPorFamiliar: Boolean = false,
    val fechaVisto: Timestamp? = null,
    val visualizadoPorFamiliar: Boolean = false,
    val fechaVisualizacion: Timestamp? = null,
    
    // Metadatos
    val ultimaModificacion: Timestamp = Timestamp.now(),
    val creadoPor: String = "",
    val modificadoPor: String = "",
    
    // Plantilla - Nueva funcionalidad
    val plantillaId: String? = null // ID de la plantilla utilizada (si se usó una)
) {
    /**
     * Función de extensión para convertir el modelo antiguo RegistroDiario a RegistroActividad.
     * 
     * Esta función es útil durante la migración de datos del sistema antiguo al nuevo,
     * asegurando la compatibilidad hacia atrás.
     * 
     * @param registro Registro diario antiguo que se va a convertir
     * @return Un nuevo objeto RegistroActividad con los datos del registro antiguo
     */
    companion object {
        fun fromRegistroDiario(registro: RegistroDiario): RegistroActividad {
            return RegistroActividad(
                id = registro.id,
                alumnoId = registro.alumnoId,
                alumnoNombre = "", // Este dato se perdía en el modelo antiguo
                claseId = registro.claseId,
                fecha = registro.fecha,
                profesorId = registro.profesorId,
                profesorNombre = null, // Este dato se perdía en el modelo antiguo
                primerPlato = registro.primerPlato,
                segundoPlato = registro.segundoPlato,
                postre = registro.postre,
                merienda = registro.merienda,
                observacionesComida = registro.observacionesComida,
                haSiestaSiNo = registro.haSiestaSiNo,
                horaInicioSiesta = registro.horaInicioSiesta,
                horaFinSiesta = registro.horaFinSiesta,
                observacionesSiesta = registro.observacionesSiesta,
                haHechoCaca = registro.haHechoCaca,
                numeroCacas = registro.numeroCacas,
                observacionesCaca = registro.observacionesCaca,
                necesitaPanales = registro.necesitaPanales,
                necesitaToallitas = registro.necesitaToallitas,
                necesitaRopaCambio = registro.necesitaRopaCambio,
                otroMaterialNecesario = registro.otroMaterialNecesario,
                observacionesGenerales = registro.observacionesGenerales,
                visualizadoPorFamiliar = registro.visualizadoPorFamiliar,
                fechaVisualizacion = registro.fechaVisualizacion,
                ultimaModificacion = registro.ultimaModificacion,
                creadoPor = registro.creadoPor,
                modificadoPor = registro.modificadoPor
            )
        }
    }
} 