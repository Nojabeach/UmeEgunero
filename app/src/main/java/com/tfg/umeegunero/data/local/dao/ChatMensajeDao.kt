package com.tfg.umeegunero.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import com.tfg.umeegunero.data.local.entity.ChatMensajeEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para interactuar con los mensajes de chat en la base de datos.
 * Proporciona métodos para insertar, actualizar, eliminar y consultar mensajes.
 */
@Dao
interface ChatMensajeDao {
    
    /**
     * Inserta un nuevo mensaje en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMensaje(mensaje: ChatMensajeEntity)
    
    /**
     * Inserta múltiples mensajes en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMensajes(mensajes: List<ChatMensajeEntity>)
    
    /**
     * Actualiza un mensaje existente.
     */
    @Update
    suspend fun updateMensaje(mensaje: ChatMensajeEntity)
    
    /**
     * Elimina un mensaje.
     */
    @Delete
    suspend fun deleteMensaje(mensaje: ChatMensajeEntity)
    
    /**
     * Obtiene un mensaje específico por su ID.
     */
    @Query("SELECT * FROM chat_mensajes WHERE id = :mensajeId")
    suspend fun getMensajeById(mensajeId: String): ChatMensajeEntity?
    
    /**
     * Obtiene todos los mensajes de una conversación ordenados por timestamp.
     * Devuelve un Flow para observar cambios.
     */
    @Query("SELECT * FROM chat_mensajes WHERE conversacionId = :conversacionId ORDER BY timestamp DESC")
    fun getMensajesByConversacionId(conversacionId: String): Flow<List<ChatMensajeEntity>>
    
    /**
     * Obtiene todos los mensajes de una conversación para usar con paginación.
     */
    @Query("SELECT * FROM chat_mensajes WHERE conversacionId = :conversacionId ORDER BY timestamp DESC")
    fun getMensajesPaginados(conversacionId: String): PagingSource<Int, ChatMensajeEntity>
    
    /**
     * Marca un mensaje como leído.
     */
    @Query("UPDATE chat_mensajes SET leido = 1, fechaLeido = :timestamp WHERE id = :mensajeId")
    suspend fun marcarComoLeido(mensajeId: String, timestamp: Long)
    
    /**
     * Obtiene el número de mensajes no leídos para un usuario en una conversación.
     */
    @Query("SELECT COUNT(*) FROM chat_mensajes WHERE conversacionId = :conversacionId AND receptorId = :usuarioId AND leido = 0")
    suspend fun getMensajesNoLeidos(conversacionId: String, usuarioId: String): Int
    
    /**
     * Marca todos los mensajes de una conversación como leídos para un usuario.
     */
    @Query("UPDATE chat_mensajes SET leido = 1, fechaLeido = :timestamp WHERE conversacionId = :conversacionId AND receptorId = :usuarioId AND leido = 0")
    suspend fun marcarTodosComoLeidos(conversacionId: String, usuarioId: String, timestamp: Long)
    
    /**
     * Elimina todos los mensajes de una conversación.
     */
    @Query("DELETE FROM chat_mensajes WHERE conversacionId = :conversacionId")
    suspend fun eliminarMensajesConversacion(conversacionId: String)
    
    /**
     * Obtiene el último mensaje de una conversación.
     */
    @Query("SELECT * FROM chat_mensajes WHERE conversacionId = :conversacionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getUltimoMensaje(conversacionId: String): ChatMensajeEntity?
} 