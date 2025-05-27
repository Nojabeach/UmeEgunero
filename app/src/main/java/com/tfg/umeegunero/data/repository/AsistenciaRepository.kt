package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.RegistroAsistencia
import com.tfg.umeegunero.data.model.Asistencia
import com.tfg.umeegunero.data.model.EstadoAsistencia
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repositorio para gestionar la asistencia de alumnos.
 *
 * Esta clase proporciona métodos para registrar, consultar y modificar
 * la asistencia de los alumnos en las clases, así como generar informes
 * detallados sobre la asistencia.
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 */
@Singleton
class AsistenciaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val asistenciaCollection = firestore.collection("asistencia")
    private val registrosAsistenciaCollection = firestore.collection("registrosAsistencia")
    
    /**
     * Registra la asistencia de un alumno
     * 
     * @param asistencia Objeto de asistencia a registrar
     * @return Resultado que indica éxito o error
     */
    suspend fun registrarAsistencia(asistencia: Asistencia): Result<String> {
        return try {
            val docRef = if (asistencia.id.isNotEmpty()) {
                asistenciaCollection.document(asistencia.id)
            } else {
                asistenciaCollection.document()
            }
            
            val asistenciaConId = if (asistencia.id.isEmpty()) {
                asistencia.copy(id = docRef.id)
            } else {
                asistencia
            }
            
            docRef.set(asistenciaConId).await()
            Result.Success(asistenciaConId.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar asistencia")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene la asistencia de un alumno en una fecha específica
     * 
     * @param alumnoId ID del alumno
     * @param fecha Fecha para la que se quiere consultar la asistencia
     * @return Resultado con la asistencia o error
     */
    suspend fun getAsistenciaAlumno(alumnoId: String, fecha: LocalDate): Result<Asistencia?> {
        return try {
            val startOfDay = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1)
            
            val startTimestamp = Timestamp(Date.from(startOfDay))
            val endTimestamp = Timestamp(Date.from(endOfDay))
            
            val snapshot = asistenciaCollection
                .whereEqualTo("alumnoId", alumnoId)
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get()
                .await()
            
            if (snapshot.documents.isEmpty()) {
                Result.Success(null)
            } else {
                val asistencia = snapshot.documents[0].toObject(Asistencia::class.java)
                Result.Success(asistencia)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener asistencia del alumno")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene las asistencias registradas para una clase en una fecha específica
     * 
     * @param claseId ID de la clase
     * @param fecha Fecha para la que se quieren consultar las asistencias
     * @return Resultado con la lista de asistencias o error
     */
    suspend fun getAsistenciaPorFechaYClase(claseId: String, fecha: LocalDate): Result<List<Asistencia>> {
        return try {
            val startOfDay = fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = fecha.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1)
            
            val startTimestamp = Timestamp(Date.from(startOfDay))
            val endTimestamp = Timestamp(Date.from(endOfDay))
            
            val snapshot = asistenciaCollection
                .whereEqualTo("claseId", claseId)
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get()
                .await()
            
            val asistencias = snapshot.documents.mapNotNull { it.toObject(Asistencia::class.java) }
            Result.Success(asistencias)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener asistencias por fecha y clase")
            Result.Error(e)
        }
    }
    
    /**
     * Actualiza el estado de asistencia de un alumno
     * 
     * @param asistenciaId ID del registro de asistencia
     * @param presente Nuevo estado de presencia
     * @param observaciones Observaciones adicionales (opcional)
     * @return Resultado que indica éxito o error
     */
    suspend fun actualizarAsistencia(
        asistenciaId: String, 
        presente: Boolean, 
        observaciones: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "presente" to presente
            )
            
            observaciones?.let { updates["observaciones"] = it }
            
            asistenciaCollection.document(asistenciaId)
                .update(updates)
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar asistencia")
            Result.Error(e)
        }
    }
    
    /**
     * Elimina un registro de asistencia
     * 
     * @param asistenciaId ID del registro de asistencia a eliminar
     * @return Resultado que indica éxito o error
     */
    suspend fun eliminarAsistencia(asistenciaId: String): Result<Unit> {
        return try {
            asistenciaCollection.document(asistenciaId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar asistencia")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene las estadísticas de asistencia para un alumno en un período
     * 
     * @param alumnoId ID del alumno
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return Resultado con las estadísticas o error
     */
    suspend fun getEstadisticasAsistencia(
        alumnoId: String, 
        fechaInicio: LocalDate, 
        fechaFin: LocalDate
    ): Result<Map<String, Any>> {
        return try {
            val startTimestamp = Timestamp(
                Date.from(
                    fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
            )
            
            val endTimestamp = Timestamp(
                Date.from(
                    fechaFin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1)
                )
            )
            
            val snapshot = registrosAsistenciaCollection
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get()
                .await()
            
            val registros = snapshot.documents.mapNotNull { it.toObject(RegistroAsistencia::class.java) }
            
            // Filtrar registros donde el alumno está presente
            val registrosConAlumno = registros.filter { registro ->
                registro.estadosAsistencia[alumnoId] == EstadoAsistencia.PRESENTE
            }
            
            val totalRegistros = registros.size
            val totalPresencias = registrosConAlumno.size
            val totalAusencias = totalRegistros - totalPresencias
            
            val porcentajeAsistencia = if (totalRegistros > 0) {
                (totalPresencias.toFloat() / totalRegistros) * 100
            } else {
                0f
            }
            
            val result = mapOf(
                "totalRegistros" to totalRegistros,
                "totalPresencias" to totalPresencias,
                "totalAusencias" to totalAusencias,
                "porcentajeAsistencia" to porcentajeAsistencia
            )
            
            Result.Success(result)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener estadísticas de asistencia")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene el registro de asistencia para una clase en una fecha específica
     * 
     * @param claseId ID de la clase
     * @param fecha Fecha para la que se quiere consultar la asistencia
     * @return Registro de asistencia o null si no existe
     */
    suspend fun obtenerRegistroAsistencia(claseId: String, fecha: Date): RegistroAsistencia? {
        return try {
            val startOfDay = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = startOfDay.plusSeconds(86399) // 23:59:59
            
            val startTimestamp = Timestamp(Date.from(startOfDay))
            val endTimestamp = Timestamp(Date.from(endOfDay))
            
            val snapshot = registrosAsistenciaCollection
                .whereEqualTo("claseId", claseId)
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get()
                .await()
            
            if (snapshot.documents.isEmpty()) {
                null
            } else {
                snapshot.documents[0].toObject(RegistroAsistencia::class.java)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro de asistencia")
            null
        }
    }

    /**
     * Guarda un registro de asistencia en Firestore
     * 
     * @param registroAsistencia el registro a guardar
     * @return true si se ha guardado correctamente, false en caso contrario
     */
    suspend fun guardarRegistroAsistencia(registroAsistencia: RegistroAsistencia): Boolean {
        return try {
            val registroId = if (registroAsistencia.id.isNotEmpty()) {
                registroAsistencia.id
            } else {
                UUID.randomUUID().toString()
            }
            
            // Comprobamos si ya existe un registro para esta clase y fecha
            val registroExistente = obtenerRegistroAsistencia(
                claseId = registroAsistencia.claseId,
                fecha = registroAsistencia.fecha.toDate()
            )
            
            val registroFinal = if (registroExistente != null) {
                // Si existe, actualizamos el registro existente
                registroAsistencia.copy(id = registroExistente.id)
            } else {
                // Si no existe, creamos uno nuevo
                registroAsistencia.copy(id = registroId)
            }
            
            registrosAsistenciaCollection.document(registroFinal.id)
                .set(registroFinal)
                .await()
            
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar registro de asistencia")
            false
        }
    }

    /**
     * Obtiene todos los registros de asistencia para una clase
     * @param claseId identificador de la clase
     * @return lista de registros de asistencia
     */
    suspend fun obtenerRegistrosAsistenciaPorClase(claseId: String): List<RegistroAsistencia> {
        return try {
            val snapshot = registrosAsistenciaCollection
                .whereEqualTo("claseId", claseId)
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.toObjects(RegistroAsistencia::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros de asistencia por clase")
            emptyList()
        }
    }

    /**
     * Elimina un registro de asistencia
     * 
     * @param registroId ID del registro de asistencia a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    suspend fun eliminarRegistroAsistencia(registroId: String): Boolean {
        return try {
            registrosAsistenciaCollection.document(registroId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar registro de asistencia")
            false
            }
        }
} 