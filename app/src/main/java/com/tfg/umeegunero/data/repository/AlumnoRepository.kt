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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects

/**
 * Repositorio para gestionar información de alumnos en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y eliminar
 * información de alumnos, permitiendo una gestión integral de los estudiantes
 * dentro del sistema educativo.
 *
 * Características principales:
 * - Registro y gestión de perfiles de alumnos
 * - Asignación a clases y cursos
 * - Seguimiento de progreso académico
 * - Gestión de información personal y académica
 * - Control de permisos y roles de alumnos
 *
 * El repositorio permite:
 * - Crear y actualizar perfiles de alumnos
 * - Gestionar asignaciones a clases y cursos
 * - Consultar información de alumnos
 * - Mantener un registro académico
 * - Facilitar el seguimiento del desarrollo educativo
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property centroRepository Repositorio de centros para obtener contexto
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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
     * @param claseId ID de la clase a la que se asignará (opcional)
     * @return ID del alumno creado
     */
    suspend fun crearAlumno(
        nombre: String,
        apellidos: String,
        dni: String,
        fechaNacimiento: String,
        cursoId: String,
        claseId: String = ""
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
    suspend fun getAlumnoByDni(dni: String): Result<Alumno>

    /**
     * Obtiene todos los alumnos asignados a un profesor
     * @param profesorId ID del profesor
     * @return Lista de alumnos asignados al profesor
     */
    suspend fun getAlumnosForProfesor(profesorId: String): List<Alumno>

    /**
     * Obtiene todos los alumnos de un centro
     * @param centroId ID del centro
     * @return Lista de alumnos del centro
     */
    suspend fun getAlumnosByCentroId(centroId: String): List<Alumno>

    /**
     * Busca alumnos que tengan a un familiar en su lista de familiares
     * 
     * Este método busca alumnos cuyo campo familiarIds contiene el ID del familiar.
     * Es una búsqueda alternativa para cuando la relación principal falla.
     * 
     * @param familiarId ID del familiar a buscar en la lista de familiares de los alumnos
     * @return Resultado con la lista de alumnos encontrados
     */
    suspend fun getAlumnosWithFamiliarId(familiarId: String): Result<List<Alumno>>

    /**
     * Actualiza la referencia a la clase en un alumno (asignación)
     * @param alumnoId ID (DNI) del alumno
     * @param claseId ID de la clase a asignar
     * @return Resultado de la operación
     */
    suspend fun asignarClaseAAlumno(alumnoId: String, claseId: String): Result<Unit>
    
    /**
     * Elimina la referencia a la clase en un alumno (desasignación)
     * @param alumnoId ID (DNI) del alumno
     * @param claseId ID de la clase a desasignar
     * @return Resultado de la operación
     */
    suspend fun desasignarClaseDeAlumno(alumnoId: String, claseId: String): Result<Unit>
    
    /**
     * Sincroniza todos los alumnos existentes con sus clases correspondientes
     * Este método busca todos los alumnos que tienen una clase asignada (aulaId o claseId)
     * y actualiza el array alumnosIds de esas clases
     * @return Resultado de la operación
     */
    suspend fun sincronizarAlumnosConClases(): Result<Unit>
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

    override suspend fun getAlumnoById(alumnoId: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val documento = firestore.collection(COLLECTION_ALUMNOS)
                .document(alumnoId) // Asume que alumnoId es el DNI/ID del documento
                .get()
                .await()

            if (documento.exists()) {
                val alumno = documento.toObject(Alumno::class.java)
                if (alumno != null) {
                    return@withContext Result.Success(alumno)
                } else {
                    Timber.e("Error al convertir el documento del alumno a objeto: $alumnoId")
                    return@withContext Result.Error(Exception("Error al convertir datos del alumno."))
                }
            } else {
                Timber.w("No se encontró el alumno con ID: $alumnoId")
                return@withContext Result.Error(Exception("Alumno no encontrado."))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumno por ID: $alumnoId")
            return@withContext Result.Error(e)
        }
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
                .whereEqualTo("claseId", claseId)
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
                .whereEqualTo("claseId", claseId)
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
     * @param claseId ID de la clase a la que se asignará (opcional)
     * @return ID del alumno creado
     */
    override suspend fun crearAlumno(
        nombre: String,
        apellidos: String,
        dni: String,
        fechaNacimiento: String,
        cursoId: String,
        claseId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Creando alumno: $nombre $apellidos (DNI: $dni) para curso: $cursoId, clase: $claseId")
            
            // Obtener el centroId del curso
            var centroId = ""
            var cursoNombre = ""
            if (cursoId.isNotEmpty()) {
                try {
                    val cursoDoc = firestore.collection("cursos").document(cursoId).get().await()
                    if (cursoDoc.exists()) {
                        centroId = cursoDoc.getString("centroId") ?: ""
                        cursoNombre = cursoDoc.getString("nombre") ?: ""
                        Timber.d("Centro obtenido del curso $cursoId: $centroId, nombre: $cursoNombre")
                    } else {
                        Timber.w("No se encontró el curso con ID: $cursoId")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener centroId del curso $cursoId")
                }
            }
            
            // Obtener información de la clase si se proporciona
            var claseNombre = ""
            var profesorId = ""
            if (claseId.isNotEmpty()) {
                try {
                    val claseDoc = firestore.collection("clases").document(claseId).get().await()
                    if (claseDoc.exists()) {
                        claseNombre = claseDoc.getString("nombre") ?: ""
                        profesorId = claseDoc.getString("profesorId") ?: ""
                        Timber.d("Clase obtenida $claseId: nombre=$claseNombre, profesorId=$profesorId")
                    } else {
                        Timber.w("No se encontró la clase con ID: $claseId")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener información de la clase $claseId")
                }
            }
            
            val alumno = Alumno(
                id = "",
                dni = dni,
                nombre = nombre,
                apellidos = apellidos,
                centroId = centroId,
                curso = cursoNombre,
                claseId = claseId,
                clase = claseNombre,
                profesorId = profesorId,
                fechaNacimiento = fechaNacimiento,
                activo = true
            )
            
            // Crear el documento del alumno usando el DNI como ID
            firestore.collection(COLLECTION_ALUMNOS)
                .document(dni)
                .set(alumno)
                .await()
            
            // Si se proporciona claseId, añadir el alumno a la lista alumnosIds de la clase
            if (claseId.isNotEmpty()) {
                try {
                    Timber.d("Intentando agregar alumno $dni a la clase $claseId")
                    val claseRef = firestore.collection("clases").document(claseId)
                    val claseDoc = claseRef.get().await()
                    
                    if (claseDoc.exists()) {
                        Timber.d("Clase $claseId encontrada, actualizando alumnosIds")
                        val alumnosIdsAny = claseDoc.get("alumnosIds")
                        val alumnosIds = when {
                            alumnosIdsAny is List<*> -> alumnosIdsAny.filterIsInstance<String>().toMutableList()
                            else -> mutableListOf()
                        }
                        
                        Timber.d("Lista actual de alumnosIds en clase $claseId: $alumnosIds")
                        
                        if (!alumnosIds.contains(dni)) {
                            alumnosIds.add(dni)
                            claseRef.update("alumnosIds", alumnosIds).await()
                            Timber.d("✅ Alumno $dni añadido exitosamente a la lista alumnosIds de la clase $claseId")
                            Timber.d("Nueva lista de alumnosIds: $alumnosIds")
                        } else {
                            Timber.d("El alumno $dni ya estaba en la lista alumnosIds de la clase $claseId")
                        }
                    } else {
                        Timber.w("⚠️ La clase $claseId no existe, no se puede actualizar alumnosIds")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al añadir alumno $dni a la lista de la clase $claseId, pero el alumno se creó correctamente")
                    // No fallar la creación del alumno por este error, pero es importante que se registre
                }
            } else {
                Timber.d("No se proporcionó claseId, no se actualiza ninguna clase")
            }
                
            Timber.d("Alumno creado exitosamente con DNI: $dni, centroId: $centroId, claseId: $claseId")
            return@withContext Result.Success(dni) // Devolver el DNI como ID
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
            val hijosIds = familiarDoc.get("hijosIds")
            val hijosIdsList = when {
                hijosIds is List<*> -> hijosIds.filterIsInstance<String>()
                else -> emptyList()
            }

            if (hijosIdsList.isEmpty()) {
                Timber.i("Familiar con ID $familiarId no tiene hijos asociados.")
                return@withContext Result.Success(emptyList())
            }

            // 2. Consultar los alumnos usando los IDs obtenidos (máximo 10 IDs por consulta 'in')
            // Firestore limita las consultas 'in' a 10 elementos. Si hay más hijos, hay que dividir la consulta.
            if (hijosIdsList.size > 10) {
                 Timber.w("La consulta 'in' de Firestore está limitada a 10 IDs. Se necesitan múltiples consultas para $familiarId.")
                 // Implementación simplificada: Por ahora, solo tomamos los primeros 10.
                 // Una implementación completa dividiría hijosIds en chunks de 10 y haría múltiples consultas.
                 val primerosDiezIds = hijosIdsList.take(10)
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                     .whereIn(com.google.firebase.firestore.FieldPath.documentId(), primerosDiezIds)
                     .get()
                     .await()
                 val alumnos = query.toObjects(Alumno::class.java)
                 return@withContext Result.Success(alumnos)

            } else {
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), hijosIdsList)
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
            val hijosIds = familiarDoc.get("hijosIds")
            val hijosIdsList = when {
                hijosIds is List<*> -> hijosIds.filterIsInstance<String>()
                else -> emptyList()
            }

            if (hijosIdsList.isEmpty()) {
                Timber.i("Familiar con ID $familiarId no tiene hijos asociados.")
                return@withContext Result.Success(emptyList())
            }

            // 2. Consultar los alumnos usando los IDs obtenidos (máximo 10 IDs por consulta 'in')
            // Firestore limita las consultas 'in' a 10 elementos. Si hay más hijos, hay que dividir la consulta.
            if (hijosIdsList.size > 10) {
                 Timber.w("La consulta 'in' de Firestore está limitada a 10 IDs. Se necesitan múltiples consultas para $familiarId.")
                 // Implementación simplificada: Por ahora, solo tomamos los primeros 10.
                 // Una implementación completa dividiría hijosIds en chunks de 10 y haría múltiples consultas.
                 val primerosDiezIds = hijosIdsList.take(10)
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                     .whereIn(com.google.firebase.firestore.FieldPath.documentId(), primerosDiezIds)
                     .get()
                     .await()
                 val alumnos = query.toObjects(Alumno::class.java)
                 return@withContext Result.Success(alumnos)

            } else {
                 val query = firestore.collection(COLLECTION_ALUMNOS)
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), hijosIdsList)
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
            Timber.d("AlumnoRepositoryImpl: getAlumnosPorClase recibió claseId: $claseId")
            
            // Buscar alumnos por claseId
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("claseId", claseId)
                .get()
                .await()

            val alumnosEncontrados = query.toObjects(Alumno::class.java)
            Timber.d("Encontrados ${alumnosEncontrados.size} alumnos con claseId=$claseId")
            
            // Obtener la clase para verificar los alumnos listados en alumnosIds
            val alumnosDesdeClase = try {
                val claseDoc = firestore.collection("clases").document(claseId).get().await()
                if (claseDoc.exists()) {
                    Timber.d("AlumnoRepositoryImpl: Documento de clase $claseId encontrado.")
                    val alumnosIdsAny = claseDoc.get("alumnosIds")
                    val alumnosIds = when {
                        alumnosIdsAny is List<*> -> alumnosIdsAny.filterIsInstance<String>().toMutableList()
                        else -> mutableListOf()
                    }
                    Timber.d("AlumnoRepositoryImpl: La clase $claseId tiene los siguientes alumnosIds: $alumnosIds")
                    
                    if (alumnosIds.isNotEmpty()) {
                        // Obtener alumnos por sus IDs
                        val alumnosEncontrados = mutableListOf<Alumno>()
                        for (id in alumnosIds) {
                            try {
                                Timber.d("AlumnoRepositoryImpl: Buscando alumno con DNI: $id (desde alumnosIds de la clase $claseId)")
                                val alumnoQuery = firestore.collection(COLLECTION_ALUMNOS)
                                    .whereEqualTo("dni", id)
                                    .limit(1)
                                    .get()
                                    .await()
                                    
                                if (!alumnoQuery.isEmpty) {
                                    val alumno = alumnoQuery.toObjects(Alumno::class.java).first()
                                    if (alumno != null) {
                                        Timber.d("AlumnoRepositoryImpl: Encontrado alumno: ${alumno.nombre} ${alumno.apellidos} con DNI $id")
                                        alumnosEncontrados.add(alumno)
                                    } else {
                                        Timber.w("AlumnoRepositoryImpl: Alumno con DNI $id encontrado pero no se pudo convertir a objeto.")
                                    }
                                } else {
                                    Timber.w("AlumnoRepositoryImpl: No se encontró alumno con DNI: $id (listado en clase $claseId)")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "AlumnoRepositoryImpl: Error al obtener alumno con DNI $id")
                            }
                        }
                        alumnosEncontrados
                    } else {
                        Timber.d("AlumnoRepositoryImpl: La clase $claseId no tiene alumnosIds.")
                        emptyList()
                    }
                } else {
                    Timber.w("AlumnoRepositoryImpl: No se encontró el documento de la clase con ID: $claseId")
                    emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "AlumnoRepositoryImpl: Error al obtener alumnos desde la clase $claseId")
                emptyList()
            }

            // Combinar resultados eliminando duplicados
            val todosLosAlumnos = (alumnosEncontrados + alumnosDesdeClase).distinctBy { it.dni }
            
            Timber.d("Total de ${todosLosAlumnos.size} alumnos encontrados para la clase $claseId")
            return@withContext todosLosAlumnos
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
            Timber.d("Actualizando profesor $profesorId para alumno $alumnoDni")
            
            // Buscar el documento del alumno por su DNI
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", alumnoDni)
                .limit(1)
                .get()
                .await()
                
            if (query.isEmpty) {
                Timber.e("No se encontró el alumno con DNI: $alumnoDni")
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            // Obtener la referencia al documento y actualizar el campo profesorId
            val documentRef = query.documents.first().reference
            documentRef.update("profesorId", profesorId).await()
            
            // También guardarlo en el array profesorIds si existe
            documentRef.update("profesorIds", com.google.firebase.firestore.FieldValue.arrayUnion(profesorId)).await()
            
            Timber.d("Profesor actualizado correctamente para el alumno $alumnoDni")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar profesor para alumno $alumnoDni")
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
    override suspend fun getAlumnoByDni(dni: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            Timber.d("AlumnoRepository: Buscando alumno con DNI: $dni")
            
            if (dni.isBlank()) {
                Timber.e("AlumnoRepository: El DNI está vacío")
                return@withContext Result.Error(Exception("DNI vacío o nulo"))
            }
            
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", dni)
                .limit(1)
                .get()
                .await()

            Timber.d("AlumnoRepository: Consulta ejecutada para DNI '$dni'. Documentos encontrados: ${query.size()}")
            
            if (!query.isEmpty) {
                val alumno = query.toObjects(Alumno::class.java).first()
                if (alumno != null) {
                    Timber.d("AlumnoRepository: Alumno encontrado: ${alumno.nombre} ${alumno.apellidos}")
                    return@withContext Result.Success(alumno)
                } else {
                    Timber.e("AlumnoRepository: No se pudo convertir el documento a Alumno")
                    return@withContext Result.Error(Exception("Error al convertir el documento a Alumno"))
                }
            } else {
                Timber.e("AlumnoRepository: No se encontró ningún alumno con DNI: $dni")
                
                // Intento de búsqueda alternativa usando el DNI como ID del documento
                try {
                    Timber.d("AlumnoRepository: Intentando búsqueda alternativa con DNI como ID del documento")
                    val docSnapshot = firestore.collection(COLLECTION_ALUMNOS).document(dni).get().await()
                    
                    if (docSnapshot.exists()) {
                        val alumno = docSnapshot.toObject(Alumno::class.java)
                        if (alumno != null) {
                            Timber.d("AlumnoRepository: Alumno encontrado con búsqueda alternativa: ${alumno.nombre} ${alumno.apellidos}")
                            return@withContext Result.Success(alumno)
                        }
                    }
                    
                    Timber.e("AlumnoRepository: La búsqueda alternativa también falló para DNI: $dni")
                } catch (e: Exception) {
                    Timber.e(e, "AlumnoRepository: Error en búsqueda alternativa por ID: $dni")
                }
                
                return@withContext Result.Error(Exception("No se encontró ningún alumno con DNI: $dni"))
            }
        } catch (e: Exception) {
            Timber.e(e, "AlumnoRepository: Error al obtener alumno por DNI: $dni")
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

    /**
     * Obtiene todos los alumnos de un centro
     * @param centroId ID del centro
     * @return Lista de alumnos del centro
     */
    override suspend fun getAlumnosByCentroId(centroId: String): List<Alumno> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("centroId", centroId)
                .get()
                .await()

            Timber.d("Recuperados ${query.size()} alumnos para el centro: $centroId")
            return@withContext query.toObjects(Alumno::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener alumnos por centro: $centroId")
            return@withContext emptyList()
        }
    }

    /**
     * Busca alumnos que tengan a un familiar en su lista de familiares
     * 
     * Este método busca alumnos cuyo campo familiarIds contiene el ID del familiar.
     * Es una búsqueda alternativa para cuando la relación principal falla.
     * 
     * @param familiarId ID del familiar a buscar en la lista de familiares de los alumnos
     * @return Resultado con la lista de alumnos encontrados
     */
    override suspend fun getAlumnosWithFamiliarId(familiarId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando alumnos con familiar ID: $familiarId en lista familiarIds")
            
            // Consulta alumnos donde el array familiarIds contiene el ID dado
            val querySnapshot = firestore.collection(COLLECTION_ALUMNOS)
                .whereArrayContains("familiarIds", familiarId)
                .get()
                .await()
            
            // Mapea los documentos a objetos Alumno
            val alumnos = querySnapshot.documents.mapNotNull { document ->
                try {
                    val alumno = document.toObject(Alumno::class.java)
                    if (alumno != null) {
                        // Asegurarse de asignar el ID del documento si no está presente
                        if (alumno.id.isEmpty()) {
                            alumno.copy(id = document.id)
                        } else {
                            alumno
                        }
                    } else {
                        Timber.w("No se pudo convertir documento ${document.id} a Alumno")
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al convertir documento a Alumno: ${document.id}")
                    null
                }
            }
            
            // Si no encontramos alumnos, intentemos buscar por DNI del familiar
            if (alumnos.isEmpty()) {
                Timber.d("No se encontraron alumnos por familiarIds, intentando por campo 'familiares'")
                
                // Consultar todos los alumnos y filtrar aquellos que tengan el familiarId en su lista de familiares
                val todosLosAlumnos = firestore.collection(COLLECTION_ALUMNOS)
                    .get()
                    .await()
                    
                val alumnosConFamiliar = todosLosAlumnos.documents.mapNotNull { document ->
                    try {
                        val alumno = document.toObject(Alumno::class.java)
                        val familiares = document.get("familiares")
                        val familiaresList = if (familiares is List<*>) {
                            familiares.filterIsInstance<Map<String, Any>>()
                        } else {
                            emptyList()
                        }
                        
                        // Comprobar si algún familiar tiene el ID buscado
                        val tieneFamiliar = familiaresList.any { familiar ->
                            val id = familiar["id"] as? String ?: ""
                            id == familiarId
                        }
                        
                        if (tieneFamiliar) {
                            Timber.d("Encontrado alumno ${alumno?.nombre} con familiar $familiarId en campo 'familiares'")
                            alumno?.copy(id = document.id)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al procesar alumno: ${document.id}")
                        null
                    }
                }
                
                Timber.d("Búsqueda alternativa encontró ${alumnosConFamiliar.size} alumnos con familiar $familiarId")
                return@withContext Result.Success(alumnosConFamiliar)
            }
            
            Timber.d("Encontrados ${alumnos.size} alumnos con familiar ID: $familiarId")
            return@withContext Result.Success(alumnos)
            
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar alumnos con familiar ID: $familiarId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza la referencia a la clase en un alumno (asignación)
     * @param alumnoId ID (DNI) del alumno
     * @param claseId ID de la clase a asignar
     * @return Resultado de la operación
     */
    override suspend fun asignarClaseAAlumno(alumnoId: String, claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Actualizando clase $claseId para alumno $alumnoId")
            
            // Buscar el documento del alumno por su DNI
            val query = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", alumnoId)
                .limit(1)
                .get()
                .await()
                
            if (query.isEmpty) {
                Timber.e("No se encontró el alumno con DNI: $alumnoId")
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            // Obtener la referencia al documento y actualizar el campo claseId
            val documentRef = query.documents.first().reference
            documentRef.update("claseId", claseId).await()
            
            Timber.d("Clase actualizada correctamente para el alumno $alumnoId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar clase para alumno $alumnoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Elimina la referencia a la clase en un alumno (desasignación)
     * @param alumnoId ID (DNI) del alumno
     * @param claseId ID de la clase a desasignar
     * @return Resultado de la operación
     */
    override suspend fun desasignarClaseDeAlumno(alumnoId: String, claseId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Primero registramos la operación para mejor seguimiento
            Timber.d("AlumnoRepository: Desasignando clase $claseId del alumno con DNI $alumnoId")
            
            // Intentar varias formas de buscar al alumno
            var encontrado = false
            
            // 1. Búsqueda primaria por DNI
            Timber.d("AlumnoRepository: Buscando alumno por DNI=$alumnoId")
            val queryDni = firestore.collection(COLLECTION_ALUMNOS)
                .whereEqualTo("dni", alumnoId)
                .limit(1)
                .get()
                .await()
                
            if (!queryDni.isEmpty) {
                Timber.d("AlumnoRepository: Alumno encontrado por DNI")
                val documentSnapshot = queryDni.documents.first()
                val docRef = documentSnapshot.reference
                
                // Eliminar referencias a la clase
                desasignarClaseDeDocumento(documentSnapshot, docRef, claseId)
                encontrado = true
            } else {
                Timber.w("AlumnoRepository: No se encontró alumno con DNI=$alumnoId, intentando búsqueda alternativa")
                
                // 2. Búsqueda alternativa: usar id directo
                try {
                    val docSnapshot = firestore.collection(COLLECTION_ALUMNOS).document(alumnoId).get().await()
                    if (docSnapshot.exists()) {
                        Timber.d("AlumnoRepository: Alumno encontrado por ID directo")
                        val docRef = docSnapshot.reference
                        
                        // Eliminar referencias a la clase
                        desasignarClaseDeDocumento(docSnapshot, docRef, claseId)
                        encontrado = true
                    }
                } catch (e: Exception) {
                    Timber.e(e, "AlumnoRepository: Error en búsqueda alternativa por ID")
                }
                
                // 3. Otra búsqueda alternativa: por cualquier campo que pueda identificar al alumno
                if (!encontrado) {
                    val queryAlternativa = firestore.collection(COLLECTION_ALUMNOS)
                        .whereEqualTo("id", alumnoId)
                        .limit(1)
                        .get()
                .await()
            
                    if (!queryAlternativa.isEmpty) {
                        Timber.d("AlumnoRepository: Alumno encontrado por campo ID")
                        val documentSnapshot = queryAlternativa.documents.first()
                        val docRef = documentSnapshot.reference
                        
                        // Eliminar referencias a la clase
                        desasignarClaseDeDocumento(documentSnapshot, docRef, claseId)
                        encontrado = true
                    }
                }
            }
            
            if (!encontrado) {
                // Si el alumno no se encontró, pero es simplemente porque ya no existe,
                // consideramos que no está vinculado a la clase, así que el resultado es exitoso
                Timber.i("AlumnoRepository: No se pudo encontrar el alumno $alumnoId, pero se considera desvinculado de la clase")
                return@withContext Result.Success(Unit)
            }
            
            Timber.d("AlumnoRepository: Clase $claseId desasignada correctamente del alumno $alumnoId")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AlumnoRepository: Error al desasignar clase $claseId del alumno $alumnoId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Método auxiliar para eliminar referencias a una clase en un documento de alumno
     */
    private suspend fun desasignarClaseDeDocumento(documentSnapshot: com.google.firebase.firestore.DocumentSnapshot, docRef: com.google.firebase.firestore.DocumentReference, claseId: String) {
        // Verificar el contenido actual para debugging
        val claseIdField = documentSnapshot.getString("claseId")
        val claseIdsAny = documentSnapshot.get("claseIds")
        val claseIds = when {
            claseIdsAny is List<*> -> claseIdsAny.filterIsInstance<String>()
            else -> emptyList()
        }
        
        Timber.d("AlumnoRepository: Estado actual - claseId=$claseIdField, claseIds=$claseIds")
        
        // 1. Eliminar el campo claseId si coincide con la clase que queremos desasignar
        if (claseIdField == claseId) {
            docRef.update("claseId", null).await()
            Timber.d("AlumnoRepository: Campo claseId eliminado")
        }
        
        // 2. Eliminar el ID de la clase del array claseIds si existe
        if (claseIds.contains(claseId)) {
            docRef.update("claseIds", com.google.firebase.firestore.FieldValue.arrayRemove(claseId)).await()
            Timber.d("AlumnoRepository: ClaseId eliminado del array claseIds")
        }
    }
    
    /**
     * Sincroniza todos los alumnos existentes con sus clases correspondientes
     * Este método busca todos los alumnos que tienen una clase asignada (aulaId o claseId)
     * y actualiza el array alumnosIds de esas clases
     * @return Resultado de la operación
     */
    override suspend fun sincronizarAlumnosConClases(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("🔄 Iniciando sincronización de alumnos con clases")
            
            // 1. Obtener todos los alumnos
            val alumnosSnapshot = firestore.collection(COLLECTION_ALUMNOS).get().await()
            val alumnos = alumnosSnapshot.toObjects(Alumno::class.java)
            
            Timber.d("📊 Encontrados ${alumnos.size} alumnos para sincronizar")
            
            // 2. Agrupar alumnos por clase
            val alumnosPorClase = mutableMapOf<String, MutableList<String>>()
            
            for (alumno in alumnos) {
                // Verificar claseId
                if (!alumno.claseId.isNullOrBlank()) {
                    val claseId = alumno.claseId
                    if (!alumnosPorClase.containsKey(claseId)) {
                        alumnosPorClase[claseId] = mutableListOf()
                    }
                    alumnosPorClase[claseId]!!.add(alumno.dni)
                    Timber.d("📝 Alumno ${alumno.dni} asignado a clase $claseId")
                }
            }
            
            Timber.d("🏫 Encontradas ${alumnosPorClase.size} clases con alumnos asignados")
            
            // 3. Actualizar cada clase con su lista de alumnos
            var clasesActualizadas = 0
            var errores = 0
            
            for ((claseId, alumnosIds) in alumnosPorClase) {
                try {
                    val claseRef = firestore.collection("clases").document(claseId)
                    val claseDoc = claseRef.get().await()
                    
                    if (claseDoc.exists()) {
                        // Obtener la lista actual de alumnosIds
                        val alumnosIdsActuales = when (val alumnosIdsAny = claseDoc.get("alumnosIds")) {
                            is List<*> -> alumnosIdsAny.filterIsInstance<String>()
                            else -> emptyList()
                        }
                        
                        // Combinar y eliminar duplicados
                        val alumnosIdsCombinados = (alumnosIdsActuales + alumnosIds).distinct()
                        
                        if (alumnosIdsCombinados != alumnosIdsActuales) {
                            // Actualizar la clase
                            claseRef.update("alumnosIds", alumnosIdsCombinados).await()
                            clasesActualizadas++
                            
                            Timber.d("✅ Clase $claseId actualizada: ${alumnosIdsActuales.size} -> ${alumnosIdsCombinados.size} alumnos")
                            Timber.d("   Alumnos: $alumnosIdsCombinados")
                        } else {
                            Timber.d("ℹ️ Clase $claseId ya estaba sincronizada")
                        }
                    } else {
                        Timber.w("⚠️ Clase $claseId no existe en Firestore")
                        errores++
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al actualizar clase $claseId")
                    errores++
                }
            }
            
            Timber.d("🎉 Sincronización completada: $clasesActualizadas clases actualizadas, $errores errores")
            
            if (errores > 0) {
                return@withContext Result.Error(Exception("Sincronización completada con $errores errores"))
            } else {
                return@withContext Result.Success(Unit)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "💥 Error general en la sincronización de alumnos con clases")
            return@withContext Result.Error(e)
        }
    }
} 