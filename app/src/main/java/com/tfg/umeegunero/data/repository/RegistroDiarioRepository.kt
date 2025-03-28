package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.RegistroDiario
import com.tfg.umeegunero.data.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistroDiarioRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val registrosCollection = firestore.collection("registrosDiarios")
    
    /**
     * Obtiene o crea un registro diario para un alumno y fecha específicos
     */
    suspend fun obtenerOCrearRegistroDiario(
        alumnoId: String,
        claseId: String,
        profesorId: String,
        fecha: Date = Date()
    ): Result<RegistroDiario> = withContext(Dispatchers.IO) {
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
            
            // Buscar si ya existe un registro para este alumno en esta fecha
            val query = registrosCollection
                .whereEqualTo("alumnoId", alumnoId)
                .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDia))
                .whereLessThanOrEqualTo("fecha", Timestamp(finDia))
                .limit(1)
                .get()
                .await()
            
            if (!query.isEmpty) {
                // Si existe, devolver el registro existente
                val registroExistente = query.documents[0].toObject(RegistroDiario::class.java)
                return@withContext Result.Success(registroExistente!!)
            } else {
                // Si no existe, crear uno nuevo
                val nuevoRegistro = RegistroDiario(
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
                
                return@withContext Result.Success(registroConId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener/crear registro diario para alumno $alumnoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Actualiza un registro diario existente
     */
    suspend fun actualizarRegistroDiario(registro: RegistroDiario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val registroActualizado = registro.copy(
                ultimaModificacion = Timestamp.now(),
                modificadoPor = registro.modificadoPor
            )
            
            registrosCollection.document(registro.id)
                .set(registroActualizado)
                .await()
                
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar registro diario ${registro.id}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Marca un registro como visualizado por los familiares
     */
    suspend fun marcarComoVisualizado(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            registrosCollection.document(registroId)
                .update(
                    mapOf(
                        "visualizadoPorFamiliar" to true,
                        "fechaVisualizacion" to Timestamp.now()
                    )
                )
                .await()
                
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como visualizado: $registroId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los registros diarios de un alumno
     */
    fun obtenerRegistrosAlumno(alumnoId: String, limit: Long = 30): Flow<Result<List<RegistroDiario>>> = flow {
        emit(Result.Loading)
        
        try {
            val query = registrosCollection
                .whereEqualTo("alumnoId", alumnoId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
                
            val registros = query.toObjects(RegistroDiario::class.java)
            emit(Result.Success(registros))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros del alumno $alumnoId")
            emit(Result.Error(e))
        }
    }
    
    /**
     * Obtiene todos los registros de una clase para una fecha específica
     */
    suspend fun obtenerRegistrosPorClaseYFecha(
        claseId: String,
        fecha: Date = Date()
    ): Result<List<RegistroDiario>> = withContext(Dispatchers.IO) {
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
            
            val query = registrosCollection
                .whereEqualTo("claseId", claseId)
                .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDia))
                .whereLessThanOrEqualTo("fecha", Timestamp(finDia))
                .get()
                .await()
                
            val registros = query.toObjects(RegistroDiario::class.java)
            return@withContext Result.Success(registros)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros para la clase $claseId en fecha específica")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene los registros no visualizados para un familiar específico
     */
    suspend fun obtenerRegistrosNoVisualizados(
        alumnosIds: List<String>
    ): Result<List<RegistroDiario>> = withContext(Dispatchers.IO) {
        try {
            if (alumnosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            val resultados = mutableListOf<RegistroDiario>()
            
            // Consultamos por cada alumno (no se puede usar whereIn con otro filtro complejo)
            for (alumnoId in alumnosIds) {
                val query = registrosCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .whereEqualTo("visualizadoPorFamiliar", false)
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    
                resultados.addAll(query.toObjects(RegistroDiario::class.java))
            }
            
            return@withContext Result.Success(resultados)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros no visualizados")
            return@withContext Result.Error(e)
        }
    }
} 