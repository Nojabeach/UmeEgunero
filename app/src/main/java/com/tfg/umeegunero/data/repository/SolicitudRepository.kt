package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.EstadoSolicitud
import com.tfg.umeegunero.data.model.SolicitudVinculacion
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las solicitudes de vinculación entre familiares y alumnos.
 * 
 * Este repositorio se encarga de todas las operaciones relacionadas con las solicitudes
 * de vinculación, como crear nuevas solicitudes, obtener solicitudes existentes,
 * y actualizar el estado de las solicitudes.
 * 
 * @property firestore Instancia de FirebaseFirestore para acceder a la base de datos
 */
@Singleton
class SolicitudRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_SOLICITUDES = "solicitudes_vinculacion"
    }
    
    /**
     * Crea una nueva solicitud de vinculación en la base de datos
     * 
     * @param solicitud Datos de la solicitud a crear
     * @return Resultado con la solicitud creada o error
     */
    suspend fun crearSolicitudVinculacion(solicitud: SolicitudVinculacion): Result<SolicitudVinculacion> {
        return try {
            val docRef = firestore.collection(COLLECTION_SOLICITUDES).document()
            val solicitudConId = solicitud.copy(id = docRef.id)
            
            docRef.set(solicitudConId).await()
            Result.Success(solicitudConId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las solicitudes de vinculación asociadas a un familiar
     * 
     * @param familiarId ID del familiar
     * @return Lista de solicitudes o error
     */
    suspend fun getSolicitudesByFamiliarId(familiarId: String): Result<List<SolicitudVinculacion>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("familiarId", familiarId)
                .get()
                .await()
            
            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SolicitudVinculacion::class.java)
            }
            
            Result.Success(solicitudes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene todas las solicitudes de vinculación pendientes para un centro
     * 
     * @param centroId ID del centro educativo
     * @return Lista de solicitudes pendientes o error
     */
    suspend fun getSolicitudesPendientesByCentroId(centroId: String): Result<List<SolicitudVinculacion>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("centroId", centroId)
                .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.name)
                .get()
                .await()
            
            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SolicitudVinculacion::class.java)
            }
            
            Result.Success(solicitudes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene el historial completo de solicitudes de vinculación para un centro
     * 
     * @param centroId ID del centro educativo
     * @return Lista de solicitudes procesadas o error
     */
    suspend fun getHistorialSolicitudesByCentroId(centroId: String): Result<List<SolicitudVinculacion>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SOLICITUDES)
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
            
            val solicitudes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SolicitudVinculacion::class.java)
            }.sortedByDescending { it.fechaSolicitud }
            
            Result.Success(solicitudes)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Actualiza el estado de una solicitud de vinculación
     * 
     * @param solicitudId ID de la solicitud
     * @param nuevoEstado Nuevo estado de la solicitud
     * @return Resultado de la operación
     */
    suspend fun actualizarEstadoSolicitud(solicitudId: String, nuevoEstado: String): Result<Boolean> {
        return try {
            firestore.collection(COLLECTION_SOLICITUDES)
                .document(solicitudId)
                .update("estado", nuevoEstado)
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Actualiza el estado de una solicitud de vinculación incluyendo el administrador que la procesó
     * 
     * @param solicitudId ID de la solicitud
     * @param nuevoEstado Nuevo estado de la solicitud
     * @param adminId ID del administrador que procesa la solicitud
     * @param nombreAdmin Nombre del administrador que procesa la solicitud
     * @param observaciones Observaciones adicionales sobre la solicitud (opcional)
     * @return Resultado de la operación
     */
    suspend fun procesarSolicitud(
        solicitudId: String, 
        nuevoEstado: EstadoSolicitud,
        adminId: String,
        nombreAdmin: String,
        observaciones: String = ""
    ): Result<Boolean> {
        return try {
            val actualizaciones = mutableMapOf<String, Any>(
                "estado" to nuevoEstado.name,
                "adminId" to adminId,
                "nombreAdmin" to nombreAdmin,
                "fechaProcesamiento" to Timestamp.now()
            )
            
            if (observaciones.isNotEmpty()) {
                actualizaciones["observaciones"] = observaciones
            }
            
            firestore.collection(COLLECTION_SOLICITUDES)
                .document(solicitudId)
                .update(actualizaciones)
                .await()
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
} 