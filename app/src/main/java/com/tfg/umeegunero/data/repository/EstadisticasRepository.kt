package com.tfg.umeegunero.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.ActividadReciente
import com.tfg.umeegunero.feature.admin.viewmodel.AccesoPorCentro
import com.tfg.umeegunero.util.PdfExporter
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repositorio para gestionar las estadísticas generales de la aplicación en Firestore.
 *
 * Esta clase permite obtener información básica sobre el uso de la aplicación, como el número de usuarios activos,
 * sesiones promedio y tiempo promedio de uso, si estos datos siguen siendo relevantes para la app.
 *
 * Todas las funciones relacionadas con reportes de uso y características han sido eliminadas.
 */
@Singleton
class EstadisticasRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ESTADISTICAS = "estadisticas"
        private const val DOC_USO_APLICACION = "uso_aplicacion"
    }

    // Flow para el contenido del informe
    private val _informeContenido = MutableStateFlow("")
    val informeContenido: StateFlow<String> = _informeContenido.asStateFlow()
    
    // Formateador de fechas para el informe
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))

    /**
     * Obtiene las estadísticas generales de uso de la aplicación
     *
     * @param periodo Periodo de tiempo para el que se quieren obtener las estadísticas
     * @return [Result] con los datos de usuarios activos, sesiones promedio y tiempo promedio
     */
    suspend fun obtenerEstadisticasUso(periodo: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val estadisticasRef = firestore.collection(COLLECTION_ESTADISTICAS)
                .document(DOC_USO_APLICACION)

            val estadisticasDoc = estadisticasRef.get().await()

            if (estadisticasDoc.exists()) {
                val resultado = mutableMapOf<String, Any>()

                // Obtener estadísticas generales según el periodo
                val campoUsuarios = when (periodo) {
                    "Última semana" -> "usuarios_activos_semana"
                    "Último trimestre" -> "usuarios_activos_trimestre"
                    "Último año" -> "usuarios_activos_anio"
                    else -> "usuarios_activos" // Por defecto último mes
                }

                val campoSesiones = when (periodo) {
                    "Última semana" -> "sesiones_promedio_semana"
                    "Último trimestre" -> "sesiones_promedio_trimestre"
                    "Último año" -> "sesiones_promedio_anio"
                    else -> "sesiones_promedio" // Por defecto último mes
                }

                val campoTiempo = when (periodo) {
                    "Última semana" -> "tiempo_promedio_sesion_semana"
                    "Último trimestre" -> "tiempo_promedio_sesion_trimestre"
                    "Último año" -> "tiempo_promedio_sesion_anio"
                    else -> "tiempo_promedio_sesion" // Por defecto último mes
                }

                resultado["usuariosActivos"] = estadisticasDoc.getLong(campoUsuarios) ?: 0
                resultado["sesionesPromedio"] = estadisticasDoc.getDouble(campoSesiones) ?: 0.0
                resultado["tiempoPromedioSesion"] = estadisticasDoc.getString(campoTiempo) ?: "0 min"
                resultado["ultimaActualizacion"] = estadisticasDoc.getTimestamp("ultima_actualizacion") ?: Timestamp.now()

                return@withContext Result.Success(resultado)
            } else {
                return@withContext Result.Error(Exception("No se encontraron estadísticas de uso"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener estadísticas de uso")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Genera un informe con las estadísticas proporcionadas
     */
    suspend fun generarInforme(
        totalUsuarios: Int,
        totalCentros: Int,
        totalProfesores: Int,
        totalAlumnos: Int,
        totalFamiliares: Int,
        totalAdministradores: Int,
        accesosPorCentro: List<AccesoPorCentro>,
        actividadesRecientes: List<ActividadReciente>
    ): Boolean {
        return try {
            // Procesar datos para el informe
            val informe = StringBuilder().apply {
                appendLine("=====================================================")
                appendLine("   INFORME DE ESTADÍSTICAS DEL SISTEMA UME EGUNERO   ")
                appendLine("=====================================================")
                appendLine("Fecha de generación: ${dateFormatter.format(Date())}")
                appendLine()
                
                appendLine("1. RESUMEN GENERAL:")
                appendLine("------------------")
                appendLine("• Total de Centros: $totalCentros")
                appendLine("• Total de Usuarios: $totalUsuarios")
                appendLine()
                
                appendLine("2. DISTRIBUCIÓN DE USUARIOS:")
                appendLine("--------------------------")
                appendLine("• Profesores: $totalProfesores (${calcularPorcentaje(totalProfesores, totalUsuarios)}%)")
                appendLine("• Alumnos: $totalAlumnos (${calcularPorcentaje(totalAlumnos, totalUsuarios)}%)")
                appendLine("• Familiares: $totalFamiliares (${calcularPorcentaje(totalFamiliares, totalUsuarios)}%)")
                appendLine("• Administradores: $totalAdministradores (${calcularPorcentaje(totalAdministradores, totalUsuarios)}%)")
                appendLine()
                
                appendLine("3. ACCESOS POR CENTRO:")
                appendLine("---------------------")
                accesosPorCentro.forEach { centro ->
                    val nombreMostrado = when {
                        centro.nombreCentro.isBlank() -> "UmeEgunero Admin"
                        centro.nombreCentro.equals("Centro", ignoreCase = true) -> "UmeEgunero Admin"
                        else -> centro.nombreCentro
                    }
                    appendLine("• $nombreMostrado: ${centro.numeroAccesos} accesos (${centro.porcentajeUso.toInt()}%)")
                }
                appendLine()
                
                appendLine("4. ACTIVIDADES RECIENTES:")
                appendLine("------------------------")
                actividadesRecientes.take(15).forEach { actividad ->
                    val fechaFormateada = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(actividad.fecha)
                    appendLine("• [$fechaFormateada] ${actividad.tipo}: ${actividad.descripcion}")
                }
                appendLine()
                
                appendLine("5. CONCLUSIONES Y RECOMENDACIONES:")
                appendLine("----------------------------------")
                
                // Generar algunas conclusiones básicas basadas en los datos
                val ratioFamiliarAlumno = if (totalAlumnos > 0) totalFamiliares.toFloat() / totalAlumnos else 0f
                
                appendLine("• Ratio familiar/alumno: ${String.format("%.2f", ratioFamiliarAlumno)} familiares por alumno")
                
                if (ratioFamiliarAlumno < 1.0f) {
                    appendLine("  → Se recomienda promover la participación de más familiares en la plataforma")
                } else if (ratioFamiliarAlumno > 1.5f) {
                    appendLine("  → Excelente participación familiar, se recomienda mantener las estrategias actuales")
                }
                
                appendLine()
                appendLine("=====================================================")
                appendLine("                FIN DEL INFORME                     ")
                appendLine("=====================================================")
            }.toString()
            
            // Guardar el informe en Firestore con un ID más descriptivo
            val informeId = "informe_${System.currentTimeMillis()}"
            firestore.collection("informes")
                .document(informeId)
                .set(mapOf(
                    "contenido" to informe,
                    "fechaGeneracion" to com.google.firebase.Timestamp.now(),
                    "tipo" to "estadisticas",
                    "nombre" to "Informe de Estadísticas",
                    "centros" to totalCentros,
                    "usuarios" to totalUsuarios,
                    "profesores" to totalProfesores,
                    "alumnos" to totalAlumnos,
                    "familiares" to totalFamiliares,
                    "administradores" to totalAdministradores
                ))
                .await()
            
            _informeContenido.update { informe }
            Timber.d("Informe generado y guardado correctamente con ID: $informeId")
            
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al generar informe")
            false
        }
    }

    /**
     * Calcula el porcentaje de un valor sobre un total
     */
    private fun calcularPorcentaje(valor: Int, total: Int): Int {
        return if (total > 0) ((valor.toFloat() / total) * 100).toInt() else 0
    }

    /**
     * Exporta el informe como un archivo PDF y lo guarda en el almacenamiento del dispositivo
     */
    suspend fun exportarInformeComoPDF(context: Context, contenido: String, nombreArchivo: String): Boolean {
        return try {
            // Verificar si podemos usar el componente de exportación PDF
            val pdfExporter = PdfExporter(context)
            val exported = pdfExporter.createPdfFromText(
                contenido = contenido,
                fileName = nombreArchivo,
                title = "INFORME DE ESTADÍSTICAS DEL SISTEMA UME EGUNERO"
            )
            
            if (!exported) {
                // Método alternativo: guardar como archivo de texto
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val txtFileName = "informe_estadisticas_$timeStamp.txt"
                
                var outputStream: OutputStream? = null
                var success = false
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Usar MediaStore para Android 10 y superior
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, txtFileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        
                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            outputStream = resolver.openOutputStream(uri)
                            outputStream?.write(contenido.toByteArray())
                            success = true
                            Timber.d("Informe de texto guardado en MediaStore: $uri")
                        }
                    } else {
                        // Método tradicional para Android 9 y anteriores
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs()
                        }
                        
                        val file = File(downloadsDir, txtFileName)
                        outputStream = FileOutputStream(file)
                        outputStream.write(contenido.toByteArray())
                        success = true
                        Timber.d("Informe de texto guardado en la carpeta de descargas: ${file.absolutePath}")
                    }
                } finally {
                    outputStream?.close()
                }
                
                success
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al exportar informe como PDF")
            false
        }
    }

    /**
     * Obtiene el contenido del último informe generado
     */
    fun getInformeContenido(): String {
        return _informeContenido.value
    }
} 