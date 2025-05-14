package com.tfg.umeegunero.data.repository

import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.local.dao.ChatMensajeDao
import com.tfg.umeegunero.data.local.dao.ConversacionDao
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.ChatMessage
import com.tfg.umeegunero.data.model.InteractionStatus
import com.tfg.umeegunero.data.model.local.MensajeEntity
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import com.tfg.umeegunero.data.model.ConversacionInfo

/**
 * Repositorio para gestionar la comunicación y conversaciones en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para interactuar con el sistema de mensajería,
 * permitiendo la creación, envío, recepción y gestión de conversaciones entre
 * diferentes tipos de usuarios (profesores, familiares, administradores).
 *
 * Características principales:
 * - Creación y gestión de conversaciones
 * - Envío y recepción de mensajes
 * - Manejo de adjuntos
 * - Seguimiento de mensajes no leídos
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property storage Instancia de FirebaseStorage para gestión de archivos adjuntos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class ChatRepository @Inject constructor(
    private val chatMensajeDao: ChatMensajeDao,
    private val conversacionDao: ConversacionDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "ChatRepository"
    
    init {
        // Iniciar sincronización en segundo plano al crear el repositorio
        scope.launch {
            try {
                sincronizarConversaciones()
            } catch (e: Exception) {
                Log.e(TAG, "Error en sincronización inicial de conversaciones", e)
            }
        }
    }
    
    /**
     * Obtiene todas las conversaciones de un usuario.
     */
    fun getConversaciones(usuarioId: String): Flow<List<ConversacionEntity>> {
        return conversacionDao.getConversacionesByUsuarioId(usuarioId)
    }
    
    /**
     * Obtiene las conversaciones con mensajes no leídos.
     */
    fun getConversacionesConNoLeidos(usuarioId: String): Flow<List<ConversacionEntity>> {
        return conversacionDao.getConversacionesConNoLeidos(usuarioId)
    }
    
    /**
     * Busca conversaciones por el nombre del otro participante.
     */
    fun buscarConversaciones(usuarioId: String, query: String): Flow<List<ConversacionEntity>> {
        return conversacionDao.buscarConversaciones(usuarioId, query)
    }
    
    /**
     * Obtiene el número total de mensajes no leídos para un usuario.
     */
    fun getTotalMensajesNoLeidos(usuarioId: String): Flow<Int> {
        return conversacionDao.getTotalMensajesNoLeidos(usuarioId)
    }
    
    /**
     * Obtiene una conversación específica por su ID.
     */
    suspend fun getConversacionById(conversacionId: String): ConversacionEntity? {
        return conversacionDao.getConversacionById(conversacionId)
    }
    
    /**
     * Obtiene o crea una conversación entre dos usuarios.
     */
    suspend fun getOrCreateConversacion(
        usuario1Id: String,
        usuario2Id: String,
        nombreUsuario1: String,
        nombreUsuario2: String,
        alumnoId: String? = null
    ): Result<ConversacionEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar conversación existente
                var conversacion = conversacionDao.getConversacionEntreUsuarios(usuario1Id, usuario2Id)
                
                if (conversacion == null) {
                    // Crear nueva conversación
                    val conversacionId = UUID.randomUUID().toString()
                    conversacion = ConversacionEntity(
                        id = conversacionId,
                        participante1Id = usuario1Id,
                        participante2Id = usuario2Id,
                        nombreParticipante1 = nombreUsuario1,
                        nombreParticipante2 = nombreUsuario2,
                        alumnoId = alumnoId
                    )
                    
                    // Guardar en Room
                    conversacionDao.insertConversacion(conversacion)
                    
                    // Guardar en Firestore
                    val conversacionMap = mapOf(
                        "id" to conversacionId,
                        "participante1Id" to usuario1Id,
                        "participante2Id" to usuario2Id,
                        "nombreParticipante1" to nombreUsuario1,
                        "nombreParticipante2" to nombreUsuario2,
                        "createdAt" to Timestamp.now(),
                        "updatedAt" to Timestamp.now(),
                        "alumnoId" to alumnoId
                    )
                    
                    firestore.collection("conversaciones")
                        .document(conversacionId)
                        .set(conversacionMap)
                        .await()
                }
                
                Result.Success(conversacion)
            } catch (e: Exception) {
                Log.e(TAG, "Error al crear/obtener conversación", e)
                Result.Error(Exception(e.message ?: "Error desconocido al crear conversación"))
            }
        }
    }
    
    /**
     * Obtiene los mensajes de una conversación
     */
    suspend fun getMensajesByConversacionId(conversacionId: String): List<MensajeEntity> {
        return try {
            val querySnapshot = firestore.collection("mensajes")
                .whereEqualTo("conversacionId", conversacionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val mensajes = querySnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                
                // Convertir los adjuntos
                val adjuntosList = (data["adjuntos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                
                // Crear entidad de mensaje
                MensajeEntity(
                    id = doc.id,
                    emisorId = data["emisorId"] as? String ?: "",
                    receptorId = data["receptorId"] as? String ?: "",
                    timestamp = (data["timestamp"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                    texto = data["texto"] as? String ?: "",
                    leido = data["leido"] as? Boolean ?: false,
                    fechaLeido = (data["fechaLeido"] as? Timestamp)?.toDate()?.time,
                    conversacionId = data["conversacionId"] as? String ?: "",
                    alumnoId = data["alumnoId"] as? String,
                    tipoAdjunto = data["tipoMensaje"] as? String,
                    adjuntos = adjuntosList
                )
            }
            
            mensajes
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensajes de la conversación $conversacionId")
            emptyList()
        }
    }
    
    /**
     * Obtiene los mensajes de una conversación paginados.
     */
    fun getMensajesPaginados(conversacionId: String): Flow<PagingData<ChatMensajeEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {
            chatMensajeDao.getMensajesPaginados(conversacionId)
        }.flow
    }
    
    /**
     * Envía un nuevo mensaje.
     */
    suspend fun enviarMensaje(mensaje: MensajeEntity): Result<String> {
        return try {
            // Crear mensaje en Firestore
            val mensajeData = mapOf(
                "emisorId" to mensaje.emisorId,
                "receptorId" to mensaje.receptorId,
                "timestamp" to Timestamp(java.util.Date(mensaje.timestamp)),
                "texto" to mensaje.texto,
                "leido" to false,
                "conversacionId" to mensaje.conversacionId,
                "alumnoId" to mensaje.alumnoId,
                "tipoMensaje" to (mensaje.tipoAdjunto ?: "TEXTO"),
                "adjuntos" to mensaje.adjuntos
            )
            
            // Crear documento
            val docRef = firestore.collection("mensajes").document()
            docRef.set(mensajeData).await()
            
            // Actualizar conversación
            actualizarConversacionConNuevoMensaje(
                conversacionId = mensaje.conversacionId,
                emisorId = mensaje.emisorId,
                receptorId = mensaje.receptorId,
                texto = mensaje.texto,
                timestamp = mensaje.timestamp
            )
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar mensaje", e)
            Result.Error(Exception(e.message ?: "Error desconocido al enviar mensaje"))
        }
    }
    
    /**
     * Actualiza la conversación con un nuevo mensaje
     */
    private suspend fun actualizarConversacionConNuevoMensaje(
        conversacionId: String,
        emisorId: String,
        receptorId: String,
        texto: String,
        timestamp: Long
    ) {
        try {
            val updates = mapOf(
                "ultimoMensaje" to texto,
                "fechaUltimoMensaje" to Timestamp(java.util.Date(timestamp)),
                "ultimoEmisorId" to emisorId,
                "${receptorId}_noLeidos" to FieldValue.increment(1)
            )
            
            firestore.collection("conversaciones")
                .document(conversacionId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar conversación con nuevo mensaje", e)
            throw e
        }
    }
    
    /**
     * Marca un mensaje como leído
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String) {
        try {
            // Actualizar en Room
            chatMensajeDao.marcarComoLeido(mensajeId, System.currentTimeMillis())
            
            // Actualizar en Firestore
            firestore.collection("mensajes")
                .document(mensajeId)
                .update(
                    mapOf(
                        "leido" to true,
                        "fechaLeido" to Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensaje como leído: $mensajeId")
            throw e
        }
    }
    
    /**
     * Cambia el estado destacado de un mensaje
     */
    suspend fun toggleMensajeDestacado(mensajeId: String, destacado: Boolean) {
        try {
            // Actualizar en Room
            val mensaje = chatMensajeDao.getMensajeById(mensajeId)
            mensaje?.let {
                chatMensajeDao.updateMensaje(it.copy(destacado = destacado))
            }
            
            // Actualizar en Firestore
            firestore.collection("mensajes")
                .document(mensajeId)
                .update("destacado", destacado)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al cambiar estado destacado del mensaje: $mensajeId")
            throw e
        }
    }
    
    /**
     * Elimina un mensaje
     */
    suspend fun eliminarMensaje(mensajeId: String) {
        try {
            // Eliminar en Room
            val mensaje = chatMensajeDao.getMensajeById(mensajeId)
            mensaje?.let {
                chatMensajeDao.deleteMensaje(it)
            }
            
            // Eliminar en Firestore
            firestore.collection("mensajes")
                .document(mensajeId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar mensaje: $mensajeId")
            throw e
        }
    }
    
    /**
     * Marca todos los mensajes de una conversación como leídos.
     */
    suspend fun marcarTodosComoLeidos(conversacionId: String, usuarioId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                
                // Actualizar en Room
                chatMensajeDao.marcarTodosComoLeidos(conversacionId, usuarioId, timestamp)
                
                // Resetear contadores
                val conversacion = conversacionDao.getConversacionById(conversacionId)
                if (conversacion != null) {
                    if (conversacion.participante1Id == usuarioId) {
                        conversacionDao.resetearNoLeidosP1(conversacionId, usuarioId)
                    } else if (conversacion.participante2Id == usuarioId) {
                        conversacionDao.resetearNoLeidosP2(conversacionId, usuarioId)
                    }
                }
                
                // Obtener IDs de mensajes no leídos para actualizar en Firestore
                val mensajesNoLeidos = firestore.collection("mensajes")
                    .whereEqualTo("conversacionId", conversacionId)
                    .whereEqualTo("receptorId", usuarioId)
                    .whereEqualTo("leido", false)
                    .get()
                    .await()
                
                val batch = firestore.batch()
                val timestampFirestore = Timestamp(java.util.Date(timestamp))
                
                mensajesNoLeidos.documents.forEach { doc ->
                    batch.update(doc.reference, 
                        mapOf(
                            "leido" to true,
                            "fechaLeido" to timestampFirestore
                        )
                    )
                }
                
                batch.commit().await()
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar todos los mensajes como leídos", e)
                Result.Error(Exception(e.message ?: "Error al marcar todos los mensajes como leídos"))
            }
        }
    }
    
    /**
     * Desactiva una conversación.
     */
    suspend fun desactivarConversacion(conversacionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Desactivar en Room
                conversacionDao.desactivarConversacion(conversacionId)
                
                // Desactivar en Firestore
                val updateMap = mapOf(
                    "activa" to false
                )
                
                firestore.collection("conversaciones")
                    .document(conversacionId)
                    .update(updateMap)
                    .await()
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al desactivar conversación", e)
                Result.Error(Exception(e.message ?: "Error al desactivar conversación"))
            }
        }
    }
    
    /**
     * Sincroniza los mensajes y conversaciones con Firestore.
     */
    suspend fun sincronizarConversaciones() {
        withContext(Dispatchers.IO) {
            try {
                val conversacionesNoSincronizadas = conversacionDao.getConversacionesNoSincronizadas()
                if (conversacionesNoSincronizadas.isNotEmpty()) {
                    val batch = firestore.batch()
                    conversacionesNoSincronizadas.forEach { conversacion ->
                        val conversacionRef = firestore.collection("conversaciones").document(conversacion.id)
                        val conversacionMap = mapOf(
                            "id" to conversacion.id,
                            "participante1Id" to conversacion.participante1Id,
                            "participante2Id" to conversacion.participante2Id,
                            "nombreParticipante1" to conversacion.nombreParticipante1,
                            "nombreParticipante2" to conversacion.nombreParticipante2,
                            "ultimoMensaje" to conversacion.ultimoMensaje,
                            "ultimoMensajeTimestamp" to conversacion.ultimoMensajeTimestamp,
                            "alumnoId" to conversacion.alumnoId,
                            "activa" to conversacion.activa
                        )
                        batch.set(conversacionRef, conversacionMap)
                        conversacionDao.marcarComoSincronizada(conversacion.id)
                    }
                    batch.commit().await()
                } else {
                    // No hay conversaciones para sincronizar
                    Timber.d("No hay conversaciones para sincronizar")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar conversaciones", e)
            }
        }
    }
    
    /**
     * Convierte una entidad ChatMensajeEntity a un modelo ChatMessage para la UI.
     */
    fun ChatMensajeEntity.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = this.id,
            senderId = this.emisorId,
            text = this.texto,
            timestamp = this.timestamp,
            isRead = this.leido,
            readTimestamp = this.fechaLeido,
            attachmentType = when (this.tipoAdjunto) {
                "IMAGE" -> AttachmentType.IMAGE
                "PDF" -> AttachmentType.PDF
                "AUDIO" -> AttachmentType.AUDIO
                "LOCATION" -> AttachmentType.LOCATION
                else -> null
            },
            attachmentUrl = this.urlAdjunto,
            interactionStatus = try {
                InteractionStatus.valueOf(this.interaccionEstado)
            } catch (e: Exception) {
                InteractionStatus.NONE
            },
            isTranslated = this.estaTraducido,
            originalText = this.textoOriginal
        )
    }
    
    /**
     * Convierte un modelo ChatMessage a una entidad ChatMensajeEntity.
     */
    fun ChatMessage.toEntity(conversacionId: String, receptorId: String): ChatMensajeEntity {
        return ChatMensajeEntity(
            id = this.id,
            emisorId = this.senderId,
            receptorId = receptorId,
            timestamp = this.timestamp,
            texto = this.text,
            leido = this.isRead,
            fechaLeido = this.readTimestamp,
            conversacionId = conversacionId,
            tipoAdjunto = this.attachmentType?.name,
            urlAdjunto = this.attachmentUrl,
            interaccionEstado = this.interactionStatus.name,
            estaTraducido = this.isTranslated,
            textoOriginal = this.originalText
        )
    }
    
    /**
     * Actualiza el último mensaje de una conversación.
     */
    suspend fun actualizarUltimoMensaje(
        conversacionId: String,
        texto: String, 
        timestamp: Long
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Actualizar en Room
                conversacionDao.actualizarUltimoMensaje(
                    conversacionId = conversacionId,
                    texto = texto,
                    timestamp = timestamp
                )
                
                // Actualizar en Firestore
                val updates = mapOf(
                    "ultimoMensaje" to texto,
                    "ultimoMensajeTimestamp" to timestamp
                )
                
                firestore.collection("conversaciones")
                    .document(conversacionId)
                    .update(updates)
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar último mensaje", e)
                throw e
            }
        }
    }
    
    /**
     * Obtiene las conversaciones de un usuario
     */
    suspend fun getConversacionesByUsuarioId(usuarioId: String): List<ConversacionInfo> {
        // Implementación temporal que devuelve una lista vacía
        return emptyList()
    }
    
    /**
     * Crea una nueva conversación o devuelve una existente
     */
    suspend fun obtenerOCrearConversacion(
        usuarioId: String,
        otroUsuarioId: String,
        alumnoId: String? = null
    ): Result<String> {
        return try {
            // Buscar conversación existente
            val claveConversacion = if (alumnoId != null) {
                // Conversación sobre un alumno
                listOf(usuarioId, otroUsuarioId).sorted().joinToString(":") + ":$alumnoId"
            } else {
                // Conversación directa
                listOf(usuarioId, otroUsuarioId).sorted().joinToString(":")
            }
            
            val query = firestore.collection("conversaciones")
                .whereEqualTo("claveConversacion", claveConversacion)
                .get()
                .await()
            
            // Si existe, devolver su ID
            if (!query.isEmpty) {
                val conversacionId = query.documents.first().id
                return Result.Success(conversacionId)
            }
            
            // Crear nueva conversación
            val conversacionData = mapOf(
                "participantes" to listOf(usuarioId, otroUsuarioId),
                "claveConversacion" to claveConversacion,
                "fechaCreacion" to Timestamp.now(),
                "ultimoMensaje" to "",
                "fechaUltimoMensaje" to Timestamp.now(),
                "ultimoEmisorId" to "",
                "${usuarioId}_noLeidos" to 0,
                "${otroUsuarioId}_noLeidos" to 0,
                "alumnoId" to alumnoId
            )
            
            val docRef = firestore.collection("conversaciones").document()
            docRef.set(conversacionData).await()
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener o crear conversación", e)
            Result.Error(e)
        }
    }
    
    /**
     * Sube un archivo adjunto al storage
     */
    suspend fun subirAdjunto(uri: Uri, conversacionId: String): Result<String> {
        return try {
            val extension = uri.lastPathSegment?.substringAfterLast(".") ?: "jpg"
            val fileName = "mensajes/${conversacionId}/${UUID.randomUUID()}.$extension"
            
            val fileRef = storage.reference.child(fileName)
            fileRef.putFile(uri).await()
            
            val downloadUrl = fileRef.downloadUrl.await()
            Result.Success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error al subir adjunto", e)
            Result.Error(e)
        }
    }
} 