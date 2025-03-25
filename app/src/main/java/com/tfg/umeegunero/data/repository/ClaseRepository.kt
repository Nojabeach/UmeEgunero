package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.Clase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las clases en Firestore
 */
@Singleton
class ClaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val clasesCollection = firestore.collection("clases")
    
    /**
     * Obtiene todas las clases asociadas a un curso
     */
    suspend fun getClasesByCursoId(cursoId: String): Result<List<Clase>> {
        return try {
            Timber.d("Obteniendo clases para el curso: $cursoId")
            val snapshot = clasesCollection
                .whereEqualTo("cursoId", cursoId)
                .get()
                .await()
            
            val clases = snapshot.documents.mapNotNull { doc ->
                val clase = doc.toObject<Clase>()
                clase?.copy(id = doc.id)
            }
            
            Timber.d("Obtenidas ${clases.size} clases para el curso $cursoId")
            Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases para el curso $cursoId")
            Result.Error(e)
        }
    }
    
    /**
     * Guarda una clase en Firestore
     */
    suspend fun guardarClase(clase: Clase): Result<String> {
        return try {
            val docRef = if (clase.id.isBlank()) {
                val newDocRef = clasesCollection.document()
                newDocRef.set(clase).await()
                newDocRef
            } else {
                val existingDocRef = clasesCollection.document(clase.id)
                existingDocRef.set(clase).await()
                existingDocRef
            }
            
            Timber.d("Clase guardada con ID: ${docRef.id}")
            Result.Success(docRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar clase")
            Result.Error(e)
        }
    }
    
    /**
     * Elimina una clase de Firestore
     */
    suspend fun eliminarClase(claseId: String): Result<Boolean> {
        return try {
            clasesCollection.document(claseId).delete().await()
            Timber.d("Clase eliminada con ID: $claseId")
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar clase $claseId")
            Result.Error(e)
        }
    }
    
    /**
     * Obtiene una clase por su ID
     */
    suspend fun getClaseById(claseId: String): Result<Clase> {
        return try {
            val docSnapshot = clasesCollection.document(claseId).get().await()
            if (docSnapshot.exists()) {
                val clase = docSnapshot.toObject<Clase>()?.copy(id = docSnapshot.id)
                if (clase != null) {
                    Timber.d("Clase obtenida: $claseId")
                    Result.Success(clase)
                } else {
                    val error = Exception("La clase existe pero no se pudo convertir")
                    Timber.e(error, "Error al obtener clase $claseId")
                    Result.Error(error)
                }
            } else {
                val error = Exception("No se encontró la clase con ID $claseId")
                Timber.e(error)
                Result.Error(error)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clase $claseId")
            Result.Error(e)
        }
    }
}