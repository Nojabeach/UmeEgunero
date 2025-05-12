package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tfg.umeegunero.data.model.CacaControl
import com.tfg.umeegunero.data.model.Comidas
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.NecesidadesFisiologicas
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Siesta
import java.util.Date

/**
 * Entidad Room para el almacenamiento local de registros de actividad.
 * 
 * Esta entidad representa la versión local (persistente) del modelo RegistroActividad
 * para permitir el funcionamiento offline de la aplicación. Incluye soporte para
 * etiquetas personalizadas y uso de plantillas.
 * 
 * @author Maitane (Estudiante 2º DAM)
 * @version 1.1
 */
@Entity(tableName = "registros_actividad")
@TypeConverters(StringListConverter::class)
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
    
    // Etiquetas - Nueva funcionalidad
    val etiquetas: List<String> = emptyList(),
    
    // Control de visualización
    val vistoPorFamiliar: Boolean = false,
    val fechaVistoTimestamp: Long? = null,
    val visualizadoPorFamiliar: Boolean = false,
    val fechaVisualizacionTimestamp: Long? = null,
    
    // Metadatos
    val ultimaModificacionTimestamp: Long = Date().time,
    val creadoPor: String = "",
    val modificadoPor: String = "",
    
    // Plantilla - Nueva funcionalidad
    val plantillaId: String? = null,
    
    // Campo para sincronización - Indica si el registro está sincronizado con el servidor
    val isSynced: Boolean = false
) {
    companion object {
        /**
         * Convierte un modelo RegistroActividad a una entidad RegistroActividadEntity
         * 
         * @param registro Modelo de dominio
         * @param isSynced Estado de sincronización
         * @return Entidad para Room
         */
        fun fromRegistroActividad(registro: RegistroActividad, isSynced: Boolean = false): RegistroActividadEntity {
            return RegistroActividadEntity(
                id = registro.id,
                alumnoId = registro.alumnoId,
                alumnoNombre = registro.alumnoNombre,
                claseId = registro.claseId,
                fechaTimestamp = registro.fecha.seconds * 1000,
                profesorId = registro.profesorId,
                profesorNombre = registro.profesorNombre,
                primerPlato = registro.comidas.primerPlato.estadoComida.name,
                segundoPlato = registro.comidas.segundoPlato.estadoComida.name,
                postre = registro.comidas.postre.estadoComida.name,
                merienda = registro.comidas.primerPlato.estadoComida.name,
                observacionesComida = registro.observacionesComida,
                haSiestaSiNo = registro.siesta != null,
                horaInicioSiestaTimestamp = registro.siesta?.inicio?.seconds?.times(1000) ?: registro.horaInicioSiesta?.seconds?.times(1000),
                horaFinSiestaTimestamp = registro.siesta?.fin?.seconds?.times(1000) ?: registro.horaFinSiesta?.seconds?.times(1000),
                observacionesSiesta = registro.siesta?.observaciones ?: registro.observacionesSiesta,
                haHechoCaca = registro.necesidadesFisiologicas.caca,
                numeroCacas = if (registro.necesidadesFisiologicas.caca) 1 else 0,
                observacionesCaca = registro.necesidadesFisiologicas.observaciones,
                necesitaPanales = registro.necesitaPanales,
                necesitaToallitas = registro.necesitaToallitas,
                necesitaRopaCambio = registro.necesitaRopaCambio,
                otroMaterialNecesario = registro.otroMaterialNecesario,
                observaciones = registro.observaciones,
                observacionesGenerales = registro.observacionesGenerales,
                etiquetas = registro.etiquetas,
                vistoPorFamiliar = registro.vistoPorFamiliar,
                fechaVistoTimestamp = registro.fechaVisto?.seconds?.times(1000),
                visualizadoPorFamiliar = registro.visualizadoPorFamiliar,
                fechaVisualizacionTimestamp = registro.fechaVisualizacion?.seconds?.times(1000),
                ultimaModificacionTimestamp = registro.ultimaModificacion.seconds * 1000,
                creadoPor = registro.creadoPor,
                modificadoPor = registro.modificadoPor,
                plantillaId = registro.plantillaId,
                isSynced = isSynced
            )
        }
    }
    
    /**
     * Convierte la entidad a un modelo de dominio RegistroActividad
     * 
     * @return Modelo de dominio
     */
    fun toRegistroActividad(): RegistroActividad {
        return RegistroActividad(
            id = id,
            alumnoId = alumnoId,
            alumnoNombre = alumnoNombre,
            claseId = claseId,
            fecha = Timestamp(fechaTimestamp / 1000, 0),
            profesorId = profesorId,
            profesorNombre = profesorNombre,
            comidas = Comidas(
                primerPlato = com.tfg.umeegunero.data.model.Plato(
                    "", EstadoComida.valueOf(primerPlato)
                ),
                segundoPlato = com.tfg.umeegunero.data.model.Plato(
                    "", EstadoComida.valueOf(segundoPlato)
                ),
                postre = com.tfg.umeegunero.data.model.Plato(
                    "", EstadoComida.valueOf(postre)
                )
            ),
            siesta = Siesta(
                duracion = 0,
                inicio = horaInicioSiestaTimestamp?.let { Timestamp(it / 1000, 0) },
                fin = horaFinSiestaTimestamp?.let { Timestamp(it / 1000, 0) },
                observaciones = observacionesSiesta
            ),
            necesidadesFisiologicas = NecesidadesFisiologicas(
                pipi = true,
                caca = haHechoCaca,
                observaciones = observacionesCaca
            ),
            necesitaPanales = necesitaPanales,
            necesitaToallitas = necesitaToallitas,
            necesitaRopaCambio = necesitaRopaCambio,
            otroMaterialNecesario = otroMaterialNecesario,
            observaciones = observaciones,
            observacionesGenerales = observacionesGenerales,
            etiquetas = etiquetas,
            vistoPorFamiliar = vistoPorFamiliar,
            fechaVisto = fechaVistoTimestamp?.let { Timestamp(it / 1000, 0) },
            visualizadoPorFamiliar = visualizadoPorFamiliar,
            fechaVisualizacion = fechaVisualizacionTimestamp?.let { Timestamp(it / 1000, 0) },
            ultimaModificacion = Timestamp(ultimaModificacionTimestamp / 1000, 0),
            creadoPor = creadoPor,
            modificadoPor = modificadoPor,
            plantillaId = plantillaId
        )
    }
}

/**
 * Conversor para listas de strings en Room
 */
class StringListConverter {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }
} 