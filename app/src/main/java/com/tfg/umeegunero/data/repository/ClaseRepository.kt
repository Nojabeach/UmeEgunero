package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar las clases
 */
interface ClaseRepository {
    /**
     * Obtiene una clase por su ID
     */
    suspend fun getClaseById(claseId: String): Result<Clase>
    
    /**
     * Obtiene las clases de un centro
     */
    suspend fun getClasesByCentro(centroId: String): Result<List<Clase>>
    
    /**
     * Obtiene las clases de un profesor
     */
    suspend fun getClasesByProfesor(profesorId: String): Result<List<Clase>>
    
    /**
     * Obtiene todas las clases asociadas a un curso
     */
    suspend fun getClasesByCursoId(cursoId: String): Result<List<Clase>>
    
    /**
     * Guarda una clase en Firestore
     */
    suspend fun guardarClase(clase: Clase): Result<String>
    
    /**
     * Elimina una clase de Firestore
     */
    suspend fun eliminarClase(claseId: String): Result<Boolean>
}

/**
 * Implementación del repositorio de clases
 */
@Singleton
class ClaseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ClaseRepository {
    private val clasesCollection = firestore.collection("clases")
    
    override suspend fun getClasesByCursoId(cursoId: String): Result<List<Clase>> {
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
    
    override suspend fun guardarClase(clase: Clase): Result<String> {
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
    
    override suspend fun eliminarClase(claseId: String): Result<Boolean> {
        return try {
            clasesCollection.document(claseId).delete().await()
            Timber.d("Clase eliminada con ID: $claseId")
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar clase $claseId")
            Result.Error(e)
        }
    }
    
    override suspend fun getClaseById(claseId: String): Result<Clase> {
        return try {
            val document = clasesCollection.document(claseId).get().await()
            if (document.exists()) {
                val clase = document.toObject<Clase>()?.copy(id = document.id)
                if (clase != null) {
                    Result.Success(clase)
                } else {
                    Result.Error(Exception("No se pudo convertir la clase"))
                }
            } else {
                Result.Error(Exception("La clase con ID $claseId no existe"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clase con ID $claseId")
            Result.Error(e)
        }
    }
    
    override suspend fun getClasesByCentro(centroId: String): Result<List<Clase>> {
        return try {
            val snapshot = clasesCollection
                .whereEqualTo("centroId", centroId)
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            val clases = snapshot.documents.mapNotNull { doc ->
                val clase = doc.toObject<Clase>()
                clase?.copy(id = doc.id)
            }
            
            Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases del centro $centroId")
            Result.Error(e)
        }
    }
    
    override suspend fun getClasesByProfesor(profesorId: String): Result<List<Clase>> {
        return try {
            // Obtenemos clases donde el profesor es titular
            val snapshotTitular = clasesCollection
                .whereEqualTo("profesorTitularId", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            val clasesTitular = snapshotTitular.documents.mapNotNull { doc ->
                val clase = doc.toObject<Clase>()
                clase?.copy(id = doc.id)
            }
            
            // Obtenemos clases donde el profesor es auxiliar
            val snapshotAuxiliar = clasesCollection
                .whereArrayContains("profesoresAuxiliaresIds", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            val clasesAuxiliar = snapshotAuxiliar.documents.mapNotNull { doc ->
                val clase = doc.toObject<Clase>()
                clase?.copy(id = doc.id)
            }
            
            // Combinamos los resultados, eliminando duplicados
            val todasLasClases = (clasesTitular + clasesAuxiliar).distinctBy { it.id }
            
            Result.Success(todasLasClases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases del profesor $profesorId")
            Result.Error(e)
        }
    }
}