package com.tfg.umeegunero.feature.admin.viewmodel

import android.content.Context
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.EstadisticasUiState
import com.tfg.umeegunero.data.model.ActividadReciente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * ViewModel para la pantalla de estadísticas del administrador
 */
@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))

    init {
        cargarEstadisticas()
    }

    /**
     * Carga las estadísticas desde Firestore
     */
    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Obtener estadísticas de Firestore
                val centros = firestore.collection("centros").get().await()
                val usuarios = firestore.collection("usuarios").get().await()
                val actividades = firestore.collection("actividades")
                    .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()
                
                // Contar por roles - Corregido para usar el campo correcto
                val profesores = usuarios.documents.filter { 
                    it.getString("tipoUsuario") == "PROFESOR" || 
                    it.getString("rol") == "PROFESOR" 
                }
                val alumnos = usuarios.documents.filter { 
                    it.getString("tipoUsuario") == "ALUMNO" || 
                    it.getString("rol") == "ALUMNO" 
                }
                val familiares = usuarios.documents.filter { 
                    it.getString("tipoUsuario") == "FAMILIAR" || 
                    it.getString("rol") == "FAMILIAR" 
                }
                
                // Obtener nuevos registros en los últimos 7 días
                val fechaLimite = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                
                val nuevosCentros = centros.documents.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosProfesores = profesores.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosAlumnos = alumnos.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosFamiliares = familiares.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                // Total de nuevos registros
                val nuevosRegistros = nuevosProfesores + nuevosAlumnos + nuevosFamiliares

                // Procesar actividades recientes
                val actividadesRecientes = actividades.documents.mapNotNull { doc ->
                    try {
                        ActividadReciente(
                            id = doc.id,
                            tipo = doc.getString("tipo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fecha = doc.getTimestamp("fecha")?.toDate() ?: Date(),
                            usuarioId = doc.getString("usuarioId") ?: "",
                            detalles = doc.getString("detalles") ?: ""
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error al procesar actividad: ${doc.id}")
                        null
                    }
                }

                val fechaActualizacion = dateFormatter.format(Date())
                
                // Actualizar estado UI
                _uiState.update { it.copy(
                    isLoading = false,
                    totalCentros = centros.size(),
                    totalUsuarios = usuarios.size(),
                    totalProfesores = profesores.size,
                    totalAlumnos = alumnos.size,
                    totalFamiliares = familiares.size,
                    nuevosCentros = nuevosCentros,
                    nuevosProfesores = nuevosProfesores,
                    nuevosAlumnos = nuevosAlumnos,
                    nuevosFamiliares = nuevosFamiliares,
                    nuevosRegistros = nuevosRegistros,
                    fechaActualizacion = fechaActualizacion,
                    actividadesRecientes = actividadesRecientes,
                    error = ""
                ) }
                
                Timber.d("Estadísticas actualizadas: ${Date()}")
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar estadísticas")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar estadísticas: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Carga las estadísticas desde Firestore para un período específico
     * @param dias Número de días atrás para considerar como "nuevos registros"
     */
    fun cargarEstadisticasPorPeriodo(dias: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Obtener estadísticas de Firestore
                val centros = firestore.collection("centros").get().await()
                val usuarios = firestore.collection("usuarios").get().await()
                val actividades = firestore.collection("actividades")
                    .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()
                
                // Contar por roles
                val profesores = usuarios.documents.filter { it.getString("rol") == "PROFESOR" }
                val alumnos = usuarios.documents.filter { it.getString("rol") == "ALUMNO" }
                val familiares = usuarios.documents.filter { it.getString("rol") == "FAMILIAR" }
                
                // Obtener nuevos registros en el período especificado
                val fechaLimite = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -dias)
                }.time
                
                val nuevosCentros = centros.documents.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosProfesores = profesores.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosAlumnos = alumnos.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosFamiliares = familiares.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }

                // Procesar actividades recientes
                val actividadesRecientes = actividades.documents.mapNotNull { doc ->
                    try {
                        ActividadReciente(
                            id = doc.id,
                            tipo = doc.getString("tipo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fecha = doc.getTimestamp("fecha")?.toDate() ?: Date(),
                            usuarioId = doc.getString("usuarioId") ?: "",
                            detalles = doc.getString("detalles") ?: ""
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error al procesar actividad: ${doc.id}")
                        null
                    }
                }

                // Formato para el nombre del período
                val nombrePeriodo = when (dias) {
                    7 -> "última semana"
                    30 -> "último mes"
                    90 -> "último trimestre"
                    365 -> "último año"
                    else -> "$dias días"
                }
                
                val fechaActualizacion = "${dateFormatter.format(Date())} (${nombrePeriodo})"
                
                // Actualizar estado UI
                _uiState.update { it.copy(
                    isLoading = false,
                    totalCentros = centros.size(),
                    totalUsuarios = usuarios.size(),
                    totalProfesores = profesores.size,
                    totalAlumnos = alumnos.size,
                    totalFamiliares = familiares.size,
                    nuevosCentros = nuevosCentros,
                    nuevosProfesores = nuevosProfesores,
                    nuevosAlumnos = nuevosAlumnos,
                    nuevosFamiliares = nuevosFamiliares,
                    fechaActualizacion = fechaActualizacion,
                    actividadesRecientes = actividadesRecientes
                ) }
                
                Timber.d("Estadísticas actualizadas para período de $dias días: ${Date()}")
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar estadísticas"
                ) }
                Timber.e(e, "Error al cargar estadísticas para período $dias")
            }
        }
    }

    /**
     * Genera un informe detallado
     */
    fun generarInforme() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Obtener todos los datos necesarios para el informe
                val centros = firestore.collection("centros").get().await()
                val usuarios = firestore.collection("usuarios").get().await()
                val actividades = firestore.collection("actividades")
                    .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                // Procesar datos para el informe
                val informe = StringBuilder().apply {
                    appendLine("Informe de Estadísticas del Sistema")
                    appendLine("Fecha de generación: ${dateFormatter.format(Date())}")
                    appendLine()
                    
                    appendLine("Resumen General:")
                    appendLine("- Total de Centros: ${centros.size()}")
                    appendLine("- Total de Usuarios: ${usuarios.size()}")
                    
                    // Desglose por roles
                    val profesores = usuarios.documents.filter { 
                        it.getString("tipoUsuario") == "PROFESOR" || 
                        it.getString("rol") == "PROFESOR" 
                    }
                    val alumnos = usuarios.documents.filter { 
                        it.getString("tipoUsuario") == "ALUMNO" || 
                        it.getString("rol") == "ALUMNO" 
                    }
                    val familiares = usuarios.documents.filter { 
                        it.getString("tipoUsuario") == "FAMILIAR" || 
                        it.getString("rol") == "FAMILIAR" 
                    }
                    
                    appendLine("\nDistribución de Usuarios:")
                    appendLine("- Profesores: ${profesores.size}")
                    appendLine("- Alumnos: ${alumnos.size}")
                    appendLine("- Familiares: ${familiares.size}")
                    
                    // Actividad reciente
                    appendLine("\nActividad Reciente:")
                    actividades.documents.take(10).forEach { doc ->
                        appendLine("- ${doc.getString("descripcion")} (${doc.getTimestamp("fecha")?.toDate()?.let { dateFormatter.format(it) }})")
                    }
                }.toString()
                
                // Guardar el informe en Firestore con un ID más descriptivo
                val informeId = "informe_${System.currentTimeMillis()}"
                firestore.collection("informes")
                    .document(informeId)
                    .set(mapOf(
                        "contenido" to informe,
                        "fechaGeneracion" to com.google.firebase.Timestamp.now(),
                        "tipo" to "estadisticas",
                        "nombre" to "Informe de Estadísticas"
                    ))
                    .await()
                
                _uiState.update { it.copy(
                    isLoading = false,
                    informeGenerado = true,
                    informeContenido = informe
                ) }
                
                Timber.d("Informe generado y guardado correctamente con ID: $informeId")
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al generar informe"
                ) }
                Timber.e(e, "Error al generar informe")
            }
        }
    }

    /**
     * Descarga el informe generado al almacenamiento del dispositivo.
     * @param context Contexto de Android necesario para acceder al almacenamiento
     */
    fun descargarInforme(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Verificar que haya un informe generado
                if (uiState.value.informeContenido.isEmpty()) {
                    throw IOException("No hay informe generado para descargar")
                }
                
                // Crear nombre de archivo único
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("es", "ES")).format(Date())
                val fileName = "informe_estadisticas_$timeStamp.txt"
                
                var uri: Uri? = null
                var outputStream: OutputStream? = null
                var success = false
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Usar MediaStore para Android 10 y superior
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        
                        val resolver = context.contentResolver
                        uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            outputStream = resolver.openOutputStream(uri)
                            outputStream?.write(uiState.value.informeContenido.toByteArray())
                            success = true
                            Timber.d("Informe guardado en MediaStore: $uri")
                        }
                    } else {
                        // Método tradicional para Android 9 y anteriores
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs()
                        }
                        
                        val file = File(downloadsDir, fileName)
                        outputStream = FileOutputStream(file)
                        outputStream.write(uiState.value.informeContenido.toByteArray())
                        success = true
                        Timber.d("Informe guardado en la carpeta de descargas: ${file.absolutePath}")
                    }
                } finally {
                    outputStream?.close()
                }
                
                if (!success) {
                    throw IOException("No se pudo guardar el archivo")
                }
                
                // Actualizar estado
                _uiState.update { it.copy(
                    isLoading = false,
                    informeDescargado = true,
                    error = ""
                ) }
            } catch (e: Exception) {
                Timber.e(e, "Error al descargar informe")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al descargar informe: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Exporta datos del sistema
     */
    fun exportarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simulamos la exportación
            delay(2000)
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Recarga las estadísticas
     */
    fun recargarEstadisticas() {
        Timber.d("Recargando estadísticas...")
        cargarEstadisticas()
    }
} 