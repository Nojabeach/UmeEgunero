package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar operaciones relacionadas con las clases en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para interactuar con las clases, incluyendo operaciones
 * de consulta, creación, actualización y eliminación.
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property centroRepository Repositorio de centros para obtener información adicional
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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
     * Obtiene un Flow de todas las clases asociadas a un curso, actualizándose en tiempo real.
     */
    fun obtenerClasesPorCursoFlow(cursoId: String): Flow<Result<List<Clase>>>
    
    /**
     * Guarda una clase en Firestore
     */
    suspend fun guardarClase(clase: Clase): Result<String>
    
    /**
     * Elimina una clase de Firestore
     */
    suspend fun eliminarClase(claseId: String): Result<Boolean>

    /**
     * Asigna un profesor a una clase como auxiliar
     */
    suspend fun asignarProfesorAClase(profesorId: String, claseId: String): Result<Unit>

    /**
     * Desasigna un profesor de una clase
     */
    suspend fun desasignarProfesorDeClase(profesorId: String, claseId: String): Result<Unit>

    /**
     * Obtiene los profesores asignados a una clase
     */
    suspend fun getProfesoresByClaseId(claseId: String): Result<List<String>>
    
    /**
     * Asigna un profesor principal a una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor a asignar
     * @return Resultado de la operación
     */
    suspend fun asignarProfesor(claseId: String, profesorId: String): Result<Unit>
    
    /**
     * Desasigna el profesor principal de una clase
     * @param claseId ID de la clase
     * @return Resultado de la operación
     */
    suspend fun desasignarProfesor(claseId: String): Result<Unit>
}

/**
 * Implementación del repositorio de clases
 */
@Singleton
class ClaseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ClaseRepository {
    private val clasesCollection = firestore.collection("clases")
    
    private fun mapearListaSegura(data: Map<String, Any?>, campo: String): List<String> {
        return (data[campo] as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()
    }

    override suspend fun getClasesByCursoId(cursoId: String): Result<List<Clase>> {
        return try {
            Timber.d("Obteniendo clases para el curso: $cursoId")
            val snapshot = clasesCollection
                .whereEqualTo("cursoId", cursoId)
                .get()
                .await()
            
            val clases = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                
                Clase(
                    id = doc.id,
                    cursoId = data["cursoId"] as? String ?: "",
                    centroId = data["centroId"] as? String ?: "",
                    nombre = data["nombre"] as? String ?: "",
                    profesorId = data["profesorId"] as? String,
                    profesorTitularId = data["profesorTitularId"] as? String,
                    profesoresAuxiliaresIds = mapearListaSegura(data, "profesoresAuxiliaresIds"),
                    alumnosIds = mapearListaSegura(data, "alumnosIds"),
                    capacidadMaxima = (data["capacidadMaxima"] as? Number)?.toInt(),
                    activo = data["activo"] as? Boolean ?: true,
                    horario = data["horario"] as? String ?: "",
                    aula = data["aula"] as? String ?: ""
                )
            }
            
            Timber.d("Obtenidas ${clases.size} clases para el curso $cursoId")
            Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases para el curso $cursoId")
            Result.Error(e)
        }
    }
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtenerClasesPorCursoFlow(cursoId: String): Flow<Result<List<Clase>>> = callbackFlow {
        Timber.d("Creando Flow para clases del curso ID: $cursoId")

        val query: Query = clasesCollection.whereEqualTo("cursoId", cursoId)

        // Log de la consulta de Firestore
        Timber.d("Consulta de Firestore: collection=clases, filtro=cursoId=$cursoId")

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error al escuchar cambios en clases del curso $cursoId")
                trySend(Result.Error(error)).isSuccess
                return@addSnapshotListener
            }
            
            if (snapshot == null) {
                Timber.w("Snapshot nulo para clases del curso $cursoId")
                trySend(Result.Success(emptyList())).isSuccess
                return@addSnapshotListener
            }

            // Log de los documentos recibidos
            Timber.d("Documentos recibidos: ${snapshot.documents.size}")
            snapshot.documents.forEach { document ->
                Timber.d("Documento: id=${document.id}, data=${document.data}")
            }

            val clases = snapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                
                Clase(
                    id = document.id,
                    cursoId = data["cursoId"] as? String ?: "",
                    centroId = data["centroId"] as? String ?: "",
                    nombre = data["nombre"] as? String ?: "",
                    profesorId = data["profesorId"] as? String,
                    profesorTitularId = data["profesorTitularId"] as? String,
                    profesoresAuxiliaresIds = mapearListaSegura(data, "profesoresAuxiliaresIds"),
                    alumnosIds = mapearListaSegura(data, "alumnosIds"),
                    capacidadMaxima = (data["capacidadMaxima"] as? Number)?.toInt(),
                    activo = data["activo"] as? Boolean ?: true,
                    horario = data["horario"] as? String ?: "",
                    aula = data["aula"] as? String ?: ""
                )
            }
            
            Timber.d("Snapshot recibido: ${clases.size} clases para el curso $cursoId")
            trySend(Result.Success(clases)).isSuccess
        }

        awaitClose {
            Timber.d("Cancelando listener para clases del curso $cursoId")
            listenerRegistration.remove()
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
                val data = document.data ?: return Result.Error(Exception("Datos de clase vacíos"))
                
                val clase = Clase(
                    id = document.id,
                    cursoId = data["cursoId"] as? String ?: "",
                    centroId = data["centroId"] as? String ?: "",
                    nombre = data["nombre"] as? String ?: "",
                    profesorId = data["profesorId"] as? String,
                    profesorTitularId = data["profesorTitularId"] as? String,
                    profesoresAuxiliaresIds = mapearListaSegura(data, "profesoresAuxiliaresIds"),
                    alumnosIds = mapearListaSegura(data, "alumnosIds"),
                    capacidadMaxima = (data["capacidadMaxima"] as? Number)?.toInt(),
                    activo = data["activo"] as? Boolean ?: true,
                    horario = data["horario"] as? String ?: "",
                    aula = data["aula"] as? String ?: ""
                )
                
                Result.Success(clase)
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
                val data = doc.data ?: return@mapNotNull null
                
                Clase(
                    id = doc.id,
                    cursoId = data["cursoId"] as? String ?: "",
                    centroId = data["centroId"] as? String ?: "",
                    nombre = data["nombre"] as? String ?: "",
                    profesorId = data["profesorId"] as? String,
                    profesorTitularId = data["profesorTitularId"] as? String,
                    profesoresAuxiliaresIds = mapearListaSegura(data, "profesoresAuxiliaresIds"),
                    alumnosIds = mapearListaSegura(data, "alumnosIds"),
                    capacidadMaxima = (data["capacidadMaxima"] as? Number)?.toInt(),
                    activo = data["activo"] as? Boolean ?: true,
                    horario = data["horario"] as? String ?: "",
                    aula = data["aula"] as? String ?: ""
                )
            }
            
            Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases del centro $centroId")
            Result.Error(e)
        }
    }
    
    override suspend fun getClasesByProfesor(profesorId: String): Result<List<Clase>> {
        return try {
            // Obtenemos clases donde el profesor es el profesor principal (nuevo campo)
            val snapshotPrincipal = clasesCollection
                .whereEqualTo("profesorId", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            val clasesPrincipal = snapshotPrincipal.documents.mapNotNull { doc ->
                val clase = doc.toObject<Clase>()
                clase?.copy(id = doc.id)
            }
            
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
            val todasLasClases = (clasesPrincipal + clasesTitular + clasesAuxiliar).distinctBy { it.id }
            
            Timber.d("Encontradas ${todasLasClases.size} clases para el profesor $profesorId")
            
            Result.Success(todasLasClases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases del profesor $profesorId")
            Result.Error(e)
        }
    }

    override suspend fun asignarProfesorAClase(profesorId: String, claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val claseDoc = clasesCollection.document(claseId).get().await()
            
            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase no existe"))
            }
            
            val clase = claseDoc.toObject<Clase>()
            
            // Si la clase ya tiene este profesor como titular, no hacemos nada
            if (clase?.profesorTitularId == profesorId) {
                return@withContext Result.Success(Unit)
            }
            
            // Comprobamos si el profesor ya está en la lista de auxiliares
            val profesoresAuxiliares = clase?.profesoresAuxiliaresIds?.toMutableList() ?: mutableListOf()
            
            if (!profesoresAuxiliares.contains(profesorId)) {
                profesoresAuxiliares.add(profesorId)
                
                clasesCollection.document(claseId)
                    .update("profesoresAuxiliaresIds", profesoresAuxiliares)
                    .await()
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar profesor a clase")
            return@withContext Result.Error(e)
        }
    }

    override suspend fun desasignarProfesorDeClase(profesorId: String, claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val claseDoc = clasesCollection.document(claseId).get().await()
            
            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase no existe"))
            }
            
            val clase = claseDoc.toObject<Clase>()
            
            // Si el profesor es el titular, no permitimos eliminarlo por esta vía
            if (clase?.profesorTitularId == profesorId) {
                return@withContext Result.Error(Exception("No se puede eliminar al profesor titular mediante esta operación"))
            }
            
            // Eliminamos al profesor de la lista de auxiliares
            val profesoresAuxiliares = clase?.profesoresAuxiliaresIds?.toMutableList() ?: mutableListOf()
            
            if (profesoresAuxiliares.contains(profesorId)) {
                profesoresAuxiliares.remove(profesorId)
                
                clasesCollection.document(claseId)
                    .update("profesoresAuxiliaresIds", profesoresAuxiliares)
                    .await()
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al desasignar profesor de clase")
            return@withContext Result.Error(e)
        }
    }

    override suspend fun getProfesoresByClaseId(claseId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val claseDoc = clasesCollection.document(claseId).get().await()
            
            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase no existe"))
            }
            
            val clase = claseDoc.toObject<Clase>()
            
            // Combinamos el titular y los auxiliares
            val todosLosProfesores = mutableListOf<String>()
            clase?.profesorTitularId?.let { 
                if (it.isNotEmpty()) todosLosProfesores.add(it) 
            }
            clase?.profesoresAuxiliaresIds?.let { todosLosProfesores.addAll(it) }
            
            return@withContext Result.Success(todosLosProfesores.distinct())
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores de clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Asigna un profesor principal a una clase
     * @param claseId ID de la clase
     * @param profesorId ID del profesor a asignar
     * @return Resultado de la operación
     */
    override suspend fun asignarProfesor(claseId: String, profesorId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Asignando profesor principal $profesorId a clase $claseId")
            
            // Actualizar el campo profesorId en la clase
            clasesCollection.document(claseId)
                .update("profesorId", profesorId)
                .await()
            
            Timber.d("Profesor $profesorId asignado correctamente a clase $claseId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar profesor principal a clase")
            Result.Error(e)
        }
    }
    
    /**
     * Desasigna el profesor principal de una clase
     * @param claseId ID de la clase
     * @return Resultado de la operación
     */
    override suspend fun desasignarProfesor(claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Eliminando profesor principal de clase $claseId")
            
            // Establecer el campo profesorId a null o cadena vacía
            clasesCollection.document(claseId)
                .update("profesorId", null)
                .await()
            
            Timber.d("Profesor principal eliminado correctamente de clase $claseId")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar profesor principal de clase")
            Result.Error(e)
        }
    }
}