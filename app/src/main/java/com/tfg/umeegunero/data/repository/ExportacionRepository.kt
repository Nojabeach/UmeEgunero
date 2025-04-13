package com.tfg.umeegunero.data.repository

import android.net.Uri
import com.tfg.umeegunero.data.model.Resultado
import java.io.File
import java.util.Date
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones para exportar datos del sistema a diferentes formatos.
 * Permite exportar información como registros de actividades, comunicados y alumnos.
 */
interface ExportacionRepository {

    /**
     * Tipos de contenido que pueden ser exportados.
     */
    enum class TipoContenido {
        REGISTRO_ACTIVIDADES,
        COMUNICADOS,
        ALUMNOS,
        ASISTENCIA
    }

    /**
     * Formatos de exportación disponibles.
     */
    enum class FormatoExportacion {
        PDF,
        CSV,
        EXCEL
    }

    /**
     * Exporta datos según el tipo de contenido y formato especificados.
     *
     * @param tipoContenido El tipo de contenido a exportar
     * @param formato El formato de exportación deseado
     * @param fechaInicio Fecha de inicio para filtrar datos (opcional)
     * @param fechaFin Fecha de fin para filtrar datos (opcional)
     * @param idClase Identificador de la clase para filtrar datos (opcional)
     * @return Flow con resultado de la operación que contiene el archivo exportado
     */
    suspend fun exportarDatos(
        tipoContenido: TipoContenido,
        formato: FormatoExportacion,
        fechaInicio: Date? = null,
        fechaFin: Date? = null,
        idClase: String? = null
    ): Flow<Resultado<File>>
    
    /**
     * Verifica si hay datos disponibles para exportar según los criterios especificados.
     *
     * @param tipoContenido El tipo de contenido a verificar
     * @param fechaInicio Fecha de inicio para filtrar datos (opcional)
     * @param fechaFin Fecha de fin para filtrar datos (opcional)
     * @param idClase Identificador de la clase para filtrar datos (opcional)
     * @return Flow con resultado indicando si hay datos disponibles
     */
    suspend fun hayDatosParaExportar(
        tipoContenido: TipoContenido,
        fechaInicio: Date? = null,
        fechaFin: Date? = null,
        idClase: String? = null
    ): Flow<Resultado<Boolean>>
    
    /**
     * Comparte el archivo exportado utilizando el intento de compartir del sistema.
     *
     * @param archivo El archivo a compartir
     * @param tipoContenido Tipo de contenido para incluir en la descripción
     * @return Flow con resultado indicando si la operación fue exitosa
     */
    fun compartirArchivo(
        archivo: File,
        tipoContenido: TipoContenido
    ): Flow<Resultado<Boolean>>
} 