package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Comunicado
import com.tfg.umeegunero.data.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para la gestión de comunicados en Firestore
 */
@Singleton
class ComunicadoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val comunicadosCollection = firestore.collection("comunicados")
    
    /**
     * Obtiene la lista de todos los comunicados
     */
    suspend fun getComunicados(): Result<List<Comunicado>> = try {
        val snapshot = comunicadosCollection.get().await()
        val comunicados = snapshot.toObjects(Comunicado::class.java)
        Result.Success(comunicados)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener comunicados")
        Result.Error(e)
    }
    
    /**
     * Obtiene la lista de comunicados filtrados por tipo de usuario
     */
    suspend fun getComunicadosByTipoUsuario(
        tipoUsuario: com.tfg.umeegunero.data.model.TipoUsuario
    ): Result<List<Comunicado>> = try {
        val snapshot = comunicadosCollection
            .whereArrayContains("tiposDestinatarios", tipoUsuario)
            .get()
            .await()
        val comunicados = snapshot.toObjects(Comunicado::class.java)
        Result.Success(comunicados)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener comunicados por tipo de usuario")
        Result.Error(e)
    }
    
    /**
     * Obtiene un comunicado por su ID
     */
    suspend fun getComunicadoById(comunicadoId: String): Result<Comunicado> = try {
        val documentSnapshot = comunicadosCollection.document(comunicadoId).get().await()
        if (documentSnapshot.exists()) {
            val comunicado = documentSnapshot.toObject(Comunicado::class.java)
                ?: throw Exception("Error al convertir comunicado")
            Result.Success(comunicado)
        } else {
            Result.Error(Exception("No se encontró el comunicado"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener comunicado por ID")
        Result.Error(e)
    }
    
    /**
     * Crea un nuevo comunicado
     */
    suspend fun crearComunicado(comunicado: Comunicado): Result<Unit> = try {
        comunicadosCollection.document(comunicado.id).set(comunicado).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al crear comunicado")
        Result.Error(e)
    }
    
    /**
     * Actualiza un comunicado existente
     */
    suspend fun actualizarComunicado(comunicado: Comunicado): Result<Unit> = try {
        comunicadosCollection.document(comunicado.id).set(comunicado).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al actualizar comunicado")
        Result.Error(e)
    }
    
    /**
     * Elimina un comunicado
     */
    suspend fun eliminarComunicado(comunicadoId: String): Result<Unit> = try {
        comunicadosCollection.document(comunicadoId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al eliminar comunicado")
        Result.Error(e)
    }
    
    /**
     * Marca un comunicado como inactivo (archivar)
     */
    suspend fun archivarComunicado(comunicadoId: String): Result<Unit> = try {
        comunicadosCollection.document(comunicadoId)
            .update("activo", false)
            .await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al archivar comunicado")
        Result.Error(e)
    }
} 