package com.tfg.umeegunero.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.tfg.umeegunero.util.Result

/**
 * Repositorio para gestionar los datos de los profesores
 *
 * @property firestore Instancia de Firestore para acceder a la base de datos
 */
@Singleton
class ProfesorRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val alumnoRepository: AlumnoRepository,
    private val claseRepository: ClaseRepository
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
    
    /**
     * Elimina un profesor completamente del sistema
     *
     * @param profesorId ID del profesor a eliminar
     * @param actualizarReferencias Si es true, actualiza referencias en clases y alumnos
     * @return Resultado de la operación
     */
    suspend fun eliminarProfesor(profesorId: String, actualizarReferencias: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Verificar si el profesor existe
            val profesorDoc = profesoresCollection.document(profesorId).get().await()
            
            if (!profesorDoc.exists()) {
                return@withContext Result.Error(Exception("El profesor con ID $profesorId no existe"))
            }
            
            // Obtener datos del profesor para referencias posteriores
            val profesor = Profesor(
                id = profesorDoc.id,
                usuarioId = profesorDoc.getString("usuarioId") ?: "",
                nombre = profesorDoc.getString("nombre") ?: "",
                apellidos = profesorDoc.getString("apellidos") ?: "",
                claseId = profesorDoc.getString("claseId") ?: "",
                centroId = profesorDoc.getString("centroId") ?: ""
            )
            
            // 2. Si tenemos que actualizar referencias, lo hacemos primero
            if (actualizarReferencias) {
                // 2.1 Eliminar profesor de las clases donde está asignado
                if (profesor.claseId.isNotEmpty()) {
                    try {
                        claseRepository.desasignarProfesor(profesor.claseId)
                        Timber.d("Profesor desasignado de clase: ${profesor.claseId}")
                    } catch (e: Exception) {
                        Timber.e(e, "Error al desasignar profesor de clase ${profesor.claseId}")
                    }
                }
                
                // 2.2 Eliminar profesor de clases donde es auxiliar
                try {
                    // Buscar todas las clases asignadas a este profesor
                    when (val clasesResult = claseRepository.getClasesByProfesor(profesorId)) {
                        is Result.Success -> {
                            clasesResult.data.forEach { clase ->
                                try {
                                    // Desasignar como profesor auxiliar
                                    claseRepository.desasignarProfesorDeClase(profesorId, clase.id)
                                    Timber.d("Profesor desasignado como auxiliar de clase: ${clase.id}")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al desasignar profesor auxiliar de clase ${clase.id}")
                                }
                            }
                        }
                        else -> Timber.w("No se pudieron obtener las clases del profesor $profesorId")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al desasignar profesor de clases auxiliares")
                }
                
                // 2.3 Actualizar alumnos que tienen este profesor asignado
                try {
                    val alumnos = alumnoRepository.getAlumnosForProfesor(profesorId)
                    Timber.d("Eliminando profesor de ${alumnos.size} alumnos")
                    
                    alumnos.forEach { alumno ->
                        try {
                            alumnoRepository.eliminarProfesor(alumno.dni)
                            Timber.d("Profesor eliminado del alumno: ${alumno.dni}")
                        } catch (e: Exception) {
                            Timber.e(e, "Error al eliminar profesor del alumno ${alumno.dni}")
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al actualizar alumnos del profesor")
                }
            }
            
            // 3. Finalmente, eliminar el documento del profesor
            profesoresCollection.document(profesorId).delete().await()
            Timber.d("Profesor con ID $profesorId eliminado correctamente")
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar profesor con ID $profesorId: ${e.message}")
            return@withContext Result.Error(e)
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