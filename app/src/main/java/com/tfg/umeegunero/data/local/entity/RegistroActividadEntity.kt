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
    val comidasJson: String = "", // Comidas almacenado como JSON
    val observacionesComida: String = "",
    
    // Datos de siesta
    val haSiestaSiNo: Boolean = false,
    val horaInicioSiesta: String = "", // Formato HH:mm
    val horaFinSiesta: String = "", // Formato HH:mm
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
    val observacionesGenerales: String = "",
    
    // Etiquetas - Nueva funcionalidad
    val etiquetas: List<String> = emptyList(),
    
    // Control de visualización
    val vistoPorFamiliar: Boolean = false,
    val fechaVistoTimestamp: Long? = null,
    val vistoPorJson: String = "{}", // Map<String, Boolean> como JSON
    
    // Metadatos
    val ultimaModificacionTimestamp: Long = Date().time,
    val creadoPor: String = "",
    val modificadoPor: String = "",
    
    // Actividades
    val actividades: List<String> = emptyList(),
    
    // Plantilla - Nueva funcionalidad
    val plantillaId: String? = null,
    
    // Eliminación lógica
    val eliminado: Boolean = false,
    
    // Campo para sincronización - Indica si el registro está sincronizado con el servidor
    val isSynced: Boolean = false
) {
    companion object {
        private val gson = Gson()
        
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
                comidasJson = gson.toJson(registro.comidas),
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
                etiquetas = registro.etiquetas,
                vistoPorFamiliar = registro.vistoPorFamiliar,
                fechaVistoTimestamp = registro.fechaVisto?.seconds?.times(1000),
                vistoPorJson = gson.toJson(registro.vistoPor),
                ultimaModificacionTimestamp = registro.ultimaModificacion.seconds * 1000,
                creadoPor = registro.creadoPor,
                modificadoPor = registro.modificadoPor,
                actividades = registro.actividades,
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
        val comidas = try {
            gson.fromJson(comidasJson, Comidas::class.java) ?: Comidas()
        } catch (e: Exception) {
            Comidas()
        }
        
        val vistoPor = try {
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            gson.fromJson<Map<String, Boolean>>(vistoPorJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap<String, Boolean>()
        }
        
        return RegistroActividad(
            id = id,
            alumnoId = alumnoId,
            alumnoNombre = alumnoNombre,
            claseId = claseId,
            fecha = Timestamp(fechaTimestamp / 1000, 0),
            profesorId = profesorId ?: "",
            profesorNombre = profesorNombre,
            comidas = comidas,
            observacionesComida = observacionesComida,
            haSiestaSiNo = haSiestaSiNo,
            horaInicioSiesta = horaInicioSiesta,
            horaFinSiesta = horaFinSiesta,
            observacionesSiesta = observacionesSiesta,
            haHechoCaca = haHechoCaca,
            numeroCacas = numeroCacas,
            observacionesCaca = observacionesCaca,
            necesitaPanales = necesitaPanales,
            necesitaToallitas = necesitaToallitas,
            necesitaRopaCambio = necesitaRopaCambio,
            otroMaterialNecesario = otroMaterialNecesario,
            observacionesGenerales = observacionesGenerales,
            etiquetas = etiquetas,
            vistoPorFamiliar = vistoPorFamiliar,
            fechaVisto = fechaVistoTimestamp?.let { Timestamp(it / 1000, 0) },
            vistoPor = vistoPor,
            ultimaModificacion = Timestamp(ultimaModificacionTimestamp / 1000, 0),
            creadoPor = creadoPor,
            modificadoPor = modificadoPor,
            actividades = actividades,
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