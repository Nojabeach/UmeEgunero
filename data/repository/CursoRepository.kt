// Añadir los métodos requeridos por los ViewModels
/**
 * Obtiene todas las clases asociadas a un curso
 */
suspend fun obtenerClasesPorCurso(cursoId: String): Result<List<Clase>> {
    return try {
        Timber.d("Obteniendo clases para el curso: $cursoId")
        val snapshot = firestore.collection("clases")
            .whereEqualTo("cursoId", cursoId)
            .get()
            .await()
        
        val clases = snapshot.documents.mapNotNull { doc ->
            val clase = doc.toObject(Clase::class.java)
            clase?.copy(id = doc.id)
        }
        
        Timber.d("Obtenidas ${clases.size} clases para el curso $cursoId")
        Result.Success(clases)
    } catch (e: Exception) {
        Timber.e(e, "Error al obtener clases para el curso $cursoId")
        Result.Error(e)
    }
}

/**
 * Añade un nuevo curso
 */
suspend fun agregarCurso(curso: Curso): Result<String> {
    return createCurso(curso)
}

/**
 * Modifica un curso existente
 */
suspend fun modificarCurso(curso: Curso): Result<String> {
    return updateCurso(curso)
}

/**
 * Borra un curso
 */
suspend fun borrarCurso(cursoId: String): Result<Unit> {
    return deleteCurso(cursoId)
}

/**
 * Asigna un alumno a una clase
 */
suspend fun asignarAlumnoAClase(alumnoId: String, claseId: String): Result<Unit> {
    return try {
        val claseDoc = firestore.collection("clases").document(claseId).get().await()
        
        if (!claseDoc.exists()) {
            return Result.Error(Exception("La clase no existe"))
        }
        
        val clase = claseDoc.toObject(Clase::class.java)
        val alumnosActuales = clase?.alumnos ?: mutableListOf()
        
        if (!alumnosActuales.contains(alumnoId)) {
            alumnosActuales.add(alumnoId)
            
            firestore.collection("clases").document(claseId)
                .update("alumnos", alumnosActuales)
                .await()
        }
        
        Result.Success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error al asignar alumno a clase")
        Result.Error(e)
    }
} 