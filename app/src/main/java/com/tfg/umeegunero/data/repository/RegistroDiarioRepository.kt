package com.tfg.umeegunero.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
 * Soporta almacenamiento offline mediante Room.
 * 
 * @param firestore Instancia de FirebaseFirestore inyectada por Hilt
 * @param registroActividadDao DAO para acceso a la base de datos local
 * @param context Contexto de la aplicación para verificar conectividad
 * @author Estudiante 2º DAM
 */
@Singleton
class RegistroDiarioRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val registroActividadDao: RegistroActividadDao,
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
                val registrosNoSincronizados = registroActividadDao.getRegistrosNoSincronizados()
                Timber.d("Sincronizando ${registrosNoSincronizados.size} registros pendientes")
                
                registrosNoSincronizados.forEach { entidad ->
                    launch {
                        try {
                            val registro = entidad.toRegistroActividad()
                            registrosCollection.document(registro.id)
                                .set(registro)
                                .await()
                            
                            // Marcar como sincronizado en la BD local
                            registroActividadDao.marcarRegistroComoSincronizado(entidad.id)
                            Timber.d("Registro ${entidad.id} sincronizado con éxito")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al sincronizar registro ${entidad.id}")
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
                        registroActividadDao.insertRegistroActividad(
                            RegistroActividadEntity.fromRegistroActividad(registroExistente)
                        )
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
                        registroActividadDao.insertRegistroActividad(
                            RegistroActividadEntity.fromRegistroActividad(registroConId)
                        )
                        
                        return@withContext Result.Success(registroConId)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener/crear registro en Firestore, intentando con local")
                    // Si falla Firestore, intentamos con local
                }
            }
            
            // Si no hay conexión o falló la operación en Firestore, intentamos con local
            val startTimestamp = inicioDia.time
            val endTimestamp = finDia.time
            
            // Buscamos en la base de datos local
            val registrosLocales = registroActividadDao.getRegistrosActividadByClaseAndFecha(
                claseId, startTimestamp, endTimestamp
            ).filter { it.alumnoId == alumnoId }
            
            if (registrosLocales.isNotEmpty()) {
                // Si hay registros locales, devolvemos el primero
                return@withContext Result.Success(registrosLocales.first().toRegistroActividad())
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
                registroActividadDao.insertRegistroActividad(
                    RegistroActividadEntity.fromRegistroActividad(nuevoRegistro, sincronizado = false)
                )
                
                return@withContext Result.Success(nuevoRegistro)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener/crear registro diario para alumno $alumnoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Genera un ID local temporal para registros creados sin conexión
     */
    private fun generateLocalId(): String {
        return "local_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Actualiza un registro diario existente.
     * 
     * @param registro Registro a actualizar
     * @return Resultado de la operación (éxito o error)
     */
    suspend fun actualizarRegistroDiario(registro: RegistroActividad): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val registroActualizado = registro.copy(
                ultimaModificacion = Timestamp.now(),
                modificadoPor = registro.modificadoPor
            )
            
            // Si hay conexión, actualizamos en Firestore
            val sincronizado = if (isNetworkAvailable()) {
                try {
                    registrosCollection.document(registro.id)
                        .set(registroActualizado)
                        .await()
                    true
                } catch (e: Exception) {
                    Timber.e(e, "Error al actualizar en Firestore, guardando solo en local")
                    false
                }
            } else {
                false
            }
            
            // Siempre actualizamos en la base de datos local
            registroActividadDao.insertRegistroActividad(
                RegistroActividadEntity.fromRegistroActividad(registroActualizado, sincronizado)
            )
                
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar registro diario ${registro.id}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Marca un registro como visualizado por los familiares.
     * 
     * @param registroId ID del registro a marcar
     * @return Resultado de la operación (éxito o error)
     */
    suspend fun marcarComoVisualizado(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val timestamp = Date().time
            
            // Actualizamos en la base de datos local
            registroActividadDao.marcarRegistroComoVisto(registroId, timestamp)
            
            // Si hay conexión, también actualizamos en Firestore
            if (isNetworkAvailable()) {
                try {
                    registrosCollection.document(registroId)
                        .update(
                            mapOf(
                                "visualizadoPorFamiliar" to true,
                                "fechaVisualizacion" to Timestamp.now(),
                                "vistoPorFamiliar" to true,
                                "fechaVisto" to Timestamp.now()
                            )
                        )
                        .await()
                    
                    // Si la actualización en Firestore fue exitosa, marcamos como sincronizado
                    registroActividadDao.marcarRegistroComoSincronizado(registroId)
                } catch (e: Exception) {
                    Timber.e(e, "Error al marcar como visto en Firestore, se sincronizará más tarde")
                    // No propagamos la excepción, ya que la actualización local fue exitosa
                }
            }
                
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como visualizado: $registroId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los registros diarios de un alumno.
     * 
     * @param alumnoId ID del alumno
     * @param limit Límite de registros a obtener
     * @return Flow de resultado con la lista de registros
     */
    fun obtenerRegistrosAlumno(alumnoId: String, limit: Long = 30): Flow<Result<List<RegistroActividad>>> = flow {
        emit(Result.Loading)
        
        // Primero intentamos obtener los datos desde Room
        val localFlow = registroActividadDao.getRegistrosActividadByAlumno(alumnoId)
            .map { entidades -> entidades.map { it.toRegistroActividad() } }
            .catch { e ->
                Timber.e(e, "Error al obtener registros locales para alumno $alumnoId")
                emit(Result.Error(Exception(e)))
            }
        
        // Emitimos primero los datos locales
        localFlow.collect { registrosLocales ->
            emit(Result.Success(registrosLocales))
        }
        
        // Si hay conexión, obtenemos los datos actualizados de Firestore
        if (isNetworkAvailable()) {
            try {
                val query = registrosCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .await()
                    
                val registros = query.toObjects(RegistroActividad::class.java)
                
                // Guardamos en la base de datos local para futuras consultas
                val entidades = registros.map { RegistroActividadEntity.fromRegistroActividad(it) }
                registroActividadDao.insertRegistrosActividad(entidades)
                
                emit(Result.Success(registros))
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener registros del alumno $alumnoId desde Firestore")
                // No emitimos error si ya teníamos datos locales
            }
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
            
            val startTimestamp = inicioDia.time
            val endTimestamp = finDia.time
            
            // Primero intentamos obtener desde la base de datos local
            val entidadesLocales = registroActividadDao.getRegistrosActividadByClaseAndFecha(
                claseId, startTimestamp, endTimestamp
            )
            
            // Si hay registros locales y no hay conexión, los devolvemos
            if (entidadesLocales.isNotEmpty() && !isNetworkAvailable()) {
                return@withContext Result.Success(
                    entidadesLocales.map { it.toRegistroActividad() }
                )
            }
            
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
                    val entidades = registros.map { RegistroActividadEntity.fromRegistroActividad(it) }
                    registroActividadDao.insertRegistrosActividad(entidades)
                    
                    return@withContext Result.Success(registros)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros de Firestore para clase $claseId")
                    // Si hay registros locales, los devolvemos como fallback
                    if (entidadesLocales.isNotEmpty()) {
                        return@withContext Result.Success(
                            entidadesLocales.map { it.toRegistroActividad() }
                        )
                    }
                    return@withContext Result.Error(e)
                }
            }
            
            // Si no hay conexión y no teníamos datos locales
            if (entidadesLocales.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            return@withContext Result.Success(
                entidadesLocales.map { it.toRegistroActividad() }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros para la clase $claseId en fecha específica")
            return@withContext Result.Error(e)
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
            
            // Primero intentamos obtener desde la base de datos local
            val entidadesLocales = registroActividadDao.getRegistrosActividadNoVistos(alumnosIds)
            
            // Si hay registros locales y no hay conexión, los devolvemos
            if (entidadesLocales.isNotEmpty() && !isNetworkAvailable()) {
                return@withContext Result.Success(
                    entidadesLocales.map { it.toRegistroActividad() }
                )
            }
            
            // Si hay conexión, intentamos obtener datos actualizados de Firestore
            if (isNetworkAvailable()) {
                try {
                    val resultados = mutableListOf<RegistroActividad>()
                    
                    // Consultamos por cada alumno (no se puede usar whereIn con otro filtro complejo)
                    for (alumnoId in alumnosIds) {
                        val query = registrosCollection
                            .whereEqualTo("alumnoId", alumnoId)
                            .whereEqualTo("vistoPorFamiliar", false)
                            .orderBy("fecha", Query.Direction.DESCENDING)
                            .get()
                            .await()
                            
                        resultados.addAll(query.toObjects(RegistroActividad::class.java))
                    }
                    
                    // Guardamos en la base de datos local para futuras consultas
                    val entidades = resultados.map { RegistroActividadEntity.fromRegistroActividad(it) }
                    registroActividadDao.insertRegistrosActividad(entidades)
                    
                    return@withContext Result.Success(resultados)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros no visualizados de Firestore")
                    // Si hay registros locales, los devolvemos como fallback
                    if (entidadesLocales.isNotEmpty()) {
                        return@withContext Result.Success(
                            entidadesLocales.map { it.toRegistroActividad() }
                        )
                    }
                    return@withContext Result.Error(e)
                }
            }
            
            // Si no hay conexión y no teníamos datos locales
            if (entidadesLocales.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            return@withContext Result.Success(
                entidadesLocales.map { it.toRegistroActividad() }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros no visualizados")
            return@withContext Result.Error(e)
        }
    }
} 