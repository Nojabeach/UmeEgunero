package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.EstadisticasComunicado
import com.tfg.umeegunero.data.model.MessageStatus
import com.tfg.umeegunero.data.model.MessageType
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.UnifiedMessage
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modelo para estadísticas de lectura de un comunicado
 */
data class EstadisticasComunicado(
    val totalDestinatarios: Int = 0,
    val totalLeidos: Int = 0,
    val totalConfirmados: Int = 0,
    val porcentajeLeido: Float = 0f,
    val porcentajeConfirmado: Float = 0f,
    val usuariosLeidos: List<String> = emptyList(),
    val usuariosConfirmados: List<String> = emptyList(),
    val usuariosPendientes: List<String> = emptyList()
)

/**
 * Repositorio para gestionar comunicados en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y eliminar
 * comunicados oficiales dentro del contexto educativo, permitiendo a diferentes
 * tipos de usuarios (profesores, administradores) informar y comunicar
 * información importante a familias y alumnos.
 *
 * Características principales:
 * - Creación de comunicados oficiales
 * - Gestión de destinatarios (centro, curso, clase, usuarios específicos)
 * - Control de lectura y confirmación de comunicados
 * - Soporte para diferentes tipos de comunicados
 * - Registro de interacciones con comunicados
 *
 * El repositorio permite:
 * - Enviar comunicados generales o específicos
 * - Rastrear la lectura de comunicados
 * - Gestionar la visibilidad de comunicados
 * - Notificar sobre nuevos comunicados
 * - Mantener un historial de comunicaciones
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property notificacionRepository Repositorio para enviar notificaciones relacionadas
 * @property unifiedMessageRepository Repositorio para enviar mensajes unificados
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class ComunicadoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificacionRepository: NotificacionRepository,
    private val unifiedMessageRepository: UnifiedMessageRepository?
) {
    
    private val comunicadosCollection = firestore.collection("comunicados")
    private val COMUNICADOS_COLLECTION = "comunicados"
    
    /**
     * Obtiene la lista de todos los comunicados
     */
    suspend fun getComunicados(): Result<List<Comunicado>> = try {
        val snapshot = comunicadosCollection.get().await()
        val comunicados = snapshot.toObjects(Comunicado::class.java)
        Result.Success(comunicados)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener comunicados")
        Result.Error(e)
    }
    
    /**
     * Obtiene la lista de comunicados filtrados por tipo de usuario
     */
    suspend fun getComunicadosByTipoUsuario(
        tipoUsuario: com.tfg.umeegunero.data.model.TipoUsuario
    ): Result<List<Comunicado>> = try {
        val snapshot = comunicadosCollection
            .whereArrayContains("tiposDestinatarios", tipoUsuario.toString())
            .get()
            .await()
        val comunicados = snapshot.toObjects(Comunicado::class.java)
        Result.Success(comunicados)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener comunicados por tipo de usuario")
        Result.Error(e)
    }
    
    /**
     * Obtiene un comunicado por su ID
     */
    suspend fun getComunicadoById(comunicadoId: String): Result<Comunicado> = try {
        val documentSnapshot = comunicadosCollection.document(comunicadoId).get().await()
        if (documentSnapshot.exists()) {
            val comunicado = documentSnapshot.toObject(Comunicado::class.java)
                ?: throw Exception("Error al convertir comunicado")
            Result.Success(comunicado)
        } else {
            Result.Error(Exception("No se encontró el comunicado"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener comunicado por ID")
        Result.Error(e)
    }
    
    /**
     * Crea un nuevo comunicado y envía notificaciones a los destinatarios
     * 
     * @param comunicado Objeto Comunicado a crear
     * @return Resultado que indica éxito o error
     */
    suspend fun crearComunicado(comunicado: Comunicado): Result<Unit> = try {
        // Primero guardamos el comunicado en Firestore
        comunicadosCollection.document(comunicado.id).set(comunicado).await()
        
        // Después creamos notificaciones para cada tipo de destinatario
        enviarNotificacionesComunicado(comunicado)
        
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al crear comunicado")
        Result.Error(e)
    }
    
    /**
     * Envía notificaciones a los destinatarios del comunicado
     * 
     * @param comunicado El comunicado del que se enviarán notificaciones
     */
    private suspend fun enviarNotificacionesComunicado(comunicado: Comunicado) {
        try {
            // Por cada tipo de destinatario, creamos una notificación
            comunicado.tiposDestinatarios.forEach { tipoDestinatario ->
                // En un sistema real, aquí obtendríamos la lista de usuarios de este tipo
                // y crearíamos una notificación para cada uno.
                // Por ahora, creamos una notificación genérica para el tipo de usuario
                
                val notificacionResult = notificacionRepository.crearNotificacionMensaje(
                    titulo = "Nuevo comunicado: ${comunicado.titulo}",
                    mensaje = "Remitente: ${comunicado.remitente}",
                    tipo = "comunicado",
                    destinatarioId = "", // En un sistema real, aquí iría el ID de cada destinatario
                    destinatarioTipo = tipoDestinatario,
                    origenId = comunicado.id,
                    // Acción a realizar cuando se pulsa la notificación (navegar al detalle del comunicado)
                    accion = "detalle_comunicado/${comunicado.id}"
                )
                
                if (notificacionResult is Result.Error) {
                    Timber.e(notificacionResult.exception, 
                        "Error al crear notificación para comunicado ${comunicado.id}")
                }
            }
            
            Timber.d("Notificaciones enviadas para el comunicado ${comunicado.id}")
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar notificaciones del comunicado")
        }
    }
    
    /**
     * Actualiza un comunicado existente
     */
    suspend fun actualizarComunicado(comunicado: Comunicado): Result<Unit> = try {
        comunicadosCollection.document(comunicado.id).set(comunicado).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al actualizar comunicado")
        Result.Error(e)
    }
    
    /**
     * Elimina un comunicado
     */
    suspend fun eliminarComunicado(comunicadoId: String): Result<Unit> = try {
        comunicadosCollection.document(comunicadoId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al eliminar comunicado")
        Result.Error(e)
    }
    
    /**
     * Marca un comunicado como inactivo (archivar)
     */
    suspend fun archivarComunicado(comunicadoId: String): Result<Unit> = try {
        comunicadosCollection.document(comunicadoId)
            .update("activo", false)
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al archivar comunicado")
        Result.Error(e)
    }
    
    /**
     * Marca un comunicado como leído por un usuario.
     * 
     * @param comunicadoId Identificador del comunicado
     * @param usuarioId Identificador del usuario
     * @return Resultado que indica éxito o error
     */
    suspend fun marcarComoLeido(comunicadoId: String, usuarioId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val comunicadoRef = firestore.collection("comunicados")
                    .document(comunicadoId)
                
                // Actualiza la lista de usuarios que han leído (añade sin duplicar)
                comunicadoRef.update(
                    "usuariosLeidos", FieldValue.arrayUnion(usuarioId)
                ).await()
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error al marcar comunicado como leído")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Confirma la lectura de un comunicado por un usuario.
     * 
     * @param comunicadoId Identificador del comunicado
     * @param usuarioId Identificador del usuario
     * @return Resultado que indica éxito o error
     */
    suspend fun confirmarLectura(comunicadoId: String, usuarioId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val comunicadoRef = firestore.collection("comunicados")
                    .document(comunicadoId)
                
                // Primero marcamos como leído (por si no lo estaba ya)
                when (val result = marcarComoLeido(comunicadoId, usuarioId)) {
                    is Result.Error -> return@withContext result
                    else -> {}
                }
                
                // Actualiza la lista de usuarios que han confirmado (añade sin duplicar)
                comunicadoRef.update(
                    "usuariosConfirmados", FieldValue.arrayUnion(usuarioId)
                ).await()
                
                Result.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error al confirmar lectura de comunicado")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Verifica si un usuario ha leído un comunicado.
     * 
     * @param comunicadoId Identificador del comunicado
     * @param usuarioId Identificador del usuario
     * @return Resultado que contiene true si el usuario ha leído, false si no
     */
    suspend fun haLeidoComunicado(comunicadoId: String, usuarioId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val comunicadoSnapshot = firestore.collection("comunicados")
                    .document(comunicadoId)
                    .get()
                    .await()
                
                if (!comunicadoSnapshot.exists()) {
                    return@withContext Result.Error(Exception("El comunicado no existe"))
                }
                
                val usuariosLeido = comunicadoSnapshot.get("usuariosLeidos") as? List<String> ?: emptyList()
                Result.Success(usuariosLeido.contains(usuarioId))
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar si el comunicado ha sido leído")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Verifica si un usuario ha confirmado la lectura de un comunicado.
     * 
     * @param comunicadoId Identificador del comunicado
     * @param usuarioId Identificador del usuario
     * @return Resultado que contiene true si el usuario ha confirmado, false si no
     */
    suspend fun haConfirmadoLectura(comunicadoId: String, usuarioId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val comunicadoSnapshot = firestore.collection("comunicados")
                    .document(comunicadoId)
                    .get()
                    .await()
                
                if (!comunicadoSnapshot.exists()) {
                    return@withContext Result.Error(Exception("El comunicado no existe"))
                }
                
                val usuariosConfirmado = comunicadoSnapshot.get("usuariosConfirmados") as? List<String> ?: emptyList()
                Result.Success(usuariosConfirmado.contains(usuarioId))
            } catch (e: Exception) {
                Timber.e(e, "Error al verificar si el comunicado ha sido confirmado")
                Result.Error(e)
            }
        }
    }
    
    /**
     * Obtiene las estadísticas de lectura de un comunicado
     * 
     * @param comunicadoId ID del comunicado
     * @param listaDestinatarios Lista de IDs de usuarios destinatarios (opcional)
     * @return Estadísticas de lectura del comunicado
     */
    suspend fun obtenerEstadisticasComunicado(
        comunicadoId: String, 
        listaDestinatarios: List<String>? = null
    ): Result<EstadisticasComunicado> {
        return try {
            val documento = comunicadosCollection.document(comunicadoId).get().await()
            if (!documento.exists()) {
                Result.Error(Exception("El comunicado no existe"))
            } else {
                val comunicado = documento.toObject(Comunicado::class.java)
                    ?: return Result.Error(Exception("Error al convertir comunicado"))
                
                // Si no se proporciona una lista de destinatarios, usamos la actual
                // En un caso real, esta lista se obtendría de otro lugar (p.ej. una tabla de asignaciones)
                val destinatarios = listaDestinatarios ?: comunicado.usuariosLeidos + comunicado.usuariosConfirmados
                
                val totalDestinatarios = destinatarios.size
                val usuariosLeidos = comunicado.usuariosLeidos
                val usuariosConfirmados = comunicado.usuariosConfirmados
                
                val totalLeidos = usuariosLeidos.size
                val totalConfirmados = usuariosConfirmados.size
                
                val porcentajeLeido = if (totalDestinatarios > 0) 
                    totalLeidos.toFloat() / totalDestinatarios.toFloat() else 0f
                val porcentajeConfirmado = if (totalDestinatarios > 0) 
                    totalConfirmados.toFloat() / totalDestinatarios.toFloat() else 0f
                
                val usuariosPendientes = destinatarios.filter { 
                    !usuariosLeidos.contains(it) && !usuariosConfirmados.contains(it) 
                }
                
                Result.Success(EstadisticasComunicado(
                    totalDestinatarios = totalDestinatarios,
                    totalLeidos = totalLeidos,
                    totalConfirmados = totalConfirmados,
                    porcentajeLeido = porcentajeLeido,
                    porcentajeConfirmado = porcentajeConfirmado,
                    usuariosLeidos = usuariosLeidos,
                    usuariosConfirmados = usuariosConfirmados,
                    usuariosPendientes = usuariosPendientes
                ))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener estadísticas de comunicado")
            Result.Error(e)
        }
    }
    
    /**
     * Envía un comunicado y lo registra también como mensaje unificado
     */
    suspend fun enviarComunicado(comunicado: Comunicado): Result<String> {
        return try {
            // 1. Guardar el comunicado en su colección
            val docRef = comunicadosCollection.document()
            val comunicadoConId = comunicado.copy(id = docRef.id)
            docRef.set(comunicadoConId).await()
            
            // 2. Crear mensajes unificados para cada tipo de destinatario
            for (tipoDestinatario in comunicado.tiposDestinatarios) {
                val unifiedMessage = UnifiedMessage(
                    senderId = comunicado.creadoPor,
                    senderName = comunicado.remitente,
                    receiverId = "", // Vacío para comunicados generales
                    title = comunicado.titulo,
                    content = comunicado.mensaje,
                    type = MessageType.ANNOUNCEMENT, // Usar ANNOUNCEMENT para comunicados
                    timestamp = comunicado.fechaCreacion,
                    status = MessageStatus.UNREAD,
                    metadata = mapOf(
                        "comunicadoId" to docRef.id,
                        "requiereConfirmacion" to comunicado.requiereConfirmacion.toString(),
                        "tipoDestinatario" to tipoDestinatario.toString()
                    )
                )
                
                // Guardar en mensajes unificados
                unifiedMessageRepository?.sendMessage(unifiedMessage)
            }
            
            // 3. También enviar notificaciones como estaba antes
            enviarNotificacionesComunicado(comunicadoConId)
            
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar comunicado")
            Result.Error(e)
        }
    }
} 