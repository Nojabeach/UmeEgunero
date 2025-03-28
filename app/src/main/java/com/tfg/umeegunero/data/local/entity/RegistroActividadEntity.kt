package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import java.util.Date

/**
 * Entidad Room para el almacenamiento local de registros de actividad.
 * 
 * Esta entidad representa la versión local (persistente) del modelo RegistroActividad
 * para permitir el funcionamiento offline de la aplicación.
 * 
 * @author Estudiante 2º DAM
 */
@Entity(tableName = "registros_actividad")
data class RegistroActividadEntity(
    @PrimaryKey
    val id: String,
    val alumnoId: String,
    val alumnoNombre: String = "",
    val claseId: String = "",
    val fechaTimestamp: Long, // Timestamp de la fecha almacenado como Long
    
    // Datos del profesor
    val profesorId: String? = null,
    val profesorNombre: String? = null,
    
    // Datos de comidas
    val primerPlato: String = EstadoComida.NO_SERVIDO.name,
    val segundoPlato: String = EstadoComida.NO_SERVIDO.name,
    val postre: String = EstadoComida.NO_SERVIDO.name,
    val merienda: String = EstadoComida.NO_SERVIDO.name,
    val observacionesComida: String = "",
    
    // Datos de siesta
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiestaTimestamp: Long? = null, // Timestamp almacenado como Long
    val horaFinSiestaTimestamp: Long? = null,
    val observacionesSiesta: String = "",
    
    // Datos de necesidades fisiológicas
    val haHechoCaca: Boolean = false,
    val numeroCacas: Int = 0,
    val observacionesCaca: String = "",
    
    // Material a reponer
    val necesitaPanales: Boolean = false,
    val necesitaToallitas: Boolean = false,
    val necesitaRopaCambio: Boolean = false,
    val otroMaterialNecesario: String = "",
    
    // Observaciones generales
    val observaciones: String? = null,
    val observacionesGenerales: String = "",
    
    // Control de visualización
    val vistoPorFamiliar: Boolean = false,
    val fechaVistoTimestamp: Long? = null,
    val visualizadoPorFamiliar: Boolean = false,
    val fechaVisualizacionTimestamp: Long? = null,
    
    // Metadatos
    val ultimaModificacionTimestamp: Long = Date().time,
    val creadoPor: String = "",
    val modificadoPor: String = "",
    
    // Campo para sincronización - Indica si el registro está sincronizado con el servidor
    val isSynced: Boolean = false
) {
    /**
     * Convierte la entidad Room a un modelo de dominio RegistroActividad
     */
    fun toRegistroActividad(): RegistroActividad {
        return RegistroActividad(
            id = id,
            alumnoId = alumnoId,
            alumnoNombre = alumnoNombre,
            claseId = claseId,
            fecha = Timestamp(Date(fechaTimestamp)),
            profesorId = profesorId,
            profesorNombre = profesorNombre,
            primerPlato = try { EstadoComida.valueOf(primerPlato) } catch (e: Exception) { EstadoComida.NO_SERVIDO },
            segundoPlato = try { EstadoComida.valueOf(segundoPlato) } catch (e: Exception) { EstadoComida.NO_SERVIDO },
            postre = try { EstadoComida.valueOf(postre) } catch (e: Exception) { EstadoComida.NO_SERVIDO },
            merienda = try { EstadoComida.valueOf(merienda) } catch (e: Exception) { EstadoComida.NO_SERVIDO },
            observacionesComida = observacionesComida,
            haSiestaSiNo = haSiestaSiNo,
            horaInicioSiesta = horaInicioSiestaTimestamp?.let { Timestamp(Date(it)) },
            horaFinSiesta = horaFinSiestaTimestamp?.let { Timestamp(Date(it)) },
            observacionesSiesta = observacionesSiesta,
            haHechoCaca = haHechoCaca,
            numeroCacas = numeroCacas,
            observacionesCaca = observacionesCaca,
            necesitaPanales = necesitaPanales,
            necesitaToallitas = necesitaToallitas,
            necesitaRopaCambio = necesitaRopaCambio,
            otroMaterialNecesario = otroMaterialNecesario,
            observaciones = observaciones,
            observacionesGenerales = observacionesGenerales,
            vistoPorFamiliar = vistoPorFamiliar,
            fechaVisto = fechaVistoTimestamp?.let { Timestamp(Date(it)) },
            visualizadoPorFamiliar = visualizadoPorFamiliar,
            fechaVisualizacion = fechaVisualizacionTimestamp?.let { Timestamp(Date(it)) },
            ultimaModificacion = Timestamp(Date(ultimaModificacionTimestamp)),
            creadoPor = creadoPor,
            modificadoPor = modificadoPor
        )
    }

    companion object {
        /**
         * Crea una entidad Room a partir de un modelo de dominio RegistroActividad
         * 
         * @param registro El registro de actividad del dominio
         * @param isSynced Indica si está sincronizado con el servidor
         * @return La entidad Room correspondiente
         */
        fun fromRegistroActividad(registro: RegistroActividad, isSynced: Boolean = true): RegistroActividadEntity {
            return RegistroActividadEntity(
                id = registro.id,
                alumnoId = registro.alumnoId,
                alumnoNombre = registro.alumnoNombre,
                claseId = registro.claseId,
                fechaTimestamp = registro.fecha.toDate().time,
                profesorId = registro.profesorId,
                profesorNombre = registro.profesorNombre,
                primerPlato = registro.primerPlato.name,
                segundoPlato = registro.segundoPlato.name,
                postre = registro.postre.name,
                merienda = registro.merienda.name,
                observacionesComida = registro.observacionesComida,
                haSiestaSiNo = registro.haSiestaSiNo,
                horaInicioSiestaTimestamp = registro.horaInicioSiesta?.toDate()?.time,
                horaFinSiestaTimestamp = registro.horaFinSiesta?.toDate()?.time,
                observacionesSiesta = registro.observacionesSiesta,
                haHechoCaca = registro.haHechoCaca,
                numeroCacas = registro.numeroCacas,
                observacionesCaca = registro.observacionesCaca,
                necesitaPanales = registro.necesitaPanales,
                necesitaToallitas = registro.necesitaToallitas,
                necesitaRopaCambio = registro.necesitaRopaCambio,
                otroMaterialNecesario = registro.otroMaterialNecesario,
                observaciones = registro.observaciones,
                observacionesGenerales = registro.observacionesGenerales,
                vistoPorFamiliar = registro.vistoPorFamiliar,
                fechaVistoTimestamp = registro.fechaVisto?.toDate()?.time,
                visualizadoPorFamiliar = registro.visualizadoPorFamiliar,
                fechaVisualizacionTimestamp = registro.fechaVisualizacion?.toDate()?.time,
                ultimaModificacionTimestamp = registro.ultimaModificacion.toDate().time,
                creadoPor = registro.creadoPor,
                modificadoPor = registro.modificadoPor,
                isSynced = isSynced
            )
        }
    }
} 