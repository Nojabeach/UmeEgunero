package com.tfg.umeegunero.util

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para manejar operaciones con Firebase
 */
@Singleton
class FirebaseUtil @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    /**
     * Crea un nuevo documento en Firestore
     */
    suspend fun crearDocumento(documentId: String, data: Map<String, Any?>) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(documentId)
                .set(data)
                .await()
            Timber.d("Documento creado exitosamente: $documentId")
        } catch (e: Exception) {
            Timber.e(e, "Error al crear documento: $documentId")
            throw e
        }
    }
    
    /**
     * Actualiza un documento existente en Firestore
     */
    suspend fun actualizarDocumento(documentId: String, data: Map<String, Any?>) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(documentId)
                .update(data)
                .await()
            Timber.d("Documento actualizado exitosamente: $documentId")
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar documento: $documentId")
            throw e
        }
    }
    
    /**
     * Elimina un documento de Firestore
     */
    suspend fun eliminarDocumento(documentId: String) {
        try {
            firestore.collection(COLLECTION_NAME)
                .document(documentId)
                .delete()
                .await()
            Timber.d("Documento eliminado exitosamente: $documentId")
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar documento: $documentId")
            throw e
        }
    }
    
    companion object {
        private const val COLLECTION_NAME = "comunicados"
    }
} 