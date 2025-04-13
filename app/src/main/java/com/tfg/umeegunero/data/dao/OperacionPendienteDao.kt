package com.tfg.umeegunero.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tfg.umeegunero.data.model.OperacionPendiente
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones pendientes de sincronización
 */
@Dao
interface OperacionPendienteDao {
    
    /**
     * Inserta una nueva operación pendiente
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(operacion: OperacionPendiente)
    
    /**
     * Inserta una lista de operaciones pendientes
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(operaciones: List<OperacionPendiente>)
    
    /**
     * Actualiza una operación pendiente existente
     */
    @Update
    suspend fun actualizar(operacion: OperacionPendiente)
    
    /**
     * Elimina una operación pendiente por su ID
     */
    @Query("DELETE FROM operaciones_pendientes WHERE id = :id")
    suspend fun eliminarPorId(id: String)
    
    /**
     * Elimina una lista de operaciones pendientes
     */
    @Delete
    suspend fun eliminarOperaciones(operaciones: List<OperacionPendiente>)
    
    /**
     * Obtiene todas las operaciones pendientes como Flow
     */
    @Query("SELECT * FROM operaciones_pendientes ORDER BY timestamp ASC")
    fun obtenerTodasLasOperacionesPendientes(): Flow<List<OperacionPendiente>>
    
    /**
     * Obtiene todas las operaciones pendientes como lista
     */
    @Query("SELECT * FROM operaciones_pendientes ORDER BY timestamp ASC")
    suspend fun obtenerOperacionesList(): List<OperacionPendiente>
    
    /**
     * Obtiene las operaciones pendientes por tipo
     */
    @Query("SELECT * FROM operaciones_pendientes WHERE tipo = :tipo ORDER BY timestamp ASC")
    suspend fun obtenerOperacionesPorTipo(tipo: String): List<OperacionPendiente>
    
    /**
     * Obtiene las operaciones pendientes por tipo de entidad
     */
    @Query("SELECT * FROM operaciones_pendientes WHERE tipoEntidad = :tipoEntidad ORDER BY timestamp ASC")
    suspend fun obtenerOperacionesPorTipoEntidad(tipoEntidad: String): List<OperacionPendiente>
    
    /**
     * Obtiene las operaciones que han fallado más de cierto número de intentos
     */
    @Query("SELECT * FROM operaciones_pendientes WHERE intentos >= :maxIntentos")
    suspend fun obtenerOperacionesFallidas(maxIntentos: Int): List<OperacionPendiente>
    
    /**
     * Cuenta el número total de operaciones pendientes
     */
    @Query("SELECT COUNT(*) FROM operaciones_pendientes")
    suspend fun contarOperacionesPendientes(): Int
    
    /**
     * Vacía la tabla de operaciones pendientes
     */
    @Query("DELETE FROM operaciones_pendientes")
    suspend fun vaciarTabla()
} 