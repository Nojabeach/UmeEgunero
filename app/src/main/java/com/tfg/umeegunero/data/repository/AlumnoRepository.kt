package com.tfg.umeegunero.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
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

    /**
     * Obtiene todos los alumnos de una clase específica
     * @param claseId ID de la clase
     * @return Resultado con la lista de alumnos
     */
    suspend fun obtenerAlumnosPorClase(claseId: String): Result<List<Alumno>>

    /**
     * Obtiene todos los alumnos del sistema
     */
    suspend fun getAlumnos(): Result<List<Alumno>>
    
    /**
     * Obtiene todos los alumnos de un curso específico
     * @param cursoId ID del curso
     * @return Lista de alumnos
     */
    suspend fun getAlumnosByCursoId(cursoId: String): Result<List<Alumno>>
    
    /**
     * Obtiene todos los alumnos de una clase específica
     * @param claseId ID de la clase
     * @return Lista de alumnos
     */
    suspend fun getAlumnosByClaseId(claseId: String): Result<List<Alumno>>
    
    /**
     * Crea un nuevo alumno en el sistema
     * @param nombre Nombre del alumno
     * @param apellidos Apellidos del alumno
     * @param dni DNI o documento de identidad
     * @param fechaNacimiento Fecha de nacimiento en formato string
     * @param cursoId ID del curso al que pertenece
     * @return ID del alumno creado
     */
    suspend fun crearAlumno(
        nombre: String,
        apellidos: String,
        dni: String,
        fechaNacimiento: String,
        cursoId: String
    ): Result<String>

    /**
     * Obtiene la lista de alumnos asociados a un familiar.
     * @param familiarId ID del familiar.
     * @return Resultado con la lista de alumnos.
     */
    suspend fun obtenerAlumnosPorFamiliar(familiarId: String): Result<List<Alumno>>

    /**
     * Obtiene la lista de alumnos asociados a un familiar
     * @param familiarId ID del familiar
     * @return Lista de alumnos vinculados al familiar
     */
    suspend fun getAlumnosByFamiliarId(familiarId: String): Result<List<Alumno>>
    
    /**
     * Obtiene todos los alumnos de una clase específica de forma directa
     * @param claseId ID de la clase
     * @return Lista de alumnos de la clase
     */
    suspend fun getAlumnosPorClase(claseId: String): List<Alumno>
    
    /**
     * Actualiza el profesor asignado a un alumno
     * @param alumnoDni DNI del alumno
     * @param profesorId ID del profesor
     * @return Resultado indicando éxito o error
     */
    suspend fun actualizarProfesor(alumnoDni: String, profesorId: String): Result<Unit>
    
    /**
     * Elimina la referencia al profesor de un alumno
     * @param alumnoDni DNI del alumno
     * @return Resultado indicando éxito o error
     */
    suspend fun eliminarProfesor(alumnoDni: String): Result<Unit>

    /**
     * Obtiene un alumno por su DNI
     * @param dni DNI del alumno a buscar
     * @return Resultado con el alumno encontrado o error
     */
    suspend fun getAlumnoPorDni(dni: String): Result<Alumno>

    /**
     * Obtiene todos los alumnos asignados a un profesor
     * @param profesorId ID del profesor
     * @return Lista de alumnos asignados al profesor
     */
    suspend fun getAlumnosForProfesor(profesorId: String): List<Alumno>
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

    /**
     * Obtiene todos los alumnos de una clase específica
     * @param claseId ID de la clase
     * @return Resultado con la lista de alumnos
     */
    override suspend fun obtenerAlumnosPorClase(claseId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("aulaId", claseId)
                .get()
                .await()

            val alumnos = query.toObjects(Alumno::class.java)
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los alumnos del sistema
     */
    override suspend fun getAlumnos(): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .get()
                .await()

            val alumnos = query.toObjects(Alumno::class.java)
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos del sistema")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los alumnos de un curso específico
     * @param cursoId ID del curso
     * @return Lista de alumnos
     */
    override suspend fun getAlumnosByCursoId(cursoId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("cursoId", cursoId)
                .get()
                .await()

            val alumnos = query.toObjects(Alumno::class.java)
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por curso")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los alumnos de una clase específica
     * @param claseId ID de la clase
     * @return Lista de alumnos
     */
    override suspend fun getAlumnosByClaseId(claseId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("aulaId", claseId)
                .get()
                .await()

            val alumnos = query.toObjects(Alumno::class.java)
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por clase")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea un nuevo alumno en el sistema
     * @param nombre Nombre del alumno
     * @param apellidos Apellidos del alumno
     * @param dni DNI o documento de identidad
     * @param fechaNacimiento Fecha de nacimiento en formato string
     * @param cursoId ID del curso al que pertenece
     * @return ID del alumno creado
     */
    override suspend fun crearAlumno(
        nombre: String,
        apellidos: String,
        dni: String,
        fechaNacimiento: String,
        cursoId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val alumno = Alumno(
                id = "",
                dni = dni,
                nombre = nombre,
                apellidos = apellidos,
                centroId = "",
                aulaId = "",
                fechaNacimiento = fechaNacimiento
            )
            val documento = firestore.collection(COLLECTION_ALUMNOS)
                .add(alumno)
                .await()
            return@withContext Result.Success(documento.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear alumno")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene la lista de alumnos asociados a un familiar.
     * @param familiarId ID del familiar.
     * @return Resultado con la lista de alumnos.
     */
    override suspend fun obtenerAlumnosPorFamiliar(familiarId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener el documento del familiar para sacar los IDs de los hijos
            val familiarDoc = firestore.collection("usuarios") // Asumiendo colección "usuarios"
                .document(familiarId)
                .get()
                .await()

            if (!familiarDoc.exists()) {
                Timber.w("Familiar con ID $familiarId no encontrado.")
                return@withContext Result.Success(emptyList()) // O Result.Error si se prefiere
            }

            // Asumiendo que el documento del familiar tiene un campo List<String> llamado "hijosIds"
            val hijosIds = familiarDoc.get("hijosIds") as? List<String>

            if (hijosIds == null || hijosIds.isEmpty()) {
                Timber.i("Familiar con ID $familiarId no tiene hijos asociados.")
                return@withContext Result.Success(emptyList())
            }

            // 2. Consultar los alumnos usando los IDs obtenidos (máximo 10 IDs por consulta 'in')
            // Firestore limita las consultas 'in' a 10 elementos. Si hay más hijos, hay que dividir la consulta.
            if (hijosIds.size > 10) {
                 Timber.w("La consulta 'in' de Firestore está limitada a 10 IDs. Se necesitan múltiples consultas para $familiarId.")
                 // Implementación simplificada: Por ahora, solo tomamos los primeros 10.
                 // Una implementación completa dividiría hijosIds en chunks de 10 y haría múltiples consultas.
                 val primerosDiezIds = hijosIds.take(10)
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                     .whereIn(com.google.firebase.firestore.FieldPath.documentId(), primerosDiezIds)
                     .get()
                     .await()
                 val alumnos = query.toObjects(Alumno::class.java)
                 return@withContext Result.Success(alumnos)

            } else {
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), hijosIds)
                    .get()
                    .await()
                 val alumnos = query.toObjects(Alumno::class.java)
                 return@withContext Result.Success(alumnos)
            }

        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por familiar ID: $familiarId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene la lista de alumnos asociados a un familiar
     * @param familiarId ID del familiar
     * @return Lista de alumnos vinculados al familiar
     */
    override suspend fun getAlumnosByFamiliarId(familiarId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            // 1. Obtener el documento del familiar para sacar los IDs de los hijos
            val familiarDoc = firestore.collection("usuarios") // Asumiendo colección "usuarios"
                .document(familiarId)
                .get()
                .await()

            if (!familiarDoc.exists()) {
                Timber.w("Familiar con ID $familiarId no encontrado.")
                return@withContext Result.Success(emptyList()) // O Result.Error si se prefiere
            }

            // Asumiendo que el documento del familiar tiene un campo List<String> llamado "hijosIds"
            val hijosIds = familiarDoc.get("hijosIds") as? List<String>

            if (hijosIds == null || hijosIds.isEmpty()) {
                Timber.i("Familiar con ID $familiarId no tiene hijos asociados.")
                return@withContext Result.Success(emptyList())
            }

            // 2. Consultar los alumnos usando los IDs obtenidos (máximo 10 IDs por consulta 'in')
            // Firestore limita las consultas 'in' a 10 elementos. Si hay más hijos, hay que dividir la consulta.
            if (hijosIds.size > 10) {
                 Timber.w("La consulta 'in' de Firestore está limitada a 10 IDs. Se necesitan múltiples consultas para $familiarId.")
                 // Implementación simplificada: Por ahora, solo tomamos los primeros 10.
                 // Una implementación completa dividiría hijosIds en chunks de 10 y haría múltiples consultas.
                 val primerosDiezIds = hijosIds.take(10)
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                     .whereIn(com.google.firebase.firestore.FieldPath.documentId(), primerosDiezIds)
                     .get()
                     .await()
                 val alumnos = query.toObjects(Alumno::class.java)
                 return@withContext Result.Success(alumnos)

            } else {
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), hijosIds)
                    .get()
                    .await()
                 val alumnos = query.toObjects(Alumno::class.java)
                 return@withContext Result.Success(alumnos)
            }

        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por familiar ID: $familiarId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los alumnos de una clase específica de forma directa
     * @param claseId ID de la clase
     * @return Lista de alumnos de la clase
     */
    override suspend fun getAlumnosPorClase(claseId: String): List<Alumno> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("aulaId", claseId)
                .get()
                .await()
                
            return@withContext query.toObjects(Alumno::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por clase: $claseId")
            return@withContext emptyList<Alumno>()
        }
    }
    
    /**
     * Actualiza el profesor asignado a un alumno
     * @param alumnoDni DNI del alumno
     * @param profesorId ID del profesor
     * @return Resultado indicando éxito o error
     */
    override suspend fun actualizarProfesor(alumnoDni: String, profesorId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Buscar primero el documento del alumno por su DNI
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", alumnoDni)
                .limit(1)
                .get()
                .await()
                
            if (query.isEmpty) {
                Timber.w("No se encontró alumno con DNI: $alumnoDni")
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            // Actualizar el documento con el nuevo profesorId
            val documentSnapshot = query.documents.first()
            firestore.collection(COLLECTION_ALUMNOS)
                .document(documentSnapshot.id)
                .update("profesorId", profesorId)
                .await()
            
            Timber.d("Profesor actualizado para alumno: $alumnoDni, nuevo profesor: $profesorId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar profesor para alumno: $alumnoDni")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina la referencia al profesor de un alumno
     * @param alumnoDni DNI del alumno
     * @return Resultado indicando éxito o error
     */
    override suspend fun eliminarProfesor(alumnoDni: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Buscar primero el documento del alumno por su DNI
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", alumnoDni)
                .limit(1)
                .get()
                .await()
                
            if (query.isEmpty) {
                Timber.w("No se encontró alumno con DNI: $alumnoDni")
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            // Eliminar el campo profesorId del documento
            val documentSnapshot = query.documents.first()
            
            // Firestore permite actualizar a null para eliminar campos
            firestore.collection(COLLECTION_ALUMNOS)
                .document(documentSnapshot.id)
                .update("profesorId", null)
                .await()
            
            Timber.d("Profesor eliminado para alumno: $alumnoDni")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al eliminar profesor para alumno: $alumnoDni")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un alumno por su DNI
     * @param dni DNI del alumno a buscar
     * @return Resultado con el alumno encontrado o error
     */
    override suspend fun getAlumnoPorDni(dni: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", dni)
                .limit(1)
                .get()
                .await()
                
            if (query.isEmpty) {
                Timber.w("No se encontró alumno con DNI: $dni")
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val alumno = query.documents.first().toObject(Alumno::class.java)
            if (alumno != null) {
                Timber.d("Alumno encontrado por DNI: $dni - ${alumno.nombre} ${alumno.apellidos}")
                return@withContext Result.Success(alumno)
            } else {
                Timber.w("Error al convertir documento a Alumno para DNI: $dni")
                return@withContext Result.Error(Exception("Error al convertir documento a Alumno"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar alumno por DNI: $dni")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los alumnos asignados a un profesor
     * @param profesorId ID del profesor
     * @return Lista de alumnos asignados al profesor
     */
    override suspend fun getAlumnosForProfesor(profesorId: String): List<Alumno> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("profesorId", profesorId)
                .get()
                .await()

            return@withContext query.toObjects(Alumno::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos para el profesor: $profesorId")
            return@withContext emptyList()
        }
    }
} 