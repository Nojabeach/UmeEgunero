package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar el registro de actividades de los usuarios en la aplicación.
 * Permite registrar acciones como inicio de sesión, cambios de contraseña, etc.
 */
@Singleton
class ActividadRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ACTIVIDADES = "actividades"
    }
    
    /**
     * Registra una actividad realizada por un usuario
     * 
     * @param tipo Tipo de actividad (LOGIN, CAMBIO_CONTRASEÑA, etc.)
     * @param descripcion Descripción de la actividad
     * @param usuarioId ID del usuario que realizó la actividad
     * @param detalles Detalles adicionales (opcional)
     * @return true si el registro fue exitoso, false en caso contrario
     */
    suspend fun registrarActividad(
        tipo: String,
        descripcion: String,
        usuarioId: String,
        detalles: String = ""
    ): Boolean {
        return try {
            val actividadId = UUID.randomUUID().toString()
            
            val actividad = hashMapOf(
                "id" to actividadId,
                "tipo" to tipo,
                "descripcion" to descripcion,
                "usuarioId" to usuarioId,
                "detalles" to detalles,
                "fecha" to Timestamp.now(),
                "fechaRegistro" to Timestamp.now()
            )
            
            firestore.collection(COLLECTION_ACTIVIDADES)
                .document(actividadId)
                .set(actividad)
                .await()
            
            Timber.d("Actividad registrada: $tipo para usuario $usuarioId")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error al registrar actividad: $tipo para usuario $usuarioId")
            false
        }
    }
    
    /**
     * Obtiene las actividades recientes de un usuario
     * 
     * @param usuarioId ID del usuario
     * @param limit Número máximo de actividades a obtener
     * @return Lista de actividades ordenadas por fecha (más recientes primero)
     */
    suspend fun obtenerActividadesUsuario(usuarioId: String, limit: Int = 10): List<Map<String, Any>> {
        return try {
            val query = firestore.collection(COLLECTION_ACTIVIDADES)
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            query.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener actividades del usuario $usuarioId")
            emptyList()
        }
    }
} 