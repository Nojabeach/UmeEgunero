package com.tfg.umeegunero.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los datos de los profesores
 *
 * @property firestore Instancia de Firestore para acceder a la base de datos
 */
@Singleton
class ProfesorRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val profesoresCollection = firestore.collection("profesores")
    
    /**
     * Obtiene un profesor por su ID de usuario
     *
     * @param usuarioId ID del usuario asociado al profesor
     * @return El profesor si existe, null en caso contrario
     */
    suspend fun getProfesorPorUsuarioId(usuarioId: String): Profesor? {
        return try {
            val querySnapshot = profesoresCollection
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                Timber.d("No se encontró profesor con usuarioId: $usuarioId")
                null
            } else {
                val documento = querySnapshot.documents.first()
                Profesor(
                    id = documento.id,
                    usuarioId = documento.getString("usuarioId") ?: "",
                    nombre = documento.getString("nombre") ?: "",
                    apellidos = documento.getString("apellidos") ?: "",
                    claseId = documento.getString("claseId") ?: "",
                    centroId = documento.getString("centroId") ?: "",
                    especialidad = documento.getString("especialidad") ?: "",
                    activo = documento.getBoolean("activo") ?: true
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesor por usuarioId: $usuarioId")
            null
        }
    }
    
    /**
     * Obtiene un profesor por su ID
     *
     * @param profesorId ID del profesor
     * @return El profesor si existe, null en caso contrario
     */
    suspend fun getProfesorPorId(profesorId: String): Profesor? {
        return try {
            val documento = profesoresCollection.document(profesorId).get().await()
            
            if (!documento.exists()) {
                Timber.d("No se encontró profesor con ID: $profesorId")
                null
            } else {
                Profesor(
                    id = documento.id,
                    usuarioId = documento.getString("usuarioId") ?: "",
                    nombre = documento.getString("nombre") ?: "",
                    apellidos = documento.getString("apellidos") ?: "",
                    claseId = documento.getString("claseId") ?: "",
                    centroId = documento.getString("centroId") ?: "",
                    especialidad = documento.getString("especialidad") ?: "",
                    activo = documento.getBoolean("activo") ?: true
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesor por ID: $profesorId")
            null
        }
    }
    
    /**
     * Obtiene la lista de profesores por centro
     *
     * @param centroId ID del centro
     * @return Lista de profesores del centro
     */
    suspend fun getProfesoresPorCentro(centroId: String): List<Profesor> {
        return try {
            val querySnapshot = profesoresCollection
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
            
            querySnapshot.documents.mapNotNull { documento ->
                Profesor(
                    id = documento.id,
                    usuarioId = documento.getString("usuarioId") ?: "",
                    nombre = documento.getString("nombre") ?: "",
                    apellidos = documento.getString("apellidos") ?: "",
                    claseId = documento.getString("claseId") ?: "",
                    centroId = documento.getString("centroId") ?: "",
                    especialidad = documento.getString("especialidad") ?: "",
                    activo = documento.getBoolean("activo") ?: true
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores por centro: $centroId")
            emptyList()
        }
    }
}

/**
 * Modelo de datos para un profesor
 *
 * @property id ID único del profesor
 * @property usuarioId ID del usuario asociado
 * @property nombre Nombre del profesor
 * @property apellidos Apellidos del profesor
 * @property claseId ID de la clase asignada
 * @property centroId ID del centro educativo
 * @property especialidad Especialidad del profesor
 * @property activo Indica si el profesor está activo
 */
data class Profesor(
    val id: String = "",
    val usuarioId: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val claseId: String = "",
    val centroId: String = "",
    val especialidad: String = "",
    val activo: Boolean = true
) 