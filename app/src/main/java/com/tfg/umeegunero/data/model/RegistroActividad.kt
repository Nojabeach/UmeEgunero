package com.tfg.umeegunero.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Modelo que representa un registro de actividad diaria de un alumno.
 * 
 * Este modelo unifica las funcionalidades de los antiguos modelos
 * RegistroDiario y RegistroActividad para evitar duplicidad y
 * simplificar la estructura de datos.
 * 
 * @author Estudiante 2º DAM
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
    
    // Control de visualización
    val vistoPorFamiliar: Boolean = false,
    val fechaVisto: Timestamp? = null,
    val visualizadoPorFamiliar: Boolean = false,
    val fechaVisualizacion: Timestamp? = null,
    
    // Metadatos
    val ultimaModificacion: Timestamp = Timestamp.now(),
    val creadoPor: String = "",
    val modificadoPor: String = ""
) {
    /**
     * Función de extensión para convertir el modelo antiguo RegistroDiario a RegistroActividad
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