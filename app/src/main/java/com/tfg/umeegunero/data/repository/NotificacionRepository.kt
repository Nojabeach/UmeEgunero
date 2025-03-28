package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.EstadoNotificacion
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.NotificacionForm
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.TipoDestino
import com.tfg.umeegunero.data.model.TipoNotificacion
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

/**
 * Repositorio encargado de la gestión de notificaciones en la aplicación
 */
@Singleton
class NotificacionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val notificacionesCollection = firestore.collection("notificaciones")
    
    /**
     * Obtiene las notificaciones para un usuario específico
     * @param usuarioId ID del usuario
     * @param limit Límite de notificaciones a recuperar
     * @return Flow con el resultado de la operación conteniendo la lista de notificaciones
     */
    fun getNotificacionesUsuario(usuarioId: String, limit: Long = 50): Flow<Result<List<Notificacion>>> = flow {
        emit(Result.Loading)
        try {
            val query = notificacionesCollection
                .whereEqualTo("usuarioDestinatarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(limit)
                
            val snapshot = query.get().await()
            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)
            }
            
            emit(Result.Success(notificaciones))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener notificaciones para el usuario $usuarioId")
            emit(Result.Error(e))
        }
    }
    
    /**
     * Obtiene notificaciones del sistema (generales)
     * @return Flow con el resultado de la operación conteniendo la lista de notificaciones del sistema
     */
    fun getNotificacionesSistema(limit: Long = 20): Flow<Result<List<Notificacion>>> = flow {
        emit(Result.Loading)
        try {
            val query = notificacionesCollection
                .whereEqualTo("tipo", TipoNotificacion.SISTEMA.name)
                .whereEqualTo("usuarioDestinatarioId", "")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(limit)
                
            val snapshot = query.get().await()
            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)
            }
            
            emit(Result.Success(notificaciones))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener notificaciones del sistema")
            emit(Result.Error(e))
        }
    }
    
    /**
     * Obtiene notificaciones para un centro específico
     * @param centroId ID del centro educativo
     * @return Flow con el resultado de la operación conteniendo la lista de notificaciones del centro
     */
    fun getNotificacionesCentro(centroId: String, limit: Long = 30): Flow<Result<List<Notificacion>>> = flow {
        emit(Result.Loading)
        try {
            val query = notificacionesCollection
                .whereEqualTo("centroId", centroId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(limit)
                
            val snapshot = query.get().await()
            val notificaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)
            }
            
            emit(Result.Success(notificaciones))
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener notificaciones para el centro $centroId")
            emit(Result.Error(e))
        }
    }
    
    /**
     * Obtiene las notificaciones de un centro específico
     * @param centroId ID del centro
     * @return Lista de notificaciones del centro
     */
    suspend fun getNotificacionesByCentro(centroId: String): List<Notificacion> = withContext(Dispatchers.IO) {
        try {
            val query = notificacionesCollection
                .whereEqualTo("centroId", centroId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                
            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener notificaciones para el centro $centroId")
            emptyList()
        }
    }
    
    /**
     * Marca una notificación como leída
     * @param notificacionId ID de la notificación
     * @return Result indicando éxito o error
     */
    suspend fun marcarComoLeida(notificacionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            notificacionesCollection.document(notificacionId)
                .update("leida", true)
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar notificación $notificacionId como leída")
            Result.Error(e)
        }
    }
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     * @param usuarioId ID del usuario
     * @return Result indicando éxito o error
     */
    suspend fun marcarTodasComoLeidas(usuarioId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batch = firestore.batch()
            
            val query = notificacionesCollection
                .whereEqualTo("usuarioDestinatarioId", usuarioId)
                .whereEqualTo("leida", false)
                
            val snapshot = query.get().await()
            
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "leida", true)
            }
            
            batch.commit().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar todas las notificaciones como leídas para el usuario $usuarioId")
            Result.Error(e)
        }
    }
    
    /**
     * Crea una nueva notificación
     * @param notificacion Objeto Notificacion a crear
     * @return Result con la notificación creada o error
     */
    suspend fun crearNotificacion(notificacion: Notificacion): Result<Notificacion> = withContext(Dispatchers.IO) {
        try {
            val docRef = notificacionesCollection.document()
            val notificacionConId = notificacion.copy(id = docRef.id)
            
            docRef.set(notificacionConId).await()
            Result.Success(notificacionConId)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear notificación")
            Result.Error(e)
        }
    }
    
    /**
     * Crea una nueva notificación (versión directa sin Result)
     * @param notificacion Objeto Notificacion a crear
     */
    suspend fun createNotificacion(notificacion: Notificacion) = withContext(Dispatchers.IO) {
        try {
            notificacionesCollection.add(notificacion).await()
        } catch (e: Exception) {
            Timber.e(e, "Error al crear notificación")
            throw e
        }
    }
    
    /**
     * Elimina una notificación
     * @param notificacionId ID de la notificación a eliminar
     * @return Result indicando éxito o error
     */
    suspend fun eliminarNotificacion(notificacionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            notificacionesCollection.document(notificacionId)
                .delete()
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar notificación $notificacionId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene el número de notificaciones no leídas para un usuario
     * @param usuarioId ID del usuario
     * @return Result con el conteo o error
     */
    suspend fun getConteoNoLeidas(usuarioId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val query = notificacionesCollection
                .whereEqualTo("usuarioDestinatarioId", usuarioId)
                .whereEqualTo("leida", false)
                
            val snapshot = query.get().await()
            Result.Success(snapshot.size())
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener conteo de notificaciones no leídas para usuario $usuarioId")
            Result.Error(e)
        }
    }
    
    /**
     * Envía una notificación a todos los usuarios de un centro
     * @param centroId ID del centro
     * @param titulo Título de la notificación
     * @param mensaje Mensaje de la notificación
     * @param tipo Tipo de notificación
     * @return Result indicando éxito o error
     */
    suspend fun enviarNotificacionCentro(
        centroId: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.SISTEMA
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notificacion = Notificacion(
                titulo = titulo,
                mensaje = mensaje,
                fecha = Timestamp.now(),
                tipo = tipo,
                centroId = centroId
            )
            
            notificacionesCollection.add(notificacion).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar notificación al centro $centroId")
            Result.Error(e)
        }
    }
} 