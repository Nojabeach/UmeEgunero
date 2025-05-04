package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.IncidenciaEntity
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las incidencias de alumnos.
 * 
 * Este repositorio permite a los profesores crear, consultar y gestionar incidencias
 * relacionadas con los alumnos.
 */
@Singleton
class IncidenciaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_INCIDENCIAS = "incidencias"
    }
    
    /**
     * Crea una nueva incidencia en la base de datos
     * 
     * @param incidencia Datos de la incidencia a crear
     * @return true si la operación fue exitosa, false en caso contrario
     */
    suspend fun createIncidencia(incidencia: IncidenciaEntity): Boolean {
        return try {
            // Si la incidencia no tiene un ID, generamos uno
            val incidenciaId = incidencia.id.ifEmpty { 
                firestore.collection(COLLECTION_INCIDENCIAS).document().id 
            }
            
            // Aseguramos que la incidencia tenga un ID
            val incidenciaToSave = if (incidencia.id.isEmpty()) {
                incidencia.copy(id = incidenciaId)
            } else {
                incidencia
            }
            
            // Guardamos la incidencia en Firestore
            firestore.collection(COLLECTION_INCIDENCIAS)
                .document(incidenciaToSave.id)
                .set(incidenciaToSave)
                .await()
                
            Timber.d("Incidencia creada con éxito: ${incidenciaToSave.id}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al crear incidencia")
            false
        }
    }
    
    /**
     * Obtiene todas las incidencias asociadas a un alumno
     * 
     * @param alumnoId ID del alumno
     * @return Lista de incidencias del alumno
     */
    suspend fun getIncidenciasByAlumnoId(alumnoId: String): List<IncidenciaEntity> {
        return try {
            val snapshot = firestore.collection(COLLECTION_INCIDENCIAS)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()
                
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(IncidenciaEntity::class.java)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener incidencias del alumno $alumnoId")
            emptyList()
        }
    }
    
    /**
     * Obtiene todas las incidencias creadas por un profesor
     * 
     * @param profesorId ID del profesor
     * @return Lista de incidencias creadas por el profesor
     */
    suspend fun getIncidenciasByProfesorId(profesorId: String): List<IncidenciaEntity> {
        return try {
            val snapshot = firestore.collection(COLLECTION_INCIDENCIAS)
                .whereEqualTo("profesorId", profesorId)
                .get()
                .await()
                
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(IncidenciaEntity::class.java)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener incidencias del profesor $profesorId")
            emptyList()
        }
    }
    
    /**
     * Actualiza el estado de una incidencia
     * 
     * @param incidenciaId ID de la incidencia
     * @param nuevoEstado Nuevo estado de la incidencia
     * @return true si la operación fue exitosa, false en caso contrario
     */
    suspend fun actualizarEstadoIncidencia(incidenciaId: String, nuevoEstado: String): Boolean {
        return try {
            firestore.collection(COLLECTION_INCIDENCIAS)
                .document(incidenciaId)
                .update("estado", nuevoEstado)
                .await()
                
            Timber.d("Estado de incidencia $incidenciaId actualizado a $nuevoEstado")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar estado de incidencia $incidenciaId")
            false
        }
    }
    
    /**
     * Obtiene una incidencia por su ID
     * 
     * @param incidenciaId ID de la incidencia
     * @return La incidencia o null si no existe
     */
    suspend fun getIncidenciaById(incidenciaId: String): IncidenciaEntity? {
        return try {
            val doc = firestore.collection(COLLECTION_INCIDENCIAS)
                .document(incidenciaId)
                .get()
                .await()
                
            doc.toObject(IncidenciaEntity::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener incidencia $incidenciaId")
            null
        }
    }
} 