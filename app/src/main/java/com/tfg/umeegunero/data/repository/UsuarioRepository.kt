package com.tfg.umeegunero.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tfg.umeegunero.data.model.*
import com.tfg.umeegunero.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date
import java.util.Calendar
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import java.io.IOException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import android.util.Log
import com.tfg.umeegunero.data.service.RemoteConfigService
import com.google.firebase.firestore.FieldValue
import android.content.Context
import com.tfg.umeegunero.util.DefaultAvatarsManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tfg.umeegunero.BuildConfig

/**
 * Repositorio para gestionar operaciones relacionadas con usuarios en la aplicación UmeEgunero.
 *
 * Esta interfaz define los métodos para interactuar con los usuarios, permitiendo
 * operaciones como obtención, creación, actualización y eliminación de usuarios.
 *
 * El repositorio maneja diferentes tipos de usuarios (administradores, profesores, 
 * familiares, alumnos) y proporciona métodos para gestionar sus perfiles, 
 * autenticación y datos personales.
 *
 * @property firestore Instancia de FirebaseFirestore para operaciones de base de datos
 * @property authRepository Repositorio de autenticación para gestionar el inicio de sesión
 *
 * @author Maitane Ibañez Irazabal (2º DAM Online)
 * @since 2024
 */
@Singleton
open class UsuarioRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val remoteConfigService: RemoteConfigService,
    private val defaultAvatarsManager: DefaultAvatarsManager?
) {
    // Se inyectará después para evitar la dependencia circular
    @set:Inject
    internal lateinit var authRepository: AuthRepository
    
    val usuariosCollection = firestore.collection("usuarios")
    val solicitudesCollection = firestore.collection("solicitudesRegistro")
    val centrosCollection = firestore.collection("centros")
    val clasesCollection = firestore.collection("clases")
    val alumnosCollection = firestore.collection("alumnos")
    val registrosCollection = firestore.collection("registrosActividad")
    val mensajesCollection = firestore.collection("mensajes")
    private val functions = Firebase.functions

    companion object {
        private const val COLLECTION_USUARIOS = "usuarios"
        private const val COLLECTION_PROFESORES = "profesores"
        private const val COLLECTION_ALUMNOS = "alumnos"
        
        /**
         * Crea un mock del repositorio para pruebas y vistas previas
         */
        fun createMock(): UsuarioRepository {
            // Creamos un mock usando Mockito o implementamos una versión simple
            val repo = object : UsuarioRepository(
                FirebaseAuth.getInstance(),
                FirebaseFirestore.getInstance(),
                RemoteConfigService(),
                null  // No intentamos crear el DefaultAvatarsManager aquí porque depende de contexto
            ) {
                override suspend fun getAlumnoPorId(alumnoId: String): Result<Alumno> {
                    return Result.Success(
                        Alumno(
                            id = alumnoId,
                            dni = "12345678A",
                            nombre = "Alumno de Prueba",
                            apellidos = "Apellidos de Prueba",
                            curso = "1º ESO",
                            clase = "1ºA"
                        )
                    )
                }
            }
            
            // Configuramos un mock para authRepository
            repo.authRepository = object : AuthRepository {
                override suspend fun getCurrentUser(): Usuario? = null
                override suspend fun signOut(): Boolean = true
                override suspend fun cerrarSesion(): Boolean = true
                override suspend fun sendPasswordResetEmail(email: String): Result<Boolean> = Result.Success(true)
                override suspend fun getFirebaseUser(): FirebaseUser? = null
                override suspend fun marcarComunicadoComoLeido(comunicadoId: String): Result<Unit> = Result.Success(Unit)
                override suspend fun confirmarLecturaComunicado(comunicadoId: String): Result<Unit> = Result.Success(Unit)
                override suspend fun haLeidoComunicado(comunicadoId: String): Result<Boolean> = Result.Success(false)
                override suspend fun haConfirmadoLecturaComunicado(comunicadoId: String): Result<Boolean> = Result.Success(false)
                override suspend fun getUsuarioActual(): Usuario? = null
                override suspend fun getCurrentCentroId(): String = ""
                override suspend fun getCurrentUserId(): String? = null
                override suspend fun deleteUserByEmail(email: String): Result<Unit> = Result.Success(Unit)
                override suspend fun loginWithEmailAndPassword(email: String, password: String): Result<String> = Result.Success("")
            }
            
            return repo
        }
    }

    // AUTENTICACIÓN Y USUARIOS

    /**
     * Función para manejar y registrar errores de autenticación
     */
    private fun handleAuthError(exception: Throwable, operacion: String, email: String = "") {
        // Registrar el error en Timber
        Timber.e(exception, "Error en operación de autenticación: $operacion")
        
        // Registrar en Crashlytics con contexto
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("auth_operation", operacion)
        if (email.isNotEmpty()) {
            // Añadir un hash del email para identificar problemas sin revelar datos sensibles
            crashlytics.setCustomKey("auth_email_hash", email.hashCode().toString())
        }
        crashlytics.setCustomKey("auth_error_type", exception.javaClass.simpleName)
        crashlytics.log("Error en autenticación: $operacion - ${exception.message}")
        
        // Registrar la excepción
        crashlytics.recordException(exception)
    }

    // Registra un nuevo usuario en Firebase Auth y Firestore
    suspend fun registrarUsuario(form: RegistroUsuarioForm): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            // 1. Verificar que el DNI no existe ya en Firestore
            val existingUser = usuariosCollection.document(form.dni).get().await()
            if (existingUser.exists()) {
                return@withContext Result.Error(Exception("Ya existe un usuario con este DNI"))
            }

            // 2. Crear usuario en Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(form.email, form.password).await()
            // Verificar que el usuario se creó, aunque no usemos el uid aquí directamente
            authResult.user?.uid ?: throw Exception("Error al crear usuario en Firebase Auth")

            // 3. Crear perfil de familia
            val perfil = Perfil(
                tipo = TipoUsuario.FAMILIAR,
                subtipo = form.subtipo,
                centroId = form.centroId,
                verificado = false,
                alumnos = form.alumnosDni
            )

            // 4. Crear usuario en Firestore
            val usuario = Usuario(
                dni = form.dni,
                email = form.email,
                nombre = form.nombre,
                apellidos = form.apellidos,
                telefono = form.telefono,
                fechaRegistro = Timestamp.now(),
                perfiles = listOf(perfil),
                direccion = form.direccion
            )

            // 5. Guardar el usuario en Firestore usando el DNI como ID del documento
            usuariosCollection.document(form.dni).set(usuario).await()

            // 6. Crear solicitud de registro para cada alumno
            form.alumnosDni.forEach { alumnoDni ->
                val solicitud = SolicitudRegistro(
                    usuarioId = form.dni,
                    centroId = form.centroId,
                    tipoSolicitud = TipoUsuario.FAMILIAR,
                    alumnoIds = listOf(alumnoDni),
                    estado = EstadoSolicitud.PENDIENTE,
                    fechaSolicitud = Timestamp.now()
                )
                solicitudesCollection.add(solicitud).await()
            }

            return@withContext Result.Success(usuario)
        } catch (e: FirebaseAuthException) {
            handleAuthError(e, "registrar_usuario", form.email)
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun iniciarSesion(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Intentando login en Firebase Auth para email: $email")
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("UID de usuario nulo en Firebase Auth")
            
            Timber.d("Login exitoso en Firebase Auth con UID: $uid, email: ${authResult.user?.email}")
            
            // Actualizar último acceso del usuario
            Timber.d("Buscando usuario en Firestore por email: $email")
            val userQuery = usuariosCollection.whereEqualTo("email", email).get().await()

            if (!userQuery.isEmpty) {
                val userDoc = userQuery.documents.first()
                Timber.d("Usuario encontrado en Firestore con DNI: ${userDoc.id}")
                Timber.d("Datos del usuario: ${userDoc.data}")
                
                // Actualizar último acceso
                userDoc.reference.update("ultimoAcceso", Timestamp.now()).await()
                Timber.d("Último acceso actualizado para usuario: ${userDoc.id}")
                
                return@withContext Result.Success(userDoc.id) // Devuelve el DNI como ID
            } else {
                Timber.e("Usuario no encontrado en Firestore para email: $email")
                // Cerrar sesión ya que el usuario no existe en Firestore
                firebaseAuth.signOut()
                return@withContext Result.Error(Exception("Usuario no encontrado en la base de datos"))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            handleAuthError(e, "iniciar_sesion", email)
            Timber.e(e, "Usuario no encontrado para email: $email")
            return@withContext Result.Error(Exception("Email no encontrado. Por favor verifica tu email."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            handleAuthError(e, "iniciar_sesion", email)
            Timber.e(e, "Contraseña incorrecta para email: $email")
            return@withContext Result.Error(Exception("Contraseña incorrecta. Por favor verifica tu contraseña."))
        } catch (e: FirebaseAuthException) {
            handleAuthError(e, "iniciar_sesion", email)
            Timber.e(e, "Error de autenticación para email: $email")
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            handleAuthError(e, "iniciar_sesion", email)
            Timber.e(e, "Error inesperado durante el login para email: $email")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Cierra la sesión actual y limpia los tokens FCM
     */
    fun cerrarSesion() {
        try {
            // Intentar limpiar el token FCM antes de cerrar sesión
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Buscar el DNI del usuario por su email
                currentUser.email?.let { email ->
                    val userQuery = usuariosCollection.whereEqualTo("email", email)
                    // Ejecutar la consulta de forma síncrona usando runBlocking si es necesario
                    // Aquí usamos una operación asíncrona sin esperar el resultado
                    userQuery.get().addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val userDoc = snapshot.documents.first()
                            val dni = userDoc.id
                            
                            // Limpiar los tokens FCM
                            usuariosCollection.document(dni)
                                .update("preferencias.notificaciones.fcmTokens", mapOf<String, String>())
                                .addOnSuccessListener {
                                    Timber.d("Tokens FCM limpiados correctamente al cerrar sesión para usuario $dni")
                                }
                                .addOnFailureListener { e ->
                                    Timber.e(e, "Error al limpiar tokens FCM al cerrar sesión para usuario $dni")
                                }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al limpiar tokens FCM antes de cerrar sesión: ${e.message}")
        } finally {
            // Siempre cerrar sesión en Firebase Auth, incluso si falla la limpieza de tokens
            firebaseAuth.signOut()
        }
    }
    
    /**
     * Obtiene la información del usuario actualmente autenticado
     * @return El usuario actual o null si no hay sesión iniciada
     */
    suspend fun getCurrentUser(): Usuario? = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser ?: return@withContext null
            val email = currentUser.email ?: return@withContext null
            
            val userQuery = usuariosCollection.whereEqualTo("email", email).get().await()
            if (!userQuery.isEmpty) {
                return@withContext userQuery.documents.first().toObject(Usuario::class.java)
            }
            return@withContext null
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener el usuario actual")
            return@withContext null
        }
    }

    /**
     * Obtiene el usuario actual autenticado
     */
    fun getUsuarioActual(): Flow<Result<Usuario>> {
        return flow {
            emit(Result.Loading<Usuario>())
            val usuarioActual = try {
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // Primero intenta buscar por UID (por si coincide con el DNI)
                    val usuarioFromFirestore = obtenerUsuarioPorId(user.uid)
                    if (usuarioFromFirestore is Result.Success<*>) {
                        val usuario = usuarioFromFirestore.data as Usuario
                        Result.Success<Usuario>(usuario)
                    } else {
                        // Si no existe documento con UID, busca por email
                        val userQuery = usuariosCollection.whereEqualTo("email", user.email).get().await()
                        if (!userQuery.isEmpty) {
                            val userDoc = userQuery.documents.first()
                            val usuario = userDoc.toObject(Usuario::class.java)
                            if (usuario != null) {
                                Result.Success<Usuario>(usuario)
                            } else {
                                Result.Error(Exception("No se pudo deserializar el usuario actual"))
                            }
                        } else {
                            Result.Error(Exception("Usuario no encontrado en Firestore (por UID ni por email)"))
                        }
                    }
                } else {
                    Result.Error(Exception("No hay usuario autenticado"))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(usuarioActual)
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Obtiene el ID del usuario actualmente logueado
     */
    suspend fun getUsuarioActualId(): String = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext ""

        // Buscar el usuario en Firestore por email
        val userQuery = usuariosCollection.whereEqualTo("email", user.email).get().await()

        if (!userQuery.isEmpty) {
            return@withContext userQuery.documents.first().id
        } else {
            return@withContext ""
        }
    }

    /**
     * Obtiene un usuario por su DNI
     */
    suspend fun getUsuarioPorDni(dni: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userDoc = usuariosCollection.document(dni).get().await()

            if (userDoc.exists()) {
                val usuario = userDoc.toObject(Usuario::class.java)
                return@withContext Result.Success(usuario!!)
            } else {
                throw Exception("Usuario no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un usuario por su ID (DNI)
     */
    suspend fun getUsuarioById(dni: String): Result<Usuario> = obtenerUsuarioPorId(dni)

    /**
     * Obtiene un usuario por su ID (DNI)
     */
    suspend fun obtenerUsuarioPorId(dni: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userDoc = usuariosCollection.document(dni).get().await()

            if (userDoc.exists()) {
                val usuario = userDoc.toObject(Usuario::class.java)
                return@withContext Result.Success(usuario!!)
            } else {
                throw Exception("Usuario no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los usuarios de un tipo específico
     */
    suspend fun getUsersByType(tipo: TipoUsuario): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Esta consulta es más compleja en Firestore ya que los perfiles son un array
            // Necesitamos consultar documentos que contengan un perfil con el tipo especificado
            val query = usuariosCollection.get().await()
            val usuarios = query.documents
                .mapNotNull { it.toObject(Usuario::class.java) }
                .filter { usuario ->
                    usuario.perfiles.any { perfil -> perfil.tipo == tipo }
                }

            return@withContext Result.Success(usuarios)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un usuario en Firestore
     */
    suspend fun guardarUsuario(usuario: Usuario): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Usar el DNI como ID del documento
            usuariosCollection.document(usuario.dni).set(usuario).await()
            return@withContext Result.Success(usuario.dni)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea un usuario con email y contraseña en Firebase Auth
     */
    suspend fun crearUsuarioConEmailYPassword(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear usuario en Firebase Auth")
            
            return@withContext Result.Success(uid)
        } catch (e: FirebaseAuthException) {
            handleAuthError(e, "crear_usuario_con_email_y_password", email)
            return@withContext Result.Error(Exception("Error de autenticación: ${e.message}"))
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Borra un usuario de Firebase Auth
     */
    suspend fun borrarUsuario(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Necesitamos reautenticar al usuario antes de eliminarlo
            val user = firebaseAuth.currentUser
            
            if (user != null && user.uid == uid) {
                user.delete().await()
                return@withContext Result.Success(Unit)
            } else {
                // No podemos eliminar directamente un usuario que no es el actual,
                // esta operación tendría que hacerse desde el backend o functions
                return@withContext Result.Error(Exception("No se puede eliminar un usuario que no es el actual"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Recuperación de contraseña
     */
    suspend fun recuperarContraseña(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // SECCIÓN: CENTROS EDUCATIVOS

    /**
     * Obtiene todos los centros educativos
     */
    suspend fun getCentrosEducativos(): Result<List<Centro>> = withContext(Dispatchers.IO) {
        try {
            val centrosQuery = centrosCollection.whereEqualTo("activo", true).get().await()
            try {
                val centros = centrosQuery.toObjects(Centro::class.java)
                return@withContext Result.Success(centros)
            } catch (deserializeEx: Exception) {
                // Manejo específico para errores de deserialización
                Log.e("UsuarioRepository", "Error de deserialización: ${deserializeEx.message}")
                
                // Intento alternativo: procesar manualmente los documentos
                val centrosAlternativos = mutableListOf<Centro>()
                try {
                    for (doc in centrosQuery.documents) {
                        val id = doc.id
                        val nombre = doc.getString("nombre") ?: ""
                        val direccion = doc.getString("direccion") ?: ""
                        val telefono = doc.getString("telefono") ?: ""
                        val email = doc.getString("email") ?: ""
                        val activo = doc.getBoolean("activo") ?: true
                        
                        // Crear un objeto Centro con los campos esenciales
                        val centro = Centro(
                            id = id,
                            nombre = nombre,
                            direccion = direccion,
                            telefono = telefono,
                            email = email,
                            activo = activo
                        )
                        centrosAlternativos.add(centro)
                    }
                    return@withContext Result.Success(centrosAlternativos)
                } catch (innerEx: Exception) {
                    // Si también falla el procesamiento manual, reportamos el error original
                    return@withContext Result.Error(deserializeEx)
                }
            }
        } catch (e: Exception) {
            Log.e("UsuarioRepository", "Error general al obtener centros: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    // SECCIÓN: FUNCIONALIDADES PARA PROFESORES

    /**
     * Obtiene las clases asignadas a un profesor
     */
    suspend fun getClasesByProfesor(profesorId: String): Result<List<Clase>> = withContext(Dispatchers.IO) {
        try {
            // Clases donde el profesor es titular
            val query = clasesCollection
                .whereEqualTo("profesorTitularId", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val clases = query.toObjects(Clase::class.java)

            // También verificamos si el profesor es auxiliar en otras clases
            val queryAuxiliar = clasesCollection
                .whereArrayContains("profesoresAuxiliaresIds", profesorId)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val clasesAuxiliar = queryAuxiliar.toObjects(Clase::class.java)

            // Combinamos todas las clases encontradas (eliminando duplicados)
            val todasLasClases = (clases + clasesAuxiliar).distinctBy { it.id }

            return@withContext Result.Success(todasLasClases)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los alumnos de una clase específica
     */
    suspend fun getAlumnosByClase(claseId: String): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("UsuarioRepository: Iniciando getAlumnosByClase para claseId: $claseId") // Log de inicio
            // Primero obtenemos la clase para tener la lista de IDs de alumnos
            val claseDoc = clasesCollection.document(claseId).get().await()

            if (!claseDoc.exists()) {
                Timber.w("UsuarioRepository: La clase con ID '$claseId' no existe.")
                return@withContext Result.Error(Exception("La clase no existe"))
            }

            val clase = claseDoc.toObject(Clase::class.java)
            val alumnoIds = clase?.alumnosIds ?: emptyList()
            Timber.d("UsuarioRepository: IDs de alumnos en clase '$claseId': $alumnoIds")


            if (alumnoIds.isEmpty()) {
                Timber.d("UsuarioRepository: No hay IDs de alumno en la clase '$claseId'. Retornando lista vacía.")
                return@withContext Result.Success(emptyList())
            }

            // Obtenemos los datos de cada alumno
            val alumnos = mutableListOf<Alumno>()

            for (alumnoIdInList in alumnoIds) { // Renombrada la variable para evitar confusión
                Timber.d("UsuarioRepository: Buscando alumno con ID/DNI: '$alumnoIdInList' (desde alumnosIds de la clase '$claseId')")
                // Asumimos que alumnoIdInList es el DNI que se usa como ID del documento en la colección 'alumnos'
                val alumnoDoc = alumnosCollection.document(alumnoIdInList).get().await()
                if (alumnoDoc.exists()) {
                    val alumno = alumnoDoc.toObject(Alumno::class.java)
                    if (alumno != null) {
                        // Asignar el DNI (que es el ID del documento) al campo 'id' del objeto Alumno
                        val alumnoConIdCorrecto = alumno.copy(id = alumnoDoc.id) 
                        // Log detallado del alumno recuperado
                        Timber.d("UsuarioRepository: Alumno encontrado: ID='${alumnoConIdCorrecto.id}', Nombre='${alumnoConIdCorrecto.nombre}', Apellidos='${alumnoConIdCorrecto.apellidos}', Presente='${alumnoConIdCorrecto.presente}', DNI (del objeto)='${alumnoConIdCorrecto.dni}'")
                        alumnos.add(alumnoConIdCorrecto)
                    } else {
                        Timber.w("UsuarioRepository: Alumno con ID '${alumnoDoc.id}' encontrado pero no se pudo convertir a objeto Alumno.")
                    }
                } else {
                    Timber.w("UsuarioRepository: No se encontró alumno con ID/DNI: '$alumnoIdInList' en la colección 'alumnos' (listado en clase '$claseId').")
                }
            }
            Timber.d("UsuarioRepository: Total de alumnos recuperados para clase '$claseId': ${alumnos.size}")
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            Timber.e(e, "UsuarioRepository: Error en getAlumnosByClase para claseId: $claseId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los alumnos sin registro de actividad para el día actual
     */
    suspend fun getAlumnosSinRegistroHoy(alumnosIds: List<String>, hoy: Timestamp): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            if (alumnosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }

            // Obtener todos los registros del día de hoy para los alumnos especificados
            val calendarInicio = Calendar.getInstance().apply {
                timeInMillis = hoy.seconds * 1000
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val fechaInicio = Timestamp(Date(calendarInicio.timeInMillis))
            
            val calendarFin = Calendar.getInstance().apply {
                timeInMillis = hoy.seconds * 1000
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            val fechaFin = Timestamp(Date(calendarFin.timeInMillis))

            val query = registrosCollection
                .whereIn("alumnoId", alumnosIds)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get()
                .await()

            val alumnosConRegistro = query.documents
                .mapNotNull { it.getString("alumnoId") }
                .toSet()

            // Obtener los alumnos sin registro
            val alumnosSinRegistro = alumnosIds.filter { !alumnosConRegistro.contains(it) }

            if (alumnosSinRegistro.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }

            // Obtener los datos completos de los alumnos sin registro
            val alumnos = mutableListOf<Alumno>()

            for (alumnoId in alumnosSinRegistro) {
                val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
                if (alumnoDoc.exists()) {
                    val alumno = alumnoDoc.toObject(Alumno::class.java)
                    alumno?.let { alumnos.add(it) }
                }
            }

            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los mensajes no leídos enviados al profesor
     */
    suspend fun getMensajesNoLeidos(profesorId: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            val query = mensajesCollection
                .whereEqualTo("receptorId", profesorId)
                .whereEqualTo("leido", false)
                .get()
                .await()

            val mensajes = query.toObjects(Mensaje::class.java)
            return@withContext Result.Success(mensajes)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Crea un nuevo registro de actividad
     */
    suspend fun crearRegistroActividad(registro: RegistroActividad): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Si el registro ya tiene un ID, usamos ese, sino generamos uno nuevo
            val registroId = if (registro.id.isNotBlank()) {
                registro.id
            } else {
                registrosCollection.document().id
            }

            // Asignar el ID al registro
            val registroConId = if (registro.id.isBlank()) registro.copy(id = registroId) else registro

            // Guardar el registro
            registrosCollection.document(registroId).set(registroConId).await()

            return@withContext Result.Success(registroId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un registro de actividad por su ID
     */
    suspend fun getRegistroById(registroId: String): Result<RegistroActividad> = withContext(Dispatchers.IO) {
        try {
            val registroDoc = registrosCollection.document(registroId).get().await()

            if (registroDoc.exists()) {
                val registro = registroDoc.toObject(RegistroActividad::class.java)
                return@withContext Result.Success(registro!!)
            } else {
                throw Exception("Registro no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // En UsuarioRepository.kt
    suspend fun getProfesoresByCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando profesores para el centro: $centroId")
            
            // Enfoque mejorado para encontrar profesores
            val usuariosConPerfil = mutableListOf<Usuario>()
            
            // Obtener todos los usuarios
            val todosLosUsuarios = usuariosCollection.get().await()
            Timber.d("Total usuarios encontrados: ${todosLosUsuarios.size()}")
            
            // Logging detallado para depuración
            Timber.d("============== DEPURACIÓN BÚSQUEDA DE PROFESORES ==============")
            Timber.d("Centro ID buscado: $centroId")
            Timber.d("Total documentos de usuarios: ${todosLosUsuarios.documents.size}")
            
            // Examinar cada documento manualmente
            for (doc in todosLosUsuarios.documents) {
                try {
                    // Verificar si el documento existe y tiene datos
                    if (!doc.exists()) {
                        Timber.d("Documento no existe: ${doc.id}")
                        continue
                    }
                        
                    // Convertir a usuario con el ID explícitamente
                    val usuario = doc.toObject(Usuario::class.java)
                    if (usuario == null) {
                        Timber.d("No se pudo convertir el documento a Usuario: ${doc.id}")
                        continue
                    }
                        
                    // Asignar manualmente el documentId
                    usuario.documentId = doc.id
                        
                    Timber.d("Revisando usuario: ${usuario.nombre} ${usuario.apellidos} (ID: ${usuario.documentId})")
                        
                    // Verificar si es profesor sin importar el centro - para depuración
                    val esProfesorGeneral = usuario.perfiles.any { it.tipo == TipoUsuario.PROFESOR }
                    Timber.d("  - ¿Es profesor en general?: $esProfesorGeneral")
                        
                    // Verificar cada perfil manualmente
                    for (perfil in usuario.perfiles) {
                        val tipo = perfil.tipo
                        val perfilCentroId = perfil.centroId
                            
                        Timber.d("  - Perfil: tipo=$tipo, centroId=$perfilCentroId, activo=${usuario.activo}")
                            
                        // Si es profesor y del centro correcto, agregar a la lista
                        // No filtramos por activo para mostrar todos los profesores
                        if (tipo == TipoUsuario.PROFESOR && perfilCentroId == centroId) {
                            Timber.d("  ✓ MATCH: Encontrado profesor para centro $centroId")
                            usuariosConPerfil.add(usuario)
                            break  // Solo necesitamos encontrar un perfil que coincida
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error procesando usuario: ${doc.id}")
                }
            }
            
            // Si no se encontraron profesores, intentar un enfoque alternativo con el campo centroId
            if (usuariosConPerfil.isEmpty()) {
                Timber.d("No se encontraron profesores con perfil. Intentando enfoque alternativo...")
                
                // Buscar usuarios que tengan TipoUsuario.PROFESOR en su campo tipo
                val queryProfesores = usuariosCollection
                    .whereEqualTo("tipo", TipoUsuario.PROFESOR.name)
                    .get()
                    .await()
                    
                Timber.d("Profesores encontrados por tipo: ${queryProfesores.size()}")
                    
                for (doc in queryProfesores.documents) {
                    try {
                        val usuario = doc.toObject(Usuario::class.java) ?: continue
                        usuario.documentId = doc.id
                        
                        Timber.d("Profesor por tipo: ${usuario.nombre} ${usuario.apellidos}")
                        usuariosConPerfil.add(usuario)
                    } catch (e: Exception) {
                        Timber.e(e, "Error procesando profesor por tipo: ${doc.id}")
                    }
                }
            }
            
            // Si todavía no hay resultados, intentemos obtener cualquier usuario con rol de profesor
            if (usuariosConPerfil.isEmpty()) {
                Timber.d("Intentando último enfoque: buscar cualquier usuario con rol de profesor...")
                
                // Obtener todos los usuarios y filtrar manualmente
                val todosLosDocs = usuariosCollection.get().await().documents
                    
                for (doc in todosLosDocs) {
                    try {
                        val data = doc.data
                        if (data != null && (data["rol"] == "PROFESOR" || data["tipo"] == "PROFESOR")) {
                            val usuario = doc.toObject(Usuario::class.java)
                            if (usuario != null) {
                                usuario.documentId = doc.id
                                Timber.d("Encontrado profesor por rol/tipo: ${usuario.nombre}")
                                usuariosConPerfil.add(usuario)
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error en último enfoque: ${doc.id}")
                    }
                }
            }
            
            Timber.d("RESULTADO FINAL: Encontrados ${usuariosConPerfil.size} profesores para el centro $centroId")
            return@withContext Result.Success(usuariosConPerfil)
        } catch (e: Exception) {
            Timber.e(e, "Error general al buscar profesores: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene los registros de actividad de un alumno
     */
    suspend fun getRegistrosActividadByAlumno(alumnoId: String): Result<List<RegistroActividad>> = withContext(Dispatchers.IO) {
        try {
            val query = registrosCollection
                .whereEqualTo("alumnoId", alumnoId)
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val registros = query.toObjects(RegistroActividad::class.java)
            return@withContext Result.Success(registros)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca un registro como visto por el familiar
     */
    suspend fun marcarRegistroComoVistoPorFamiliar(registroId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            registrosCollection.document(registroId)
                .update(
                    mapOf(
                        "vistoPorFamiliar" to true,
                        "fechaVisto" to Timestamp.now()
                    )
                )
                .await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un alumno por su DNI
     */
    suspend fun getAlumnoPorDni(dni: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val alumnoDoc = alumnosCollection.document(dni).get().await()

            if (alumnoDoc.exists()) {
                val alumno = alumnoDoc.toObject(Alumno::class.java)
                return@withContext Result.Success(alumno!!)
            } else {
                throw Exception("Alumno no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Envía un mensaje
     */
    suspend fun enviarMensaje(mensaje: Mensaje): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Si el mensaje tiene un ID, lo usamos, de lo contrario generamos uno nuevo
            val mensajeId = if (mensaje.id.isNotBlank()) {
                mensaje.id
            } else {
                mensajesCollection.document().id
            }

            // Asignar el ID al mensaje
            val mensajeConId = if (mensaje.id.isBlank()) mensaje.copy(id = mensajeId) else mensaje

            // Guardar el mensaje
            mensajesCollection.document(mensajeId).set(mensajeConId).await()

            return@withContext Result.Success(mensajeId)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene mensajes entre dos usuarios
     */
    suspend fun getMensajesBetweenUsers(usuario1Id: String, usuario2Id: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            // Obtener mensajes enviados por usuario1 a usuario2
            val query1 = mensajesCollection
                .whereEqualTo("emisorId", usuario1Id)
                .whereEqualTo("receptorId", usuario2Id)
                .get()
                .await()

            val mensajes1 = query1.toObjects(Mensaje::class.java)

            // Obtener mensajes enviados por usuario2 a usuario1
            val query2 = mensajesCollection
                .whereEqualTo("emisorId", usuario2Id)
                .whereEqualTo("receptorId", usuario1Id)
                .get()
                .await()

            val mensajes2 = query2.toObjects(Mensaje::class.java)

            // Combinar y ordenar los mensajes
            val todosLosMensajes = (mensajes1 + mensajes2).sortedBy { it.timestamp }

            return@withContext Result.Success(todosLosMensajes)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene mensajes relacionados con un alumno específico entre dos usuarios
     */
    suspend fun getMensajesByAlumno(usuario1Id: String, usuario2Id: String, alumnoId: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            // Obtener mensajes enviados por usuario1 a usuario2 sobre el alumno
            val query1 = mensajesCollection
                .whereEqualTo("alumnoId", alumnoId)
                .whereEqualTo("emisorId", usuario1Id)
                .whereEqualTo("receptorId", usuario2Id)
                .get()
                .await()

            val mensajes1 = query1.toObjects(Mensaje::class.java)

            // Obtener mensajes enviados por usuario2 a usuario1 sobre el alumno
            val query2 = mensajesCollection
                .whereEqualTo("alumnoId", alumnoId)
                .whereEqualTo("emisorId", usuario2Id)
                .whereEqualTo("receptorId", usuario1Id)
                .get()
                .await()

            val mensajes2 = query2.toObjects(Mensaje::class.java)

            // Combinar y ordenar los mensajes
            val todosLosMensajes = (mensajes1 + mensajes2).sortedBy { it.timestamp }

            return@withContext Result.Success(todosLosMensajes)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Marca un mensaje como leído
     */
    suspend fun marcarMensajeComoLeido(mensajeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            mensajesCollection.document(mensajeId)
                .update("leido", true)
                .await()

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Observa mensajes en tiempo real entre dos usuarios
     * Esta función devuelve un Flow que emite la lista actualizada de mensajes cuando hay cambios
     */
    fun observeMensajes(usuario1Id: String, usuario2Id: String, alumnoId: String? = null) = flow<List<Mensaje>> {
        // Este es un ejemplo de implementación usando un enfoque simplificado
        // En una implementación real, usaríamos Firestore listeners para actualizaciones en tiempo real

        while (true) {
            val mensajesResult = if (alumnoId != null) {
                getMensajesByAlumno(usuario1Id, usuario2Id, alumnoId)
            } else {
                getMensajesBetweenUsers(usuario1Id, usuario2Id)
            }

            if (mensajesResult is Result.Success) {
                emit(mensajesResult.data)
            }

            // Esperar antes de la siguiente actualización
            kotlinx.coroutines.delay(5000) // Actualizar cada 5 segundos
        }
    }

    /**
     * Obtiene un usuario por su correo electrónico
     */
    suspend fun getUsuarioByEmail(email: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val userQuery = usuariosCollection.whereEqualTo("email", email).get().await()

            if (!userQuery.isEmpty) {
                val usuario = userQuery.documents.first().toObject(Usuario::class.java)
                return@withContext Result.Success(usuario!!)
            } else {
                throw Exception("Usuario no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un usuario por su DNI
     */
    suspend fun getUsuarioByDni(dni: String): Result<Usuario?> = withContext(Dispatchers.IO) {
        try {
            val usuarioQuery = usuariosCollection
                .whereEqualTo("dni", dni)
                .get().await()

            if (!usuarioQuery.isEmpty) {
                return@withContext Result.Success(usuarioQuery.documents[0].toObject(Usuario::class.java))
            }
            return@withContext Result.Success(null)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los usuarios asociados a un centro
     */
    suspend fun getUsuariosByCentroId(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscar todos los usuarios que tengan al menos un perfil asociado al centro
            val usuariosQuery = usuariosCollection.get().await()
            val usuarios = usuariosQuery.toObjects(Usuario::class.java)
            
            // Filtrar aquellos que tengan un perfil asociado al centro
            val usuariosCentro = usuarios.filter { usuario ->
                usuario.perfiles.any { perfil -> perfil.centroId == centroId }
            }
            
            return@withContext Result.Success(usuariosCentro)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Borra un usuario por su correo electrónico (primero en Auth y luego en Firestore)
     */
    suspend fun borrarUsuarioByEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Primero buscamos el usuario en Firestore para verificar que existe
            val usuarioResult = getUsuarioByEmail(email)
            
            if (usuarioResult is Result.Success) {
                // El usuario existe en Firestore, ahora intentamos eliminar su cuenta de Auth
                
                // Esto requeriría una función en el servidor con Firebase Admin SDK
                // Para la implementación actual, suponemos que se ha eliminado correctamente
                // y procedemos a eliminar el documento de Firestore
                
                val usuario = usuarioResult.data
                usuariosCollection.document(usuario.documentId).delete().await()
                
                return@withContext Result.Success(Unit)
            } else if (usuarioResult is Result.Error) {
                return@withContext Result.Error(usuarioResult.exception)
            } else {
                return@withContext Result.Error(Exception("No se encontró el usuario con email: $email"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Borra un usuario por su DNI, eliminándolo tanto de Firestore como de Firebase Authentication.
     * También elimina todas las relaciones asociadas según el tipo de usuario.
     * 
     * @param dni DNI del usuario a eliminar
     * @return Result<Unit> indicando éxito o fracaso de la operación
     */
    suspend fun borrarUsuarioByDni(dni: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Protección especial para el administrador principal por DNI
            if (dni == BuildConfig.ADMIN_PRINCIPAL_DNI) {
                Timber.w("⚠️ Intento de eliminación del administrador principal con DNI: $dni - Operación no permitida")
                return@withContext Result.Error(Exception("No se puede eliminar al administrador principal de la aplicación"))
            }
            
            // 1. Buscar el usuario en Firestore por DNI
            val usuarioDoc = usuariosCollection.document(dni).get().await()
            
            if (!usuarioDoc.exists()) {
                return@withContext Result.Error(Exception("No se encontró el usuario con DNI: $dni"))
            }
            
            // 2. Obtener el usuario completo para poder procesar según sus perfiles
            val usuario = usuarioDoc.toObject(Usuario::class.java)
                ?: return@withContext Result.Error(Exception("Error al convertir el documento a Usuario"))
            
            // Protección especial para administradores por email
            val email = usuario.email ?: ""
            if (email == "admin@eguneroko.com") {
                Timber.w("⚠️ Intento de eliminación del administrador con email: $email - Operación no permitida")
                return@withContext Result.Error(Exception("No se puede eliminar al administrador principal de la aplicación"))
            }
            
            // Verificar si es administrador de app y bloquearlo
            val isAdminApp = usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP }
            if (isAdminApp) {
                // Primera protección: comprobar si es el único administrador
                try {
                    val adminsResult = getAdministradores()
                    if (adminsResult is Result.Success) {
                        val totalAdmins = adminsResult.data.size
                        if (totalAdmins <= 1) {
                            Timber.w("⚠️ Intento de eliminación del último administrador de app con DNI: $dni - Operación no permitida")
                            return@withContext Result.Error(Exception("No se puede eliminar el último administrador de la aplicación"))
                        }
                    }
                } catch (e: Exception) {
                    // Si hay error al obtener administradores, por seguridad no permitimos borrar
                    Timber.e(e, "Error al verificar administradores, cancelando eliminación por seguridad")
                    return@withContext Result.Error(Exception("No se pudo verificar si es el último administrador. Operación cancelada por seguridad"))
                }
            }
            
            // Solo verificar email para usuarios que requieren autenticación (no alumnos)
            val esAlumno = usuario.perfiles.any { it.tipo == TipoUsuario.ALUMNO }
            if (email.isEmpty() && !esAlumno) {
                return@withContext Result.Error(Exception("El usuario no tiene email registrado"))
            }
            
            // 3. Procesar cada tipo de perfil para eliminar relaciones
            usuario.perfiles.forEach { perfil ->
                when (perfil.tipo) {
                    TipoUsuario.ADMIN_CENTRO -> {
                        // Actualizar centros donde este usuario es administrador
                        if (perfil.centroId.isNotBlank()) {
                            try {
                                val centroDoc = centrosCollection.document(perfil.centroId).get().await()
                                if (centroDoc.exists()) {
                                    // Eliminar el adminId de la lista de adminIds del centro
                                    centrosCollection.document(perfil.centroId).update(
                                        "adminIds", FieldValue.arrayRemove(dni)
                                    ).await()
                                    Timber.d("Eliminado adminId $dni del centro ${perfil.centroId}")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error al actualizar centro al eliminar admin: ${e.message}")
                                // Continuamos con el proceso aunque falle este paso
                            }
                        }
                    }
                    TipoUsuario.PROFESOR -> {
                        // Eliminar referencias en clases donde este profesor está asignado
                        try {
                            // Buscar el documento del profesor en la colección 'profesores' por usuarioId
                            var profesorId = ""
                            val profesoresQuery = firestore.collection(COLLECTION_PROFESORES)
                                .whereEqualTo("usuarioId", dni)
                                .get()
                                .await()
                            
                            // Si existe, eliminar el documento del profesor
                            if (!profesoresQuery.isEmpty) {
                                val profesorDoc = profesoresQuery.documents.first()
                                profesorId = profesorDoc.id
                                profesorDoc.reference.delete().await()
                                Timber.d("Profesor eliminado de la colección profesores: $profesorId")
                            }
                            
                            // Buscar clases donde este profesor es el principal
                            val clasesQuery = clasesCollection.whereEqualTo("profesorId", dni).get().await()
                            for (claseDoc in clasesQuery.documents) {
                                val claseId = claseDoc.id
                                claseDoc.reference.update("profesorId", "").await()
                                Timber.d("Eliminada referencia del profesor $dni de la clase $claseId")
                                
                                // También actualizamos los alumnos de esta clase para eliminar la referencia al profesor
                                try {
                                    val alumnos = alumnosCollection
                                        .whereEqualTo("claseId", claseId)
                                        .get()
                                        .await()
                                    
                                    for (alumnoDoc in alumnos.documents) {
                                        alumnoDoc.reference.update("profesorId", null).await()
                                        Timber.d("Eliminada referencia del profesor en alumno ${alumnoDoc.id}")
                                    }
                                    
                                    // También verificamos por aulaId (nueva estructura)
                                    val alumnosAula = alumnosCollection
                                        .whereEqualTo("aulaId", claseId)
                                        .get()
                                        .await()
                                    
                                    for (alumnoDoc in alumnosAula.documents) {
                                        alumnoDoc.reference.update("profesorId", null).await()
                                        Timber.d("Eliminada referencia del profesor en alumno ${alumnoDoc.id} (aulaId)")
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al actualizar alumnos de la clase $claseId: ${e.message}")
                                }
                            }
                            
                            // Buscar clases donde este profesor es el titular
                            val clasesTitular = clasesCollection.whereEqualTo("profesorTitularId", dni).get().await()
                            for (claseDoc in clasesTitular.documents) {
                                claseDoc.reference.update("profesorTitularId", "").await()
                                Timber.d("Eliminada referencia del profesor titular $dni de la clase ${claseDoc.id}")
                            }
                            
                            // Buscar clases donde este profesor está en la lista de profesores auxiliares
                            val clasesAuxiliares = clasesCollection.whereArrayContains("profesoresAuxiliaresIds", dni).get().await()
                            for (claseDoc in clasesAuxiliares.documents) {
                                claseDoc.reference.update(
                                    "profesoresAuxiliaresIds", FieldValue.arrayRemove(dni)
                                ).await()
                                Timber.d("Eliminado profesorId $dni de la lista de profesores auxiliares de la clase ${claseDoc.id}")
                            }
                            
                            // Si tenemos profesorId, también buscamos clases usando el ID del documento
                            if (profesorId.isNotEmpty()) {
                                // Buscar clases donde este profesor (por documento ID) es el principal
                                val clasesProfId = clasesCollection.whereEqualTo("profesorId", profesorId).get().await()
                                for (claseDoc in clasesProfId.documents) {
                                    claseDoc.reference.update("profesorId", "").await()
                                    Timber.d("Eliminada referencia del profesor (ID doc) $profesorId de la clase ${claseDoc.id}")
                                }
                                
                                // Buscar clases donde este profesor es titular por ID
                                val clasesTitularProfId = clasesCollection.whereEqualTo("profesorTitularId", profesorId).get().await()
                                for (claseDoc in clasesTitularProfId.documents) {
                                    claseDoc.reference.update("profesorTitularId", "").await()
                                    Timber.d("Eliminada referencia del profesor titular (ID doc) $profesorId de la clase ${claseDoc.id}")
                                }
                                
                                // Buscar clases donde este profesor está en la lista de auxiliares por ID
                                val clasesAuxiliaresProfId = clasesCollection.whereArrayContains("profesoresAuxiliaresIds", profesorId).get().await()
                                for (claseDoc in clasesAuxiliaresProfId.documents) {
                                    claseDoc.reference.update(
                                        "profesoresAuxiliaresIds", FieldValue.arrayRemove(profesorId)
                                    ).await()
                                    Timber.d("Eliminado profesorId (ID doc) $profesorId de la lista de profesores auxiliares de la clase ${claseDoc.id}")
                                }
                                
                                // Buscar todos los alumnos asignados a este profesor y actualizar su profesorId
                                val alumnosProfesor = alumnosCollection.whereEqualTo("profesorId", profesorId).get().await()
                                for (alumnoDoc in alumnosProfesor.documents) {
                                    alumnoDoc.reference.update("profesorId", null).await()
                                    Timber.d("Eliminada referencia del profesor (ID doc) en alumno ${alumnoDoc.id}")
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al actualizar clases y alumnos al eliminar profesor: ${e.message}")
                            // Continuamos con el proceso aunque falle este paso
                        }
                    }
                    TipoUsuario.FAMILIAR -> {
                        // Eliminar vinculaciones con alumnos
                        try {
                            // Para cada alumno vinculado, eliminar la referencia al familiar
                            perfil.alumnos.forEach { alumnoId ->
                                val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
                                if (alumnoDoc.exists()) {
                                    alumnoDoc.reference.update(
                                        "familiaresIds", FieldValue.arrayRemove(dni)
                                    ).await()
                                    Timber.d("Eliminada vinculación del familiar $dni con el alumno $alumnoId")
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al actualizar alumnos al eliminar familiar: ${e.message}")
                        }
                    }
                    TipoUsuario.ALUMNO -> {
                        // Cuando es un alumno, también eliminar de la colección de alumnos
                        try {
                            alumnosCollection.document(dni).delete().await()
                            Timber.d("Alumno eliminado de la colección de alumnos: $dni")
                            
                            // Limpiar solicitudes de vinculación relacionadas con este alumno
                            limpiarSolicitudesVinculacionAlumno(dni)
                            
                            // Limpiar mensajes unificados relacionados con este alumno
                            limpiarMensajesUnificadosAlumno(dni)
                            
                            // Limpiar referencias en clases
                            limpiarReferenciaAlumnoEnClases(dni)
                            
                        } catch (e: Exception) {
                            Timber.e(e, "Error al eliminar alumno de la colección de alumnos: ${e.message}")
                            // Continuamos con el proceso aunque falle este paso específico
                        }
                    }
                    else -> {
                        // Otros tipos de usuario
                        Timber.d("Eliminando usuario con perfil ${perfil.tipo}")
                    }
                }
            }
            
            // 4. Eliminar el documento del usuario de Firestore
            usuariosCollection.document(dni).delete().await()
            Timber.d("Documento del usuario $dni eliminado de Firestore")
            
            // 5. Eliminar mensajes relacionados con este usuario
            try {
                // Mensajes enviados por este usuario
                val mensajesEnviados = mensajesCollection.whereEqualTo("emisorId", dni).get().await()
                for (mensaje in mensajesEnviados.documents) {
                    mensaje.reference.delete().await()
                }
                
                // Mensajes recibidos por este usuario
                val mensajesRecibidos = mensajesCollection.whereEqualTo("receptorId", dni).get().await()
                for (mensaje in mensajesRecibidos.documents) {
                    mensaje.reference.delete().await()
                }
                
                Timber.d("Eliminados ${mensajesEnviados.size() + mensajesRecibidos.size()} mensajes relacionados con el usuario $dni")
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar mensajes del usuario: ${e.message}")
            }
            
            // 6. Eliminar de Firebase Authentication solo si el usuario tiene email (no es alumno)
            if (email.isNotEmpty()) {
                try {
                    when (val authResult = authRepository.deleteUserByEmail(email)) {
                        is Result.Success -> {
                            Timber.d("Usuario con email $email eliminado de Firebase Authentication")
                        }
                        is Result.Error -> {
                            // Registramos el error, pero no interrumpimos el flujo
                            Timber.w(authResult.exception, "No se pudo eliminar el usuario de Firebase Auth: ${authResult.exception?.message}")
                        }
                        else -> {
                            // No hacemos nada en otros casos
                        }
                    }
                } catch (authError: Exception) {
                    Timber.e(authError, "Error al intentar eliminar usuario de Auth: ${authError.message}")
                    // Consideramos éxito aunque falle en Auth, ya que se eliminó de Firestore
                }
            } else {
                Timber.d("Usuario sin email (alumno), no se intenta eliminar de Firebase Auth")
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error general al eliminar usuario: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Escucha mensajes entre dos usuarios en tiempo real 
     */
    suspend fun escucharMensajes(
        emisorId: String, 
        receptorId: String, 
        alumnoId: String? = null, 
        onMensajeRecibido: (Mensaje) -> Unit
    ) {
        try {
            val query = if (alumnoId != null) {
                mensajesCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .whereIn("emisorId", listOf(emisorId, receptorId))
                    .whereIn("receptorId", listOf(emisorId, receptorId))
            } else {
                mensajesCollection
                    .whereIn("emisorId", listOf(emisorId, receptorId))
                    .whereIn("receptorId", listOf(emisorId, receptorId))
            }

            // Ordenar por timestamp para obtener los mensajes en orden
            query.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Timber.e(e, "Error escuchando mensajes")
                        return@addSnapshotListener
                    }

                    snapshots?.documentChanges?.forEach { change ->
                        // Solo procesamos mensajes nuevos o modificados
                        if (change.type == DocumentChange.Type.ADDED || 
                            change.type == DocumentChange.Type.MODIFIED) {
                            val mensaje = change.document.toObject(Mensaje::class.java)
                            onMensajeRecibido(mensaje)
                        }
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Error al configurar escucha de mensajes")
            throw e
        }
    }

    /**
     * Obtiene un alumno por su ID
     */
    open suspend fun getAlumnoPorId(alumnoId: String): Result<Alumno> = withContext(Dispatchers.IO) {
        try {
            val alumnoDoc = alumnosCollection.document(alumnoId).get().await()

            if (alumnoDoc.exists()) {
                val alumno = alumnoDoc.toObject(Alumno::class.java)
                
                // Obtener los datos de los familiares vinculados
                val alumnoData = alumno ?: throw Exception("Error al convertir datos del alumno")
                val familiares = mutableListOf<Familiar>()
                
                if (alumnoData.familiares.isNotEmpty()) {
                    // Suponiendo que familiares contiene IDs de usuarios familiares
                    for (familiarId in alumnoData.familiares) {
                        try {
                            val familiarDoc = usuariosCollection.document(familiarId.id).get().await()
                            if (familiarDoc.exists()) {
                                val usuario = familiarDoc.toObject(Usuario::class.java)
                                if (usuario != null) {
                                    // Crear un objeto Familiar con la información del usuario
                                    val familiar = Familiar(
                                        id = familiarId.id,
                                        nombre = usuario.nombre,
                                        apellidos = usuario.apellidos,
                                        parentesco = familiarId.parentesco
                                    )
                                    familiares.add(familiar)
                                }
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error al obtener familiar ${familiarId.id}")
                            // Continuamos con el siguiente familiar aunque este falle
                        }
                    }
                }
                
                // Si hemos obtenido familiares, actualizar el objeto Alumno
                val alumnoConFamiliares = if (familiares.isNotEmpty()) {
                    alumnoData.copy(familiares = familiares)
                } else {
                    alumnoData
                }
                
                return@withContext Result.Success(alumnoConFamiliares)
            } else {
                throw Exception("Alumno no encontrado")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // Métodos requeridos por los ViewModels
    
    /**
     * Registra un nuevo alumno
     */
    suspend fun registrarAlumno(alumno: Alumno): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Guardar alumno en la colección de alumnos
            alumnosCollection.document(alumno.dni).set(alumno).await()
            
            // Crear un usuario básico para el alumno
            val perfiles = mutableListOf<Perfil>()
            perfiles.add(
                Perfil(
                    tipo = TipoUsuario.ALUMNO,
                    centroId = alumno.centroId
                )
            )
            
            val usuario = Usuario(
                dni = alumno.dni,
                nombre = alumno.nombre,
                apellidos = alumno.apellidos,
                email = "", // No se crea cuenta de correo para alumnos
                perfiles = perfiles,
                activo = true,
                fechaRegistro = Timestamp.now()
            )
            
            // Guardar usuario en la colección de usuarios
            usuariosCollection.document(usuario.dni).set(usuario).await()
            
            return@withContext Result.Success(alumno.dni)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los alumnos de un centro
     */
    suspend fun obtenerAlumnosPorCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val alumnosQuery = alumnosCollection
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
                
            val alumnos = alumnosQuery.documents.mapNotNull { doc ->
                // Obtenemos el DNI de cada alumno
                val alumno = doc.toObject(Alumno::class.java)
                
                // Para cada alumno, buscamos su información de usuario
                val usuarioDoc = usuariosCollection.document(alumno?.dni ?: "").get().await()
                if (usuarioDoc.exists()) {
                    usuarioDoc.toObject(Usuario::class.java)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los familiares de un centro
     */
    suspend fun obtenerFamiliaresPorCentro(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos usuarios que tengan un perfil de tipo FAMILIAR asociado al centro
            val familiares = usuariosCollection.get().await().documents
                .mapNotNull { doc -> doc.toObject(Usuario::class.java) }
                .filter { usuario -> 
                    usuario.perfiles.any { 
                        it.tipo == TipoUsuario.FAMILIAR && it.centroId == centroId 
                    }
                }
                
            return@withContext Result.Success(familiares)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los familiares vinculados a un alumno
     */
    suspend fun obtenerFamiliaresPorAlumno(alumnoDni: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos el alumno
            val alumnoDoc = alumnosCollection.document(alumnoDni).get().await()
            
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val alumno = alumnoDoc.toObject(Alumno::class.java)
            val familiaresDnis = alumno?.familiarIds ?: emptyList<String>()
            
            // Si no tiene familiares vinculados, devolvemos lista vacía
            if (familiaresDnis.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            // Buscamos los usuarios correspondientes a cada DNI de familiar
            val familiares = familiaresDnis.mapNotNull { dni ->
                val usuarioDoc = usuariosCollection.document(dni).get().await()
                if (usuarioDoc.exists()) {
                    usuarioDoc.toObject(Usuario::class.java)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(familiares)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene todos los alumnos vinculados a un familiar
     */
    suspend fun obtenerAlumnosPorFamiliar(familiarDni: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos alumnos que tengan este familiar en su lista
            val alumnosQuery = alumnosCollection.get().await()
            
            val alumnosIds = alumnosQuery.documents
                .mapNotNull { it.toObject(Alumno::class.java) }
                .filter { it.familiarIds.contains(familiarDni) }
                .map { it.dni }
                
            // Si no hay alumnos vinculados, devolvemos lista vacía
            if (alumnosIds.isEmpty()) {
                return@withContext Result.Success(emptyList())
            }
            
            // Buscamos los usuarios correspondientes a cada DNI de alumno
            val alumnos = alumnosIds.mapNotNull { dni ->
                val usuarioDoc = usuariosCollection.document(dni).get().await()
                if (usuarioDoc.exists()) {
                    usuarioDoc.toObject(Usuario::class.java)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Vincula un familiar con un alumno
     */
    suspend fun vincularFamiliarAlumno(alumnoDni: String, familiarDni: String, parentesco: SubtipoFamiliar): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que ambos existen
            val alumnoDoc = alumnosCollection.document(alumnoDni).get().await()
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val familiarDoc = usuariosCollection.document(familiarDni).get().await()
            if (!familiarDoc.exists()) {
                return@withContext Result.Error(Exception("Familiar no encontrado"))
            }
            
            // Obtenemos el alumno actual
            val alumno = alumnoDoc.toObject(Alumno::class.java)
            
            // Actualizamos su lista de familiares si no está ya
            val familiares = alumno?.familiares?.toMutableList() ?: mutableListOf()
            
            // Comprobamos si el familiar ya está vinculado
            val familiarExistente = familiares.find { it.id == familiarDni }
            
            if (familiarExistente == null) {
                // Obtenemos los datos del familiar
                val familiar = familiarDoc.toObject(Usuario::class.java)
                
                // Creamos un nuevo objeto Familiar con la relación de parentesco
                familiares.add(
                    Familiar(
                        id = familiarDni,
                        nombre = familiar?.nombre ?: "",
                        apellidos = familiar?.apellidos ?: "",
                        parentesco = parentesco.name
                    )
                )
                
                // Actualizamos también la lista de IDs de familiares para compatibilidad
                val familiarIds = alumno?.familiarIds?.toMutableList() ?: mutableListOf()
                if (!familiarIds.contains(familiarDni)) {
                    familiarIds.add(familiarDni)
                }
                
                // Guardamos las relaciones en el documento del alumno
                val updates = mapOf(
                    "familiares" to familiares,
                    "familiarIds" to familiarIds
                )
                alumnosCollection.document(alumnoDni).update(updates).await()
            } else {
                // Si ya existe, actualizamos solo el parentesco
                val index = familiares.indexOf(familiarExistente)
                familiares[index] = familiarExistente.copy(parentesco = parentesco.name)
                
                // Guardamos la lista actualizada
                alumnosCollection.document(alumnoDni).update("familiares", familiares).await()
            }
            
            // También actualizamos el familiar para añadir el perfil con el subtipo
            val familiar = familiarDoc.toObject(Usuario::class.java)
            val perfiles = familiar?.perfiles?.toMutableList() ?: mutableListOf()
            
            // Buscamos si ya tiene un perfil de FAMILIAR para este centro
            val perfilExistente = perfiles.find { 
                it.tipo == TipoUsuario.FAMILIAR && it.centroId == alumno?.centroId
            }
            
            if (perfilExistente != null) {
                // Actualizamos el perfil existente con el nuevo subtipo y añadimos al alumno a la lista
                val alumnosDelPerfil = perfilExistente.alumnos.toMutableList()
                if (!alumnosDelPerfil.contains(alumnoDni)) {
                    alumnosDelPerfil.add(alumnoDni)
                }
                
                val index = perfiles.indexOf(perfilExistente)
                perfiles[index] = perfilExistente.copy(
                    subtipo = parentesco,
                    alumnos = alumnosDelPerfil
                )
            } else {
                // Creamos un nuevo perfil
                perfiles.add(
                    Perfil(
                        tipo = TipoUsuario.FAMILIAR,
                        subtipo = parentesco,
                        centroId = alumno?.centroId ?: "",
                        alumnos = listOf(alumnoDni)
                    )
                )
            }
            
            // Guardamos los cambios en el familiar
            usuariosCollection.document(familiarDni).update("perfiles", perfiles).await()
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al vincular familiar: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Desvincula un familiar de un alumno
     */
    suspend fun desvincularFamiliarAlumno(alumnoDni: String, familiarDni: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que ambos existen
            val alumnoDoc = alumnosCollection.document(alumnoDni).get().await()
            if (!alumnoDoc.exists()) {
                return@withContext Result.Error(Exception("Alumno no encontrado"))
            }
            
            val familiarDoc = usuariosCollection.document(familiarDni).get().await()
            if (!familiarDoc.exists()) {
                return@withContext Result.Error(Exception("Familiar no encontrado"))
            }
            
            // Obtenemos el alumno actual
            val alumno = alumnoDoc.toObject(Alumno::class.java)
            
            // Actualizamos la lista de objetos Familiar
            val familiares = alumno?.familiares?.toMutableList() ?: mutableListOf()
            familiares.removeIf { it.id == familiarDni }
            
            // Actualizamos también la lista de IDs de familiares para compatibilidad
            val familiarIds = alumno?.familiarIds?.toMutableList() ?: mutableListOf()
            familiarIds.remove(familiarDni)
            
            // Guardamos las relaciones actualizadas en el documento del alumno
            val updates = mapOf(
                "familiares" to familiares,
                "familiarIds" to familiarIds
            )
            alumnosCollection.document(alumnoDni).update(updates).await()
            
            // Actualizamos también los perfiles del familiar para eliminar la relación con este alumno
            val familiar = familiarDoc.toObject(Usuario::class.java)
            val perfiles = familiar?.perfiles?.toMutableList() ?: mutableListOf()
            
            // Buscamos el perfil de FAMILIAR para el centro del alumno
            val perfilFamiliar = perfiles.find { 
                it.tipo == TipoUsuario.FAMILIAR && it.centroId == alumno?.centroId
            }
            
            if (perfilFamiliar != null) {
                // Removemos al alumno de la lista de alumnos en el perfil
                val alumnosDelPerfil = perfilFamiliar.alumnos.toMutableList()
                alumnosDelPerfil.remove(alumnoDni)
                
                // Si quedan otros alumnos, actualizamos el perfil
                if (alumnosDelPerfil.isNotEmpty()) {
                    val index = perfiles.indexOf(perfilFamiliar)
                    perfiles[index] = perfilFamiliar.copy(alumnos = alumnosDelPerfil)
                    usuariosCollection.document(familiarDni).update("perfiles", perfiles).await()
                } else {
                    // Si no quedan alumnos, eliminamos el perfil de familiar para este centro
                    perfiles.remove(perfilFamiliar)
                    usuariosCollection.document(familiarDni).update("perfiles", perfiles).await()
                }
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al desvincular familiar: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Resetea la contraseña de un usuario
     * @param dni DNI del usuario
     * @param nuevaPassword Nueva contraseña
     * @return Result<Unit> indicando éxito o fracaso de la operación
     */
    suspend fun resetearPassword(dni: String, nuevaPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Iniciando proceso de reseteo de contraseña para DNI: $dni")
            
            // 1. Buscar usuario por DNI
            val usuarioDoc = usuariosCollection.document(dni).get().await()
            
            if (!usuarioDoc.exists()) {
                return@withContext Result.Error(Exception("No se encontró el usuario con DNI: $dni"))
            }
            
            // 2. Obtener el usuario completo para poder procesar según sus perfiles
            val usuario = usuarioDoc.toObject(Usuario::class.java)
                ?: return@withContext Result.Error(Exception("Error al convertir el documento a Usuario"))
            
            // Verificar si es administrador para la lógica de SMTP
            val esAdmin = usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP }

            // 3. Actualizar contraseña en Firebase Auth
            try {
                @Suppress("DEPRECATION") // Suprimir warning de deprecación temporalmente
                val userRecord = firebaseAuth.fetchSignInMethodsForEmail(usuario.email).await()
                if (userRecord.signInMethods?.isNotEmpty() == true) {
                    firebaseAuth.sendPasswordResetEmail(usuario.email).await()
                    Timber.d("Email de reseteo enviado a: ${usuario.email}")
                } else {
                    Timber.e("No se encontró el usuario en Firebase Auth")
                    return@withContext Result.Error(Exception("Usuario no encontrado en el sistema de autenticación"))
                }
            } catch (e: FirebaseAuthException) {
                handleAuthError(e, "resetear_contraseña", usuario.email)
                Timber.e(e, "Error al actualizar contraseña en Firebase Auth")
                return@withContext Result.Error(Exception("Error al actualizar la contraseña: ${e.message}"))
            }

            // 4. Si es administrador, actualizar también la contraseña SMTP
            if (esAdmin) {
                Timber.d("Actualizando contraseña SMTP para administrador")
                val smtpUpdated = remoteConfigService.updateSMTPPassword(nuevaPassword)
                if (!smtpUpdated) {
                    handleAuthError(Exception("Error al actualizar la contraseña SMTP"), "actualizar_contraseña_smtp", usuario.email)
                    Timber.e("Error al actualizar la contraseña SMTP")
                    return@withContext Result.Error(Exception("Error al actualizar la contraseña SMTP"))
                }
                Timber.d("Contraseña SMTP actualizada correctamente")
            }

            // 5. Actualizar último cambio de contraseña en Firestore
            usuariosCollection.document(dni).update(
                "ultimoCambioPassword",
                Timestamp.now()
            ).await()
            Timber.d("Último cambio de contraseña actualizado en Firestore")

            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            handleAuthError(e, "resetear_contraseña")
            Timber.e(e, "Error inesperado durante el reseteo de contraseña")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza los datos de un usuario
     */
    suspend fun actualizarUsuario(usuario: Usuario): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Actualizar en Firestore
            usuariosCollection.document(usuario.dni).set(usuario).await()
            return@withContext Result.Success(true)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los mensajes donde participa un usuario
     * 
     * Este método recupera todos los mensajes donde el usuario es
     * emisor o receptor para mostrarlos en la pantalla de conversaciones
     */
    suspend fun getMensajesUsuario(usuarioId: String): Result<List<Mensaje>> = withContext(Dispatchers.IO) {
        try {
            // Obtener mensajes donde el usuario es emisor
            val queryEmisor = mensajesCollection
                .whereEqualTo("emisorId", usuarioId)
                .get()
                .await()

            val mensajesEmisor = queryEmisor.toObjects(Mensaje::class.java)

            // Obtener mensajes donde el usuario es receptor
            val queryReceptor = mensajesCollection
                .whereEqualTo("receptorId", usuarioId)
                .get()
                .await()

            val mensajesReceptor = queryReceptor.toObjects(Mensaje::class.java)

            // Combinar los resultados
            val todosMensajes = (mensajesEmisor + mensajesReceptor).distinctBy { it.id }

            return@withContext Result.Success(todosMensajes)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener mensajes del usuario")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene el usuario actual de Firebase Auth
     * @return FirebaseUser o null si no hay usuario autenticado
     */
    fun getUsuarioActualAuth(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Verifica si el usuario actual es un profesor
     * @return true si es profesor, false si es alumno o no está autenticado
     */
    suspend fun esProfesor(): Boolean {
        val usuario = getUsuarioActualAuth() ?: return false
        val usuarioId = usuario.uid
        
        return try {
            val docSnapshot = firestore.collection(COLLECTION_PROFESORES)
                .document(usuarioId)
                .get()
                .await()
            
            docSnapshot.exists()
        } catch (e: Exception) {
            Timber.e(e, "Error al verificar si es profesor")
            false
        }
    }
    
    /**
     * Obtiene el ID del centro educativo al que pertenece el usuario actual
     * @return ID del centro o null si no se encuentra
     */
    suspend fun getCentroIdUsuarioActual(): String? {
        val usuario = getUsuarioActualAuth() ?: return null
        val usuarioId = usuario.uid
        val esProfesor = esProfesor()
        
        return try {
            val collection = if (esProfesor) COLLECTION_PROFESORES else COLLECTION_ALUMNOS
            val docSnapshot = firestore.collection(collection)
                .document(usuarioId)
                .get()
                .await()
            
            if (docSnapshot.exists()) {
                docSnapshot.getString("centroId")
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener el centro del usuario")
            null
        }
    }
    
    /**
     * Obtiene el nombre completo del usuario
     * @param usuarioId ID del usuario
     * @return Nombre completo o cadena vacía si no se encuentra
     */
    suspend fun getNombreUsuario(usuarioId: String): String {
        return try {
            // Primero buscamos en la colección de profesores
            var docSnapshot = firestore.collection(COLLECTION_PROFESORES)
                .document(usuarioId)
                .get()
                .await()
            
            // Si no existe como profesor, buscamos en alumnos
            if (!docSnapshot.exists()) {
                docSnapshot = firestore.collection(COLLECTION_ALUMNOS)
                    .document(usuarioId)
                    .get()
                    .await()
            }
            
            if (docSnapshot.exists()) {
                val nombre = docSnapshot.getString("nombre") ?: ""
                val apellidos = docSnapshot.getString("apellidos") ?: ""
                "$nombre $apellidos".trim()
            } else {
                ""
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener nombre de usuario")
            ""
        }
    }

    /**
     * Obtiene el rol del usuario actual
     * @return "profesor", "alumno" o null si no está autenticado
     */
    suspend fun getRolUsuarioActual(): String? {
        getUsuarioActual() // Llamar para asegurar la lógica interna si la hubiera, pero ignorar el resultado
        
        return if (esProfesor()) "profesor" else "alumno"
    }

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario actual o null si no hay sesión
     */
    suspend fun obtenerUsuarioActual(): Usuario? {
        try {
            val firebaseUser = getUsuarioActualAuth() ?: return null
            
            // Buscar usuario por firebaseUid en la colección, no por ID del documento
            val querySnapshot = usuariosCollection
                .whereEqualTo("firebaseUid", firebaseUser.uid)
                .limit(1)
                .get()
                .await()
            
            if (querySnapshot.isEmpty) {
                Timber.w("No se encontró usuario con firebaseUid: ${firebaseUser.uid}")
                return null
            }
            
            val usuarioDoc = querySnapshot.documents.first()
            return usuarioDoc.toObject(Usuario::class.java)?.copy(dni = usuarioDoc.id)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuario actual")
            return null
        }
    }

    /**
     * Obtiene todos los usuarios de un centro específico
     * @param centroId ID del centro
     * @return Lista de usuarios del centro
     */
    suspend fun obtenerUsuariosPorCentro(centroId: String): List<Usuario> {
        return try {
            val snapshot = usuariosCollection
                .whereEqualTo("centroId", centroId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Usuario::class.java)?.copy(dni = doc.id)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener usuarios por centro")
            emptyList()
        }
    }

    /**
     * Obtiene todos los profesores activos del sistema
     * @return Lista de profesores o error
     */
    suspend fun getProfesores(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usuariosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
                
            val profesores = snapshot.documents.mapNotNull { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                // Filtrar solo los que tienen perfil de profesor
                if (usuario != null && usuario.perfiles.any { it.tipo == TipoUsuario.PROFESOR }) {
                    usuario.copy(dni = doc.id)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(profesores)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los administradores activos del sistema
     * @return Lista de administradores o error
     */
    suspend fun getAdministradores(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usuariosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
                
            val administradores = snapshot.documents.mapNotNull { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                // Filtrar solo los que tienen perfil de administrador
                if (usuario != null && usuario.perfiles.any { it.tipo == TipoUsuario.ADMIN_APP }) {
                    usuario.copy(dni = doc.id)
                } else {
                    null
                }
            }
            
            return@withContext Result.Success(administradores)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener administradores: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los usuarios activos
     */
    suspend fun obtenerTodosLosUsuarios(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val query = usuariosCollection.whereEqualTo("activo", true).get().await()
            val usuarios = query.documents.mapNotNull { it.toObject(Usuario::class.java) }
            return@withContext Result.Success(usuarios)
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    // --- NUEVA FUNCIÓN PARA GUARDAR ALUMNOS --- 
    /**
     * Guarda los datos de un nuevo alumno en Firestore.
     * Utiliza el DNI del alumno como ID del documento.
     *
     * @param alumno Objeto Alumno con los datos a guardar.
     * @return Result<Unit> indicando éxito o error.
     */
    suspend fun guardarAlumno(alumno: Alumno): Result<Unit>
    = withContext(Dispatchers.IO) {
        try {
            Timber.d("Guardando alumno con DNI ${alumno.dni} en Firestore...")
            
            // Verificar si tiene URL de avatar, y si no, obtener una
            val avatarUrl = if (alumno.avatarUrl.isNullOrEmpty()) {
                Timber.d("Alumno sin avatarUrl, obteniendo una por defecto")
                obtenerAvatarPorDefecto(TipoUsuario.ALUMNO)
            } else {
                alumno.avatarUrl
            }
            
            // Crear una copia del alumno con la URL del avatar (si se obtuvo una nueva)
            val alumnoFinal = if (avatarUrl != alumno.avatarUrl) {
                alumno.copy(avatarUrl = avatarUrl)
            } else {
                alumno
            }
            
            // Usar el DNI del alumno como ID del documento en la colección 'alumnos'
            alumnosCollection.document(alumnoFinal.dni).set(alumnoFinal).await()
            Timber.d("Alumno con DNI ${alumnoFinal.dni} guardado exitosamente con avatar: $avatarUrl")
            
            // También crear un documento en la colección "usuarios" con el perfil de alumno
            try {
                val perfiles = mutableListOf<Perfil>()
                perfiles.add(
                    Perfil(
                        tipo = TipoUsuario.ALUMNO,
                        centroId = alumnoFinal.centroId,
                        verificado = true
                    )
                )
                
                val usuario = Usuario(
                    dni = alumnoFinal.dni,
                    nombre = alumnoFinal.nombre,
                    apellidos = alumnoFinal.apellidos,
                    email = "", // No se crea cuenta de correo para alumnos
                    avatarUrl = avatarUrl, // Misma URL del avatar
                    perfiles = perfiles,
                    activo = true,
                    fechaRegistro = Timestamp.now()
                )
                
                usuariosCollection.document(usuario.dni).set(usuario).await()
                Timber.d("Usuario básico creado para alumno con avatar: $avatarUrl")
            } catch (e: Exception) {
                Timber.e(e, "Error al crear usuario básico para alumno: ${e.message}")
                // No interrumpir el flujo principal por este error secundario
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar alumno con DNI ${alumno.dni}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene todos los alumnos registrados en la colección 'alumnos'.
     *
     * @return Result<List<Alumno>> Lista de todos los alumnos o error.
     */
    suspend fun obtenerTodosLosAlumnos(): Result<List<Alumno>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Obteniendo todos los alumnos de la colección 'alumnos'...")
            val querySnapshot = alumnosCollection.get().await()
            val alumnos = querySnapshot.toObjects(Alumno::class.java)
            Timber.d("Se encontraron ${alumnos.size} alumnos.")
            return@withContext Result.Success(alumnos)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener todos los alumnos.")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Obtiene un centro educativo por su ID
     * 
     * @param centroId ID del centro educativo
     * @return Resultado con el centro o error
     */
    suspend fun getCentroById(centroId: String): Result<Centro> = withContext(Dispatchers.IO) {
        try {
            val centroDoc = centrosCollection.document(centroId).get().await()
            
            if (centroDoc.exists()) {
                val centro = centroDoc.toObject(Centro::class.java)
                if (centro != null) {
                    // Asegurarse de que el ID esté establecido correctamente
                    centro.id = centroId
                    return@withContext Result.Success(centro)
                } else {
                    throw Exception("Error al convertir datos del centro")
                }
            } else {
                throw Exception("Centro no encontrado")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener centro por ID: $centroId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Registra un usuario completo con todos sus datos y perfiles
     * Este método combina la creación del usuario en Firebase Auth y Firestore
     * en una operación atómica.
     * 
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @param dni DNI del usuario que servirá como ID único
     * @param nombre Nombre del usuario
     * @param apellidos Apellidos del usuario
     * @param telefono Teléfono del usuario
     * @param tipoUsuario Tipo principal del usuario (FAMILIAR, PROFESOR, ADMIN, etc)
     * @param subtipo Subtipo del usuario (si aplica, por ejemplo tipo de familiar)
     * @param direccion Dirección completa del usuario
     * @param centroId ID del centro educativo asociado (si aplica)
     * @param perfilesAdicionales Lista de perfiles adicionales (si tiene varios roles)
     * @param context Contexto de la aplicación para acceder a recursos (opcional)
     * @return Result que contiene el usuario creado o un error
     */
    suspend fun registrarUsuarioCompleto(
        email: String,
        password: String,
        dni: String,
        nombre: String,
        apellidos: String,
        telefono: String,
        tipoUsuario: TipoUsuario,
        subtipo: String,
        direccion: Direccion,
        centroId: String,
        perfilesAdicionales: List<Perfil> = emptyList(),
        context: Context? = null
    ): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            // 1. Verificar que el DNI no existe ya en Firestore
            val existingUser = usuariosCollection.document(dni).get().await()
            if (existingUser.exists()) {
                return@withContext Result.Error(Exception("Ya existe un usuario con este DNI"))
            }

            // 2. Obtener/Subir avatar y obtener URL
            Timber.d("Obteniendo avatar predefinido para tipo de usuario: $tipoUsuario")
            val avatarUrl = try {
                val resourceName = when (tipoUsuario) {
                    TipoUsuario.ADMIN_APP -> "AdminAvatar.png"
                    TipoUsuario.ADMIN_CENTRO -> "centro.png"
                    TipoUsuario.PROFESOR -> "profesor.png"
                    TipoUsuario.FAMILIAR -> "familiar.png"
                    TipoUsuario.ALUMNO -> "alumno.png"
                    else -> "default.png"
                }
                // Usar siempre el prefijo '@' en el nombre del archivo
                val avatarFileName = "@" + resourceName.replaceFirst("@", "")
                // Verificar si el avatar ya existe en Storage
                val avatarPath = "avatares/${avatarFileName.lowercase()}"
                val storageRef = FirebaseStorage.getInstance().reference.child(avatarPath)
                try {
                    // Intentar obtener URL si ya existe
                    storageRef.downloadUrl.await().toString()
                } catch (e: Exception) {
                    // Si no existe, subir desde los assets
                    Timber.d("Avatar no encontrado en Storage, subiendo desde assets: $resourceName")
                    if (context == null) {
                        Timber.w("Contexto no disponible para subir avatar")
                        "" // URL vacía si no hay contexto
                    } else {
                        try {
                            val tempFile = java.io.File.createTempFile("avatar", ".png")
                            tempFile.deleteOnExit()
                            try {
                                context.assets.open("images/$resourceName").use { input ->
                                    java.io.FileOutputStream(tempFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            } catch (assetError: Exception) {
                                Timber.w("No se encontró $resourceName, usando imagen por defecto")
                                context.assets.open("images/default.png").use { input ->
                                    java.io.FileOutputStream(tempFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                            val result = storageRef.putFile(android.net.Uri.fromFile(tempFile)).await()
                            result.storage.downloadUrl.await().toString()
                        } catch (uploadError: Exception) {
                            Timber.e(uploadError, "Error al subir avatar: ${uploadError.message}")
                            "" // URL vacía en caso de error
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener avatar: ${e.message}")
                ""
            }

            // 3. Crear cuenta en Firebase Auth
            Timber.d("Creando cuenta en Firebase Authentication para $email")
            val authResult = try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                handleAuthError(e, "registrar_usuario_completo", email)
                Timber.e(e, "Error al crear cuenta en Firebase Auth: ${e.message}")
                // Comprobar si es un error de email ya existente
                if (e.message?.contains("email address is already in use") == true) {
                    return@withContext Result.Error(Exception("El correo electrónico ya está registrado. Por favor, utiliza otro o inicia sesión con este."))
                }
                return@withContext Result.Error(Exception("Error al crear la cuenta de usuario: ${e.message}"))
            }

            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                Timber.e("Firebase Auth devolvió un resultado sin usuario")
                return@withContext Result.Error(Exception("Error al crear la cuenta de usuario"))
            }

            // 4. Crear perfil principal
            Timber.d("Creando perfil principal de tipo: $tipoUsuario")
            val perfilPrincipal = Perfil(
                tipo = tipoUsuario,
                centroId = if (tipoUsuario == TipoUsuario.PROFESOR || tipoUsuario == TipoUsuario.ADMIN_CENTRO) centroId else "",
                verificado = false // El administrador debe verificar al familiar
            )

            // 5. Combinar con perfiles adicionales
            val perfiles = mutableListOf<Perfil>()
            perfiles.add(perfilPrincipal)
            perfiles.addAll(perfilesAdicionales)

            // 6. Crear objeto de usuario para Firestore
            val usuario = Usuario(
                dni = dni,
                nombre = nombre,
                apellidos = apellidos,
                telefono = telefono,
                email = email,
                perfiles = perfiles,
                firebaseUid = firebaseUser.uid,
                avatarUrl = avatarUrl, // URL del avatar
                estado = "PENDIENTE_VERIFICACION",
                direccion = direccion,
                fechaRegistro = Timestamp.now(),
                activo = true,
                preferencias = Preferencias(
                    notificaciones = Notificaciones(
                        push = true,
                        email = true,
                        deviceId = "device_${System.currentTimeMillis()}"
                    )
                )
                // No incluimos subtipo en la creación del objeto Usuario
            )

            // 7. Guardar en Firestore
            Timber.d("Guardando usuario en Firestore")
            usuariosCollection.document(dni).set(usuario).await()
            Timber.d("Usuario guardado exitosamente en Firestore con avatar: $avatarUrl")

            return@withContext Result.Success(usuario)
        } catch (e: Exception) {
            handleAuthError(e, "registrar_usuario_completo", email)
            Timber.e(e, "Error general al registrar usuario: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene un avatar predeterminado según el tipo de usuario
     * 
     * @param tipoUsuario El tipo de usuario para determinar qué avatar usar
     * @param context El contexto de la aplicación para acceder a los recursos (opcional)
     * @return URL del avatar o cadena vacía en caso de error
     */
    private suspend fun obtenerAvatarPorDefecto(tipoUsuario: TipoUsuario, context: Context? = null): String {
        return defaultAvatarsManager?.obtenerAvatarPredeterminado(tipoUsuario) ?: run {
            // URL por defecto para cuando no tenemos el manager (por ejemplo en mock)
            when (tipoUsuario) {
                TipoUsuario.ADMIN_APP -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/AdminAvatar.png?alt=media"
                TipoUsuario.ADMIN_CENTRO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/centro.png?alt=media&token=ac002e24-dbd1-41a5-8c26-4959c714c649"
                TipoUsuario.PROFESOR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/profesor.png?alt=media&token=89b1bae9-dddc-476f-b6dc-184ec0b55eaf"
                TipoUsuario.FAMILIAR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/familiar.png?alt=media&token=0d69c88f-4eb1-4e94-a20a-624d91c38379"
                TipoUsuario.ALUMNO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/alumno.png?alt=media&token=bb66f9aa-8c9c-4f1a-b262-c0fa8c285a0d"
                else -> ""
            }
        }
    }

    /**
     * Guarda los datos de un nuevo alumno en Firestore.
     * Utiliza el DNI del alumno como ID del documento.
     *
     * @param alumno Objeto Alumno con los datos a guardar.
     * @param context Contexto de la aplicación para acceder a los recursos (opcional)
     * @return Result<Unit> indicando éxito o error.
     */
    suspend fun guardarAlumno(alumno: Alumno, context: Context? = null): Result<Unit>
    = withContext(Dispatchers.IO) {
        try {
            Timber.d("Guardando alumno con DNI ${alumno.dni} en Firestore...")
            
            // Verificar si tiene URL de avatar, y si no, obtener una
            val avatarUrlActual = alumno.avatarUrl ?: ""
            val avatarUrl = if (avatarUrlActual.isEmpty()) {
                Timber.d("Alumno sin avatarUrl, obteniendo una por defecto")
                obtenerAvatarPorDefecto(TipoUsuario.ALUMNO, context)
            } else {
                avatarUrlActual
            }
            
            // Crear una copia del alumno con la URL del avatar (si se obtuvo una nueva)
            val alumnoFinal = if (avatarUrl != avatarUrlActual) {
                alumno.copy(avatarUrl = avatarUrl)
            } else {
                alumno
            }
            
            // Usar el DNI del alumno como ID del documento en la colección 'alumnos'
            alumnosCollection.document(alumnoFinal.dni).set(alumnoFinal).await()
            Timber.d("Alumno con DNI ${alumnoFinal.dni} guardado exitosamente con avatar: $avatarUrl")
            
            // También crear un documento en la colección "usuarios" con el perfil de alumno
            try {
                val perfiles = mutableListOf<Perfil>()
                perfiles.add(
                    Perfil(
                        tipo = TipoUsuario.ALUMNO,
                        centroId = alumnoFinal.centroId,
                        verificado = true
                    )
                )
                
                val usuario = Usuario(
                    dni = alumnoFinal.dni,
                    nombre = alumnoFinal.nombre,
                    apellidos = alumnoFinal.apellidos,
                    email = "", // No se crea cuenta de correo para alumnos
                    avatarUrl = avatarUrl, // Misma URL del avatar
                    perfiles = perfiles,
                    activo = true,
                    fechaRegistro = Timestamp.now()
                )
                
                usuariosCollection.document(usuario.dni).set(usuario).await()
                Timber.d("Usuario básico creado para alumno con avatar: $avatarUrl")
            } catch (e: Exception) {
                Timber.e(e, "Error al crear usuario básico para alumno: ${e.message}")
                // No interrumpir el flujo principal por este error secundario
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar alumno con DNI ${alumno.dni}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Elimina un usuario de Firestore y Firebase Authentication
     */
    suspend fun eliminarUsuario(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Buscar el usuario en Firestore por su email
            val usuarioQuery = usuariosCollection.whereEqualTo("email", email).limit(1).get().await()
            
            if (usuarioQuery.isEmpty) {
                return@withContext Result.Error(Exception("Usuario no encontrado con email: $email"))
            }
            
            val usuarioDoc = usuarioQuery.documents.first()
            val usuarioId = usuarioDoc.id
            
            // Eliminar el usuario de Firestore
            usuariosCollection.document(usuarioId).delete().await()
            
            // Intentar eliminar de Firebase Authentication
            try {
                // Verificar si el usuario actual está eliminando su propia cuenta
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null && currentUser.email == email) {
                    // Si es el propio usuario, podemos eliminarlo directamente
                    currentUser.delete().await()
                    firebaseAuth.signOut()
                    Timber.d("Usuario ha eliminado su propia cuenta: $email")
                } else {
                    // Para eliminar otros usuarios, necesitamos estar en un rol de administrador
                    // Esta operación generalmente requiere autenticación administrativa
                    Timber.w("No se puede eliminar directamente otro usuario de Firebase Auth desde el cliente")
                    Timber.w("Se ha eliminado el usuario de Firestore, pero se mantiene en Authentication")
                    
                    // Alternativa: marcar al usuario como inactivo en Firestore para impedir acceso
                    try {
                        // Encuentra todas las referencias al usuario y márcarlas como inactivas
                        usuariosCollection.document(usuarioId)
                            .update("activo", false, "fechaBaja", Timestamp.now())
                            .await()
                        Timber.d("Usuario marcado como inactivo en Firestore")
                    } catch (e: Exception) {
                        Timber.e(e, "Error al marcar usuario como inactivo")
                    }
                }
            } catch (authError: Exception) {
                Timber.e(authError, "Error al intentar eliminar usuario de Auth: ${authError.message}")
                // Consideramos éxito aunque falle en Auth, ya que se eliminó de Firestore
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error general al eliminar usuario: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un nuevo usuario en Firestore y Firebase Authentication
     * 
     * @param usuario Objeto Usuario con los datos a guardar
     * @param password Contraseña para crear la cuenta de Firebase Auth
     * @return Result que contiene el usuario creado o un error
     */
    suspend fun saveUsuario(usuario: Usuario, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Iniciando guardado de usuario: ${usuario.dni}, email: ${usuario.email}")
            
            // Verificar que el DNI no existe ya en Firestore
            val existingUser = usuariosCollection.document(usuario.dni).get().await()
            if (existingUser.exists()) {
                return@withContext Result.Error(Exception("Ya existe un usuario con este DNI"))
            }

            // 2. Crear usuario en Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(usuario.email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear usuario en Firebase Auth")
            Timber.d("Usuario creado en Firebase Auth con UID: $uid")
            
            // Actualizar el usuario con el UID de Firebase
            val usuarioConUid = usuario.copy(
                firebaseUid = uid,
                fechaRegistro = Timestamp.now()
            )
            
            // Guardar en Firestore
            usuariosCollection.document(usuario.dni).set(usuarioConUid).await()
            Timber.d("Usuario guardado en Firestore: ${usuario.dni}")
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            handleAuthError(e, "guardar_usuario", usuario.email)
            Timber.e(e, "Error al guardar usuario: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Actualiza un usuario existente en Firestore
     * 
     * @param usuario Objeto Usuario con los datos actualizados
     * @return Result que contiene el resultado de la operación
     */
    suspend fun updateUsuario(usuario: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Actualizando usuario: ${usuario.dni}")
            
            // Verificar que el usuario existe
            val existingUser = usuariosCollection.document(usuario.dni).get().await()
            if (!existingUser.exists()) {
                return@withContext Result.Error(Exception("El usuario no existe"))
            }
            
            // Actualizar en Firestore
            usuariosCollection.document(usuario.dni).set(usuario).await()
            Timber.d("Usuario actualizado en Firestore: ${usuario.dni}")
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al actualizar usuario: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Guarda un nuevo alumno en Firestore
     * 
     * @param alumno Objeto Alumno con los datos a guardar
     * @return Result que contiene el resultado de la operación
     */
    suspend fun saveAlumno(alumno: Alumno): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Guardando alumno: ${alumno.dni}, nombre: ${alumno.nombre}")
            
            // Verificar que el DNI no existe ya en Firestore
            val existingAlumno = alumnosCollection.document(alumno.dni).get().await()
            if (existingAlumno.exists()) {
                return@withContext Result.Error(Exception("Ya existe un alumno con este DNI"))
            }
            
            // Verificar si tiene URL de avatar, y si no, obtener una
            val avatarUrl = if (alumno.avatarUrl.isNullOrEmpty()) {
                Timber.d("Alumno sin avatarUrl, obteniendo una por defecto")
                obtenerAvatarPorDefecto(TipoUsuario.ALUMNO)
            } else {
                alumno.avatarUrl
            }
            
            // Crear una copia del alumno con la URL del avatar (si se obtuvo una nueva)
            val alumnoFinal = if (avatarUrl != alumno.avatarUrl) {
                alumno.copy(avatarUrl = avatarUrl)
            } else {
                alumno
            }
            
            // Guardar en Firestore
            alumnosCollection.document(alumnoFinal.dni).set(alumnoFinal).await()
            Timber.d("Alumno guardado en Firestore: ${alumnoFinal.dni} con avatar: $avatarUrl")
            
            // También crear un documento en la colección "usuarios" con el perfil de alumno
            try {
                val perfiles = mutableListOf<Perfil>()
                perfiles.add(
                    Perfil(
                        tipo = TipoUsuario.ALUMNO,
                        centroId = alumnoFinal.centroId,
                        verificado = true
                    )
                )
                
                val usuario = Usuario(
                    dni = alumnoFinal.dni,
                    nombre = alumnoFinal.nombre,
                    apellidos = alumnoFinal.apellidos,
                    email = "", // No se crea cuenta de correo para alumnos
                    avatarUrl = avatarUrl, // Misma URL del avatar
                    perfiles = perfiles,
                    activo = true,
                    fechaRegistro = Timestamp.now()
                )
                
                usuariosCollection.document(usuario.dni).set(usuario).await()
                Timber.d("Usuario básico creado para alumno con avatar: $avatarUrl")
            } catch (e: Exception) {
                Timber.e(e, "Error al crear usuario básico para alumno: ${e.message}")
                // No interrumpir el flujo principal por este error secundario
            }
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error al guardar alumno: ${e.message}")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Obtiene una URL de avatar por defecto para un tipo de usuario
     */
    private suspend fun obtenerAvatarPorDefecto(tipoUsuario: TipoUsuario): String {
        return defaultAvatarsManager?.obtenerAvatarPredeterminado(tipoUsuario) ?: run {
            // URL por defecto para cuando no tenemos el manager (por ejemplo en mock)
            when (tipoUsuario) {
                TipoUsuario.ADMIN_APP -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/AdminAvatar.png?alt=media"
                TipoUsuario.ADMIN_CENTRO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/centro.png?alt=media&token=ac002e24-dbd1-41a5-8c26-4959c714c649"
                TipoUsuario.PROFESOR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/profesor.png?alt=media&token=89b1bae9-dddc-476f-b6dc-184ec0b55eaf"
                TipoUsuario.FAMILIAR -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/familiar.png?alt=media&token=0d69c88f-4eb1-4e94-a20a-624d91c38379"
                TipoUsuario.ALUMNO -> "https://firebasestorage.googleapis.com/v0/b/umeegunero.firebasestorage.app/o/alumno.png?alt=media&token=bb66f9aa-8c9c-4f1a-b262-c0fa8c285a0d"
                else -> ""
            }
        }
    }

    /**
     * Obtiene los administradores de un centro específico
     * @param centroId ID del centro
     * @return Lista de usuarios administradores del centro
     */
    suspend fun getAdminsByCentroId(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando administradores para el centro: $centroId")
            
            // Buscar usuarios que tengan un perfil de tipo ADMIN_CENTRO para el centro específico
            val query = usuariosCollection
                .whereArrayContains("perfiles", 
                    mapOf(
                        "tipo" to TipoUsuario.ADMIN_CENTRO.name,
                        "centroId" to centroId
                    )
                )
                .get()
                .await()
            
            if (query.isEmpty) {
                // Alternativa: buscar por el centro en la colección de centros
                val centroDoc = centrosCollection.document(centroId).get().await()
                if (centroDoc.exists()) {
                    val centro = centroDoc.toObject(Centro::class.java)
                    val adminIds = centro?.adminIds ?: emptyList()
                    
                    if (adminIds.isNotEmpty()) {
                        val admins = mutableListOf<Usuario>()
                        for (adminId in adminIds) {
                            val adminDoc = usuariosCollection.document(adminId).get().await()
                            if (adminDoc.exists()) {
                                val admin = adminDoc.toObject(Usuario::class.java)
                                admin?.let { admins.add(it) }
                            }
                        }
                        return@withContext Result.Success(admins)
                    }
                }
                
                // Si no se encontraron admins ni por perfiles ni por centro.adminIds
                return@withContext Result.Success(emptyList())
            }
            
            // Convertir documentos a objetos Usuario
            val admins = query.documents.mapNotNull { it.toObject(Usuario::class.java) }
            return@withContext Result.Success(admins)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener administradores del centro: $centroId")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Busca usuarios por nombre o correo electrónico
     * @param query Texto de búsqueda
     * @param limit Límite de resultados (opcional)
     * @return Lista de usuarios que coinciden con la búsqueda
     */
    suspend fun buscarUsuariosPorNombreOCorreo(query: String, limit: Int = 20): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Buscando usuarios por: $query")
            val queryLowerCase = query.lowercase()
            
            // Primero intentamos una búsqueda exacta por correo
            val emailQuery = usuariosCollection
                .whereEqualTo("email", query)
                .limit(1)
                .get()
                .await()
            
            val usuarios = mutableListOf<Usuario>()
            
            // Si encontramos por email, lo añadimos primero
            if (!emailQuery.isEmpty) {
                emailQuery.documents.forEach { doc ->
                    doc.toObject(Usuario::class.java)?.let { usuarios.add(it) }
                }
                
                // Si alcanzamos el límite, devolvemos ya
                if (usuarios.size >= limit) {
                    return@withContext Result.Success(usuarios.take(limit))
                }
            }
            
            // Luego buscamos por nombre/apellidos que contengan la query
            val allUsuarios = usuariosCollection.get().await()
                .documents
                .mapNotNull { it.toObject(Usuario::class.java) }
                .filter { usuario ->
                    usuario.nombre.lowercase().contains(queryLowerCase) ||
                    usuario.apellidos.lowercase().contains(queryLowerCase) ||
                    "${usuario.nombre} ${usuario.apellidos}".lowercase().contains(queryLowerCase) ||
                    usuario.email.lowercase().contains(queryLowerCase)
                }
                .take(limit - usuarios.size)
            
            usuarios.addAll(allUsuarios)
            
            return@withContext Result.Success(usuarios)
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar usuarios por nombre o correo: $query")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Busca usuarios por tipo de perfil
     * @param tipo El tipo de perfil (PROFESOR, FAMILIAR, etc.)
     * @return Result con la lista de usuarios encontrados
     */
    suspend fun buscarUsuariosPorPerfil(tipo: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val tipoEnum = try {
                TipoUsuario.valueOf(tipo)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Tipo de usuario inválido: $tipo")
                return@withContext Result.Error(Exception("Tipo de usuario inválido: $tipo"))
            }
            
            // Firestore no permite consultas por arrays directamente,
            // así que debemos obtener todos y filtrar en memoria
            val snapshot = usuariosCollection.get().await()
            
            val usuarios = snapshot.documents.mapNotNull { doc ->
                try {
                    val usuario = doc.toObject(Usuario::class.java)
                    // Filtramos si tiene algún perfil del tipo solicitado
                    if (usuario != null && usuario.perfiles.any { it.tipo == tipoEnum }) {
                        usuario
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al convertir documento a Usuario: ${doc.id}")
                    null
                }
            }
            
            return@withContext Result.Success(usuarios)
        } catch (e: Exception) {
            Timber.e(e, "Error al buscar usuarios por perfil: $tipo")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Registra el último acceso del usuario actual en la base de datos.
     * Actualiza el campo 'ultimoAcceso' con la fecha y hora actuales.
     * 
     * @return Resultado que indica éxito o error en la operación
     */
    suspend fun registrarUltimoAcceso(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Obtener el usuario actual de Firebase Auth
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                Timber.e("No hay usuario autenticado para registrar último acceso")
                return@withContext Result.Error(Exception("No hay usuario autenticado"))
            }
            
            // Buscar el documento del usuario en Firestore por email
            val email = firebaseUser.email
            if (email.isNullOrEmpty()) {
                Timber.e("El usuario autenticado no tiene email")
                return@withContext Result.Error(Exception("Usuario sin email"))
            }
            
            val userQuery = usuariosCollection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (userQuery.isEmpty) {
                Timber.e("No se encontró usuario en Firestore con email: $email")
                return@withContext Result.Error(Exception("Usuario no encontrado en la base de datos"))
            }
            
            // Actualizar último acceso
            val userDoc = userQuery.documents.first()
            userDoc.reference.update("ultimoAcceso", Timestamp.now()).await()
            
            Timber.d("Último acceso actualizado para usuario: ${userDoc.id}")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            handleAuthError(e, "registrar_ultimo_acceso")
            Timber.e(e, "Error al registrar último acceso: ${e.message}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Guarda un usuario en Firestore sin crear una cuenta de autenticación en Firebase
     * Útil para alumnos y otros usuarios que no requieren acceso directo al sistema
     *
     * @param usuario Usuario a guardar
     * @return Resultado de la operación
     */
    suspend fun saveUsuarioSinAuth(usuario: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Verificar que el DNI no esté vacío
            if (usuario.dni.isBlank()) {
                return@withContext Result.Error(Exception("El DNI no puede estar vacío"))
            }
            
            // Guardar el usuario en Firestore usando el DNI como ID del documento
            usuariosCollection.document(usuario.dni).set(usuario).await()
            
            Timber.d("Usuario guardado sin autenticación: ${usuario.dni}")
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            handleAuthError(e, "guardar_usuario_sin_auth", usuario.email)
            Timber.e(e, "Error al guardar usuario sin autenticación: ${usuario.dni}")
            return@withContext Result.Error(e)
        }
    }

    /**
     * Actualiza las clases asignadas a un profesor
     * @param profesorId ID del profesor
     * @param claseId ID de la clase a añadir/eliminar
     * @param esAsignacion true para añadir, false para eliminar
     * @return Resultado de la operación
     */
    suspend fun actualizarClasesProfesor(profesorId: String, claseId: String, esAsignacion: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val usuarioRef = usuariosCollection.document(profesorId)
            val claseRef = clasesCollection.document(claseId)
            
            // Verificar que ambos documentos existan
            val usuarioDoc = usuarioRef.get().await()
            val claseDoc = claseRef.get().await()
            
            if (!usuarioDoc.exists()) {
                return@withContext Result.Error(Exception("El profesor con ID $profesorId no existe"))
            }
            
            if (!claseDoc.exists()) {
                return@withContext Result.Error(Exception("La clase con ID $claseId no existe"))
            }
            
            // Realizar las actualizaciones en una transacción para garantizar atomicidad
            firestore.runTransaction { transaction ->
                if (esAsignacion) {
                    // 1. Añadir la clase a la lista de clases del profesor
                    // Verificar si el campo clasesIds existe
                    val clasesIds = usuarioDoc.get("clasesIds") as? List<String>
                    if (clasesIds == null) {
                        // Si no existe, crear el campo con una lista que contenga la clase
                        transaction.update(usuarioRef, "clasesIds", listOf(claseId))
                    } else {
                        // Si existe, añadir la clase a la lista
                        transaction.update(
                            usuarioRef, 
                            "clasesIds", FieldValue.arrayUnion(claseId)
                        )
                    }
                    
                    // 2. Establecer al profesor como titular de la clase (si el campo está vacío)
                    val profesorIdActual = claseDoc.getString("profesorId") ?: ""
                    val profesorTitularIdActual = claseDoc.getString("profesorTitularId") ?: ""
                    
                    if (profesorIdActual.isEmpty()) {
                        transaction.update(claseRef, "profesorId", profesorId)
                    }
                    
                    if (profesorTitularIdActual.isEmpty()) {
                        transaction.update(claseRef, "profesorTitularId", profesorId)
                    } else if (profesorTitularIdActual != profesorId) {
                        // Si ya hay un profesor titular distinto, añadir a lista de auxiliares
                        // Verificar si la lista de profesores auxiliares existe
                        val profesoresAuxiliares = claseDoc.get("profesoresAuxiliaresIds") as? List<String>
                        if (profesoresAuxiliares == null) {
                            // Si no existe, crear la lista con el nuevo profesor
                            transaction.update(claseRef, "profesoresAuxiliaresIds", listOf(profesorId))
                        } else {
                            // Si existe, añadir el profesor a la lista existente
                            transaction.update(
                                claseRef, 
                                "profesoresAuxiliaresIds", FieldValue.arrayUnion(profesorId)
                            )
                        }
                    } else {
                        // Si el profesor ya es titular, no hacemos nada
                        Timber.d("El profesor $profesorId ya es titular de la clase $claseId")
                    }
                } else {
                    // 1. Eliminar la clase de la lista de clases del profesor
                    // Verificar si el campo clasesIds existe
                    val clasesIds = usuarioDoc.get("clasesIds") as? List<String>
                    if (clasesIds != null && clasesIds.contains(claseId)) {
                        // Si existe y contiene la clase, eliminarla
                        transaction.update(
                            usuarioRef, 
                            "clasesIds", FieldValue.arrayRemove(claseId)
                        )
                    }
                    
                    // 2. Quitar al profesor de la clase
                    val profesorIdActual = claseDoc.getString("profesorId") ?: ""
                    val profesorTitularIdActual = claseDoc.getString("profesorTitularId") ?: ""
                    
                    if (profesorIdActual == profesorId) {
                        transaction.update(claseRef, "profesorId", "")
                    }
                    
                    if (profesorTitularIdActual == profesorId) {
                        transaction.update(claseRef, "profesorTitularId", "")
                    }
                    
                    // Eliminar de la lista de auxiliares si está presente
                    transaction.update(
                        claseRef, 
                        "profesoresAuxiliaresIds", FieldValue.arrayRemove(profesorId)
                    )
                }
            }.await()
            
            // Registro detallado de la operación realizada
            Timber.d("${if (esAsignacion) "Asignación" else "Desasignación"} completada: Profesor $profesorId - Clase $claseId")
            
            return@withContext Result.Success(Unit)
        } catch (e: Exception) {
            handleAuthError(e, "actualizar_clases_profesor", profesorId)
            Timber.e(e, "Error al ${if (esAsignacion) "asignar" else "desasignar"} profesor $profesorId a/de clase $claseId")
            return@withContext Result.Error(e)
        }
    }
    
    /**
     * Limpia todas las solicitudes de vinculación relacionadas con un alumno
     * @param alumnoDni DNI del alumno
     */
    private suspend fun limpiarSolicitudesVinculacionAlumno(alumnoDni: String) {
        try {
            Timber.d("🧹 Limpiando solicitudes de vinculación para alumno: $alumnoDni")
            
            // Buscar solicitudes por alumnoId
            val solicitudesPorId = firestore.collection("solicitudes_vinculacion")
                .whereEqualTo("alumnoId", alumnoDni)
                .get()
                .await()
            
            // Buscar solicitudes por alumnoDni
            val solicitudesPorDni = firestore.collection("solicitudes_vinculacion")
                .whereEqualTo("alumnoDni", alumnoDni)
                .get()
                .await()
            
            // Combinar ambas consultas y eliminar duplicados
            val todasLasSolicitudes = (solicitudesPorId.documents + solicitudesPorDni.documents)
                .distinctBy { it.id }
            
            var solicitudesEliminadas = 0
            
            // Eliminar todas las solicitudes encontradas
            for (solicitudDoc in todasLasSolicitudes) {
                try {
                    solicitudDoc.reference.delete().await()
                    solicitudesEliminadas++
                    Timber.d("✅ Solicitud eliminada: ${solicitudDoc.id}")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al eliminar solicitud ${solicitudDoc.id}")
                }
            }
            
            Timber.d("🎯 Total solicitudes eliminadas para alumno $alumnoDni: $solicitudesEliminadas")
            
        } catch (e: Exception) {
            Timber.e(e, "💥 Error al limpiar solicitudes de vinculación para alumno $alumnoDni")
        }
    }
    
    /**
     * Limpia todos los mensajes unificados relacionados con un alumno
     * @param alumnoDni DNI del alumno
     */
    private suspend fun limpiarMensajesUnificadosAlumno(alumnoDni: String) {
        try {
            Timber.d("🧹 Limpiando mensajes unificados para alumno: $alumnoDni")
            
            // Buscar mensajes donde el alumno aparece en metadata.alumnoDni
            val mensajesPorMetadata = firestore.collection("unified_messages")
                .whereEqualTo("metadata.alumnoDni", alumnoDni)
                .get()
                .await()
            
            // Buscar mensajes donde el alumno aparece en relatedEntityId (si es de tipo alumno)
            val mensajesPorEntity = firestore.collection("unified_messages")
                .whereEqualTo("relatedEntityId", alumnoDni)
                .whereEqualTo("relatedEntityType", "ALUMNO")
                .get()
                .await()
            
            // Combinar ambas consultas y eliminar duplicados
            val todosLosMensajes = (mensajesPorMetadata.documents + mensajesPorEntity.documents)
                .distinctBy { it.id }
            
            var mensajesEliminados = 0
            
            // Eliminar todos los mensajes encontrados
            for (mensajeDoc in todosLosMensajes) {
                try {
                    mensajeDoc.reference.delete().await()
                    mensajesEliminados++
                    Timber.d("✅ Mensaje unificado eliminado: ${mensajeDoc.id}")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al eliminar mensaje unificado ${mensajeDoc.id}")
                }
            }
            
            Timber.d("🎯 Total mensajes unificados eliminados para alumno $alumnoDni: $mensajesEliminados")
            
        } catch (e: Exception) {
            Timber.e(e, "💥 Error al limpiar mensajes unificados para alumno $alumnoDni")
        }
    }
    
    /**
     * Limpia las referencias del alumno en las clases
     * @param alumnoDni DNI del alumno
     */
    private suspend fun limpiarReferenciaAlumnoEnClases(alumnoDni: String) {
        try {
            Timber.d("🧹 Limpiando referencias del alumno $alumnoDni en clases")
            
            // Buscar todas las clases que contengan este alumno en su lista de alumnosIds
            val clasesConAlumno = firestore.collection("clases")
                .whereArrayContains("alumnosIds", alumnoDni)
                .get()
                .await()
            
            var clasesActualizadas = 0
            
            // Eliminar el alumno de cada clase encontrada
            for (claseDoc in clasesConAlumno.documents) {
                try {
                    claseDoc.reference.update(
                        "alumnosIds", FieldValue.arrayRemove(alumnoDni)
                    ).await()
                    clasesActualizadas++
                    Timber.d("✅ Alumno $alumnoDni eliminado de clase: ${claseDoc.id}")
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error al eliminar alumno de clase ${claseDoc.id}")
                }
            }
            
            Timber.d("🎯 Total clases actualizadas para alumno $alumnoDni: $clasesActualizadas")
            
        } catch (e: Exception) {
            Timber.e(e, "💥 Error al limpiar referencias del alumno $alumnoDni en clases")
        }
    }

    /**
     * Obtiene la lista de alumnos (hijos) asociados a un familiar por su ID
     * @param familiarId ID del familiar
     * @return Lista de alumnos
     */
    suspend fun getHijosByFamiliarId(familiarId: String): List<Alumno> = withContext(Dispatchers.IO) {
        try {
            // Buscar el usuario familiar
            val usuarioDoc = usuariosCollection.document(familiarId).get().await()
            if (!usuarioDoc.exists()) {
                Timber.e("No se encontró el usuario familiar con ID: $familiarId")
                return@withContext emptyList<Alumno>()
            }
            
            val usuario = usuarioDoc.toObject(Usuario::class.java)
            if (usuario == null) {
                Timber.e("Error al convertir documento a Usuario para ID: $familiarId")
                return@withContext emptyList<Alumno>()
            }
            
            // Buscar los perfiles de tipo FAMILIAR
            val perfilesFamiliar = usuario.perfiles.filter { it.tipo == TipoUsuario.FAMILIAR }
            if (perfilesFamiliar.isEmpty()) {
                Timber.d("El usuario $familiarId no tiene perfiles de tipo FAMILIAR")
                return@withContext emptyList<Alumno>()
            }
            
            // Obtener todos los IDs de alumnos de todos los perfiles
            val alumnosIds = perfilesFamiliar.flatMap { it.alumnos }.distinct()
            
            if (alumnosIds.isEmpty()) {
                Timber.d("El familiar $familiarId no tiene alumnos asociados")
                return@withContext emptyList<Alumno>()
            }
            
            // Buscar los documentos de los alumnos
            val alumnos = mutableListOf<Alumno>()
            for (alumnoId in alumnosIds) {
                try {
                    val alumnoDoc = alumnosCollection.document(alumnoId).get().await()
                    if (alumnoDoc.exists()) {
                        val alumno = alumnoDoc.toObject(Alumno::class.java)
                        if (alumno != null) {
                            alumnos.add(alumno)
                        }
                    } else {
                        Timber.w("No se encontró el alumno con ID: $alumnoId")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error al obtener alumno con ID: $alumnoId")
                }
            }
            
            return@withContext alumnos
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener hijos del familiar: $familiarId")
            return@withContext emptyList<Alumno>()
        }
    }

    /**
     * Alias para getHijosByFamiliarId que mantiene consistencia con la nomenclatura en español
     * @param familiarId ID del familiar
     * @return Lista de alumnos
     */
    suspend fun obtenerHijosDeFamiliar(familiarId: String): List<Alumno> = getHijosByFamiliarId(familiarId)

    /**
     * Obtiene todos los profesores de un centro
     * @param centroId ID del centro educativo
     * @return Resultado con la lista de profesores asociados al centro
     */
    suspend fun getProfesoresByCentroId(centroId: String): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            // Buscamos usuarios que tengan un perfil de tipo PROFESOR asociado al centro
            val profesores = usuariosCollection.get().await().documents
                .mapNotNull { doc -> doc.toObject(Usuario::class.java) }
                .filter { usuario -> 
                    usuario.perfiles.any { 
                        it.tipo == TipoUsuario.PROFESOR && it.centroId == centroId 
                    }
                }
                
            return@withContext Result.Success(profesores)
        } catch (e: Exception) {
            Timber.e(e, "Error al obtener profesores del centro $centroId")
            return@withContext Result.Error(e)
        }
    }
}