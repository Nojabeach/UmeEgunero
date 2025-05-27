package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.tfg.umeegunero.data.model.Curso
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repositorio para gestionar operaciones relacionadas con los cursos en la aplicación UmeEgunero.
 *
 * Esta interfaz define los métodos para interactuar con los cursos, permitiendo
 * operaciones como obtención, creación, actualización y eliminación de cursos.
 *
 * Los cursos representan períodos académicos en los que se organizan las actividades educativas.
 * Cada curso está asociado a un centro educativo y puede contener múltiples clases.
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property centroRepository Repositorio de centros para obtener información adicional
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
class CursoRepository @Inject constructor() {
    private val firestore = Firebase.firestore
    private val cursosCollection = firestore.collection("cursos")
    private val clasesCollection = firestore.collection("clases")
    private val usuariosCollection = firestore.collection("usuarios")

    /**
     * Obtiene todos los cursos
     */
    suspend fun getAllCursos(): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val cursosSnapshot = cursosCollection.get().await()
            val cursos = cursosSnapshot.toObjects(Curso::class.java)
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un curso por su ID
     */
    suspend fun getCursoById(cursoId: String): Result<Curso> = withContext(Dispatchers.IO) {
        try {
            val cursoDoc = cursosCollection.document(cursoId).get().await()

            if (cursoDoc.exists()) {
                val curso = cursoDoc.toObject<Curso>()
                return@withContext Result.Success(curso!!)
            } else {
                throw Exception("Curso no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un curso
     */
    suspend fun saveCurso(curso: Curso): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cursoId = if (curso.id.isBlank()) {
                cursosCollection.document().id
            } else {
                curso.id
            }

            val cursoWithId = if (curso.id.isBlank()) curso.copy(id = cursoId) else curso

            cursosCollection.document(cursoId).set(cursoWithId).await()

            return@withContext Result.Success(cursoId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un curso
     */
    suspend fun deleteCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cursosCollection.document(cursoId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Desactiva un curso
     */
    suspend fun deactivateCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cursosCollection.document(cursoId).update("activo", false).await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todas las clases asociadas a un curso
     */
    suspend fun obtenerClasesPorCurso(cursoId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("⭐⭐⭐ Obteniendo clases para el curso: $cursoId")
            val snapshot = firestore.collection("clases")
                .whereEqualTo("cursoId", cursoId)
                .get()
                .await()
            
            Timber.d("📄 Documentos encontrados: ${snapshot.documents.size}")
            
            val clases = snapshot.documents.mapNotNull { doc ->
                try {
                    val clase = doc.toObject<Clase>()
                    Timber.d("📝 Documento: id=${doc.id}, data=${doc.data}")
                    clase?.copy(id = doc.id)
                } catch (e: Exception) {
                    Timber.e(e, "🔴 Error mapeando clase desde documento ${doc.id}")
                    null
                }
            }
            
            Timber.d("✅ Obtenidas ${clases.size} clases para el curso $cursoId")
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error al obtener clases para el curso $cursoId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Añade un nuevo curso
     */
    suspend fun agregarCurso(curso: Curso): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cursoId = if (curso.id.isBlank()) cursosCollection.document().id else curso.id
            val cursoWithId = if (curso.id.isBlank()) curso.copy(id = cursoId) else curso

            cursosCollection.document(cursoId).set(cursoWithId).await()
            return@withContext Result.Success(cursoId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Modifica un curso existente
     */
    suspend fun modificarCurso(curso: Curso): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el curso existe
            val cursoDoc = cursosCollection.document(curso.id).get().await()

            if (!cursoDoc.exists()) {
                return@withContext Result.Error(Exception("El curso no existe"))
            }

            // Actualizar el curso
            cursosCollection.document(curso.id).set(curso).await()
            return@withContext Result.Success(curso.id)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Borra un curso
     */
    suspend fun borrarCurso(cursoId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cursosCollection.document(cursoId).delete().await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Asigna un alumno a una clase
     */
    suspend fun asignarAlumnoAClase(alumnoId: String, claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val claseDoc = firestore.collection("clases").document(claseId).get().await()
            
            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase no existe"))
            }
            
            val clase = claseDoc.toObject<Clase>()
            val alumnosActuales = clase?.alumnosIds ?: mutableListOf<String>()
            val nuevosAlumnos = alumnosActuales.toMutableList()
            
            if (!nuevosAlumnos.contains(alumnoId)) {
                nuevosAlumnos.add(alumnoId)
                
                firestore.collection("clases").document(claseId)
                    .update("alumnosIds", nuevosAlumnos)
                    .await()
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al asignar alumno a clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un curso en Firestore
     * @param curso El curso a guardar
     */
    suspend fun guardarCurso(curso: Curso) {
        try {
            cursosCollection.document(curso.id).set(curso).await()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al guardar el curso: ${e.message}")
        }
    }
    
    /**
     * Obtiene un curso por su ID
     * @param cursoId ID del curso
     * @return El curso encontrado
     */
    suspend fun obtenerCursoPorId(cursoId: String): Curso? {
        return try {
            val document = cursosCollection.document(cursoId).get().await()
            document.toObject<Curso>()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al obtener el curso: ${e.message}")
        }
    }
    
    /**
     * Obtiene un flujo de los cursos asociados a un centro, actualizándose en tiempo real.
     * @param centroId ID del centro
     * @param soloActivos si true, solo devuelve cursos activos, si false, devuelve todos
     * @return Flow que emite Result con la lista de cursos del centro cada vez que cambian
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun obtenerCursosPorCentroFlow(centroId: String, soloActivos: Boolean = true): Flow<Result<List<Curso>>> = callbackFlow {
        Timber.d("🔄🔄 Creando Flow para cursos del centro ID: $centroId (soloActivos=$soloActivos)")
        
        // Crear query base filtrando por centroId
        var query: Query = cursosCollection.whereEqualTo("centroId", centroId)
        
        // Aplicar filtro de activo solo si se solicita
        if (soloActivos) {
            query = query.whereEqualTo("activo", true)
        }

        // Enviar un estado de carga inicial
        trySend(Result.Loading()).isSuccess
        
        // Primer carga sin esperar cambios en tiempo real
        try {
            Timber.d("🔍 Consultando cursos para centroId=$centroId")
            val snapshot = query.get().await()
            
            val cursos = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    Timber.d("📄 Documento curso: id=${document.id}, data=$data")
                    
                    val curso = document.toObject<Curso>()?.copy(id = document.id)
                    curso?.also { Timber.d("✅ Curso mapeado: ${it.nombre} (${it.id})") }
                    curso
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al mapear documento de curso: ${document.id}")
                    null
                }
            }
            
            Timber.d("📊 Carga inicial: ${cursos.size} cursos encontrados para centroId=$centroId")
            trySend(Result.Success(cursos)).isSuccess
        } catch (e: Exception) {
            Timber.e(e, "❌ Error en carga inicial de cursos para centroId=$centroId")
            trySend(Result.Error(e)).isSuccess
        }
        
        // Configurar listener para actualizaciones en tiempo real
        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "❌ Error al escuchar cambios en cursos del centro $centroId")
                trySend(Result.Error(error)).isSuccess
                return@addSnapshotListener
            }
            
            if (snapshot == null) {
                Timber.w("⚠️ Snapshot nulo para cursos del centro $centroId")
                trySend(Result.Success(emptyList())).isSuccess
                return@addSnapshotListener
            }

            val cursos = snapshot.documents.mapNotNull { document ->
                try {
                    val curso = document.toObject<Curso>()
                    // Asegurar que el ID del documento se asigna al objeto
                    curso?.copy(id = document.id)
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al mapear documento de curso: ${document.id}, data: ${document.data}")
                    null
                }
            }
            
            Timber.d("✅ Actualización: ${cursos.size} cursos para centroId=$centroId")
            trySend(Result.Success(cursos)).isSuccess
        }

        // Limpiar el listener cuando el Flow se cierra
        awaitClose {
            Timber.d("🧹 Limpiando listener de cursos para centroId=$centroId")
            listenerRegistration.remove()
        }
    }

    /**
     * Obtiene los cursos asociados a un centro (versión suspendida, no reactiva)
     * @param centroId ID del centro
     * @param soloActivos si true, solo devuelve cursos activos, si false, devuelve todos
     * @return lista de cursos del centro
     */
    suspend fun obtenerCursosPorCentro(centroId: String, soloActivos: Boolean = true): List<Curso> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Consultando cursos para el centro ID: $centroId (soloActivos=$soloActivos)")
            
            // Crear query base filtrando por centroId
            var query: Query = cursosCollection.whereEqualTo("centroId", centroId)
            
            // Aplicar filtro de activo solo si se solicita
            if (soloActivos) {
                query = query.whereEqualTo("activo", true)
            }
            
            val snapshot = query.get().await()
            
            val cursos = snapshot.documents.mapNotNull { document ->
                try {
                    val curso = document.toObject<Curso>()
                    // Asegurar que el ID del documento se asigna al objeto
                    curso?.copy(id = document.id)
                } catch (e: Exception) {
                    Timber.e(e, "Error al mapear documento de curso: ${document.id}, data: ${document.data}")
                    null
                }
            }
            
            Timber.d("Encontrados ${cursos.size} cursos para el centro $centroId (soloActivos=$soloActivos)")
            cursos
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener cursos por centro: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Actualiza un curso existente
     * @param curso El curso con los datos actualizados
     */
    suspend fun actualizarCurso(curso: Curso) {
        try {
            cursosCollection.document(curso.id).set(curso).await()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al actualizar el curso: ${e.message}")
        }
    }
    
    /**
     * Actualiza el estado activo de un curso
     * @param cursoId ID del curso
     * @param activo Nuevo estado activo
     */
    suspend fun actualizarEstadoActivo(cursoId: String, activo: Boolean) {
        try {
            cursosCollection.document(cursoId).update("activo", activo).await()
        } catch (e: FirebaseFirestoreException) {
            throw Exception("Error al actualizar el estado del curso: ${e.message}")
        }
    }

    /**
     * Obtiene todos los cursos
     */
    suspend fun getCursos(): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = cursosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
                
            val cursos = snapshot.documents.mapNotNull { doc ->
                val curso = doc.toObject<Curso>()
                curso?.copy(id = doc.id)
            }
            
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener cursos: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene cursos por centro con formato Result para usar en la interfaz de mensajes
     * @param centroId ID del centro
     * @return Result con la lista de cursos o error
     */
    suspend fun getCursosPorCentro(centroId: String): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            val cursos = obtenerCursosPorCentro(centroId, true)
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener cursos por centro: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los cursos asignados a un profesor
     * @param profesorId ID del profesor
     * @return Result con la lista de cursos
     */
    suspend fun getCursosByProfesorId(profesorId: String): Result<List<Curso>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo cursos para el profesor: $profesorId")
            
            // Primero obtenemos las clases asignadas al profesor
            val clasesResult = firestore.collection("clases")
                .whereEqualTo("profesorId", profesorId)
                .get()
                .await()
                
            // También buscar en profesorTitularId y profesoresAuxiliaresIds
            val clasesProfTitular = firestore.collection("clases")
                .whereEqualTo("profesorTitularId", profesorId)
                .get()
                .await()
                
            val clasesAuxiliares = firestore.collection("clases")
                .whereArrayContains("profesoresAuxiliaresIds", profesorId)
                .get()
                .await()
                
            // Combinar resultados y eliminar duplicados
            val documentos = (clasesResult.documents + 
                            clasesProfTitular.documents +
                            clasesAuxiliares.documents).distinctBy { it.id }
            
            // Obtener IDs de cursos únicos
            val cursosIds = documentos.mapNotNull { doc ->
                doc.getString("cursoId")
            }.distinct()
            
            Timber.d("IDs de cursos encontrados para el profesor $profesorId: $cursosIds")
            
            if (cursosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            // Ahora obtenemos los detalles de cada curso
            val cursos = mutableListOf<Curso>()
            for (cursoId in cursosIds) {
                val cursoDoc = cursosCollection.document(cursoId).get().await()
                if (cursoDoc.exists()) {
                    val curso = cursoDoc.toObject<Curso>()?.copy(id = cursoDoc.id)
                    if (curso != null) {
                        cursos.add(curso)
                    }
                }
            }
            
            Timber.d("Obtenidos ${cursos.size} cursos para el profesor $profesorId")
            return@withContext Result.Success(cursos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener cursos para el profesor $profesorId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene las clases de un curso específico asignadas a un profesor
     * @param cursoId ID del curso
     * @param profesorId ID del profesor
     * @return Result con la lista de clases
     */
    suspend fun getClasesByCursoAndProfesor(cursoId: String, profesorId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo clases para el curso $cursoId y profesor $profesorId")
            
            // Consultar clases donde el profesor es el titular
            val clasesQuery1 = firestore.collection("clases")
                .whereEqualTo("cursoId", cursoId)
                .whereEqualTo("profesorId", profesorId)
                .get()
                .await()
                
            // Consultar clases donde el profesor es el titular (campo alternativo)
            val clasesQuery2 = firestore.collection("clases")
                .whereEqualTo("cursoId", cursoId)
                .whereEqualTo("profesorTitularId", profesorId)
                .get()
                .await()
                
            // Consultar clases donde el profesor es auxiliar
            val clasesQuery3 = firestore.collection("clases")
                .whereEqualTo("cursoId", cursoId)
                .whereArrayContains("profesoresAuxiliaresIds", profesorId)
                .get()
                .await()
                
            // Combinar resultados y eliminar duplicados
            val documentos = (clasesQuery1.documents + 
                            clasesQuery2.documents +
                            clasesQuery3.documents).distinctBy { it.id }
            
            val clases = documentos.mapNotNull { doc ->
                try {
                    doc.toObject<Clase>()?.copy(id = doc.id)
                } catch (e: Exception) {
                    Timber.e(e, "Error al convertir documento a Clase: ${doc.id}")
                    null
                }
            }
            
            Timber.d("Encontradas ${clases.size} clases para el curso $cursoId y profesor $profesorId")
            return@withContext Result.Success(clases)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener clases para el curso $cursoId y profesor $profesorId")
            return@withContext Result.Error(e)
        }
    }
}