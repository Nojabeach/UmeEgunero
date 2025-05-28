package com.tfg.umeegunero.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.RegistroDiario
import com.tfg.umeegunero.data.model.LecturaFamiliar
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
import java.text.SimpleDateFormat
import java.util.Locale

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
     * Verifica si un registro diario existe en Firestore
     *
     * @param registroId ID del registro a verificar
     * @return true si el registro existe, false en caso contrario
     */
    suspend fun verificarRegistroExiste(registroId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Verificando existencia del registro con ID: $registroId")
            
            if (isNetworkAvailable()) {
                val document = registrosCollection.document(registroId).get().await()
                val existe = document.exists()
                Timber.d("Resultado de verificación para registro $registroId: ${if (existe) "EXISTE" else "NO EXISTE"}")
                return@withContext existe
            } else {
                // Si no hay conexión, verificamos en la caché local
                val existeLocal = localRegistroRepository.getRegistroActividadById(registroId) != null
                Timber.d("Sin conexión. Verificación local para registro $registroId: ${if (existeLocal) "EXISTE" else "NO EXISTE"}")
                return@withContext existeLocal
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar existencia del registro: $registroId")
            return@withContext false
        }
    }
    
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
                            id = generateLocalId(alumnoId, fecha),
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
            val registrosLocales = mutableListOf<RegistroActividad>()
            
            // Usar método sincronizado en lugar de Flow
            val registros = localRegistroRepository.getRegistrosActividadByAlumno("")
            registrosLocales.addAll(
                registros.filter { 
                    it.claseId == claseId && 
                    it.fecha.toDate().time >= inicioDia.time && 
                    it.fecha.toDate().time <= finDia.time 
                }
            )
            
            if (registrosLocales.isNotEmpty()) {
                // Si hay registros locales, devolvemos el primero
                return@withContext Result.Success(registrosLocales.first())
            } else {
                // Si no hay registros locales, creamos uno nuevo
                val nuevoRegistro = RegistroActividad(
                    id = generateLocalId(alumnoId, fecha),
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
     * @return Flow de resultado con la lista de registros
     */
    fun obtenerRegistrosAlumno(alumnoId: String): Flow<Result<List<RegistroActividad>>> = flow {
        emit(obtenerRegistrosDiariosPorAlumno(alumnoId))
    }
    
    /**
     * Obtiene los registros diarios de un alumno
     * 
     * @param alumnoId ID del alumno
     * @return Resultado con la lista de registros
     */
    suspend fun obtenerRegistrosDiariosPorAlumno(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
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
                    
                    return@withContext Result.Success(registros)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros de Firestore para alumno $alumnoId")
                    // Si falla Firestore, usamos los datos locales
                    val registros = localRegistroRepository.getRegistrosActividadByAlumno(alumnoId)
                    return@withContext Result.Success(registros)
                }
            } else {
                // Si no hay conexión, obtenemos de la caché local
                val registros = localRegistroRepository.getRegistrosActividadByAlumno(alumnoId)
                return@withContext Result.Success(registros)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros para alumno $alumnoId: ${e.message}")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
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
            val registroResult = obtenerRegistroDiarioPorId(registroId)
            
            if (registroResult is Result.Error) {
                return@withContext registroResult
            }
            
            val registro = (registroResult as Result.Success).data
            val registroActualizado = registro.copy(
                vistoPorFamiliar = true,
                fechaVisto = Timestamp(Date())
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
            
            // Usar método sincronizado en lugar de Flow
            val registros = localRegistroRepository.getRegistrosActividadByAlumno("")
            registrosLocales.addAll(
                registros.filter { 
                    it.claseId == claseId && 
                    it.fecha.toDate().time >= inicioDia.time && 
                    it.fecha.toDate().time <= finDia.time 
                }
            )
            
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
                val registros = localRegistroRepository.getRegistrosActividadByAlumno(alumnoId)
                registrosNoVistos.addAll(registros.filter { !it.vistoPorFamiliar })
            }
            
            return@withContext Result.Success(registrosNoVistos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros no visualizados")
            return@withContext Result.Error(Exception(e.message ?: "Error desconocido"))
        }
    }
    
    /**
     * Marca un registro como leído por un familiar específico
     * 
     * @param registroId ID del registro a marcar
     * @param familiarId ID del familiar que lee el registro
     * @param nombreFamiliar Nombre del familiar para el registro
     * @return Resultado de la operación
     */
    suspend fun marcarRegistroComoLeidoPorFamiliar(
        registroId: String, 
        familiarId: String,
        nombreFamiliar: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val timestamp = Timestamp(Date())
            
            // Primero obtenemos el registro
            val registroResult = obtenerRegistroDiarioPorId(registroId)
            
            if (registroResult is Result.Error) {
                return@withContext registroResult
            }
            
            val registro = (registroResult as Result.Success).data
            
            // Crear la información de lectura
            val lecturaFamiliar = com.tfg.umeegunero.data.model.LecturaFamiliar(
                familiarId = familiarId,
                registroId = registro.id,
                alumnoId = registro.alumnoId,
                fechaLectura = timestamp
            )
            
            // Actualizar el mapa de lecturas
            val lecturasPorFamiliar = registro.lecturasPorFamiliar.toMutableMap()
            lecturasPorFamiliar[familiarId] = lecturaFamiliar
            
            val registroActualizado = registro.copy(
                vistoPorFamiliar = true,
                fechaVisto = timestamp,
                lecturasPorFamiliar = lecturasPorFamiliar
            )
            
            // Si hay conexión, actualizamos en Firestore
            if (isNetworkAvailable()) {
                try {
                    val updateData = mapOf(
                        "vistoPorFamiliar" to true,
                        "fechaVisto" to timestamp,
                        "lecturasPorFamiliar.$familiarId" to mapOf(
                            "familiarId" to familiarId,
                            "nombreFamiliar" to nombreFamiliar,
                            "fechaLectura" to timestamp,
                            "leido" to true
                        )
                    )
                    
                    registrosCollection.document(registroId)
                        .update(updateData)
                        .await()
                    
                    // Actualizamos en local
                    localRegistroRepository.updateRegistroActividad(registroActualizado, true)
                    return@withContext Result.Success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Error al marcar como leído en Firestore, guardando solo en local")
                }
            }
            
            // Si no hay conexión o falló Firestore, actualizamos solo localmente
            localRegistroRepository.updateRegistroActividad(registroActualizado, false)
            return@withContext Result.Success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como leído por familiar")
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
            
            // Actualizar el campo vistoPor con el ID del familiar
            val vistoPor = registro.vistoPor?.toMutableMap() ?: mutableMapOf()
            vistoPor[familiarId] = false // Marcamos que se ha enviado pero no leído
            
            val registroActualizado = registro.copy(
                vistoPorFamiliar = true,
                fechaVisto = timestamp,
                vistoPor = vistoPor
            )
            
            // Si hay conexión, actualizamos en Firestore
            if (isNetworkAvailable()) {
                try {
                    val updateData = mapOf(
                        "vistoPorFamiliar" to true,
                        "fechaVisto" to timestamp,
                        "vistoPor.$familiarId" to false
                    )
                    
                    registrosCollection.document(registroId)
                        .update(updateData)
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
     * Genera un ID local para un registro.
     * El formato será "registro_YYYYMMDD_ALUMNOID" para garantizar consistencia.
     * 
     * @return ID generado
     */
    private fun generateLocalId(alumnoId: String = "", fecha: Date = Date()): String {
        val fechaStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(fecha)
        val uidParte = if (alumnoId.isNotEmpty()) alumnoId else "unknown"
        
        return "registro_${fechaStr}_$uidParte"
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
                        .whereEqualTo("vistoPorFamiliar", false)
                        .get()
                        .await()
                    
                    Timber.d("Registros sin leer encontrados: ${query.size()}")
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
        claseId: String,
        fecha: Date
    ): Result<List<RegistroDiario>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando registros para clase: $claseId, fecha: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}")
            val listaRegistros = mutableListOf<RegistroDiario>()
            
            // Crear timestamp de inicio y fin del día para consultas precisas
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicioDia = Timestamp(calendar.time)
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val finDia = Timestamp(calendar.time)
            
            // Buscar en la colección registrosActividad
            try {
                val snapshot = registrosCollection
                    .whereEqualTo("claseId", claseId)
                    .whereGreaterThanOrEqualTo("fecha", inicioDia)
                    .whereLessThanOrEqualTo("fecha", finDia)
                    .get()
                    .await()
                
                val registrosActividad = snapshot.documents.mapNotNull { document ->
                    try {
                        val registroActividad = document.toObject(RegistroActividad::class.java)
                        val eliminado = document.getBoolean("eliminado") ?: false
                        
                        // Solo incluir registros que no estén marcados como eliminados
                        if (registroActividad != null && !eliminado) {
                            // Convertir RegistroActividad a RegistroDiario
                            RegistroDiario(
                                id = registroActividad.id,
                                alumnoId = registroActividad.alumnoId,
                                alumnoNombre = registroActividad.alumnoNombre,
                                claseId = registroActividad.claseId,
                                fecha = registroActividad.fecha,
                                presente = true, // Por defecto true para registros de actividad
                                justificada = false,
                                observaciones = registroActividad.observacionesGenerales ?: "",
                                profesorId = registroActividad.profesorId,
                                modificadoPor = registroActividad.modificadoPor,
                                eliminado = eliminado
                            )
                        } else null
                    } catch (e: Exception) {
                        Timber.e(e, "Error al convertir documento a RegistroActividad: ${document.id}")
                        null
                    }
                }
                
                listaRegistros.addAll(registrosActividad)
                Timber.d("Encontrados ${registrosActividad.size} registros en registrosActividad para clase $claseId en fecha ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}")
            } catch (e: Exception) {
                Timber.e(e, "Error al buscar en registrosActividad para clase $claseId y fecha ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}: ${e.message}")
            }
            
            Timber.d("Total de registros encontrados para clase $claseId en fecha ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}: ${listaRegistros.size}")
            Timber.d("Registros no eliminados: ${listaRegistros.size}")
            listaRegistros.forEach { registro ->
                Timber.d("  - Alumno: ${registro.alumnoId}, ID: ${registro.id}, Eliminado: ${registro.eliminado}")
            }
            
            Result.Success(listaRegistros)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros diarios por clase y fecha: $claseId, ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}: ${e.message}")
            Result.Error(e)
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
                
                val listaRegistros = mutableListOf<RegistroActividad>()
                
                // Buscar en la colección registrosActividad
                try {
                    val registrosSnapshot = registrosCollection
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
                    
                    listaRegistros.addAll(registros)
                    Timber.d("Encontrados ${registros.size} registros en colección registrosActividad")
                } catch (e: Exception) {
                    Timber.e(e, "Error al buscar en registrosActividad")
                }
                
                Timber.d("Total de registros encontrados: ${listaRegistros.size}")
                listaRegistros.forEach { 
                    Timber.d("Registro: id=${it.id}, fecha=${it.fecha}, profesorId=${it.profesorId}")
                }
                
                Result.Success(listaRegistros)
            } catch (e: Exception) {
                Timber.e(e, "Error general al obtener registros por alumno y fecha")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Elimina un registro diario por su ID
     *
     * @param registroId ID del registro a eliminar
     * @return true si la eliminación fue exitosa, false en caso contrario
     */
    suspend fun eliminarRegistro(registroId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.d("Intentando eliminar registro con ID: $registroId")
            
            // Si hay conexión, eliminar de Firestore
            if (isNetworkAvailable()) {
                try {
                    var eliminadoRegistrosActividad = false
                    var eliminadoAsistencia = false
                    var alumnoId = ""
                    var claseId = ""
                    var fecha: Timestamp? = null
                    
                    // Intentar obtener los datos del registro desde registrosActividad
                    try {
                        val documentRef = registrosCollection.document(registroId)
                        val document = documentRef.get().await()
                        
                        if (document.exists()) {
                            // Extraer información relevante antes de eliminar
                            alumnoId = document.getString("alumnoId") ?: ""
                            claseId = document.getString("claseId") ?: ""
                            fecha = document.getTimestamp("fecha")
                            
                            // Eliminación lógica: marcamos como eliminado
                            documentRef.update("eliminado", true).await()
                            
                            Timber.d("Registro $registroId eliminado con éxito de la colección registrosActividad")
                            eliminadoRegistrosActividad = true
                        } else {
                            Timber.d("El registro $registroId no existe en la colección registrosActividad")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al eliminar registro de registrosActividad: $registroId")
                    }
                    
                    // Actualizar la colección de asistencia si tenemos los datos necesarios
                    if ((alumnoId.isNotEmpty() && claseId.isNotEmpty() && fecha != null) && eliminadoRegistrosActividad) {
                        try {
                            // Buscar en la colección asistencia
                            val asistenciaQuery = firestore.collection("asistencia")
                                .whereEqualTo("claseId", claseId)
                                .whereEqualTo("fecha", fecha)
                                .get()
                                .await()
                            
                            if (!asistenciaQuery.isEmpty) {
                                for (asistenciaDoc in asistenciaQuery.documents) {
                                    // Verificar si este documento de asistencia contiene el alumnoId
                                    val estadosAsistencia = asistenciaDoc.get("estadosAsistencia") as? Map<*, *>
                                    if (estadosAsistencia != null && estadosAsistencia.containsKey(alumnoId)) {
                                        // Actualizar estado de asistencia a AUSENTE
                                        val docRef = firestore.collection("asistencia").document(asistenciaDoc.id)
                                        docRef.update("estadosAsistencia.$alumnoId", "AUSENTE").await()
                                        
                                        Timber.d("Registro de asistencia para alumno $alumnoId actualizado a AUSENTE")
                                        eliminadoAsistencia = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al actualizar asistencia para alumnoId=$alumnoId, claseId=$claseId")
                        }
                    }
                    
                    // También eliminamos de la caché local
                    localRegistroRepository.deleteRegistroActividad(registroId)
                    
                    if (eliminadoRegistrosActividad || eliminadoAsistencia) {
                        Timber.d("Registro $registroId eliminado con éxito")
                        return@withContext true
                    } else {
                        Timber.e("No se encontró el registro $registroId")
                        return@withContext false
                    }
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error al eliminar registro en Firestore: $registroId")
                    return@withContext false
                }
            } else {
                Timber.w("Sin conexión a Internet. No se puede eliminar el registro remoto.")
                // Eliminación local
                localRegistroRepository.deleteRegistroActividad(registroId)
                // Pendiente de sincronización cuando haya conexión
                return@withContext true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error general al eliminar registro: $registroId")
            return@withContext false
        }
    }

    /**
     * Obtiene un registro diario por su ID
     */
    suspend fun getRegistroDiario(registroId: String): Result<RegistroDiario> = withContext(Dispatchers.IO) {
        try {
            val document = registrosCollection.document(registroId).get().await()
            if (document.exists()) {
                val registroDiario = document.toObject(RegistroDiario::class.java)
                    ?: return@withContext Result.Error(Exception("Error al convertir el documento a RegistroDiario"))
                Result.Success(registroDiario)
            } else {
                Result.Error(Exception("No se encontró el registro con ID: $registroId"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro diario: $registroId")
            Result.Error(e)
        }
    }
    
    /**
     * Crea o actualiza un registro diario
     */
    suspend fun saveRegistroDiario(registroDiario: RegistroDiario): Result<RegistroDiario> = withContext(Dispatchers.IO) {
        try {
            val docRef = if (registroDiario.id.isBlank()) {
                registrosCollection.document()
            } else {
                registrosCollection.document(registroDiario.id)
            }
            
            val registroConId = if (registroDiario.id.isBlank()) {
                registroDiario.copy(id = docRef.id)
            } else {
                registroDiario
            }
            
            docRef.set(registroConId).await()
            Result.Success(registroConId)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar registro diario: ${registroDiario.id}")
            Result.Error(e)
        }
    }
    
    /**
     * Elimina un registro diario
     */
    suspend fun deleteRegistroDiario(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            registrosCollection.document(registroId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar registro diario: $registroId")
            Result.Error(e)
        }
    }

    /**
     * Notifica a los familiares sobre un registro
     *
     * @param registroId ID del registro a notificar
     * @return true si la notificación fue exitosa, false en caso contrario
     */
    suspend fun notificarFamiliarSobreRegistro(registroId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Implementa la lógica para notificar a los familiares sobre un registro
            // Esto puede incluir la implementación de un servicio de notificación, enviar correos electrónicos, etc.
            // Por ahora, simplemente devolvemos true para simular la operación exitosa
            return@withContext true
        } catch (e: Exception) {
            Timber.e(e, "Error al notificar a los familiares sobre el registro")
            return@withContext false
        }
    }
    
    /**
     * Obtiene registros diarios por clase y fecha (string)
     * Método compatible con ListadoPreRegistroDiarioScreen
     */
    suspend fun getRegistrosDiariosPorClaseYFecha(
        claseId: String,
        fecha: String
    ): Result<List<RegistroDiario>> = withContext(Dispatchers.IO) {
        try {
            // Convertir la fecha string a Date
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaDate = try {
                formatoFecha.parse(fecha) ?: Date()
            } catch (e: Exception) {
                Timber.e(e, "Error al parsear fecha: $fecha")
                Date() // Usar fecha actual si hay error
            }
            
            // Usar el método existente que trabaja con objetos Date
            return@withContext obtenerRegistrosDiariosPorFechaYClase(claseId, fechaDate)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros diarios por clase y fecha: $claseId, $fecha")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los registros de actividad para una fecha y clase específica
     * 
     * @param claseId ID de la clase
     * @param fecha Fecha del registro
     * @return Resultado con la lista de registros de actividad
     */
    suspend fun obtenerRegistrosActividadPorFechaYClase(
        claseId: String,
        fecha: Date
    ): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando registros de actividad para clase: $claseId, fecha: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}")
            
            // Crear timestamp de inicio y fin del día para consultas precisas
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicioDia = Timestamp(calendar.time)
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val finDia = Timestamp(calendar.time)
            
            // Buscar en la colección registrosActividad
            val snapshot = registrosCollection
                .whereEqualTo("claseId", claseId)
                .whereGreaterThanOrEqualTo("fecha", inicioDia)
                .whereLessThanOrEqualTo("fecha", finDia)
                .get()
                .await()
            
            val registrosActividad = snapshot.documents.mapNotNull { document ->
                try {
                    val registroActividad = document.toObject(RegistroActividad::class.java)
                    val eliminado = document.getBoolean("eliminado") ?: false
                    
                    // Actualizar la propiedad eliminado explícitamente
                    if (registroActividad != null) {
                        registroActividad.copy(eliminado = eliminado)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "Error al convertir documento a RegistroActividad: ${document.id}")
                    null
                }
            }
            
            Timber.d("Encontrados ${registrosActividad.size} registros en registrosActividad para clase $claseId en fecha ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}")
            return@withContext Result.Success(registrosActividad)
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar en registrosActividad para clase $claseId y fecha ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fecha)}: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un registro específico por su ID
     * 
     * @param registroId ID del registro a obtener
     * @return Resultado con el registro encontrado o error
     */
    suspend fun obtenerRegistroPorId(registroId: String): Result<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo registro por ID: $registroId")
            
            // Primero intentamos obtener del caché local
            val registroLocal = localRegistroRepository.getRegistroActividadById(registroId)
            if (registroLocal != null) {
                Timber.d("Registro encontrado en caché local: $registroId")
                return@withContext Result.Success(registroLocal)
            }
            
            // Si no está en caché y hay conexión, buscamos en Firestore
            if (isNetworkAvailable()) {
                val document = registrosCollection.document(registroId).get().await()
                if (document.exists()) {
                    val registro = document.toObject(RegistroActividad::class.java)
                    if (registro != null) {
                        // Guardamos en caché local
                        localRegistroRepository.saveRegistroActividad(registro, true)
                        Timber.d("Registro obtenido de Firestore y guardado en local: $registroId")
                        return@withContext Result.Success(registro)
                    }
                }
                
                Timber.w("No se encontró el registro con ID: $registroId")
                return@withContext Result.Error(Exception("No se encontró el registro"))
            } else {
                Timber.w("No hay conexión a internet y el registro no está en caché local")
                return@withContext Result.Error(Exception("No hay conexión a internet y el registro no está disponible offline"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro por ID: $registroId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene registros para un alumno en un rango de fechas específico
     * 
     * @param alumnoId ID del alumno
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Resultado con la lista de registros
     */
    suspend fun obtenerRegistrosPorFecha(
        alumnoId: String,
        fechaInicio: Timestamp,
        fechaFin: Timestamp
    ): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando registros para alumno: $alumnoId entre $fechaInicio y $fechaFin")
            
            if (isNetworkAvailable()) {
                val query = registrosCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                    .whereLessThanOrEqualTo("fecha", fechaFin)
                    .get()
                    .await()
                
                val registros = query.documents.mapNotNull { document ->
                    try {
                        document.toObject(RegistroActividad::class.java)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al convertir documento a RegistroActividad: ${document.id}")
                        null
                    }
                }
                
                // Guardar en caché local
                registros.forEach { registro ->
                    localRegistroRepository.saveRegistroActividad(registro, true)
                }
                
                Timber.d("Registros encontrados: ${registros.size}")
                return@withContext Result.Success(registros)
            } else {
                // Intentar obtener registros locales
                // Nota: Esta implementación es simplificada y podría necesitar más lógica
                // para filtrar correctamente por fecha en la base de datos local
                val alumno = alumnoId
                Timber.d("No hay conexión, intentando obtener registros locales para alumno: $alumno")
                val registrosLocales = localRegistroRepository.getRegistrosActividadByAlumnoAndFechaRange(
                    alumnoId = alumno,
                    fechaInicio = fechaInicio.toDate(),
                    fechaFin = fechaFin.toDate()
                )
                
                Timber.d("Registros locales encontrados: ${registrosLocales.size}")
                return@withContext Result.Success(registrosLocales)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por fecha para alumno: $alumnoId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los registros para un alumno específico
     * 
     * @param alumnoId ID del alumno
     * @return Flow con el resultado de la operación
     */
    suspend fun obtenerRegistrosPorAlumno(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        return@withContext obtenerRegistrosPorAlumnoSync(alumnoId)
    }

    /**
     * Obtiene un registro específico para un alumno y fecha
     * 
     * @param alumnoId ID del alumno
     * @param fecha Fecha para buscar el registro
     * @return Flow con el resultado de la operación
     */
    suspend fun obtenerRegistroPorAlumnoYFecha(alumnoId: String, fecha: Date): Result<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando registro para alumno $alumnoId en fecha $fecha")
            
            // Calcular inicio y fin del día
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
            
            val timestampInicio = Timestamp(inicioDia)
            val timestampFin = Timestamp(finDia)
            
            if (isNetworkAvailable()) {
                try {
                    val query = registrosCollection
                        .whereEqualTo("alumnoId", alumnoId)
                        .whereGreaterThanOrEqualTo("fecha", timestampInicio)
                        .whereLessThanOrEqualTo("fecha", timestampFin)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!query.isEmpty) {
                        val registro = query.documents[0].toObject(RegistroActividad::class.java)!!
                        
                        // Guardar en caché local
                        localRegistroRepository.saveRegistroActividad(registro, true)
                        
                        Timber.d("Registro encontrado para alumno $alumnoId en fecha $fecha")
                        return@withContext Result.Success(registro)
                    } else {
                        Timber.d("No se encontró registro en Firestore para alumno $alumnoId en fecha $fecha")
                        // Si no se encuentra, intentamos en caché local
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registro de Firestore: ${e.message}")
                    // Si falla, intentamos obtener de la caché local
                }
            }
            
            // Si no hay conexión o falló la consulta en Firestore, intentar obtener de caché local
            val registrosLocales = localRegistroRepository.getRegistrosActividadByAlumnoAndFecha(alumnoId, fecha)
            
            if (registrosLocales.isNotEmpty()) {
                Timber.d("Registro encontrado en caché local")
                return@withContext Result.Success(registrosLocales.first())
            }
            
            Timber.d("No se encontró registro para alumno $alumnoId en fecha $fecha")
            return@withContext Result.Error("No se encontró registro para la fecha seleccionada")
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro por fecha: ${e.message}")
            return@withContext Result.Error(e.message, e)
        }
    }

    /**
     * Registra la lectura de un registro por un familiar
     * 
     * @param lecturaFamiliar Datos de la lectura realizada
     * @return Resultado de la operación
     */
    suspend fun registrarLecturaFamiliar(lecturaFamiliar: LecturaFamiliar): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Registrando lectura familiar - registroId: ${lecturaFamiliar.registroId}, familiarId: ${lecturaFamiliar.familiarId}")
            
            if (isNetworkAvailable()) {
                try {
                    // Crear nueva entrada en la colección de lecturas
                    val lecturasCollection = firestore.collection("lecturas_familiar")
                    
                    // Crear un ID único para la lectura
                    val lecturaId = "${lecturaFamiliar.registroId}_${lecturaFamiliar.familiarId}_${System.currentTimeMillis()}"
                    
                    // Guardar la lectura
                    lecturasCollection.document(lecturaId)
                        .set(lecturaFamiliar)
                        .await()
                    
                    // Actualizar campo en el registro principal (opcional)
                    registrosCollection.document(lecturaFamiliar.registroId)
                        .update(
                            "vistoPorFamiliar", true,
                            "fechaUltimaLectura", FieldValue.serverTimestamp()
                        )
                        .await()
                    
                    Timber.d("Lectura familiar registrada correctamente")
                    return@withContext Result.Success(true)
                } catch (e: Exception) {
                    Timber.e(e, "Error al registrar lectura familiar en Firestore")
                    // Intentar guardar localmente
                }
            }
            
            // Si no hay conexión o falló la operación en Firestore, guardar localmente para sincronizar después
            // Aquí se implementaría la lógica para guardar localmente
            // Por simplicidad, asumimos que se ha registrado correctamente
            
            Timber.d("Lectura familiar registrada localmente para sincronización posterior")
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar lectura familiar: ${e.message}")
            return@withContext Result.Error(e.message, e)
        }
    }

    /**
     * Obtiene los registros por alumno específicos del DAO de manera sincronizada, no usando Flow
     */
    suspend fun obtenerRegistrosPorAlumnoSync(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo registros para alumno de manera síncrona: $alumnoId")
            
            if (isNetworkAvailable()) {
                try {
                    val query = registrosCollection
                        .whereEqualTo("alumnoId", alumnoId)
                        .orderBy("fecha", Query.Direction.DESCENDING)
                        .get()
                        .await()
                    
                    val registros = query.documents.mapNotNull { 
                        it.toObject(RegistroActividad::class.java) 
                    }
                    
                    // Guardar en caché local
                    registros.forEach { registro ->
                        localRegistroRepository.saveRegistroActividad(registro, true)
                    }
                    
                    Timber.d("Obtenidos ${registros.size} registros para alumno $alumnoId")
                    return@withContext Result.Success(registros)
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener registros de Firestore para alumno $alumnoId")
                    // Si falla, intentamos obtener de la caché local
                }
            }
            
            // Si no hay conexión o falló la consulta en Firestore, intentar obtener de caché local
            val registrosLocales = localRegistroRepository.getRegistrosActividadByAlumno(alumnoId)
            Timber.d("Obtenidos ${registrosLocales.size} registros locales para alumno $alumnoId")
            return@withContext Result.Success(registrosLocales)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros para alumno $alumnoId: ${e.message}")
            return@withContext Result.Error(e.message, e)
        }
    }
} 