package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.Result
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.model.TipoUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las tareas en Firestore
 */
@Singleton
class TareaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_TAREAS = "tareas"
        private const val COLLECTION_ENTREGAS = "entregasTareas"
    }

    /**
     * Obtiene todas las tareas asociadas a un profesor
     * @param profesorId ID del profesor
     * @return Resultado con la lista de tareas
     */
    suspend fun obtenerTareasPorProfesor(profesorId: String): Result<List<Tarea>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_TAREAS)
                .whereEqualTo("profesorId", profesorId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val tareas = query.toObjects(Tarea::class.java)
            return@withContext Result.Success(tareas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener tareas por profesor")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las tareas asociadas a una clase
     * @param claseId ID de la clase
     * @return Resultado con la lista de tareas
     */
    suspend fun obtenerTareasPorClase(claseId: String): Result<List<Tarea>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_TAREAS)
                .whereEqualTo("claseId", claseId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val tareas = query.toObjects(Tarea::class.java)
            return@withContext Result.Success(tareas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener tareas por clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene una tarea específica por su ID
     * @param tareaId ID de la tarea
     * @return Resultado con la tarea o null si no existe
     */
    suspend fun obtenerTarea(tareaId: String): Result<Tarea?> = withContext(Dispatchers.IO) {
        try {
            val documento = firestore.collection(COLLECTION_TAREAS)
                .document(tareaId)
                .get()
                .await()

            if (documento.exists()) {
                val tarea = documento.toObject(Tarea::class.java)
                return@withContext Result.Success(tarea)
            } else {
                return@withContext Result.Success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener tarea por ID")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea una nueva tarea
     * @param tarea Objeto Tarea a crear
     * @return Resultado con el ID de la tarea creada
     */
    suspend fun crearTarea(tarea: Tarea): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tareaId = if (tarea.id.isNotEmpty()) tarea.id else UUID.randomUUID().toString()
            val nuevaTarea = tarea.copy(id = tareaId)

            firestore.collection(COLLECTION_TAREAS)
                .document(tareaId)
                .set(nuevaTarea)
                .await()

            return@withContext Result.Success(tareaId)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear tarea")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza una tarea existente
     * @param tarea Objeto Tarea con los nuevos datos
     * @return Resultado con true si se actualizó correctamente
     */
    suspend fun actualizarTarea(tarea: Tarea): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (tarea.id.isEmpty()) {
                return@withContext Result.Error(IllegalArgumentException("La tarea debe tener un ID"))
            }

            firestore.collection(COLLECTION_TAREAS)
                .document(tarea.id)
                .set(tarea)
                .await()

            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar tarea")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina una tarea
     * @param tareaId ID de la tarea a eliminar
     * @return Resultado con true si se eliminó correctamente
     */
    suspend fun eliminarTarea(tareaId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Primero eliminamos todas las entregas asociadas a esta tarea
            val entregasQuery = firestore.collection(COLLECTION_ENTREGAS)
                .whereEqualTo("tareaId", tareaId)
                .get()
                .await()

            // Eliminamos cada entrega en una transacción
            val batch = firestore.batch()
            entregasQuery.documents.forEach { doc ->
                batch.delete(firestore.collection(COLLECTION_ENTREGAS).document(doc.id))
            }

            // Eliminamos la tarea
            batch.delete(firestore.collection(COLLECTION_TAREAS).document(tareaId))
            
            // Ejecutamos todas las operaciones juntas
            batch.commit().await()

            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar tarea")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las entregas de una tarea
     * @param tareaId ID de la tarea
     * @return Resultado con la lista de entregas
     */
    suspend fun obtenerEntregasPorTarea(tareaId: String): Result<List<EntregaTarea>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ENTREGAS)
                .whereEqualTo("tareaId", tareaId)
                .orderBy("fechaEntrega", Query.Direction.DESCENDING)
                .get()
                .await()

            val entregas = query.toObjects(EntregaTarea::class.java)
            return@withContext Result.Success(entregas)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener entregas por tarea")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene la entrega de un alumno para una tarea específica
     * @param tareaId ID de la tarea
     * @param alumnoId ID del alumno
     * @return Resultado con la entrega o null si no existe
     */
    suspend fun obtenerEntregaAlumno(tareaId: String, alumnoId: String): Result<EntregaTarea?> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ENTREGAS)
                .whereEqualTo("tareaId", tareaId)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                val entrega = query.documents.first().toObject(EntregaTarea::class.java)
                return@withContext Result.Success(entrega)
            } else {
                return@withContext Result.Success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener entrega de alumno")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda una entrega de tarea
     * @param entrega Objeto EntregaTarea a guardar
     * @return Resultado con el ID de la entrega
     */
    suspend fun guardarEntrega(entrega: EntregaTarea): Result<String> = withContext(Dispatchers.IO) {
        try {
            val entregaId = if (entrega.id.isNotEmpty()) entrega.id else UUID.randomUUID().toString()
            val nuevaEntrega = entrega.copy(id = entregaId)

            firestore.collection(COLLECTION_ENTREGAS)
                .document(entregaId)
                .set(nuevaEntrega)
                .await()

            return@withContext Result.Success(entregaId)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar entrega")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Califica una entrega de tarea
     * @param entregaId ID de la entrega
     * @param calificacion Calificación asignada
     * @param feedback Comentario del profesor
     * @return Resultado con true si se calificó correctamente
     */
    suspend fun calificarEntrega(
        entregaId: String,
        calificacion: Double,
        feedback: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION_ENTREGAS)
                .document(entregaId)
                .update(
                    mapOf(
                        "calificacion" to calificacion,
                        "feedbackProfesor" to feedback
                    )
                )
                .await()

            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al calificar entrega")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las tareas asociadas a un alumno
     * @param alumnoId ID del alumno
     * @return Resultado con la lista de tareas
     */
    suspend fun obtenerTareasPorAlumno(alumnoId: String): Result<List<Tarea>> = withContext(Dispatchers.IO) {
        try {
            // Primero buscamos tareas específicas del alumno
            val queryAlumno = firestore.collection(COLLECTION_TAREAS)
                .whereEqualTo("alumnoId", alumnoId)
                .get()
                .await()
                
            val tareasAlumno = queryAlumno.toObjects(Tarea::class.java)
            
            // Luego buscamos tareas de la clase del alumno
            val alumnoDoc = firestore.collection("alumnos")
                .document(alumnoId)
                .get()
                .await()
            
            if (alumnoDoc.exists()) {
                val claseId = alumnoDoc.getString("aulaId")
                
                if (claseId != null) {
                    val queryClase = firestore.collection(COLLECTION_TAREAS)
                        .whereEqualTo("claseId", claseId)
                        .whereEqualTo("alumnoId", "")  // Tareas para toda la clase (sin alumno específico)
                        .get()
                        .await()
                    
                    val tareasClase = queryClase.toObjects(Tarea::class.java)
                    
                    // Combinar ambas listas y ordenar por fecha de creación descendente
                    return@withContext Result.Success(
                        (tareasAlumno + tareasClase).sortedByDescending { it.fechaCreacion.seconds }
                    )
                }
            }
            
            // Si no pudimos obtener la clase, devolvemos solo las tareas específicas del alumno
            return@withContext Result.Success(tareasAlumno)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener tareas por alumno")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las tareas asociadas a un alumno - Versión compatible con el nuevo ViewModel
     * @param alumnoId ID del alumno
     * @return Resultado con la lista de tareas
     */
    suspend fun getTareasByAlumnoId(alumnoId: String): Result<List<Tarea>> {
        return obtenerTareasPorAlumno(alumnoId)
    }

    /**
     * Actualiza el estado de una tarea existente
     * @param tareaId ID de la tarea a actualizar
     * @param nuevoEstado Nuevo estado de la tarea
     * @return Resultado con true si se actualizó correctamente
     */
    suspend fun actualizarEstadoTarea(tareaId: String, nuevoEstado: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (tareaId.isEmpty()) {
                return@withContext Result.Error(IllegalArgumentException("La tarea debe tener un ID"))
            }

            firestore.collection(COLLECTION_TAREAS)
                .document(tareaId)
                .update("estado", nuevoEstado)
                .await()

            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar estado de tarea")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca una tarea como revisada por un familiar
     * @param tareaId ID de la tarea
     * @param familiarId ID del familiar que revisa la tarea
     * @param comentario Comentario opcional del familiar
     * @return Resultado con true si se actualizó correctamente
     */
    suspend fun marcarTareaComoRevisadaPorFamiliar(
        tareaId: String,
        familiarId: String,
        comentario: String = ""
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (tareaId.isEmpty() || familiarId.isEmpty()) {
                return@withContext Result.Error(IllegalArgumentException("Se requieren tareaId y familiarId"))
            }

            // Actualizar los campos relacionados con la revisión del familiar
            firestore.collection(COLLECTION_TAREAS)
                .document(tareaId)
                .update(
                    mapOf(
                        "revisadaPorFamiliar" to true,
                        "familiarRevisorId" to familiarId,
                        "fechaRevision" to Timestamp.now(),
                        "comentariosFamiliar" to comentario
                    )
                )
                .await()

            return@withContext Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al marcar tarea como revisada por familiar")
            return@withContext Result.Error(e)
        }
    }
} 