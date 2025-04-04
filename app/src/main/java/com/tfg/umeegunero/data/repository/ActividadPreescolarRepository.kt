package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.ActividadPreescolar
import com.tfg.umeegunero.data.model.EstadoActividad
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar actividades preescolares en Firestore
 */
@Singleton
class ActividadPreescolarRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_ACTIVIDADES = "actividades_preescolares"
    }

    /**
     * Obtiene todas las actividades asignadas a un profesor específico
     * @param profesorId ID del profesor
     * @return Resultado con la lista de actividades o error
     */
    suspend fun obtenerActividadesPorProfesor(profesorId: String): Result<List<ActividadPreescolar>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ACTIVIDADES)
                .whereEqualTo("profesorId", profesorId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val actividades = query.toObjects(ActividadPreescolar::class.java)
            
            return@withContext Result.Success(actividades)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener actividades por profesor")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las actividades de una clase específica
     * @param claseId ID de la clase
     * @return Resultado con la lista de actividades o error
     */
    suspend fun obtenerActividadesPorClase(claseId: String): Result<List<ActividadPreescolar>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ACTIVIDADES)
                .whereEqualTo("claseId", claseId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val actividades = query.toObjects(ActividadPreescolar::class.java)
            
            return@withContext Result.Success(actividades)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener actividades por clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las actividades asignadas a un alumno específico
     * @param alumnoId ID del alumno
     * @return Resultado con la lista de actividades o error
     */
    suspend fun obtenerActividadesPorAlumno(alumnoId: String): Result<List<ActividadPreescolar>> = withContext(Dispatchers.IO) {
        try {
            // Buscar actividades específicas del alumno
            val queryAlumno = firestore.collection(COLLECTION_ACTIVIDADES)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()

            val actividadesAlumno = queryAlumno.toObjects(ActividadPreescolar::class.java)
            
            // Buscar también actividades de su clase (que no tengan alumnoId específico)
            val claseId = obtenerClaseDelAlumno(alumnoId)
            
            if (claseId != null) {
                val queryClase = firestore.collection(COLLECTION_ACTIVIDADES)
                    .whereEqualTo("claseId", claseId)
                    .whereEqualTo("alumnoId", null)
                    .get()
                    .await()
                
                val actividadesClase = queryClase.toObjects(ActividadPreescolar::class.java)
                
                // Combinar ambas listas y ordenar por fecha de creación descendente
                return@withContext Result.Success(
                    (actividadesAlumno + actividadesClase).sortedByDescending { it.fechaCreacion.seconds }
                )
            }
            
            return@withContext Result.Success(actividadesAlumno)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener actividades por alumno")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene la clase a la que pertenece un alumno
     * @param alumnoId ID del alumno
     * @return ID de la clase o null si no se encuentra
     */
    private suspend fun obtenerClaseDelAlumno(alumnoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection("alumnos")
                .document(alumnoId)
                .get()
                .await()
            
            if (document.exists()) {
                return@withContext document.getString("aulaId")
            }
            
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clase del alumno")
            return@withContext null
        }
    }

    /**
     * Obtiene una actividad específica por su ID
     * @param actividadId ID de la actividad
     * @return Resultado con la actividad o null si no existe
     */
    suspend fun obtenerActividad(actividadId: String): Result<ActividadPreescolar?> = withContext(Dispatchers.IO) {
        try {
            val documento = firestore.collection(COLLECTION_ACTIVIDADES)
                .document(actividadId)
                .get()
                .await()

            if (documento.exists()) {
                val actividad = documento.toObject(ActividadPreescolar::class.java)
                return@withContext Result.Success(actividad)
            } else {
                return@withContext Result.Success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener actividad por ID")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea una nueva actividad preescolar
     * @param actividad Objeto ActividadPreescolar a crear
     * @return Resultado con el ID de la actividad creada o error
     */
    suspend fun crearActividad(actividad: ActividadPreescolar): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Generar ID aleatorio si no se proporciona uno
            val id = if (actividad.id.isEmpty()) UUID.randomUUID().toString() else actividad.id
            
            // Crear documento con ID generado
            val documentRef = firestore.collection(COLLECTION_ACTIVIDADES).document(id)
            
            // Crear copia de la actividad con el nuevo ID
            val actividadConId = actividad.copy(id = id)
            
            // Guardar en Firestore
            documentRef.set(actividadConId).await()
            
            return@withContext Result.Success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear actividad preescolar")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza una actividad existente
     * @param actividad Objeto ActividadPreescolar con los nuevos datos
     * @return Resultado con true si se actualizó correctamente
     */
    suspend fun actualizarActividad(actividad: ActividadPreescolar): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Verificar que la actividad tenga un ID válido
            if (actividad.id.isEmpty()) {
                return@withContext Result.Error(Exception("ID de actividad inválido"))
            }
            
            // Actualizar en Firestore
            firestore.collection(COLLECTION_ACTIVIDADES)
                .document(actividad.id)
                .set(actividad)
                .await()
            
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar actividad preescolar")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina una actividad
     * @param actividadId ID de la actividad a eliminar
     * @return Resultado con true si se eliminó correctamente
     */
    suspend fun eliminarActividad(actividadId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_ACTIVIDADES)
                .document(actividadId)
                .delete()
                .await()
            
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar actividad preescolar")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca una actividad como revisada por el familiar
     * @param actividadId ID de la actividad
     * @param comentario Comentario opcional del familiar
     * @return Resultado con true si se marcó correctamente
     */
    suspend fun marcarComoRevisada(actividadId: String, comentario: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Obtener referencia al documento
            val docRef = firestore.collection(COLLECTION_ACTIVIDADES).document(actividadId)
            
            // Actualizar solo los campos necesarios
            val updates = hashMapOf<String, Any>(
                "revisadaPorFamiliar" to true,
                "fechaRevision" to Timestamp.now()
            )
            
            // Añadir comentario si no está vacío
            if (comentario.isNotEmpty()) {
                updates["comentariosFamiliar"] = comentario
            }
            
            // Aplicar actualización
            docRef.update(updates).await()
            
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar actividad como revisada")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca una actividad como realizada
     * @param actividadId ID de la actividad
     * @param comentario Comentario opcional del profesor
     * @return Resultado con true si se marcó correctamente
     */
    suspend fun marcarComoRealizada(actividadId: String, comentario: String = ""): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Obtener referencia al documento
            val docRef = firestore.collection(COLLECTION_ACTIVIDADES).document(actividadId)
            
            // Actualizar solo los campos necesarios
            val updates = hashMapOf<String, Any>(
                "estado" to EstadoActividad.REALIZADA
            )
            
            // Añadir comentario si no está vacío
            if (comentario.isNotEmpty()) {
                updates["comentariosProfesor"] = comentario
            }
            
            // Aplicar actualización
            docRef.update(updates).await()
            
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar actividad como realizada")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene las actividades más recientes para un profesor
     * @param profesorId ID del profesor
     * @param limite Número máximo de actividades a retornar
     * @return Resultado con la lista de actividades o error
     */
    suspend fun obtenerActividadesRecientes(profesorId: String, limite: Int = 10): Result<List<ActividadPreescolar>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ACTIVIDADES)
                .whereEqualTo("profesorId", profesorId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()

            val actividades = query.toObjects(ActividadPreescolar::class.java)
            
            return@withContext Result.Success(actividades)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener actividades recientes")
            return@withContext Result.Error(e)
        }
    }
} 