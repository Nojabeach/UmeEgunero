package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Repositorio para la gestión de cursos académicos
 */
@Singleton
class CursoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val cursosCollection = firestore.collection("cursos")

    /**
     * Obtiene todos los cursos
     */
    suspend fun getAllCursos(): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val cursosSnapshot = cursosCollection.get().await()
            val cursos = cursosSnapshot.toObjects(Curso::class.java)
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un curso por su ID
     */
    suspend fun getCursoById(cursoId: String): Result<Curso> = withContext(Dispatchers.IO) {
        try {
            val cursoDoc = cursosCollection.document(cursoId).get().await()

            if (cursoDoc.exists()) {
                val curso = cursoDoc.toObject(Curso::class.java)
                return@withContext Result.Success(curso!!)
            } else {
                throw Exception("Curso no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un curso
     */
    suspend fun saveCurso(curso: Curso): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cursoId = if (curso.id.isBlank()) {
                cursosCollection.document().id
            } else {
                curso.id
            }

            val cursoWithId = if (curso.id.isBlank()) curso.copy(id = cursoId) else curso

            cursosCollection.document(cursoId).set(cursoWithId).await()

            return@withContext Result.Success(cursoId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un curso
     */
    suspend fun deleteCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cursosCollection.document(cursoId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Desactiva un curso
     */
    suspend fun deactivateCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cursosCollection.document(cursoId).update("activo", false).await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las clases asociadas a un curso
     */
    suspend fun obtenerClasesPorCurso(cursoId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo clases para el curso: $cursoId")
            val snapshot = firestore.collection("clases")
                .whereEqualTo("cursoId", cursoId)
                .get()
                .await()
            
            val clases = snapshot.documents.mapNotNull { doc ->
                val clase = doc.toObject(Clase::class.java)
                clase?.copy(id = doc.id)
            }
            
            Timber.d("Obtenidas ${clases.size} clases para el curso $cursoId")
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases para el curso $cursoId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Añade un nuevo curso
     */
    suspend fun agregarCurso(curso: Curso): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cursoId = if (curso.id.isBlank()) cursosCollection.document().id else curso.id
            val cursoWithId = if (curso.id.isBlank()) curso.copy(id = cursoId) else curso

            cursosCollection.document(cursoId).set(cursoWithId).await()
            return@withContext Result.Success(cursoId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Modifica un curso existente
     */
    suspend fun modificarCurso(curso: Curso): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el curso existe
            val cursoDoc = cursosCollection.document(curso.id).get().await()

            if (!cursoDoc.exists()) {
                return@withContext Result.Error(Exception("El curso no existe"))
            }

            // Actualizar el curso
            cursosCollection.document(curso.id).set(curso).await()
            return@withContext Result.Success(curso.id)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Borra un curso
     */
    suspend fun borrarCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cursosCollection.document(cursoId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Asigna un alumno a una clase
     */
    suspend fun asignarAlumnoAClase(alumnoId: String, claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val claseDoc = firestore.collection("clases").document(claseId).get().await()
            
            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase no existe"))
            }
            
            val clase = claseDoc.toObject(Clase::class.java)
            val alumnosActuales = clase?.alumnosIds ?: mutableListOf<String>()
            val nuevosAlumnos = alumnosActuales.toMutableList()
            
            if (!nuevosAlumnos.contains(alumnoId)) {
                nuevosAlumnos.add(alumnoId)
                
                firestore.collection("clases").document(claseId)
                    .update("alumnosIds", nuevosAlumnos)
                    .await()
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar alumno a clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un curso en Firestore
     * @param curso El curso a guardar
     */
    suspend fun guardarCurso(curso: Curso) {
        try {
            cursosCollection.document(curso.id).set(curso).await()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al guardar el curso: ${e.message}")
        }
    }
    
    /**
     * Obtiene un curso por su ID
     * @param cursoId ID del curso
     * @return El curso encontrado
     */
    suspend fun obtenerCursoPorId(cursoId: String): Curso? {
        return try {
            val document = cursosCollection.document(cursoId).get().await()
            document.toObject(Curso::class.java)
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al obtener el curso: ${e.message}")
        }
    }
    
    /**
     * Obtiene todos los cursos de un centro educativo
     * @param centroId ID del centro educativo
     * @param soloActivos Si true, solo devuelve cursos activos
     * @return Lista de cursos del centro
     */
    suspend fun obtenerCursosPorCentro(centroId: String, soloActivos: Boolean = false): List<Curso> {
        return try {
            Timber.d("Consultando cursos para centro: $centroId (soloActivos: $soloActivos)")
            
            val query = cursosCollection
                .whereEqualTo("centroId", centroId)
            
            // Aplicamos el filtro de activos si se solicita
            val queryFinal = if (soloActivos) {
                query.whereEqualTo("activo", true)
            } else {
                query
            }
            
            val querySnapshot = queryFinal
                .orderBy("nombre", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val cursos = querySnapshot.documents.mapNotNull { it.toObject(Curso::class.java) }
            
            Timber.d("Se encontraron ${cursos.size} cursos para el centro $centroId")
            cursos.forEach { curso ->
                Timber.d("  - Curso encontrado: ${curso.id} - ${curso.nombre} (activo: ${curso.activo})")
            }
            
            cursos
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Error al obtener los cursos: ${e.message}")
            throw Exception("Error al obtener los cursos: ${e.message}")
        }
    }
    
    /**
     * Obtiene todos los cursos de un centro educativo con Result
     * @param centroId ID del centro educativo
     * @param soloActivos Si true, solo devuelve cursos activos
     * @return Result con la lista de cursos del centro
     */
    suspend fun obtenerCursosPorCentroResult(centroId: String, soloActivos: Boolean = false): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Consultando cursos para centro: $centroId (soloActivos: $soloActivos)")
            
            val cursos = obtenerCursosPorCentro(centroId, soloActivos)
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener cursos por centro: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Actualiza un curso existente
     * @param curso El curso con los datos actualizados
     */
    suspend fun actualizarCurso(curso: Curso) {
        try {
            cursosCollection.document(curso.id).set(curso).await()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al actualizar el curso: ${e.message}")
        }
    }
    
    /**
     * Actualiza el estado activo de un curso
     * @param cursoId ID del curso
     * @param activo Nuevo estado activo
     */
    suspend fun actualizarEstadoActivo(cursoId: String, activo: Boolean) {
        try {
            cursosCollection.document(cursoId).update("activo", activo).await()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al actualizar el estado del curso: ${e.message}")
        }
    }

    /**
     * Obtiene todos los cursos
     */
    suspend fun getCursos(): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = cursosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
                
            val cursos = snapshot.documents.mapNotNull { doc ->
                val curso = doc.toObject(Curso::class.java)
                curso?.copy(id = doc.id)
            }
            
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener cursos: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
}