package com.tfg.umeegunero.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los registros diarios de actividades de los alumnos.
 * 
 * Esta clase maneja las operaciones CRUD para los registros de actividades,
 * así como consultas específicas por alumno, clase, fecha, etc.
 * Soporta almacenamiento offline mediante el repositorio local.
 * 
 * @param firestore Instancia de FirebaseFirestore inyectada por Hilt
 * @param localRegistroRepository Repositorio local para acceso a la base de datos local
 * @param context Contexto de la aplicación para verificar conectividad
 * @author Estudiante 2º DAM
 */
@Singleton
class RegistroDiarioRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val localRegistroRepository: LocalRegistroActividadRepository,
    private val context: Context
) {
    private val registrosCollection = firestore.collection("registrosActividad")
    
    /**
     * Verifica si el dispositivo tiene conexión a Internet
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    
    /**
     * Sincroniza los registros pendientes con Firestore cuando hay conexión
     */
    suspend fun sincronizarRegistrosPendientes() {
        if (!isNetworkAvailable()) {
            return
        }
        
        withContext(Dispatchers.IO) {
            try {
                val registrosNoSincronizados = localRegistroRepository.getUnsyncedRegistros()
                Timber.d("Sincronizando ${registrosNoSincronizados.size} registros pendientes")
                
                registrosNoSincronizados.forEach { registro ->
                    launch {
                        try {
                            registrosCollection.document(registro.id)
                                .set(registro)
                                .await()
                            
                            // Marcar como sincronizado en la BD local
                            localRegistroRepository.markAsSynced(registro.id)
                            Timber.d("Registro ${registro.id} sincronizado con éxito")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al sincronizar registro ${registro.id}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al sincronizar registros pendientes")
            }
        }
    }
    
    /**
     * Obtiene o crea un registro diario para un alumno y fecha específicos.
     * 
     * @param alumnoId ID del alumno
     * @param claseId ID de la clase
     * @param profesorId ID del profesor
     * @param fecha Fecha para el registro (por defecto, la fecha actual)
     * @return Resultado con el registro obtenido o creado
     */
    suspend fun obtenerOCrearRegistroDiario(
        alumnoId: String,
        claseId: String,
        profesorId: String,
        fecha: Date = Date()
    ): Result<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            // Obtener los límites del día (inicio y fin)
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicioDia = calendar.time
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val finDia = calendar.time
            
            // Si hay conexión, intentamos obtener desde Firestore
            if (isNetworkAvailable()) {
                try {
                    // Buscar si ya existe un registro para este alumno en esta fecha
                    val query = registrosCollection
                        .whereEqualTo("alumnoId", alumnoId)
                        .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDia))
                        .whereLessThanOrEqualTo("fecha", Timestamp(finDia))
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!query.isEmpty) {
                        // Si existe, guardar en local y devolver
                        val registroExistente = query.documents[0].toObject(RegistroActividad::class.java)!!
                        // Guardar en caché local
                        localRegistroRepository.saveRegistroActividad(registroExistente, true)
                        return@withContext Result.Success(registroExistente)
                    } else {
                        // Si no existe, crear uno nuevo
                        val nuevoRegistro = RegistroActividad(
                            alumnoId = alumnoId,
                            claseId = claseId,
                            profesorId = profesorId,
                            fecha = Timestamp(fecha),
                            creadoPor = profesorId,
                            modificadoPor = profesorId
                        )
                        
                        val documentRef = registrosCollection.document()
                        val registroConId = nuevoRegistro.copy(id = documentRef.id)
                        documentRef.set(registroConId).await()
                        
                        // Guardar en caché local
                        localRegistroRepository.saveRegistroActividad(registroConId, true)
                        
                        return@withContext Result.Success(registroConId)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener/crear registro en Firestore, intentando con local")
                    // Si falla Firestore, intentamos con local
                }
            }
            
            // Si no hay conexión o falló la operación en Firestore, intentamos con local
            val registrosLocales = localRegistroRepository.getRegistrosActividadByAlumnoAndFecha(alumnoId, fecha)
            
            if (registrosLocales.isNotEmpty()) {
                // Si hay registros locales, devolvemos el primero
                return@withContext Result.Success(registrosLocales.first())
            } else {
                // Si no hay registros locales, creamos uno nuevo
                val nuevoRegistro = RegistroActividad(
                    id = generateLocalId(), // Generamos un ID temporal
                    alumnoId = alumnoId,
                    claseId = claseId,
                    profesorId = profesorId,
                    fecha = Timestamp(fecha),
                    creadoPor = profesorId,
                    modificadoPor = profesorId
                )
                
                // Guardamos en local con marca de "no sincronizado"
                localRegistroRepository.saveRegistroActividad(nuevoRegistro, false)
                
                return@withContext Result.Success(nuevoRegistro)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener/crear registro diario")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Actualiza un registro diario existente.
     * 
     * @param registro Registro a actualizar
     * @return Resultado de la operación (éxito o error)
     */
    suspend fun actualizarRegistroDiario(registro: RegistroActividad): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext guardarRegistroDiario(registro)
    }
    
    /**
     * Guarda un registro diario en Firestore y localmente
     * 
     * @param registro El registro a guardar
     * @return Resultado de la operación
     */
    suspend fun guardarRegistroDiario(registro: RegistroActividad): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Actualizar el registro con la última modificación
            val registroActualizado = registro.copy(
                ultimaModificacion = Timestamp(Date())
            )
            
            // Si hay conexión, guardamos en Firestore
            if (isNetworkAvailable()) {
                try {
                    registrosCollection.document(registroActualizado.id)
                        .set(registroActualizado)
                        .await()
                    
                    // Guardar en local sincronizado
                    localRegistroRepository.saveRegistroActividad(registroActualizado, true)
                    return@withContext Result.Success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Error al guardar en Firestore, guardando solo en local")
                }
            }
            
            // Si no hay conexión o falló Firestore, guardamos solo localmente
            localRegistroRepository.saveRegistroActividad(registroActualizado, false)
            return@withContext Result.Success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar registro diario")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Obtiene un registro diario por su ID
     * 
     * @param registroId ID del registro
     * @return Resultado con el registro si existe
     */
    suspend fun obtenerRegistroDiarioPorId(registroId: String): Result<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            // Primero intentamos obtenerlo de la caché local
            val registroLocal = localRegistroRepository.getRegistroActividadById(registroId)
            
            if (registroLocal != null) {
                return@withContext Result.Success(registroLocal)
            }
            
            // Si no está en caché y hay conexión, lo buscamos en Firestore
            if (isNetworkAvailable()) {
                try {
                    val documentSnapshot = registrosCollection.document(registroId).get().await()
                    
                    if (documentSnapshot.exists()) {
                        val registro = documentSnapshot.toObject(RegistroActividad::class.java)!!
                        // Guardamos en caché local
                        localRegistroRepository.saveRegistroActividad(registro, true)
                        return@withContext Result.Success(registro)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registro de Firestore")
                }
            }
            
            return@withContext Result.Error(Exception("No se encontró el registro con ID $registroId"))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro por ID")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Obtiene los registros diarios de un alumno.
     * 
     * @param alumnoId ID del alumno
     * @param limit Límite de registros a obtener
     * @return Flow de resultado con la lista de registros
     */
    fun obtenerRegistrosAlumno(alumnoId: String, limit: Long = 30): Flow<Result<List<RegistroActividad>>> {
        return obtenerRegistrosDiariosPorAlumno(alumnoId)
    }
    
    /**
     * Obtiene los registros diarios de un alumno
     * 
     * @param alumnoId ID del alumno
     * @return Flow de resultado con la lista de registros
     */
    fun obtenerRegistrosDiariosPorAlumno(alumnoId: String): Flow<Result<List<RegistroActividad>>> = flow {
        emit(Result.Loading())
        
        try {
            // Si hay conexión, intentamos obtener desde Firestore
            if (isNetworkAvailable()) {
                try {
                    val registros = registrosCollection
                        .whereEqualTo("alumnoId", alumnoId)
                        .orderBy("fecha", Query.Direction.DESCENDING)
                        .get()
                        .await()
                        .toObjects(RegistroActividad::class.java)
                    
                    // Guardamos los resultados en la caché local
                    localRegistroRepository.saveRegistrosActividad(registros, true)
                    
                    emit(Result.Success(registros))
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros de Firestore, usando local")
                    // Si falla Firestore, emitimos los datos locales
                    localRegistroRepository.getRegistrosActividadByAlumno(alumnoId)
                        .collect { registros ->
                            emit(Result.Success(registros))
                        }
                }
            } else {
                // Si no hay conexión, usamos los datos locales
                localRegistroRepository.getRegistrosActividadByAlumno(alumnoId)
                    .collect { registros ->
                        emit(Result.Success(registros))
                    }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros del alumno")
            emit(Result.Error(Exception(e.message ?: "Error desconocido")))
        }
    }.catch { e ->
        Timber.e(e, "Error en el flujo de registros por alumno")
        emit(Result.Error(Exception(e.message ?: "Error desconocido")))
    }
    
    /**
     * Marca un registro como visualizado por los familiares.
     * 
     * @param registroId ID del registro a marcar
     * @return Resultado de la operación (éxito o error)
     */
    suspend fun marcarComoVisualizado(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val registroResult = obtenerRegistroDiarioPorId(registroId)
            
            if (registroResult is Result.Error) {
                return@withContext registroResult
            }
            
            val registro = (registroResult as Result.Success).data
            val registroActualizado = registro.copy(
                vistoPorFamiliar = true,
                visualizadoPorFamiliar = true,
                fechaVisto = Timestamp(Date()),
                fechaVisualizacion = Timestamp(Date())
            )
            
            return@withContext guardarRegistroDiario(registroActualizado)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como visualizado: $registroId")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Obtiene todos los registros de una clase para una fecha específica.
     * 
     * @param claseId ID de la clase
     * @param fecha Fecha para filtrar (por defecto, la fecha actual)
     * @return Resultado con la lista de registros
     */
    suspend fun obtenerRegistrosPorClaseYFecha(
        claseId: String,
        fecha: Date = Date()
    ): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            // Obtener los límites del día
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicioDia = calendar.time
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val finDia = calendar.time
            
            // Si hay conexión, intentamos obtener datos actualizados de Firestore
            if (isNetworkAvailable()) {
                try {
                    val query = registrosCollection
                        .whereEqualTo("claseId", claseId)
                        .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDia))
                        .whereLessThanOrEqualTo("fecha", Timestamp(finDia))
                        .get()
                        .await()
                        
                    val registros = query.toObjects(RegistroActividad::class.java)
                    
                    // Guardamos en la base de datos local para futuras consultas
                    localRegistroRepository.saveRegistrosActividad(registros, true)
                    
                    return@withContext Result.Success(registros)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros de Firestore para clase $claseId")
                    // Intentamos con los datos locales como fallback
                }
            }
            
            // Si no hay conexión o falló Firestore, usamos datos locales
            // Transformamos las fechas para la consulta local
            val registrosLocales = mutableListOf<RegistroActividad>()
            localRegistroRepository.getRegistrosActividadByAlumno("").collect { registros ->
                registrosLocales.addAll(
                    registros.filter { 
                        it.claseId == claseId && 
                        it.fecha.toDate().time >= inicioDia.time && 
                        it.fecha.toDate().time <= finDia.time 
                    }
                )
            }
            
            return@withContext Result.Success(registrosLocales)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros para la clase $claseId en fecha específica")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Obtiene los registros no visualizados para un familiar específico.
     * 
     * @param alumnosIds Lista de IDs de alumnos asociados al familiar
     * @return Resultado con la lista de registros no visualizados
     */
    suspend fun obtenerRegistrosNoVisualizados(
        alumnosIds: List<String>
    ): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            if (alumnosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            val registrosNoVistos = mutableListOf<RegistroActividad>()
            
            // Si hay conexión, intentamos obtener datos actualizados de Firestore
            if (isNetworkAvailable()) {
                try {
                    for (alumnoId in alumnosIds) {
                        val query = registrosCollection
                            .whereEqualTo("alumnoId", alumnoId)
                            .whereEqualTo("vistoPorFamiliar", false)
                            .orderBy("fecha", Query.Direction.DESCENDING)
                            .get()
                            .await()
                        
                        val registros = query.toObjects(RegistroActividad::class.java)
                        registrosNoVistos.addAll(registros)
                    }
                    
                    // Guardamos en la base de datos local para futuras consultas
                    if (registrosNoVistos.isNotEmpty()) {
                        localRegistroRepository.saveRegistrosActividad(registrosNoVistos, true)
                    }
                    
                    return@withContext Result.Success(registrosNoVistos)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros no visualizados de Firestore")
                    // Intentamos con datos locales como fallback
                }
            }
            
            // Si no hay conexión o falló Firestore, usamos datos locales
            for (alumnoId in alumnosIds) {
                localRegistroRepository.getRegistrosActividadByAlumno(alumnoId).collect { registros ->
                    registrosNoVistos.addAll(registros.filter { !it.vistoPorFamiliar })
                }
            }
            
            return@withContext Result.Success(registrosNoVistos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros no visualizados")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Marca un registro como visto por el familiar
     * 
     * @param registroId ID del registro a marcar
     * @param familiarId ID del familiar que visualiza
     * @return Resultado de la operación
     */
    suspend fun marcarRegistroComoVisto(registroId: String, familiarId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val timestamp = Timestamp(Date())
            
            // Primero obtenemos el registro
            val registroResult = obtenerRegistroDiarioPorId(registroId)
            
            if (registroResult is Result.Error) {
                return@withContext registroResult
            }
            
            val registro = (registroResult as Result.Success).data
            val registroActualizado = registro.copy(
                vistoPorFamiliar = true,
                visualizadoPorFamiliar = true,
                fechaVisto = timestamp,
                fechaVisualizacion = timestamp
            )
            
            // Si hay conexión, actualizamos en Firestore
            if (isNetworkAvailable()) {
                try {
                    registrosCollection.document(registroId)
                        .update(
                            mapOf(
                                "vistoPorFamiliar" to true,
                                "visualizadoPorFamiliar" to true,
                                "fechaVisto" to timestamp,
                                "fechaVisualizacion" to timestamp
                            )
                        )
                        .await()
                    
                    // Actualizamos en local
                    localRegistroRepository.updateRegistroActividad(registroActualizado, true)
                    return@withContext Result.Success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Error al marcar como visto en Firestore, guardando solo en local")
                }
            }
            
            // Si no hay conexión o falló Firestore, actualizamos solo localmente
            localRegistroRepository.updateRegistroActividad(registroActualizado, false)
            return@withContext Result.Success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como visto")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Genera un ID local temporal para registros creados sin conexión
     */
    private fun generateLocalId(): String {
        return "local_${System.currentTimeMillis()}_${(0..999).random()}"
    }
    
    /**
     * Obtiene los registros de actividad de un alumno específico.
     * 
     * @param alumnoId ID del alumno
     * @return Resultado con la lista de registros del alumno
     */
    suspend fun getRegistrosActividadByAlumnoId(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            // Intentar obtener desde Firestore si hay conexión
            if (isNetworkAvailable()) {
                try {
                    val query = registrosCollection
                        .whereEqualTo("alumnoId", alumnoId)
                        .orderBy("fecha", Query.Direction.DESCENDING)
                        .limit(20) // Limitamos a los 20 registros más recientes
                        .get()
                        .await()
                    
                    val registros = query.documents.mapNotNull { doc ->
                        doc.toObject(RegistroActividad::class.java)
                    }
                    
                    // Almacenar en caché local
                    registros.forEach { registro ->
                        localRegistroRepository.saveRegistroActividad(registro, true)
                    }
                    
                    return@withContext Result.Success(registros)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros desde Firestore, intentando con local")
                    // Si falla, continuamos con local
                }
            }
            
            // Obtener desde local si no hay conexión o falló Firestore
            val registrosLocales = localRegistroRepository.getRegistrosActividadByAlumnoId(alumnoId)
            return@withContext Result.Success(registrosLocales)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros del alumno $alumnoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene el número de registros sin leer por un familiar.
     * 
     * @param familiarId ID del familiar
     * @return Resultado con el número de registros sin leer
     */
    suspend fun getRegistrosSinLeerCount(familiarId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (isNetworkAvailable()) {
                try {
                    // Consultar los registros no leídos por este familiar
                    val query = registrosCollection
                        .whereNotEqualTo("vistoPor.$familiarId", true)
                        .get()
                        .await()
                    
                    return@withContext Result.Success(query.size())
                } catch (e: Exception) {
                    Timber.e(e, "Error al contar registros sin leer desde Firestore")
                    // Si falla, intentamos con local
                }
            }
            
            // Contar desde local si no hay conexión o falló Firestore
            val count = localRegistroRepository.getRegistrosSinLeerCount(familiarId)
            return@withContext Result.Success(count)
        } catch (e: Exception) {
            Timber.e(e, "Error al contar registros sin leer del familiar $familiarId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene registros de un alumno en un rango de fechas específico
     * @param alumnoId ID del alumno
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Resultado con la lista de registros encontrados
     */
    suspend fun obtenerRegistrosPorFecha(
        alumnoId: String,
        fechaInicio: Date,
        fechaFin: Date
    ): Result<List<RegistroActividad>> {
        return try {
            val registrosRef = firestore.collection("registrosActividad")
                .whereEqualTo("alumnoId", alumnoId)
                .whereGreaterThanOrEqualTo("fecha", Timestamp(fechaInicio.time / 1000, 0))
                .whereLessThanOrEqualTo("fecha", Timestamp(fechaFin.time / 1000, 0))
                .get()
                .await()
                
            val registros = registrosRef.documents.mapNotNull { documento ->
                try {
                    documento.toObject(RegistroActividad::class.java)
                } catch (e: Exception) {
                    Timber.e(e, "Error al convertir documento a RegistroActividad")
                    null
                }
            }
            
            Result.Success(registros)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por fecha para alumno: $alumnoId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene los registros diarios para una fecha y clase específica
     * 
     * @param fecha Fecha del registro
     * @param claseId ID de la clase
     * @return Resultado con la lista de registros
     */
    suspend fun obtenerRegistrosDiariosPorFechaYClase(
        fecha: Date,
        claseId: String
    ): Result<List<RegistroActividad>> {
        return withContext(Dispatchers.IO) {
            try {
                // Convertir fecha a timestamp para el filtrado
                val calendar = Calendar.getInstance()
                calendar.time = fecha
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.time
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endOfDay = calendar.time
                
                val startTimestamp = Timestamp(startOfDay)
                val endTimestamp = Timestamp(endOfDay)
                
                Timber.d("Buscando registros para clase: $claseId, entre $startOfDay y $endOfDay")
                
                val registrosSnapshot = firestore.collection("registrosActividad")
                    .whereEqualTo("claseId", claseId)
                    .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                    .whereLessThanOrEqualTo("fecha", endTimestamp)
                    .get()
                    .await()
                
                val registros = registrosSnapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(RegistroActividad::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al convertir documento a RegistroActividad: ${document.id}")
                        null
                    }
                }
                
                Timber.d("Se encontraron ${registros.size} registros para la clase $claseId y fecha $fecha")
                registros.forEach { 
                    Timber.d("Registro: alumnoId=${it.alumnoId}, fecha=${it.fecha}, creado por=${it.creadoPor}")
                }
                
                Result.Success(registros)
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener registros por fecha y clase")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Obtiene los registros diarios para un alumno en un rango de fechas específico
     * 
     * @param alumnoId ID del alumno
     * @param fechaInicio Timestamp de inicio del rango
     * @param fechaFin Timestamp de fin del rango
     * @return Resultado con la lista de registros
     */
    suspend fun obtenerRegistrosPorFechaYAlumno(
        alumnoId: String,
        fechaInicio: Timestamp,
        fechaFin: Timestamp
    ): Result<List<RegistroActividad>> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Buscando registros para alumno: $alumnoId, entre $fechaInicio y $fechaFin")
                
                val registrosSnapshot = firestore.collection("registrosActividad")
                    .whereEqualTo("alumnoId", alumnoId)
                    .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                    .whereLessThanOrEqualTo("fecha", fechaFin)
                    .get()
                    .await()
                
                val registros = registrosSnapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(RegistroActividad::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al convertir documento a RegistroActividad: ${document.id}")
                        null
                    }
                }
                
                Timber.d("Se encontraron ${registros.size} registros para el alumno $alumnoId en el rango de fechas")
                registros.forEach { 
                    Timber.d("Registro: id=${it.id}, fecha=${it.fecha}, creado por=${it.creadoPor}")
                }
                
                Result.Success(registros)
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener registros por alumno y fecha")
                Result.Error(e)
            }
        }
    }
} 