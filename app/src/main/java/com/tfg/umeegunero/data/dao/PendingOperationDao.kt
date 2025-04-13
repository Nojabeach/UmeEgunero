package com.tfg.umeegunero.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tfg.umeegunero.data.model.PendingOperation
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestionar las operaciones pendientes en la base de datos local
 */
@Dao
interface PendingOperationDao {
    
    /**
     * Obtiene todas las operaciones pendientes
     */
    @Query("SELECT * FROM pending_operations ORDER BY timestamp ASC")
    fun getAllPendingOperations(): Flow<List<PendingOperation>>
    
    /**
     * Obtiene una operación pendiente por su ID
     */
    @Query("SELECT * FROM pending_operations WHERE id = :id")
    suspend fun getById(id: String): PendingOperation?
    
    /**
     * Obtiene operaciones pendientes por tipo de entidad
     */
    @Query("SELECT * FROM pending_operations WHERE entityType = :entityType ORDER BY timestamp ASC")
    suspend fun getByEntityType(entityType: PendingOperation.EntityType): List<PendingOperation>
    
    /**
     * Obtiene operaciones pendientes por tipo de operación
     */
    @Query("SELECT * FROM pending_operations WHERE type = :type ORDER BY timestamp ASC")
    suspend fun getByType(type: PendingOperation.Type): List<PendingOperation>
    
    /**
     * Inserta una nueva operación pendiente
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperation)
    
    /**
     * Inserta múltiples operaciones pendientes
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(operations: List<PendingOperation>)
    
    /**
     * Actualiza una operación pendiente existente
     */
    @Update
    suspend fun update(operation: PendingOperation)
    
    /**
     * Elimina una operación pendiente
     */
    @Delete
    suspend fun delete(operation: PendingOperation)
    
    /**
     * Elimina una operación pendiente por su ID
     */
    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Elimina todas las operaciones pendientes
     */
    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
    
    /**
     * Obtiene el número total de operaciones pendientes
     */
    @Query("SELECT COUNT(*) FROM pending_operations")
    suspend fun getCount(): Int
    
    /**
     * Obtiene operaciones pendientes para una entidad específica
     */
    @Query("SELECT * FROM pending_operations WHERE entityType = :entityType AND entityId = :entityId ORDER BY timestamp ASC")
    suspend fun getByEntity(entityType: PendingOperation.EntityType, entityId: String): List<PendingOperation>

    /**
     * Incrementa el contador de reintentos de una operación
     */
    @Query("UPDATE pending_operations SET retryCount = retryCount + 1, lastError = :errorMessage WHERE id = :id")
    suspend fun incrementRetryCount(id: String, errorMessage: String?)
    
    /**
     * Obtiene las operaciones que han fallado más de cierto número de veces
     */
    @Query("SELECT * FROM pending_operations WHERE retryCount >= :maxRetries")
    suspend fun getFailedOperations(maxRetries: Int): List<PendingOperation>
} 