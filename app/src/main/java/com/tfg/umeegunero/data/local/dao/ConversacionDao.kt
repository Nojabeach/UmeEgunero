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
    @Query("SELECT * FROM conversaciones WHERE id = :id")
    suspend fun getConversacionById(id: String): ConversacionEntity?
    
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
        WHERE (participante1Id = :usuarioId AND noLeidosParticipante1 > 0) 
        OR (participante2Id = :usuarioId AND noLeidosParticipante2 > 0)
        ORDER BY ultimoMensajeTimestamp DESC
    """)
    fun getConversacionesConNoLeidos(usuarioId: String): Flow<List<ConversacionEntity>>
    
    /**
     * Actualiza el último mensaje de una conversación.
     */
    @Query("""
        UPDATE conversaciones
        SET ultimoMensaje = :texto,
            ultimoMensajeTimestamp = :timestamp
        WHERE id = :conversacionId
    """)
    suspend fun actualizarUltimoMensaje(conversacionId: String, texto: String, timestamp: Long)
    
    /**
     * Actualiza el contador de mensajes no leídos.
     */
    @Transaction
    @Query("""
        UPDATE conversaciones
        SET noLeidosParticipante1 = noLeidosParticipante1 + 1
        WHERE id = :conversacionId AND participante1Id = :receptorId
    """)
    suspend fun incrementarNoLeidosP1(conversacionId: String, receptorId: String)
    
    @Transaction
    @Query("""
        UPDATE conversaciones
        SET noLeidosParticipante2 = noLeidosParticipante2 + 1
        WHERE id = :conversacionId AND participante2Id = :receptorId
    """)
    suspend fun incrementarNoLeidosP2(conversacionId: String, receptorId: String)
    
    @Transaction
    @Query("""
        UPDATE conversaciones
        SET noLeidosParticipante1 = 0
        WHERE id = :conversacionId AND participante1Id = :usuarioId
    """)
    suspend fun resetearNoLeidosP1(conversacionId: String, usuarioId: String)
    
    @Transaction
    @Query("""
        UPDATE conversaciones
        SET noLeidosParticipante2 = 0
        WHERE id = :conversacionId AND participante2Id = :usuarioId
    """)
    suspend fun resetearNoLeidosP2(conversacionId: String, usuarioId: String)
    
    /**
     * Obtiene conversaciones que necesitan ser sincronizadas.
     */
    @Query("SELECT * FROM conversaciones WHERE sincronizada = 0")
    suspend fun getConversacionesNoSincronizadas(): List<ConversacionEntity>
    
    /**
     * Marca conversaciones como sincronizadas.
     */
    @Query("UPDATE conversaciones SET sincronizada = 1 WHERE id = :conversacionId")
    suspend fun marcarComoSincronizada(conversacionId: String)
    
    /**
     * Desactiva una conversación.
     */
    @Query("UPDATE conversaciones SET activa = 0 WHERE id = :conversacionId")
    suspend fun desactivarConversacion(conversacionId: String)
    
    /**
     * Actualiza el contador de mensajes no leídos.
     */
    @Transaction
    @Query("""
        UPDATE conversaciones
        SET noLeidosParticipante1 = :contador
        WHERE id = :conversacionId AND participante1Id = :usuarioId
    """)
    suspend fun actualizarContadorNoLeidos(conversacionId: String, usuarioId: String, contador: Int = 0)
    
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
        SELECT SUM(CASE WHEN participante1Id = :usuarioId THEN noLeidosParticipante1 ELSE noLeidosParticipante2 END) FROM conversaciones
        WHERE participante1Id = :usuarioId OR participante2Id = :usuarioId
    """)
    fun getTotalMensajesNoLeidos(usuarioId: String): Flow<Int>
} 