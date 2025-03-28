package com.tfg.umeegunero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la entidad RegistroActividadEntity.
 * 
 * Proporciona métodos para acceder a la base de datos local y realizar
 * operaciones CRUD sobre los registros de actividad.
 * 
 * @author Estudiante 2º DAM
 */
@Dao
interface RegistroActividadDao {
    
    /**
     * Inserta un nuevo registro de actividad en la base de datos.
     * En caso de conflicto (mismo ID), reemplaza el registro existente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistroActividad(registro: RegistroActividadEntity)
    
    /**
     * Inserta múltiples registros de actividad en la base de datos.
     * En caso de conflicto (mismo ID), reemplaza los registros existentes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistrosActividad(registros: List<RegistroActividadEntity>)
    
    /**
     * Actualiza un registro de actividad existente en la base de datos.
     */
    @Update
    suspend fun updateRegistroActividad(registro: RegistroActividadEntity)
    
    /**
     * Obtiene un registro de actividad por su ID.
     */
    @Query("SELECT * FROM registros_actividad WHERE id = :id")
    suspend fun getRegistroActividadById(id: String): RegistroActividadEntity?
    
    /**
     * Obtiene todos los registros de actividad de un alumno.
     * Los resultados se ordenan por fecha en orden descendente (más recientes primero).
     */
    @Query("SELECT * FROM registros_actividad WHERE alumnoId = :alumnoId ORDER BY fechaTimestamp DESC")
    fun getRegistrosActividadByAlumno(alumnoId: String): Flow<List<RegistroActividadEntity>>
    
    /**
     * Obtiene todos los registros de actividad de una clase en una fecha específica.
     */
    @Query("SELECT * FROM registros_actividad WHERE claseId = :claseId AND fechaTimestamp BETWEEN :startTimestamp AND :endTimestamp")
    suspend fun getRegistrosActividadByClaseAndFecha(claseId: String, startTimestamp: Long, endTimestamp: Long): List<RegistroActividadEntity>
    
    /**
     * Obtiene los registros de actividad no vistos por el familiar de un alumno.
     */
    @Query("SELECT * FROM registros_actividad WHERE alumnoId IN (:alumnosIds) AND vistoPorFamiliar = 0 ORDER BY fechaTimestamp DESC")
    suspend fun getRegistrosActividadNoVistos(alumnosIds: List<String>): List<RegistroActividadEntity>
    
    /**
     * Marca un registro de actividad como visto por el familiar.
     */
    @Query("UPDATE registros_actividad SET vistoPorFamiliar = 1, visualizadoPorFamiliar = 1, fechaVistoTimestamp = :timestamp, fechaVisualizacionTimestamp = :timestamp, sincronizado = 0 WHERE id = :id")
    suspend fun marcarRegistroComoVisto(id: String, timestamp: Long)
    
    /**
     * Obtiene todos los registros que no están sincronizados con el servidor.
     */
    @Query("SELECT * FROM registros_actividad WHERE sincronizado = 0")
    suspend fun getRegistrosNoSincronizados(): List<RegistroActividadEntity>
    
    /**
     * Marca un registro como sincronizado con el servidor.
     */
    @Query("UPDATE registros_actividad SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarRegistroComoSincronizado(id: String)
    
    /**
     * Elimina un registro de actividad por su ID.
     */
    @Query("DELETE FROM registros_actividad WHERE id = :id")
    suspend fun deleteRegistroActividad(id: String)
    
    /**
     * Elimina todos los registros de actividad de un alumno.
     */
    @Query("DELETE FROM registros_actividad WHERE alumnoId = :alumnoId")
    suspend fun deleteRegistrosActividadByAlumno(alumnoId: String)
} 