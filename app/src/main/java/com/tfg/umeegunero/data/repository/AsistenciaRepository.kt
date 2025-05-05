package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.RegistroAsistencia
import com.tfg.umeegunero.data.model.Asistencia
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar la asistencia de alumnos en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para registrar, consultar y gestionar
 * la asistencia de los alumnos en diferentes contextos educativos, permitiendo
 * un seguimiento detallado de la presencia y participación de los estudiantes.
 *
 * Características principales:
 * - Registro de asistencia diaria
 * - Generación de informes de asistencia
 * - Control de ausencias y retrasos
 * - Notificación a familias sobre inasistencias
 * - Gestión de justificaciones de ausencia
 *
 * El repositorio permite:
 * - Registrar la asistencia de alumnos por clase
 * - Consultar histórico de asistencia
 * - Generar estadísticas de asistencia
 * - Notificar a familias sobre ausencias
 * - Gestionar permisos y justificaciones
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property notificacionRepository Repositorio para enviar notificaciones relacionadas
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class RegistroAsistenciaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ASISTENCIAS = "registrosAsistencia"
    }

    /**
     * Guarda un registro de asistencia en Firestore
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
            
            val idFinal = registroExistente?.id ?: registroId
            
            // Si existe, actualizamos, si no, creamos uno nuevo
            firestore.collection(COLLECTION_ASISTENCIAS)
                .document(idFinal)
                .set(registroAsistencia.copy(id = idFinal))
                .await()
            
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar registro de asistencia")
            false
        }
    }

    /**
     * Obtiene un registro de asistencia específico por clase y fecha
     * @param claseId identificador de la clase
     * @param fecha fecha del registro
     * @return el registro de asistencia o null si no existe
     */
    suspend fun obtenerRegistroAsistencia(claseId: String, fecha: Date): RegistroAsistencia? {
        return try {
            // Convertimos la fecha a un rango del día completo
            val calendar = java.util.Calendar.getInstance()
            calendar.time = fecha
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val inicioDelDia = calendar.time
            
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val finDelDia = calendar.time
            
            // Buscamos registros para esta clase en el rango de fecha
            val snapshot = firestore.collection(COLLECTION_ASISTENCIAS)
                .whereEqualTo("claseId", claseId)
                .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDelDia))
                .whereLessThanOrEqualTo("fecha", Timestamp(finDelDia))
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                null
            } else {
                snapshot.documents.first().toObject(RegistroAsistencia::class.java)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registro de asistencia")
            null
        }
    }

    /**
     * Obtiene todos los registros de asistencia para una clase
     * @param claseId identificador de la clase
     * @return lista de registros de asistencia
     */
    suspend fun obtenerRegistrosAsistenciaPorClase(claseId: String): List<RegistroAsistencia> {
        return try {
            val snapshot = firestore.collection(COLLECTION_ASISTENCIAS)
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
     * Obtiene todos los registros de asistencia para un alumno en una clase específica
     * @param claseId identificador de la clase
     * @param dniAlumno DNI del alumno
     * @return lista de registros de asistencia que contienen al alumno
     */
    suspend fun obtenerRegistrosAsistenciaPorAlumno(
        claseId: String,
        dniAlumno: String
    ): List<RegistroAsistencia> {
        return try {
            // Primero obtenemos todos los registros de la clase
            val registros = obtenerRegistrosAsistenciaPorClase(claseId)
            
            // Filtramos los que contienen al alumno
            registros.filter { registro ->
                registro.estadosAsistencia.containsKey(dniAlumno)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener registros de asistencia por alumno")
            emptyList()
        }
    }

    /**
     * Elimina un registro de asistencia
     * @param registroId identificador del registro
     * @return true si se ha eliminado correctamente, false en caso contrario
     */
    suspend fun eliminarRegistroAsistencia(registroId: String): Boolean {
        return try {
            firestore.collection(COLLECTION_ASISTENCIAS)
                .document(registroId)
                .delete()
                .await()
            
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar registro de asistencia")
            false
        }
    }
}

/**
 * Repositorio para gestionar la asistencia de los alumnos
 */
interface AsistenciaRepository {
    /**
     * Registra la asistencia de una clase para una fecha determinada
     */
    suspend fun registrarAsistencia(
        claseId: String,
        fecha: Date,
        estadosAsistencia: Map<String, Asistencia>
    ): Result<Boolean>
    
    /**
     * Obtiene el registro de asistencia de una clase para una fecha determinada
     */
    suspend fun obtenerAsistencia(
        claseId: String,
        fecha: Date
    ): Result<Map<String, Asistencia>>
    
    /**
     * Guarda un registro de asistencia completo
     */
    suspend fun guardarRegistroAsistencia(registroAsistencia: RegistroAsistencia): Result<Unit>
    
    /**
     * Obtiene un registro de asistencia para una clase y fecha específicas
     */
    suspend fun obtenerRegistroAsistencia(claseId: String, fecha: Date): RegistroAsistencia?
}

/**
 * Implementación del repositorio de asistencia
 */
@Singleton
class AsistenciaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AsistenciaRepository {
    
    private val asistenciaCollection = firestore.collection("asistencia")
    
    override suspend fun registrarAsistencia(
        claseId: String,
        fecha: Date,
        estadosAsistencia: Map<String, Asistencia>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val registroId = UUID.randomUUID().toString()
            
            val registro = RegistroAsistencia(
                id = registroId,
                claseId = claseId,
                fecha = Timestamp(fecha),
                estadosAsistencia = estadosAsistencia
            )
            
            asistenciaCollection.document(registroId)
                .set(registro)
                .await()
                
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar asistencia")
            Result.Error(e)
        }
    }
    
    override suspend fun obtenerAsistencia(
        claseId: String,
        fecha: Date
    ): Result<Map<String, Asistencia>> = withContext(Dispatchers.IO) {
        try {
            val calendario = java.util.Calendar.getInstance()
            calendario.time = fecha
            calendario.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendario.set(java.util.Calendar.MINUTE, 0)
            calendario.set(java.util.Calendar.SECOND, 0)
            val inicioDelDia = calendario.time
            
            calendario.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendario.set(java.util.Calendar.MINUTE, 59)
            calendario.set(java.util.Calendar.SECOND, 59)
            val finDelDia = calendario.time
            
            val snapshot = asistenciaCollection
                .whereEqualTo("claseId", claseId)
                .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDelDia))
                .whereLessThanOrEqualTo("fecha", Timestamp(finDelDia))
                .get()
                .await()
                
            if (snapshot.isEmpty) {
                Result.Success(emptyMap())
            } else {
                val registro = snapshot.documents.first()
                    .toObject(RegistroAsistencia::class.java)
                    
                if (registro != null) {
                    Result.Success(registro.estadosAsistencia)
                } else {
                    Result.Success(emptyMap())
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener asistencia")
            Result.Error(e)
        }
    }
    
    override suspend fun guardarRegistroAsistencia(registroAsistencia: RegistroAsistencia): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                val registroId = if (registroAsistencia.id.isNotEmpty()) {
                    registroAsistencia.id
                } else {
                    UUID.randomUUID().toString()
                }
                
                // Comprobamos si ya existe un registro para esta clase y fecha
                val registro = obtenerRegistroAsistencia(
                    claseId = registroAsistencia.claseId,
                    fecha = registroAsistencia.fecha.toDate()
                )
                
                val idFinal = registro?.id ?: registroId
                
                asistenciaCollection.document(idFinal)
                    .set(registroAsistencia.copy(id = idFinal))
                    .await()
                    
                Result.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Error al guardar registro de asistencia")
                Result.Error(e)
            }
        }
    
    override suspend fun obtenerRegistroAsistencia(claseId: String, fecha: Date): RegistroAsistencia? = 
        withContext(Dispatchers.IO) {
            try {
                val calendario = java.util.Calendar.getInstance()
                calendario.time = fecha
                calendario.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendario.set(java.util.Calendar.MINUTE, 0)
                calendario.set(java.util.Calendar.SECOND, 0)
                val inicioDelDia = calendario.time
                
                calendario.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendario.set(java.util.Calendar.MINUTE, 59)
                calendario.set(java.util.Calendar.SECOND, 59)
                val finDelDia = calendario.time
                
                val snapshot = asistenciaCollection
                    .whereEqualTo("claseId", claseId)
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(inicioDelDia))
                    .whereLessThanOrEqualTo("fecha", Timestamp(finDelDia))
                    .get()
                    .await()
                    
                if (snapshot.isEmpty) {
                    null
                } else {
                    snapshot.documents.first().toObject(RegistroAsistencia::class.java)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener registro de asistencia")
                null
            }
        }
} 