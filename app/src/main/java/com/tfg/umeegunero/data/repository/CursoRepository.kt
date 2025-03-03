package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Curso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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
     * Obtiene los cursos de un centro específico
     */
    suspend fun getCursosByCentro(centroId: String): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val cursosSnapshot = cursosCollection
                .whereEqualTo("centroId", centroId)
                .whereEqualTo("activo", true)
                .get().await()

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
}