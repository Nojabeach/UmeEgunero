package com.tfg.umeegunero.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.Mensaje
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modelo de datos para representar una conversación con información resumida
 */
data class ConversacionInfo(
    val conversacionId: String,
    val participanteId: String,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: Timestamp?,
    val mensajesNoLeidos: Int,
    val alumnoId: String? = null
)

/**
 * Repositorio para gestionar mensajes y conversaciones
 */
@Singleton
class MensajeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    // Referencia al Storage para adjuntos
    private val storage = FirebaseStorage.getInstance()
    
    // Referencia a colecciones
    private val conversacionesRef = firestore.collection("conversaciones")
    private val mensajesRef = firestore.collection("mensajes")
    
    // Cache de mensajes por conversación
    private val mensajesPorConversacion = mutableMapOf<String, MutableStateFlow<List<Mensaje>>>()
    
    // Cache de conversaciones por usuario
    private val conversacionesPorUsuario = mutableMapOf<String, MutableStateFlow<List<ConversacionInfo>>>()
    
    /**
     * Obtiene los mensajes de una conversación como flujo
     */
    fun obtenerMensajes(conversacionId: String): Flow<List<Mensaje>> {
        // Utilizar cache si existe
        if (!mensajesPorConversacion.containsKey(conversacionId)) {
            mensajesPorConversacion[conversacionId] = MutableStateFlow(emptyList())
            
            // Escuchar cambios en tiempo real
            mensajesRef
                .whereEqualTo("conversacionId", conversacionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error al obtener mensajes")
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val mensajes = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Mensaje::class.java)?.copy(id = doc.id)
                        }
                        
                        mensajesPorConversacion[conversacionId]?.value = mensajes
                    }
                }
        }
        
        return mensajesPorConversacion[conversacionId]!!.asStateFlow()
    }
    
    /**
     * Obtiene las conversaciones de un usuario como flujo
     */
    fun obtenerConversaciones(usuarioId: String): Flow<List<ConversacionInfo>> {
        // Utilizar cache si existe
        if (!conversacionesPorUsuario.containsKey(usuarioId)) {
            conversacionesPorUsuario[usuarioId] = MutableStateFlow(emptyList())
            
            // Escuchar cambios en tiempo real
            conversacionesRef
                .whereArrayContains("participantes", usuarioId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error al obtener conversaciones")
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val conversaciones = snapshot.documents.mapNotNull { doc ->
                            try {
                                val participantes = doc.get("participantes") as? List<String> ?: emptyList()
                                val ultimoMensaje = doc.getString("ultimoMensaje") ?: ""
                                val fechaUltimoMensaje = doc.getTimestamp("fechaUltimoMensaje")
                                val alumnoId = doc.getString("alumnoId")
                                
                                // Encontrar el otro participante
                                val participanteId = participantes.firstOrNull { it != usuarioId }
                                    ?: return@mapNotNull null
                                
                                // Contar mensajes no leídos
                                val mensajesNoLeidos = doc.getLong("${usuarioId}_noLeidos")?.toInt() ?: 0
                                
                                ConversacionInfo(
                                    conversacionId = doc.id,
                                    participanteId = participanteId,
                                    ultimoMensaje = ultimoMensaje,
                                    fechaUltimoMensaje = fechaUltimoMensaje,
                                    mensajesNoLeidos = mensajesNoLeidos,
                                    alumnoId = alumnoId
                                )
                            } catch (e: Exception) {
                                Timber.e(e, "Error al procesar conversación ${doc.id}")
                                null
                            }
                        }
                        
                        conversacionesPorUsuario[usuarioId]?.value = conversaciones
                    }
                }
        }
        
        return conversacionesPorUsuario[usuarioId]!!.asStateFlow()
    }
    
    /**
     * Envía un mensaje en una conversación
     * @return ID del mensaje creado
     */
    suspend fun enviarMensaje(conversacionId: String, mensaje: Mensaje): String {
        try {
            // Si el mensaje ya tiene ID, lo actualizamos
            if (mensaje.id.isNotEmpty()) {
                mensajesRef.document(mensaje.id).set(mensaje).await()
                return mensaje.id
            }
            
            // Crear nuevo mensaje
            val nuevoMensajeRef = mensajesRef.document()
            val nuevoMensaje = mensaje.copy(
                id = nuevoMensajeRef.id,
                conversacionId = conversacionId
            )
            
            // Guardar mensaje
            nuevoMensajeRef.set(nuevoMensaje).await()
            
            // Actualizar conversación
            actualizarConversacionConNuevoMensaje(conversacionId, nuevoMensaje)
            
            return nuevoMensajeRef.id
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar mensaje")
            throw e
        }
    }
    
    /**
     * Actualiza la conversación con un nuevo mensaje
     */
    private suspend fun actualizarConversacionConNuevoMensaje(conversacionId: String, mensaje: Mensaje) {
        try {
            val conversacionRef = conversacionesRef.document(conversacionId)
            val conversacionDoc = conversacionRef.get().await()
            
            if (!conversacionDoc.exists()) {
                throw Exception("La conversación no existe")
            }
            
            val participantes = conversacionDoc.get("participantes") as? List<String> ?: emptyList()
            
            // Incrementar contador de mensajes no leídos para el receptor
            val updates = mutableMapOf<String, Any>(
                "ultimoMensaje" to mensaje.texto,
                "fechaUltimoMensaje" to mensaje.timestamp,
                "ultimoEmisorId" to mensaje.emisorId
            )
            
            // Incrementar contador para cada participante que no sea el emisor
            participantes.forEach { participanteId ->
                if (participanteId != mensaje.emisorId) {
                    updates["${participanteId}_noLeidos"] = FieldValue.increment(1)
                }
            }
            
            conversacionRef.update(updates).await()
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar conversación")
            throw e
        }
    }
    
    /**
     * Marca un mensaje como leído
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String) {
        try {
            val mensajeRef = mensajesRef.document(mensajeId)
            val mensajeDoc = mensajeRef.get().await()
            
            if (!mensajeDoc.exists()) {
                throw Exception("El mensaje no existe")
            }
            
            mensajeRef.update(
                mapOf(
                    "leido" to true,
                    "fechaLeido" to Timestamp.now()
                )
            ).await()
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensaje como leído")
            throw e
        }
    }
    
    /**
     * Marca todos los mensajes de una conversación como leídos
     */
    suspend fun marcarConversacionComoLeida(conversacionId: String) {
        try {
            val firebaseUser = authRepository.getFirebaseUser()
            val usuarioId = firebaseUser?.uid
                ?: throw Exception("Usuario no autenticado")
                
            // Actualizar conversación
            val conversacionRef = conversacionesRef.document(conversacionId)
            conversacionRef.update("${usuarioId}_noLeidos", 0).await()
            
            // Marcar mensajes como leídos
            val mensajesNoLeidos = mensajesRef
                .whereEqualTo("conversacionId", conversacionId)
                .whereEqualTo("receptorId", usuarioId)
                .whereEqualTo("leido", false)
                .get()
                .await()
            
            // Actualizar cada mensaje no leído
            val batch = firestore.batch()
            mensajesNoLeidos.documents.forEach { doc ->
                batch.update(
                    doc.reference,
                    mapOf(
                        "leido" to true,
                        "fechaLeido" to Timestamp.now()
                    )
                )
            }
            
            batch.commit().await()
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar conversación como leída")
            throw e
        }
    }
    
    /**
     * Crea una nueva conversación entre dos usuarios
     * @return ID de la conversación creada
     */
    suspend fun crearConversacion(
        usuarioId: String,
        participanteId: String,
        alumnoId: String? = null
    ): String {
        try {
            // Verificar si ya existe una conversación
            val claveConversacion = if (alumnoId != null) {
                // Para conversaciones relacionadas con un alumno específico
                listOf(usuarioId, participanteId).sorted().joinToString(":") + ":$alumnoId"
            } else {
                // Para conversaciones directas entre usuario y participante
                listOf(usuarioId, participanteId).sorted().joinToString(":")
            }
            
            // Buscar conversación existente
            val conversacionExistente = conversacionesRef
                .whereEqualTo("claveConversacion", claveConversacion)
                .get()
                .await()
            
            // Si existe, devolver su ID
            if (!conversacionExistente.isEmpty) {
                return conversacionExistente.documents.first().id
            }
            
            // Crear nueva conversación
            val nuevaConversacionRef = conversacionesRef.document()
            val conversacion = hashMapOf(
                "participantes" to listOf(usuarioId, participanteId),
                "claveConversacion" to claveConversacion,
                "fechaCreacion" to Timestamp.now(),
                "ultimoMensaje" to "",
                "fechaUltimoMensaje" to Timestamp.now(),
                "ultimoEmisorId" to "",
                "${usuarioId}_noLeidos" to 0,
                "${participanteId}_noLeidos" to 0
            )
            
            // Añadir alumnoId si existe
            if (alumnoId != null) {
                conversacion["alumnoId"] = alumnoId
            }
            
            nuevaConversacionRef.set(conversacion).await()
            
            return nuevaConversacionRef.id
        } catch (e: Exception) {
            Timber.e(e, "Error al crear conversación")
            throw e
        }
    }
    
    /**
     * Elimina una conversación y todos sus mensajes
     */
    suspend fun eliminarConversacion(conversacionId: String) {
        try {
            // Primero obtener todos los mensajes para eliminar adjuntos
            val mensajes = mensajesRef
                .whereEqualTo("conversacionId", conversacionId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Mensaje::class.java) }
            
            // Eliminar adjuntos de mensajes
            mensajes.forEach { mensaje ->
                mensaje.adjuntos?.forEach { url ->
                    try {
                        val fileRef = storage.getReferenceFromUrl(url)
                        fileRef.delete().await()
                    } catch (e: Exception) {
                        Timber.e(e, "Error al eliminar adjunto: $url")
                    }
                }
            }
            
            // Eliminar mensajes en lotes
            val mensajesDocs = mensajesRef
                .whereEqualTo("conversacionId", conversacionId)
                .get()
                .await()
            
            val batch = firestore.batch()
            mensajesDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Eliminar la conversación
            batch.delete(conversacionesRef.document(conversacionId))
            
            batch.commit().await()
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar conversación")
            throw e
        }
    }
    
    /**
     * Sube adjuntos al Storage y devuelve las URLs
     */
    suspend fun subirAdjuntos(mensajeId: String, uris: List<Uri>): List<String> {
        val urls = mutableListOf<String>()
        
        try {
            for (uri in uris) {
                val extension = getFileExtension(uri.toString())
                val fileName = "adjuntos/${mensajeId}/${UUID.randomUUID()}.$extension"
                val fileRef = storage.reference.child(fileName)
                
                // Subir archivo
                fileRef.putFile(uri).await()
                
                // Obtener URL
                val downloadUrl = fileRef.downloadUrl.await()
                urls.add(downloadUrl.toString())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al subir adjuntos")
            throw e
        }
        
        return urls
    }
    
    /**
     * Actualiza la lista de adjuntos de un mensaje
     */
    suspend fun actualizarAdjuntosMensaje(mensajeId: String, adjuntos: List<String>) {
        try {
            mensajesRef.document(mensajeId)
                .update("adjuntos", adjuntos)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar adjuntos")
            throw e
        }
    }
    
    /**
     * Obtiene la extensión de un archivo desde su URI
     */
    private fun getFileExtension(uri: String): String {
        val fileName = uri.substringAfterLast("/")
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            "bin" // Si no hay extensión, usar "bin" por defecto
        }
    }
} 