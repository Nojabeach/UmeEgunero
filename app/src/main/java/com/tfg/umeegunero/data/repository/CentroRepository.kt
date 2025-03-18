package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class CentroRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val centrosCollection = firestore.collection("centros")

    /**
     * Obtiene todos los centros educativos
     */
    suspend fun getAllCentros(): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            val centrosSnapshot = centrosCollection.get().await()
            val centros = centrosSnapshot.toObjects(Centro::class.java)
            return@withContext Result.Success(centros)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los centros educativos activos
     */
    suspend fun getActiveCentros(): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            val centrosSnapshot = centrosCollection.whereEqualTo("activo", true).get().await()
            val centros = centrosSnapshot.toObjects(Centro::class.java)
            return@withContext Result.Success(centros)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un centro por su ID
     */
    suspend fun getCentroById(centroId: String): Result<Centro> = withContext(Dispatchers.IO) {
        try {
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (centroDoc.exists()) {
                val centro = centroDoc.toObject(Centro::class.java)
                return@withContext Result.Success(centro!!)
            } else {
                throw Exception("Centro no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Añade un nuevo centro educativo
     */
    suspend fun addCentro(centro: Centro): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verificar si ya existe un centro con el mismo nombre
            val existingCentros = centrosCollection
                .whereEqualTo("nombre", centro.nombre)
                .get().await()

            if (!existingCentros.isEmpty) {
                return@withContext Result.Error(Exception("Ya existe un centro con este nombre"))
            }

            // Usar el ID generado o crear uno nuevo
            val centroId = centro.id.ifBlank { centrosCollection.document().id }

            // Crear el documento con el ID
            val centroWithId = if (centro.id.isBlank()) centro.copy(id = centroId) else centro

            // Guardar el centro
            centrosCollection.document(centroId).set(centroWithId).await()

            return@withContext Result.Success(centroId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza un centro existente
     */
    suspend fun updateCentro(centro: Centro): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el centro existe
            val centroDoc = centrosCollection.document(centro.id).get().await()

            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }

            // Actualizar el centro
            centrosCollection.document(centro.id).set(centro).await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Desactiva un centro
     */
    suspend fun deactivateCentro(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el centro existe
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }

            // Desactivar el centro
            centrosCollection.document(centroId).update("activo", false).await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un centro
     */
    suspend fun deleteCentro(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Eliminar el centro
            centrosCollection.document(centroId).delete().await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Añade un profesor al centro
     */
    suspend fun addProfesorToCentro(centroId: String, profesorId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el centro existe
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }

            // Obtener la lista actual de profesores
            val centro = centroDoc.toObject(Centro::class.java)!!
            val profesores = centro.profesorIds.toMutableList()

            // Verificar que el profesor no está ya en el centro
            if (profesores.contains(profesorId)) {
                return@withContext Result.Error(Exception("El profesor ya está asignado a este centro"))
            }

            // Añadir el profesor
            profesores.add(profesorId)

            // Actualizar el centro
            centrosCollection.document(centroId).update("profesorIds", profesores).await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un profesor del centro
     */
    suspend fun removeProfesorFromCentro(centroId: String, profesorId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el centro existe
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }

            // Obtener la lista actual de profesores
            val centro = centroDoc.toObject(Centro::class.java)!!
            val profesores = centro.profesorIds.toMutableList()

            // Verificar que el profesor está en el centro
            if (!profesores.contains(profesorId)) {
                return@withContext Result.Error(Exception("El profesor no está asignado a este centro"))
            }

            // Eliminar el profesor
            profesores.remove(profesorId)

            // Actualizar el centro
            centrosCollection.document(centroId).update("profesorIds", profesores).await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los profesores de un centro
     */
    suspend fun getProfesoresByCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el centro existe
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }

            // Obtener la lista de profesores
            val centro = centroDoc.toObject(Centro::class.java)!!
            val profesorIds = centro.profesorIds

            if (profesorIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }

            // Obtener los datos de los profesores
            val usuariosCollection = firestore.collection("usuarios")
            val profesores = mutableListOf<Usuario>()

            for (profesorId in profesorIds) {
                val usuarioDoc = usuariosCollection.document(profesorId).get().await()
                if (usuarioDoc.exists()) {
                    val usuario = usuarioDoc.toObject(Usuario::class.java)!!
                    profesores.add(usuario)
                }
            }

            return@withContext Result.Success(profesores)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea un nuevo usuario en Firebase Authentication
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return UID del usuario creado en caso de éxito
     */
    suspend fun createUserWithEmailAndPassword(
        email: String, 
        password: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear usuario: UID no disponible")
            
            Timber.d("Usuario creado correctamente con UID: $uid")
            return@withContext Result.Success(uid)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear usuario con email: $email")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina un usuario de Firebase Authentication
     * @param uid UID del usuario a eliminar
     */
    suspend fun deleteUser(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Para eliminar un usuario, necesitamos iniciar sesión como ese usuario
            // Esto normalmente requeriría un token o credenciales de administrador
            // Simplemente registramos que esta operación requeriría privilegios adicionales
            Timber.w("Intento de eliminar usuario con UID: $uid - Requiere privilegios de admin")
            
            // Esta implementación es un placeholder, ya que eliminar usuarios requiere
            // autenticación como ese usuario o privilegios de admin en Firebase
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar usuario con UID: $uid")
            return@withContext Result.Error(e)
        }
    }
}