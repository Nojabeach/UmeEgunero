package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los alumnos
 */
interface AlumnoRepository {
    /**
     * Obtiene un alumno por su ID
     */
    suspend fun getAlumnoById(alumnoId: String): Result<Alumno>
    
    /**
     * Obtiene la lista de alumnos de un centro
     */
    suspend fun getAlumnosByCentro(centroId: String): Result<List<Alumno>>
    
    /**
     * Obtiene la lista de alumnos de una clase
     */
    suspend fun getAlumnosByClase(claseId: String): Result<List<Alumno>>

    /**
     * Obtiene un alumno por su ID (usando nombre nuevo para el Sprint 2)
     */
    suspend fun obtenerAlumnoPorId(alumnoId: String): Result<Alumno?>
}

/**
 * Implementación del repositorio de alumnos
 */
@Singleton
class AlumnoRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlumnoRepository {
    
    companion object {
        private const val COLLECTION_ALUMNOS = "alumnos"
    }

    override suspend fun getAlumnoById(alumnoId: String): Result<Alumno> {
        // Implementación de prueba que devuelve un alumno ficticio
        return Result.Success(
            Alumno(
                id = alumnoId,
                dni = "12345678A",
                nombre = "Alumno de prueba",
                apellidos = "Apellidos de prueba",
                centroId = "centro_prueba",
                aulaId = "aula_prueba", // Reemplazado claseId por aulaId según el modelo
                fechaNacimiento = "01/01/2015"
            )
        )
    }
    
    override suspend fun getAlumnosByCentro(centroId: String): Result<List<Alumno>> {
        // Implementación de prueba que devuelve una lista vacía
        return Result.Success(emptyList())
    }
    
    override suspend fun getAlumnosByClase(claseId: String): Result<List<Alumno>> {
        // Implementación de prueba que devuelve una lista vacía
        return Result.Success(emptyList())
    }

    /**
     * Obtiene un alumno por su ID usando Firestore
     * @param alumnoId ID del alumno
     * @return Resultado con el alumno o null si no existe
     */
    override suspend fun obtenerAlumnoPorId(alumnoId: String): Result<Alumno?> = withContext(Dispatchers.IO) {
        try {
            val documento = firestore.collection(COLLECTION_ALUMNOS)
                .document(alumnoId)
                .get()
                .await()

            if (documento.exists()) {
                val alumno = documento.toObject(Alumno::class.java)
                return@withContext Result.Success(alumno)
            } else {
                return@withContext Result.Success(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumno por ID")
            return@withContext Result.Error(e)
        }
    }
} 