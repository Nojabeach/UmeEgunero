package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.tfg.umeegunero.data.model.EstadoNotificacionAusencia
import com.tfg.umeegunero.data.model.NotificacionAusencia
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar las notificaciones de ausencia de los alumnos.
 */
@Singleton
class NotificacionAusenciaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val ausenciasCollection = firestore.collection("notificaciones_ausencia")
    
    /**
     * Registra una nueva notificación de ausencia.
     * 
     * @param notificacion La notificación a guardar
     * @return Resultado con la notificación guardada o error
     */
    suspend fun registrarAusencia(notificacion: NotificacionAusencia): Result<NotificacionAusencia> = withContext(Dispatchers.IO) {
        try {
            val notificacionId = if (notificacion.id.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                notificacion.id
            }
            
            val notificacionConId = notificacion.copy(
                id = notificacionId,
                fechaNotificacion = Timestamp.now()
            )
            
            ausenciasCollection.document(notificacionId)
                .set(notificacionConId)
                .await()
            
            Timber.d("Notificación de ausencia registrada con ID: $notificacionId")
            Result.Success(notificacionConId)
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar notificación de ausencia")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las notificaciones de ausencia para un alumno específico.
     * 
     * @param alumnoId ID del alumno
     * @return Resultado con la lista de notificaciones
     */
    suspend fun obtenerAusenciasPorAlumno(alumnoId: String): Result<List<NotificacionAusencia>> = withContext(Dispatchers.IO) {
        try {
            val query = ausenciasCollection
                .whereEqualTo("alumnoId", alumnoId)
                .orderBy("fechaNotificacion", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val notificaciones = query.documents.mapNotNull { document ->
                document.toObject(NotificacionAusencia::class.java)
            }
            
            Result.Success(notificaciones)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener ausencias del alumno: $alumnoId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las notificaciones de ausencia registradas por un familiar.
     * 
     * @param familiarId ID del familiar
     * @return Resultado con la lista de notificaciones
     */
    suspend fun obtenerAusenciasPorFamiliar(familiarId: String): Result<List<NotificacionAusencia>> = withContext(Dispatchers.IO) {
        try {
            val query = ausenciasCollection
                .whereEqualTo("familiarId", familiarId)
                .orderBy("fechaNotificacion", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val notificaciones = query.documents.mapNotNull { document ->
                document.toObject(NotificacionAusencia::class.java)
            }
            
            Result.Success(notificaciones)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener ausencias registradas por familiar: $familiarId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las notificaciones de ausencia para una clase específica.
     * 
     * @param claseId ID de la clase
     * @return Resultado con la lista de notificaciones
     */
    suspend fun obtenerAusenciasPorClase(claseId: String): Result<List<NotificacionAusencia>> = withContext(Dispatchers.IO) {
        try {
            val query = ausenciasCollection
                .whereEqualTo("claseId", claseId)
                .orderBy("fechaNotificacion", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val notificaciones = query.documents.mapNotNull { document ->
                document.toObject(NotificacionAusencia::class.java)
            }
            
            Result.Success(notificaciones)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener ausencias para clase: $claseId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las notificaciones de ausencia para una fecha específica.
     * 
     * @param fecha Fecha para filtrar
     * @return Resultado con la lista de notificaciones
     */
    suspend fun obtenerAusenciasPorFecha(fecha: Date): Result<List<NotificacionAusencia>> = withContext(Dispatchers.IO) {
        try {
            // Convertir la fecha a inicio y fin del día
            val calendar = java.util.Calendar.getInstance()
            calendar.time = fecha
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val inicioDia = calendar.time
            
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val finDia = calendar.time
            
            val timestampInicio = Timestamp(inicioDia)
            val timestampFin = Timestamp(finDia)
            
            val query = ausenciasCollection
                .whereGreaterThanOrEqualTo("fechaAusencia", timestampInicio)
                .whereLessThanOrEqualTo("fechaAusencia", timestampFin)
                .get()
                .await()
                
            val notificaciones = query.documents.mapNotNull { document ->
                document.toObject(NotificacionAusencia::class.java)
            }
            
            Result.Success(notificaciones)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener ausencias por fecha")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene las notificaciones de ausencia pendientes para una clase.
     * 
     * @param claseId ID de la clase
     * @return Resultado con la lista de notificaciones pendientes
     */
    suspend fun obtenerAusenciasPendientesPorClase(claseId: String): Result<List<NotificacionAusencia>> = withContext(Dispatchers.IO) {
        try {
            val query = ausenciasCollection
                .whereEqualTo("claseId", claseId)
                .whereEqualTo("estado", EstadoNotificacionAusencia.PENDIENTE.name)
                .orderBy("fechaNotificacion", Query.Direction.DESCENDING)
                .get()
                .await()
                
            val notificaciones = query.documents.mapNotNull { document ->
                document.toObject(NotificacionAusencia::class.java)
            }
            
            Result.Success(notificaciones)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener ausencias pendientes para clase: $claseId")
            Result.Error(e)
        }
    }
    
    /**
     * Actualiza el estado de una notificación de ausencia.
     * 
     * @param notificacionId ID de la notificación
     * @param estado Nuevo estado
     * @param profesorId ID del profesor que procesa la notificación
     * @return Resultado del proceso
     */
    suspend fun actualizarEstadoAusencia(
        notificacionId: String, 
        estado: EstadoNotificacionAusencia,
        profesorId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mapOf(
                "estado" to estado.name,
                "vistaPorProfesor" to true,
                "profesorId" to profesorId,
                "fechaVistoPorProfesor" to Timestamp.now()
            )
            
            ausenciasCollection.document(notificacionId)
                .update(updates)
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar estado de ausencia: $notificacionId")
            Result.Error(e)
        }
    }

    /**
     * Observa las ausencias de una clase en tiempo real
     * 
     * @param claseId ID de la clase a observar
     * @return Flow que emite las ausencias actualizadas cuando hay cambios
     */
    fun observarAusenciasPorClase(claseId: String): Flow<Result<List<NotificacionAusencia>>> = flow {
        emit(Result.Loading())
        
        firestore.collection("notificaciones_ausencia")
            .whereEqualTo("claseId", claseId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.isEmpty) {
                    val ausencias = snapshot.documents.mapNotNull { document ->
                        document.toObject(NotificacionAusencia::class.java)?.copy(id = document.id)
                    }
                    Result.Success(ausencias) as Result<List<NotificacionAusencia>>
                } else {
                    Result.Success(emptyList<NotificacionAusencia>())
                }
            }
            .catch { e ->
                Timber.e(e, "Error al observar ausencias por clase: $claseId")
                emit(Result.Error(e))
            }
            .collect { result ->
                emit(result)
            }
    }
} 