package com.tfg.umeegunero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tfg.umeegunero.data.local.entity.ConversacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para interactuar con las conversaciones en la base de datos.
 * Proporciona métodos para insertar, actualizar, eliminar y consultar conversaciones.
 */
@Dao
interface ConversacionDao {
    
    /**
     * Inserta una nueva conversación en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversacion(conversacion: ConversacionEntity)
    
    /**
     * Inserta múltiples conversaciones en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversaciones(conversaciones: List<ConversacionEntity>)
    
    /**
     * Actualiza una conversación existente.
     */
    @Update
    suspend fun updateConversacion(conversacion: ConversacionEntity)
    
    /**
     * Obtiene una conversación específica por su ID.
     */
    @Query("SELECT * FROM conversaciones WHERE id = :conversacionId")
    suspend fun getConversacionById(conversacionId: String): ConversacionEntity?
    
    /**
     * Obtiene una conversación entre dos usuarios.
     */
    @Query("""
        SELECT * FROM conversaciones 
        WHERE (participante1Id = :usuario1Id AND participante2Id = :usuario2Id) 
           OR (participante1Id = :usuario2Id AND participante2Id = :usuario1Id)
        LIMIT 1
    """)
    suspend fun getConversacionEntreUsuarios(usuario1Id: String, usuario2Id: String): ConversacionEntity?
    
    /**
     * Obtiene todas las conversaciones de un usuario ordenadas por la fecha del último mensaje.
     */
    @Query("""
        SELECT * FROM conversaciones 
        WHERE participante1Id = :usuarioId OR participante2Id = :usuarioId
        ORDER BY ultimoMensajeTimestamp DESC
    """)
    fun getConversacionesByUsuarioId(usuarioId: String): Flow<List<ConversacionEntity>>
    
    /**
     * Obtiene las conversaciones con mensajes no leídos para un usuario.
     */
    @Query("""
        SELECT * FROM conversaciones 
        WHERE (participante1Id = :usuarioId OR participante2Id = :usuarioId) 
        AND tieneNoLeidos = 1 AND ultimoMensajeEmisorId != :usuarioId
        ORDER BY ultimoMensajeTimestamp DESC
    """)
    fun getConversacionesConNoLeidos(usuarioId: String): Flow<List<ConversacionEntity>>
    
    /**
     * Actualiza el último mensaje de una conversación.
     */
    @Query("""
        UPDATE conversaciones
        SET ultimoMensajeId = :mensajeId,
            ultimoMensajeTexto = :mensajeTexto,
            ultimoMensajeTimestamp = :mensajeTimestamp,
            ultimoMensajeEmisorId = :emisorId,
            updatedAt = :timestamp
        WHERE id = :conversacionId
    """)
    suspend fun actualizarUltimoMensaje(
        conversacionId: String,
        mensajeId: String,
        mensajeTexto: String,
        mensajeTimestamp: Long,
        emisorId: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Actualiza el contador de mensajes no leídos.
     */
    @Transaction
    @Query("""
        UPDATE conversaciones
        SET cantidadNoLeidos = (
            SELECT COUNT(*) FROM chat_mensajes
            WHERE conversacionId = :conversacionId AND receptorId = :receptorId AND leido = 0
        ),
        tieneNoLeidos = (
            SELECT COUNT(*) FROM chat_mensajes
            WHERE conversacionId = :conversacionId AND receptorId = :receptorId AND leido = 0
        ) > 0
        WHERE id = :conversacionId
    """)
    suspend fun actualizarContadorNoLeidos(conversacionId: String, receptorId: String)
    
    /**
     * Elimina una conversación por su ID.
     */
    @Query("DELETE FROM conversaciones WHERE id = :conversacionId")
    suspend fun eliminarConversacion(conversacionId: String)
    
    /**
     * Busca conversaciones por el nombre del otro participante.
     */
    @Query("""
        SELECT * FROM conversaciones 
        WHERE (participante1Id = :usuarioId AND nombreParticipante2 LIKE '%' || :query || '%')
           OR (participante2Id = :usuarioId AND nombreParticipante1 LIKE '%' || :query || '%')
        ORDER BY ultimoMensajeTimestamp DESC
    """)
    fun buscarConversaciones(usuarioId: String, query: String): Flow<List<ConversacionEntity>>
    
    /**
     * Obtiene el número total de mensajes no leídos para un usuario.
     */
    @Query("""
        SELECT SUM(cantidadNoLeidos) FROM conversaciones
        WHERE (participante1Id = :usuarioId OR participante2Id = :usuarioId)
        AND ultimoMensajeEmisorId != :usuarioId
    """)
    fun getTotalMensajesNoLeidos(usuarioId: String): Flow<Int>
    
    /**
     * Obtiene conversaciones que necesitan ser sincronizadas.
     */
    @Query("SELECT * FROM conversaciones WHERE sincronizado = 0")
    suspend fun getConversacionesNoSincronizadas(): List<ConversacionEntity>
    
    /**
     * Marca conversaciones como sincronizadas.
     */
    @Query("UPDATE conversaciones SET sincronizado = 1 WHERE id IN (:conversacionIds)")
    suspend fun marcarComoSincronizadas(conversacionIds: List<String>)
} 