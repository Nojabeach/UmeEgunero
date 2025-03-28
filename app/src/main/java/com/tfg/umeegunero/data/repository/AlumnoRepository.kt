package com.tfg.umeegunero.data.repository

import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Result
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
}

/**
 * Implementación del repositorio de alumnos
 */
@Singleton
class AlumnoRepositoryImpl @Inject constructor() : AlumnoRepository {
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
} 