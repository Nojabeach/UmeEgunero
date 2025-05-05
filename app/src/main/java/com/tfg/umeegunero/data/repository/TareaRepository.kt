package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.EntregaTarea
import com.tfg.umeegunero.data.model.EstadoTarea
import com.tfg.umeegunero.data.model.Tarea
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.util.FirestorePagination
import com.tfg.umeegunero.util.QueryFilter
import com.tfg.umeegunero.util.QueryOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.tfg.umeegunero.util.Result

/**
 * Repositorio para gestionar operaciones relacionadas con tareas en la aplicación UmeEgunero.
 *
 * Esta interfaz define los métodos para interactuar con las tareas, permitiendo
 * operaciones como creación, asignación, seguimiento y gestión de entregas.
 *
 * El repositorio maneja diferentes tipos de tareas para distintos roles de usuarios:
 * - Profesores pueden crear y asignar tareas
 * - Alumnos pueden ver y entregar tareas
 * - Familiares pueden hacer seguimiento de las tareas de sus hijos
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property storageRepository Repositorio para gestionar almacenamiento de archivos adjuntos
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class TareaRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_TAREAS = "tareas"
        private const val COLLECTION_ENTREGAS = "entregasTareas"
        private const val DEFAULT_PAGE_SIZE = 20
    }

    // Cache local de tareas
    private val _tareasLocal = MutableStateFlow<List<Tarea>>(emptyList())
    val tareasLocal: Flow<List<Tarea>> = _tareasLocal.asStateFlow()
    
    // Map de paginadores para diferentes consultas
    private val paginadores = mutableMapOf<String, FirestorePagination<Tarea>>()

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
     * Obtiene tareas asociadas a un profesor con paginación.
     * 
     * @param profesorId ID del profesor
     * @param pageNumber Número de página a cargar (empezando por 0)
     * @param pageSize Tamaño de página (elementos por página)
     * @return Resultado con las tareas de la página solicitada
     */
    suspend fun obtenerTareasPorProfesorPaginadas(
        profesorId: String, 
        pageNumber: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): Result<List<Tarea>> = withContext(Dispatchers.IO) {
        try {
            // Clave única para este paginador
            val paginadorKey = "profesor_$profesorId"
            
            // Obtener o crear el paginador para esta consulta
            val paginador = paginadores.getOrPut(paginadorKey) {
                FirestorePagination(
                    db = firestore,
                    collectionPath = COLLECTION_TAREAS,
                    pageSize = pageSize,
                    orderBy = "fechaCreacion",
                    descending = true
                ) { data -> 
                    Tarea(
                        id = data["id"] as String,
                        titulo = data["titulo"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        fechaCreacion = data["fechaCreacion"] as? Timestamp ?: Timestamp.now(),
                        fechaEntrega = data["fechaEntrega"] as? Timestamp,
                        profesorId = data["profesorId"] as? String ?: "",
                        profesorNombre = data["profesorNombre"] as? String ?: "",
                        estado = EstadoTarea.valueOf(data["estado"] as? String ?: EstadoTarea.PENDIENTE.name),
                        claseId = data["claseId"] as? String ?: "",
                        alumnoId = data["alumnoId"] as? String ?: "",
                        asignatura = data["asignatura"] as? String ?: "",
                        // Otros campos según sea necesario
                        revisadaPorFamiliar = data["revisadaPorFamiliar"] as? Boolean ?: false,
                        fechaRevision = data["fechaRevision"] as? Timestamp,
                        comentariosFamiliar = data["comentariosFamiliar"] as? String ?: ""
                    )
                }
            }
            
            // Configurar filtros para el profesor específico
            paginador.setFilters(listOf(
                QueryFilter("profesorId", QueryOperator.EQUAL, profesorId)
            ))
            
            // Cargar la página solicitada
            when (val result = paginador.loadPage(pageNumber)) {
                is Result.Success -> {
                    Timber.d("Página $pageNumber cargada: ${result.data.size} tareas")
                    return@withContext Result.Success(result.data)
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error cargando página $pageNumber")
                    return@withContext Result.Error(result.exception ?: Exception("Error desconocido"))
                }
                else -> {
                    return@withContext Result.Error(Exception("Resultado no esperado"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener tareas paginadas por profesor")
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
     * Obtiene tareas asociadas a una clase con paginación.
     * 
     * @param claseId ID de la clase
     * @param pageNumber Número de página a cargar (empezando por 0)
     * @param pageSize Tamaño de página (elementos por página)
     * @return Resultado con las tareas de la página solicitada
     */
    suspend fun obtenerTareasPorClasePaginadas(
        claseId: String, 
        pageNumber: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): Result<List<Tarea>> = withContext(Dispatchers.IO) {
        try {
            // Clave única para este paginador
            val paginadorKey = "clase_$claseId"
            
            // Obtener o crear el paginador
            val paginador = paginadores.getOrPut(paginadorKey) {
                FirestorePagination(
                    db = firestore,
                    collectionPath = COLLECTION_TAREAS,
                    pageSize = pageSize,
                    orderBy = "fechaCreacion",
                    descending = true
                ) { data -> 
                    Tarea(
                        id = data["id"] as String,
                        titulo = data["titulo"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        fechaCreacion = data["fechaCreacion"] as? Timestamp ?: Timestamp.now(),
                        fechaEntrega = data["fechaEntrega"] as? Timestamp,
                        profesorId = data["profesorId"] as? String ?: "",
                        profesorNombre = data["profesorNombre"] as? String ?: "",
                        estado = EstadoTarea.valueOf(data["estado"] as? String ?: EstadoTarea.PENDIENTE.name),
                        claseId = data["claseId"] as? String ?: "",
                        alumnoId = data["alumnoId"] as? String ?: "",
                        asignatura = data["asignatura"] as? String ?: "",
                        revisadaPorFamiliar = data["revisadaPorFamiliar"] as? Boolean ?: false
                    )
                }
            }
            
            // Configurar filtros
            paginador.setFilters(listOf(
                QueryFilter("claseId", QueryOperator.EQUAL, claseId)
            ))
            
            // Cargar la página solicitada
            when (val result = paginador.loadPage(pageNumber)) {
                is Result.Success -> {
                    return@withContext Result.Success(result.data)
                }
                is Result.Error -> {
                    return@withContext Result.Error(result.exception ?: Exception("Error desconocido"))
                }
                else -> {
                    return@withContext Result.Error(Exception("Resultado no esperado"))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener tareas paginadas por clase")
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
                        "comentarioProfesor" to feedback
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

    /**
     * Sincroniza las tareas desde Firestore y actualiza la caché local
     * @param usuarioId ID del usuario (alumno o profesor)
     * @param esProfesor Indica si el usuario es profesor
     */
    suspend fun actualizarTareasLocales(usuarioId: String, esProfesor: Boolean) {
        try {
            Timber.d("Sincronizando tareas para usuario $usuarioId (profesor: $esProfesor)")
            
            val tareasResult = if (esProfesor) {
                obtenerTareasPorProfesor(usuarioId)
            } else {
                obtenerTareasPorAlumno(usuarioId)
            }
            
            // Actualizar la caché local si el resultado es éxito
            when (tareasResult) {
                is Result.Success -> {
                    val tareas = tareasResult.data
                    _tareasLocal.value = tareas
                    Timber.d("Se sincronizaron ${tareas.size} tareas")
                }
                is Result.Error -> {
                    Timber.e(tareasResult.exception, "Error al obtener tareas: ${tareasResult.exception?.message}")
                }
                is Result.Loading -> {
                    Timber.d("Sincronización de tareas en progreso")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al sincronizar tareas")
        }
    }

    /**
     * Actualiza una entrega de tarea existente.
     *
     * @param tareaId ID de la tarea asociada a la entrega
     * @param entregaId ID de la entrega a actualizar
     * @param estado Nuevo estado de la entrega
     * @param comentarioCambio Comentario sobre el cambio realizado (ej. motivo de rechazo)
     * @param profesorRevisorId ID del profesor que revisa la entrega (opcional)
     * @param calificacion Calificación opcional para la entrega
     * @return Result<Unit> Resultado de la operación
     */
    /*
    suspend fun actualizarEntregaTarea(
        tareaId: String,
        entregaId: String,
        estado: EstadoEntrega,
        comentarioCambio: String?,
        profesorRevisorId: String?, // ID del profesor que revisa la entrega
        calificacion: Double?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Actualizando entrega $entregaId de la tarea $tareaId a estado: $estado")
            
            val entregaRef = firestore.collection(COLLECTION_ENTREGAS).document(entregaId)
            
            val updateData = hashMapOf<String, Any>(
                "estado" to estado.toString(),
                "fechaModificacion" to Timestamp.now()
            )
            
            // Añadir campos opcionales solo si no son nulos
            comentarioCambio?.let { updateData["comentarioCambio"] = it }
            profesorRevisorId?.let { updateData["profesorRevisorId"] = it }
            calificacion?.let { updateData["calificacion"] = it }
            
            entregaRef.update(updateData).await()
            
            Timber.d("Entrega actualizada correctamente")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar entrega $entregaId de tarea $tareaId")
            return@withContext Result.Error(e)
        }
    }
    */
} 