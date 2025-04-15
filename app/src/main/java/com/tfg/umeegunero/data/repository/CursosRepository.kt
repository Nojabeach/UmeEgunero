package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.Curso
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CursosRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val CURSOS_COLLECTION = "cursos"
    }

    /**
     * Obtiene todos los cursos disponibles en la base de datos.
     * 
     * @return Lista de cursos académicos.
     */
    suspend fun getAllCursos(): List<Curso> {
        return try {
            firestore.collection(CURSOS_COLLECTION)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject<Curso>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene los cursos asociados a un centro específico.
     * 
     * @param centroId Identificador del centro.
     * @return Lista de cursos del centro.
     */
    suspend fun getCursosByCentro(centroId: String): List<Curso> {
        return try {
            firestore.collection(CURSOS_COLLECTION)
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject<Curso>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene un curso específico por su ID.
     * 
     * @param cursoId Identificador del curso.
     * @return El curso encontrado o null si no existe.
     */
    suspend fun getCursoById(cursoId: String): Curso? {
        return try {
            firestore.collection(CURSOS_COLLECTION)
                .document(cursoId)
                .get()
                .await()
                .toObject<Curso>()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crea un nuevo curso académico.
     * 
     * @param curso Datos del curso a crear.
     * @return ID del curso creado o null si falla.
     */
    suspend fun crearCurso(curso: Curso): String? {
        return try {
            val cursoConTimestamp = curso.copy(fechaCreacion = Timestamp.now())
            val documentRef = firestore.collection(CURSOS_COLLECTION).document()
            val cursoConId = cursoConTimestamp.copy(id = documentRef.id)
            documentRef.set(cursoConId).await()
            cursoConId.id
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza un curso existente.
     * 
     * @param curso Datos actualizados del curso.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    suspend fun actualizarCurso(curso: Curso): Boolean {
        return try {
            firestore.collection(CURSOS_COLLECTION)
                .document(curso.id)
                .set(curso)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina un curso académico.
     * 
     * @param cursoId Identificador del curso a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    suspend fun eliminarCurso(cursoId: String): Boolean {
        return try {
            firestore.collection(CURSOS_COLLECTION)
                .document(cursoId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
} 