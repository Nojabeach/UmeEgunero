/**
 * Módulo de estadísticas del sistema UmeEgunero.
 * 
 * Este módulo implementa la gestión y análisis de estadísticas
 * del sistema para los administradores.
 */
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
import java.util.concurrent.TimeUnit

/**
 * Modelo para datos estadísticos de gráficos temporales 
 */
data class DatosTemporales(
    val etiquetas: List<String> = emptyList(),
    val valores: List<Int> = emptyList()
)

/**
 * Modelo para datos estadísticos de actividad por tipo de usuario
 */
data class ActividadPorUsuario(
    val profesores: DatosTemporales = DatosTemporales(),
    val alumnos: DatosTemporales = DatosTemporales(),
    val familiares: DatosTemporales = DatosTemporales(),
    val accesosRecentesPorCentro: DatosTemporales = DatosTemporales()
)

/**
 * Modelo para accesos por centro
 */
data class AccesoPorCentro(
    val centroId: String,
    val nombreCentro: String,
    val numeroAccesos: Int,
    val porcentajeUso: Float,
    val ultimaActividad: Date? = null
)

/**
 * Datos para mostrar el porcentaje de uso de un centro
 */
data class PorcentajeUso(
    val centroId: String,
    val nombreCentro: String,
    val porcentaje: Double,
    val totalUsuarios: Int,
    val usuariosActivos: Int
)

/**
 * ViewModel para la pantalla de estadísticas del administrador
 */
@HiltViewModel
class EstadisticasViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()
    
    private val _actividadPorUsuario = MutableStateFlow(ActividadPorUsuario())
    val actividadPorUsuario: StateFlow<ActividadPorUsuario> = _actividadPorUsuario.asStateFlow()
    
    private val _accesosPorCentro = MutableStateFlow<List<AccesoPorCentro>>(emptyList())
    val accesosPorCentro: StateFlow<List<AccesoPorCentro>> = _accesosPorCentro.asStateFlow()
    
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("es", "ES"))

    init {
        cargarEstadisticas()
    }

    /**
     * Carga las estadísticas del sistema
     */
    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Centros
                val centrosResult = firestore.collection("centros").get().await()
                val totalCentros = centrosResult.size()
                val nuevosCentros = centrosResult.documents.count { doc ->
                    val fechaCreacion = doc.getTimestamp("fechaCreacion")
                    if (fechaCreacion != null) {
                        val fechaCreacionDate = fechaCreacion.toDate()
                        val diffDays = TimeUnit.MILLISECONDS.toDays(Date().time - fechaCreacionDate.time)
                        diffDays <= 30 // Centros creados en los últimos 30 días
                    } else {
                        false
                    }
                }
                
                // Usuarios
                val usuariosResult = firestore.collection("usuarios").get().await()
                val totalUsuarios = usuariosResult.size()
                
                // Contar usuarios por tipo según sus perfiles
                var totalProfesores = 0
                var totalAlumnos = 0
                var totalFamiliares = 0
                var totalAdministradores = 0
                var totalAdministradoresApp = 0
                var totalAdministradoresCentro = 0
                
                // Contadores para nuevos usuarios (últimos 30 días)
                var nuevosProfesores = 0
                var nuevosAlumnos = 0
                var nuevosFamiliares = 0
                var nuevosAdministradores = 0
                
                val fechaLimite30Dias = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.time
                
                // Obtener información de últimos accesos para la actividad reciente
                val accesosPorCentro = mutableMapOf<String, Int>()
                val actividadReciente = mutableListOf<ActividadReciente>()
                val centrosUltimaActividad = mutableMapOf<String, Date>()
                val ahora = Date()
                
                for (doc in usuariosResult.documents) {
                    val perfiles = doc.get("perfiles") as? List<Map<String, Any>> ?: emptyList()
                    val fechaRegistro = doc.getTimestamp("fechaRegistro")?.toDate()
                    val esNuevoUsuario = fechaRegistro != null && fechaRegistro.after(fechaLimite30Dias)
                    
                    // Contar por tipo de usuario en perfiles
                    for (perfil in perfiles) {
                        when (perfil["tipo"] as? String) {
                            "PROFESOR" -> {
                                totalProfesores++
                                if (esNuevoUsuario) nuevosProfesores++
                            }
                            "ALUMNO" -> {
                                totalAlumnos++
                                if (esNuevoUsuario) nuevosAlumnos++
                            }
                            "FAMILIAR" -> {
                                totalFamiliares++
                                if (esNuevoUsuario) nuevosFamiliares++
                            }
                            "ADMIN_CENTRO" -> {
                                totalAdministradores++
                                totalAdministradoresCentro++
                                if (esNuevoUsuario) nuevosAdministradores++
                            }
                            "ADMIN_APP" -> {
                                totalAdministradores++
                                totalAdministradoresApp++
                                if (esNuevoUsuario) nuevosAdministradores++
                            }
                        }
                        
                        // Contabilizar accesos por centro
                        val centroId = perfil["centroId"] as? String
                        if (centroId != null) {
                            val ultimoAcceso = doc.getTimestamp("ultimoAcceso")
                            if (ultimoAcceso != null) {
                                // Solo contamos accesos en los últimos 30 días
                                val ultimoAccesoDate = ultimoAcceso.toDate()
                                val diffDays = TimeUnit.MILLISECONDS.toDays(ahora.time - ultimoAccesoDate.time)
                                if (diffDays <= 30) {
                                    accesosPorCentro[centroId] = accesosPorCentro.getOrDefault(centroId, 0) + 1
                                    
                                    // Actualizar fecha de última actividad del centro si es más reciente
                                    if (!centrosUltimaActividad.containsKey(centroId) || 
                                        centrosUltimaActividad[centroId]!!.before(ultimoAccesoDate)) {
                                        centrosUltimaActividad[centroId] = ultimoAccesoDate
                                    }
                                }
                                
                                // Añadir a la lista de actividad reciente (últimos 10 accesos)
                                val nombre = doc.getString("nombre") ?: ""
                                val apellidos = doc.getString("apellidos") ?: ""
                                val tipoUsuario = perfil["tipo"] as? String ?: "DESCONOCIDO"
                                
                                actividadReciente.add(
                                    ActividadReciente(
                                        id = doc.id,
                                        tipo = "LOGIN",
                                        descripcion = "Acceso de $nombre $apellidos",
                                        fecha = ultimoAccesoDate,
                                        usuarioId = doc.id,
                                        detalles = "Tipo: $tipoUsuario, Centro: $centroId"
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Ordenar por fecha de acceso más reciente y quedarnos con los 10 últimos
                val actividadRecienteOrdenada = actividadReciente
                    .sortedByDescending { it.fecha }
                    .take(10)
                
                // Calcular porcentaje de uso por centro
                val porcentajeUsoPorCentro = mutableListOf<PorcentajeUso>()
                
                for (centroDoc in centrosResult.documents) {
                    val centroId = centroDoc.id
                    val nombreCentro = centroDoc.getString("nombre")?.takeIf { it.isNotBlank() } 
                        ?: "Centro ${centroId.take(8)}"
                    
                    // Contar usuarios de este centro
                    var usuariosCentro = 0
                    for (usuarioDoc in usuariosResult.documents) {
                        val perfiles = usuarioDoc.get("perfiles") as? List<Map<String, Any>> ?: emptyList()
                        for (perfil in perfiles) {
                            if (perfil["centroId"] == centroId) {
                                usuariosCentro++
                                break
                            }
                        }
                    }
                    
                    // Calcular porcentaje de uso
                    val accesos = accesosPorCentro.getOrDefault(centroId, 0)
                    val porcentaje = if (usuariosCentro > 0) {
                        (accesos.toDouble() / usuariosCentro) * 100
                    } else {
                        0.0
                    }
                    
                    porcentajeUsoPorCentro.add(
                        PorcentajeUso(
                            centroId = centroId,
                            nombreCentro = nombreCentro,
                            porcentaje = porcentaje,
                            totalUsuarios = usuariosCentro,
                            usuariosActivos = accesos
                        )
                    )
                }
                
                // Ordenar por porcentaje de mayor a menor
                val porcentajeUsoOrdenado = porcentajeUsoPorCentro.sortedByDescending { it.porcentaje }
                
                // Actualizar accesos por centro
                val accesosPorCentroList = accesosPorCentro.map { (centroId, numeroAccesos) ->
                    val nombreCentro = centrosResult.documents.find { it.id == centroId }
                        ?.getString("nombre")?.takeIf { it.isNotBlank() } 
                        ?: "Centro ${centroId.take(8)}"
                    val porcentajeUso = if (totalUsuarios > 0) (numeroAccesos.toFloat() / totalUsuarios) * 100 else 0f
                    
                    AccesoPorCentro(
                        centroId = centroId,
                        nombreCentro = nombreCentro,
                        numeroAccesos = numeroAccesos,
                        porcentajeUso = porcentajeUso,
                        ultimaActividad = centrosUltimaActividad[centroId]
                    )
                }.sortedByDescending { it.numeroAccesos }
                
                _accesosPorCentro.update { accesosPorCentroList }
                
                // Actualizar datos para el gráfico de accesos por centro
                actualizarDatosAccesosPorCentro(accesosPorCentroList)
                
                val fechaActualizacion = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        totalCentros = totalCentros,
                        totalUsuarios = totalUsuarios,
                        totalProfesores = totalProfesores,
                        totalAlumnos = totalAlumnos,
                        totalFamiliares = totalFamiliares,
                        totalAdministradores = totalAdministradores,
                        totalAdministradoresApp = totalAdministradoresApp,
                        totalAdministradoresCentro = totalAdministradoresCentro,
                        nuevosCentros = nuevosCentros,
                        nuevosProfesores = nuevosProfesores,
                        nuevosAlumnos = nuevosAlumnos,
                        nuevosFamiliares = nuevosFamiliares,
                        nuevosRegistros = nuevosProfesores + nuevosAlumnos + nuevosFamiliares + nuevosAdministradores,
                        fechaActualizacion = fechaActualizacion,
                        actividadesRecientes = actividadRecienteOrdenada
                    ) 
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar estadísticas")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar estadísticas: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Actualiza los datos para el gráfico de accesos por centro
     */
    private fun actualizarDatosAccesosPorCentro(accesosPorCentro: List<AccesoPorCentro>) {
        val centros = accesosPorCentro.sortedByDescending { it.numeroAccesos }.take(6)
        
        val etiquetas = centros.map { it.nombreCentro.take(10) + "..." }
        val valores = centros.map { it.numeroAccesos }
        
        _actividadPorUsuario.update { currentActivity ->
            currentActivity.copy(
                accesosRecentesPorCentro = DatosTemporales(
                    etiquetas = etiquetas,
                    valores = valores
                )
            )
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
                
                // Contar por roles usando el campo "tipo" dentro de perfiles
                val profesores = mutableListOf<String>()
                val alumnos = mutableListOf<String>()
                val familiares = mutableListOf<String>()
                val administradores = mutableListOf<String>()
                val administradoresApp = mutableListOf<String>()
                val administradoresCentro = mutableListOf<String>()
                
                // Mapeo para almacenar nombre de centros
                val centrosMap = centros.documents.associate { 
                    it.id to (it.getString("nombre")?.takeIf { nombre -> nombre.isNotBlank() } 
                        ?: "Centro ${it.id.take(8)}") 
                }
                
                // Contadores de accesos por centro
                val accesosPorCentroMap = mutableMapOf<String, Int>()
                
                // Obtener nuevos registros en el período especificado
                val fechaLimite = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -dias)
                }.time
                
                // Procesar usuarios y sus perfiles
                usuarios.documents.forEach { doc ->
                    val perfiles = doc.get("perfiles") as? List<Map<String, Any>> ?: emptyList()
                    
                    // Inicializar contador de accesos por centro
                    perfiles.forEach { perfil ->
                        val centroId = perfil["centroId"] as? String
                        if (centroId != null) {
                            accesosPorCentroMap[centroId] = accesosPorCentroMap.getOrDefault(centroId, 0)
                        }
                    }
                    
                    // Categorizar usuario según sus perfiles
                    var isProfesor = false
                    var isAlumno = false
                    var isFamiliar = false
                    var isAdmin = false
                    var isAdminApp = false
                    var isAdminCentro = false
                    
                    for (perfil in perfiles) {
                        when (perfil["tipo"] as? String) {
                            "PROFESOR" -> isProfesor = true
                            "ALUMNO" -> isAlumno = true
                            "FAMILIAR" -> isFamiliar = true
                            "ADMIN_CENTRO" -> {
                                isAdmin = true
                                isAdminCentro = true
                            }
                            "ADMIN_APP" -> {
                                isAdmin = true
                                isAdminApp = true
                            }
                        }
                    }
                    
                    // Registrar el usuario en la categoría correspondiente
                    if (isProfesor) profesores.add(doc.id)
                    if (isAlumno) alumnos.add(doc.id)
                    if (isFamiliar) familiares.add(doc.id)
                    if (isAdmin) administradores.add(doc.id)
                    if (isAdminApp) administradoresApp.add(doc.id)
                    if (isAdminCentro) administradoresCentro.add(doc.id)
                    
                    // Registrar último acceso si existe y está dentro del período
                    val ultimoAcceso = doc.getTimestamp("ultimoAcceso")
                    if (ultimoAcceso != null && ultimoAcceso.toDate().after(fechaLimite)) {
                        perfiles.forEach { perfil ->
                            val centroId = perfil["centroId"] as? String
                            if (centroId != null) {
                                accesosPorCentroMap[centroId] = accesosPorCentroMap.getOrDefault(centroId, 0) + 1
                            }
                        }
                    }
                }
                
                // Calcular porcentajes de uso por centro
                val totalAccesos = accesosPorCentroMap.values.sum().toFloat().coerceAtLeast(1f)
                val accesosPorCentroList = accesosPorCentroMap.map { (centroId, accesos) ->
                    AccesoPorCentro(
                        centroId = centroId,
                        nombreCentro = centrosMap[centroId] ?: "Centro ${centroId.take(8)}",
                        numeroAccesos = accesos,
                        porcentajeUso = (accesos / totalAccesos) * 100f
                    )
                }.sortedByDescending { it.numeroAccesos }
                
                _accesosPorCentro.update { accesosPorCentroList }
                
                // Contar nuevos centros y usuarios en el período específico
                val nuevosCentros = centros.documents.count { doc ->
                    doc.getTimestamp("fechaCreacion")?.toDate()?.after(fechaLimite) == true
                }
                
                val nuevosProfesores = usuarios.documents.count { doc ->
                    val fechaCreacion = doc.getTimestamp("fechaRegistro")?.toDate()
                    val perfiles = doc.get("perfiles") as? List<Map<String, Any>> ?: emptyList()
                    var isProfesor = false
                    
                    for (perfil in perfiles) {
                        if ((perfil["tipo"] as? String) == "PROFESOR") {
                            isProfesor = true
                            break
                        }
                    }
                    
                    isProfesor && fechaCreacion?.after(fechaLimite) == true
                }
                
                val nuevosAlumnos = usuarios.documents.count { doc ->
                    val fechaCreacion = doc.getTimestamp("fechaRegistro")?.toDate()
                    val perfiles = doc.get("perfiles") as? List<Map<String, Any>> ?: emptyList()
                    var isAlumno = false
                    
                    for (perfil in perfiles) {
                        if ((perfil["tipo"] as? String) == "ALUMNO") {
                            isAlumno = true
                            break
                        }
                    }
                    
                    isAlumno && fechaCreacion?.after(fechaLimite) == true
                }
                
                val nuevosFamiliares = usuarios.documents.count { doc ->
                    val fechaCreacion = doc.getTimestamp("fechaRegistro")?.toDate()
                    val perfiles = doc.get("perfiles") as? List<Map<String, Any>> ?: emptyList()
                    var isFamiliar = false
                    
                    for (perfil in perfiles) {
                        if ((perfil["tipo"] as? String) == "FAMILIAR") {
                            isFamiliar = true
                            break
                        }
                    }
                    
                    isFamiliar && fechaCreacion?.after(fechaLimite) == true
                }

                // Procesar actividades recientes y accesos
                val actividadesRecientes = mutableListOf<ActividadReciente>()
                
                // Añadir últimos accesos como actividades recientes
                val ultimosAccesos = usuarios.documents
                    .filter { 
                        val acceso = it.getTimestamp("ultimoAcceso")
                        acceso != null && acceso.toDate().after(fechaLimite)
                    }
                    .sortedByDescending { it.getTimestamp("ultimoAcceso")?.toDate() }
                    .take(5)
                    
                ultimosAccesos.forEach { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    val nombreCompleto = "$nombre $apellidos"
                    val ultimoAcceso = doc.getTimestamp("ultimoAcceso")?.toDate() ?: Date()
                    
                    actividadesRecientes.add(
                        ActividadReciente(
                            id = doc.id,
                            tipo = "LOGIN",
                            descripcion = "Acceso de $nombreCompleto",
                            fecha = ultimoAcceso,
                            usuarioId = doc.id,
                            detalles = "Último acceso al sistema"
                        )
                    )
                }
                
                // Añadir otras actividades recientes en el período
                actividades.documents
                    .filter { doc -> 
                        val fecha = doc.getTimestamp("fecha")?.toDate()
                        fecha != null && fecha.after(fechaLimite)
                    }
                    .forEach { doc ->
                        try {
                            actividadesRecientes.add(
                                ActividadReciente(
                                    id = doc.id,
                                    tipo = doc.getString("tipo") ?: "",
                                    descripcion = doc.getString("descripcion") ?: "",
                                    fecha = doc.getTimestamp("fecha")?.toDate() ?: Date(),
                                    usuarioId = doc.getString("usuarioId") ?: "",
                                    detalles = doc.getString("detalles") ?: ""
                                )
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error al procesar actividad: ${doc.id}")
                        }
                    }
                
                // Ordenar actividades por fecha
                val actividadesOrdenadas = actividadesRecientes
                    .sortedByDescending { it.fecha }
                    .take(10)

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
                    totalAdministradores = administradores.size,
                    totalAdministradoresApp = administradoresApp.size,
                    totalAdministradoresCentro = administradoresCentro.size,
                    nuevosCentros = nuevosCentros,
                    nuevosProfesores = nuevosProfesores,
                    nuevosAlumnos = nuevosAlumnos,
                    nuevosFamiliares = nuevosFamiliares,
                    fechaActualizacion = fechaActualizacion,
                    actividadesRecientes = actividadesOrdenadas
                ) }
                
                // Actualizar datos para gráfico de accesos por centro
                actualizarDatosAccesosPorCentro(accesosPorCentroList)
                
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
                
                // Obtener actividad reciente por tipo
                val tiposActividad = actividades.documents
                    .groupingBy { it.getString("tipo") ?: "DESCONOCIDO" }
                    .eachCount()
                
                // Datos de registros diarios
                val registrosDiarios = firestore.collection("registros_diarios").get().await()
                
                // Últimos 30 días de actividad para el gráfico
                val fechaLimite30Dias = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.time
                
                // Historial de actividad de los últimos 30 días
                val actividadPorDia = mutableMapOf<String, Int>()
                val formatoDia = SimpleDateFormat("dd/MM", Locale("es", "ES"))
                
                // Inicializar el mapa con todos los días (en los últimos 30)
                for (i in 29 downTo 0) {
                    val fecha = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -i)
                    }.time
                    actividadPorDia[formatoDia.format(fecha)] = 0
                }
                
                // Contar actividades por día
                actividades.documents.forEach { doc ->
                    val fecha = doc.getTimestamp("fecha")?.toDate()
                    if (fecha != null && fecha.after(fechaLimite30Dias)) {
                        val diaFormateado = formatoDia.format(fecha)
                        actividadPorDia[diaFormateado] = (actividadPorDia[diaFormateado] ?: 0) + 1
                    }
                }
                
                // Procesar datos para el informe
                val informe = StringBuilder().apply {
                    appendLine("=====================================================")
                    appendLine("   INFORME DE ESTADÍSTICAS DEL SISTEMA UME EGUNERO   ")
                    appendLine("=====================================================")
                    appendLine("Fecha de generación: ${dateFormatter.format(Date())}")
                    appendLine()
                    
                    appendLine("1. RESUMEN GENERAL:")
                    appendLine("------------------")
                    appendLine("• Total de Centros: ${centros.size()}")
                    appendLine("• Total de Usuarios: ${usuarios.size()}")
                    appendLine()
                    
                    appendLine("2. DISTRIBUCIÓN DE USUARIOS:")
                    appendLine("--------------------------")
                    appendLine("• Profesores: ${profesores.size} (${calcularPorcentaje(profesores.size, usuarios.size())}%)")
                    appendLine("• Alumnos: ${alumnos.size} (${calcularPorcentaje(alumnos.size, usuarios.size())}%)")
                    appendLine("• Familiares: ${familiares.size} (${calcularPorcentaje(familiares.size, usuarios.size())}%)")
                    appendLine()
                    
                    appendLine("3. ACTIVIDAD DEL SISTEMA:")
                    appendLine("-----------------------")
                    appendLine("• Total de actividades registradas: ${actividades.size()}")
                    appendLine("• Registros diarios generados: ${registrosDiarios.size()}")
                    appendLine()
                    
                    appendLine("4. DISTRIBUCIÓN DE ACTIVIDADES POR TIPO:")
                    appendLine("--------------------------------------")
                    tiposActividad.entries.sortedByDescending { it.value }.forEach { (tipo, cantidad) ->
                        appendLine("• $tipo: $cantidad (${calcularPorcentaje(cantidad, actividades.size())}%)")
                    }
                    appendLine()
                    
                    appendLine("5. GRÁFICO DE ACTIVIDAD (ÚLTIMOS 30 DÍAS):")
                    appendLine("---------------------------------------")
                    
                    // Crear un gráfico ASCII simple
                    val maxActividad = actividadPorDia.values.maxOrNull() ?: 0
                    val escala = if (maxActividad > 0) 20.0 / maxActividad else 1.0
                    
                    for ((fecha, cantidad) in actividadPorDia.entries.sortedBy { it.key }) {
                        val barras = (cantidad * escala).toInt()
                        appendLine("$fecha | ${"█".repeat(barras)} $cantidad")
                    }
                    appendLine()
                    
                    appendLine("6. ACTIVIDADES RECIENTES:")
                    appendLine("------------------------")
                    actividades.documents.take(15).forEach { doc ->
                        val fecha = doc.getTimestamp("fecha")?.toDate()?.let { dateFormatter.format(it) } ?: "Fecha desconocida"
                        val descripcion = doc.getString("descripcion") ?: "Sin descripción"
                        val usuario = doc.getString("usuarioId") ?: "Usuario desconocido"
                        appendLine("• [$fecha] $usuario: $descripcion")
                    }
                    appendLine()
                    
                    appendLine("7. CONCLUSIONES Y RECOMENDACIONES:")
                    appendLine("----------------------------------")
                    
                    // Generar algunas conclusiones básicas basadas en los datos
                    val ratioFamiliarAlumno = if (alumnos.size > 0) familiares.size.toFloat() / alumnos.size else 0f
                    
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
                        "centros" to centros.size(),
                        "usuarios" to usuarios.size(),
                        "profesores" to profesores.size,
                        "alumnos" to alumnos.size,
                        "familiares" to familiares.size
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
     * Calcula el porcentaje redondeado a entero
     */
    private fun calcularPorcentaje(valor: Int, total: Int): Int {
        return if (total > 0) {
            ((valor.toFloat() / total.toFloat()) * 100).toInt()
        } else {
            0
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