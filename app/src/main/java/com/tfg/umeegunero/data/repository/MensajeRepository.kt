package com.tfg.umeegunero.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.Mensaje
import com.tfg.umeegunero.data.model.TipoDestinatario
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para la gestión de mensajes.
 * Esta clase será reemplazada gradualmente por ChatRepository.
 * @deprecated Use ChatRepository instead
 */
@Deprecated("Use ChatRepository instead")
@Singleton
class MensajeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val storage: FirebaseStorage
) {
    private val TAG = "MensajeRepository"
    
    companion object {
        private const val COLLECTION_MENSAJES = "mensajes"
    }
    
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
    fun obtenerMensajes(conversacionId: String): Flow<List<Mensaje>> = flow {
        try {
            val mensajes = mutableListOf<Mensaje>()
            
            // Obtener mensajes de Firestore
            val querySnapshot = firestore.collection("mensajes")
                .whereEqualTo("conversacionId", conversacionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            
            // Convertir documentos a objetos Mensaje
            for (document in querySnapshot.documents) {
                val mensaje = toMensaje(document)
                if (mensaje != null) {
                    mensajes.add(mensaje)
                }
            }
            
            emit(mensajes)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener mensajes", e)
            emit(emptyList())
        }
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
                                    fechaUltimoMensaje = fechaUltimoMensaje?.toDate()?.time ?: 0L,
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
     * Obtiene mensajes recibidos por un usuario
     * @param usuarioId ID del usuario
     * @return Flow con la lista de mensajes
     */
    fun getMensajesRecibidos(usuarioId: String): Flow<List<Mensaje>> = callbackFlow {
        val listenerRegistration = firestore.collection(COLLECTION_MENSAJES)
            .whereEqualTo("destinatarioId", usuarioId)
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error al obtener mensajes recibidos")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    Mensaje.fromSnapshot(doc)
                } ?: emptyList()
                
                trySend(mensajes)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Obtiene mensajes enviados por un usuario
     * @param usuarioId ID del usuario
     * @return Flow con la lista de mensajes
     */
    fun getMensajesEnviados(usuarioId: String): Flow<List<Mensaje>> = callbackFlow {
        val listenerRegistration = firestore.collection(COLLECTION_MENSAJES)
            .whereEqualTo("remitente", usuarioId)
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error al obtener mensajes enviados")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    Mensaje.fromSnapshot(doc)
                } ?: emptyList()
                
                trySend(mensajes)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Obtiene mensajes destacados de un usuario
     * @param usuarioId ID del usuario
     * @return Flow con la lista de mensajes
     */
    fun getMensajesDestacados(usuarioId: String): Flow<List<Mensaje>> = callbackFlow {
        val listenerRegistration = firestore.collection(COLLECTION_MENSAJES)
            .whereEqualTo("destinatarioId", usuarioId)
            .whereEqualTo("destacado", true)
            .orderBy("fechaEnvio", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error al obtener mensajes destacados")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    Mensaje.fromSnapshot(doc)
                } ?: emptyList()
                
                trySend(mensajes)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Obtiene un mensaje específico
     * @param mensajeId ID del mensaje
     * @return Mensaje encontrado o null
     */
    suspend fun getMensaje(mensajeId: String): Mensaje? {
        return try {
            val doc = firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .get()
                .await()
            
            Mensaje.fromSnapshot(doc)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensaje")
            null
        }
    }
    
    /**
     * Envía un nuevo mensaje
     * @param mensaje Mensaje a enviar
     * @return ID del mensaje creado
     */
    suspend fun enviarMensaje(mensaje: Mensaje): String {
        return try {
            val docRef = firestore.collection(COLLECTION_MENSAJES)
                .add(mensaje.toMap())
                .await()
            
            docRef.id
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar mensaje")
            throw e
        }
    }
    
    /**
     * Marca un mensaje como leído
     * @param mensajeId ID del mensaje
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String) {
        try {
            val updateData = mapOf(
                "leido" to true,
                "fechaLeido" to Timestamp.now()
            )
            
            firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .update(updateData)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensaje como leído")
            throw e
        }
    }
    
    /**
     * Destaca/desmarca un mensaje
     * @param mensajeId ID del mensaje
     * @param destacado Indica si se debe destacar o desmarcar
     */
    suspend fun toggleMensajeDestacado(mensajeId: String, destacado: Boolean) {
        try {
            firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .update("destacado", destacado)
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar estado destacado del mensaje")
            throw e
        }
    }
    
    /**
     * Elimina un mensaje
     * @param mensajeId ID del mensaje
     */
    suspend fun eliminarMensaje(mensajeId: String) {
        try {
            firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar mensaje")
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
    
    /**
     * Convierte un DocumentSnapshot a un objeto Mensaje
     */
    private fun toMensaje(doc: DocumentSnapshot): Mensaje? {
        return try {
            val data = doc.data ?: return null
            
            val adjuntosData = data["adjuntos"]
            val adjuntosList = when (adjuntosData) {
                is List<*> -> adjuntosData.filterIsInstance<String>()
                else -> null
            }
            
            Mensaje(
                id = doc.id,
                emisorId = data["emisorId"] as? String ?: "",
                receptorId = data["receptorId"] as? String ?: "",
                timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                texto = data["texto"] as? String ?: "",
                leido = data["leido"] as? Boolean ?: false,
                fechaLeido = data["fechaLeido"] as? Timestamp,
                conversacionId = data["conversacionId"] as? String ?: "",
                alumnoId = data["alumnoId"] as? String,
                adjuntos = adjuntosList,
                tipoMensaje = data["tipoMensaje"] as? String ?: "TEXTO",
                remitente = data["remitente"] as? String ?: "",
                remitenteNombre = data["remitenteNombre"] as? String ?: "",
                destinatarioId = data["destinatarioId"] as? String ?: "",
                destinatarioNombre = data["destinatarioNombre"] as? String ?: "",
                asunto = data["asunto"] as? String ?: "",
                contenido = data["contenido"] as? String ?: "",
                fechaEnvio = data["fechaEnvio"] as? Timestamp ?: Timestamp.now(),
                destacado = data["destacado"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir documento a Mensaje", e)
            null
        }
    }

    /**
     * Obtiene todos los mensajes enviados o recibidos por un usuario (para la bandeja de entrada)
     * 
     * @param usuarioId ID del usuario
     * @return Lista de mensajes
     */
    suspend fun getMensajesForUsuario(usuarioId: String): List<Mensaje> {
        return try {
            val mensajesRecibidos = firestore.collection(COLLECTION_MENSAJES)
                .whereEqualTo("destinatarioId", usuarioId)
                .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { Mensaje.fromSnapshot(it) }
            
            val mensajesEnviados = firestore.collection(COLLECTION_MENSAJES)
                .whereEqualTo("remitente", usuarioId)
                .orderBy("fechaEnvio", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .mapNotNull { Mensaje.fromSnapshot(it) }
            
            // Combinar y ordenar por fecha descendente
            (mensajesRecibidos + mensajesEnviados).sortedByDescending { it.fechaEnvio }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensajes del usuario")
            emptyList()
        }
    }

    /**
     * Marca un mensaje como leído y devuelve el mensaje actualizado
     * 
     * @param mensajeId ID del mensaje
     * @param usuarioDni DNI del usuario 
     * @return Mensaje actualizado o null si ocurre un error
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String, usuarioDni: String): Mensaje? {
        return try {
            val updateData = mapOf(
                "leido" to true,
                "fechaLeido" to Timestamp.now(),
                "leidos" to FieldValue.arrayUnion(usuarioDni)
            )
            
            firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .update(updateData)
                .await()
            
            // Obtener el mensaje actualizado
            getMensaje(mensajeId)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar mensaje como leído: $mensajeId")
            null
        }
    }

    /**
     * Cambia el estado destacado de un mensaje y devuelve el mensaje actualizado
     * 
     * @param mensajeId ID del mensaje
     * @param usuarioDni DNI del usuario
     * @param destacado Nuevo estado destacado
     * @return Mensaje actualizado o null si ocurre un error
     */
    suspend fun toggleMensajeDestacado(mensajeId: String, usuarioDni: String, destacado: Boolean): Mensaje? {
        return try {
            firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .update("destacado", destacado)
                .await()
            
            // Obtener el mensaje actualizado
            getMensaje(mensajeId)
        } catch (e: Exception) {
            Timber.e(e, "Error al cambiar estado destacado del mensaje: $mensajeId")
            null
        }
    }

    /**
     * Elimina un mensaje para un usuario
     * 
     * @param mensajeId ID del mensaje
     * @param usuarioDni DNI del usuario
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    suspend fun eliminarMensaje(mensajeId: String, usuarioDni: String): Boolean {
        return try {
            // En una implementación real, podríamos marcar el mensaje como eliminado
            // para ese usuario específico, pero aquí simplemente lo eliminamos
            firestore.collection(COLLECTION_MENSAJES)
                .document(mensajeId)
                .delete()
                .await()
            
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar mensaje: $mensajeId")
            false
        }
    }
} 