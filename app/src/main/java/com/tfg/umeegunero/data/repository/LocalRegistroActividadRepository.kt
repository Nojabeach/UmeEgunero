package com.tfg.umeegunero.data.repository

import com.tfg.umeegunero.data.local.dao.RegistroActividadDao
import com.tfg.umeegunero.data.local.entity.RegistroActividadEntity
import com.tfg.umeegunero.data.model.RegistroActividad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar registros de actividad locales en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para almacenar, recuperar y gestionar
 * registros de actividad locales, permitiendo un seguimiento detallado
 * de las interacciones y eventos realizados por los usuarios en la aplicación.
 *
 * Características principales:
 * - Almacenamiento local de registros de actividad
 * - Sincronización con registros en la nube
 * - Soporte para diferentes tipos de actividades
 * - Registro de eventos de usuario
 * - Gestión de privacidad y seguridad
 *
 * El repositorio permite:
 * - Registrar acciones de usuario
 * - Mantener un historial de interacciones
 * - Auditar cambios y accesos
 * - Generar informes de actividad
 * - Depurar y monitorear el uso de la aplicación
 *
 * @property database Base de datos local para almacenar registros de actividad
 * @property firestore Instancia de FirebaseFirestore para sincronización de registros
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class LocalRegistroActividadRepository @Inject constructor(
    private val registroActividadDao: RegistroActividadDao
) {
    /**
     * Guarda un registro de actividad en la base de datos local.
     * 
     * @param registro El registro a guardar
     * @param isSynced Indica si el registro está sincronizado con el servidor
     */
    suspend fun saveRegistroActividad(registro: RegistroActividad, isSynced: Boolean) {
        try {
            val entity = RegistroActividadEntity.fromRegistroActividad(registro, isSynced)
            registroActividadDao.insertRegistroActividad(entity)
            Timber.d("Registro guardado localmente: ${registro.id}")
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar registro en BD local: ${registro.id}")
        }
    }
    
    /**
     * Guarda múltiples registros de actividad en la base de datos local.
     * 
     * @param registros Lista de registros a guardar
     * @param isSynced Indica si los registros están sincronizados con el servidor
     */
    suspend fun saveRegistrosActividad(registros: List<RegistroActividad>, isSynced: Boolean) {
        try {
            val entities = registros.map { RegistroActividadEntity.fromRegistroActividad(it, isSynced) }
            registroActividadDao.insertRegistrosActividad(entities)
            Timber.d("Guardados ${registros.size} registros en BD local")
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar múltiples registros en BD local")
        }
    }
    
    /**
     * Actualiza un registro de actividad existente en la base de datos local.
     * 
     * @param registro El registro a actualizar
     * @param isSynced Indica si el registro está sincronizado con el servidor
     */
    suspend fun updateRegistroActividad(registro: RegistroActividad, isSynced: Boolean) {
        try {
            val entity = RegistroActividadEntity.fromRegistroActividad(registro, isSynced)
            val rowsAffected = registroActividadDao.updateRegistroActividad(entity)
            Timber.d("Registro actualizado localmente: ${registro.id}, filas afectadas: $rowsAffected")
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar registro en BD local: ${registro.id}")
        }
    }
    
    /**
     * Obtiene un registro de actividad por su ID.
     * 
     * @param id ID del registro
     * @return El registro encontrado o null si no existe
     */
    suspend fun getRegistroActividadById(id: String): RegistroActividad? {
        return try {
            val entity = registroActividadDao.getRegistroActividadById(id)
            entity?.toRegistroActividad()
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro por ID: $id")
            null
        }
    }
    
    /**
     * Obtiene los registros de actividad de un alumno.
     * 
     * @param alumnoId ID del alumno
     * @return Flow con la lista de registros
     */
    fun getRegistrosActividadByAlumno(alumnoId: String): Flow<List<RegistroActividad>> {
        return registroActividadDao.getRegistrosActividadByAlumno(alumnoId)
            .map { entities -> entities.map { it.toRegistroActividad() } }
    }
    
    /**
     * Obtiene los registros de actividad de un alumno en una fecha específica.
     * 
     * @param alumnoId ID del alumno
     * @param fecha Fecha para filtrar registros
     * @return Lista de registros encontrados
     */
    suspend fun getRegistrosActividadByAlumnoAndFecha(alumnoId: String, fecha: Date): List<RegistroActividad> {
        try {
            // Obtener límites del día (inicio y fin)
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicioDia = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val finDia = calendar.timeInMillis
            
            val entities = registroActividadDao.getRegistrosActividadByAlumnoAndFecha(alumnoId, inicioDia, finDia)
            return entities.map { it.toRegistroActividad() }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por alumno y fecha: $alumnoId")
            return emptyList()
        }
    }
    
    /**
     * Obtiene los registros de actividad de una clase en una fecha específica.
     * 
     * @param claseId ID de la clase
     * @param fecha Fecha para filtrar registros
     * @return Lista de registros encontrados
     */
    suspend fun getRegistrosActividadByClaseAndFecha(claseId: String, fecha: Date): List<RegistroActividad> {
        try {
            // Obtener límites del día (inicio y fin)
            val calendar = Calendar.getInstance()
            calendar.time = fecha
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val inicioDia = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val finDia = calendar.timeInMillis
            
            val entities = registroActividadDao.getRegistrosActividadByClaseAndFecha(claseId, inicioDia, finDia)
            return entities.map { it.toRegistroActividad() }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por clase y fecha: $claseId")
            return emptyList()
        }
    }
    
    /**
     * Obtiene los registros no sincronizados con el servidor.
     * 
     * @return Lista de registros no sincronizados
     */
    suspend fun getUnsyncedRegistros(): List<RegistroActividad> {
        try {
            val entities = registroActividadDao.getUnsyncedRegistros()
            return entities.map { it.toRegistroActividad() }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros no sincronizados")
            return emptyList()
        }
    }
    
    /**
     * Marca un registro como sincronizado con el servidor.
     * 
     * @param id ID del registro
     * @return true si se actualizó correctamente, false en caso contrario
     */
    suspend fun markAsSynced(id: String): Boolean {
        return try {
            val rowsAffected = registroActividadDao.markAsSynced(id)
            val success = rowsAffected > 0
            Timber.d("Registro marcado como sincronizado: $id, éxito: $success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar registro como sincronizado: $id")
            false
        }
    }
    
    /**
     * Elimina un registro por su ID.
     * 
     * @param id ID del registro a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    suspend fun deleteRegistroById(id: String): Boolean {
        return try {
            val rowsAffected = registroActividadDao.deleteRegistroById(id)
            val success = rowsAffected > 0
            Timber.d("Registro eliminado: $id, éxito: $success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar registro: $id")
            false
        }
    }
    
    /**
     * Elimina todos los registros de la base de datos local.
     * 
     * @return Número de registros eliminados
     */
    suspend fun deleteAllRegistros(): Int {
        return try {
            val rowsAffected = registroActividadDao.deleteAllRegistros()
            Timber.d("Se eliminaron $rowsAffected registros de la BD local")
            rowsAffected
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar todos los registros")
            0
        }
    }
    
    /**
     * Obtiene los registros de actividad de un alumno por su ID.
     * 
     * @param alumnoId ID del alumno
     * @return Lista de registros del alumno
     */
    suspend fun getRegistrosActividadByAlumnoId(alumnoId: String): List<RegistroActividad> {
        return try {
            val entities = registroActividadDao.getRegistrosActividadByAlumnoIdList(alumnoId)
            entities.map { it.toRegistroActividad() }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por alumnoId: $alumnoId")
            emptyList()
        }
    }
    
    /**
     * Obtiene el número de registros no leídos.
     * El parámetro familiarId se ignora en la implementación actual pero se mantiene
     * para compatibilidad con la interfaz.
     * 
     * @param familiarId ID del familiar (ignorado en esta implementación)
     * @return Número de registros sin leer
     */
    suspend fun getRegistrosSinLeerCount(familiarId: String): Int {
        return try {
            // Este DAO no tiene en cuenta el familiarId, solo cuenta todos los registros no vistos
            registroActividadDao.getRegistrosSinLeerCount()
        } catch (e: Exception) {
            Timber.e(e, "Error al contar registros sin leer para el familiar: $familiarId")
            0
        }
    }
    
    /**
     * Elimina un registro de actividad de la base de datos local por su ID
     *
     * @param registroId ID del registro a eliminar
     */
    suspend fun deleteRegistroActividad(registroId: String) {
        try {
            Timber.d("Eliminando registro local con ID: $registroId")
            val rowsAffected = registroActividadDao.deleteRegistroById(registroId)
            Timber.d("Registro eliminado: $registroId, filas afectadas: $rowsAffected")
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar registro local: $registroId")
        }
    }
    
    /**
     * Obtiene los registros de actividad de un alumno entre dos fechas.
     * 
     * @param alumnoId ID del alumno
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de registros encontrados en ese rango de fechas
     */
    suspend fun getRegistrosActividadByAlumnoAndFechaRange(
        alumnoId: String,
        fechaInicio: Date,
        fechaFin: Date
    ): List<RegistroActividad> {
        try {
            // Convertir fechas a timestamps
            val inicioDia = fechaInicio.time
            val finDia = fechaFin.time
            
            Timber.d("Buscando registros locales para alumno $alumnoId entre $fechaInicio y $fechaFin")
            
            val entities = registroActividadDao.getRegistrosActividadByAlumnoAndFecha(
                alumnoId = alumnoId,
                startTime = inicioDia,
                endTime = finDia
            )
            
            Timber.d("Encontrados ${entities.size} registros locales")
            return entities.map { it.toRegistroActividad() }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros por alumno y rango de fechas: $alumnoId")
            return emptyList()
        }
    }
} 