package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Curso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val clasesCollection = firestore.collection("clases")

    /**
     * Obtiene todas las clases
     */
    suspend fun getAllClases(): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            val clasesSnapshot = clasesCollection.get().await()
            val clases = clasesSnapshot.toObjects(Clase::class.java)
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene las clases de un centro específico
     */
    suspend fun getClasesByCentro(centroId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            val clasesSnapshot = clasesCollection
                .whereEqualTo("centroId", centroId)
                .whereEqualTo("activo", true)
                .get().await()

            val clases = clasesSnapshot.toObjects(Clase::class.java)
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene las clases de un curso específico
     */
    suspend fun getClasesByCurso(cursoId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            val clasesSnapshot = clasesCollection
                .whereEqualTo("cursoId", cursoId)
                .whereEqualTo("activo", true)
                .get().await()

            val clases = clasesSnapshot.toObjects(Clase::class.java)
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene una clase por su ID
     */
    suspend fun getClaseById(claseId: String): Result<Clase> = withContext(Dispatchers.IO) {
        try {
            val claseDoc = clasesCollection.document(claseId).get().await()

            if (claseDoc.exists()) {
                val clase = claseDoc.toObject(Clase::class.java)
                return@withContext Result.Success(clase!!)
            } else {
                throw Exception("Clase no encontrada")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda una clase
     */
    suspend fun saveClase(clase: Clase): Result<String> = withContext(Dispatchers.IO) {
        try {
            val claseId = if (clase.id.isBlank()) {
                clasesCollection.document().id
            } else {
                clase.id
            }

            val claseWithId = if (clase.id.isBlank()) clase.copy(id = claseId) else clase

            clasesCollection.document(claseId).set(claseWithId).await()

            return@withContext Result.Success(claseId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina una clase
     */
    suspend fun deleteClase(claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            clasesCollection.document(claseId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Desactiva una clase
     */
    suspend fun deactivateClase(claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            clasesCollection.document(claseId).update("activo", false).await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
}