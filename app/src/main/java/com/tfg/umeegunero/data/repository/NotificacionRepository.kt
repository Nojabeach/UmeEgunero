package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.EstadoNotificacion
import com.tfg.umeegunero.data.model.Notificacion
import com.tfg.umeegunero.data.model.NotificacionForm
import com.tfg.umeegunero.util.Result
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
import java.util.UUID

/**
 * Repositorio para gestionar notificaciones en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, enviar, recuperar y gestionar
 * notificaciones para diferentes tipos de usuarios (profesores, familiares, 
 * administradores, alumnos).
 *
 * Características principales:
 * - Creación de notificaciones personalizadas
 * - Envío de notificaciones push
 * - Gestión de preferencias de notificación
 * - Seguimiento de notificaciones leídas/no leídas
 *
 * El repositorio maneja diferentes tipos de notificaciones como:
 * - Comunicados
 * - Recordatorios de tareas
 * - Alertas de actividades
 * - Mensajes nuevos
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property messaging Servicio de Firebase Cloud Messaging para notificaciones push
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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
        emit(Result.Loading())
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
        emit(Result.Loading())
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
        emit(Result.Loading())
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
            if (e.message?.contains("FAILED_PRECONDITION") == true && e.message?.contains("requires an index") == true) {
                // Error específico de índice faltante en Firestore
                Timber.e(e, "Error al obtener notificaciones para el centro $centroId (falta índice en Firestore)")
                
                // Intentar una consulta más simple como fallback
                try {
                    val fallbackQuery = notificacionesCollection
                        .whereEqualTo("centroId", centroId)
                        .limit(limit)
                        
                    val fallbackSnapshot = fallbackQuery.get().await()
                    val fallbackNotificaciones = fallbackSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Notificacion::class.java)
                    }.sortedByDescending { it.fecha }
                    
                    emit(Result.Success(fallbackNotificaciones))
                } catch (fallbackEx: Exception) {
                    Timber.e(fallbackEx, "Error en el fallback para notificaciones del centro $centroId")
                    emit(Result.Error(fallbackEx))
                }
            } else {
                // Otro tipo de error
                Timber.e(e, "Error al obtener notificaciones para el centro $centroId")
                emit(Result.Error(e))
            }
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
            if (e.message?.contains("FAILED_PRECONDITION") == true && e.message?.contains("requires an index") == true) {
                // Error específico de índice faltante en Firestore
                Timber.e(e, "Error al obtener notificaciones para el centro $centroId (falta índice en Firestore)")
                
                // Intentar una consulta más simple como fallback
                try {
                    val fallbackQuery = notificacionesCollection
                        .whereEqualTo("centroId", centroId)
                        
                    val fallbackSnapshot = fallbackQuery.get().await()
                    fallbackSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Notificacion::class.java)
                    }.sortedByDescending { it.fecha }
                } catch (fallbackEx: Exception) {
                    Timber.e(fallbackEx, "Error en el fallback para notificaciones del centro $centroId")
                    emptyList()
                }
            } else {
                // Otro tipo de error
                Timber.e(e, "Error al obtener notificaciones para el centro $centroId")
                emptyList()
            }
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
    suspend fun crearNotificacion(notificacion: Notificacion): Result<String> {
        return try {
            val docId = if (notificacion.id.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                notificacion.id
            }
            
            val docRef = notificacionesCollection.document(docId)
            val notificacionConId = notificacion.copy(id = docId)
            
            docRef.set(notificacionConId).await()
            
            // Envío de notificación push si corresponde
            enviarNotificacionPush(notificacionConId)
            
            Result.Success(docId)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear notificación")
            Result.Error(e)
        }
    }
    
    /**
     * Crea una notificación asociada a un mensaje o comunicado
     * 
     * @param titulo Título de la notificación
     * @param mensaje Contenido de la notificación
     * @param tipo Tipo de notificación (comunicado, mensaje, etc.)
     * @param destinatarioId ID del usuario destinatario
     * @param destinatarioTipo Tipo del usuario destinatario
     * @param origenId ID del objeto origen (comunicado, mensaje, etc.)
     * @param centroId ID del centro (opcional)
     * @param accion Acción a realizar al pulsar (ruta de navegación)
     * @return Resultado con el ID de la notificación o error
     */
    suspend fun crearNotificacionMensaje(
        titulo: String,
        mensaje: String,
        tipo: String,
        destinatarioId: String,
        destinatarioTipo: String,
        origenId: String,
        centroId: String = "",
        accion: String = ""
    ): Result<String> {
        // Convertir el tipo String a TipoNotificacion
        val tipoNotificacion = when (tipo.lowercase()) {
            "comunicado" -> TipoNotificacion.COMUNICADO
            "mensaje" -> TipoNotificacion.MENSAJE
            "evento" -> TipoNotificacion.EVENTO
            "tarea" -> TipoNotificacion.TAREA
            "sistema" -> TipoNotificacion.SISTEMA
            else -> TipoNotificacion.GENERAL
        }
        
        val notificacion = Notificacion(
            titulo = titulo,
            mensaje = mensaje,
            tipo = tipoNotificacion,
            usuarioDestinatarioId = destinatarioId,
            centroId = centroId,
            fecha = Timestamp.now(),
            leida = false
        )
        
        return crearNotificacion(notificacion)
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

    /**
     * Envía una notificación push usando la integración con Firebase Cloud Messaging
     * 
     * Esta implementación se basa en el sistema existente que utiliza Cloud Functions
     * y Google Apps Script (GAS) como intermediarios para enviar notificaciones push.
     * La función detecta cuando se crea una nueva notificación en Firestore y automáticamente
     * dispara el envío de la notificación push al dispositivo del usuario.
     *
     * @param notificacion La notificación a enviar como push
     */
    private fun enviarNotificacionPush(notificacion: Notificacion) {
        // La notificación push se envía automáticamente mediante Cloud Functions
        // cuando se crea un documento en la colección "notificaciones"
        
        // No es necesario hacer nada aquí, ya que el trigger de Cloud Functions
        // se encarga de detectar el nuevo documento y enviar la notificación push
        
        Timber.d("✅ Notificación registrada en Firestore: ${notificacion.titulo}")
        Timber.d("   La Cloud Function enviará automáticamente la notificación push al dispositivo")
        Timber.d("   → Destinatario: ${notificacion.destinatarioId} (${notificacion.destinatarioTipo})")
        Timber.d("   → Título: ${notificacion.titulo}")
        Timber.d("   → Mensaje: ${notificacion.mensaje}")
        
        // El sistema utiliza tokens FCM almacenados en Firestore y Cloud Functions
        // para determinar a qué dispositivos enviar la notificación
    }
} 