package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Comunicado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.tfg.umeegunero.util.Result

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
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class ComunicadoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
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
     * Crea un nuevo comunicado
     */
    suspend fun crearComunicado(comunicado: Comunicado): Result<Unit> = try {
        comunicadosCollection.document(comunicado.id).set(comunicado).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al crear comunicado")
        Result.Error(e)
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
                    "usuariosQueHanLeido", FieldValue.arrayUnion(usuarioId)
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
                    "usuariosQueHanConfirmado", FieldValue.arrayUnion(usuarioId)
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
                
                val usuariosLeido = comunicadoSnapshot.get("usuariosQueHanLeido") as? List<String> ?: emptyList()
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
                
                val usuariosConfirmado = comunicadoSnapshot.get("usuariosQueHanConfirmado") as? List<String> ?: emptyList()
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
} 