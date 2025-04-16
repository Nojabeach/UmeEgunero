package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Familiar
import com.tfg.umeegunero.data.model.Perfil
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.data.model.Usuario
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar los familiares de alumnos
 *
 * Esta interfaz define las operaciones relacionadas con la gestión de familiares
 * y sus vinculaciones con alumnos del centro educativo.
 */
interface FamiliarRepository {
    /**
     * Obtiene todos los familiares del sistema
     */
    suspend fun getFamiliares(): Result<List<Usuario>>
    
    /**
     * Obtiene los familiares vinculados a un alumno específico
     * @param alumnoId Identificador del alumno
     */
    suspend fun getFamiliaresByAlumnoId(alumnoId: String): Result<List<Usuario>>
    
    /**
     * Vincula un familiar a un alumno con un tipo de parentesco
     * @param familiarId Identificador del familiar
     * @param alumnoId Identificador del alumno
     * @param parentesco Tipo de relación (PADRE, MADRE, TUTOR, etc.)
     */
    suspend fun vincularFamiliarAlumno(familiarId: String, alumnoId: String, parentesco: String): Result<Unit>
    
    /**
     * Desvincula un familiar de un alumno
     * @param familiarId Identificador del familiar
     * @param alumnoId Identificador del alumno
     */
    suspend fun desvincularFamiliarAlumno(familiarId: String, alumnoId: String): Result<Unit>
    
    /**
     * Crea un nuevo familiar en el sistema
     * @param nombre Nombre del familiar
     * @param apellidos Apellidos del familiar
     * @param dni DNI del familiar (se usará como ID)
     * @param email Correo electrónico
     * @param telefono Teléfono de contacto
     */
    suspend fun crearFamiliar(
        nombre: String, 
        apellidos: String, 
        dni: String, 
        email: String, 
        telefono: String
    ): Result<String>
}

/**
 * Implementación del repositorio de familiares
 */
@Singleton
class FamiliarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FamiliarRepository {
    
    private val usuariosCollection = firestore.collection("usuarios")
    private val alumnosCollection = firestore.collection("alumnos")
    private val vinculacionesCollection = firestore.collection("vinculaciones_familiar_alumno")
    
    override suspend fun getFamiliares(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usuariosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            val familiares = snapshot.documents.mapNotNull { doc ->
                val familiar = doc.toObject<Usuario>()
                // Filtramos para incluir solo usuarios con perfil familiar
                if (familiar != null && familiar.perfiles.any { it.tipo == TipoUsuario.FAMILIAR }) {
                    familiar.copy(dni = doc.id)
                } else {
                    null
                }
            }
            
            Result.Success(familiares)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener familiares")
            Result.Error(e)
        }
    }
    
    override suspend fun getFamiliaresByAlumnoId(alumnoId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Obtener el documento del alumno
            val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
            
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("El alumno no existe"))
            }
            
            // Obtener los IDs de los familiares vinculados
            val alumno = alumnoDoc.toObject<Alumno>()
            val familiarIds = alumno?.familiarIds ?: emptyList()
            
            if (familiarIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            // Obtener los datos de cada familiar
            val familiares = familiarIds.mapNotNull { familiarId ->
                val familiarDoc = usuariosCollection.document(familiarId).get().await()
                if (familiarDoc.exists()) {
                    val familiar = familiarDoc.toObject<Usuario>()
                    familiar?.copy(dni = familiarDoc.id)
                } else {
                    null
                }
            }
            
            Result.Success(familiares)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener familiares del alumno $alumnoId")
            Result.Error(e)
        }
    }
    
    override suspend fun vincularFamiliarAlumno(
        familiarId: String, 
        alumnoId: String, 
        parentesco: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que ambos existen
            val familiarDoc = usuariosCollection.document(familiarId).get().await()
            val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
            
            if (!familiarDoc.exists()) {
                return@withContext Result.Error(Exception("El familiar no existe"))
            }
            
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("El alumno no existe"))
            }
            
            // Actualizar la lista de familiares del alumno
            val alumno = alumnoDoc.toObject<Alumno>()
            val familiaresActuales = alumno?.familiarIds?.toMutableList() ?: mutableListOf()
            
            if (!familiaresActuales.contains(familiarId)) {
                familiaresActuales.add(familiarId)
                
                // Actualizar el alumno
                alumnosCollection.document(alumnoId)
                    .update("familiarIds", familiaresActuales)
                    .await()
                
                // Crear registro de vinculación con el parentesco
                val vinculacionId = "${familiarId}_${alumnoId}"
                val vinculacion = hashMapOf(
                    "familiarId" to familiarId,
                    "alumnoId" to alumnoId,
                    "parentesco" to parentesco,
                    "fechaCreacion" to com.google.firebase.Timestamp.now()
                )
                
                vinculacionesCollection.document(vinculacionId)
                    .set(vinculacion)
                    .await()
                
                // Añadir a la lista de familiares del alumno si no existe
                val familiaresLista = alumno?.familiares?.toMutableList() ?: mutableListOf()
                
                // Verificar si el familiar ya está en la lista
                if (!familiaresLista.any { it.id == familiarId }) {
                    val familiar = familiarDoc.toObject<Usuario>()
                    
                    familiaresLista.add(
                        Familiar(
                            id = familiarId,
                            nombre = familiar?.nombre ?: "",
                            apellidos = familiar?.apellidos ?: "",
                            parentesco = parentesco
                        )
                    )
                    
                    alumnosCollection.document(alumnoId)
                        .update("familiares", familiaresLista)
                        .await()
                }
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al vincular familiar $familiarId con alumno $alumnoId")
            Result.Error(e)
        }
    }
    
    override suspend fun desvincularFamiliarAlumno(
        familiarId: String, 
        alumnoId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el alumno existe
            val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
            
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("El alumno no existe"))
            }
            
            // Actualizar la lista de familiares del alumno
            val alumno = alumnoDoc.toObject<Alumno>()
            val familiaresActuales = alumno?.familiarIds?.toMutableList() ?: mutableListOf()
            
            if (familiaresActuales.contains(familiarId)) {
                familiaresActuales.remove(familiarId)
                
                // Actualizar el alumno
                alumnosCollection.document(alumnoId)
                    .update("familiarIds", familiaresActuales)
                    .await()
                
                // Eliminar registro de vinculación
                val vinculacionId = "${familiarId}_${alumnoId}"
                vinculacionesCollection.document(vinculacionId)
                    .delete()
                    .await()
                
                // Eliminar de la lista de familiares del alumno
                val familiaresLista = alumno?.familiares?.toMutableList() ?: mutableListOf()
                familiaresLista.removeIf { it.id == familiarId }
                
                alumnosCollection.document(alumnoId)
                    .update("familiares", familiaresLista)
                    .await()
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al desvincular familiar $familiarId de alumno $alumnoId")
            Result.Error(e)
        }
    }
    
    override suspend fun crearFamiliar(
        nombre: String, 
        apellidos: String, 
        dni: String, 
        email: String, 
        telefono: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Verificar que no existe un usuario con ese DNI
            val usuarioExistente = usuariosCollection.document(dni).get().await()
            
            if (usuarioExistente.exists()) {
                return@withContext Result.Error(Exception("Ya existe un usuario con ese DNI"))
            }
            
            // Crear el nuevo familiar
            val nuevoFamiliar = Usuario(
                dni = dni,
                nombre = nombre,
                apellidos = apellidos,
                email = email,
                telefono = telefono,
                perfiles = listOf(Perfil(tipo = TipoUsuario.FAMILIAR)),
                activo = true
            )
            
            // Guardar en Firestore
            usuariosCollection.document(dni)
                .set(nuevoFamiliar)
                .await()
            
            Result.Success(dni)
        } catch (e: Exception) {
            Timber.e(e, "Error al crear familiar")
            Result.Error(e)
        }
    }
} 