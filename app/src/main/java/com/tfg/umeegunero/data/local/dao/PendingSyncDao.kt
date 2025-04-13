package com.tfg.umeegunero.data.local.dao

import androidx.room.*
import com.tfg.umeegunero.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para manejar las operaciones pendientes de sincronización
 */
@Dao
interface PendingSyncDao {
    
    /**
     * Inserta una nueva operación pendiente de sincronización
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSync(pendingSync: PendingSyncEntity)
    
    /**
     * Obtiene todas las operaciones pendientes
     */
    @Query("SELECT * FROM pending_sync ORDER BY timestamp DESC")
    fun getAllPendingSync(): Flow<List<PendingSyncEntity>>
    
    /**
     * Obtiene las operaciones pendientes por estado
     */
    @Query("SELECT * FROM pending_sync WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingOperations(): Flow<List<PendingSyncEntity>>
    
    /**
     * Obtiene las operaciones fallidas
     */
    @Query("SELECT * FROM pending_sync WHERE status = 'ERROR' ORDER BY timestamp DESC")
    fun getFailedOperations(): Flow<List<PendingSyncEntity>>
    
    /**
     * Actualiza el estado de una operación
     */
    @Update
    suspend fun updatePendingSync(pendingSync: PendingSyncEntity)
    
    /**
     * Elimina una operación pendiente
     */
    @Delete
    suspend fun deletePendingSync(pendingSync: PendingSyncEntity)
    
    /**
     * Elimina todas las operaciones completadas
     */
    @Query("DELETE FROM pending_sync WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedOperations()
    
    /**
     * Actualiza el estado de una operación y la vuelve a intentar
     */
    @Query("UPDATE pending_sync SET retryCount = retryCount + 1, status = 'PENDING' WHERE id = :id")
    suspend fun retryOperation(id: String)
    
    /**
     * Obtiene el número de operaciones pendientes
     */
    @Query("SELECT COUNT(*) FROM pending_sync WHERE status = 'PENDING'")
    suspend fun getPendingOperationsCount(): Int
} 