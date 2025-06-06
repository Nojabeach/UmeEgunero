package com.tfg.umeegunero.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Centro
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.RegistroActividad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.tfg.umeegunero.util.Result
import com.tfg.umeegunero.util.FirestoreCache
import com.tfg.umeegunero.util.ErrorHandler

/**
 * Repositorio para gestionar centros educativos en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y eliminar
 * información de centros educativos, permitiendo una gestión integral de la
 * información institucional y organizativa de los centros.
 *
 * Características principales:
 * - Creación y gestión de centros educativos
 * - Almacenamiento de información institucional
 * - Gestión de usuarios asociados al centro
 * - Control de permisos y roles por centro
 * - Sincronización de información entre usuarios
 *
 * El repositorio permite:
 * - Registrar nuevos centros educativos
 * - Actualizar información de centros
 * - Gestionar usuarios por centro
 * - Configurar parámetros específicos del centro
 * - Mantener un directorio de centros
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property storageRepository Repositorio para gestionar archivos asociados al centro
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class CentroRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firestoreCache: FirestoreCache,
    private val errorHandler: ErrorHandler
) {
    private val centrosCollection = firestore.collection("centros")
    private val functions = Firebase.functions
    
    // Constantes para las claves de caché
    private object CacheKeys {
        const val ALL_CENTROS = "all_centros"
        const val ACTIVE_CENTROS = "active_centros"
        const val CENTRO_BY_ID_PREFIX = "centro_id_"
        const val CENTRO_BY_NOMBRE_PREFIX = "centro_nombre_"
    }

    /**
     * Obtiene todos los centros educativos con soporte de caché
     */
    suspend fun getAllCentros(): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            // Intentar obtener de la caché primero
            val cachedCentros = firestoreCache.get<List<Centro>>(CacheKeys.ALL_CENTROS)
            
            if (cachedCentros != null) {
                return@withContext Result.Success(cachedCentros)
            }
            
            // Si no está en caché, obtener de Firestore
            val centrosSnapshot = centrosCollection.get().await()
            val centros = centrosSnapshot.toObjects(Centro::class.java)
            
            // Guardar en caché para futuras consultas
            firestoreCache.put(CacheKeys.ALL_CENTROS, centros)
            
            return@withContext Result.Success(centros)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al obtener todos los centros: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los centros educativos activos con soporte de caché
     */
    suspend fun getActiveCentros(): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            // Intentar obtener de la caché primero
            val cachedCentros = firestoreCache.get<List<Centro>>(CacheKeys.ACTIVE_CENTROS)
            
            if (cachedCentros != null) {
                return@withContext Result.Success(cachedCentros)
            }
            
            // Si no está en caché, obtener de Firestore
            val centrosSnapshot = centrosCollection.whereEqualTo("activo", true).get().await()
            val centros = centrosSnapshot.toObjects(Centro::class.java)
            
            // Guardar en caché para futuras consultas
            firestoreCache.put(CacheKeys.ACTIVE_CENTROS, centros)
            
            return@withContext Result.Success(centros)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al obtener centros activos: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un centro por su nombre con soporte de caché
     */
    suspend fun getCentroByNombre(nombre: String): Result<Centro?> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = "${CacheKeys.CENTRO_BY_NOMBRE_PREFIX}$nombre"
            
            // Intentar obtener de la caché primero
            val cachedCentro = firestoreCache.get<Centro>(cacheKey)
            
            if (cachedCentro != null) {
                return@withContext Result.Success(cachedCentro)
            }
            
            // Si no está en caché, obtener de Firestore
            val centroQuery = centrosCollection
                .whereEqualTo("nombre", nombre)
                .get().await()

            if (!centroQuery.isEmpty) {
                val centro = centroQuery.documents[0].toObject(Centro::class.java)
                
                // Guardar en caché para futuras consultas
                if (centro != null) {
                    firestoreCache.put(cacheKey, centro)
                }
                
                return@withContext Result.Success(centro)
            }
            return@withContext Result.Success(null)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al obtener centro por nombre: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un centro por su ID con soporte de caché
     */
    suspend fun getCentroById(centroId: String): Result<Centro> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = "${CacheKeys.CENTRO_BY_ID_PREFIX}$centroId"
            
            // Intentar obtener de la caché primero
            val cachedCentro = firestoreCache.get<Centro>(cacheKey)
            
            if (cachedCentro != null) {
                return@withContext Result.Success(cachedCentro)
            }
            
            // Si no está en caché, obtener de Firestore
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (centroDoc.exists()) {
                val centro = centroDoc.toObject(Centro::class.java)
                
                // Guardar en caché para futuras consultas
                if (centro != null) {
                    firestoreCache.put(cacheKey, centro)
                }
                
                return@withContext Result.Success(centro!!)
            } else {
                throw Exception("Centro no encontrado")
            }
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al obtener centro por ID: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Método alternativo para obtener un centro por ID
     * Para mantener compatibilidad con el código existente
     */
    suspend fun obtenerCentroPorId(centroId: String): Centro? = withContext(Dispatchers.IO) {
        try {
            val result = getCentroById(centroId)
            if (result is Result.Success) {
                return@withContext result.data
            }
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro por ID")
            return@withContext null
        }
    }

    /**
     * Añade un nuevo centro educativo e invalida la caché relacionada
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
            
            // Invalidar cachés relacionadas
            invalidarCaches()

            return@withContext Result.Success(centroId)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al añadir centro: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza un centro existente usando su ID e invalida la caché relacionada
     */
    suspend fun updateCentro(centroId: String, centro: Centro): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el centro existe
            val centroDoc = centrosCollection.document(centroId).get().await()

            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }

            // Asegurarnos de que el centro tiene el ID correcto
            val centroConId = centro.copy(id = centroId)

            // Actualizar el centro
            centrosCollection.document(centroId).set(centroConId).await()
            
            // Invalidar cachés relacionadas
            invalidarCaches()
            firestoreCache.invalidate("${CacheKeys.CENTRO_BY_ID_PREFIX}$centroId")
            firestoreCache.invalidate("${CacheKeys.CENTRO_BY_NOMBRE_PREFIX}${centro.nombre}")

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al actualizar centro: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Desactiva un centro e invalida la caché relacionada
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
            
            // Invalidar cachés relacionadas
            invalidarCaches()
            firestoreCache.invalidate("${CacheKeys.CENTRO_BY_ID_PREFIX}$centroId")
            
            // Obtener el nombre para invalidar también la caché por nombre
            val centro = centroDoc.toObject(Centro::class.java)
            if (centro != null) {
                firestoreCache.invalidate("${CacheKeys.CENTRO_BY_NOMBRE_PREFIX}${centro.nombre}")
            }

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al desactivar centro: $errorMsg")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un centro e invalida la caché relacionada
     */
    suspend fun deleteCentro(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Obtener el centro para invalidar caché por nombre después
            val centroDoc = centrosCollection.document(centroId).get().await()
            val centro = centroDoc.toObject(Centro::class.java)
            
            // Eliminar el centro
            centrosCollection.document(centroId).delete().await()
            
            // Invalidar cachés relacionadas
            invalidarCaches()
            firestoreCache.invalidate("${CacheKeys.CENTRO_BY_ID_PREFIX}$centroId")
            
            if (centro != null) {
                firestoreCache.invalidate("${CacheKeys.CENTRO_BY_NOMBRE_PREFIX}${centro.nombre}")
            }

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al eliminar centro: $errorMsg")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Invalida todas las cachés relacionadas con centros
     */
    private suspend fun invalidarCaches() {
        firestoreCache.invalidate(CacheKeys.ALL_CENTROS)
        firestoreCache.invalidate(CacheKeys.ACTIVE_CENTROS)
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

            // Añadir el profesor y actualizar
            profesores.add(profesorId)
            centrosCollection.document(centroId).update("profesorIds", profesores).await()
            
            // Invalidar cachés relacionadas
            invalidarCaches()
            firestoreCache.invalidate("${CacheKeys.CENTRO_BY_ID_PREFIX}$centroId")

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            val errorMsg = errorHandler.procesarError(e)
            Timber.e(e, "Error al añadir profesor al centro: $errorMsg")
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
     * Elimina un usuario de Firebase Auth
     */
    suspend fun deleteUser(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.currentUser?.let { currentUser ->
                // Solo se puede eliminar si es el usuario actual
                if (currentUser.uid == uid) {
                    currentUser.delete().await()
                    return@withContext Result.Success(Unit)
                } else {
                    // Para eliminar otros usuarios se necesita usar Firebase Admin SDK
                    // desde un servidor o función Cloud
                    return@withContext Result.Error(Exception("No se puede eliminar otro usuario desde el cliente"))
                }
            } ?: return@withContext Result.Error(Exception("No hay usuario autenticado"))
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los cursos de un centro específico
     */
    suspend fun getCursosByCentro(centroId: String): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val cursosCollection = firestore.collection("cursos")
            val cursosSnapshot = cursosCollection
                .whereEqualTo("centroId", centroId)
                .get().await()

            val cursos = cursosSnapshot.toObjects(Curso::class.java)
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene las clases de un centro específico
     */
    suspend fun getClasesByCentro(centroId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            val clasesCollection = firestore.collection("clases")
            val clasesSnapshot = clasesCollection
                .whereEqualTo("centroId", centroId)
                .get().await()

            val clases = clasesSnapshot.toObjects(Clase::class.java)
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los alumnos de un centro específico
     */
    suspend fun getAlumnosByCentro(centroId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            val alumnosCollection = firestore.collection("alumnos")
            val alumnosSnapshot = alumnosCollection
                .whereEqualTo("centroId", centroId)
                .get().await()

            val alumnos = alumnosSnapshot.toObjects(Alumno::class.java)
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los registros de actividad de un alumno
     */
    suspend fun getRegistrosActividadByAlumno(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            val registrosCollection = firestore.collection("registrosActividad")
            val registrosSnapshot = registrosCollection
                .whereEqualTo("alumnoId", alumnoId)
                .get().await()

            val registros = registrosSnapshot.toObjects(RegistroActividad::class.java)
            return@withContext Result.Success(registros)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un registro de actividad
     */
    suspend fun deleteRegistroActividad(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val registrosCollection = firestore.collection("registrosActividad")
            registrosCollection.document(registroId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina una clase
     */
    suspend fun deleteClase(claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val clasesCollection = firestore.collection("clases")
            clasesCollection.document(claseId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un curso
     */
    suspend fun deleteCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cursosCollection = firestore.collection("cursos")
            cursosCollection.document(cursoId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un alumno
     */
    suspend fun deleteAlumno(alumnoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val alumnosCollection = firestore.collection("alumnos")
            alumnosCollection.document(alumnoId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina todas las solicitudes de registro asociadas a un centro
     */
    suspend fun deleteSolicitudesByCentroId(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val solicitudesCollection = firestore.collection("solicitudesRegistro")
            val solicitudesSnapshot = solicitudesCollection
                .whereEqualTo("centroId", centroId)
                .get().await()

            val batch = firestore.batch()
            solicitudesSnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }
            batch.commit().await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina todos los mensajes relacionados con usuarios de un centro
     * Esta operación es más compleja y puede requerir múltiples consultas
     */
    suspend fun deleteMensajesByCentroId(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Obtener primero todos los usuarios del centro para conocer sus IDs
            val usuariosCollection = firestore.collection("usuarios")
            val usuariosQuery = usuariosCollection.get().await()
            val usuarios = usuariosQuery.toObjects(Usuario::class.java)
            
            // Filtrar usuarios con perfiles en este centro
            val usuariosCentro = usuarios.filter { usuario ->
                usuario.perfiles.any { perfil -> perfil.centroId == centroId }
            }
            
            val usuariosIds = usuariosCentro.map { it.documentId }
            
            if (usuariosIds.isEmpty()) {
                return@withContext Result.Success(Unit)
            }
            
            // Ahora buscamos mensajes donde alguno de estos usuarios sea emisor o receptor
            val mensajesCollection = firestore.collection("mensajes")
            
            // Debido a limitaciones de Firestore, es posible que necesitemos hacer varias consultas
            // para grandes cantidades de usuarios
            val batchSize = 10 // Firestore tiene límites en las cláusulas "in"
            val allMensajes = mutableListOf<String>()
            
            // Procesar usuarios en lotes para encontrar mensajes
            for (i in usuariosIds.indices step batchSize) {
                val batchEnd = minOf(i + batchSize, usuariosIds.size)
                val usuariosBatch = usuariosIds.subList(i, batchEnd)
                
                // Buscar mensajes donde usuarios del lote son emisores
                val emisorQuery = mensajesCollection
                    .whereIn("emisorId", usuariosBatch)
                    .get().await()
                
                emisorQuery.documents.forEach { 
                    allMensajes.add(it.id)
                }
                
                // Buscar mensajes donde usuarios del lote son receptores
                val receptorQuery = mensajesCollection
                    .whereIn("receptorId", usuariosBatch)
                    .get().await()
                
                receptorQuery.documents.forEach { 
                    allMensajes.add(it.id)
                }
            }
            
            // Eliminar mensajes encontrados en lotes
            val uniqueMensajes = allMensajes.toSet()
            Timber.d("Se eliminarán ${uniqueMensajes.size} mensajes relacionados con el centro")
            
            for (mensajeId in uniqueMensajes) {
                try {
                    mensajesCollection.document(mensajeId).delete().await()
                } catch (e: Exception) {
                    Timber.e(e, "Error al eliminar mensaje $mensajeId")
                    // Continuamos con el siguiente mensaje
                }
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina completamente un centro y todos sus datos asociados
     */
    suspend fun deleteCentroCompleto(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Iniciando proceso de eliminación completa del centro $centroId")
            
            // 0. Obtener la información del centro para saber los admins
            val centroDoc = centrosCollection.document(centroId).get().await()
            if (!centroDoc.exists()) {
                return@withContext Result.Error(Exception("El centro no existe"))
            }
            
            val centro = centroDoc.toObject(Centro::class.java)!!
            val adminIds = centro.adminIds
            
            // 1. Obtener todos los datos relacionados con el centro
            val alumnosResult = getAlumnosByCentro(centroId)
            val alumnos = if (alumnosResult is Result.Success) alumnosResult.data else emptyList()
            
            val clasesResult = getClasesByCentro(centroId)
            val clases = if (clasesResult is Result.Success) clasesResult.data else emptyList()
            
            val cursosResult = getCursosByCentro(centroId)
            val cursos = if (cursosResult is Result.Success) cursosResult.data else emptyList()
            
            // 2. Eliminar registros de actividad de alumnos
            for (alumno in alumnos) {
                deleteRegistrosActividadByAlumno(alumno.dni)
            }
            Timber.d("Eliminados registros de actividad de ${alumnos.size} alumnos")
            
            // 3. Eliminar evaluaciones y asistencias de las clases
            for (clase in clases) {
                deleteEvaluacionesByClase(clase.id)
                deleteAsistenciasByClase(clase.id)
            }
            Timber.d("Eliminadas evaluaciones y asistencias de ${clases.size} clases")
            
            // 4. Eliminar horarios y materiales educativos de los cursos
            for (curso in cursos) {
                deleteHorariosByCurso(curso.id)
                deleteMaterialesByCurso(curso.id)
            }
            Timber.d("Eliminados horarios y materiales de ${cursos.size} cursos")
            
            // 5. Eliminar alumnos, clases y cursos
            for (alumno in alumnos) {
                deleteAlumno(alumno.dni)
            }
            for (clase in clases) {
                deleteClase(clase.id)
            }
            for (curso in cursos) {
                deleteCurso(curso.id)
            }
            Timber.d("Eliminados ${alumnos.size} alumnos, ${clases.size} clases y ${cursos.size} cursos")
            
            // 6. Eliminar solicitudes, mensajes y otros documentos relacionados
            deleteSolicitudesByCentroId(centroId)
            deleteMensajesByCentroId(centroId)
            deleteNotificacionesByCentroId(centroId)
            deleteEventosByCentroId(centroId)
            
            // 7. Eliminar los usuarios administradores del centro
            val usuariosCollection = firestore.collection("usuarios")
            val emailsToDelete = mutableListOf<String>()
            
            for (adminId in adminIds) {
                try {
                    // Obtener el usuario para conseguir su email
                    val usuarioDoc = usuariosCollection.document(adminId).get().await()
                    if (usuarioDoc.exists()) {
                        val usuario = usuarioDoc.toObject(Usuario::class.java)!!
                        val email = usuario.email
                        
                        // Añadir el email a la lista para eliminación en lote
                        emailsToDelete.add(email)
                        
                        // Eliminar de Firestore
                        usuariosCollection.document(adminId).delete().await()
                        Timber.d("Eliminado usuario administrador $adminId de Firestore")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al eliminar usuario administrador $adminId")
                }
            }
            
            // Intentar eliminar los usuarios de Firebase Authentication mediante Cloud Function
            if (emailsToDelete.isNotEmpty()) {
                try {
                    Timber.d("Se necesitan eliminar ${emailsToDelete.size} usuarios de Firebase Authentication")
                    
                    // Usar Firebase Auth Admin SDK a través de Firebase Functions
                    // en lugar de llamar directamente a una Cloud Function que ya no existe
                    val data = hashMapOf(
                        "action" to "deleteUsers",
                        "emails" to emailsToDelete
                    )
                    
                    // Llamada a la Cloud Function genérica de administración de usuarios
                    functions
                        .getHttpsCallable("manageUsers")
                        .call(data)
                        .addOnSuccessListener { result ->
                            val resultData = result.data
                            Timber.d("Resultado de eliminación de usuarios: $resultData")
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "Error al llamar a la Cloud Function para eliminar usuarios")
                        }
                    
                    Timber.d("Solicitud enviada para eliminar ${emailsToDelete.size} usuarios de Firebase Authentication")
                } catch (e: Exception) {
                    Timber.e(e, "Error al intentar eliminar usuarios de Firebase Authentication: ${e.message}")
                }
            }
            
            Timber.d("Procesados ${adminIds.size} administradores del centro")
            
            // 8. Finalmente, eliminar el centro
            centrosCollection.document(centroId).delete().await()
            Timber.d("Centro $centroId eliminado completamente")
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar completamente el centro $centroId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todos los registros de actividad de un alumno
     */
    suspend fun deleteRegistrosActividadByAlumno(alumnoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val registrosCollection = firestore.collection("registrosActividad")
            val registrosQuery = registrosCollection
                .whereEqualTo("alumnoId", alumnoId)
                .get().await()
                
            val batch = firestore.batch()
            var contadorDocs = 0
            
            for (documento in registrosQuery.documents) {
                batch.delete(documento.reference)
                contadorDocs++
                
                // Firestore tiene un límite de 500 operaciones por lote
                if (contadorDocs >= 450) {
                    batch.commit().await()
                    contadorDocs = 0
                }
            }
            
            if (contadorDocs > 0) {
                batch.commit().await()
            }
            
            Timber.d("Eliminados ${registrosQuery.size()} registros del alumno $alumnoId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar registros del alumno $alumnoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todas las evaluaciones de una clase
     */
    suspend fun deleteEvaluacionesByClase(claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val evaluacionesCollection = firestore.collection("evaluaciones")
            val evaluacionesQuery = evaluacionesCollection
                .whereEqualTo("claseId", claseId)
                .get().await()
                
            val batch = firestore.batch()
            var contadorDocs = 0
            
            for (documento in evaluacionesQuery.documents) {
                batch.delete(documento.reference)
                contadorDocs++
                
                if (contadorDocs >= 450) {
                    batch.commit().await()
                    contadorDocs = 0
                }
            }
            
            if (contadorDocs > 0) {
                batch.commit().await()
            }
            
            Timber.d("Eliminadas ${evaluacionesQuery.size()} evaluaciones de la clase $claseId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar evaluaciones de la clase $claseId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todas las asistencias de una clase
     */
    suspend fun deleteAsistenciasByClase(claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val asistenciasCollection = firestore.collection("asistencias")
            val asistenciasQuery = asistenciasCollection
                .whereEqualTo("claseId", claseId)
                .get().await()
                
            val batch = firestore.batch()
            var contadorDocs = 0
            
            for (documento in asistenciasQuery.documents) {
                batch.delete(documento.reference)
                contadorDocs++
                
                if (contadorDocs >= 450) {
                    batch.commit().await()
                    contadorDocs = 0
                }
            }
            
            if (contadorDocs > 0) {
                batch.commit().await()
            }
            
            Timber.d("Eliminadas ${asistenciasQuery.size()} asistencias de la clase $claseId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar asistencias de la clase $claseId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todos los horarios de un curso
     */
    suspend fun deleteHorariosByCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val horariosCollection = firestore.collection("horarios")
            val horariosQuery = horariosCollection
                .whereEqualTo("cursoId", cursoId)
                .get().await()
                
            val batch = firestore.batch()
            for (documento in horariosQuery.documents) {
                batch.delete(documento.reference)
            }
            
            batch.commit().await()
            Timber.d("Eliminados ${horariosQuery.size()} horarios del curso $cursoId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar horarios del curso $cursoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todos los materiales educativos de un curso
     */
    suspend fun deleteMaterialesByCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val materialesCollection = firestore.collection("materialesEducativos")
            val materialesQuery = materialesCollection
                .whereEqualTo("cursoId", cursoId)
                .get().await()
                
            val batch = firestore.batch()
            for (documento in materialesQuery.documents) {
                batch.delete(documento.reference)
            }
            
            batch.commit().await()
            Timber.d("Eliminados ${materialesQuery.size()} materiales del curso $cursoId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar materiales del curso $cursoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todas las notificaciones relacionadas con un centro
     */
    suspend fun deleteNotificacionesByCentroId(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val notificacionesCollection = firestore.collection("notificaciones")
            val notificacionesQuery = notificacionesCollection
                .whereEqualTo("centroId", centroId)
                .get().await()
                
            val batch = firestore.batch()
            var contadorDocs = 0
            
            for (documento in notificacionesQuery.documents) {
                batch.delete(documento.reference)
                contadorDocs++
                
                if (contadorDocs >= 450) {
                    batch.commit().await()
                    contadorDocs = 0
                }
            }
            
            if (contadorDocs > 0) {
                batch.commit().await()
            }
            
            Timber.d("Eliminadas ${notificacionesQuery.size()} notificaciones del centro $centroId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar notificaciones del centro $centroId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina todos los eventos relacionados con un centro
     */
    suspend fun deleteEventosByCentroId(centroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val eventosCollection = firestore.collection("eventos")
            val eventosQuery = eventosCollection
                .whereEqualTo("centroId", centroId)
                .get().await()
                
            val batch = firestore.batch()
            var contadorDocs = 0
            
            for (documento in eventosQuery.documents) {
                batch.delete(documento.reference)
                contadorDocs++
                
                if (contadorDocs >= 450) {
                    batch.commit().await()
                    contadorDocs = 0
                }
            }
            
            if (contadorDocs > 0) {
                batch.commit().await()
            }
            
            Timber.d("Eliminados ${eventosQuery.size()} eventos del centro $centroId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar eventos del centro $centroId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los centros donde un usuario es administrador o profesor
     */
    suspend fun getCentrosByAdminOrProfesor(usuarioId: String): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando centros donde el usuario $usuarioId es admin o profesor")
            
            // Buscar centros donde el usuario es administrador
            val centrosAdminSnapshot = centrosCollection
                .whereArrayContains("adminIds", usuarioId)
                .get()
                .await()
                
            val centrosAdmin = centrosAdminSnapshot.toObjects(Centro::class.java)
            Timber.d("Se encontraron ${centrosAdmin.size} centros donde el usuario es admin")
            
            // Buscar centros donde el usuario es profesor
            val centrosProfesorSnapshot = centrosCollection
                .whereArrayContains("profesorIds", usuarioId)
                .get()
                .await()
                
            val centrosProfesor = centrosProfesorSnapshot.toObjects(Centro::class.java)
            Timber.d("Se encontraron ${centrosProfesor.size} centros donde el usuario es profesor")
            
            // Combinar y eliminar duplicados
            val todosCentros = (centrosAdmin + centrosProfesor).distinctBy { it.id }
            Timber.d("Total de centros encontrados (sin duplicados): ${todosCentros.size}")
            
            return@withContext Result.Success(todosCentros)
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar centros para el usuario $usuarioId: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los centros activos
     * @return Flow con la lista de centros activos
     */
    suspend fun getCentros(): Flow<List<Centro>> = flow {
        try {
            val centrosSnapshot = centrosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            val centros = centrosSnapshot.toObjects(Centro::class.java)
            emit(centros)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centros activos")
            emit(emptyList())
        }
    }
}