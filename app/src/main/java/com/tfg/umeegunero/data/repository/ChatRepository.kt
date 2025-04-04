package com.tfg.umeegunero.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.local.dao.ChatMensajeDao
import com.tfg.umeegunero.data.local.dao.ConversacionDao
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.InteractionStatus
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.feature.profesor.screen.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los mensajes de chat y conversaciones.
 * Combina el acceso a la base de datos local con la sincronización remota en Firestore.
 */
@Singleton
class ChatRepository @Inject constructor(
    private val chatMensajeDao: ChatMensajeDao,
    private val conversacionDao: ConversacionDao,
    private val firestore: FirebaseFirestore
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
     * Obtiene los mensajes de una conversación como Flow.
     */
    fun getMensajesByConversacionId(conversacionId: String): Flow<List<ChatMensajeEntity>> {
        return chatMensajeDao.getMensajesByConversacionId(conversacionId)
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
    suspend fun enviarMensaje(
        conversacionId: String,
        emisorId: String,
        receptorId: String,
        texto: String,
        tipoAdjunto: String? = null,
        urlAdjunto: String? = null
    ): Result<ChatMensajeEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val mensajeId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                
                val mensaje = ChatMensajeEntity(
                    id = mensajeId,
                    emisorId = emisorId,
                    receptorId = receptorId,
                    timestamp = timestamp,
                    texto = texto,
                    leido = false,
                    conversacionId = conversacionId,
                    tipoAdjunto = tipoAdjunto,
                    urlAdjunto = urlAdjunto
                )
                
                // Guardar en Room
                chatMensajeDao.insertMensaje(mensaje)
                
                // Actualizar último mensaje en la conversación
                conversacionDao.actualizarUltimoMensaje(
                    conversacionId = conversacionId,
                    mensajeId = mensajeId,
                    mensajeTexto = texto,
                    mensajeTimestamp = timestamp,
                    emisorId = emisorId
                )
                
                // Actualizar contador de no leídos
                conversacionDao.actualizarContadorNoLeidos(conversacionId, receptorId)
                
                // Guardar en Firestore
                val mensajeMap = mapOf(
                    "id" to mensajeId,
                    "emisorId" to emisorId,
                    "receptorId" to receptorId,
                    "timestamp" to Timestamp(java.util.Date(timestamp)),
                    "texto" to texto,
                    "leido" to false,
                    "conversacionId" to conversacionId,
                    "tipoAdjunto" to tipoAdjunto,
                    "urlAdjunto" to urlAdjunto
                )
                
                firestore.collection("mensajes")
                    .document(mensajeId)
                    .set(mensajeMap)
                    .await()
                
                // Actualizar también el documento de la conversación
                val updateMap = mapOf(
                    "ultimoMensajeId" to mensajeId,
                    "ultimoMensajeTexto" to texto,
                    "ultimoMensajeTimestamp" to Timestamp(java.util.Date(timestamp)),
                    "ultimoMensajeEmisorId" to emisorId,
                    "updatedAt" to Timestamp.now()
                )
                
                firestore.collection("conversaciones")
                    .document(conversacionId)
                    .update(updateMap)
                    .await()
                
                Result.Success(mensaje)
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar mensaje", e)
                Result.Error(Exception(e.message ?: "Error desconocido al enviar mensaje"))
            }
        }
    }
    
    /**
     * Marca un mensaje como leído.
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                
                // Actualizar en Room
                chatMensajeDao.marcarMensajeComoLeido(mensajeId, timestamp)
                
                // Obtener mensaje para conocer la conversación
                val mensaje = chatMensajeDao.getMensajeById(mensajeId)
                mensaje?.let {
                    // Actualizar contadores
                    conversacionDao.actualizarContadorNoLeidos(it.conversacionId, it.receptorId)
                    
                    // Actualizar en Firestore
                    val updateMap = mapOf(
                        "leido" to true,
                        "fechaLeido" to Timestamp(java.util.Date(timestamp))
                    )
                    
                    firestore.collection("mensajes")
                        .document(mensajeId)
                        .update(updateMap)
                        .await()
                }
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar mensaje como leído", e)
                Result.Error(Exception(e.message ?: "Error al marcar mensaje como leído"))
            }
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
                
                // Actualizar contadores
                conversacionDao.actualizarContadorNoLeidos(conversacionId, usuarioId)
                
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
     * Actualiza el estado de interacción de un mensaje.
     */
    suspend fun actualizarEstadoInteraccion(mensajeId: String, estado: InteractionStatus): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                chatMensajeDao.actualizarEstadoInteraccion(mensajeId, estado.name)
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar estado de interacción", e)
                Result.Error(Exception(e.message ?: "Error al actualizar estado de interacción"))
            }
        }
    }
    
    /**
     * Elimina una conversación y todos sus mensajes.
     */
    suspend fun eliminarConversacion(conversacionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Eliminar en Room
                chatMensajeDao.eliminarMensajesDeConversacion(conversacionId)
                conversacionDao.eliminarConversacion(conversacionId)
                
                // Eliminar en Firestore
                val mensajesRef = firestore.collection("mensajes")
                    .whereEqualTo("conversacionId", conversacionId)
                    .get()
                    .await()
                
                val batch = firestore.batch()
                
                mensajesRef.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                
                batch.delete(firestore.collection("conversaciones").document(conversacionId))
                batch.commit().await()
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar conversación", e)
                Result.Error(Exception(e.message ?: "Error al eliminar conversación"))
            }
        }
    }
    
    /**
     * Sincroniza los mensajes y conversaciones con Firestore.
     */
    suspend fun sincronizarConversaciones() {
        withContext(Dispatchers.IO) {
            try {
                // Sincronizar mensajes no sincronizados a Firestore
                val mensajesNoSincronizados = chatMensajeDao.getMensajesNoSincronizados()
                
                if (mensajesNoSincronizados.isNotEmpty()) {
                    val batch = firestore.batch()
                    val mensajesIds = mutableListOf<String>()
                    
                    mensajesNoSincronizados.forEach { mensaje ->
                        mensajesIds.add(mensaje.id)
                        
                        val mensajeRef = firestore.collection("mensajes").document(mensaje.id)
                        
                        val mensajeMap = mapOf(
                            "id" to mensaje.id,
                            "emisorId" to mensaje.emisorId,
                            "receptorId" to mensaje.receptorId,
                            "timestamp" to Timestamp(java.util.Date(mensaje.timestamp)),
                            "texto" to mensaje.texto,
                            "leido" to mensaje.leido,
                            "fechaLeido" to mensaje.fechaLeido?.let { Timestamp(java.util.Date(it)) },
                            "conversacionId" to mensaje.conversacionId,
                            "tipoAdjunto" to mensaje.tipoAdjunto,
                            "urlAdjunto" to mensaje.urlAdjunto
                        )
                        
                        batch.set(mensajeRef, mensajeMap)
                    }
                    
                    batch.commit().await()
                    chatMensajeDao.marcarComoSincronizados(mensajesIds)
                }
                
                // Sincronizar conversaciones no sincronizadas a Firestore
                val conversacionesNoSincronizadas = conversacionDao.getConversacionesNoSincronizadas()
                
                if (conversacionesNoSincronizadas.isNotEmpty()) {
                    val batch = firestore.batch()
                    val conversacionesIds = mutableListOf<String>()
                    
                    conversacionesNoSincronizadas.forEach { conversacion ->
                        conversacionesIds.add(conversacion.id)
                        
                        val conversacionRef = firestore.collection("conversaciones").document(conversacion.id)
                        
                        val conversacionMap = mapOf(
                            "id" to conversacion.id,
                            "participante1Id" to conversacion.participante1Id,
                            "participante2Id" to conversacion.participante2Id,
                            "nombreParticipante1" to conversacion.nombreParticipante1,
                            "nombreParticipante2" to conversacion.nombreParticipante2,
                            "ultimoMensajeId" to conversacion.ultimoMensajeId,
                            "ultimoMensajeTexto" to conversacion.ultimoMensajeTexto,
                            "ultimoMensajeTimestamp" to conversacion.ultimoMensajeTimestamp?.let { Timestamp(java.util.Date(it)) },
                            "ultimoMensajeEmisorId" to conversacion.ultimoMensajeEmisorId,
                            "alumnoId" to conversacion.alumnoId,
                            "createdAt" to Timestamp(java.util.Date(conversacion.createdAt)),
                            "updatedAt" to Timestamp(java.util.Date(conversacion.updatedAt))
                        )
                        
                        batch.set(conversacionRef, conversacionMap)
                    }
                    
                    batch.commit().await()
                    conversacionDao.marcarComoSincronizadas(conversacionesIds)
                }
                
                // Traer nuevos mensajes de Firestore
                // Comentando esta línea que causa error con .flow
                // val ultimoMensajeTimestamp = chatMensajeDao.getMensajesPaginados("").flow
                
                firestore.collection("mensajes")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50) // Limitar la cantidad de mensajes por vez
                    .get()
                    .await()
                    .documents.forEach { doc ->
                        val mensaje = doc.toObject(Mensaje::class.java)
                        mensaje?.let {
                            val entity = ChatMensajeEntity(
                                id = it.id,
                                emisorId = it.emisorId,
                                receptorId = it.receptorId,
                                timestamp = it.timestamp.toDate().time,
                                texto = it.texto,
                                leido = it.leido,
                                fechaLeido = it.fechaLeido?.toDate()?.time,
                                alumnoId = it.alumnoId,
                                conversacionId = it.conversacionId,
                                tipoAdjunto = it.adjuntos?.firstOrNull(),
                                sincronizado = true
                            )
                            chatMensajeDao.insertMensaje(entity)
                        }
                    }
                
                // Actualizar contadores de no leídos
                val conversaciones = conversacionDao.getConversacionesNoSincronizadas()
                conversaciones.forEach { conversacion ->
                    val updateMap = mutableMapOf<String, Any>()
                    
                    // Contar mensajes no leídos para participante1
                    val noLeidosP1 = firestore.collection("mensajes")
                        .whereEqualTo("conversacionId", conversacion.id)
                        .whereEqualTo("receptorId", conversacion.participante1Id)
                        .whereEqualTo("leido", false)
                        .get()
                        .await()
                        .documents.size
                    
                    // Contar mensajes no leídos para participante2
                    val noLeidosP2 = firestore.collection("mensajes")
                        .whereEqualTo("conversacionId", conversacion.id)
                        .whereEqualTo("receptorId", conversacion.participante2Id)
                        .whereEqualTo("leido", false)
                        .get()
                        .await()
                        .documents.size
                    
                    // Actualizar en Room
                    if (noLeidosP1 > 0) {
                        conversacionDao.actualizarContadorNoLeidos(conversacion.id, conversacion.participante1Id)
                    }
                    
                    if (noLeidosP2 > 0) {
                        conversacionDao.actualizarContadorNoLeidos(conversacion.id, conversacion.participante2Id)
                    }
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
        val attachmentType = when (this.tipoAdjunto) {
            "IMAGE" -> AttachmentType.IMAGE
            "PDF" -> AttachmentType.PDF
            "AUDIO" -> AttachmentType.AUDIO
            "LOCATION" -> AttachmentType.LOCATION
            else -> null
        }
        
        val interactionStatus = try {
            InteractionStatus.valueOf(this.interaccionEstado)
        } catch (e: Exception) {
            InteractionStatus.NONE
        }
        
        return ChatMessage(
            id = this.id,
            senderId = this.emisorId,
            text = this.texto,
            timestamp = this.timestamp,
            isRead = this.leido,
            readTimestamp = this.fechaLeido,
            attachmentType = attachmentType,
            attachmentUrl = this.urlAdjunto,
            interactionStatus = interactionStatus,
            isTranslated = this.estaTraducido,
            originalText = this.textoOriginal
        )
    }
    
    /**
     * Convierte un modelo ChatMessage a una entidad ChatMensajeEntity.
     */
    fun ChatMessage.toEntity(conversacionId: String): ChatMensajeEntity {
        return ChatMensajeEntity(
            id = this.id,
            emisorId = this.senderId,
            receptorId = "", // Necesita ser completado
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
} 