package com.tfg.umeegunero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO para acceder a los registros de actividad almacenados localmente.
 * Proporciona métodos para realizar operaciones CRUD en la tabla de registros.
 */
@Dao
interface RegistroActividadDao {
    
    /**
     * Inserta un nuevo registro de actividad.
     * Si ya existe uno con el mismo ID, lo reemplaza.
     * 
     * @param registro El registro a insertar
     * @return El ID del registro insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistroActividad(registro: RegistroActividadEntity): Long
    
    /**
     * Inserta múltiples registros de actividad.
     * Si ya existen con el mismo ID, los reemplaza.
     * 
     * @param registros Lista de registros a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistrosActividad(registros: List<RegistroActividadEntity>)
    
    /**
     * Actualiza un registro de actividad existente.
     * 
     * @param registro El registro a actualizar
     * @return Número de filas afectadas
     */
    @Update
    suspend fun updateRegistroActividad(registro: RegistroActividadEntity): Int
    
    /**
     * Obtiene un registro de actividad por su ID.
     * 
     * @param id ID del registro
     * @return El registro encontrado o null si no existe
     */
    @Query("SELECT * FROM registros_actividad WHERE id = :id LIMIT 1")
    suspend fun getRegistroActividadById(id: String): RegistroActividadEntity?
    
    /**
     * Obtiene todos los registros de actividad para un alumno específico.
     * 
     * @param alumnoId ID del alumno
     * @return Flow con la lista de registros
     */
    @Query("SELECT * FROM registros_actividad WHERE alumnoId = :alumnoId ORDER BY fechaTimestamp DESC")
    fun getRegistrosActividadByAlumno(alumnoId: String): Flow<List<RegistroActividadEntity>>
    
    /**
     * Obtiene los registros de actividad de un alumno en una fecha específica.
     * 
     * @param alumnoId ID del alumno
     * @param startTime Timestamp de inicio del día
     * @param endTime Timestamp de fin del día
     * @return Lista de registros encontrados
     */
    @Query("SELECT * FROM registros_actividad WHERE alumnoId = :alumnoId AND fechaTimestamp BETWEEN :startTime AND :endTime ORDER BY fechaTimestamp DESC")
    suspend fun getRegistrosActividadByAlumnoAndFecha(alumnoId: String, startTime: Long, endTime: Long): List<RegistroActividadEntity>
    
    /**
     * Obtiene todos los registros de actividad para una clase en una fecha específica.
     * 
     * @param claseId ID de la clase
     * @param startTime Timestamp de inicio del día
     * @param endTime Timestamp de fin del día
     * @return Lista de registros encontrados
     */
    @Query("SELECT * FROM registros_actividad WHERE claseId = :claseId AND fechaTimestamp BETWEEN :startTime AND :endTime ORDER BY fechaTimestamp DESC")
    suspend fun getRegistrosActividadByClaseAndFecha(claseId: String, startTime: Long, endTime: Long): List<RegistroActividadEntity>
    
    /**
     * Obtiene todos los registros no sincronizados con el servidor.
     * 
     * @return Lista de registros no sincronizados
     */
    @Query("SELECT * FROM registros_actividad WHERE isSynced = 0")
    suspend fun getUnsyncedRegistros(): List<RegistroActividadEntity>
    
    /**
     * Marca un registro como sincronizado con el servidor.
     * 
     * @param id ID del registro
     * @return Número de filas afectadas
     */
    @Query("UPDATE registros_actividad SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String): Int
    
    /**
     * Elimina un registro por su ID.
     * 
     * @param id ID del registro a eliminar
     * @return Número de filas afectadas
     */
    @Query("DELETE FROM registros_actividad WHERE id = :id")
    suspend fun deleteRegistroById(id: String): Int
    
    /**
     * Elimina todos los registros de la tabla.
     * 
     * @return Número de filas afectadas
     */
    @Query("DELETE FROM registros_actividad")
    suspend fun deleteAllRegistros(): Int
} 