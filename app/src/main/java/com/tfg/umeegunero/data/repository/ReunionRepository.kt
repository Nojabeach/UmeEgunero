package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Reunion
import com.tfg.umeegunero.data.model.EstadoReunion
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para la gestión de reuniones en Firestore
 */
@Singleton
class ReunionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val reunionesCollection = firestore.collection("reuniones")
    
    /**
     * Obtiene todas las reuniones
     */
    suspend fun getReuniones(): Result<List<Reunion>> = try {
        val snapshot = reunionesCollection
            .orderBy("fechaInicio", Query.Direction.DESCENDING)
            .get()
            .await()
        val reuniones = snapshot.toObjects(Reunion::class.java)
        Result.Success(reuniones)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener reuniones")
        Result.Error(e)
    }
    
    /**
     * Obtiene las reuniones de un usuario específico
     */
    suspend fun getReunionesUsuario(usuarioId: String): Result<List<Reunion>> = try {
        val snapshot = reunionesCollection
            .whereArrayContains("participantesIds", usuarioId)
            .orderBy("fechaInicio", Query.Direction.DESCENDING)
            .get()
            .await()
        val reuniones = snapshot.toObjects(Reunion::class.java)
        Result.Success(reuniones)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener reuniones del usuario")
        Result.Error(e)
    }
    
    /**
     * Obtiene una reunión por su ID
     */
    suspend fun getReunion(reunionId: String): Result<Reunion> = try {
        val documentSnapshot = reunionesCollection.document(reunionId).get().await()
        if (documentSnapshot.exists()) {
            val reunion = documentSnapshot.toObject(Reunion::class.java)
                ?: throw Exception("Error al convertir reunión")
            Result.Success(reunion)
        } else {
            Result.Error(Exception("No se encontró la reunión"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener reunión por ID")
        Result.Error(e)
    }
    
    /**
     * Crea una nueva reunión
     */
    suspend fun crearReunion(reunion: Reunion): Result<String> = try {
        val docRef = reunionesCollection.document()
        val reunionConId = reunion.copy(id = docRef.id)
        docRef.set(reunionConId).await()
        Result.Success(docRef.id)
    } catch (e: Exception) {
        Timber.e(e, "Error al crear reunión")
        Result.Error(e)
    }
    
    /**
     * Actualiza una reunión existente
     */
    suspend fun actualizarReunion(reunion: Reunion): Result<Unit> = try {
        reunionesCollection.document(reunion.id).set(reunion).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al actualizar reunión")
        Result.Error(e)
    }
    
    /**
     * Elimina una reunión
     */
    suspend fun eliminarReunion(reunionId: String): Result<Unit> = try {
        reunionesCollection.document(reunionId).delete().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al eliminar reunión")
        Result.Error(e)
    }
    
    /**
     * Confirma la asistencia de un usuario a una reunión
     */
    suspend fun confirmarAsistencia(reunionId: String, usuarioId: String): Result<Unit> = try {
        val reunionResult = getReunion(reunionId)
        when (reunionResult) {
            is Result.Success -> {
                val reunion = reunionResult.data
                if (reunion.participantesIds.contains(usuarioId)) {
                    val updateData = mapOf(
                        "estado" to EstadoReunion.CONFIRMADA,
                        "participantesIds" to reunion.participantesIds,
                        "participantesNombres" to reunion.participantesNombres
                    )
                    reunionesCollection.document(reunionId).update(updateData).await()
                    Result.Success(Unit)
                } else {
                    Result.Error(Exception("El usuario no está en la lista de participantes"))
                }
            }
            is Result.Error -> Result.Error(reunionResult.exception ?: Exception("Error desconocido"))
            is Result.Loading -> Result.Error(Exception("Cargando datos..."))
        }
    } catch (e: Exception) {
        Timber.e(e, "Error al confirmar asistencia")
        Result.Error(e)
    }
    
    /**
     * Obtiene las reuniones próximas en un rango de fechas
     */
    suspend fun getReunionesProximas(
        fechaInicio: Timestamp,
        fechaFin: Timestamp
    ): Result<List<Reunion>> = try {
        val snapshot = reunionesCollection
            .whereGreaterThanOrEqualTo("fechaInicio", fechaInicio)
            .whereLessThanOrEqualTo("fechaInicio", fechaFin)
            .orderBy("fechaInicio", Query.Direction.ASCENDING)
            .get()
            .await()
        val reuniones = snapshot.toObjects(Reunion::class.java)
        Result.Success(reuniones)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener reuniones próximas")
        Result.Error(e)
    }
} 