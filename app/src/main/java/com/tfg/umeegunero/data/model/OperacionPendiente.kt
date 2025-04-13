package com.tfg.umeegunero.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tfg.umeegunero.data.local.converter.ConverterEstadoOperacion
import com.tfg.umeegunero.data.local.converter.ConverterMapStringAny
import com.tfg.umeegunero.data.local.converter.ConverterTipoEntidad
import com.tfg.umeegunero.data.local.converter.ConverterTipoOperacion
import java.util.UUID

/**
 * Modelo para operaciones pendientes de sincronización
 */
@Entity(tableName = "operaciones_pendientes")
@TypeConverters(
    ConverterTipoOperacion::class, 
    ConverterTipoEntidad::class, 
    ConverterEstadoOperacion::class,
    ConverterMapStringAny::class
)
data class OperacionPendiente(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    var tipo: Tipo,
    val tipoEntidad: TipoEntidad,
    val entidadId: String,
    val datos: Map<String, Any?>,
    var mensajeError: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    var intentos: Int = 0,
    var estado: Estado = Estado.PENDIENTE
) {
    /**
     * Tipo de operación a realizar
     */
    enum class Tipo {
        CREAR,
        ACTUALIZAR,
        ELIMINAR,
        FIRMA_DIGITAL,
        COMUNICADO,
        CONFIRMACION_LECTURA,
        ARCHIVO
    }

    /**
     * Tipo de entidad sobre la que se realiza la operación
     */
    enum class TipoEntidad {
        COMUNICADO,
        FIRMA_DIGITAL,
        CONFIRMACION_LECTURA,
        USUARIO,
        ARCHIVO
    }

    /**
     * Estado de la operación pendiente
     */
    enum class Estado {
        PENDIENTE,
        EN_PROCESO,
        COMPLETADA,
        ERROR
    }

    companion object {
        fun crearFirmaDigital(
            firmaBase64: String,
            comunicadoId: String,
            usuarioId: String
        ): OperacionPendiente {
            val datos = mapOf(
                "firmaBase64" to firmaBase64,
                "comunicadoId" to comunicadoId,
                "usuarioId" to usuarioId,
                "timestamp" to System.currentTimeMillis()
            )
            
            return OperacionPendiente(
                tipo = Tipo.FIRMA_DIGITAL,
                tipoEntidad = TipoEntidad.FIRMA_DIGITAL,
                entidadId = comunicadoId,
                datos = datos
            )
        }

        fun crearComunicado(
            comunicadoId: String,
            accion: String,
            datosAdicionales: Map<String, Any> = emptyMap()
        ): OperacionPendiente {
            val datos = mutableMapOf<String, Any>(
                "comunicadoId" to comunicadoId,
                "accion" to accion
            )
            datos.putAll(datosAdicionales)
            
            return OperacionPendiente(
                tipo = Tipo.COMUNICADO,
                tipoEntidad = TipoEntidad.COMUNICADO,
                entidadId = comunicadoId,
                datos = datos
            )
        }
        
        fun crearConfirmacionLectura(
            comunicadoId: String,
            usuarioId: String
        ): OperacionPendiente {
            val datos = mapOf(
                "comunicadoId" to comunicadoId,
                "usuarioId" to usuarioId
            )
            
            return OperacionPendiente(
                tipo = Tipo.CONFIRMACION_LECTURA,
                tipoEntidad = TipoEntidad.CONFIRMACION_LECTURA,
                entidadId = comunicadoId,
                datos = datos
            )
        }

        fun crearArchivo(
            archivoId: String,
            rutaLocal: String,
            tipoArchivo: String,
            metadatos: Map<String, Any> = emptyMap()
        ): OperacionPendiente {
            val datos = mutableMapOf<String, Any>(
                "archivoId" to archivoId,
                "rutaLocal" to rutaLocal,
                "tipoArchivo" to tipoArchivo
            )
            datos.putAll(metadatos)
            
            return OperacionPendiente(
                tipo = Tipo.ARCHIVO,
                tipoEntidad = TipoEntidad.ARCHIVO,
                entidadId = archivoId,
                datos = datos
            )
        }
    }
} 