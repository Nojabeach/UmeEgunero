package com.tfg.umeegunero.data.repository

import com.google.firebase.firestore.FieldPath
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
 * Repositorio para gestionar información de familiares en la aplicación UmeEgunero.
 *
 * Esta clase proporciona métodos para crear, recuperar, actualizar y eliminar
 * información de familiares, permitiendo una gestión integral de las relaciones
 * familiares en el contexto educativo.
 *
 * Características principales:
 * - Registro y gestión de perfiles de familiares
 * - Vinculación de familiares con alumnos
 * - Control de permisos y roles familiares
 * - Seguimiento de comunicaciones y notificaciones
 * - Gestión de autorizaciones y consentimientos
 *
 * El repositorio permite:
 * - Crear y actualizar perfiles de familiares
 * - Gestionar vínculos familiares-alumnos
 * - Consultar información de familiares
 * - Mantener un registro de relaciones familiares
 * - Facilitar la comunicación entre familia y centro
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para identificar al usuario actual
 * @property centroRepository Repositorio de centros para obtener contexto
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
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

    /**
     * Obtiene un familiar por su ID de usuario
     * @param usuarioId Identificador del usuario
     * @return Información del familiar o null si no existe
     */
    suspend fun getFamiliarByUsuarioId(usuarioId: String): Result<Familiar?>

    /**
     * Obtiene los IDs de los hijos vinculados a un familiar directamente desde Firestore
     * 
     * @param familiarId ID del familiar
     * @return Lista de IDs de hijos o null si hay error o no existen
     */
    suspend fun obtenerHijosIdsPorFamiliarId(familiarId: String): List<String>?
    
    /**
     * Busca directamente en la colección vinculaciones_familiar_alumno
     * 
     * @param familiarId ID del familiar
     * @return Resultado con la lista de IDs de alumnos vinculados
     */
    suspend fun getAlumnoIdsByVinculaciones(familiarId: String): Result<List<String>>
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

    override suspend fun getFamiliarByUsuarioId(usuarioId: String): Result<Familiar?> = withContext(Dispatchers.IO) {
        try {
            val familiarDoc = usuariosCollection.document(usuarioId).get().await()
            
            if (!familiarDoc.exists()) {
                return@withContext Result.Error(Exception("El familiar no existe"))
            }
            
            val familiar = familiarDoc.toObject<Familiar>()
            
            Result.Success(familiar)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener familiar por ID de usuario")
            Result.Error(e)
        }
    }

    override suspend fun obtenerHijosIdsPorFamiliarId(familiarId: String): List<String>? = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando hijos IDs para familiar: $familiarId")
            
            // Intentar obtener directamente del documento del familiar
            val familiarDoc = firestore.collection("familiares")
                .whereEqualTo("id", familiarId)
                .limit(1)
                .get()
                .await()
                
            if (!familiarDoc.isEmpty) {
                val doc = familiarDoc.documents.first()
                val hijosIds = doc.get("hijosIds") as? List<String>
                
                if (hijosIds != null) {
                    Timber.d("Encontrados ${hijosIds.size} hijosIds en documento familiar")
                    return@withContext hijosIds
                }
                
                // Intentar otros posibles nombres de campo
                val alumnosIds = doc.get("alumnosIds") as? List<String>
                if (alumnosIds != null) {
                    Timber.d("Encontrados ${alumnosIds.size} alumnosIds en documento familiar")
                    return@withContext alumnosIds
                }
                
                // Otro posible nombre de campo
                val vinculadosIds = doc.get("vinculadosIds") as? List<String>
                if (vinculadosIds != null) {
                    Timber.d("Encontrados ${vinculadosIds.size} vinculadosIds en documento familiar")
                    return@withContext vinculadosIds
                }
            } else {
                Timber.d("No se encontró documento de familiar para ID: $familiarId")
            }
            
            // También probar en la colección usuarios
            val usuarioDoc = firestore.collection("usuarios")
                .document(familiarId)
                .get()
                .await()
                
            if (usuarioDoc.exists()) {
                val hijosIds = usuarioDoc.get("hijosIds") as? List<String>
                
                if (hijosIds != null) {
                    Timber.d("Encontrados ${hijosIds.size} hijosIds en documento usuario")
                    return@withContext hijosIds
                }
            }
            
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener hijos IDs para familiar: $familiarId")
            return@withContext null
        }
    }

    override suspend fun getAlumnoIdsByVinculaciones(familiarId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando vinculaciones directamente para el familiar: $familiarId")
            
            val query = vinculacionesCollection
                .whereEqualTo("familiarId", familiarId)
                .get()
                .await()
            
            if (query.isEmpty) {
                // También probar buscando por documentos que empiecen con el ID del familiar
                Timber.d("No se encontraron vinculaciones por campo 'familiarId', probando por ID del documento")
                
                // Buscar documentos cuyo ID empiece con el familiarId seguido de guion bajo
                val directQuery = vinculacionesCollection
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), familiarId + "_")
                    .whereLessThanOrEqualTo(FieldPath.documentId(), familiarId + "_\uf8ff")
                    .get()
                    .await()
                
                if (directQuery.isEmpty) {
                    Timber.d("No se encontraron vinculaciones para el familiar: $familiarId")
                    return@withContext Result.Success(emptyList())
                }
                
                val alumnosIds = directQuery.documents.mapNotNull { doc ->
                    // El ID debería tener el formato "familiarId_alumnoId"
                    try {
                        val idParts = doc.id.split("_")
                        if (idParts.size == 2) {
                            val alumnoId = idParts[1]
                            Timber.d("Encontrada vinculación por ID: ${doc.id}, alumnoId: $alumnoId")
                            alumnoId
                        } else {
                            // Intentar obtener el alumnoId del campo en el documento
                            doc.getString("alumnoId")?.also {
                                Timber.d("Encontrada vinculación con campo alumnoId: $it")
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error al procesar vinculación ${doc.id}")
                        null
                    }
                }
                
                Timber.d("Encontrados ${alumnosIds.size} alumnos por ID de vinculación")
                return@withContext Result.Success(alumnosIds)
            }
            
            // Procesamos los resultados de la consulta por campo familiarId
            val alumnosIds = query.documents.mapNotNull { doc ->
                doc.getString("alumnoId")?.also {
                    Timber.d("Encontrada vinculación con campo alumnoId: $it")
                }
            }
            
            Timber.d("Encontrados ${alumnosIds.size} alumnos vinculados por familiarId")
            return@withContext Result.Success(alumnosIds)
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar vinculaciones: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
} 